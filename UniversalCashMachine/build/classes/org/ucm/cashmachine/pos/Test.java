/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.pos;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.ucm.cashmachine.*;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Test {

    private Path pathToSystemXML = Paths.get(System.getProperty("user.home"),
            ".saas", "app", "config", "system.xml");
    private CashMachine cashMachine;

    public Test() {
        CashMachineService service = CashMachineService.getInstance(pathToSystemXML);
        cashMachine = CashMachineFactory.getInstance().getCashMachine(
                pathToSystemXML,
                service.getDefaultCashMachine(),
                service.getConfigDir(service.getDefaultCashMachine()));
    }

    private CashMachineResponse printReciept() {
        Receipt reciept = new Receipt();
        reciept.addReceiptRow("Item 1", new BigDecimal("1"), new BigDecimal("10"));
        reciept.addReceiptRow("Item 2", new BigDecimal("2"), new BigDecimal("100"));
        reciept.addReceiptRow("Item 3", new BigDecimal("3"), new BigDecimal("1000"));

        try {
            CashMachineResponse cmr = cashMachine.printFiscalReceipt(
                    reciept,
                    new BigDecimal(4000),
                    1,
                    1,
                    48);
            return cmr;

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private CashMachineResponse paymentMoney() {
        try {
            CashMachineResponse cmr = cashMachine.paymentMoney(new BigDecimal(500), 48);
            return cmr;

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    private CashMachineResponse depositionMoney() {
        try {
            CashMachineResponse cmr = cashMachine.depositionMoney(new BigDecimal(1000), 48);
            return cmr;

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    private CashMachineResponse printZReport() {
        try {
            return cashMachine.printZReport(48);

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private CashMachineResponse printXReport() {
        try {
            return cashMachine.printXReport(48);

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static void main(String args[]) {
        Test t = new Test();

        for (int i = 0; i < 10; i++) {
            CashMachineResponse cmr = t.printReciept();
            System.out.println("printReciept, CashMachineResponse getErrorInfo = " + cmr.getErrorInfo());
            System.out.println("printReciept, CashMachineResponse = " + cmr.toString());
            System.out.println("");
        }


//        CashMachineResponse cmr = t.depositionMoney();
//        System.out.println("printZReport, CashMachineResponse = " + cmr.getErrorInfo());
//        System.out.println("printZReport, CashMachineResponse = " + cmr.toString());

//        CashMachineResponse cmr = t.paymentMoney();
//        System.out.println("printZReport, CashMachineResponse = " + cmr.getErrorInfo());
//        System.out.println("printZReport, CashMachineResponse = " + cmr.toString());
//
//        CashMachineResponse cmr = t.printZReport();
//        System.out.println("printZReport, CashMachineResponse = " + cmr.getErrorInfo());
//        System.out.println("printZReport, CashMachineResponse = " + cmr.toString());

//        CashMachineResponse cmr = t.printXReport();
//        System.out.println("printZReport, CashMachineResponse = " + cmr.getErrorInfo());
//        System.out.println("printZReport, CashMachineResponse = " + cmr.toString());

    }
}
