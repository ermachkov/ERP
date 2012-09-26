/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.order.unpaid;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import org.ubo.document.Order;
import org.uui.component.CheckBox;
import org.uui.component.ComboBox;
import org.uui.component.Component;
import org.uui.component.TextField;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class FilterPanel extends Component {

    private TextField txtCustomer, txtDescription, txtSum;
    private CheckBox chkDate;
    private ComboBox cboPaidType;

    public FilterPanel(String sessionId) { super(sessionId);
        txtCustomer = new TextField(getSession());
        txtCustomer.setLabel("Получатель");

        txtSum = new TextField(getSession());
        txtSum.setStyle("text-align: center;");
        txtSum.setLabel("Сумма");
        
        txtDescription = new TextField(getSession());
        txtDescription.setLabel("Описание");
        
        chkDate = new CheckBox(getSession(), "Поиск по дате");
        chkDate.setChecked(false);
        chkDate.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                //dateSearchEnable(chkDate.isChecked());
            }
        });
        
        cboPaidType = new ComboBox(getSession());
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("Все", "-1");
        map.put("Наличные", "" + Order.UNPAID);
        map.put("Банк", "" + Order.WAIT_PAY_BANK);
        cboPaidType.setItems(map);
        cboPaidType.setLabel("Вид платежа");
    }
    
    public int getPaidStatus(){
        return Integer.valueOf(cboPaidType.getSelectedValue().trim()).intValue();
    }
    
    public abstract void dateSearchEnable(boolean isEnabled);
    
    public boolean isDateSearchEnable(){
        return chkDate.isChecked();
    }

    public String getCustomerName() {
        return txtCustomer.getText().trim();
    }
    
    public String getDescription(){
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
                + chkDate.getModel()
                + "</div>"
                + "<div style='float:right;'>"
                + cboPaidType.getModel()
                + "&nbsp;"
                + txtDescription.getModel()
                + "&nbsp;"
                + txtCustomer.getModel()
                + "&nbsp;"
                + txtSum.getModel()
                + "</div>"
                + "</div>";

        return model;
    }
}
