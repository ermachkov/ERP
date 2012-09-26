/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.quantity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Quantity implements Serializable{
    
    private Locale locale;
    static final long serialVersionUID = 100000000000010002L;
    private static int ADD = 0, SUBSTRACT = 1, MULTIPLY = 2, DIVIDE = 3;
    private static Quantity self = null;
    
    private Quantity() {
        locale = Locale.getDefault();
    }

    public synchronized static Quantity getInstance() {
        if(self == null){
            self = new Quantity();
        }
        
        return self;
    }

    public String getAddFormatted(String val1, String val2) {
        String out = null;
        try {
            val1 = val1.replaceAll(",", ".");
            val2 = val2.replaceAll(",", ".");
            NumberFormat decimalFormat = NumberFormat.getInstance(locale);
            decimalFormat.setGroupingUsed(false);
            decimalFormat.setMinimumFractionDigits(3);
            decimalFormat.setMaximumFractionDigits(3);
            out = decimalFormat.format(getResult(val1, val2, Quantity.ADD).doubleValue());

        } catch (Exception e) {
            Logger.getLogger(Quantity.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);

        } finally {
            return out;
        }
    }

    /**
     * 
     * @param val1
     * @param val2
     * @return
     * BigDecimal, result may be is null
     */
    public static BigDecimal ADD(String val1, String val2) {
        BigDecimal result = null;
        String val = Quantity.getInstance().getAddFormatted(val1, val2);
        if (val != null) {
            result = new BigDecimal(val.replaceAll(",", "."));
        }
        return result;
    }

    public String getSubstarctFormatted(String val1, String val2) {
        String out = null;
        try {
            val1 = val1.replaceAll(",", ".");
            val2 = val2.replaceAll(",", ".");
            NumberFormat decimalFormat = NumberFormat.getInstance(locale);
            decimalFormat.setGroupingUsed(false);
            decimalFormat.setMinimumFractionDigits(3);
            decimalFormat.setMaximumFractionDigits(3);
            out = decimalFormat.format(getResult(val1, val2, Quantity.SUBSTRACT).doubleValue());

        } catch (Exception e) {
            Logger.getLogger(Quantity.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);

        } finally {
            if(out != null){
                out = out.replaceAll(",", ".");
            }
            return out;
        }
    }
    
    public static BigDecimal SUBSTRACT(String val1, String val2){
        BigDecimal result = null;
        String val = Quantity.getInstance().getSubstarctFormatted(val1, val2);
        if (val != null) {
            result = new BigDecimal(val.replaceAll(",", "."));
        }
        return result;
    }

    public String getMultiplyFormatted(String val1, String val2) {
        String out = null;
        try {
            val1 = val1.replaceAll(",", ".");
            val2 = val2.replaceAll(",", ".");
            NumberFormat decimalFormat = NumberFormat.getInstance(locale);
            decimalFormat.setGroupingUsed(false);
            decimalFormat.setMinimumFractionDigits(3);
            decimalFormat.setMaximumFractionDigits(3);
            out = decimalFormat.format(getResult(val1, val2, Quantity.MULTIPLY).doubleValue());

        } catch (Exception e) {
            Logger.getLogger(Quantity.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);

        } finally {
            if(out != null){
                out = out.replaceAll(",", ".");
            }
            return out;
        }
    }
    
    public static BigDecimal MULTIPLY(String val1, String val2){
        BigDecimal result = null;
        String val = Quantity.getInstance().getMultiplyFormatted(val1, val2);
        if (val != null) {
            result = new BigDecimal(val.replaceAll(",", "."));
        }
        return result;
    }

    public String getDivideFormatted(String val1, String val2) {
        String out = null;
        try {
            val1 = val1.replaceAll(",", ".");
            val2 = val2.replaceAll(",", ".");
            NumberFormat decimalFormat = NumberFormat.getInstance(locale);
            decimalFormat.setGroupingUsed(false);
            decimalFormat.setMinimumFractionDigits(3);
            decimalFormat.setMaximumFractionDigits(3);
            out = decimalFormat.format(getResult(val1, val2, Quantity.DIVIDE).doubleValue());

        } catch (Exception e) {
            Logger.getLogger(Quantity.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);

        } finally {
            if(out != null){
                out = out.replaceAll(",", ".");
            }
            return out;
        }
    }
    
    public static BigDecimal DIVIDE(String val1, String val2){
        BigDecimal result = null;
        String val = Quantity.getInstance().getDivideFormatted(val1, val2);
        if (val != null) {
            result = new BigDecimal(val.replaceAll(",", "."));
        }
        return result;
    }

    public BigDecimal getResult(String val1, String val2, int action) {
        BigDecimal result = null;
        try {
            val1 = stringHandler(val1);
            val2 = stringHandler(val2);
            BigDecimal v1 = new BigDecimal(val1);
            BigDecimal v2 = new BigDecimal(val2);

            switch (action) {
                case 0:
                    result = v1.add(v2, new MathContext(9));
                    break;

                case 1:
                    result = v1.subtract(v2, new MathContext(9));
                    break;

                case 2:
                    result = v1.multiply(v2, new MathContext(9));
                    break;

                case 3:
                    result = v1.divide(v2, 9, BigDecimal.ROUND_HALF_UP);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            Logger.getLogger(Quantity.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);
        } finally {
            return result;
        }
    }

    private String stringHandler(String str) throws Exception {
        str = str.replaceAll(",", ".");
        Float.parseFloat(str);
        return str;
    }

    public void setLocale(String language, String country) {
        locale = new Locale(language, country);
    }
    
    public static String format(Number quantity) {
        return Quantity.format(new BigDecimal(quantity.doubleValue()));
    }

    public static String format(BigDecimal quantity) {
        String out = null;
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(3);
        decimalFormat.setMaximumFractionDigits(3);
        
        try {
            out = decimalFormat.format(quantity).replaceAll(",", ".");
        } finally {
            return out;
        }
    }
    
    public static String format(String quantity) {
        String out = null;
        String val = quantity.replaceAll(",", ".");
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(3);
        decimalFormat.setMaximumFractionDigits(3);
        
        try {
            out = decimalFormat.format(Double.parseDouble(val)).replaceAll(",", ".");
        } finally {
            return out;
        }
    }
}
