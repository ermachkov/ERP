/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.orderdeferred;

import java.math.BigDecimal;
import org.uui.component.CheckBox;
import org.uui.component.Component;
import org.uui.component.TextField;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class FilterPanel extends Component {

    private TextField txtCustomer, txtSum;
    private CheckBox chkDate;

    public FilterPanel(String sessionId) { super(sessionId);
        txtCustomer = new TextField(getSession());
        txtCustomer.setLabel("Получатель");

        txtSum = new TextField(getSession());
        txtSum.setStyle("text-align: center;");
        txtSum.setLabel("Сумма");
        
        chkDate = new CheckBox(getSession(), "Поиск по дате");
        chkDate.setChecked(false);
        chkDate.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                dateSearchEnable(chkDate.isChecked());
            }
        });
    }
    
    public abstract void dateSearchEnable(boolean isEnabled);
    
    public boolean isDateSearchEnable(){
        return chkDate.isChecked();
    }

    public String getCustomerName() {
        return txtCustomer.getText().trim();
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
                + txtCustomer.getModel()
                + "&nbsp;"
                + txtSum.getModel()
                + "</div>"
                + "</div>";

        return model;
    }
}
