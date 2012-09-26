/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.atol;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.ubo.money.Money;
import org.ucm.cashmachine.CashMachineException;
import org.ucm.cashmachine.ReceiptRow;
import org.w3c.dom.Document;

/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 29.03.2010
 */
public class KKMWorker {

    //private static KKMWorker self = null;
    private AtolDriver atolDriver;
    private Document commandXML, responseXML;
    private int MAX_TEXT_LENGHT = 20;

    public KKMWorker(int speed, String port, int maxTextLen, Document commandXML,
            Document responseXML, String pathToConfig) {
        //atolDriver = AtolDriver.getInstance(speed, port, pathToConfig);
        atolDriver = new AtolDriver(speed, port, pathToConfig);
        this.commandXML = commandXML;
        this.responseXML = responseXML;
        MAX_TEXT_LENGHT = maxTextLen;
    }

//    public synchronized static KKMWorker getInstance(int speed, String port,
//            int maxTextLen, Document commandXML, Document responseXML,
//            String pathToConfig) {
//        if (self == null) {
//            self = new KKMWorker(speed, port, maxTextLen, commandXML, responseXML, pathToConfig);
//        }
//
//        return self;
//    }
    public boolean isConnect() {
        return atolDriver.isConnected();
    }

    private boolean connect() {
        boolean isSuccess = true;
        if (!atolDriver.isConnected()) {
            try {
                isSuccess = atolDriver.connect();
            } finally {
                return isSuccess;
            }

        } else {
            return isSuccess;
        }
    }

    public ArrayList<Byte> singleAction(String cmd, int password) {
        ArrayList<Byte> list = new ArrayList<>();
        if (connect()) {
            list = atolDriver.executeCommand(cmd, password);
        }

        return list;
    }

    private List<Integer> prepareMoney(BigDecimal sum) {
        List<Integer> list = new ArrayList<>();
        String s = "";
        String strSum = Money.formatToMoney(sum.doubleValue());
        strSum = strSum.replaceAll("\\.", "");
        for (int i = strSum.length(); i < 10; i++) {
            s += "0";
        }

        strSum = s + strSum;

        list.add(Integer.valueOf(strSum.substring(0, 2), 16).intValue());
        list.add(Integer.valueOf(strSum.substring(2, 4), 16).intValue());
        list.add(Integer.valueOf(strSum.substring(4, 6), 16).intValue());
        list.add(Integer.valueOf(strSum.substring(6, 8), 16).intValue());
        list.add(Integer.valueOf(strSum.substring(8, 10), 16).intValue());

        return list;
    }

    private List<Integer> prepareQuantity(BigDecimal quantity) {
        List<Integer> list = new ArrayList<>();
        String s = "";
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(3);
        nf.setGroupingUsed(false);
        String strCount = nf.format(quantity.doubleValue()).replaceAll(",", "");
        for (int i = strCount.length(); i < 10; i++) {
            s += "0";
        }

        strCount = s + strCount;
        list.add(Integer.valueOf(strCount.substring(0, 2), 16).intValue());
        list.add(Integer.valueOf(strCount.substring(2, 4), 16).intValue());
        list.add(Integer.valueOf(strCount.substring(4, 6), 16).intValue());
        list.add(Integer.valueOf(strCount.substring(6, 8), 16).intValue());
        list.add(Integer.valueOf(strCount.substring(8, 10), 16).intValue());

        return list;
    }

    private int[] convertListToArray(ArrayList<Integer> list) {
        int[] array = new int[list.size()];
        int i = 0;
        for (int val : list) {
            array[i] = val;
            i++;
        }

        return array;
    }

    public ArrayList<Byte> printDocByKPK(String number, int password) {
        ArrayList<Byte> list = new ArrayList<>();
        if (connect()) {
            ArrayList<Integer> cmdList = new ArrayList<>();
            cmdList.add(0);
            cmdList.add(0);
            cmdList.add(171);

            cmdList.add(Integer.valueOf(number.substring(0, 2), 16).intValue());
            cmdList.add(Integer.valueOf(number.substring(2, 4), 16).intValue());
            cmdList.add(Integer.valueOf(number.substring(4, 6), 16).intValue());
            cmdList.add(Integer.valueOf(number.substring(6, 8), 16).intValue());

            list = atolDriver.executeCommand(
                    convertListToArray(cmdList), 
                    atolDriver.getTimeOut("print_doc_by_kpk"));
        }

        return list;
    }
    
    public ArrayList<Byte> eklzExecute(String hexValue, int password){
        ArrayList<Byte> list = new ArrayList<>();
        if (connect()) {
            ArrayList<Integer> cmdList = new ArrayList<>();
            cmdList.add(0);
            cmdList.add(0);
            cmdList.add(175);

            cmdList.add(Integer.valueOf(hexValue, 16).intValue());

            list = atolDriver.executeCommand(
                    convertListToArray(cmdList), 
                    atolDriver.getTimeOut("eklz_execute"));
        }

        return list;
    }
    
    public ArrayList<Byte> getDocFromEKLZ(String kpkNumber, int password){
        ArrayList<Byte> list = new ArrayList<>();
        if (connect()) {
            ArrayList<Integer> cmdList = new ArrayList<>();
            cmdList.add(0);
            cmdList.add(0);
            cmdList.add(175);

            cmdList.add(Integer.valueOf("21", 16).intValue());
            cmdList.add(Integer.valueOf(kpkNumber.substring(0, 2), 16).intValue());
            cmdList.add(Integer.valueOf(kpkNumber.substring(2, 4), 16).intValue());
            cmdList.add(Integer.valueOf(kpkNumber.substring(4, 6), 16).intValue());
            cmdList.add(Integer.valueOf(kpkNumber.substring(6, 8), 16).intValue());

            list = atolDriver.executeCommand(
                    convertListToArray(cmdList), 
                    atolDriver.getTimeOut("eklz_execute"));
        }

        return list;
    }

    public ArrayList<Byte> depositionMoney(BigDecimal sum, int password) {
        ArrayList<Byte> list = new ArrayList<>();
        if (connect()) {
            ArrayList<Integer> cmdList = new ArrayList<>();
            cmdList.add(0);
            cmdList.add(0);
            cmdList.add(73);
            cmdList.add(0);

            cmdList.addAll(prepareMoney(sum));

            list = atolDriver.executeCommand(convertListToArray(cmdList), atolDriver.getTimeOut("money_in"));
        }

        return list;
    }

    public ArrayList<Byte> paymentMoney(BigDecimal sum, int password) {
        ArrayList<Byte> list = new ArrayList<>();
        if (connect()) {
            ArrayList<Integer> cmdList = new ArrayList<>();
            cmdList.add(0);
            cmdList.add(0);
            cmdList.add(79);
            cmdList.add(0);

            cmdList.addAll(prepareMoney(sum));

            list = atolDriver.executeCommand(convertListToArray(cmdList), atolDriver.getTimeOut("money_out"));
        }

        return list;
    }

    public ArrayList<Byte> printString(String str, boolean isToBuffer) {
        ArrayList<Byte> list = new ArrayList<>();

        String printDirect = "0, "// system passwd
                + "0, " // system passwd
                + "135, "
                + "0, "// <Флаги (1)>
                + "1, "// Принтер (1)
                + "2, "// Шрифты (1)
                + "0, "// Множители (1)
                + "0, "// Межстрочие (1)
                + "0, "// Яркость (1)>
                + "1, "// РежимыЧЛ (1) если 0 печать в буфер
                + "1, "// РежимыКЛ (1)
                + "0, "// Форматирование (1)
                + "0, "// резерв
                + "0"; // резерв

        String printToBuffer = "0, "// system passwd
                + "0, " // system passwd
                + "135, "
                + "0, "// <Флаги (1)>
                + "1, "// Принтер (1)
                + "2, "// Шрифты (1)
                + "0, "// Множители (1)
                + "0, "// Межстрочие (1)
                + "0, "// Яркость (1)>
                + "1, "// РежимыЧЛ (1) если 0 печать в буфер
                + "1, "// РежимыКЛ (1)
                + "0, "// Форматирование (1)
                + "0, "// резерв
                + "0"; // резерв
        String commandString = isToBuffer ? printToBuffer : printDirect;

        try {
            byte b[] = str.getBytes("cp866");
            for (int i = 0; i < b.length; i++) {
                commandString = commandString + ", " + (b[i] & 0xFF);
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, str, e);
        }

        String arr[] = commandString.split(",");
        int cmd[] = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            cmd[i] = Integer.parseInt(arr[i].trim());
        }

        int timeout = atolDriver.getTimeOut("print_field");
        list = atolDriver.executeCommand(cmd, timeout);

        return list;
    }

    public List<ResponseItemAtol> printFiscalReceiptRow(ReceiptRow receiptRow,
            int section, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        List<ResponseItemAtol> responseList = new ArrayList<>();
        ArrayList<Byte> list = new ArrayList<>();

        String text = receiptRow.getItemName();
//        if(text.length() > MAX_TEXT_LENGHT){
//            text = text.substring(0, MAX_TEXT_LENGHT);
//        }


        if (text.length() > MAX_TEXT_LENGHT) {
            int n = text.length();

            int s = n % MAX_TEXT_LENGHT;
            int count = (n - s) / MAX_TEXT_LENGHT;
            for (int i = 0; i < count; i++) {
                int first;
                if (i == 0) {
                    first = i * MAX_TEXT_LENGHT;
                } else {
                    first = i * MAX_TEXT_LENGHT - 1;
                }

                int last = first + MAX_TEXT_LENGHT - 1;
                String txt = text.substring(first, last) + "-";
                list = printString(txt, true);
                responseList.add(ResponseItemAtol.newResponseItemAtol("print_field",
                        commandXML, responseXML, list));
            }

            String txt = text.substring((count * MAX_TEXT_LENGHT) - 2, text.length());
            list = printString(txt, true);
            responseList.add(ResponseItemAtol.newResponseItemAtol("print_field",
                    commandXML, responseXML, list));

        } else {
            list = printString(text, true);
            responseList.add(ResponseItemAtol.newResponseItemAtol("print_field",
                    commandXML, responseXML, list));
        }

//        list = printString(text, true);
//        responseList.add(ResponseItemAtol.newResponseItemAtol("print_field", 
//                commandXML, responseXML, list));

        ArrayList<Integer> cmdList = new ArrayList<>();
        cmdList.add(0);
        cmdList.add(0);
        cmdList.add(82);
        cmdList.add(0);

        cmdList.addAll(prepareMoney(receiptRow.getPrice()));
        cmdList.addAll(prepareQuantity(receiptRow.getQuantity()));

        String strSection = "" + section;
        if (strSection.length() == 1) {
            strSection = "0" + strSection;
        }
        cmdList.add(Integer.valueOf(strSection, 16).intValue());

        list = atolDriver.executeCommand(convertListToArray(cmdList), atolDriver.getTimeOut("register_sale_item"));
        responseList.add(ResponseItemAtol.newResponseItemAtol("register_sale_item",
                commandXML, responseXML, list));

        return responseList;
    }

    // paymentType = 1-cash, 2-4
    public ArrayList<Byte> closeCouponWithDelivery(BigDecimal cash, int paymentType) {
        ArrayList<Byte> list = new ArrayList<>();

        ArrayList<Integer> cmdList = new ArrayList<>();
        cmdList.add(0);
        cmdList.add(0);
        cmdList.add(74);
        cmdList.add(0);
        cmdList.add(paymentType);

        cmdList.addAll(prepareMoney(cash));

        list = atolDriver.executeCommand(convertListToArray(cmdList), atolDriver.getTimeOut("close_reciept_with_delivery"));

        return list;
    }

    public ArrayList<Byte> openShift(String info, int password) {
        ArrayList<Byte> list = new ArrayList<>();

        ArrayList<Integer> cmdList = new ArrayList<>();
        cmdList.add(0);
        cmdList.add(0);
        cmdList.add(154);
        cmdList.add(0);

        if (info.length() > MAX_TEXT_LENGHT) {
            info = info.substring(0, MAX_TEXT_LENGHT);
        }

        try {
            byte b[] = info.getBytes("cp866");
            for (int i = 0; i < b.length; i++) {
                cmdList.add(Integer.parseInt("" + (b[i] & 0xFF)));
            }

        } catch (UnsupportedEncodingException | NumberFormatException e) {
            Logger.getGlobal().log(Level.SEVERE, info, e);
        }

        list = atolDriver.executeCommand(convertListToArray(cmdList),
                atolDriver.getTimeOut("open_shift"));

        return list;
    }

    public ArrayList<Byte> setTotalDiscountSum(BigDecimal totalDiscountSum) {
        ArrayList<Byte> list = new ArrayList<>();
        ArrayList<Integer> cmdList = new ArrayList<>();
        cmdList.add(0);
        cmdList.add(0);
        cmdList.add(67);
        cmdList.add(0); // Флаги(1)
        cmdList.add(0); // Область(1) 0 – на весь чек, 1 – на последнюю операцию.
        cmdList.add(1); // Тип(1) 0 – процентная, 1 – суммой.
        cmdList.add(0); // Знак(1) 0 – скидка, 1 – надбавка.

        cmdList.addAll(prepareMoney(totalDiscountSum));

        list = atolDriver.executeCommand(convertListToArray(cmdList),
                atolDriver.getTimeOut("total_discount"));

        return list;
    }
}
