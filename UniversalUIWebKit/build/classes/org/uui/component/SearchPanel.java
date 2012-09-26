/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONObject;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class SearchPanel extends Component {

    public String model = "", identificator = "", label = "", style = "",
            labelStyle = "", cssClass = "", text = "";
    private boolean isEnabled = true;
    private EventListenerList listenerList = new EventListenerList();

    public SearchPanel(String sessionId) {
        super(sessionId);
    }

    public SearchPanel(String sessionId, String label) {
        super(sessionId);
        this.label = label;
    }

    public void setEnable(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        model = "";
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
        model = "";
    }

    public String getLabelStyle() {
        return labelStyle;
    }

    public void setLabelStyle(String labelStyle) {
        this.labelStyle = labelStyle;
        model = "";
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
        model = "";
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

    public String getLabel() {
        if (label == null) {
            label = "";
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void setModel(String html) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getModel() {
        boolean isDefined = true;
        if (model == null) {
            isDefined = false;

        } else if (model.equals("")) {
            isDefined = false;
        }

        if (!isDefined) {//друг
            String s = Objects.toString(getStyle(), "null");
            String _style = "";
            if (!s.equals("null") && !s.equals("")) {
                _style = "style='" + getStyle() + "' ";
            }

            s = Objects.toString(getCssClass(), "null");
            String _css = "class='liveSearch' ";
            if (!s.equals("null") && !s.equals("")) {
                _css = "class='" + getCssClass() + "' ";
            }

            s = Objects.toString(getLabelStyle(), "null");
            String _labelStyle = "";
            if (!s.equals("null") && !s.equals("")) {
                _labelStyle = "style='" + getLabelStyle() + "' ";
            }

            String disabled = "";
            if (!isEnabled) {
                disabled = " disabled='disabled' ";
            }

            model = "<span " + _labelStyle + ">" + getLabel() + "</span>"
                    + "<input type='text' " + _style
                    + "identificator='" + getIdentificator() + "' "
                    + _css + " value='" + text + "' " + disabled + "/>";
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
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.getString("eventType").equals("change")) {
                setText(jsonObject.getString("data"));
            }

            if (jsonObject.getString("eventType").equals("keyup")) {
                setText(jsonObject.getString("value"));
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, json, e);
        }

        fireExplorerEvent(new UIEvent(json));
    }
}
