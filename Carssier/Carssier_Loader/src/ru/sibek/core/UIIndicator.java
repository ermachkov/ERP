/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import java.math.BigDecimal;
import java.util.Date;
import org.ubo.datetime.DateTime;
import org.ubo.money.Money;
import org.uui.component.Component;
import ru.sibek.business.accountbook.AccountBookHandler;
import ru.sibek.business.core.CarssierCore;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class UIIndicator extends Component {

    public UIIndicator(String session) {
        super(session);
    }

    @Override
    public String getModel() {
        AccountBookHandler.getInstance().loadSyntheticAccounts();
        BigDecimal[] balance = AccountBookHandler.getInstance().getTrialBalance("50",
                DateTime.getFormatedDate("yyyy-mm-dd HH:mm:ss", new Date()));
        BigDecimal cash = Money.SUBSTRACT(balance[0].toString(), balance[1].toString());
        
        String model = "<table width='200' height='100%'>"
                + "<tr>"
                + "<td valign='middle' align='center'>"
                + "<span style='font-size:90%; font-weight:900; color:black;'>"
                + CarssierCore.getInstance().getLoggedUser(getSession()).getName()
                + "</span><br/>"
                + "<div class='cashBox' style='font-size:90%; "
                + "font-weight:bold; color:black;' align='center;"
                + "border-top: 1px dotted black;'></div>"
                + "</td>"
                + "</tr>"
                + "</table>";

        return model;
    }
}
