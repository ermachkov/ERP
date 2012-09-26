/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class SmartSearchPanel extends SearchPanel {

    private Button button;

    public SmartSearchPanel(String sessionId, String labelText, String buttonText) {
        super(sessionId, labelText);
        super.setStyle("width:70%; font-size:80%; margin-left:5px;");
        super.setLabelStyle("font-size:80%;");
        super.setCssClass("smartSearchInputText");
        
        button = new Button(sessionId, buttonText);
        button.setCssClass("btnSmartSearchPanelAdd");
        button.setStyle("font-size:80%;");
    }
    
    public Button getAddButton(){
        return button;
    }

    @Override
    public String getModel() {
        return "<div style='width:100%; border-bottom: 1px gray solid;' class='smartSearchPanel'>" 
                + super.getModel() + button.getModel() + "</div>";
    }
}
