/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class ComboBox extends Component {

    private LinkedHashMap<String, String> data;
    private String model = "", label = "", labelStyle = "", comboStyle = "",
            identificator = "", cssClass = "comboBox", id = "";
    private boolean isEnabled = true;
    private int selectedIndex = -1;
    private String selectedValue = "";
    private EventListenerList listenerList = new EventListenerList();

    public ComboBox(String sessionId, String label, String labelStyle, LinkedHashMap<String, String> data, String comboStyle) {
        super(sessionId);
        Objects.requireNonNull(data, "Data can't be null");
        this.data = data;
        this.label = label;
        this.labelStyle = labelStyle;
        this.comboStyle = comboStyle;
    }

    public ComboBox(String sessionId, LinkedHashMap<String, String> data) {
        super(sessionId);
        Objects.requireNonNull(data, "Data can't be null");
        this.data = data;
        selectedIndex = 0;
        if (!data.isEmpty()) {
            selectedValue = data.get(data.keySet().toArray(new String[data.size()])[0]);
        }

    }

    public ComboBox(String sessionId) {
        super(sessionId);
        this.data = new LinkedHashMap<>();
        this.selectedIndex = 0;
        this.selectedValue = "";
    }

    public LinkedHashMap<String, String> getItems() {
        return data;
    }

    /**
     * 
     * @param data 
     * LinkedHashMap should have structure key(label) -> value
     */
    public void setItems(LinkedHashMap<String, String> data) {
        this.data = data;
        model = "";
    }

    public void setSelectedIndex(int index) {
        if (index < 0) {
            return;
        }

        selectedIndex = index;

        Iterator<String> it = data.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            String key = it.next();
            String value = data.get(key);
            if (i == index) {
                selectedValue = value;
                break;
            }
            i++;
        }

        model = "";
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        model = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        model = "";
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
        model = "";
    }

    public String getStyle() {
        return comboStyle;
    }

    public void setStyle(String comboStyle) {
        this.comboStyle = comboStyle;
        model = "";
    }

    public String getLabelStyle() {
        return labelStyle;
    }

    public void setLabelStyle(String labelStyle) {
        this.labelStyle = labelStyle;
        model = "";
    }

    public String getLabel() {
        if (label == null) {
            label = "";
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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
            String options = "";
            Iterator<String> it = data.keySet().iterator();
            int index = 0;
            while (it.hasNext()) {
                String key = it.next();
                String value = data.get(key);
                if (selectedIndex != -1) {
                    if (index == selectedIndex) {
                        options += "<option value='" + value + "' selected>" + key + "</option>";

                    } else {
                        options += "<option value='" + value + "'>" + key + "</option>";
                    }

                } else {
                    options += "<option value='" + value + "'>" + key + "</option>";
                }

                index++;
            }

            String s = Objects.toString(getCssClass(), "null");
            String css = "";
            if (!s.equals("null") && !s.equals("")) {
                css = "class='" + getCssClass() + "'";
            }

            s = Objects.toString(getStyle(), "null");
            String _style = "";
            if (!s.equals("null") && !s.equals("")) {
                _style = "style='" + getStyle() + "' ";
            }

            s = Objects.toString(getId(), "null");
            String _id = "";
            if (!s.equals("null") && !s.equals("")) {
                _id = "id='" + getId() + "'";
            }

            s = Objects.toString(getLabelStyle(), "null");
            String _labelStyle = "";
            if (!s.equals("null") && !s.equals("")) {
                _labelStyle = "style='" + getLabelStyle() + "'";
            }

            String disabled = "";
            if (!isEnabled) {
                disabled = " disabled='disabled' ";
            }

            model = "<span " + _labelStyle + ">" + getLabel() + "</span>"
                    + "<select identificator='" + getIdentificator() + "' "
                    + css + " " + _style + " " + _id + " " + disabled + ">"
                    + options + "</select>";
        }

        return model;
    }

    public void setSelectedValue(String value) {
        Iterator<String> it = data.keySet().iterator();
        int i = 0, index = -1;
        while (it.hasNext()) {
            String k = it.next();
            String val = data.get(k);
            if (val.equals(value)) {
                index = i;
                break;
            }
            i++;
        }

        if (index >= 0) {
            setSelectedIndex(index);
        }
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

    /**
     * 
     * @return 
     * Return selected value
     */
    public String getSelectedValue() {
        Iterator<String> it = data.keySet().iterator();
        int i = 0;
        while(it.hasNext()){
            String key = it.next();
            if(i == selectedIndex){
                selectedValue = data.get(key);
            }
            i++;
        }
        return selectedValue;
    }

    /**
     * 
     * @return 
     * Return selected key (label)
     */
    public String getSelectedKey() {
        return data.keySet().toArray(new String[data.keySet().size()])[selectedIndex];
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
            //selectedValue = jsonObject.getString("selectedValue");
            selectedIndex = jsonObject.getInt("selectedIndex");
            model = null;

            fireExplorerEvent(new UIEvent(json));

        } catch (JSONException ex) {
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    @Override
    public String toString() {
        return "ComboBox{" + "data=" + data + ", model=" + model + ", "
                + "label=" + label + ", identificator=" + identificator + ", "
                + "listenerList=" + listenerList + '}';
    }
}
