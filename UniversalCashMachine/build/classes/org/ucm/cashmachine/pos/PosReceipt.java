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
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ucm.cashmachine.Receipt;
import org.ucm.cashmachine.ReceiptRow;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class PosReceipt {

    private JSONArray rows;
    private BigDecimal totalDiscountSum, cash;
    private int paymentType, section, password, receiptType = 1; 
    private JSONObject jsonReceipt;
    public static final int TYPE_SALE = 1, TYPE_MONEY_IN = 2, TYPE_MONEY_OUT = 3;

    private PosReceipt(JSONObject jsonReceipt) {
        this.jsonReceipt = jsonReceipt;
        init();
    }

    private PosReceipt() {
        rows = new JSONArray();
    }
    
    private void init(){
        try {
            rows = jsonReceipt.getJSONArray("rows");
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    public static PosReceipt getEmptyPosReceipt() {
        PosReceipt posReceipt = new PosReceipt();
        return posReceipt;
    }

    /**
     * 
     * @param receiptFile
     * @return PosReceipt or null
     */
    public static PosReceipt getFromJSON(Path receiptFile) {
        try {
            String s = new String(Files.readAllBytes(receiptFile));
            JSONObject jsonObject = new JSONObject(s);
            PosReceipt posReceipt = new PosReceipt(jsonObject);
            return posReceipt;

        } catch (IOException | JSONException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return null;
        }
    }

    /**
     * 
     * @param receiptsDir
     * @return Path to json file or null
     */
    public Path createReciept(Path receiptsDir, int numberReceipt, int numberDoc) {
        try {
            JSONObject json = new JSONObject();
            json.put("rows", rows);
            json.put("password", password);
            json.put("paymentType", paymentType);
            json.put("section", section);
            json.put("cash", cash);
            json.put("totalDiscountSum", totalDiscountSum);
            json.put("receiptType", receiptType);
            json.put("date", new Date().getTime());
            json.put("numberReceipt", numberReceipt);
            json.put("numberDoc", numberDoc);

            int newNumber = 0;
            for (File file : receiptsDir.toFile().listFiles()) {
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
            
            Files.write(receiptsDir.resolve("" + newNumber + ".json"), json.toString().getBytes());

            return receiptsDir.resolve("" + newNumber + ".json");

        } catch (JSONException | IOException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return null;
        }
    }
    
    /**
     * 
     * @return Number of receipt or -1
     */
    public int getNumberDoc(){
        try {
            return jsonReceipt.getInt("numberDoc");

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return -1;
        }
    }
    
    /**
     * 
     * @return Number of receipt or -1
     */
    public int getNumbeReceipt(){
        try {
            return jsonReceipt.getInt("numberReceipt");

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return -1;
        }
    }
    
    public void setReceiptType(int receiptType){
        this.receiptType = receiptType;
    }
    
    /**
     * 
     * @return Receipt type<br/>
     * <ul>
     * <li>1 - sell</li>
     * <li>2 - money deposition</li>
     * <li>3 - money out</li>
     * </ul>
     */
    public int getReceiptType() {
        try {
            return jsonReceipt.getInt("receiptType");

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return -1;
        }
    }

    public void setPassword(int password) {
        this.password = password;
    }

    /**
     * 
     * @return password or -1
     */
    public int getPassword() {
        try {
            return jsonReceipt.getInt("password");

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return -1;
        }
    }

    /**
     * 
     * @param paymentType 
     * 
     */
    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    /**
     * 
     * @return PaymentType or -1
     */
    public int getPaymentType() {
        try {
            return jsonReceipt.getInt("paymentType");

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return -1;
        }
    }

    public void setSection(int section) {
        this.section = section;
    }

    /**
     * 
     * @return section or -1
     */
    public int getSection() {
        try {
            return jsonReceipt.getInt("section");

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return -1;
        }
    }

    public void setCash(BigDecimal cash) {
        this.cash = cash;
    }

    /**
     * 
     * @return cash or null
     */
    public BigDecimal getCash() {
        try {
            return new BigDecimal(jsonReceipt.getDouble("cash"));

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return new BigDecimal("-1");
        }
    }

    public void setReceipt(Receipt receipt) {
        try {
            for (ReceiptRow row : receipt.getReceiptRows()) {
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("itemName", row.getItemName());
                jsonItem.put("price", row.getPrice().doubleValue());
                jsonItem.put("quantity", row.getQuantity().doubleValue());

                rows.put(jsonItem);
            }

            totalDiscountSum = receipt.getTotalDiscountSum();

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }
    }

    /**
     * 
     * @return Receipt or null
     */
    public Receipt getReceipt() {
        try {
            Receipt receipt = new Receipt();
            for (int i = 0; i < rows.length(); i++) {
                JSONObject jsonItem = rows.getJSONObject(i);
                ReceiptRow row = new ReceiptRow(
                        jsonItem.getString("itemName"),
                        new BigDecimal(jsonItem.getDouble("quantity")),
                        new BigDecimal(jsonItem.getDouble("price")));
                receipt.addReceiptRow(row);
            }
            
            receipt.setTotalDiscount(getTotalDiscountSum());
            
            return receipt;

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return null;
        }
    }
    
    /**
     * 
     * @return TotalDiscountSum or -1
     */
    public BigDecimal getTotalDiscountSum() {
        try {
            return new BigDecimal(jsonReceipt.getDouble("totalDiscountSum"));

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return new BigDecimal("-1");
        }
    }
    
    /**
     * 
     * @return date or -1
     */
    public long getDate() {
        try {
            return jsonReceipt.getLong("date");

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return -1;
        }
    }
}
