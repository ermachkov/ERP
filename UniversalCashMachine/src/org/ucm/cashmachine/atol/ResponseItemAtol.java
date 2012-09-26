/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.atol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.ucm.cashmachine.CashMachineException;
import org.ucm.cashmachine.ResponseItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ResponseItemAtol implements ResponseItem {

    private boolean isError = true;
    private String commandName = "unknown", commandNameHuman = "unknown",
            errorString = "Unknown fatal error", value = "";

    private ResponseItemAtol() {
        //
    }

    public static ResponseItemAtol newResponseItemAtol(String commandName,
            Document commandXML, Document responseXML, ArrayList<Byte> list)
            throws XPathExpressionException, NullPointerException, CashMachineException {
        ResponseItemAtol ria = new ResponseItemAtol();
        ria.setInfo(commandName, commandXML, responseXML, list);
        return ria;

    }

    public void setInfo(String commandName, Document commandXML,
            Document responseXML, ArrayList<Byte> list)
            throws XPathExpressionException, NullPointerException, CashMachineException {

        Objects.requireNonNull(commandName, "Command name must not be null");
        Objects.requireNonNull(commandXML, "Command XML must not be null");
        Objects.requireNonNull(responseXML, "Response xml must not be null");
        Objects.requireNonNull(list, "List of response must not be null");

        this.commandName = commandName;
        XPath xPath = XPathFactory.newInstance().newXPath();

        String xPathString;
        if (commandName.startsWith("mode_")) {
            xPathString = "/commands/modes/" + commandName + "/response_ok/field[@pos='0']";

        } else {
            xPathString = "/commands/requests/" + commandName + "/response_ok/field[@pos='0']";
        }

        XPathExpression xPathExpression = xPath.compile(xPathString);
        Node node = (Node) xPathExpression.evaluate(commandXML, XPathConstants.NODE);
        String firstResponse = node.getAttributes().getNamedItem("value").getNodeValue();
        commandNameHuman = node.getAttributes().getNamedItem("name").getNodeValue();

        Logger.getGlobal().log(Level.INFO, "Command = {0}, response = {1}", new Object[]{commandName, list});

        if (list.isEmpty()) {
            isError = true;
            errorString = "Can't connect to cashmachine!";
            throw new CashMachineException("Can't connect to cashmachine!");

        } else {
            System.out.println("firstResponse >>>> " + firstResponse);
            switch (firstResponse) {
                case "85":
                    String errVal = Integer.toHexString(list.get(2));
                    if (errVal.length() == 1) {
                        errVal = "0x0" + errVal.toUpperCase();

                    } else if (errVal.length() == 2) {
                        errVal = "0x" + errVal.toUpperCase();

                    } else {
                        errVal = "0x" + errVal.substring(6).toUpperCase();
                    }

                    xPathString = "/errors/error[@code='" + errVal + "']";
                    System.out.println("xPathString = " + xPathString);
                    xPathExpression = xPath.compile(xPathString);
                    node = (Node) xPathExpression.evaluate(responseXML, XPathConstants.NODE);
                    if (node != null) {
                        errorString = node.getAttributes().getNamedItem("message").getNodeValue();
                    }

                    if (errVal.equals("0x00")) {
                        isError = false;
                    }

                    break;

                case "77":
                    List<Byte> sumDirtyList = list.subList(2, list.size() - 2);
                    System.out.println("sumDirtyList: " + sumDirtyList);

                    String str = "";
                    int index = 0;
                    for (Byte B : sumDirtyList) {
                        String s;
                        if (B == 0x10) {
                            if (index + 1 < sumDirtyList.size()) {
                                if (sumDirtyList.get(index + 1) == 0x10
                                        || sumDirtyList.get(index + 1) == 0x03) {
                                    index++;
                                    continue;
                                }
                            }
                        }

                        s = Integer.toHexString(0xFF & B.byteValue());
                        if (s.length() == 1) {
                            s = "0" + s;
                        }

                        str += s;
                        index++;
                    }

                    System.out.println("Result string: " + str);
                    value = str.substring(0, str.length() - 2) + "."
                            + str.substring(str.length() - 2, str.length());
                    errorString = "Ошибок нет";
                    break;
            }
        }

    }

    @Override
    public boolean isError() {
        return isError;
    }

    @Override
    public String getHumanCommand() {
        return commandNameHuman;
    }

    @Override
    public String getHumanError() {
        return errorString;
    }

    @Override
    public String getSystemCommandName() {
        return commandName;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ResponseItemAtol{" + "isError=" + isError + ", commandName="
                + commandName + ", commandNameHuman=" + commandNameHuman
                + ", errorString=" + errorString + ", value=" + value + '}';
    }
}
