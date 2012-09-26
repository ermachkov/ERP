/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.print;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.employee.Employee;
import org.ubo.money.Money;
import org.ubo.quantity.Quantity;
import org.ubo.utils.NumberToWords;
import org.uui.db.DataBase;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class OrderToHTML {
    
    public static String convert(Order order, Path pathToTemplate){
        String html = "";
        DataBase dataBase = CarssierDataBase.getDataBase();
        String table = "<table class='orderTable' width=\"100%\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">"
                + "<tr align=\"center\">"
                + "<th width=\"10%\">#</th>"
                + "<th width=\"50%\">Наименование</th>"
                + "<th>Кол-во</th>"
                + "<th>Цена</th>"
                + "<th>Скидка в %</th>"
                + "<th>Сумма</th>"
                + "</tr>";

        int rowNum = 1;
        for (OrderRow row : order.getOrderRows()) {
            table += "<tr>"
                    + "<td align=\"center\" style=\"padding:3pt;\">" + rowNum + "</td>"
                    + "<td style=\"padding:3pt;\">"
                    + row.getSalesItem(dataBase).getShortName() + "</td>"
                    + "<td align=\"center\" style=\"padding:3pt;\">"
                    + Quantity.format(row.getCount()) + "</td>"
                    + "<td align=\"center\" style=\"padding:3pt;\">"
                    + Money.formatToMoney(row.getPrice().doubleValue()) + "</td>"
                    + "<td align=\"center\" style=\"padding:3pt;\">"
                    + row.getDiscount() + "</td>"
                    + "<td align=\"center\" style=\"padding:3pt;\">"
                    + Money.formatToMoney(row.getSumWithDiscount().doubleValue()) + "</td>"
                    + "</tr>";
            rowNum++;
        }

        table += "<tr>"
                + "<td colspan=\"5\" align=\"right\" style=\"padding:3pt;\">"
                + "ИТОГО С УЧЕТОМ СКИДОК:</td>"
                + "<td style=\"padding:3pt;\" align=\"right\">"
                + order.getTotalWithoutTotalDiscount() + "=</td>"
                + "</tr>";

        table += "<tr>"
                + "<td colspan=\"5\" align=\"right\" style=\"padding:3pt;\">"
                + "ОБЩАЯ СКИДКА " + order.getTotalPercentDiscount() + "%</td>"
                + "<td style=\"padding:3pt;\" align=\"right\">"
                + order.getTotalDiscountSum() + "=</td>"
                + "</tr>";

        table += "<tr>"
                + "<td colspan=\"5\" align=\"right\" style=\"padding:3pt;\">ИТОГО К ОПЛАТЕ:</td>"
                + "<td style=\"padding:3pt;\" align=\"right\">"
                + order.getTotalWithTotalDiscount() + "=</td>"
                + "</tr>";
        table += "</table>";
        
        try {
            BufferedReader br = Files.newBufferedReader(pathToTemplate, Charset.forName("utf-8"));
            String str;
            while ((str = br.readLine()) != null) {
                str = str.replaceAll("\\{order_id\\}", order.getId() + " от " 
                        + DateTime.getFormatedDate("dd.MM.yyyy HH:mm", order.getDate()));
                // Supplier info
                str = str.replaceAll("\\{order_supplier_short_name\\}", order.getSupplier().getShortName());
                str = str.replaceAll("\\{order_supplier_full_name\\}", order.getSupplier().getFullName());
                str = str.replaceAll("\\{order_supplier_inn\\}", order.getSupplier().getINN());
                str = str.replaceAll("\\{order_supplier_address\\}", 
                        order.getSupplier().getDefaultAddress().
                        getFormatted("{city}, {street}, {house}"));
                str = str.replaceAll("\\{order_supplier_phone\\}", 
                        order.getSupplier().getDefaultContacts().getDefaultPhone());
                str = str.replaceAll("\\{order_supplier_email\\}", 
                        order.getSupplier().getDefaultContacts().getDefaultEmail());
                str = str.replaceAll("\\{order_supplier_www\\}", 
                        order.getSupplier().getDefaultContacts().getDefaultURL());
                // Supplier bank
                str = str.replaceAll("\\{order_supplier_account_bank\\}", 
                        order.getSupplier().getDefaultAccount().getBank());
                str = str.replaceAll("\\{order_supplier_account_account\\}", 
                        order.getSupplier().getDefaultAccount().getAccount());
                str = str.replaceAll("\\{order_supplier_account_ks\\}", 
                        order.getSupplier().getDefaultAccount().getKs());
                str = str.replaceAll("\\{order_supplier_account_bik\\}", 
                        order.getSupplier().getDefaultAccount().getBik());
                str = str.replaceAll("\\{order_supplier_account_inn\\}", 
                        order.getSupplier().getDefaultAccount().getInn());
                
                // Customer info 
                str = str.replaceAll("\\{order_customer_short_name\\}", order.getCustomer().getShortName());
                str = str.replaceAll("\\{order_customer_full_name\\}", order.getSupplier().getFullName());
                str = str.replaceAll("\\{order_customer_inn\\}", order.getSupplier().getINN());
                str = str.replaceAll("\\{order_customer_address\\}", 
                        order.getSupplier().getDefaultAddress().
                        getFormatted("{city}, {street}, {house}"));
                str = str.replaceAll("\\{order_customer_phone\\}", 
                        order.getSupplier().getDefaultContacts().getDefaultPhone());
                str = str.replaceAll("\\{order_customer_email\\}", 
                        order.getSupplier().getDefaultContacts().getDefaultEmail());
                str = str.replaceAll("\\{order_customer_www\\}", 
                        order.getSupplier().getDefaultContacts().getDefaultURL());
                // Customer bank
                str = str.replaceAll("\\{order_customer_account_bank\\}", 
                        order.getSupplier().getDefaultAccount().getBank());
                str = str.replaceAll("\\{order_customer_account_account\\}", 
                        order.getSupplier().getDefaultAccount().getAccount());
                str = str.replaceAll("\\{order_customer_account_ks\\}", 
                        order.getSupplier().getDefaultAccount().getKs());
                str = str.replaceAll("\\{order_customer_account_bik\\}", 
                        order.getSupplier().getDefaultAccount().getBik());
                str = str.replaceAll("\\{order_customer_account_inn\\}", 
                        order.getSupplier().getDefaultAccount().getInn());
                
                // Table & Sum
                str = str.replaceAll("\\{order_table\\}", table);
                str = str.replaceAll("\\{order_total_sum\\}", getMoneyString(order));
                
                // Master & CashMaster
                Employee master, cashMaster;
                if((master = order.getMaster()) != null){
                    str = str.replaceAll("\\{master_full_name\\}", master.getFullName());
                    str = str.replaceAll("\\{master_short_name\\}", master.getShortName());
                }
                
                if((cashMaster = order.getCashmaster()) != null){
                    str = str.replaceAll("\\{cash_master_full_name\\}", cashMaster.getFullName());
                    str = str.replaceAll("\\{cash_master_short_name\\}", cashMaster.getShortName());
                }
                
                
                html += str;
            }
            
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, html, ex);
            return ex.getMessage();
        }
        
        return html;
    }
    
    public static String getMoneyString(Order order){
        String str = NumberToWords.convert(order.getTotalWithTotalDiscount().intValue()) + " руб. ";
        String c = Money.formatToMoney(order.getTotalWithTotalDiscount().doubleValue());
        String arr[] = c.split("\\.");
        str = str + NumberToWords.convert(Integer.parseInt(arr[1])) + " коп.";
        return str;
    }
    
}
