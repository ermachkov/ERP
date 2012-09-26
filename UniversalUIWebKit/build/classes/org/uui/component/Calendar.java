/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.json.JSONObject;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class Calendar extends Component {

    private String style = "", format;
    private Date date;
    private boolean isEnabled = true;
    private EventListenerList listenerList = new EventListenerList();

    public Calendar(String sessionId, Date date, String format) {
        super(sessionId);
        this.date = date;
        this.format = format;
    }
    
    public void addUIEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireCalendarEvent(UIEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    public String getModel() {
        StringBuilder sb = new StringBuilder();
        
        String _style = "style='" + style + "'";
        if (style == null) {
            _style = "";
        } else if (style.equals("")) {
            _style = "";
        }

        String disabled = "";
        if (!isEnabled) {
            disabled = " disabled='disabled' ";
        }

        sb.append("<input type='text' class='calendar' " + "identificator='")
                .append(getIdentificator()).append("' ").append(_style)
                .append(" " + "value='")
                .append(DateTime.getFormatedDate(format, date)).append("' ")
                .append(disabled).append(" />");

        return sb.toString();
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
                date = DateTime.getDateFromString(format, jsonObject.getString("date"));
                fireCalendarEvent(new UIEvent(json));
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
}
