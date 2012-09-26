/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ubo.money;

import java.math.BigDecimal;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface MoneyModel {

    public String getAddFormatted(String val1, String val2);

    public String getSubstarctFormatted(String val1, String val2);

    public String getMultiplyFormatted(String val1, String val2);

    public String getDivideFormatted(String val1, String val2);

    public BigDecimal getResult(String val1, String val2, int action);

    /**
     *
     * @param language lowercase
     * @param country uppercase
     */
    public void setLocale(String language, String country);

}
