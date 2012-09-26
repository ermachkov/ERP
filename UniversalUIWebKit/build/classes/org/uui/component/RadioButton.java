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
public class RadioButton extends Component {

    private EventListenerList listenerList = new EventListenerList();
    private boolean isChecked;
    private String label = "", radioName = "", styleLabel = "";

    public RadioButton(String sessionId, String radioName, String label, boolean isChecked) {
        super(sessionId);
        this.radioName = radioName;
        this.label = label;
        this.isChecked = isChecked;
    }

    public String getStyleLabel() {
        return styleLabel;
    }

    public void setStyleLabel(String styleLabel) {
        this.styleLabel = styleLabel;
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

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    public String getModel() {
        String s = Objects.toString(getStyleLabel(), "null");
        String _labelStyle = "";
        if (!s.equals("null") && !s.equals("")) {
            _labelStyle = "style='" + getStyleLabel() + "' ";
        }

        String checked = "";
        if (isChecked()) {
            checked = "checked";
        }
        String model = "<input type='radio' name='" + radioName + "' "
                + " class='radioButton' "
                + "identificator='" + getIdentificator() + "' " + checked + "/>"
                + "<span " + _labelStyle + ">" + label + "</span>";
        return model;
    }

    @Override
    public void fireEvent(String json) {if(json == null){             return;         }                  if(json.equals("")){             return;         }                  if(json.equals("{}")){             return;         }
        UIEvent uiEvent = new UIEvent(json);
        try {
            isChecked = uiEvent.getJSONObject().getBoolean("checked");
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        fireExplorerEvent(uiEvent);
    }
}
