/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import org.ubo.utils.Result;
import org.ucm.cashmachine.pos.*;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CashMachinePos implements CashMachine {

    private Path pathToFiscalFolder, receiptsDir, zReportDir, xReportDir, archiveDir;
    private FiscalPrinter printer;
    private POSUtil posUtil;

    public CashMachinePos(int speed, String port, int textLen, String pathToKKMConfig) {
        pathToFiscalFolder = Paths.get(pathToKKMConfig);

        if (!pathToFiscalFolder.toFile().exists()) {
            pathToFiscalFolder.toFile().mkdirs();
        }

        receiptsDir = pathToFiscalFolder.resolve("receipts");
        if (!receiptsDir.toFile().exists()) {
            receiptsDir.toFile().mkdir();
        }

        zReportDir = pathToFiscalFolder.resolve("z-reports");
        if (!zReportDir.toFile().exists()) {
            zReportDir.toFile().mkdir();
        }

        xReportDir = pathToFiscalFolder.resolve("x-reports");
        if (!xReportDir.toFile().exists()) {
            xReportDir.toFile().mkdir();
        }

        archiveDir = pathToFiscalFolder.resolve("archive");
        if (!archiveDir.toFile().exists()) {
            archiveDir.toFile().mkdir();
        }

        if (port.trim().toLowerCase().startsWith("printer:")) {
            printer = new POSPrinter(pathToFiscalFolder, port.split(":")[1]);
            
        } else if (port.trim().toLowerCase().startsWith("html:")) {
            printer = new HTMLPrinter(pathToFiscalFolder, port.split(":")[1]);
        }

        posUtil = new POSUtil(pathToFiscalFolder);

    }

    @Override
    public CashMachineResponse printXReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        File files[] = xReportDir.toFile().listFiles();
        if (files.length == 0) {
            ZReport zReport = ZReport.getEmptyZReport();
            zReport.setCancelCount(0);
            zReport.setCash(posUtil.getCashForShift());
            zReport.setDate(new Date());
            zReport.setMoneyIn(posUtil.getMoneyInForShift());
            zReport.setMoneyOut(posUtil.getMoneyOutForShift());
            zReport.setMoneyOutCount(posUtil.getMoneyOutCountForShift());
            zReport.setNumber(posUtil.getNextZReportNumber(xReportDir));
            zReport.setReturnCount(0);
            zReport.setSalesSum(posUtil.getSalesSum());
            zReport.setSalesCount(posUtil.getSalesCountForShift());
            zReport.setStornoCount(0);
            zReport.setTotalCash(posUtil.getToatalCash(posUtil.getCashForShift()));
            zReport.setTotalSales(posUtil.getTotalSales(posUtil.getSalesSum()));

            Path zReportFile = zReport.createZReport(xReportDir);
            if (zReportFile != null) {
                Result r = printer.printXReport(zReportFile);
                if (r.isError()) {
                    PosResponseItem response = new PosResponseItem();
                    response.setHumanCommand("printXReport");
                    response.setHumanError(r.getReason());
                    response.setIsError(true);
                    response.setSystemCommandName("printXReport");
                    cmr.addResponseItem(response);

                } else {
                    PosResponseItem response = new PosResponseItem();
                    response.setHumanCommand("printXReport");
                    response.setHumanError("Ошибок нет");
                    response.setIsError(false);
                    response.setSystemCommandName("printXReport");
                    response.setValue("" + r.getObject());
                    cmr.addResponseItem(response);
                }
            }

        } else {
            if (receiptsDir.toFile().listFiles().length == 0) {
                PosResponseItem response = new PosResponseItem();
                response.setHumanCommand("printXReport");
                response.setHumanError("Смена закрыта");
                response.setIsError(true);
                response.setSystemCommandName("printXReport");
                cmr.addResponseItem(response);

            } else {
                ZReport zReport = ZReport.getEmptyZReport();
                zReport.setCancelCount(0);
                zReport.setCash(posUtil.getCashForShift());
                zReport.setDate(new Date());
                zReport.setMoneyIn(posUtil.getMoneyInForShift());
                zReport.setMoneyOut(posUtil.getMoneyOutForShift());
                zReport.setMoneyOutCount(posUtil.getMoneyOutCountForShift());
                zReport.setNumber(posUtil.getNextZReportNumber(xReportDir));
                zReport.setReturnCount(0);
                zReport.setSalesSum(posUtil.getSalesSum());
                zReport.setSalesCount(posUtil.getSalesCountForShift());
                zReport.setStornoCount(0);
                zReport.setTotalCash(posUtil.getToatalCash(posUtil.getCashForShift()));
                zReport.setTotalSales(posUtil.getTotalSales(posUtil.getSalesSum()));
                Path path = zReport.createZReport(xReportDir);
                if (path != null) {
                    Result r = printer.printXReport(path);
                    if (r.isError()) {
                        PosResponseItem response = new PosResponseItem();
                        response.setHumanCommand("printXReport");
                        response.setHumanError(r.getReason());
                        response.setIsError(true);
                        response.setSystemCommandName("printXReport");
                        cmr.addResponseItem(response);

                    } else {
                        PosResponseItem response = new PosResponseItem();
                        response.setHumanCommand("printXReport");
                        response.setHumanError("Ошибок нет");
                        response.setIsError(false);
                        response.setSystemCommandName("printXReport");
                        response.setValue("" + r.getObject());
                        cmr.addResponseItem(response);
                    }

                } else {
                    PosResponseItem response = new PosResponseItem();
                    response.setHumanCommand("printXReport");
                    response.setHumanError("Не могу записать X-отчет");
                    response.setIsError(true);
                    response.setSystemCommandName("printXReport");
                    cmr.addResponseItem(response);
                }
            }
        }

        return cmr;
    }

    @Override
    public CashMachineResponse printZReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        File files[] = zReportDir.toFile().listFiles();
        if (files.length == 0) {
            ZReport zReport = ZReport.getEmptyZReport();
            zReport.setCancelCount(0);
            zReport.setCash(posUtil.getCashForShift());
            zReport.setDate(new Date());
            zReport.setMoneyIn(posUtil.getMoneyInForShift());
            zReport.setMoneyOut(posUtil.getMoneyOutForShift());
            zReport.setMoneyOutCount(posUtil.getMoneyOutCountForShift());
            zReport.setNumber(posUtil.getNextZReportNumber(zReportDir));
            zReport.setReturnCount(0);
            zReport.setSalesSum(posUtil.getSalesSum());
            zReport.setSalesCount(posUtil.getSalesCountForShift());
            zReport.setStornoCount(0);
            zReport.setTotalCash(posUtil.getToatalCash(posUtil.getCashForShift()));
            zReport.setTotalSales(posUtil.getTotalSales(posUtil.getSalesSum()));
            Path zReportFile = zReport.createZReport(zReportDir);
            if (zReportFile != null) {
                Result r = printer.printZReport(zReportFile);
                if (r.isError()) {
                    PosResponseItem response = new PosResponseItem();
                    response.setHumanCommand("printZReport");
                    response.setHumanError(r.getReason());
                    response.setIsError(true);
                    response.setSystemCommandName("printZReport");
                    cmr.addResponseItem(response);
                } else {
                    PosResponseItem response = new PosResponseItem();
                    response.setHumanCommand("printZReport");
                    response.setHumanError("Ошибок нет");
                    response.setIsError(false);
                    response.setSystemCommandName("printZReport");
                    response.setValue("" + r.getObject());
                    cmr.addResponseItem(response);
                }
            }

        } else {
            if (receiptsDir.toFile().listFiles().length == 0) {
                PosResponseItem response = new PosResponseItem();
                response.setHumanCommand("printZReport");
                response.setHumanError("Смена закрыта");
                response.setIsError(true);
                response.setSystemCommandName("printZReport");
                cmr.addResponseItem(response);

            } else {
                ZReport zReport = ZReport.getEmptyZReport();
                zReport.setCancelCount(0);
                zReport.setCash(posUtil.getCashForShift());
                zReport.setDate(new Date());
                zReport.setMoneyIn(posUtil.getMoneyInForShift());
                zReport.setMoneyOut(posUtil.getMoneyOutForShift());
                zReport.setMoneyOutCount(posUtil.getMoneyOutCountForShift());
                zReport.setNumber(posUtil.getNextZReportNumber(zReportDir));
                zReport.setReturnCount(0);
                zReport.setSalesSum(posUtil.getSalesSum());
                zReport.setSalesCount(posUtil.getSalesCountForShift());
                zReport.setStornoCount(0);
                zReport.setTotalCash(posUtil.getToatalCash(posUtil.getCashForShift()));
                zReport.setTotalSales(posUtil.getTotalSales(posUtil.getSalesSum()));
                Path path = zReport.createZReport(zReportDir);
                if (path != null) {
                    Result r = printer.printZReport(path);
                    if (r.isError()) {
                        PosResponseItem response = new PosResponseItem();
                        response.setHumanCommand("printZReport");
                        response.setHumanError(r.getReason());
                        response.setIsError(true);
                        response.setSystemCommandName("printZReport");
                        cmr.addResponseItem(response);

                    } else {
                        PosResponseItem response = new PosResponseItem();
                        response.setHumanCommand("printZReport");
                        response.setHumanError("Ошибок нет");
                        response.setIsError(false);
                        response.setSystemCommandName("printZReport");
                        response.setValue("" + r.getObject());
                        cmr.addResponseItem(response);

                        Result rShiftArchive = posUtil.createShiftArchive();
                        if (rShiftArchive.isError()) {
                            response = new PosResponseItem();
                            response.setHumanCommand("printZReport");
                            response.setHumanError(r.getReason());
                            response.setIsError(true);
                            response.setSystemCommandName("printZReport");
                            cmr.addResponseItem(response);
                        }
                    }

                } else {
                    PosResponseItem response = new PosResponseItem();
                    response.setHumanCommand("printZReport");
                    response.setHumanError("Не могу записать Z-отчет");
                    response.setIsError(true);
                    response.setSystemCommandName("printZReport");
                    cmr.addResponseItem(response);
                }
            }
        }

        return cmr;
    }

    @Override
    public CashMachineResponse printZReportToBuffer(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CashMachineResponse printZReportFromBuffer(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CashMachineResponse printFiscalReceipt(Receipt receipt, BigDecimal cash,
            int paymentType, int section, int password) throws XPathExpressionException,
            NullPointerException, CashMachineException {

        CashMachineResponse cmr = new CashMachineResponse();
        ResponseItem responseItem = isShiftTimeout();
        if (responseItem.isError()) {
            cmr.addResponseItem(responseItem);
            return cmr;

        } else {

            if (cash.doubleValue() < POSUtil.getReceiptSum(receipt).doubleValue()) {
                PosResponseItem response = new PosResponseItem();
                response.setHumanCommand("getReceiptSum");
                response.setHumanError("Сумма платежа меньше суммы чека");
                response.setIsError(true);
                response.setSystemCommandName("checkSum");
                cmr.addResponseItem(response);
                return cmr;
            }

            PosReceipt posReceipt = PosReceipt.getEmptyPosReceipt();
            posReceipt.setCash(cash);
            posReceipt.setPassword(password);
            posReceipt.setPaymentType(paymentType);
            posReceipt.setReceipt(receipt);
            posReceipt.setSection(section);
            Path path = posReceipt.createReciept(receiptsDir,
                    posUtil.getNextReceiptNumber(), posUtil.getNextDocNumber());
            if (path != null) {
                Result r = printer.printReceipt(path);
                if (r.isError()) {
                    PosResponseItem response = new PosResponseItem();
                    response.setHumanCommand("printFiscalReceipt");
                    response.setHumanError(r.getReason());
                    response.setIsError(true);
                    response.setSystemCommandName("printFiscalReceipt");
                    cmr.addResponseItem(response);

                } else {
                    PosResponseItem response = new PosResponseItem();
                    response.setHumanCommand("printFiscalReceipt");
                    response.setHumanError("Ошибок нет");
                    response.setIsError(false);
                    response.setSystemCommandName("printFiscalReceipt");
                    response.setValue("" + r.getObject());
                    cmr.addResponseItem(response);
                }

            } else {
                PosResponseItem response = new PosResponseItem();
                response.setHumanCommand("printFiscalReceipt");
                response.setHumanError("Не удалось сделать запись");
                response.setIsError(true);
                response.setSystemCommandName("printFiscalReceipt");
                cmr.addResponseItem(response);
            }
            return cmr;
        }
    }

    @Override
    public CashMachineResponse printFakeReceipt(String cashMachineName, ArrayList<Map<String, BigDecimal>> couponItems, BigDecimal cash) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CashMachineResponse printString(String string, boolean isToBuffer, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CashMachineResponse registerOperator(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getCashMachineNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getDocumentNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BigDecimal getMoneyInCashBox(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return posUtil.getCashForShift();
    }

    @Override
    public CashMachineResponse depositionMoney(BigDecimal cash, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();

        PosReceipt posReceipt = PosReceipt.getEmptyPosReceipt();
        posReceipt.setCash(cash);
        posReceipt.setPassword(password);
        posReceipt.setReceiptType(2);
        Path path = posReceipt.createReciept(receiptsDir,
                posUtil.getNextReceiptNumber(), posUtil.getNextDocNumber());
        if (path == null) {
            PosResponseItem response = new PosResponseItem();
            response.setHumanCommand("depositionMoney");
            response.setHumanError("Не удалось сделать запись");
            response.setIsError(true);
            response.setSystemCommandName("depositionMoney");
            cmr.addResponseItem(response);

        } else {
            Result r = printer.printReceiptMoneyDeposition(path);
            if (r.isError()) {
                PosResponseItem response = new PosResponseItem();
                response.setHumanCommand("depositionMoney");
                response.setHumanError(r.getReason());
                response.setIsError(true);
                response.setSystemCommandName("depositionMoney");
                cmr.addResponseItem(response);

            } else {
                PosResponseItem response = new PosResponseItem();
                response.setHumanCommand("depositionMoney");
                response.setHumanError("Ошибок нет");
                response.setIsError(false);
                response.setSystemCommandName("depositionMoney");
                response.setValue("" + r.getObject());
                cmr.addResponseItem(response);
            }
        }

        return cmr;
    }

    @Override
    public CashMachineResponse paymentMoney(BigDecimal cash, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = new CashMachineResponse();
        
        BigDecimal cashBox = posUtil.getCashForShift();
        if(cashBox.doubleValue() < cash.doubleValue()){
            PosResponseItem response = new PosResponseItem();
            response.setHumanCommand("paymentMoney");
            response.setHumanError("Недостаточно наличных в кассе");
            response.setIsError(true);
            response.setSystemCommandName("paymentMoney");
            cmr.addResponseItem(response);
            
            return cmr;
        }

        PosReceipt posReceipt = PosReceipt.getEmptyPosReceipt();
        posReceipt.setCash(cash);
        posReceipt.setPassword(password);
        posReceipt.setReceiptType(3);
        Path path = posReceipt.createReciept(receiptsDir,
                posUtil.getNextReceiptNumber(), posUtil.getNextDocNumber());

        if (path == null) {
            PosResponseItem response = new PosResponseItem();
            response.setHumanCommand("paymentMoney");
            response.setHumanError("Не удалось сделать запись");
            response.setIsError(true);
            response.setSystemCommandName("paymentMoney");
            cmr.addResponseItem(response);

        } else {
            Result r = printer.printReceiptMoneyOut(path);
            if (r.isError()) {
                PosResponseItem response = new PosResponseItem();
                response.setHumanCommand("paymentMoney");
                response.setHumanError(r.getReason());
                response.setIsError(true);
                response.setSystemCommandName("paymentMoney");
                cmr.addResponseItem(response);

            } else {
                PosResponseItem response = new PosResponseItem();
                response.setHumanCommand("paymentMoney");
                response.setHumanError("Ошибок нет");
                response.setIsError(false);
                response.setSystemCommandName("paymentMoney");
                response.setValue("" + r.getObject());
                cmr.addResponseItem(response);
            }
        }

        return cmr;
    }

    @Override
    public CashMachineResponse cancelReceipt(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return PosResponse.getNoErrorsCashMachineResponse();
    }

    @Override
    public CashMachineResponse openShift(String info, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CashMachineResponse closeShift(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isConnect() {
        return true;
    }

    @Override
    public boolean disconnect() {
        return true;
    }

    private ResponseItem isShiftTimeout() {
        PosResponseItem response;
        boolean result = false;

        ZReport zReport = getLastZReport();
        System.out.println("getLastZReport() = " + zReport);
        if (zReport != null) {
            long t = zReport.getDate().getTime() + (24l * 60l * 60l * 1000l);
            long now = new Date().getTime();
            System.out.println("Last Z-report time = " + t + ", time now = " + now);
            if(now < t){
                result = true;
            }
        }

        if (result) {
            response = new PosResponseItem();
            response.setHumanCommand("isShiftTimeout");
            response.setHumanError("Ошибок нет");
            response.setIsError(false);
            response.setSystemCommandName("isShiftTimeout");

        } else {
            response = new PosResponseItem();
            response.setHumanCommand("isShiftTimeout");
            response.setHumanError("Смена превысила 24 часа");
            response.setIsError(true);
            response.setSystemCommandName("isShiftTimeout");
        }

        return response;
    }

    private ZReport getLastZReport() {
        long time = 0;
        ZReport findZReport = null;
        for (File f : zReportDir.toFile().listFiles()) {
            ZReport zReport = ZReport.getFromJSON(f.toPath());
            if (zReport.getDate().getTime() > time) {
                findZReport = zReport;
            }
        }

        return findZReport;
    }

    @Override
    public String[] getEKLZNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getINN(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getEKLZInfo(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
