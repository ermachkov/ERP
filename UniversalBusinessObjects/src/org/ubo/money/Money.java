/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.money;

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
public class Money implements MoneyModel, Serializable {

    private Locale locale;
    static final long serialVersionUID = 100000000000010002L;
    private static int ADD = 0, SUBSTRACT = 1, MULTIPLY = 2, DIVIDE = 3;
    private NumberFormat decimalFormat;
    private static Money self = null;

    private Money() {
        locale = Locale.getDefault();
        decimalFormat = NumberFormat.getInstance(locale);
    }

    public synchronized static Money getInstance() {
        if (self == null) {
            self = new Money();
        }

        return self;
    }

    @Override
    public String getAddFormatted(String val1, String val2) {
        String out = null;
        try {
            val1 = val1.replace(',', '.');
            val2 = val2.replace(',', '.');
            decimalFormat.setGroupingUsed(false);
            decimalFormat.setMinimumFractionDigits(2);
            decimalFormat.setMaximumFractionDigits(2);
            out = decimalFormat.format(getResult(val1, val2, Money.ADD).doubleValue());

        } catch (Exception e) {
            Logger.getLogger(Money.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);

        } finally {
            return out;
        }
    }

    public static String formatToMoney(double val) {
        NumberFormat decimalFormat = NumberFormat.getInstance();
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
        String str = decimalFormat.format(val);
        return str.replace(',', '.');
    }

    /**
     *
     * @param val1
     * @param val2
     * @return BigDecimal, result may be is null
     */
    public static BigDecimal ADD(String val1, String val2) {
        BigDecimal result = null;
        String val = Money.getInstance().getAddFormatted(val1, val2);
        if (val != null) {
            result = new BigDecimal(val.replace(',', '.'));
        }
        return result;
    }

    public static BigDecimal ADD(BigDecimal val1, BigDecimal val2) {
        BigDecimal result = null;
        String val = Money.getInstance().getAddFormatted(val1.toString(), val2.toString());
        if (val != null) {
            result = new BigDecimal(val.replace(',', '.'));
        }
        return result;
    }

    @Override
    public String getSubstarctFormatted(String val1, String val2) {
        String out = null;
        try {
            val1 = val1.replace(',', '.');
            val2 = val2.replace(',', '.');
            decimalFormat.setGroupingUsed(false);
            decimalFormat.setMinimumFractionDigits(2);
            decimalFormat.setMaximumFractionDigits(2);
            out = decimalFormat.format(getResult(val1, val2, Money.SUBSTRACT).doubleValue());

        } catch (Exception e) {
            Logger.getLogger(Money.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);

        } finally {
            if (out != null) {
                out = out.replace(',', '.');
            }
            return out;
        }
    }

    public static BigDecimal SUBSTRACT(String val1, String val2) {
        BigDecimal result = null;
        String val = Money.getInstance().getSubstarctFormatted(val1, val2);
        if (val != null) {
            result = new BigDecimal(val.replace(',', '.'));
        }
        return result;
    }

    public static BigDecimal SUBSTRACT(BigDecimal val1, BigDecimal val2) {
        BigDecimal result = null;
        String val = Money.getInstance().getSubstarctFormatted(val1.toString(), val2.toString());
        if (val != null) {
            result = new BigDecimal(val.replace(',', '.'));
        }
        return result;
    }

    @Override
    public String getMultiplyFormatted(String val1, String val2) {
        String out = null;
        try {
            val1 = val1.replace(',', '.');
            val2 = val2.replace(',', '.');
            decimalFormat.setGroupingUsed(false);
            decimalFormat.setMinimumFractionDigits(2);
            decimalFormat.setMaximumFractionDigits(2);
            out = decimalFormat.format(getResult(val1, val2, Money.MULTIPLY).doubleValue());

        } catch (Exception e) {
            Logger.getLogger(Money.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);

        } finally {
            if (out != null) {
                out = out.replace(',', '.');
            }
            return out;
        }
    }

    public static BigDecimal MULTIPLY(String val1, String val2) {
        BigDecimal result = null;
        String val = Money.getInstance().getMultiplyFormatted(val1, val2);
        if (val != null) {
            result = new BigDecimal(val.replace(',', '.'));
        }
        return result;
    }

    public static BigDecimal MULTIPLY(BigDecimal val1, BigDecimal val2) {
        BigDecimal result = null;
        String val = Money.getInstance().getMultiplyFormatted(val1.toString(), val2.toString());
        if (val != null) {
            result = new BigDecimal(val.replace(',', '.'));
        }
        return result;
    }

    @Override
    public String getDivideFormatted(String val1, String val2) {
        String out = null;
        try {
            val1 = val1.replace(',', '.');
            val2 = val2.replace(',', '.');
            decimalFormat.setGroupingUsed(false);
            decimalFormat.setMinimumFractionDigits(2);
            decimalFormat.setMaximumFractionDigits(2);
            out = decimalFormat.format(getResult(val1, val2, Money.DIVIDE).doubleValue());

        } catch (Exception e) {
            Logger.getLogger(Money.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);

        } finally {
            if (out != null) {
                out = out.replace(',', '.');
            }
            return out;
        }
    }

    public static BigDecimal DIVIDE(String val1, String val2) {
        BigDecimal result = null;
        String val = Money.getInstance().getDivideFormatted(val1, val2);
        if (val != null) {
            result = new BigDecimal(val.replace(',', '.'));
        }
        return result;
    }

    public static BigDecimal DIVIDE(BigDecimal val1, BigDecimal val2) {
        BigDecimal result = null;
        String val = Money.getInstance().getDivideFormatted(val1.toString(), val2.toString());
        if (val != null) {
            result = new BigDecimal(val.replace(',', '.'));
        }
        return result;
    }

    @Override
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
            Logger.getLogger(Money.class.getName()).log(Level.WARNING, MessageFormat.format("{0}, {1}",
                    new Object[]{val1, val2}), e);
        } finally {
            return result;
        }
    }

    private String stringHandler(String str) throws Exception {
        str = str.replace(',', '.');
        Float.parseFloat(str);
        return str;
    }

    @Override
    public void setLocale(String language, String country) {
        locale = new Locale(language, country);
        decimalFormat = NumberFormat.getInstance(locale);
    }
}
