/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.LinkedHashSet;
import java.util.Set;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.webkit.WebKitComponent;
import org.uui.webkit.WebKitEventBridge;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class FooterOrderPanel extends Component {

    private EventListenerList listenerList = new EventListenerList();
    private String model = "", identificator = "";
    private Label lblTotalString, lblTotalDiscountSum;
    private ComboBox cboTotalDiscount;
    private boolean isEditable = true;
    private Set<WebKitComponent> components = new LinkedHashSet<>();

    public FooterOrderPanel(String sessionId, ComboBox cboTotalDiscount) {
        super(sessionId);
        this.cboTotalDiscount = cboTotalDiscount;

        lblTotalString = new Label(sessionId, "");
        lblTotalString.setStyle("font-size:80%; font-style: italic;");

        lblTotalDiscountSum = new Label(sessionId, "");
        lblTotalDiscountSum.setStyle("font-size:80%; font-style: italic;");
    }

    public boolean addComponent(WebKitComponent component) {
        return components.add(component);
    }

    public void clearComponents() {
        components.clear();
    }

    public boolean removeComponent(WebKitComponent component) {
        return components.remove(component);
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
        model = "";
    }

    public boolean isEditable() {
        return isEditable;
    }

    public Label getLabelTotalDiscountSum() {
        return lblTotalDiscountSum;
    }

    public Label getLabelTotalString() {
        return lblTotalString;
    }

    //public Button getActionButton() {
    //    return actionButton;
    //}
    //public void setActionButton(Button actionButton) {
    //    this.actionButton = actionButton;
    //}
    public ComboBox getCboTotalDiscount() {
        return cboTotalDiscount;
    }

    public void setCboTotalDiscount(ComboBox cboTotalDiscount) {
        this.cboTotalDiscount = cboTotalDiscount;
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
        cboTotalDiscount.setEnabled(isEditable);

        String strComponents = "";
        for (WebKitComponent c : components) {
            strComponents += c.getModel();
        }

        model = "<table width='100%'>"
                + "<tr><td>" + cboTotalDiscount.getModel() + "<br/>"
                + lblTotalDiscountSum.getModel() + "</td></tr>"
                + "<tr><td>" + lblTotalString.getModel() + "</td></tr>"
                + "<tr><td>"
                + strComponents
                + "</td></tr>"
                + "</table>";
        return model;
    }

    @Override
    public String getIdentificator() {
        if (identificator == null) {
            identificator = getClass().getName();

        } else if (identificator.equals("")) {
            identificator = getClass().getName();
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

    public Set<WebKitComponent> getComponents() {
        return components;
    }
}
