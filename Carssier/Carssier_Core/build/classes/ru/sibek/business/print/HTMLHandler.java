/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.print;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.krysalis.barcode4j.BarcodeException;
import org.ubg.barcode.Barcode;
import org.ubo.document.Document;
import org.ubo.document.Order;
import org.ubo.money.Money;
import org.ubo.utils.NumberToWords;
import org.xml.sax.SAXException;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class HTMLHandler implements HTMLDocHandler {

    public Document doc;

    @Override
    public String getBarcode(String height, String moduleWidth, String orientation) {
        String code = Objects.toString(((Order) doc).getId());
        int c = 12 - code.length();
        StringBuilder sb = new StringBuilder("900000000000");
        sb = sb.replace(c, 12, code);
        Path p = Paths.get(System.getProperty("user.home"), ".saas", "app",
                "tmp", "barcode.png");
        try {
            int h = Integer.parseInt(height);
            int orient = Integer.parseInt(orientation);
            Logger.getGlobal().info("Start barcode generation...");
            Barcode.EAN13(sb.toString(), h, Double.parseDouble(moduleWidth), 300, orient, p);
            Logger.getGlobal().info("Barcode generation complite.");

        } catch (NumberFormatException | SAXException | IOException | org.apache.avalon.framework.configuration.ConfigurationException | BarcodeException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }

        return "file://" + p.toString();
    }

    @Override
    public String getHTML(Document doc, String pathToTemplate) {
        this.doc = doc;
        String html = "";
        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(pathToTemplate), Charset.forName("utf-8"));
            String str;
            while ((str = br.readLine()) != null) {
                Pattern p = Pattern.compile("\\{(.*?)\\}");
                Matcher matcher = p.matcher(str);
                while (matcher.find()) {
                    String match = matcher.group();
                    match = match.substring(1);
                    match = match.substring(0, match.length() - 1);
                    if (!match.equals("")) {
                        String replace = matcher.replaceAll(execute(match));
                        str = replace;
                    }
                }

                html += str + "\n";
            }

        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, pathToTemplate, ex);
        }

        return html;
    }

    public abstract String getHTMLTable();

    public String getTotalSumString() {
        String str = NumberToWords.convert(((Order) doc).getTotalWithTotalDiscount().intValue()) + " руб. ";
        String c = Money.formatToMoney(((Order) doc).getTotalWithTotalDiscount().doubleValue());
        String arr[] = c.split("\\.");
        str = str + NumberToWords.convert(Integer.parseInt(arr[1])) + " коп.";
        return str;
    }

    public String execute(String exec) {
        LinkedList<String> methodsName = getMethods(exec);
        LinkedList<String> parameters = getParameters(exec);

        if (isClassSelf(exec)) {
            return MethodsExecutor.execute(methodsName, parameters, this);

        } else {
            return MethodsExecutor.execute(methodsName, parameters, doc);
        }
    }

    public boolean isClassSelf(String str) {
        boolean result = false;
        String arr[] = str.split("\\.");
        String className = "";
        boolean isFirst = true;
        for (String s : arr) {
            if (s.indexOf("(") != -1) {
                break;
            }

            if (isFirst) {
                className += s;
                isFirst = false;
            } else {
                className += "." + s;
            }
        }

        if (getClass().getName().equals(className)) {
            result = true;
        }

        return result;
    }

    public LinkedList getParameters(String str) {
        LinkedList<String> list = new LinkedList<>();
        String arr[] = str.split("\\(");
        if (arr.length != 2) {
            return list;
        }

        String ps = arr[1].substring(0, arr[1].length() - 1);
        String arrps[] = ps.split(",\\s");
        for (String sps : arrps) {
            list.add(sps.trim());
        }

        int emptyCount = 0;
        for (String s : list) {
            if (s.equals("")) {
                emptyCount++;
            }
        }

        if (emptyCount == list.size()) {
            list = new LinkedList<>();
        }

        return list;
    }

    public LinkedList getMethods(String str) {
        LinkedList<String> list = new LinkedList<>();
        String arr[] = str.split("\\.");
        for (String s : arr) {
            if (s.indexOf("(") != -1) {
                list.add(s.substring(0, s.indexOf("(")));
            }
        }
        return list;
    }
}
