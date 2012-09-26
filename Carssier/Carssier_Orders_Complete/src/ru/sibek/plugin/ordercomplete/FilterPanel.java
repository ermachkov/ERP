/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.ordercomplete;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import org.ubo.document.Order;
import org.uui.component.*;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class FilterPanel extends Component {

    private RadioButton rbtDateIn, rbtDateClose;
    private TextField txtSearch, txtSum;
    private ComboBox cboPaidType, cboStatusPaid;

    public FilterPanel(String sessionId) {
        super(sessionId);
        RadioButtonGroup rbtGroup = new RadioButtonGroup();
        rbtDateIn = new RadioButton(sessionId, "rbtDate", "Оформления", true);
        rbtGroup.addRadioButton(rbtDateIn);

        rbtDateClose = new RadioButton(sessionId, "rbtDate", "Закрытия", false);
        rbtGroup.addRadioButton(rbtDateClose);

        txtSearch = new TextField(getSession());
        txtSearch.setLabel("Клиент");

        txtSum = new TextField(getSession());
        txtSum.setLabel("Сумма");
        txtSum.setStyle("text-align: center;");

        cboPaidType = new ComboBox(getSession());
        LinkedHashMap<String, String> mType = new LinkedHashMap<>();
        mType.put("Все", "-1");
        mType.put("Наличные", "" + Order.CASH);
        mType.put("Банк", "" + Order.BANK_ACCOUNT);
        mType.put("Карточка", "" + Order.CARD);
        mType.put("Левые", "" + Order.LEFT);
        cboPaidType.setItems(mType);

        cboStatusPaid = new ComboBox(getSession());
        LinkedHashMap<String, String> mStatus = new LinkedHashMap<>();
        mStatus.put("Все", "-1");
        mStatus.put("Оплачено", "" + Order.PAID);
        mStatus.put("Неоплачено", "" + Order.UNPAID);
        mStatus.put("Неподтвержден (ч/з банк)", "" + Order.WAIT_PAY_BANK);
        mStatus.put("Частично", "" + Order.CHUNK_PAID);
        cboStatusPaid.setItems(mStatus);
    }

    public int getPaidType() {
        return Integer.parseInt(cboPaidType.getSelectedValue());
    }

    public int getPaidStatus() {
        return Integer.parseInt(cboStatusPaid.getSelectedValue());
    }

    public boolean isFindByDateClosed() {
        return rbtDateClose.isChecked();
    }

    public String getCustomerName() {
        return txtSearch.getText();
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
                + "<div style='width:100%; font-size:80%; background-color:lightgray;"
                + "width:100%; height:28px; padding-top:2px;'>"
                + "<div style='float:left;'>"
                + "&nbsp;<img src='img/subbuttons/filter.png' />"
                + "Искать по дате:&nbsp"
                + rbtDateIn.getModel() + rbtDateClose.getModel()
                + "</div>"
                + "<div style='float:right;'>"
                + "&nbsp;" + cboPaidType.getModel()
                + "&nbsp;" + cboStatusPaid.getModel()
                + "&nbsp;" + txtSearch.getModel()
                + "&nbsp;" + txtSum.getModel()
                + "&nbsp;</div>"
                + "</div>";

        return model;
    }
}
