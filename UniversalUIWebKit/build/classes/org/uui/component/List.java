/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.LinkedHashSet;
import java.util.Objects;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class List extends Component {

    private EventListenerList listenerList = new EventListenerList();
    private LinkedHashSet<ListItem> listItemsSet = new LinkedHashSet<>();
    private String model = "", style = "", identificator = "", cssClass = "";

    public List(String sessionId, LinkedHashSet<ListItem> listItemsSet) {
        super(sessionId);
        this.listItemsSet = listItemsSet;
    }

    public List(String sessionId) {
        super(sessionId);
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

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        model = "";
        this.cssClass = cssClass;
    }

    public String getStyle() {
        if (style == null) {
            style = "";
        }

        if (!style.equals("")) {
            style = "style='" + style + "'";
        }
        return style;
    }

    public void setStyle(String style) {
        model = "";
        this.style = style;
    }

    public boolean addItem(ListItem item) {
        model = "";
        return listItemsSet.add(item);
    }

    public void setListItemsSet(LinkedHashSet<ListItem> listItemsSet) {
        this.listItemsSet = listItemsSet;
        model = "";
    }

    public LinkedHashSet<ListItem> getListItemsSet() {
        return listItemsSet;
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
            String css = "class='listItem' ";
            if (!s.equals("null") && !s.equals("")) {
                css = "class='" + getCssClass() + "' ";
            }

            if (style.equals("")) {
                model = "<div style='width:100%; heigth:100%' " + css + ">";

            } else {
                model = "<div " + style + " " + css + ">";
            }

            if (listItemsSet.size() > 0) {
                for (ListItem listItem : listItemsSet) {
                    model += listItem.getModel();
                }
                model = model + "</div>";

            } else {
                model = "";
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
        fireExplorerEvent(new UIEvent(json));
    }
}
