/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.salary.report;

import java.util.LinkedHashMap;
import org.uui.component.ComboBox;
import org.uui.component.Component;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class FilterPanel extends Component {

    private ComboBox cboMode;

    public FilterPanel(String sessionId) {
        super(sessionId);
        cboMode = new ComboBox(sessionId);
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("Нераспределенная з/п", "notDistributeSalary");
        m.put("Не выплаченная з/п", "notPaidSalary");
        m.put("Выплаченная з/п", "paidSalary");
        cboMode = new ComboBox(sessionId, m);
    }
    
    public ComboBox getMode(){
        return cboMode;
    }

    @Override
    public String getModel() {
        String model = ""
                + "<div>"
                + cboMode.getModel()
                + "</div>";

        return model;
    }
}
