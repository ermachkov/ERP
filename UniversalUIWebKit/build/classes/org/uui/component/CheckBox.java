/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class CheckBox extends Component {

    private String label = "", styleLabel = "", value = "", name = "", cssClass = "";
    private boolean isChecked = false, isEnabled = true;
    private EventListenerList listenerList = new EventListenerList();

    public CheckBox(String sessionId, String label, String name, String value) {
        super(sessionId);
        this.label = label;
        this.value = value;
        this.name = name;
    }

    public CheckBox(String sessionId, String label) {
        super(sessionId);
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStyleLabel() {
        return styleLabel;
    }

    public void setStyleLabel(String styleLabel) {
        this.styleLabel = styleLabel;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isSelected) {
        this.isChecked = isSelected;
    }

    public void addUIEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireExplorerEvent(UIEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    @Override
    public String getModel() {
        String checked = "";
        if (isChecked) {
            checked = "checked";
        }

        String s = Objects.toString(getCssClass(), "null");
        String css = "class='checkBox' ";
        if (!s.equals("null") && !s.equals("")) {
            css = "class='" + getCssClass() + "' ";
        }

        s = Objects.toString(getStyleLabel(), "null");
        String _labelStyle = "";
        if (!s.equals("null") && !s.equals("")) {
            _labelStyle = "style='" + getStyleLabel() + "' ";
        }

        String disabled = "";
        if (!isEnabled) {
            disabled = " disabled='disabled' ";
        }

        StringBuilder weak = new StringBuilder();
        weak.append("<input type='checkbox' ").append(checked).append(" " + "value='").append(value).append("' name='").append(name).append("' ").append(css).append(" " + "identificator='").append(getIdentificator()).append("' ").append(disabled).append("/>" + "<span ").append(_labelStyle).append(">").append(label).append("</span>");

        return weak.toString();
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
        
        UIEvent uiEvent = new UIEvent(json);
        try {
            isChecked = uiEvent.getJSONObject().getBoolean("checked");
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        fireExplorerEvent(uiEvent);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
