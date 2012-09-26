/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ucm.cashmachine.tests;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.ucm.cashmachine.*;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Tests {

    public Tests() {
        //
    }

    public static void main(String args[]) {
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        System.out.println("os name = " + osName);
        System.out.println("os architecture = " + osArch);

        if (osName.indexOf("Windows") != -1) {
            osName = "Windows";
        }

        try {
            Path cashMachineLibPath = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "rxtx", osName, osArch);
            System.setProperty("java.library.path", cashMachineLibPath.toString());
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            if (fieldSysPath != null) {
                fieldSysPath.set(System.class.getClassLoader(), null);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Logger.getGlobal().log(Level.WARNING, osArch, e);
        }

        Tests test = new Tests();
        test.getNumbers();
        //test.printDocByKPK();
        //test.executeEKLZCommand();
    }

    private void executeEKLZCommand() {
        Path pathToSystemXML = Paths.get(System.getProperty("user.home"),
                ".saas", "app", "config", "cashmachine", "Atol");
        CashMachine cashMachine = CashMachineFactory.getInstance().getAtolCashMachine("/dev/ttyUSB0",
                115200, 20, pathToSystemXML.toString());

        try {
            ((CashMachineAtol) cashMachine).eklzExecute("06", 48);
            System.out.println(">>>>>>>>> Cancel");

            ((CashMachineAtol) cashMachine).getDocFromEKLZ("00000005", 48);
            System.out.println(">>>>>>>>> getDocFromEKLZ");

            boolean hasMore = true;
            while(hasMore) {
                CashMachineResponse cmr = ((CashMachineAtol) cashMachine).eklzExecuteNext("05", 48);
                for(ResponseItem responseItem : cmr.getResponseItemList()){
                    if(responseItem.getValue().startsWith("00000b")){
                        System.out.println("###################### " + responseItem.getValue().replaceAll("\\.", ""));
                        hasMore = false;
                    }
                }
                System.out.println(">>>>>>>>> getDocumnetFragment");
            }

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void printDocByKPK() {
        Path pathToSystemXML = Paths.get(System.getProperty("user.home"),
                ".saas", "app", "config", "cashmachine", "Atol");
        CashMachine cashMachine = CashMachineFactory.getInstance().getAtolCashMachine("/dev/ttyUSB0",
                115200, 20, pathToSystemXML.toString());
        try {
            ((CashMachineAtol) cashMachine).printDocByKPK("00000006", 48);

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getNumbers() {
        Path pathToSystemXML = Paths.get(System.getProperty("user.home"),
                ".saas", "app", "config", "cashmachine", "Atol");
        CashMachine cashMachine = CashMachineFactory.getInstance().getAtolCashMachine("/dev/ttyUSB0",
                115200, 20, pathToSystemXML.toString());
        Receipt reciept = new Receipt();
        reciept.addReceiptRow("", new BigDecimal("10"), BigDecimal.ZERO);
        try {
            cashMachine.printFiscalReceipt(reciept, BigDecimal.ZERO, 1, 1, 48);

            String[] number = cashMachine.getDocumentNumber(48);
            System.out.println("Number -> " + number[0]);

            String eklzNumber[] = cashMachine.getEKLZNumber(48);
            System.out.println("eklzNumber -> " + Arrays.toString(eklzNumber));

            String cmNumber = cashMachine.getCashMachineNumber(48);
            System.out.println("cmNumber -> " + cmNumber);

            String[] INN = cashMachine.getINN(48);
            System.out.println("INN -> " + Arrays.toString(INN));

            String[] KPK = cashMachine.getEKLZInfo(48);
            System.out.println("KPK -> " + Arrays.toString(KPK));

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
    }
}
