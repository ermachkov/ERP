/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.print;

import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.money.Money;
import org.ubo.quantity.Quantity;
import org.uui.db.DataBase;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class POSBillHandler extends HTMLHandler {

    @Override
    public String getBarcode(String height, String moduleWidth, String orientation) {
        return super.getBarcode(height, moduleWidth, orientation);
    }

    @Override
    public String getHTMLTable() {
        DataBase dataBase = CarssierDataBase.getDataBase();
        String table = "<table width=\"100%\" border=\"0\" cellpadding=\"5\" "
                + "cellspacing=\"0\">"
                + "<tr>"
                + "<td width=\"50%\" align=\"center\" style=\"font-size:8pt;"
                + "color:#FFFFFF;background-color:#000000\">Наименование</td>"
                + "<td align=\"center\" style=\"font-size:8pt;"
                + "color:#FFFFFF;background-color:#000000\">Цена</td>"
                + "<td align=\"center\" style=\"font-size:8pt;"
                + "color:#FFFFFF;background-color:#000000\">Сумма</td>"
                + "</tr>";

        Order order = (Order) doc;
        int rowNum = 1;
        for (OrderRow row : order.getOrderRows()) {
            table += "<tr>"
                    + "<td style=\"padding:3pt;font-size:8pt;\">"
                    + Quantity.format(row.getCount()) + " <b>x</b> " + row.getSalesItem(dataBase).getShortName()
                    + "</td>"
                    + "<td align=\"center\" style=\"padding:3pt;font-size:8pt;\">" + Money.formatToMoney(row.getPrice().doubleValue()) + "</td>"
                    + "<td align=\"center\"  style=\"padding:3pt;font-size:8pt;\">" + Money.formatToMoney(row.getSumWithDiscount().doubleValue()) + "</td>"
                    + "</tr>";
            rowNum++;
        }

        table += "<tr>"
                + "<td colspan=\"2\" align=\"right\" style=\"padding:3pt;"
                + "font-size:8pt;border-top-style:dashed;border-top-width:.5pt;\">"
                + "ИТОГО С УЧЕТОМ СКИДОК:</td>"
                + "<td align=\"right\" style=\"padding:3pt;font-size:8pt;"
                + "border-top-style:dashed;border-top-width:.5pt;\">"
                + order.getTotalWithoutTotalDiscount() + "=</td>"
                + "</tr>";

        table += "<tr>"
                + "<td colspan=\"2\" align=\"right\" style=\"padding:3pt;font-size:8pt\">"
                + "ОБЩАЯ СКИДКА " + order.getTotalPercentDiscount() + "%</td>"
                + "<td align=\"right\" style=\"padding:3pt;font-size:8pt\">"
                + order.getTotalDiscountSum() + "=</td>"
                + "</tr>";

        table += "<tr>"
                + "<td colspan=\"2\" align=\"right\" style=\"padding:3pt;font-size:8pt;font-weight:bold;\">"
                + "ИТОГО К ОПЛАТЕ:</td>"
                + "<td align=\"right\" style=\"padding:3pt;font-size:8pt;font-weight:bold;\">"
                + order.getTotalWithTotalDiscount() + "=</td>"
                + "</tr>";
        return table + "</table>";
    }

    @Override
    public String getTotalSumString() {
        return super.getTotalSumString();
    }
}
