/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.orderinwork;

import java.math.BigDecimal;
import org.uui.component.Component;
import org.uui.component.TextField;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class FilterPanel extends Component {

    private TextField txtCustomer, txtSum;

    public FilterPanel(String sessionId) { super(sessionId);
        txtCustomer = new TextField(getSession());
        txtCustomer.setLabel("Получатель");

        txtSum = new TextField(getSession());
        txtSum.setLabel("Сумма");
        txtSum.setStyle("text-align: center;");
    }

    public String getCustomer() {
        return txtCustomer.getText();
    }

    public BigDecimal getSum() {
        BigDecimal sum = BigDecimal.ZERO;
        try {
            sum = new BigDecimal(txtSum.getText().replace(',', '.'));
        } catch (Exception e) {
        }
        
        return sum;
    }

    @Override
    public String getModel() {
        String model = ""
                + "<div style='width:100%; font-size:80%; background-color:lightgray;'>"
                + "&nbsp;<img src='img/subbuttons/filter.png' />"
                + "&nbsp;" + txtCustomer.getModel()
                + "&nbsp;" + txtSum.getModel()
                + "<div>";

        return model;
    }
}
