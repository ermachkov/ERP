/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class StringToNumber {
    
    public static BigDecimal formatToMoney(String s){
        BigDecimal result = BigDecimal.ZERO;
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        s = s.replaceAll(",", ".");
        try {
            Number number = decimalFormat.parse(s);
            result = new BigDecimal(number.doubleValue());
        } catch (Exception e){
        } finally {
            return result;
        }
    }
    
    public static BigDecimal formatToQuantity(String s){
        BigDecimal result = BigDecimal.ZERO;
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        s = s.replaceAll(",", ".");
        try {
            Number number = decimalFormat.parse(s);
            result = new BigDecimal(number.doubleValue());
        } catch (Exception e){
        } finally {
            return result;
        }
    }
    
}
