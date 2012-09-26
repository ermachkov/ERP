/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.order.paid;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.document.Order;
import org.ubo.document.journal.MoneyRecord;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.print.MediaFormat;
import org.ubo.tree.TreeBasic;
import org.ubo.utils.Result;
import org.ups.print.Printer;
import org.uui.component.*;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class PaidRightPanel extends RightPanel {

    private PaidOrderPanel orderPanel;
    private Button btnPrint, btnCancel, btnShowPrintPanel;
    private Panel printPanel;
    private CarssierCore core = CarssierCore.getInstance();

    public PaidRightPanel(String sessionId) {
        // ADD ORDER PANEL
        super(sessionId);
        orderPanel = new PaidOrderPanel(sessionId);
        orderPanel.setHelpDropMessage("Для оплаты заказа сделайте двойной щелчок на нужной строчке таблицы");

        orderPanel.getFooterOrderPanel().clearComponents();
        btnShowPrintPanel = new Button(getSession(), "Печать документов");
        orderPanel.getFooterOrderPanel().addComponent(btnShowPrintPanel);

        printPanel = new Panel();
        printPanel.setStyle("padding:15px;font-size:85%;");
        printPanel.addComponent(new Label(sessionId, "Печать документов"), "font-size:90%;font-weight:bold;");

        final CheckBox checkAll = new CheckBox(getSession(), "Выделить все", "all", "all");
        checkAll.addUIEventListener(getCheckPrintAllListener());
        checkAll.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                setEnablePrintButton();
            }
        });
        printPanel.addComponent(checkAll, "margin-top:5px;font-size:85%;");

        CheckBox checkSheet = new CheckBox(getSession(), "Счет-фактура", "sheet", "sheet");
        checkSheet.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                setEnablePrintButton();
            }
        });
        printPanel.addComponent(checkSheet, "margin-top:5px;font-size:85%;");

        CheckBox checkAct = new CheckBox(getSession(), "Акт выполненных работ", "act", "act");
        checkAct.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                setEnablePrintButton();
            }
        });
        printPanel.addComponent(checkAct, "margin-top:5px;font-size:85%;");

        CheckBox checkCopy = new CheckBox(getSession(), "Копия чека", "copy", "copy");
        checkCopy.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                setEnablePrintButton();
            }
        });
        printPanel.addComponent(checkCopy, "margin-top:5px;font-size:85%;");

        CheckBox checkPOS = new CheckBox(getSession(), "POS чек", "pos", "pos");
        checkPOS.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                setEnablePrintButton();
            }
        });
        printPanel.addComponent(checkPOS, "margin-top:5px;font-size:85%;");

        btnPrint = new Button(getSession(), "Печать");
        btnPrint.setEnabled(false);
        btnPrint.addUIEventListener(getPrintButtonListener());

        btnCancel = new Button(getSession(), "Пропустить");
        btnCancel.addUIEventListener(getPrintCancelListener());
        printPanel.addComponent(new GroupPanel(sessionId), "padding-top:10px;");

        btnShowPrintPanel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                JSMediator.setRightPanel(getSession(), printPanel.getModel());

            }
        });
    }

    class GroupPanel extends Component {
        
        public GroupPanel(String sessionId){
            super(sessionId);
        }

        @Override
        public String getModel() {
            String model = ""
                    + "<div>"
                    + btnPrint.getModel()
                    + btnCancel.getModel()
                    + "</div>";

            return model;
        }
    }

    private void setEnablePrintButton() {
        boolean isEnable = false;
        for (int i = 0; i < printPanel.getWebKitComponentsLength(); i++) {
            if (printPanel.getWebKitComponent(i) instanceof CheckBox) {
                if (((CheckBox) printPanel.getWebKitComponent(i)).isChecked()) {
                    isEnable = true;
                }
            }
        }

        btnPrint.setEnabled(isEnable);
        JSMediator.setRightPanel(getSession(), printPanel.getModel());
    }

    private UIEventListener getPrintCancelListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    orderPanel.clearBasket();
                    JSMediator.hideRightPanel(getSession());

                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            PaidPanel.class.getName(),
                            "{eventType:showWorkPanel, session:" + getSession() + "}");

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }

            }
        };

        return listener;
    }

    private UIEventListener getPrintButtonListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                Result r = CarssierCore.getInstance().getPrinter();
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                }
                Printer printer = (Printer) r.getObject();

                for (int i = 0; i < printPanel.getWebKitComponentsLength(); i++) {
                    if (printPanel.getWebKitComponent(i) instanceof CheckBox) {
                        CheckBox chkBox = (CheckBox) printPanel.getWebKitComponent(i);
                        if (chkBox.isChecked() && !chkBox.getName().equals("all")) {
                            switch (chkBox.getName()) {
                                case "sheet":
                                    JSMediator.print(getSession(), CarssierCore.getInstance().getHTMLDocument(
                                            orderPanel.getOrder(), CarssierCore.INVOICE));
                                    break;

                                case "act":
                                    JSMediator.print(getSession(), CarssierCore.getInstance().getHTMLDocument(
                                            orderPanel.getOrder(), CarssierCore.ACT));
                                    break;

                                case "copy":
                                    JSMediator.print(getSession(), CarssierCore.getInstance().getHTMLDocument(
                                            orderPanel.getOrder(), CarssierCore.BILL));
                                    break;

                                case "pos":
                                    CarssierCore.getInstance().printDocument(
                                            printer.getPosPrinter(),
                                            orderPanel.getOrder(),
                                            CarssierCore.BILL_POS,
                                            1,
                                            MediaFormat.POS_72x160());
                                    break;
                            }
                        }
                    }
                }

                orderPanel.clearBasket();
                JSMediator.hideRightPanel(getSession());

                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        PaidPanel.class.getName(),
                        "{eventType:showWorkPanel, session:" + getSession() + "}");

            }
        };

        return listener;
    }

    private UIEventListener getCheckPrintAllListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    boolean isCheck = evt.getJSONObject().getBoolean("checked");
                    for (int i = 0; i < printPanel.getWebKitComponentsLength(); i++) {
                        if (printPanel.getWebKitComponent(i) instanceof CheckBox) {
                            CheckBox chkBox = (CheckBox) printPanel.getWebKitComponent(i);
                            chkBox.setChecked(isCheck);
                            printPanel.updateComponent(i, chkBox);
                        }
                    }

                    JSMediator.setRightPanel(getSession(), printPanel.getModel());

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    public void setOrder(Order order) {
        orderPanel.setOrder(order);
        orderPanel.setEditable(false);
    }

    private Order extractOrder(ArrayList dropList) {
        Order unpaidOrder = null;
        return unpaidOrder;
    }

    private PaidRightPanel getMe() {
        return this;
    }

    @Override
    public String getName() {
        return "Заказ";
    }

    @Override
    public String getIdentificator() {
        return getClass().getName();
    }

    private String getReturnMoneyPanel(final Order order) {
        Button btnReturnMoney = new Button(getSession(), "Вернуть");
        btnReturnMoney.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                // cashmachine
                Result r = CarssierCore.getInstance().returnPaid(order);
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                    return;
                }

                // add money record
                r = core.addMoneyRecord(
                        MoneyRecord.PAY_BY_CASH,
                        order,
                        "Возврат наличных клиенту",
                        order.getSupplier(),
                        order.getCustomer(),
                        order.getTotalWithTotalDiscount());

                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                    return;
                }

                r = core.getTreeOrdersUnpayed();
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                    return;
                }

                r = core.removeOrderFromTrees(order, (TreeBasic) r.getObject());
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                    return;
                }

                JSMediator.hideRightPanel(getSession());
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        PaidPanel.class.getName(),
                        "{eventType:push, session:" + getSession() + ", action:showWorkPanel}");
            }
        });

        String panel = "<table width='100%' height='100%' cellpadding='0' cellspacing='0'>"
                + "<tr>"
                + "<td align='center' valign='middle'>"
                + "<div style='width:50%;height:50%;border-radius:18px;border-color:gray;"
                + "border-style:dotted;border-width:3px;' align='center'>"
                + "<table width='100%' height='100%'>"
                + "<tr>"
                + "<td align='center' valign='middle'>"
                + "Вернуть клиенту - " + order.getTotalWithTotalDiscount() + " руб.<br/><br/>"
                + btnReturnMoney.getModel()
                + "</div>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</td>"
                + "</tr>"
                + "</table>";
        return panel;
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

            setSession(jsonObject.getString("session"));
            orderPanel.setSession(jsonObject.getString("session"));

            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("hideRightPanel")) {
//                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
//                            PaidPanel.class.getName(),
//                            "{eventType:push, session:"+getSession()+", action:showWorkPanel}");
                }
            }

            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    JSMediator.setRightPanel(getSession(), getModel());
                    String s = orderPanel.getDescriptions();
                    if (!s.equals("")) {
                        JSMediator.setOrderDescription(getSession(), s);
                    }
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showOrder")) {
                    Result r = core.getOrder(jsonObject.getLong("dbid"));
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());

                    } else {
                        orderPanel.setOrder((Order) r.getObject());
                        JSMediator.setRightPanel(getSession(), getModel());
//                        if (order.getPaidStatus() == Order.RETURN_ALL_PAID) {
//                            JSMediator.setRightPanel(getSession(), getReturnMoneyPanel(order));
//                        } else {
//                            orderPanel.setOrder(order);
//                            JSMediator.setRightPanel(getSession(), getModel());
//                        }
                    }
                }
            }

        } catch (JSONException ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    @Override
    public String getModel() {
        return orderPanel.getModel();
    }
}
