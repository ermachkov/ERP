/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.pos;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.money.Money;
import org.ubo.utils.Result;
import org.ucm.cashmachine.ReceiptRow;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class HTMLPrinter implements FiscalPrinter {

    private Path rootPath;
    private String htmlOutputFolder;

    public HTMLPrinter(Path pathToFiscalFolder, String htmlOutputFolder) {
        this.rootPath = pathToFiscalFolder;
        this.htmlOutputFolder = htmlOutputFolder;
        init();
    }

    private void init() {
        Path pSettings = rootPath.resolve("settings");

        if (!pSettings.toFile().exists()) {
            pSettings.toFile().mkdir();
            createDefaultSetting(pSettings);
        }

        Path pConf = pSettings.resolve("settings.json");
        if (!pConf.toFile().exists()) {
            createDefaultSetting(pSettings);
        }
    }

    private void createDefaultSetting(Path pSettings) {
        try {
            JSONObject jsonConf = new JSONObject();
            jsonConf.put("paddingLeft", 0);
            jsonConf.put("paddingTop", 0);
            jsonConf.put("fontSize", 8);
            jsonConf.put("printAreaTop", 0);
            jsonConf.put("printAreaLeft", 0);
            jsonConf.put("printAreaWidth", 80);
            jsonConf.put("printAreaHeight", 160);
            jsonConf.put("pageWidth", 80);
            jsonConf.put("pageHeight", 160);

            String s = jsonConf.toString().replaceAll(",", ",\r\n");

            Files.write(pSettings.resolve("settings.json"), s.getBytes());
        } catch (JSONException | IOException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }

    }

    /**
     *
     * @param zReportFile
     * @return if success will return Result without error contain Path to
     * z-report in html, otherwise will return Result with error
     */
    @Override
    public Result printZReport(Path zReportFile) {
        try {
            ZReport zReport = ZReport.getFromJSON(zReportFile);

            List<String> lines = Files.readAllLines(rootPath.resolve(
                    Paths.get("templates", "z-report.html")), Charset.defaultCharset());

            String zReportString = "";
            for (String line : lines) {
                line = line.replaceAll("\\{number\\}", "" + zReport.getNumber());
                line = line.replaceAll("\\{date\\}", DateTime.getFormatedDate("dd.MM.yyyy HH:mm:ss", zReport.getDate()));
                line = line.replaceAll("\\{salesCount\\}", "" + zReport.getSalesCount());
                line = line.replaceAll("\\{moneyInCount\\}", "" + zReport.getMoneyInCount());
                line = line.replaceAll("\\{moneyOutCount\\}", "" + zReport.getMoneyOutCount());
                line = line.replaceAll("\\{returnCount\\}", "" + zReport.getReturnCount());
                line = line.replaceAll("\\{stornoCount\\}", "" + zReport.getStornoCount());
                line = line.replaceAll("\\{cancelCount\\}", "" + zReport.getCancelCount());
                line = line.replaceAll("\\{moneyOut\\}", "" + zReport.getMoneyOut());
                line = line.replaceAll("\\{moneyIn\\}", "" + zReport.getMoneyIn());
                line = line.replaceAll("\\{cash\\}", "" + zReport.getCash());
                line = line.replaceAll("\\{sales\\}", "" + zReport.getSalesSum());
                line = line.replaceAll("\\{totalSales\\}", "" + zReport.getTotalSales());
                line = line.replaceAll("\\{totalCash\\}", "" + zReport.getTotalCash());

                zReportString += line + "\r\n";
            }

            String fileName = "" + new Date().getTime() + ".html";
            Path pOut = Paths.get(htmlOutputFolder, "z_report_" + fileName);
            Files.write(pOut, zReportString.getBytes());

            return Result.newResultSuccess(pOut);

        } catch (Exception ex) {
            Logger.getLogger(POSPrinter.class.getName()).log(Level.SEVERE, null, ex);
            return Result.newResult(false, ex.toString());
        }
    }

    /**
     *
     * @param receiptsFile
     * @return if success will return Result without error contain Path to
     * receipt in html, otherwise will return Result with error
     */
    @Override
    public Result printReceipt(Path receiptsFile) {
        try {
            PosReceipt posReceipt = PosReceipt.getFromJSON(receiptsFile);

            List<String> lines = Files.readAllLines(rootPath.resolve(
                    Paths.get("templates", "receipt.html")), Charset.defaultCharset());

            String receipt = "";
            for (String line : lines) {
                line = line.replaceAll("\\{receiptRows\\}", getRecieptRows(posReceipt));
                line = line.replaceAll("\\{totalDiscountSum\\}",
                        "" + posReceipt.getTotalDiscountSum());
                line = line.replaceAll("\\{totalSum\\}",
                        "" + POSUtil.getReceiptSum(posReceipt.getReceipt()));
                line = line.replaceAll("\\{cash\\}",
                        "" + posReceipt.getCash());
                line = line.replaceAll("\\{rest\\}",
                        "" + Money.SUBSTRACT(
                        posReceipt.getCash(),
                        POSUtil.getReceiptSum(posReceipt.getReceipt())));
                line = line.replaceAll("\\{fiscal\\}", getFiscalString(posReceipt));

                receipt += line + "\r\n";
            }

            String fileName = "" + new Date().getTime() + ".html";
            Path pOut = Paths.get(htmlOutputFolder, "receipt_" + fileName);

            Files.write(pOut, receipt.getBytes());

            return Result.newResultSuccess(pOut);

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return Result.newResultError(e.toString());
        }
    }

    private String getFiscalString(PosReceipt posReceipt) {
        String s = ""
                + zeroLead(posReceipt.getNumbeReceipt())
                + " " + zeroLead(posReceipt.getNumberDoc())
                + " " + posReceipt.getSection()
                + " K" + posReceipt.getPassword();

        switch (posReceipt.getReceiptType()) {
            case PosReceipt.TYPE_SALE:
                s = ""
                        + "<div>"
                        + "<div style='float:left;'>" + s + "</div>"
                        + "<div style='float:right;'><strong>&#931;&nbsp;"
                        + POSUtil.getReceiptSum(posReceipt.getReceipt()) + "</strong></div>"
                        + "</div>";
                break;

            case PosReceipt.TYPE_MONEY_IN:
                s = ""
                        + "<div>"
                        + "<div style='float:left;'>" + s + "</div>"
                        + "<div style='float:right;'><strong>&#931;&nbsp;"
                        + posReceipt.getCash() + "</strong></div>"
                        + "</div>";
                break;

            case PosReceipt.TYPE_MONEY_OUT:
                s = ""
                        + "<div>"
                        + "<div style='float:left;'>" + s + "</div>"
                        + "<div style='float:right;'><strong>&#931;&nbsp;"
                        + posReceipt.getCash() + "</strong></div>"
                        + "</div>";
                break;
        }

        return s;
    }

    private String zeroLead(int val) {
        String s = "" + val;
        int count = 5 - s.length();
        String z = "";
        for (int i = 0; i < count; i++) {
            z += "0";
        }

        return z + s;
    }

    private String getRecieptRows(PosReceipt posReceipt) {
        String s = ""
                + "<table width='100%' cellpadding='2' cellspacing='0'>"
                + "<tr>"
                + "<td><strong>Наименование</strong></td>"
                + "<td align='right'><strong>Цена</strong></td>"
                + "<td align='right'><strong>Кол-во</strong></td>"
                + "<td align='right'><strong>Сумма</strong></td>"
                + "</tr>";

        for (ReceiptRow receiptRow : posReceipt.getReceipt().getReceiptRows()) {
            s += ""
                    + "<tr>"
                    + "<td>" + receiptRow.getItemName() + "</td>"
                    + "<td align='right'>" + receiptRow.getPrice() + "</td>"
                    + "<td align='right'>x" + receiptRow.getQuantity() + "</td>"
                    + "<td align='right'>=" + Money.MULTIPLY(receiptRow.getPrice(), receiptRow.getQuantity()) + "</td>"
                    + "</tr>";
        }

        return s + "</table>";
    }

    @Override
    public Result printReceiptMoneyDeposition(Path receiptsFile) {
        try {
            PosReceipt posReceipt = PosReceipt.getFromJSON(receiptsFile);

            List<String> lines = Files.readAllLines(rootPath.resolve(
                    Paths.get("templates", "receipt_money_deposition.html")), Charset.defaultCharset());

            String receipt = "";
            for (String line : lines) {
                line = line.replaceAll("\\{cash\\}",
                        "" + posReceipt.getCash());
                line = line.replaceAll("\\{fiscal\\}", getFiscalString(posReceipt));

                receipt += line + "\r\n";
            }

            String fileName = "" + new Date().getTime() + ".html";
            Path pOut = Paths.get(htmlOutputFolder, "receipt_" + fileName);

            Files.write(pOut, receipt.getBytes());

            return Result.newResultSuccess(pOut);

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return Result.newResultError(e.toString());
        }
    }

    @Override
    public Result printReceiptMoneyOut(Path receiptsFile) {
        try {
            PosReceipt posReceipt = PosReceipt.getFromJSON(receiptsFile);

            List<String> lines = Files.readAllLines(rootPath.resolve(
                    Paths.get("templates", "receipt_money_out.html")), Charset.defaultCharset());

            String receipt = "";
            for (String line : lines) {
                line = line.replaceAll("\\{cash\\}",
                        "" + posReceipt.getCash());
                line = line.replaceAll("\\{fiscal\\}", getFiscalString(posReceipt));

                receipt += line + "\r\n";
            }

            String fileName = "" + new Date().getTime() + ".html";
            Path pOut = Paths.get(htmlOutputFolder, "receipt_" + fileName);

            Files.write(pOut, receipt.getBytes());

            return Result.newResultSuccess(pOut);

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return Result.newResultError(e.toString());
        }
    }

    @Override
    public Result printXReport(Path xReportFile) {
        try {
            ZReport xReport = ZReport.getFromJSON(xReportFile);

            List<String> lines = Files.readAllLines(rootPath.resolve(
                    Paths.get("templates", "x-report.html")), Charset.defaultCharset());

            String zReport = "";
            for (String line : lines) {
                line = line.replaceAll("\\{number\\}", "" + xReport.getNumber());
                line = line.replaceAll("\\{date\\}", DateTime.getFormatedDate("dd.MM.yyyy HH:mm:ss", xReport.getDate()));
                line = line.replaceAll("\\{salesCount\\}", "" + xReport.getSalesCount());
                line = line.replaceAll("\\{moneyInCount\\}", "" + xReport.getMoneyInCount());
                line = line.replaceAll("\\{moneyOutCount\\}", "" + xReport.getMoneyOutCount());
                line = line.replaceAll("\\{returnCount\\}", "" + xReport.getReturnCount());
                line = line.replaceAll("\\{stornoCount\\}", "" + xReport.getStornoCount());
                line = line.replaceAll("\\{cancelCount\\}", "" + xReport.getCancelCount());
                line = line.replaceAll("\\{moneyOut\\}", "" + xReport.getMoneyOut());
                line = line.replaceAll("\\{moneyIn\\}", "" + xReport.getMoneyIn());
                line = line.replaceAll("\\{cash\\}", "" + xReport.getCash());
                line = line.replaceAll("\\{sales\\}", "" + xReport.getSalesSum());
                line = line.replaceAll("\\{totalSales\\}", "" + xReport.getTotalSales());
                line = line.replaceAll("\\{totalCash\\}", "" + xReport.getTotalCash());

                zReport += line + "\r\n";
            }

            String fileName = "" + new Date().getTime() + ".html";
            Path pOut = Paths.get(htmlOutputFolder, "x_report_" + fileName);
            Files.write(pOut, zReport.getBytes());

            return Result.newResultSuccess(pOut);

        } catch (Exception ex) {
            Logger.getLogger(POSPrinter.class.getName()).log(Level.SEVERE, null, ex);
            return Result.newResult(false, ex.toString());
        }
    }
}
