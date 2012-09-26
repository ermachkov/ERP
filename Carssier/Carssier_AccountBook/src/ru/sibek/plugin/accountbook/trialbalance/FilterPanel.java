/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.accountbook.trialbalance;

import org.uui.component.ComboBox;
import org.uui.component.Component;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class FilterPanel extends Component{
    
    private ComboBox cboAccounts;
    
    public FilterPanel(String sessionId){
        super(sessionId);
        cboAccounts = new ComboBox(sessionId);
    }
    
    public boolean isAccountsDataEmpty(){
        return cboAccounts.getItems().isEmpty();
    }
    
    public ComboBox getAccounts(){
        return cboAccounts;
    }
    
    @Override
    public String getModel(){
        String model = ""
                + "<div style='font-size:75%;background-color:lightgray;"
                + "width:100%; height:28px; padding-top:2px;'>"
                + cboAccounts.getModel()
                + "</div>";
        
        return model;
    }
    
}
