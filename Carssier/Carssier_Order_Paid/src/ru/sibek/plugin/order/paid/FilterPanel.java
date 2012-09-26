/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.order.paid;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import org.ubo.document.Order;
import org.uui.component.*;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class FilterPanel extends Component {

    private TextField txtCustomer, txtDescription, txtSum;
    private ComboBox cboPaidType;
    private RadioButton rbtDateIn, rbtDatePay;

    public FilterPanel(String sessionId) {
        super(sessionId);
        RadioButtonGroup rbtGroup = new RadioButtonGroup();
        rbtDateIn = new RadioButton(sessionId, "rbtDate", "Оформления", true);
        rbtGroup.addRadioButton(rbtDateIn);

        rbtDatePay = new RadioButton(sessionId, "rbtDate", "Оплаты", false);
        rbtGroup.addRadioButton(rbtDatePay);

        txtCustomer = new TextField(getSession());
        txtCustomer.setLabel("Заказчик");

        txtSum = new TextField(getSession());
        txtSum.setStyle("text-align: center; width:50px;");
        txtSum.setLabel("Сумма");

        txtDescription = new TextField(getSession());
        txtDescription.setLabel("Описание");

        cboPaidType = new ComboBox(getSession());
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("Все", "-1");
        map.put("Наличные", "" + Order.CASH);
        map.put("Банк", "" + Order.BANK_ACCOUNT);
        map.put("Карточка", "" + Order.CARD);
        map.put("Левые", "" + Order.LEFT);
        cboPaidType.setItems(map);
        cboPaidType.setLabel("Тип платежа");
    }

    public boolean isFindByDatePay() {
        return rbtDatePay.isChecked();
    }

    public int getPaidType() {
        return Integer.valueOf(cboPaidType.getSelectedValue().trim()).intValue();
    }

    public String getCustomerName() {
        return txtCustomer.getText().trim();
    }

    public String getDescription() {
        return txtDescription.getText().trim();
    }

    public BigDecimal getSum() {
        if (txtSum.getText().trim().equals("")) {
            return BigDecimal.ZERO;
        }

        try {
            BigDecimal sum = new BigDecimal(txtSum.getText().trim().replace(',', '.'));
            return sum;

        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String getModel() {
        String model = ""
                + "<div style='font-size:75%;background-color:lightgray;"
                + "width:100%; height:28px; padding-top:2px;'>"
                + "<div style='float:left;'>"
                + "&nbsp;<img src='img/subbuttons/filter.png' />"
                + "Искать по дате:&nbsp"
                + rbtDateIn.getModel() + rbtDatePay.getModel()
                + "</div>"
                + "<div style='float:right;'>"
                + cboPaidType.getModel()
                + "&nbsp;"
                + txtDescription.getModel()
                + "&nbsp;"
                + txtCustomer.getModel()
                + "&nbsp;"
                + txtSum.getModel()
                + "&nbsp;"
                + "</div>"
                + "</div>";

        return model;
    }
}
