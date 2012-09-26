/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.component.Component;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class TopButton extends Component {

    private String name;

    public TopButton(String sessionId, String name) {
        super(sessionId);
        this.name = name;
    }

    @Override
    public String getModel() {
        String model = ""
                + "<td align='center' valign='middle' "
                + "class='ribbon_top_button' "
                + "identificator='" + getIdentificator() + "' topButtonName='" + name + "'>"
                + name
                + "</td>";

        return model;
    }

    @Override
    public void fireEvent(String json) {
        if (json == null) {
            return;
        }
        if (json.equals("")) {
            return;
        }
        if (json.equals("{}")) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            setSession(jsonObject.getString("session"));
            selected(json);

        } catch (JSONException ex) {
            Logger.getLogger(LoginHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public abstract void selected(String json);
}
