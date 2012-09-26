/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;
import org.ucm.cashmachine.virtual.VirtualResponse;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class CashMachineVirtual implements CashMachine{
    
    private static CashMachineVirtual self = null;
    
    public static CashMachineVirtual getInstance() {
        if (self == null) {
            self = new CashMachineVirtual();
        }

        return self;
    }

    @Override
    public CashMachineResponse printXReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse printZReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse printZReportToBuffer(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse printZReportFromBuffer(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse printFiscalReceipt(Receipt receipt, BigDecimal cash, int paymentType, int section, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse printFakeReceipt(String cashMachineName, ArrayList<Map<String, BigDecimal>> couponItems, BigDecimal cash) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse printString(String string, boolean isToBuffer, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse registerOperator(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public BigDecimal getMoneyInCashBox(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return BigDecimal.ZERO;
    }

    @Override
    public CashMachineResponse depositionMoney(BigDecimal cash, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse paymentMoney(BigDecimal cash, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse cancelReceipt(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse openShift(String info, int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public CashMachineResponse closeShift(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return VirtualResponse.getCashMachineResponse();
    }

    @Override
    public boolean isConnect() {
        return true;
    }

    @Override
    public boolean disconnect() {
        return true;
    }

    @Override
    public String getCashMachineNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return "000000000";
    }

    @Override
    public String[] getDocumentNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        return new String[]{"000000000", "000000000"};
    }

    @Override
    public String[] getEKLZNumber(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getINN(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getEKLZInfo(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
