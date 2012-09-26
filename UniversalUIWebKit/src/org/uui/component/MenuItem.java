/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class MenuItem extends Component {

    private String text = "", model = "", identificator = "", action = "", imageIcon;
    private EventListenerList listenerList = new EventListenerList();

    public MenuItem(String sessionId, String text) {
        super(sessionId);
        this.text = text;
    }

    public MenuItem(String sessionId, String imageIcon, String text) {
        super(sessionId);
        this.imageIcon = imageIcon;
        this.text = text;
    }

    public void addMenuEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireMenuEvent(UIEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    public String getAction() {
        if (action == null) {
            action = "";
        }
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public void setModel(String html) {
        this.model = html;
    }

    @Override
    public String getModel() {
        boolean isDefined = true;
        if (model == null) {
            isDefined = false;

        } else if (model.equals("")) {
            isDefined = false;
        }

        if (!isDefined) {
            String _action = "";
            if (!getAction().equals("")) {
                _action = "action='" + getAction() + "'";
            }

            if (imageIcon != null) {
                model = "<div class='contextMenuItem' "
                        + "identificator='" + getIdentificator() + "' "
                        + _action + ">"
                        + "<table width='100%'>"
                        + "<tr>"
                        + "<td width='24'><img src='" + imageIcon + "'/></td>"
                        + "<td valign='middle' style='padding-left:5px;'>"
                        + text
                        + "</td>"
                        + "</tr>"
                        + "</table>"
                        + "</div>";

            } else {
                model = "<div class='contextMenuItem' "
                        + "identificator='" + getIdentificator() + "' "
                        + _action + ">"
                        + "<table width='100%'>"
                        + "<tr>"
                        + "<td>"
                        + text
                        + "</td>"
                        + "</tr>"
                        + "</table>"
                        + "</div>";
            }
        }

        return model;

    }

    @Override
    public String getIdentificator() {
        if (identificator == null) {
            identificator = "" + hashCode();

        } else if (identificator.equals("")) {
            identificator = "" + hashCode();
        }

        return identificator;
    }

    @Override
    public void setIdentificator(String identificator) {
        this.identificator = identificator;
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
        fireMenuEvent(new UIEvent(json));
    }

    public String toString() {
        return "MenuItem{" + "text=" + text + ", model=" + model + ", identificator=" + identificator + '}';
    }
}
