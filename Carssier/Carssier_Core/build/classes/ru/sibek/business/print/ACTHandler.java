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
public class ACTHandler extends HTMLHandler {

    @Override
    public String getHTMLTable() {
        DataBase dataBase = CarssierDataBase.getDataBase();
        String table = "<table width=\"100%\" border=\"1\" cellpadding=\"5\" "
                + "cellspacing=\"0\">"
                + "<tr align=\"center\">"
                + "<th width=\"10%\">#</th>"
                + "<th width=\"50%\">Наименование</th>"
                + "<th>Кол-во</th>"
                + "<th>Цена</th>"
                + "<th>Скидка в %</th>"
                + "<th>Сумма</th>"
                + "</tr>";

        Order order = (Order) doc;
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
        return table + "</table>";
    }

    @Override
    public String getTotalSumString() {
        return super.getTotalSumString();
    }
}
