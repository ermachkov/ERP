/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.pos;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.money.Money;
import org.ubo.utils.Result;
import org.ucm.cashmachine.Receipt;
import org.ucm.cashmachine.ReceiptRow;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class POSUtil {

    private Path rootDir;

    public POSUtil(Path rootDir) {
        this.rootDir = rootDir;
    }

    public Result createShiftArchive() {
        String errors = "";
        Path pArchiveDir = rootDir.resolve(Paths.get("archive", "" + new Date().getTime()));
        pArchiveDir.toFile().mkdir();

        Path receiptsDir = rootDir.resolve("receipts");
        for (File f : receiptsDir.toFile().listFiles()) {
            try {
                Files.move(f.toPath(), pArchiveDir.resolve(f.getName()));
            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, null, e);
                errors += e.toString();
            }
        }

        if (errors.equals("")) {
            return Result.newEmptySuccess();

        } else {
            return Result.newResultError(errors);
        }
    }

    public BigDecimal getTotalSales(BigDecimal salesForShift) {
        int zNumber = 0;
        Path p = rootDir.resolve("z-reports");
        for (File f : p.toFile().listFiles()) {
            if (f.isDirectory()) {
                continue;
            }

            try {
                int n = Integer.parseInt(f.getName().split("\\.")[0].trim());
                if (n > zNumber) {
                    zNumber = n;
                }
            } catch (Exception e) {
            }
        }

        p = rootDir.resolve(Paths.get("z-reports", "" + zNumber + ".json"));
        if (p.toFile().exists()) {
            ZReport zReport = ZReport.getFromJSON(p);
            return Money.ADD(salesForShift, zReport.getTotalSales());
        } else {
            return Money.ADD(salesForShift, BigDecimal.ZERO);
        }

    }

    public BigDecimal getToatalCash(BigDecimal cashForShift) {
        int zNumber = 0;
        Path p = rootDir.resolve("z-reports");
        for (File f : p.toFile().listFiles()) {
            if (f.isDirectory()) {
                continue;
            }

            try {
                int n = Integer.parseInt(f.getName().split("\\.")[0].trim());
                if (n > zNumber) {
                    zNumber = n;
                }
            } catch (Exception e) {
            }
        }

        p = rootDir.resolve(Paths.get("z-reports", "" + zNumber + ".json"));
        if (p.toFile().exists()) {
            ZReport zReport = ZReport.getFromJSON(p);
            return Money.ADD(cashForShift, zReport.getTotalCash());

        } else {
            return Money.ADD(cashForShift, BigDecimal.ZERO);
        }

    }

    public BigDecimal getCashForShift() {
        BigDecimal cash = Money.ADD(getMoney(PosReceipt.TYPE_SALE), getMoney(PosReceipt.TYPE_MONEY_IN));
        cash = Money.SUBSTRACT(cash, getMoney(PosReceipt.TYPE_MONEY_OUT));
        return cash;
    }

    public BigDecimal getMoneyInForShift() {
        return getMoney(PosReceipt.TYPE_MONEY_IN);
    }

    public BigDecimal getMoneyOutForShift() {
        return getMoney(PosReceipt.TYPE_MONEY_OUT);
    }

    private BigDecimal getMoney(int receiptType) {
        BigDecimal money = BigDecimal.ZERO;
        Path p = rootDir.resolve("receipts");
        for (File f : p.toFile().listFiles()) {
            if (f.isDirectory()) {
                continue;
            }

            PosReceipt posReceipt = PosReceipt.getFromJSON(f.toPath());
            if (posReceipt.getReceiptType() == receiptType) {
                switch (receiptType) {
                    case PosReceipt.TYPE_SALE:
                        money = Money.ADD(money, getReceiptSum(posReceipt.getReceipt()));
                        break;

                    case PosReceipt.TYPE_MONEY_IN:
                        money = Money.ADD(money, posReceipt.getCash());
                        break;

                    case PosReceipt.TYPE_MONEY_OUT:
                        money = Money.ADD(money, posReceipt.getCash());
                        break;
                }
            }
        }

        return money;
    }

    private int getCount(int receiptType) {
        int count = 0;
        Path p = rootDir.resolve("receipts");
        for (File f : p.toFile().listFiles()) {
            if (f.isDirectory()) {
                continue;
            }

            PosReceipt posReceipt = PosReceipt.getFromJSON(f.toPath());
            if (posReceipt.getReceiptType() == receiptType) {
                count++;
            }
        }

        return count;
    }

    public int getMoneyOutCountForShift() {
        return getCount(PosReceipt.TYPE_MONEY_OUT);
    }

    public int getSalesCountForShift() {
        return getCount(PosReceipt.TYPE_SALE);
    }

    public BigDecimal getSalesSum() {
        return getMoney(PosReceipt.TYPE_SALE);
    }

    public int getNextZReportNumber(Path zReportDir) {
        int number = 0;
        for (File f : zReportDir.toFile().listFiles()) {
            if (f.isDirectory()) {
                continue;
            }

            try {
                int n = Integer.parseInt(f.getName().split("\\.")[0].trim());
                if (n > number) {
                    number = n;
                }
            } catch (Exception e) {
            }
        }

        Path pCounter = rootDir.resolve(Paths.get("settings", "counter.json"));
        try {
            JSONObject json = new JSONObject(new String(Files.readAllBytes(pCounter)));
            int docNumber = json.getInt("docNumber") + 1;
            json.put("docNumber", docNumber);
            Files.write(pCounter, json.toString().getBytes());

        } catch (IOException | JSONException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }

        return number + 1;
    }

    public static BigDecimal getReceiptSum(Receipt receipt) {
        BigDecimal sum = BigDecimal.ZERO;
        for (ReceiptRow row : receipt.getReceiptRows()) {
            sum = Money.ADD(sum, Money.MULTIPLY(row.getQuantity(), row.getPrice()));
        }

        sum = Money.SUBSTRACT(sum, receipt.getTotalDiscountSum());

        return sum;
    }

    /**
     *
     * @return Next receipt number or -1
     */
    public int getNextReceiptNumber() {
        Path pCounter = rootDir.resolve(Paths.get("settings", "counter.json"));
        try {
            JSONObject json = new JSONObject(new String(Files.readAllBytes(pCounter)));
            int number = json.getInt("receiptNumber") + 1;
            json.put("receiptNumber", number);
            Files.write(pCounter, json.toString().getBytes());
            return number;

        } catch (IOException | JSONException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return -1;
        }
    }

    /**
     *
     * @return Next doc number or -1
     */
    public int getNextDocNumber() {
        Path pCounter = rootDir.resolve(Paths.get("settings", "counter.json"));
        try {
            JSONObject json = new JSONObject(new String(Files.readAllBytes(pCounter)));
            int number = json.getInt("docNumber") + 1;
            json.put("docNumber", number);
            Files.write(pCounter, json.toString().getBytes());

            return number;

        } catch (IOException | JSONException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return -1;
        }
    }
}
