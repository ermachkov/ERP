/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 16.02.2011 (C) Copyright by Zubanov Dmitry
 */
package org.uui.table;

import java.util.HashMap;
import java.util.Map;
import org.uui.component.Component;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

public class MacTableCell extends Component {

    private Object value;
    private boolean isEditable, isSpinnerEnable = false;
    private String style = "white-space:nowrap;";
    public Map<String, Object> additionValueMap = new HashMap<>();
    private EventListenerList listenerList = new EventListenerList();

    public MacTableCell(String sessionId, Object value, boolean isEditable) {
        super(sessionId);
        this.value = value;
        this.isEditable = isEditable;
    }
    
    public MacTableCell(String sessionId, Object value, boolean isEditable, boolean isSpinnerEnable) {
        super(sessionId);
        this.value = value;
        this.isEditable = isEditable;
        this.isSpinnerEnable = isSpinnerEnable;
    }
    
    public void setValue(String key, Object value){
        additionValueMap.put(key, value);
    }
    
    public Object getValue(String key){
        return additionValueMap.get(key);
    }
    
    public Map<String, Object> getAdditionValueMap(){
        return additionValueMap;
    }
    
    public boolean isSpinnerEnabled(){
        return isSpinnerEnable;
    }

    public String getStyle() {
        if (style == null) {
            style = "white-space:nowrap;";
        }
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MacTableCell{" + "value=" + value + ",isEditable=" + isEditable + '}';
    }

    @Override
    public String getIdentificator() {
        return "" + hashCode();
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
}
