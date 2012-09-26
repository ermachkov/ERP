/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.order.unpaid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.ubo.document.Order;
import org.ubo.document.journal.MoneyRecord;
import org.ubo.employee.Employee;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.money.Money;
import org.ubo.partner.Account;
import org.ubo.partner.Address;
import org.ubo.partner.Agent;
import org.ubo.partner.Contacts;
import org.ubo.print.MediaFormat;
import org.ubo.rules.RuleItem;
import org.ubo.tree.TreeBasic;
import org.ubo.utils.Result;
import org.ucm.cashmachine.CashMachineException;
import org.ucm.cashmachine.CashMachineResponse;
import org.ucm.cashmachine.ResponseItem;
import org.ups.print.Printer;
import org.uui.component.*;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.plugin.HasRules;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.core.ui.PopupPanel;


/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class UnpaidRightPanel extends RightPanel implements HasRules {

    private UnpaidOrderPanel orderPanel;
    private Button btnPay, btnEdit, btnSave, btnPayByCash, btnPrint, btnCancel;
    private TabbedPane tabbedPane;
    private CashTextField txtCash;
    private Label lblRest;
    private Panel printPanel;
    private CarssierCore core = CarssierCore.getInstance();
    private Order selectedOrder;

    public UnpaidRightPanel(String sessionId) {
        super(sessionId);
        // ADD ORDER PANEL
        orderPanel = new UnpaidOrderPanel(sessionId);
        orderPanel.setHelpDropMessage("Для оплаты заказа сделайте двойной щелчок на нужной строчке таблицы");

        btnPay = new Button(getSession(), "Оплатить");
        btnPay.setStyle("font-size:80%;");
        btnPay.addUIEventListener(getPayButtonListener());

        btnEdit = new Button(getSession(), "Редактировать");
        btnEdit.setStyle("font-size:80%;");
        btnEdit.addUIEventListener(getEditButtonListener());

        orderPanel.getFooterOrderPanel().clearComponents();
        orderPanel.getFooterOrderPanel().addComponent(btnPay);
        orderPanel.getFooterOrderPanel().addComponent(btnEdit);

        txtCash = new CashTextField(sessionId);
        txtCash.setStyle("text-align:center;");
        txtCash.setText("");
        txtCash.setCssClass("txtIncomeCash");
        txtCash.addUIEventListener(getCashTextFieldListener());

        lblRest = new Label(sessionId, "0.00");

        btnPayByCash = new Button(getSession(), "Оплатить");
        btnPayByCash.setEnabled(false);
        btnPayByCash.addUIEventListener(getPayCashButtonListener());

        tabbedPane = new TabbedPane();
        tabbedPane.addTab("tab-0", "Наличные", getCashPanel(false));
        tabbedPane.addTab("tab-1", "Безнал", getBankAccountPanel(false));
        tabbedPane.addTab("tab-2", "Карточка", "<div style='height:100%;'>Модуль не включён</div>");
        tabbedPane.addTab("tab-3", "Левые", "<div style='height:100%;'>Модуль не включён</div>");

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
        printPanel.addComponent(new GroupPanel(getSession()), "padding-top:10px;");

        btnSave = new Button(getSession(), "Сохранить");
        btnSave.addUIEventListener(getSaveButtonListener());
    }

    class CashTextField extends TextField {

        public CashTextField(String sessionId) {
            super(sessionId);
        }

        @Override
        public void extraHandler(String extra) {
            if (extra.equals("KEY_ENTER")) {
                paidCash();
            }
        }
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

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        RuleItem ruleItem = RuleItem.newRuleItemBoolean("canModifyOrder",
                "Имеет право редактировать заказ?", true);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    private UIEventListener getPrintCancelListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                orderPanel.clearBasket();
                JSMediator.hideRightPanel(getSession());
            }
        };

        return listener;
    }

    private UIEventListener getPrintButtonListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    for (int i = 0; i < printPanel.getWebKitComponentsLength(); i++) {
                        if (printPanel.getWebKitComponent(i) instanceof CheckBox) {
                            CheckBox chkBox = (CheckBox) printPanel.getWebKitComponent(i);
                            if (chkBox.isChecked() && !chkBox.getName().equals("all")) {
                                Result r = CarssierCore.getInstance().getPrinter();
                                if (r.isError()) {
                                    continue;
                                }

                                Printer printer = (Printer) r.getObject();

                                switch (chkBox.getName()) {
                                    case "sheet":
                                        JSMediator.print(getSession(),
                                                CarssierCore.getInstance().getHTMLDocument(
                                                orderPanel.getOrder(), CarssierCore.INVOICE));
                                        break;

                                    case "act":
                                        JSMediator.print(getSession(),
                                                CarssierCore.getInstance().getHTMLDocument(
                                                orderPanel.getOrder(), CarssierCore.ACT));
                                        break;

                                    case "copy":
                                        JSMediator.print(getSession(),
                                                CarssierCore.getInstance().getHTMLDocument(
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
                            UnpaidPanel.class.getName(),
                            "{eventType:showWorkPanel, session:\"" + getSession() + "\"}");

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }

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

    private UIEventListener getPayCashButtonListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                paidCash();
            }
        };

        return listener;
    }

    private void paidCash() {
        JSMediator.showLockPanel(getSession());
        
        try {
            BigDecimal cash = new BigDecimal(txtCash.getText().replaceAll(",", "."));
            if (cash.doubleValue() < orderPanel.getOrder().getTotalWithTotalDiscount().doubleValue()) {
                JSMediator.hideLockPanel(getSession());
                return;
            }

            Result r = CarssierCore.getInstance().paidCash(orderPanel.getOrder(), cash, 48, getSession());
            if (r.isError()) {
                if (r.getObject() instanceof CashMachineResponse) {
                    String errorMessage = "";
                    CashMachineResponse cmr = (CashMachineResponse) r.getObject();
                    for (ResponseItem responseItem : cmr.getResponseItemList()) {
                        if (responseItem.isError()) {
                            errorMessage += "Команда: " + responseItem.getHumanCommand() + "<br/>";
                            errorMessage += "Вызвала ошибку: " + responseItem.getHumanError() + "<br/><br/>";
                        }
                    }
                    PopupPanel popupPanel = new PopupPanel(getSession());
                    popupPanel.setTitle("Ошибка!");
                    popupPanel.setPanel("<div>" + errorMessage + "</div>");
                    popupPanel.showPanel();
                    JSMediator.hideLockPanel(getSession());
                    return;
                }

                if (orderPanel.getOrder().getPaidStatus() == Order.PAID) {
                    orderPanel.getOrder().setPaidStatus(Order.UNPAID);
                    core.modifyOrder(orderPanel.getOrder());

                    PopupPanel popupPanel = new PopupPanel(getSession());
                    popupPanel.setTitle("Предупреждение");
                    popupPanel.setPanel("<div>Прозошла ошибка. "
                            + "Ошибка была устранена. Попробуйте оплатить "
                            + "заказ еще раз."
                            + "<br/>"
                            + "Если и после этого оплатить заказ не удаётся, "
                            + "проверьте работу кассового аппрата или обратитесь "
                            + "к администратору."
                            + "</div>");
                    popupPanel.showPanel();

                } else {
                    JSMediator.alert(getSession(), r.toString());
                }
                JSMediator.hideLockPanel(getSession());
                return;

            } else {
                String sEmployeeId = core.getLoggedUser(getSession()).getExtraInfoByKey("employeeId");
                Result rEmployee = core.getEmployeeById(Long.parseLong(sEmployeeId));
                if (rEmployee.isError()) {
                    JSMediator.alert(getSession(), rEmployee.getReason());
                    JSMediator.hideLockPanel(getSession());
                    return;
                }
                orderPanel.getOrder().setCashmaster((Employee) rEmployee.getObject());
                Result rOrder = core.modifyOrder(orderPanel.getOrder());
                if (!rOrder.isError()) {
                    orderPanel.setOrder((Order) rOrder.getObject());
                }

                JSMediator.setRightPanel(getSession(), printPanel.getModel());

                r = core.getMoneyInCashBox(48);
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());

                } else {
                    JSMediator.setCashBox(getSession(), "" + r.getObject());
                }
            }

            txtCash.setText("");
            lblRest.setText("");
            
            JSMediator.hideLockPanel(getSession());

        } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
            JSMediator.alert(getSession(), e.toString());
            JSMediator.hideLockPanel(getSession());
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private UIEventListener getCashTextFieldListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    switch (evt.getJSONObject().getString("eventType")) {
                        case "click":
                            txtCash.setText("");
                            tabbedPane.updateTabPanel(0, getCashPanel(false));
                            btnPayByCash.setEnabled(false);
                            JSMediator.setRightPanel(getSession(), tabbedPane.getModel());
                            break;

                        case "keyup":
                            String val = txtCash.getText().replaceAll(",", ".");
                            if (val.equals("")) {
                                btnPayByCash.setEnabled(false);
                                return;
                            }

                            try {
                                Double.parseDouble(val);
                            } catch (Exception e) {
                                btnPayByCash.setEnabled(false);
                                txtCash.setText("0.00");
                                JSMediator.setRightPanel(getSession(), tabbedPane.getModel());
                                return;
                            }

                            BigDecimal rest = Money.SUBSTRACT(val,
                                    "" + orderPanel.getOrder().getTotalWithTotalDiscount());
                            if (rest.doubleValue() < 0) {
                                rest = BigDecimal.ZERO;
                                btnPayByCash.setEnabled(false);

                            } else {
                                btnPayByCash.setEnabled(true);
                            }

                            lblRest.setText(rest.toString());
                            txtCash.setText(val);
                            tabbedPane.updateTabPanel(0, getCashPanel(false));
                            JSMediator.setRightPanel(getSession(), tabbedPane.getModel());
                            break;
                    }

                } catch (JSONException e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    private UIEventListener getSaveButtonListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSMediator.showLockPanel(getSession());

                    boolean isAddNewCustomer = false;
                    Agent selectedAgent = null;
                    if (orderPanel.getHeaderOrderPanel().getSelectedCustomerId() != -1) {
                        Result r = core.getAgent(orderPanel.getHeaderOrderPanel().getSelectedCustomerId());
                        if (r.isError()) {
                            JSMediator.alert(getSession(), r.getReason());
                            return;
                        }
                        Agent a = (Agent) r.getObject();
                        if (!a.getShortName().equals(orderPanel.getHeaderOrderPanel().getCustomerName())) {
                            isAddNewCustomer = true;
                        } else {
                            selectedAgent = a;
                        }
                    } else {
                        selectedAgent = (Agent) core.getAgent(12).getObject();
                    }

                    if (!isAddNewCustomer) {
                        orderPanel.getOrder().setCustomer(selectedAgent);

                    } else {
                        Agent a = new Agent();
                        a.setShortName(orderPanel.getHeaderOrderPanel().getCustomerName());
                        a.setFullName(orderPanel.getHeaderOrderPanel().getCustomerName());

                        Address address = new Address();
                        a.setAddress("default", address);

                        Contacts contact = new Contacts();
                        a.setContacts("default", contact);

                        Account account = new Account();
                        a.setAccount("default", account);

                        Result r = core.addAgent(a);
                        if (!r.isError()) {
                            orderPanel.getOrder().setCustomer((Agent) r.getObject());

                        } else {
                            JSMediator.alert(getSession(), r.getReason());
                            return;
                        }
                    }

                    CarssierCore.getInstance().modifyOrder(orderPanel.getOrder());
                    orderPanel.getFooterOrderPanel().clearComponents();
                    orderPanel.getFooterOrderPanel().addComponent(btnPay);
                    orderPanel.getFooterOrderPanel().addComponent(btnEdit);
                    orderPanel.setEditable(false);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("eventType", "switchToView");
                    jsonObject.put("session", getSession());
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            UnpaidPanel.class.getName(), jsonObject.toString());
                    JSMediator.setRightPanel(getSession(), getModel());
                    
                    JSMediator.hideLockPanel(getSession());

                } catch (JSONException e) {
                    JSMediator.hideLockPanel(getSession());
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    private UIEventListener getEditButtonListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    orderPanel.getFooterOrderPanel().clearComponents();
                    orderPanel.getFooterOrderPanel().addComponent(btnSave);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("eventType", "switchToEdit");
                    jsonObject.put("session", getSession());
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            UnpaidPanel.class.getName(), jsonObject.toString());

                    orderPanel.setEditable(true);
                    JSMediator.setRightPanel(getSession(), getModel());

                    String s = orderPanel.getCustomrsList();
                    if (!s.equals("")) {
                        JSMediator.setCustomersSelector(getSession(), s);
                    }

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }

            }
        };

        return listener;
    }

    private UIEventListener getPayButtonListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    if (orderPanel.getOrder().getPaidType() == Order.BANK_ACCOUNT) {
                        tabbedPane.updateTabPanel(0, "");
                        tabbedPane.updateTabPanel(1, getBankAccountPanel(true));
                        JSMediator.setRightPanel(getSession(), tabbedPane.getModel());
                        JSMediator.setSelectTab(getSession(), 1);

                    } else {
                        tabbedPane.updateTabPanel(0, getCashPanel(true));
                        tabbedPane.updateTabPanel(1, getBankAccountPanel(false));
                        JSMediator.setRightPanel(getSession(), tabbedPane.getModel());
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    private String getCashPanel(boolean isInit) {
        String payment = "0.00";
        if (orderPanel.getOrder() != null) {
            payment = "" + orderPanel.getOrder().getTotalWithTotalDiscount();
            if (payment.endsWith(".00")) {
                payment = payment.substring(0, payment.length() - 3);
            }
        }

        if (isInit) {
            txtCash.setText(payment);
            //txtCash.setText("");
            btnPayByCash.setEnabled(true);
        }

        String cashPanel = "<div align='center' style='height:100%;'>"
                + "<div style='width:100%; height:50px;'>К оплате: "
                + payment + " р.</div>"
                + "<div style='width:100%; height:50px;'>Получено наличными</div>"
                + "<div style='width:100%; height:50px;'>" + txtCash.getModel() + "</div>"
                + "<div style='width:100%; height:50px;'>Сдача</div>"
                + "<div style='width:100%; height:50px;'>" + lblRest.getModel() + "</div>"
                + "<div style='width:100%; height:50px;'>" + btnPayByCash.getModel() + "</div>"
                + "</div>";
        return cashPanel;
    }

    private String getBankAccountPanel(boolean isConfirmPay) {
        String panel;

        if (isConfirmPay) {
            final CheckBox chkInvoice = new CheckBox(getSession(), "Счет на оплату");
            chkInvoice.setStyleLabel("font-size: 80%");
            
            final CheckBox chkAct = new CheckBox(getSession(), "Акт выполненных работ");
            chkAct.setStyleLabel("font-size: 80%");
            
            final CheckBox chkConfirm = new CheckBox(getSession(), "Оплата подтверждена");
            chkConfirm.setStyleLabel("font-size: 80%");

            Button btnPrintDocs = new Button(getSession(), "Печать");
            btnPrintDocs.setStyle("font-size: 80%");
            btnPrintDocs.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (chkInvoice.isChecked()) {
                            JSMediator.print(getSession(),
                                    CarssierCore.getInstance().getHTMLDocument(
                                    orderPanel.getOrder(), CarssierCore.INVOICE));
                        }

                        if (chkAct.isChecked()) {
                            JSMediator.print(getSession(),
                                    CarssierCore.getInstance().getHTMLDocument(
                                    orderPanel.getOrder(), CarssierCore.ACT));
                        }
                    } catch (Exception e) {
                        JSMediator.alert(getSession(), e.toString());
                        Logger.getGlobal().log(Level.WARNING, null);
                    }
                }
            });


            Button btnConfirmPay = new Button(getSession(), "Применить");
            btnConfirmPay.setStyle("font-size: 80%");
            btnConfirmPay.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (chkConfirm.isChecked()) {
                            core.paidBankAccount(getSession(), orderPanel.getOrder());
                            JSMediator.hideRightPanel(getSession());

//                            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
//                                    UnpaidPanel.class.getName(),
//                                    "{eventType:push, session:" + getSession() + ", action:showWorkPanel}");
                        }
                    } catch (NumberFormatException e) {
                        JSMediator.alert(getSession(), e.toString());
                        Logger.getGlobal().log(Level.WARNING, null, e);
                    }

                }
            });
            panel = ""
                    + "<div style='width:100%;'>"
                    + chkInvoice.getModel()
                    + "<br/>"
                    + chkAct.getModel()
                    + "<br/>"
                    + btnPrintDocs.getModel()
                    + "<hr/>"
                    + chkConfirm.getModel()
                    + "<br/>"
                    + btnConfirmPay.getModel()
                    + "</div>";

        } else {
            final CheckBox chkInvoice = new CheckBox(getSession(), "Счет на оплату");
            chkInvoice.setStyleLabel("font-size: 80%");
            
            final CheckBox chkAct = new CheckBox(getSession(), "Акт выполненных работ");
            chkAct.setStyleLabel("font-size: 80%");
            
            Button btnApply = new Button(getSession(), "Оформить");
            btnApply.setStyle("font-size: 80%");
            btnApply.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (chkInvoice.isChecked() || chkAct.isChecked()) {
                            JSMediator.showLockPanel(getSession());
                            String sEmployeeId = core.getLoggedUser(getSession()).getExtraInfoByKey("employeeId");
                            Result rEmployee = core.getEmployeeById(Long.parseLong(sEmployeeId));
                            if (rEmployee.isError()) {
                                JSMediator.alert(getSession(), rEmployee.getReason());
                                JSMediator.hideLockPanel(getSession());
                                return;
                            }
                            orderPanel.getOrder().setCashmaster((Employee) rEmployee.getObject());
                            Result rOrder = core.modifyOrder(orderPanel.getOrder());
                            if (!rOrder.isError()) {
                                orderPanel.setOrder((Order) rOrder.getObject());
                            }
                            JSMediator.hideLockPanel(getSession());
                        }

                        if (chkInvoice.isChecked()) {
                            JSMediator.print(getSession(),
                                    CarssierCore.getInstance().getHTMLDocument(
                                    orderPanel.getOrder(), CarssierCore.INVOICE));
                        }

                        if (chkAct.isChecked()) {
                            JSMediator.print(getSession(),
                                    CarssierCore.getInstance().getHTMLDocument(
                                    orderPanel.getOrder(), CarssierCore.ACT));
                        }

                        if (chkInvoice.isChecked() || chkAct.isChecked()) {
                            JSMediator.showLockPanel(getSession());
                            orderPanel.getOrder().setPaidType(Order.BANK_ACCOUNT);
                            orderPanel.getOrder().setPaidStatus(Order.WAIT_PAY_BANK);
                            core.modifyOrder(orderPanel.getOrder());

                            JSMediator.hideRightPanel(getSession());
                            JSMediator.hideLockPanel(getSession());

//                            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
//                                    UnpaidPanel.class.getName(),
//                                    "{eventType:push, session:" + getSession() + ", action:showWorkPanel}");
                        }

                    } catch (NumberFormatException e) {
                        Logger.getGlobal().log(Level.WARNING, null, e);
                        JSMediator.alert(getSession(), e.toString());
                    }

                }
            });

            panel = ""
                    + "<div style='width:100%;'>"
                    + chkInvoice.getModel()
                    + "<br/>"
                    + chkAct.getModel()
                    + "<br/>"
                    + btnApply.getModel()
                    + "</div>";
        }


        return panel;
    }

    public void setOrder(Order order) {
        orderPanel.setOrder(order);
        orderPanel.setEditable(false);
    }

    private UnpaidRightPanel getMe() {
        return this;
    }

    @Override
    public String getName() {
        return "Оплатить";
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
                        UnpaidPanel.class.getName(),
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
                        Order order = (Order) r.getObject();
                        selectedOrder = order;
                        if (order.getPaidStatus() == Order.RETURN_ALL_PAID) {
                            JSMediator.setRightPanel(getSession(), getReturnMoneyPanel(order));

                        } else {
                            orderPanel.setOrder(order);
                            JSMediator.setRightPanel(getSession(), getModel());
                        }
                    }
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("pay")) {
                    Result r = core.getOrder(jsonObject.getLong("orderId"));
                    if (r.isError()) {
                        return;
                    }

                    selectedOrder = (Order) r.getObject();
                    orderPanel.setOrder(selectedOrder);
                    tabbedPane.updateTabPanel(0, getCashPanel(true));

                    JSMediator.setRightPanel(getSession(), tabbedPane.getModel());
                }
            }

        } catch (JSONException ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    @Override
    public String getModel() {
        Result result = core.getRulesItemByKey(getSession(), "canModifyOrder");
        if (result.isError()) {
            JSMediator.alert(getSession(), result.getReason());

        } else {
            if (selectedOrder != null) {
                if (selectedOrder.getWorkStatus() != Order.WORK_COMPLETE) {
                    btnEdit.setEnabled((Boolean) ((RuleItem) result.getObject()).getValue());
                } else {
                    btnEdit.setEnabled(false);
                }
            }
        }

        return orderPanel.getModel();
    }
}
