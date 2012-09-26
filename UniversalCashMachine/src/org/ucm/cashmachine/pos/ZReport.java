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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ZReport {

    private JSONObject jsonObject;
    private long date;
    private int moneyOutCount, salesCount, stornoCount, returnCount, cancelCount, moneyInCount, number;
    private BigDecimal cash, totalCash, totalSales, sales, moneyIn, moneyOut;

    private ZReport(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    private ZReport() {
        //
    }

    public static ZReport getEmptyZReport() {
        return new ZReport();
    }

    public static ZReport getFromJSON(Path jsonFile) {
        try {
            String s = new String(Files.readAllBytes(jsonFile));
            JSONObject jsonObject = new JSONObject(s);
            ZReport zReport = new ZReport(jsonObject);
            return zReport;

        } catch (IOException | JSONException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return null;
        }
    }

    /**
     * 
     * @param zReportDir
     * @return Path or null
     */
    public Path createZReport(Path zReportDir) {
        try {
            JSONObject jsonZReport = new JSONObject();
            jsonZReport.put("moneyOutCount", moneyOutCount);
            jsonZReport.put("salesCount", salesCount);
            jsonZReport.put("stornoCount", stornoCount);
            jsonZReport.put("returnCount", returnCount);
            jsonZReport.put("cancelCount", cancelCount);
            jsonZReport.put("moneyInCount", moneyInCount);
            jsonZReport.put("number", number);
            
            jsonZReport.put("cash", cash);
            jsonZReport.put("totalCash", totalCash);
            jsonZReport.put("totalSales", totalSales);
            jsonZReport.put("sales", sales);
            jsonZReport.put("moneyIn", moneyIn);
            jsonZReport.put("moneyOut", moneyOut);
            
            jsonZReport.put("date", new Date().getTime());

            int newNumber = 0;
            for (File file : zReportDir.toFile().listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }

                String s = file.getName();
                try {
                    int n = Integer.valueOf(s.split("\\.")[0].trim());
                    if (n > newNumber) {
                        newNumber = n;
                    }
                } catch (Exception e) {
                }
            }

            newNumber = newNumber + 1;

            Files.write(zReportDir.resolve("" + newNumber + ".json"), jsonZReport.toString().getBytes());

            return zReportDir.resolve("" + newNumber + ".json");

        } catch (JSONException | IOException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return null;
        }
    }

    public Date getDate() {
        Date d = new Date();
        d.setTime(getLongValue("date"));
        return d;
    }

    public void setDate(Date d) {
        date = d.getTime();
    }

    public int getNumber() {
        return getIntValue("number");
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getSalesCount() {
        return getIntValue("salesCount");
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public int getStornoCount() {
        return getIntValue("stornoCount");
    }

    public void setStornoCount(int stornoCount) {
        this.stornoCount = stornoCount;
    }

    public int getReturnCount() {
        return getIntValue("returnCount");
    }

    public void setReturnCount(int returnCount) {
        this.returnCount = returnCount;
    }

    public int getCancelCount() {
        return getIntValue("cancelCount");
    }

    public void setCancelCount(int cancelCount) {
        this.cancelCount = cancelCount;
    }

    public int getMoneyInCount() {
        return getIntValue("moneyInCount");
    }

    public void setMoneyInCount(int moneyInCount) {
        this.moneyInCount = moneyInCount;
    }

    public int getMoneyOutCount() {
        return getIntValue("moneyOutCount");
    }

    public void setMoneyOutCount(int moneyOutCount) {
        this.moneyOutCount = moneyOutCount;
    }

    public BigDecimal getCash() {
        return getBigDecimalValue("cash");
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    public BigDecimal getTotalCash() {
        return getBigDecimalValue("totalCash");
    }

    public void setTotalCash(BigDecimal totalCash) {
        this.totalCash = totalCash;
    }

    public BigDecimal getTotalSales() {
        return getBigDecimalValue("totalSales");
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getSalesSum() {
        return getBigDecimalValue("sales");
    }

    public void setSalesSum(BigDecimal sales) {
        this.sales = sales;
    }

    public BigDecimal getMoneyIn() {
        return getBigDecimalValue("moneyIn");
    }

    public void setMoneyIn(BigDecimal moneyIn) {
        this.moneyIn = moneyIn;
    }

    public BigDecimal getMoneyOut() {
        return getBigDecimalValue("moneyOut");
    }

    public void setMoneyOut(BigDecimal moneyOut) {
        this.moneyOut = moneyOut;
    }

    private BigDecimal getBigDecimalValue(String key) {
        try {
            if (jsonObject.has(key)) {
                return new BigDecimal("" + jsonObject.get(key));
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }

    private int getIntValue(String key) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getInt(key);
            } else {
                return -1;
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return -1;
        }
    }

    private long getLongValue(String key) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getLong(key);
            } else {
                return -1;
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return -1;
        }
    }
}
