/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Spinner extends Component {

    private Button btnDecrement, btnIncrement;
    private TextField txtText;
    private BigDecimal value = BigDecimal.ZERO;
    private BigDecimal step = new BigDecimal(1);
    private Map<String, String> attribute = new HashMap<>();
    private EventListenerList listenerList = new EventListenerList();
    private boolean negativeMode = false;
    private String style = "white-space: nowrap; width:30%;";

    public Spinner(String sessionId, Number value, Number step) {
        super(sessionId);
        this.value = new BigDecimal(value.doubleValue());
        this.step = new BigDecimal(step.doubleValue());
        init();
    }

    public Spinner(String sessionId) {
        super(sessionId);
        this.value = new BigDecimal(0);
        this.step = new BigDecimal(1);
        init();
    }

    public Spinner(String sessionId, String value, Number step) {
        super(sessionId);
        this.value = new BigDecimal(value);
        this.step = new BigDecimal("" + step);
        init();
    }

    public void setNegativeValueEnable(boolean isEnable) {
        negativeMode = isEnable;
    }

    public boolean isNegativeEnable() {
        return negativeMode;
    }

    public void addUIEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireSpinnerEvent(UIEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    private void init() {
        btnDecrement = new Button(getSession(), "-");
        btnDecrement.setCssClass("spinnerButton");
        btnDecrement.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                BigDecimal _value = value.subtract(step);
                if (negativeMode) {
                    value = _value;

                } else {
                    if (_value.doubleValue() > 0) {
                        value = _value;
                    }
                }
                buttonEvent();
            }
        });


        btnIncrement = new Button(getSession(), "+");
        btnIncrement.setCssClass("spinnerButton");
        btnIncrement.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                value = value.add(step);
                buttonEvent();
            }
        });

        txtText = new TextField(getSession(), "" + value.intValue());
        txtText.setStyle("width:30%; text-align:center;");
        txtText.setCssClass("macTableCellEditor");
        txtText.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                setValue(txtText.getText());
                fireSpinnerEvent(evt);
            }
        });
    }

    public void setStyleTextField(String style) {
        txtText.setStyle(style);
    }

    private void buttonEvent() {
        String attr = "";
        boolean isFirst = true;
        Iterator<String> it = attribute.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String _value = attribute.get(key);
            if (isFirst) {
                attr += key + ":" + _value;
                isFirst = false;

            } else {
                attr += ", " + key + ":" + _value;
            }

        }
        fireSpinnerEvent(new UIEvent(
                "{eventType:stopCellEditing, "
                + "value:\"" + value + "\", " + attr + "}"));
    }

    public boolean setValue(String value) {
        try {
            this.value = new BigDecimal(value);
            return true;

        } catch (Exception e) {
            //Logger.getGlobal().log(Level.WARNING, value, e);
            return false;
        }

    }

    public void setValue(Number number) {
        value = new BigDecimal(number.doubleValue());
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    public String getModel() {
        String _attr = "";
        Iterator<String> it = attribute.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String v = attribute.get(key);
            _attr += key + "='" + v + "' ";
        }

        if (style == null) {
            style = "";
        }

        String _style = style.equals("") ? "" : "style='" + style + "' ";

        String model = "<div " + _style + " identificator='"
                + getIdentificator() + "' " + _attr + ">"
                + btnDecrement.getModel()
                + txtText.getModel()
                + btnIncrement.getModel()
                + "</div>";

        return model;
    }

    public void setAttribute(String key, String value) {
        attribute.put(key, value);
        txtText.setAttribute(key, value);
    }
}
