/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Label extends Component {

    private String text = "", model = "", identificator = "", style = "", cssClass = "";
    private Map<String, Object> attribute = new HashMap<>();
    private EventListenerList listenerList = new EventListenerList();

    public Label(String sessionId, String text) {
        super(sessionId);
        this.text = text;
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

    public void setStyle(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }

    public String getAttribute() {
        String attr = "";
        Iterator<String> it = attribute.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = "" + attribute.get(key);
            attr += key + "='" + value + "' ";
        }

        return attr;
    }

    public Object setAttribute(String key, Object value) {
        return attribute.put(key, value);
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
            String css = "class='label' ";
            if (!s.equals("null") && !s.equals("")) {
                css = "class='" + getCssClass() + "' ";
            }

            s = Objects.toString(getStyle(), "null");
            String _style = "";
            if (!s.equals("null") && !s.equals("")) {
                _style = "style='" + getStyle() + "' ";
            }

            model = "<span " + css + " " + getAttribute() + " identificator=\""
                    + getIdentificator() + "\" " + _style + ">"
                    + getText() + "</span>";
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
        fireExplorerEvent(new UIEvent(json));
    }

    @Override
    public String toString() {
        return "Label{" + "text=" + text + ", model=" + model + '}';
    }
}
