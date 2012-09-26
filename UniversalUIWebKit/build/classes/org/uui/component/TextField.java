/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
public class TextField extends Component {

    private String text = "", model = "", identificator = "",
            cssClass = "textField", style = "", id = "", label = "", labelStyle = "";
    private Map<String, String> attribute = new HashMap<>();
    private boolean isEnabled = true;
    private EventListenerList listenerList = new EventListenerList();

    public TextField(String sessionId) {
        super(sessionId);
    }

    public TextField(String sessionId, String text) {
        super(sessionId);
        this.text = text;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getLabelStyle() {
        return labelStyle;
    }

    public void setLabelStyle(String labelStyle) {
        this.labelStyle = labelStyle;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String key, String value) {
        attribute.put(key, value);
    }

    public void setId(String id) {
        this.id = id;
        model = "";
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
        model = "";
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
        model = "";
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    @Override
    public void setModel(String html) {
        model = html;
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
            String s = Objects.toString(getCssClass(), "null");
            String css = "class='textField' ";
            if (!s.equals("null") && !s.equals("")) {
                css = "class='" + getCssClass() + "'";
            }

            s = Objects.toString(getStyle(), "null");
            String _style = "";
            if (!s.equals("null") && !s.equals("")) {
                _style = "style='" + getStyle() + "'";
            }

            s = Objects.toString(getId(), "null");
            String _id = "";
            if (!s.equals("null") && !s.equals("")) {
                _id = "id='" + getId() + "'";
            }

            String _attr = "";
            Iterator<String> it = attribute.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = attribute.get(key);
                _attr += key + "='" + value + "' ";
            }

            s = Objects.toString(getLabelStyle(), "null");
            String _labelStyle = "";
            if (!s.equals("null") && !s.equals("")) {
                _labelStyle = "style='" + getLabelStyle() + "'";
            }

            s = Objects.toString(getLabel(), "null");
            String _label = "";
            if (!s.equals("null") && !s.equals("")) {
                _label = getLabel();
            }

            String disabled = "";
            if (!isEnabled) {
                disabled = " disabled='disabled' ";
            }

            String _text = text.replaceAll("\"", "&quot;");
            
            String append = "";
            if(!_label.trim().equals("")){
                append = "<span " + _labelStyle + ">" + _label + "</span>&nbsp;";
            }
            
            model = append + "<input type='text' value='" + _text + "' " + css + " "
                    + _style + " " + _id + " " + _attr
                    + " identificator='" + getIdentificator() + "' " + disabled + ">";
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

        UIEvent evt = new UIEvent(json);

        if (evt.getJSONObject().has("eventType")) {
            try {
                if (evt.getJSONObject().getString("eventType").equals("stopEditing")) {
                    fireExplorerEvent(evt);
                    return;
                }
                
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, json, e);
            }
        }

        try {
            setText(evt.getJSONObject().getString("value"));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, json, e);
        }

        if (evt.getJSONObject().has("extra")) {
            try {
                extraHandler(evt.getJSONObject().getString("extra"));
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, json, e);
            }
        }

        fireExplorerEvent(evt);
    }

    public void extraHandler(String extra) {
        // override code here
    }
}
