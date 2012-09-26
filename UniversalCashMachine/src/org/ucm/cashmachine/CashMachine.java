/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface CashMachine {
    
    public static final int PAYMENT_TYPE_CASH = 1, PAYMENT_TYPE_OTHER = 2;

    public CashMachineResponse printXReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException;

    public CashMachineResponse printZReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public CashMachineResponse printZReportToBuffer(int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public CashMachineResponse printZReportFromBuffer(int password) throws XPathExpressionException, NullPointerException, CashMachineException;

    /**
     * 
     * @param receipt
     * @param cash
     * @param paymentType 
     * 1-cash, 2-4
     * @param section
     * @param password
     * @return
     * @throws XPathExpressionException
     * @throws NullPointerException
     * @throws CashMachineException 
     */
    public CashMachineResponse printFiscalReceipt(Receipt receipt, 
            BigDecimal cash, int paymentType, int section, int password) throws XPathExpressionException, NullPointerException, CashMachineException;

    public CashMachineResponse printFakeReceipt(String cashMachineName, 
            ArrayList<Map<String, BigDecimal>> couponItems, BigDecimal cash) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public CashMachineResponse printString(String string, boolean isToBuffer, int password) throws XPathExpressionException, NullPointerException, CashMachineException;

    public CashMachineResponse registerOperator(int password) throws XPathExpressionException, NullPointerException, CashMachineException;

    public String getCashMachineNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public String[] getDocumentNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public String[] getEKLZNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public String[] getINN(int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public String[] getEKLZInfo(int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public BigDecimal getMoneyInCashBox(int password) throws XPathExpressionException, NullPointerException, CashMachineException;

    /**
     * Use this method for depositing money to cash machine
     * @param cash
     * @param password
     * @return
     * @throws XPathExpressionException
     * @throws NullPointerException
     * @throws CashMachineException 
     */
    public CashMachineResponse depositionMoney(BigDecimal cash, int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    /**
     * Use this method for pay from cash machine
     * @param cash
     * @param password
     * @return
     * @throws XPathExpressionException
     * @throws NullPointerException
     * @throws CashMachineException 
     */
    public CashMachineResponse paymentMoney(BigDecimal cash, int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public CashMachineResponse cancelReceipt(int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public CashMachineResponse openShift(String info, int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public CashMachineResponse closeShift(int password) throws XPathExpressionException, NullPointerException, CashMachineException;
    
    public boolean isConnect();
    
    public boolean disconnect();
}
