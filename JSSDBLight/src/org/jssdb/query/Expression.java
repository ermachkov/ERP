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
import java.util.Date;

public class Expression implements Serializable{

    public static int EQUAL = 0;
    public static int GREATER = 1;
    public static int GREATER_OR_EQUAL = 2;
    public static int LESS = 3;
    public static int LESS_OR_EQUAL = 4;
    public static int EQUAL_STRING = 5;
    private int leftExpression = -1, rightExpression = -1;
    private Object leftValue;
    private Object rightValue;
    static final long serialVersionUID = 100000000000099005L;

    public Expression(int leftExpression, Object value) {
        this.leftExpression = leftExpression;
        this.leftValue = value;
    }

    public Expression(int leftExpression, Object value, int rightExpression, Object rightValue) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
        this.leftValue = value;
        this.rightValue = rightValue;
    }

    @Override
    public String toString() {
        return MessageFormat.format("Left expression = {0}, "
                + "Right expression = {1}, value = {2}",
                new Object[]{leftExpression, rightExpression, leftValue});
    }

    public boolean compare(Object obj) {
        boolean result = false;
        if (rightExpression == -1) {
            if (leftValue instanceof Number) {
                Number numberValue = (Number) leftValue;
                if (obj instanceof Number) {
                    Number numberObject = (Number) obj;
                    if (leftExpression == Expression.EQUAL) {
                        if (numberValue.doubleValue() == numberObject.doubleValue()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.GREATER) {
                        if (numberObject.doubleValue() > numberValue.doubleValue()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.GREATER_OR_EQUAL) {
                        if (numberObject.doubleValue() >= numberValue.doubleValue()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.LESS) {
                        if (numberObject.doubleValue() < numberValue.doubleValue()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.LESS_OR_EQUAL) {
                        if (numberObject.doubleValue() <= numberValue.doubleValue()) {
                            result = true;
                        }
                    }

                } else {
                    throw new IllegalArgumentException();
                }

            } else if (leftValue instanceof String) {
                if (leftExpression == Expression.EQUAL_STRING) {
                    if (leftValue.equals("" + obj)) {
                        result = true;
                    }
                }

            } else if (leftValue instanceof Date) {
                Date dateValue = (Date) leftValue;
                Date dateObject = (Date) obj;
                if (leftExpression == Expression.EQUAL) {
                    if (dateValue.getTime() == dateObject.getTime()) {
                        result = true;
                    }
                }

                if (leftExpression == Expression.GREATER) {
                    if (dateObject.getTime() > dateValue.getTime()) {
                        result = true;
                    }
                }

                if (leftExpression == Expression.GREATER_OR_EQUAL) {
                    if (dateObject.getTime() >= dateValue.getTime()) {
                        result = true;
                    }
                }

                if (leftExpression == Expression.LESS) {
                    if (dateObject.getTime() < dateValue.getTime()) {
                        result = true;
                    }
                }

                if (leftExpression == Expression.LESS_OR_EQUAL) {
                    if (dateObject.getTime() <= dateValue.getTime()) {
                        result = true;
                    }
                }

            } else {
                if (leftValue.equals(obj)) {
                    result = true;
                }
            }

        } else {
            if (leftValue instanceof Number) {
                Number numberLeftValue = (Number) leftValue;

                if (obj instanceof Number && rightValue instanceof Number) {
                    Number numberObject = (Number) obj;
                    Number numberRightValue = (Number) rightValue;

                    if (leftExpression == Expression.GREATER
                            && rightExpression == Expression.LESS) {
                        if (numberObject.doubleValue() > numberLeftValue.doubleValue()
                                && numberObject.doubleValue() < numberRightValue.doubleValue()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.GREATER_OR_EQUAL
                            && rightExpression == Expression.LESS) {
                        if (numberObject.doubleValue() >= numberLeftValue.doubleValue()
                                && numberObject.doubleValue() < numberRightValue.doubleValue()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.GREATER_OR_EQUAL
                            && rightExpression == Expression.LESS_OR_EQUAL) {
                        if (numberObject.doubleValue() >= numberLeftValue.doubleValue()
                                && numberObject.doubleValue() <= numberRightValue.doubleValue()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.GREATER
                            && rightExpression == Expression.LESS_OR_EQUAL) {
                        if (numberObject.doubleValue() >= numberLeftValue.doubleValue()
                                && numberObject.doubleValue() <= numberRightValue.doubleValue()) {
                            result = true;
                        }
                    }

                    /*
                    if (leftExpression == Expression.LESS_OR_EQUAL
                            && rightExpression == Expression.GREATER_OR_EQUAL) {
                        if (numberLeftValue.doubleValue() >= numberObject.doubleValue()
                                && numberObject.doubleValue() <= numberRightValue.doubleValue()) {
                            result = true;
                        }
                    }
                     * 
                     */

                } else {
                    throw new IllegalArgumentException();
                }

            } else if (leftValue instanceof Date && rightValue instanceof Date) {
                if (obj instanceof Date) {
                    Date leftDateValue = (Date) leftValue;
                    Date rightDateValue = (Date) rightValue;
                    Date dateObject = (Date) obj;

                    if (leftExpression == Expression.GREATER
                            && rightExpression == Expression.LESS) {
                        if (dateObject.getTime() > leftDateValue.getTime()
                                && dateObject.getTime() < rightDateValue.getTime()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.GREATER_OR_EQUAL
                            && rightExpression == Expression.LESS) {
                        if (dateObject.getTime() >= leftDateValue.getTime()
                                && dateObject.getTime() < rightDateValue.getTime()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.GREATER_OR_EQUAL
                            && rightExpression == Expression.LESS_OR_EQUAL) {
                        if (dateObject.getTime() >= leftDateValue.getTime()
                                && dateObject.getTime() <= rightDateValue.getTime()) {
                            result = true;
                        }
                    }

                    if (leftExpression == Expression.GREATER
                            && rightExpression == Expression.LESS_OR_EQUAL) {
                        if (dateObject.getTime() > leftDateValue.getTime()
                                && dateObject.getTime() <= rightDateValue.getTime()) {
                            result = true;
                        }
                    }

                } else {
                    throw new IllegalArgumentException();
                }

            }
        }

        return result;
    }
}
