/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.Objects;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.webkit.WebKitComponent;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class ListItem extends Component {

    private EventListenerList listenerList = new EventListenerList();
    private WebKitComponent[] webKitComponents;
    private String model = "", identificator = "";
    private String cssLayouts[];

    public ListItem(String sessionId, WebKitComponent webKitComponents[], String... cssLayouts) {
        super(sessionId);
        Objects.requireNonNull(webKitComponents);
        this.webKitComponents = webKitComponents;
        this.cssLayouts = cssLayouts;
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
            model = "<div class='listItem' identificator='" + getIdentificator() + "'>";
            int index = 0;
            for (WebKitComponent wc : webKitComponents) {
                if (cssLayouts == null) {
                    model += wc.getModel();

                } else {
                    if (cssLayouts.length - 1 >= index) {
                        model += "<div style='" + cssLayouts[index] + "'>" + wc.getModel() + "</div>";

                    } else {
                        model += "<div>" + wc.getModel() + "</div>";
                    }
                }

                index++;
            }
        }

        return model + "</div>";
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
