/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.ucm.cashmachine.atol.KKMWorker;
import org.ucm.cashmachine.atol.ResponseItemAtol;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CashMachineAtol implements CashMachine {

    private static CashMachineAtol self = null;
    private KKMWorker kkmWorker;
    private Document commandXML, responseXML;
    private String innNumber = "000000000000";

    public CashMachineAtol(int speed, String port, int maxTextLen, String pathToConfig) {
        try {
            commandXML = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(Paths.get(pathToConfig, "Atol.xml").toString());
            responseXML = DocumentBuilderFactory.newInstance().
                    newDocumentBuilder().parse(Paths.get(pathToConfig, "AtolResponse.xml").toString());

            //kkmWorker = KKMWorker.getInstance(speed, port, maxTextLen, commandXML, responseXML, pathToConfig);
            kkmWorker = new KKMWorker(speed, port, maxTextLen, commandXML, responseXML, pathToConfig);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

//    public static CashMachineAtol getInstance(int speed, int password,
//            String port, int maxTextLen, String pathToConfig) {
//        if (self == null) {
//            self = new CashMachineAtol(speed, port, maxTextLen, pathToConfig);
//        }
//
//        return self;
//    }
    @Override
    public CashMachineResponse printXReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("mode_report_without_suppress", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_report_without_suppress", commandXML, responseXML, list));

        list = kkmWorker.singleAction("x-report", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("x-report", commandXML, responseXML, list));

        return cmr;
    }

    @Override
    public CashMachineResponse printZReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("mode_report_with_suppress", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_report_with_suppress", commandXML, responseXML, list));

        list = kkmWorker.singleAction("z-report", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("z-report", commandXML, responseXML, list));

        return cmr;
    }

    @Override
    public CashMachineResponse registerOperator(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("mode_register", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_register", commandXML, responseXML, list));

        return cmr;
    }

    @Override
    public BigDecimal getMoneyInCashBox(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("cash", password);
        ResponseItemAtol ria = ResponseItemAtol.newResponseItemAtol("cash", commandXML, responseXML, list);
        cmr.addResponseItem(ria);

        BigDecimal result = BigDecimal.ZERO;
        if (ria.getValue() != null) {
            if (!ria.getValue().equals("")) {
                result = new BigDecimal(ria.getValue());
            }
        }

        return result;
    }

    /**
     *
     * @param password
     * @return String array where<br/> [0]-year, [1]-month, [2]-day, [3]-hour,
     * [4]-min, [5]-kpk
     * @throws XPathExpressionException
     * @throws NullPointerException
     * @throws CashMachineException
     */
    @Override
    public String[] getEKLZInfo(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        String[] result = new String[6];

        int count = 0, maxCount = 3;
        boolean isDone = true;
        while (isDone) {
            if (count > maxCount) {
                result = new String[6];
                Arrays.fill(result, 0, 5, "0");
                break;
            }

            result = getEKLZForce(password);
            if (result.length == 6) {
                break;
            }

            LockSupport.parkNanos(1000000000);

            count++;
        }

        return result;


//        CashMachineResponse cmr = new CashMachineResponse();
//        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
//        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));
//
//        list = kkmWorker.singleAction("get_eklz_info", password);
//        ResponseItemAtol ria = ResponseItemAtol.newResponseItemAtol("get_eklz_info", commandXML, responseXML, list);
//        Logger.getGlobal().log(Level.INFO, "ResponseItemAtol {0}", ria);
//        cmr.addResponseItem(ria);
//
//        if (ria.getValue() == null) {
//            return new String[7];
//        }
//
//        System.out.println(">>> " + cmr.getResponseItemList());
//        System.out.println(">>> " + cmr.getErrorInfo());
//
//        Logger.getGlobal().log(Level.INFO, "cmr.getResponseItemList() = {0}", cmr.getResponseItemList());
//        Logger.getGlobal().log(Level.INFO, " cmr.getErrorInfo() = {0}", cmr.getErrorInfo());
//
//        String result[] = new String[7];
//        String s = ria.getValue().replaceAll("\\.", "");
//        result[0] = s.substring(12, 14); // year
//        result[1] = s.substring(14, 16); // month
//        result[2] = s.substring(16, 18); // day
//        result[3] = s.substring(18, 20); // hour
//        result[4] = s.substring(20, 22); // min
//        result[5] = s.substring(24, 32); // kpk
//
//        return result;
    }

    private String[] getEKLZForce(int password) {
        try {
            CashMachineResponse cmr = new CashMachineResponse();
            ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
            cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

            list = kkmWorker.singleAction("get_eklz_info", password);
            ResponseItemAtol ria = ResponseItemAtol.newResponseItemAtol("get_eklz_info", commandXML, responseXML, list);
            Logger.getGlobal().log(Level.INFO, "ResponseItemAtol {0}", ria);
            cmr.addResponseItem(ria);

            if (ria.getValue() == null) {
                return new String[1];
            }

            System.out.println(">>> " + cmr.getResponseItemList());
            System.out.println(">>> " + cmr.getErrorInfo());

            Logger.getGlobal().log(Level.INFO, "cmr.getResponseItemList() = {0}", cmr.getResponseItemList());
            Logger.getGlobal().log(Level.INFO, " cmr.getErrorInfo() = {0}", cmr.getErrorInfo());

            String result[] = new String[6];
            String s = ria.getValue().replaceAll("\\.", "");
            result[0] = s.substring(12, 14); // year
            result[1] = s.substring(14, 16); // month
            result[2] = s.substring(16, 18); // day
            result[3] = s.substring(18, 20); // hour
            result[4] = s.substring(20, 22); // min

            if (s.length() >= 32) {
                result[5] = s.substring(24, 32); // kpk
            }

            return result;

        } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return new String[1];
        }
    }

    @Override
    public String getCashMachineNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("get_kkm_number", password);
        ResponseItemAtol ria = ResponseItemAtol.newResponseItemAtol("get_kkm_number", commandXML, responseXML, list);
        cmr.addResponseItem(ria);

        if (ria.getValue() == null) {
            return "undefined";
        }

        //0000911517
        String s = ria.getValue().replaceAll("\\.", "");
        s = s.substring(3, s.length());

        return s;
    }

    /**
     *
     * @param password
     * @return String[0] - eklz number[10 digit], date[dd.MM.yy], shift
     * @throws XPathExpressionException
     * @throws NullPointerException
     * @throws CashMachineException
     */
    @Override
    public String[] getEKLZNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("get_eklz_number", password);
        ResponseItemAtol ria = ResponseItemAtol.newResponseItemAtol("get_eklz_number", commandXML, responseXML, list);
        cmr.addResponseItem(ria);

        if (ria.getValue() == null) {
            return new String[]{"undefined", "undefined", "undefined"};
        }

        System.out.println(">>> " + cmr.getResponseItemList());
        System.out.println(">>> " + cmr.getErrorInfo());

        String[] result = new String[3];
        result[0] = ria.getValue().replaceAll("\\.", "").substring(0, 12);
        result[1] = ria.getValue().replaceAll("\\.", "").substring(12, 18);
        result[2] = ria.getValue().replaceAll("\\.", "").substring(18,
                ria.getValue().replaceAll("\\.", "").length());

        return result;
    }

    /**
     *
     * @param password
     * @return String[], where String[0] - INN, String[1] - RNM
     * @throws XPathExpressionException
     * @throws NullPointerException
     * @throws CashMachineException
     */
    @Override
    public String[] getINN(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("get_inn_number", password);
        ResponseItemAtol ria = ResponseItemAtol.newResponseItemAtol("get_inn_number", commandXML, responseXML, list);
        cmr.addResponseItem(ria);

        if (ria.getValue() == null) {
            return new String[]{"undefined", "undefined"};
        }

        String result[] = new String[2];
        String s = ria.getValue().replaceAll("\\.", "");
        if(s.length() > 14){
            result[0] = s.substring(2, 14);
            innNumber = result[0];
            
        } else {
            result[0] = innNumber;
        }
        
        if(s.length() > 24){
            result[1] = s.substring(14, 24);
        } else {
            result[1] = "0000000000";
        }

        System.out.println(">>> " + cmr.getResponseItemList());
        System.out.println(">>> " + cmr.getErrorInfo());

        return result;
    }

    /**
     *
     * @param password
     * @return String[], where String[0] - receipt number, String[1] - document
     * number
     * @throws XPathExpressionException
     * @throws NullPointerException
     * @throws CashMachineException
     */
    @Override
    public String[] getDocumentNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        String[] result = new String[2];

        int count = 0, maxCount = 3;
        boolean isDone = true;
        while (isDone) {
            if (count > maxCount) {
                result = new String[2];
                Arrays.fill(result, 0, 1, "0");
                break;
            }

            result = getDocumentNumberForce(password);
            if (result.length == 2) {
                break;
            }

            LockSupport.parkNanos(1000000000);

            count++;
        }

        return result;

        //        CashMachineResponse cmr = new CashMachineResponse();
//        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
//        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));
//
//        list = kkmWorker.singleAction("get_reciept_numbers", password);
//        ResponseItemAtol ria = ResponseItemAtol.newResponseItemAtol("get_reciept_numbers", commandXML, responseXML, list);
//        cmr.addResponseItem(ria);
//
//        if (ria.getValue() == null) {
//            return new String[]{"undefined", "undefined"};
//        }
//
//        String s = ria.getValue().replaceAll("\\.", "");
//        String[] result = new String[2];
//        result[0] = s.substring(6, 10);
//        result[1] = s.substring(10, s.length());
//
//        return result;
    }

    private String[] getDocumentNumberForce(int password) {
        try {
            CashMachineResponse cmr = new CashMachineResponse();
            ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
            cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

            list = kkmWorker.singleAction("get_reciept_numbers", password);
            ResponseItemAtol ria = ResponseItemAtol.newResponseItemAtol("get_reciept_numbers", commandXML, responseXML, list);
            cmr.addResponseItem(ria);

            if (ria.getValue() == null) {
                return new String[]{"undefined", "undefined"};
            }

            String s = ria.getValue().replaceAll("\\.", "");
            String[] result = new String[2];
            result[0] = s.substring(6, 10);
            result[1] = s.substring(10, s.length());

            return result;

        } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return new String[1];
        }

    }

    @Override
    public CashMachineResponse depositionMoney(BigDecimal sum, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("mode_register", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_register", commandXML, responseXML, list));

        list = kkmWorker.depositionMoney(sum, password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("money_in", commandXML, responseXML, list));

        return cmr;
    }

    public CashMachineResponse eklzExecute(String hexValue, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("mode_eklz", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_eklz", commandXML, responseXML, list));

        list = kkmWorker.eklzExecute(hexValue, password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("eklz_execute", commandXML, responseXML, list));

        return cmr;
    }

    public CashMachineResponse eklzExecuteNext(String hexValue, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        ArrayList<Byte> list = kkmWorker.eklzExecute(hexValue, password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("eklz_execute", commandXML, responseXML, list));

        return cmr;
    }

    public CashMachineResponse getDocFromEKLZ(String number, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("mode_eklz", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_eklz", commandXML, responseXML, list));

        list = kkmWorker.getDocFromEKLZ(number, password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("eklz_execute", commandXML, responseXML, list));

        return cmr;
    }

    public CashMachineResponse printDocByKPK(String kpkNumber, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("mode_eklz", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_eklz", commandXML, responseXML, list));

        list = kkmWorker.printDocByKPK(kpkNumber, password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("print_doc_by_kpk", commandXML, responseXML, list));

        return cmr;
    }

    @Override
    public CashMachineResponse paymentMoney(BigDecimal sum, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("mode_register", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_register", commandXML, responseXML, list));

        list = kkmWorker.paymentMoney(sum, password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("money_out", commandXML, responseXML, list));

        return cmr;
    }

    @Override
    public boolean isConnect() {
        return kkmWorker.isConnect();
    }

    @Override
    public boolean disconnect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CashMachineResponse printString(String string, boolean isToBuffer, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        ArrayList<Byte> list = kkmWorker.printString(string, isToBuffer);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_register", commandXML, responseXML, list));

        return cmr;
    }

    @Override
    public CashMachineResponse printFiscalReceipt(Receipt receipt, BigDecimal cash,
            int paymentType, int section, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        ArrayList<Byte> list = kkmWorker.singleAction("mode_out", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML, responseXML, list));

        list = kkmWorker.singleAction("mode_register", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_register", commandXML, responseXML, list));

        list = kkmWorker.singleAction("open_sale_reciept", password);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("open_sale_reciept", commandXML, responseXML, list));

        for (ReceiptRow row : receipt.getReceiptRows()) {
            List<ResponseItemAtol> rlist = kkmWorker.printFiscalReceiptRow(row, section, password);
            cmr.getResponseItemList().addAll(rlist);
        }

        if (receipt.getTotalDiscountSum().doubleValue() > 0) {
            list = kkmWorker.setTotalDiscountSum(receipt.getTotalDiscountSum());
            cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("total_discount",
                    commandXML, responseXML, list));
        }

        list = kkmWorker.closeCouponWithDelivery(cash, paymentType);
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("close_reciept_with_delivery",
                commandXML, responseXML, list));
        
        kkmWorker.singleAction("cut_reciept", password);

        return cmr;
    }

    @Override
    public CashMachineResponse printFakeReceipt(String cashMachineName, ArrayList<Map<String, BigDecimal>> couponItems, BigDecimal cash) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CashMachineResponse cancelReceipt(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML,
                responseXML, kkmWorker.singleAction("mode_out", password)));
        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("cancel_reciept", commandXML,
                responseXML, kkmWorker.singleAction("cancel_reciept", password)));

        return cmr;
    }

    @Override
    public CashMachineResponse printZReportToBuffer(int password) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CashMachineResponse printZReportFromBuffer(int password) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CashMachineResponse openShift(String info, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_out", commandXML,
                responseXML, kkmWorker.singleAction("mode_out", password)));

        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("mode_register", commandXML,
                responseXML, kkmWorker.singleAction("mode_register", password)));

        cmr.addResponseItem(ResponseItemAtol.newResponseItemAtol("open_shift", commandXML,
                responseXML, kkmWorker.openShift(info, password)));
        return cmr;
    }

    @Override
    public CashMachineResponse closeShift(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = printZReport(password);
        return cmr;
    }
}
