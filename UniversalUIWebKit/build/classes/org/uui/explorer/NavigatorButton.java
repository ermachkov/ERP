/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.explorer;

import org.uui.component.Component;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class NavigatorButton extends Component {

    private String className, model = "", text = "", identificator = "";
    private long id;
    private EventListenerList listenerList = new EventListenerList();

    public NavigatorButton(String sessionId, String className, long id, String text) {
        super(sessionId);
        this.className = className;
        this.id = id;
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
            model = "<button class=\"navigatorButton\" className=\"" + className + "\" "
                    + "dbid=\"" + id + "\" identificator=\"" + getIdentificator() + "\">"
                    + text + "</button>";
        }

        return model;
    }

    @Override
    public String getIdentificator() {
        if(identificator == null){
            identificator = "" + hashCode();
            
        } else if(identificator.equals("")){
            identificator = "" + hashCode();
        }
        
        return identificator;
    }

    @Override
    public void setIdentificator(String identificator) {
        this.identificator = identificator;
    }

    @Override
    public void fireEvent(String json) {if(json == null){             return;         }                  if(json.equals("")){             return;         }                  if(json.equals("{}")){             return;         }
        fireExplorerEvent(new UIEvent(json));
    }
}
