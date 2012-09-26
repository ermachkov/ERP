/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import org.uui.db.DataBase;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class PropertiesPanel extends Component{
    
    public String model, identificator;
    public Button btnApply;
    
    public PropertiesPanel(String sessionId, DataBase db){
        super(sessionId);
        btnApply = new Button(sessionId, "Применить");
    }
    
    public PropertiesPanel(String sessionId, DataBase db, String className){
        super(sessionId);
        btnApply = new Button(sessionId, "Применить");
    }

    @Override
    public void setModel(String html) {
        this.model = html;
    }
    

    @Override
    public String getIdentificator() {
        String _identificator = identificator;

        if (identificator == null) {
            _identificator = "" + hashCode();

        } else if (identificator.equals("")) {
            _identificator = "" + hashCode();
        }

        return _identificator;
    }

    @Override
    public void setIdentificator(String identificator) {
        this.identificator = identificator;
    }
    
}
