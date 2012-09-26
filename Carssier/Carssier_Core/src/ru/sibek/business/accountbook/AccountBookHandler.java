/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.accountbook;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.accountbook.Account;
import org.ubo.accountbook.AccountBook;
import org.ubo.accountbook.AccountRule;
import org.ubo.accountbook.SyntheticAccount;
import org.ubo.datetime.DateTime;
import org.ubo.money.Money;
import org.ubo.utils.XPathUtil;
import org.uui.db.Condition;
import org.uui.db.DataBase;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class AccountBookHandler {

    private static AccountBookHandler self = null;
    private DataBase dataBase = CarssierDataBase.getDataBase();
    private AccountBook accountBook;
    private ArrayList<SyntheticAccount> resultSet = new ArrayList<>();

    public synchronized static AccountBookHandler getInstance() {
        if (self == null) {
            self = new AccountBookHandler();
        }

        return self;
    }
    
    public void loadSyntheticAccounts(){
        resultSet = dataBase.getAllObjectsList(SyntheticAccount.class.getName());
    }

    private AccountBookHandler() {
        ArrayList<AccountBook> list = dataBase.getAllObjectsList(AccountBook.class.getName());
        if (list.isEmpty()) {
            AccountBook _accountBook = new AccountBook();
            dataBase.addObject(_accountBook);
            accountBook = _accountBook;
        } else {
            accountBook = list.get(0);
        }

        ArrayList<Account> accountList = dataBase.getAllObjectsList(SyntheticAccount.class.getName());
        if (accountList.isEmpty()) {
            try {
                ByteArrayOutputStream baos;
                try (InputStream is = getClass().getResourceAsStream("/org/ubo/accountbook/accounts.xml")) {
                    baos = new ByteArrayOutputStream();
                    byte[] bytes = new byte[4096];
                    for (int len; (len = is.read(bytes)) > 0;) {
                        baos.write(bytes, 0, len);
                    }
                }

                String xml = new String(baos.toByteArray());
                ArrayList<Map<String, String>> l = XPathUtil.getNodesFromXMLString(
                        "//root/accounts/account", xml);
                for (Map<String, String> m : l) {
                    ArrayList<Map<String, String>> lsub = XPathUtil.getNodesFromXMLString(
                            "//root/accounts/account[@number='" + m.get("number") + "']/subaccount", xml);
                    if (lsub.isEmpty()) {
                        SyntheticAccount syntheticAccount = new SyntheticAccount();
                        syntheticAccount.setNumber(m.get("number"));
                        syntheticAccount.setName(m.get("name"));
                        syntheticAccount.setDescription(m.get("desc"));
                        if (dataBase.addObject(syntheticAccount) < 0) {
                            Logger.getGlobal().log(Level.SEVERE, "Can't added: {0}", syntheticAccount);
                            System.exit(-1000);
                        }
                        Logger.getGlobal().log(Level.INFO, "Added: {0}", syntheticAccount);

                    } else {
                        SyntheticAccount syntheticAccount = new SyntheticAccount();
                        syntheticAccount.setNumber(m.get("number"));
                        syntheticAccount.setName(m.get("name"));
                        syntheticAccount.setDescription(m.get("desc"));
                        if (dataBase.addObject(syntheticAccount) < 0) {
                            Logger.getGlobal().log(Level.SEVERE, "Can't added: {0}", syntheticAccount);
                            System.exit(-1000);
                        }
                        Logger.getGlobal().log(Level.INFO, "Added: {0}", syntheticAccount);

                        for (Map<String, String> msub : lsub) {
                            syntheticAccount = new SyntheticAccount();
                            syntheticAccount.setNumber(m.get("number") + "." + msub.get("number"));
                            syntheticAccount.setName(msub.get("name"));
                            if (dataBase.addObject(syntheticAccount) < 0) {
                                Logger.getGlobal().log(Level.SEVERE, "Can't added: {0}", syntheticAccount);
                                System.exit(-1000);
                            }
                            Logger.getGlobal().log(Level.INFO, "Added: {0}", syntheticAccount);
                        }
                    }
                }
            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, null, e);
            }
        }

        ArrayList<AccountRule> accountRuleList = dataBase.getAllObjectsList(AccountRule.class.getName());
        try {
            ByteArrayOutputStream baos;
            try (InputStream is = getClass().getResourceAsStream(
                            "/ru/sibek/business/accountbook/AccountRules.xml")) {
                baos = new ByteArrayOutputStream();
                byte[] bytes = new byte[4096];
                for (int len; (len = is.read(bytes)) > 0;) {
                    baos.write(bytes, 0, len);
                }
            }

            String xml = new String(baos.toByteArray());
            ArrayList<Map<String, String>> l = XPathUtil.getNodesFromXMLString(
                    "//root/rules/rule", xml);

            if (accountRuleList.size() != l.size()) {
                dataBase.deleteAll(AccountRule.class.getName());

                for (Map<String, String> m : l) {
                    ArrayList<Map<String, String>> lRule = XPathUtil.getNodesFromXMLString(
                            "//root/rules/rule[@name='" + m.get("name") + "']/accounts/account", xml);
                    ArrayList<SyntheticAccount> saList = new ArrayList<>();
                    for (Map<String, String> mRule : lRule) {
                        try {
                            SyntheticAccount sAccount = (SyntheticAccount) dataBase.getObject(
                                    SyntheticAccount.class.getName(),
                                    "getNumber", mRule.get("number"));
                            if (mRule.get("type").equals("credit")) {
                                sAccount.setAsCredit();
                            } else {
                                sAccount.setAsDebet();
                            }
                            saList.add(sAccount);
                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.SEVERE, "getNumber " + mRule.get("number"), e);
                            System.exit(-1000);
                        }
                    }

                    AccountRule accountRule = AccountRule.newAccountRule(
                            m.get("name"), saList.toArray(new SyntheticAccount[saList.size()]));
                    dataBase.addObject(accountRule);
                }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }
    }

    public AccountRule getAccountRuleByName(String name) {
        ArrayList<AccountRule> list = dataBase.getFilteredResultList(
                AccountRule.class.getName(),
                "getName",
                Condition.newConditionEquial(name));
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public AccountBook getAccountBook() {
        return accountBook;
    }

    /**
     * Get accountant plan
     *
     * @return
     */
    public ArrayList<Account> getAccounts() {
        ArrayList<Account> accounts = new ArrayList<>();
        ArrayList<SyntheticAccount> list = dataBase.getAllObjectsList(SyntheticAccount.class.getName());
        for (SyntheticAccount account : list) {
            if (account.getDate() == null) {
                accounts.add(account);
            }
        }

        Collections.sort(accounts, new Comparator<Account>() {
            @Override
            public int compare(Account o1, Account o2) {
                return o1.getNumber().compareTo(o2.getNumber());
            }
        });

        return accounts;
    }

    public LinkedHashMap<String, String> getUsedAccount() {
        Set<Account> accounts = new HashSet<>();
        ArrayList<AccountRule> list = dataBase.getAllObjectsList(AccountRule.class.getName());
        for (AccountRule accountRule : list) {
            accounts.addAll(accountRule.getAccountSet());
        }

        ArrayList<Account> al = new ArrayList<>();
        al.addAll(accounts);
        Collections.sort(al, new Comparator<Account>() {
            @Override
            public int compare(Account a1, Account a2) {
                return a1.getNumber().compareTo(a2.getNumber());
            }
        });

        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Account account : al) {
            result.put(account.getNumber() + " " + account.getName(), account.getNumber());
        }

        return result;
    }

    public ArrayList<SyntheticAccount> getAccountsByDates(String accountNumber, String dateStart, String dateEnd) {
        long timeStart = DateTime.getDateFromString("yyyy-MM-dd HH:mm:ss", dateStart).getTime();
        long timeEnd = DateTime.getDateFromString("yyyy-MM-dd HH:mm:ss", dateEnd).getTime();

        ArrayList<SyntheticAccount> result = new ArrayList<>();
        //ArrayList<SyntheticAccount> resultSet = dataBase.getAllObjectsList(SyntheticAccount.class.getName());
        for (SyntheticAccount account : resultSet) {

            if (account.getDate() == null) {
                continue;
            }

            if (account.getNumber() == null) {
                continue;
            }

            if (account.getDate().getTime() < timeStart) {
                continue;
            }

            if (account.getDate().getTime() > timeEnd) {
                continue;
            }

            if (!account.getNumber().equals(accountNumber)) {
                continue;
            }

            result.add(account);
        }

        Collections.sort(result, new Comparator<SyntheticAccount>() {
            @Override
            public int compare(SyntheticAccount o1, SyntheticAccount o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        return result;
    }

    /**
     *
     * @param accountNumber
     * @param dateBefore
     * @return BigDecimal array where index 0 is trial of debet, index 1 is
     * trial of credit
     */
    public BigDecimal[] getTrialBalance(String accountNumber, String dateBefore) {
        
        if(resultSet.isEmpty()){
            loadSyntheticAccounts();
        }
        
        BigDecimal[] saldo = new BigDecimal[2];
        saldo[0] = BigDecimal.ZERO;
        saldo[1] = BigDecimal.ZERO;
        
        long timeBefore = DateTime.getDateFromString("yyyy-MM-dd HH:mm:ss", dateBefore).getTime();

        ArrayList<SyntheticAccount> result = new ArrayList<>();
        //ArrayList<SyntheticAccount> resultSet = dataBase.getAllObjectsList(SyntheticAccount.class.getName());
        for (SyntheticAccount account : resultSet) {
            if(account.getDate() == null){
                continue;
            }
            
            if(account.getNumber() == null){
                continue;
            }
            
            if (!account.getNumber().equals(accountNumber)) {
                continue;
            }

            if (account.getDate().getTime() > timeBefore) {
                continue;
            }

            result.add(account);
        }

        if (result.isEmpty()) {
            return saldo;
        }

        for (SyntheticAccount account : result) {
            if (account.isDebet()) {
                saldo[0] = Money.ADD(saldo[0].toString(), account.getValue().toString());
            } else {
                saldo[1] = Money.ADD(saldo[1].toString(), account.getValue().toString());
            }
        }

        return saldo;
    }

    public ArrayList<Account> getAccounts(String accountNumber) {
        ArrayList<Account> result = new ArrayList<>();
        ArrayList<SyntheticAccount> list = dataBase.getAllObjectsList(SyntheticAccount.class.getName());
        for (Account account : list) {
            if (account.getDate() == null) {
                continue;
            }

            if (account.getNumber().equals(accountNumber)) {
                result.add(account);
            }
        }

        return result;
    }
}
//public ArrayList<SyntheticAccount> getAccountsByDates(String accountNumber, String dateStart, String dateEnd) {
//        
//        long dateTimeStart = DateTime.getDateFromString("yyyy-MM-dd HH:mm:ss", dateStart).getTime();
//        long dateTimeEnd = DateTime.getDateFromString("yyyy-MM-dd HH:mm:ss", dateEnd).getTime();
//        
//        System.out.println("Try get result set");
//        ArrayList<SyntheticAccount> resultSet = dataBase.getAllObjectsList(
//                SyntheticAccount.class.getName());
//        System.out.println("resultSet " + resultSet.size());
//        
//        ArrayList<SyntheticAccount> result = new ArrayList<>();
//        for(SyntheticAccount acc : resultSet){
//            System.out.println("SyntheticAccount acc " + acc);
//            if(acc.getDate() == null){
//                continue;
//            }
//            
//            if(acc.getNumber() == null){
//                continue;
//            }
//            
//            if(acc.getDate().getTime() >= dateTimeStart 
//                    && acc.getDate().getTime() <= dateTimeEnd
//                    && acc.getNumber().equals(accountNumber)){
//                System.out.println("SyntheticAccount ADDED " + acc);
//                result.add(acc);
//            }
//        }
//
//        Collections.sort(result, new Comparator<SyntheticAccount>() {
//
//            @Override
//            public int compare(SyntheticAccount o1, SyntheticAccount o2) {
//                return o1.getDate().compareTo(o2.getDate());
//            }
//        });
//
//        return result;
//    }
