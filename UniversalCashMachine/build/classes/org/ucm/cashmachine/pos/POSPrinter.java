/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.pos;

import java.awt.*;
import java.awt.print.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
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
public class POSPrinter implements Printable, FiscalPrinter {

    private Path rootPath;
    private int fontSize, printAreaTop, printAreaLeft, printAreaWidth,
            printAreaHeight, pageWidth, pageHeight;
    private double paddingLeft, paddingTop;
    private List<String> printStringList = new ArrayList<>();
    private String printerName = "";

    public POSPrinter(Path rootPath, String printerName) {
        this.printerName = printerName;
        this.rootPath = rootPath;
        init();
    }

    private void init() {
        try {
            Path pSettings = rootPath.resolve("settings");
            JSONObject jsonConf;

            if (!pSettings.toFile().exists()) {
                pSettings.toFile().mkdir();
                createDefaultSetting(pSettings);
            }

            Path pConf = pSettings.resolve("settings.json");
            if (!pConf.toFile().exists()) {
                createDefaultSetting(pSettings);
            }

            jsonConf = new JSONObject(new String(Files.readAllBytes(pConf)));
            jsonConf.put("printerName", printerName);
            
            paddingLeft = jsonConf.getDouble("paddingLeft") * 2.857142857;
            paddingTop = jsonConf.getDouble("paddingTop");
            fontSize = jsonConf.getInt("fontSize");

            printAreaTop = jsonConf.getInt("printAreaTop");
            printAreaLeft = jsonConf.getInt("printAreaLeft");
            printAreaWidth = jsonConf.getInt("printAreaWidth");
            printAreaHeight = jsonConf.getInt("printAreaHeight");

            printAreaHeight = jsonConf.getInt("printAreaHeight");
            printAreaHeight = jsonConf.getInt("printAreaHeight");

            pageWidth = jsonConf.getInt("pageWidth");
            pageHeight = jsonConf.getInt("pageHeight");

        } catch (JSONException | IOException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
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
            jsonConf.put("printerName", "");
            Files.write(pSettings.resolve("settings.json"), jsonConf.toString().getBytes());
            
        } catch (JSONException | IOException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }

    }

    @Override
    public Result printZReport(Path zReportFile) {
        try {
            ZReport zReport = ZReport.getFromJSON(zReportFile);

            List<String> lines = Files.readAllLines(rootPath.resolve(
                    Paths.get("templates", "z-report.txt")), Charset.defaultCharset());

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

            printStringList = Arrays.asList(zReportString.split("\r\n"));
            printDoc();
            return Result.newEmptySuccess();

        } catch (Exception ex) {
            Logger.getLogger(POSPrinter.class.getName()).log(Level.SEVERE, null, ex);
            return Result.newResult(false, ex.toString());
        }
    }
    
    @Override
    public Result printXReport(Path xReportFile) {
        try {
            ZReport xReport = ZReport.getFromJSON(xReportFile);

            List<String> lines = Files.readAllLines(rootPath.resolve(
                    Paths.get("templates", "x-report.txt")), Charset.defaultCharset());

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

            printStringList = Arrays.asList(zReport.split("\r\n"));
            printDoc();
            return Result.newEmptySuccess();

        } catch (Exception ex) {
            Logger.getLogger(POSPrinter.class.getName()).log(Level.SEVERE, null, ex);
            return Result.newResult(false, ex.toString());
        }
    }
    
    @Override
    public Result printReceipt(Path receiptsFile) {
        try {
            PosReceipt posReceipt = PosReceipt.getFromJSON(receiptsFile);

            List<String> lines = Files.readAllLines(rootPath.resolve(
                    Paths.get("templates", "receipt.txt")), Charset.defaultCharset());

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
            
            printStringList = Arrays.asList(receipt.split("\r\n"));
            printDoc();
            return Result.newEmptySuccess();

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return Result.newResultError(e.toString());
        }
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
    
    private String getFiscalString(PosReceipt posReceipt) {
        String s = "\r\n"
                + zeroLead(posReceipt.getNumbeReceipt())
                + " " + zeroLead(posReceipt.getNumberDoc())
                + " " + posReceipt.getSection()
                + " K" + posReceipt.getPassword();

        switch (posReceipt.getReceiptType()) {
            case PosReceipt.TYPE_SALE:
                s = s + " ==" + POSUtil.getReceiptSum(posReceipt.getReceipt());
                break;

            case PosReceipt.TYPE_MONEY_IN:
                s = s + posReceipt.getCash();
                break;

            case PosReceipt.TYPE_MONEY_OUT:
                s = s + posReceipt.getCash();
                break;
        }

        return s;
    }
    
    private String getRecieptRows(PosReceipt posReceipt) {
        String s = "";

        for (ReceiptRow receiptRow : posReceipt.getReceipt().getReceiptRows()) {
            s += receiptRow.getItemName() + "\r\n"
                    + receiptRow.getPrice() + " x " + receiptRow.getQuantity() 
                    + "=" + Money.MULTIPLY(receiptRow.getPrice(), receiptRow.getQuantity())
                    + "\r\n";
        }

        return s;
    }

    private void printDoc() {
        try {
            PrintService printService = null;
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
            for (PrintService ps : printServices) {
                if (ps.getName().equals(printerName)) {
                    printService = ps;
                    break;
                }
            }

            if (printService == null) {
                return;
            }

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintService(printService);
            CustomPaper customPaper = new CustomPaper(pageWidth, pageHeight);
            customPaper.setPrintArea(printAreaTop, printAreaLeft, printAreaWidth, printAreaHeight);

            PageFormat pf = job.defaultPage();
            pf.setPaper(customPaper);
            job.defaultPage(pf);
            job.setPrintable(this, pf);
            job.print();

        } catch (PrinterException | HeadlessException e) {
            System.err.println(e);
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }
    }

    @Override
    public Result printReceiptMoneyDeposition(Path receiptsFile) {
        try {
            PosReceipt posReceipt = PosReceipt.getFromJSON(receiptsFile);

            List<String> lines = Files.readAllLines(rootPath.resolve(
                    Paths.get("templates", "receipt_money_deposition.txt")), Charset.defaultCharset());

            String receipt = "";
            for (String line : lines) {
                line = line.replaceAll("\\{cash\\}",
                        "" + posReceipt.getCash());
                line = line.replaceAll("\\{fiscal\\}", getFiscalString(posReceipt));

                receipt += line + "\r\n";
            }

            printStringList = Arrays.asList(receipt.split("\r\n"));
            printDoc();
            return Result.newEmptySuccess();

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
                    Paths.get("templates", "receipt_money_out.txt")), Charset.defaultCharset());

            String receipt = "";
            for (String line : lines) {
                line = line.replaceAll("\\{cash\\}",
                        "" + posReceipt.getCash());
                line = line.replaceAll("\\{fiscal\\}", getFiscalString(posReceipt));

                receipt += line + "\r\n";
            }

            printStringList = Arrays.asList(receipt.split("\r\n"));
            printDoc();
            return Result.newEmptySuccess();

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return Result.newResultError(e.toString());
        }
    }

    final class CustomPaper extends Paper {

        private double width, height;
        private double k = 2.857142857;

        public CustomPaper(double width, double height) {
            super();
            setSize(width * k, height * k);
            this.width = width;
            this.height = height;
        }

        public void setPrintArea(double printAreaTop, double printAreaLeft,
                double printAreaWidth, double printAreaHeight) {
            super.setImageableArea(printAreaTop * k, printAreaLeft * k,
                    printAreaWidth * k, printAreaHeight * k);
        }

        @Override
        public double getWidth() {
            return width * k;
        }

        @Override
        public double getHeight() {
            return height * k;
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        System.out.println("PageFormat: width=" + pf.getWidth() + ", height=" + pf.getHeight());
        Logger.getGlobal().log(Level.INFO, "PageFormat {0}", pf);
        System.out.println("pageIndex " + pageIndex);
        Logger.getGlobal().log(Level.INFO, "pageIndex {0}", pageIndex);

        if (pageIndex == 0) {
            Graphics2D g2d = (Graphics2D) g;
            Font font = g2d.getFont();
            g2d.setFont(font.deriveFont((float) fontSize));

            g2d.translate(pf.getImageableX(), pf.getImageableY());
            g2d.setColor(Color.black);
            int step = g2d.getFont().getSize();
            step += step / 4;
            double y = paddingTop + g2d.getFont().getSize();
            for (String s : printStringList) {
                Logger.getGlobal().log(Level.INFO, "printStringList: {0}", s);
                g2d.drawString(s, (float) paddingLeft, (float) y);
                y += step;
            }

            //g2d.fillRect(0, 0, 200, 200);
            return Printable.PAGE_EXISTS;

        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }
}
