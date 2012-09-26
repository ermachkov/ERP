/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.component;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class HeaderOrderPanel extends Component {

    private String model = "", identificator = "";
    private Label lblSupplierSelector, lblCustomerSelector, lblSupplierInfo;//, lblCustomerInfo;
    private CustomerSelector txtCustomerSelector;
    private SearchPanel liveSearchPanel;
    private SmartChooser smartPanel;
    private EventListenerList listenerList = new EventListenerList();
    public static int SUPPLIER = 0, CUSTOMER = 1;
    private int smartPanelInvoiker = -1;
    public boolean isEditable = true;

    public HeaderOrderPanel(String sessionId) {
        super(sessionId);
        lblSupplierSelector = new Label(sessionId, "Поставщик");
        lblSupplierSelector.setStyle("font-size:80%;");

        lblCustomerSelector = new Label(sessionId, "Получатель");
        lblCustomerSelector.setStyle("text-decoration: underline; cursor: pointer; "
                + "font-size:80%; color:darkblue; cursor:pointer;");
        lblCustomerSelector.addUIEventListener(getCustomerListener());

        lblSupplierInfo = new Label(sessionId, "");
        lblSupplierInfo.setIdentificator("lblSupplierInfo");
        lblSupplierInfo.setStyle("font-size:80%");

        txtCustomerSelector = new CustomerSelector(sessionId);
        txtCustomerSelector.setStyle("width:98%;");
        txtCustomerSelector.setCssClass("searchCustomersField");

        liveSearchPanel = new SearchPanel(sessionId);
        liveSearchPanel.setStyle("width:98%;");
        liveSearchPanel.setCssClass("searchOrderDescription");
        liveSearchPanel.addUIEventListener(getLiveSearchListener());

        smartPanel = new SmartChooser(sessionId, null);
    }
    
    final class CustomerSelector extends TextField{
        
        private long selectedId = -1;
        
        public CustomerSelector(String sessionId){
            super(sessionId);
        }
        
        @Override
        public void extraHandler(String extra){
            try {
                selectedId = Long.parseLong(extra);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, extra, e);
            }
        }
    }
    
    public long getSelectedCustomerId(){
        return txtCustomerSelector.selectedId;
    }
    
    public void setSelectedCustomerId(long id){
        txtCustomerSelector.selectedId = id;
    }
    
    public String getCustomerName(){
        return txtCustomerSelector.getText();
    }

    public SearchPanel getLiveSearchPanel() {
        return liveSearchPanel;
    }

    public void setDescription(String text) {
        liveSearchPanel.setText(text);
    }

    public String getDescription() {
        return liveSearchPanel.getText();
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
        model = "";
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setCustomerInfo(String text) {
        txtCustomerSelector.setText(text);
        model = "";
    }

    public void setSupplierInfo(String text) {
        lblSupplierInfo.setText(text);
        model = "";
    }

    public SmartChooser getSmartPanel() {
        return smartPanel;
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

    private UIEventListener getLiveSearchListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        return listener;
    }

//    private UIEventListener getSupplierListener() {
//        UIEventListener listener = new UIEventListener() {
//
//            @Override
//            public void event(UIEvent evt) {
//                try {
//                    if (evt.getJSONObject().getString("eventType").equals("click")) {
//                        smartPanelInvoiker = HeaderOrderPanel.SUPPLIER;
//                        WebKitFrame.getInstance().browserExecutor(
//                                "getUICore().showSmartChooser('"
//                                + WebKitUtil.prepareToJS(smartPanel.getModel()) + "')");
//                    }
//                } catch (JSONException e) {
//                    Logger.getGlobal().log(Level.WARNING, null, e);
//                }
//
//            }
//        };
//
//        return listener;
//    }

    private UIEventListener getCustomerListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                smartPanelInvoiker = HeaderOrderPanel.CUSTOMER;
                try {
                    if (evt.getJSONObject().getString("eventType").equals("click")) {
                        smartPanelInvoiker = HeaderOrderPanel.CUSTOMER;
                        smartPanel.setModel("");
//                        WebKitFrame.getInstance().browserExecutor(
//                                "getUICore().showSmartChooser('"
//                                + WebKitUtil.prepareToJS(smartPanel.getModel()) + "')");
                    }
                } catch (JSONException e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    public int getSmartPanelInvoiker() {
        return smartPanelInvoiker;
    }

    @Override
    public void setModel(String html) {
        model = html;
    }

    public void refreshPanel() {
        model = "";
    }

    @Override
    public String getModel() {
        Label lblDesc = new Label(getSession(), "Описание");
        lblDesc.setStyle("font-size:85%;");
        liveSearchPanel.setEnable(isEditable);

        if (isEditable) {
            model = "<table width='99%'>"
                    + "<tr>"
                    + "<td width='20%'>" + lblSupplierSelector.getModel() + "</td>"
                    + "<td width='80%' style='border: 1px gray solid;'>"
                    + lblSupplierInfo.getModel()
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td width='20%' style='font-size:80%;'>Получатель</td>"
                    + "<td width='80%'>"
                    //+ lblCustomerInfo.getModel()
                    + txtCustomerSelector.getModel()
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td width='20%'>" + lblDesc.getModel() + "</td>"
                    + "<td width='80%'>" + liveSearchPanel.getModel() + "</td>"
                    + "</tr>"
                    + "</table>";

        } else {
            model = "<table width='99%'>"
                    + "<tr>"
                    + "<td width='20%' style='font-size:80%;'>" + lblSupplierSelector.getText() + "</td>"
                    + "<td width='80%' style='border: 1px gray solid;'>"
                    + lblSupplierInfo.getModel()
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td width='20%' style='font-size:80%;'>Получатель</td>"
                    + "<td width='80%'>"
                    //+ lblCustomerInfo.getModel()
                    + txtCustomerSelector.getModel()
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td width='20%'>" + lblDesc.getModel() + "</td>"
                    + "<td width='80%'>" + liveSearchPanel.getModel() + "</td>"
                    + "</tr>"
                    + "</table>";
        }

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
}
