/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.accountbook.trialbalance;

import java.math.BigDecimal;
import org.ubo.money.Money;
import org.ubo.quantity.Quantity;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class AccountSum {

    private BigDecimal debetSum = BigDecimal.ZERO;
    private BigDecimal creditSum = BigDecimal.ZERO;
    private BigDecimal debetCount = BigDecimal.ZERO;
    private BigDecimal creditCount = BigDecimal.ZERO;

    public AccountSum() {
        //
    }

    public BigDecimal addCreditCount(BigDecimal quantity) {
        creditCount = Quantity.ADD(creditCount.toString(), quantity.toString());
        return creditCount;
    }

    public BigDecimal addDebetCount(BigDecimal quantity) {
        debetCount = Quantity.ADD(creditCount.toString(), quantity.toString());
        return debetCount;
    }

    public BigDecimal addCredit(BigDecimal sum) {
        creditSum = Money.ADD(creditSum, sum);
        return creditSum;
    }

    public BigDecimal addDebet(BigDecimal sum) {
        debetSum = Money.ADD(debetSum, sum);
        return debetSum;
    }

    public BigDecimal getCreditSum() {
        return creditSum;
    }

    public BigDecimal getDebetSum() {
        return debetSum;
    }

    public BigDecimal getCreditCount() {
        return creditCount;
    }

    public BigDecimal getDebetCount() {
        return debetCount;
    }

    @Override
    public String toString() {
        return "AccountSum{" + "debetSum=" + debetSum + ", creditSum=" + creditSum + ", debetCount=" + debetCount + ", creditCount=" + creditCount + '}';
    }
}
