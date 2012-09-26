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
public class Button extends Component {

    private EventListenerList listenerList = new EventListenerList();
      private Map<String, Object> attribute = new HashMap<>();
    private String text = "", identificator = "", cssClass = "", style = "", image = "";
    private boolean isEnabled = true;

    public Button(String sessionId, String text) {
        super(sessionId);
        this.text = text;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
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
    
    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        StringBuilder sb = new StringBuilder();

        String s = Objects.toString(getCssClass(), "null");
        String css = " class='button' ";
        if (!s.equals("null") && !s.equals("")) {
            css = " class='" + getCssClass() + "' ";
        }

        s = Objects.toString(getStyle(), "null");
        String _style = "";
        if (!s.equals("null") && !s.equals("")) {
            _style = " style='" + getStyle() + "' ";
        }

        String disabled = "";
        if (!isEnabled) {
            disabled = " disabled='true' ";
        }

        sb.append("<button identificator='").append(getIdentificator()).append("' ").append(css).append(" ").append(_style).append(" ").append(getAttribute()).append(disabled).append(">").append(getText());

        if (!image.equals("")) {
            sb.append("<i class='").append(image).append("' style='vertical-align: middle;' />");
        }
        sb.append("</button>");

        return sb.toString();
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

        if (!isEnabled) {
            return;
        }

        fireExplorerEvent(new UIEvent(json));

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
