/*
 *  Copyright (C) 2010 Zubanov Dmitry
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 17.10.2010
 * (C) Copyright by Zubanov Dmitry
 */

package org.jssdb.query;

import java.io.Serializable;
import java.text.MessageFormat;

public class SelectById implements Serializable {
    
    private int leftExpression = -1, rightExpression = -1;
    private long leftValue = -1, rightValue = -1;

    public static int EQUAL = 0;
    public static int GREATER = 1;
    public static int GREATER_OR_EQUAL = 2;
    public static int LESS = 3;
    public static int LESS_OR_EQUAL = 4;
    static final long serialVersionUID = 100000000000099003L;

    public SelectById(int exp1, long val1){
        leftExpression = exp1;
        leftValue = val1;
    }

    public SelectById(int exp1, long val1, int exp2, long val2){
        leftExpression = exp1;
        rightExpression = exp2;
        leftValue = val1;
        rightValue = val2;
    }

    public int getLeftExpression() {
        return leftExpression;
    }

    public int getRightExpression() {
        return rightExpression;
    }

    public long getLeftValue() {
        return leftValue;
    }

    public long getRightValue() {
        return rightValue;
    }
    
    @Override
    public String toString(){
        return MessageFormat.format("Expression 1 = {0}, value 1 = {1},"
                + "Expression 2 = {2}, value 2 = {3}", 
                new Object[]{leftExpression, leftValue, rightExpression, rightValue});
    }

}
