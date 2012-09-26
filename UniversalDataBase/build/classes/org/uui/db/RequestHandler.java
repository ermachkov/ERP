/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class RequestHandler {

    public RequestHandler() {
        //
    }

    public boolean isNumber(Object val) {
        boolean result = false;
        if (val.getClass().getName().equals(byte.class.getName())
                || val.getClass().getName().equals(Byte.class.getName())
                || val.getClass().getName().equals(short.class.getName())
                || val.getClass().getName().equals(Short.class.getName())
                || val.getClass().getName().equals(int.class.getName())
                || val.getClass().getName().equals(Integer.class.getName())
                || val.getClass().getName().equals(double.class.getName())
                || val.getClass().getName().equals(Double.class.getName())
                || val.getClass().getName().equals(Long.class.getName())
                || val.getClass().getName().equals(long.class.getName())
                || val.getClass().getName().equals(Double.class.getName())
                || val.getClass().getName().equals(double.class.getName())
                || val.getClass().getName().equals(Float.class.getName())
                || val.getClass().getName().equals(float.class.getName())
                || val.getClass().getName().equals(BigDecimal.class.getName())) {
            result = true;
        }

        return result;
    }
    
    public boolean isPassedDate(Object valueFromMethod, Condition condition){
        boolean result = false;
        Date dateValue = (Date)valueFromMethod;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dateCondition = sdf.parse(condition.getConditionValue());
            
            if(condition.getType() == Condition.EQUAL){
                if(dateValue.getTime() == dateCondition.getTime()){
                    result = true;
                }
            }
            
            if(condition.getType() == Condition.LESS){
                if(dateValue.getTime() < dateCondition.getTime()){
                    result = true;
                }
            }
            
            if(condition.getType() == Condition.LESS_OR_EQUAL){
                if(dateValue.getTime() <= dateCondition.getTime()){
                    result = true;
                }
            }
            
            if(condition.getType() == Condition.MORE){
                if(dateValue.getTime() > dateCondition.getTime()){
                    result = true;
                }
            }
            
            if(condition.getType() == Condition.MORE_OR_EQUAL){
                if(dateValue.getTime() >= dateCondition.getTime()){
                    result = true;
                }
            }
            
            if(condition.getType() == Condition.NOT_EQUIAL){
                if(dateValue.getTime() != dateCondition.getTime()){
                    result = true;
                }
            }
            
        } catch (ParseException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }
        
        return result;
    }

    public boolean isPassedNumber(Object valueFromMethod, Condition condition) {
        boolean result = false;

        Number numberValue = null;
        try {
            numberValue = NumberFormat.getNumberInstance().parse(Objects.toString(valueFromMethod, "0"));
        } catch (ParseException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }

        if (condition.getType() == Condition.EQUAL) {
            if (numberValue.byteValue() == condition.getNumberValue().byteValue()) {
                result = true;
            }

            if (numberValue.shortValue() == condition.getNumberValue().shortValue()) {
                result = true;
            }

            if (numberValue.intValue() == condition.getNumberValue().intValue()) {
                result = true;
            }

            if (numberValue.longValue() == condition.getNumberValue().longValue()) {
                result = true;
            }

            if (numberValue.doubleValue() == condition.getNumberValue().doubleValue()) {
                result = true;
            }

            if (numberValue.floatValue() == condition.getNumberValue().floatValue()) {
                result = true;
            }

        } else if (condition.getType() == Condition.LESS) {
            if (numberValue.byteValue() < condition.getNumberValue().byteValue()) {
                result = true;
            }

            if (numberValue.shortValue() < condition.getNumberValue().shortValue()) {
                result = true;
            }

            if (numberValue.intValue() < condition.getNumberValue().intValue()) {
                result = true;
            }

            if (numberValue.longValue() < condition.getNumberValue().longValue()) {
                result = true;
            }

            if (numberValue.doubleValue() < condition.getNumberValue().doubleValue()) {
                result = true;
            }

            if (numberValue.floatValue() < condition.getNumberValue().floatValue()) {
                result = true;
            }

        } else if (condition.getType() == Condition.MORE) {
            if (numberValue.byteValue() > condition.getNumberValue().byteValue()) {
                result = true;
            }

            if (numberValue.shortValue() > condition.getNumberValue().shortValue()) {
                result = true;
            }

            if (numberValue.intValue() > condition.getNumberValue().intValue()) {
                result = true;
            }

            if (numberValue.longValue() > condition.getNumberValue().longValue()) {
                result = true;
            }

            if (numberValue.doubleValue() > condition.getNumberValue().doubleValue()) {
                result = true;
            }

            if (numberValue.floatValue() > condition.getNumberValue().floatValue()) {
                result = true;
            }

        } else if (condition.getType() == Condition.LESS_OR_EQUAL) {
            if (numberValue.byteValue() <= condition.getNumberValue().byteValue()) {
                result = true;
            }

            if (numberValue.shortValue() <= condition.getNumberValue().shortValue()) {
                result = true;
            }

            if (numberValue.intValue() <= condition.getNumberValue().intValue()) {
                result = true;
            }

            if (numberValue.longValue() <= condition.getNumberValue().longValue()) {
                result = true;
            }

            if (numberValue.doubleValue() <= condition.getNumberValue().doubleValue()) {
                result = true;
            }

            if (numberValue.floatValue() <= condition.getNumberValue().floatValue()) {
                result = true;
            }

        } else if (condition.getType() == Condition.MORE_OR_EQUAL) {
            if (numberValue.byteValue() >= condition.getNumberValue().byteValue()) {
                result = true;
            }

            if (numberValue.shortValue() >= condition.getNumberValue().shortValue()) {
                result = true;
            }

            if (numberValue.intValue() >= condition.getNumberValue().intValue()) {
                result = true;
            }

            if (numberValue.longValue() >= condition.getNumberValue().longValue()) {
                result = true;
            }

            if (numberValue.doubleValue() >= condition.getNumberValue().doubleValue()) {
                result = true;
            }

            if (numberValue.floatValue() >= condition.getNumberValue().floatValue()) {
                result = true;
            }
            
        } else if (condition.getType() == Condition.NOT_EQUIAL){
            if (numberValue.byteValue() != condition.getNumberValue().byteValue()) {
                result = true;
            }

            if (numberValue.shortValue() != condition.getNumberValue().shortValue()) {
                result = true;
            }

            if (numberValue.intValue() != condition.getNumberValue().intValue()) {
                result = true;
            }

            if (numberValue.longValue() != condition.getNumberValue().longValue()) {
                result = true;
            }

            if (numberValue.doubleValue() != condition.getNumberValue().doubleValue()) {
                result = true;
            }

            if (numberValue.floatValue() != condition.getNumberValue().floatValue()) {
                result = true;
            }
        }
        
        return result;
    }
}
