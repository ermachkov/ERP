/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Condition implements Serializable{

    static final long serialVersionUID = 1L;
    public static int EQUAL = 0, LESS = 1, MORE = 2, LESS_OR_EQUAL = 3,
            MORE_OR_EQUAL = 4, OBJECT_EQUAL = 5, REGEXP = 6, NOT_EQUIAL = 7;
    private int type = -1;
    private String conditionValue;

    private Condition(int type, String conditionValue) {
        this.type = type;
        this.conditionValue = conditionValue;
    }
    
    /**
     * 
     * @param type
     * @param conditionValue Сравниваемое значение<br/>
     * Сравниваемое значение всегда представляется в виде String<br/>
     * Пример:<br>
     * "1", "100.50", "Петров", "2011-05-04 23:50:20"
     * @return Condition
     */
    public static Condition newCondition(int type, String conditionValue){
        return new Condition(type, conditionValue);
    }
    
    public static Condition newConditionEquial(String conditionValue){
        return new Condition(Condition.EQUAL, conditionValue);
    }
    
    /**
     * 
     * @param regexp Сравниваемое значение в виде регулярного выражения "(.*?)[a-z](.*?)\\i"
     * @return Condition
     */
    public static Condition newConditionRegexp(String regexp){
        return new Condition(regexp);
    }

    private Condition(String regexp) {
        this(Condition.REGEXP, regexp);
    }

    public String getConditionValue() {
        return conditionValue;
    }
    
    public Number getNumberValue(){
        Number number = null;
        
        try {
            number = NumberFormat.getInstance().parse(Objects.toString(conditionValue, "0"));
        } catch (ParseException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }
        
        return number;
    }

    public int getType() {
        return type;
    }
}
