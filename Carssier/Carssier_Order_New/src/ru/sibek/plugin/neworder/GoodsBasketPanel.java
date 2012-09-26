package ru.sibek.plugin.neworder;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.employee.Employee;
import org.ubo.goods.Goods;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.json.Validator;
import org.ubo.money.Money;
import org.ubo.partner.Account;
import org.ubo.partner.Address;
import org.ubo.partner.Agent;
import org.ubo.partner.Contacts;
import org.ubo.print.MediaFormat;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.service.Service;
import org.ubo.tree.TreeFolder;
import org.ubo.tree.TreeLeaf;
import org.ubo.utils.NumberToWords;
import org.ubo.utils.Result;
import org.ubo.utils.StringToNumber;
import org.ups.print.Printer;
import org.uui.component.*;
import org.uui.component.List;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.plugin.HasRules;
import org.uui.table.MacHeaderColumn;
import org.uui.table.MacTableHeaderModel;
import org.uui.table.MacTableModel;
import org.uui.webkit.WebKitComponent;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.core.ui.PopupPanel;
import ru.sibek.database.CarssierDataBase;

public class GoodsBasketPanel extends RightPanel implements HasRules {

    private DataBase dataBase;
    private GoodsOrderPanel goodsOrderPanel;
    private CarssierCore core = CarssierCore.getInstance();
    private HeaderOrderPanel headerOrderPanel;
    private FooterOrderPanel footerOrderPanel;
    private Button orderButton;
    private Button btnSmartEditorBack;
    private ComboBox cboDiscounts;
    private CheckBox chkPrintPos;
    private SmartChooser smartChooser;
    private SmartSearchPanel smartSearchPanel;
    private Button smartAddButton;
    private SmartEditorPanel smartEditorPanel;
    private MacTableModel smartEditorTable;
    private String helpDropMessage = "Для формирования нового заказа складывйте сюда товары и услуги";
    private RadioButton rbDeffered;
    private RadioButton rbWork;
    private AtomicBoolean isDefaultSupplierDefined = new AtomicBoolean(false);
    public static final int PANEL_CLOSE = 0, PANEL_OPEN = 1;
    private int panelStatus = 0;

    public GoodsBasketPanel(String sessionId) {
        super(sessionId);
        dataBase = CarssierDataBase.getDataBase();
        goodsOrderPanel = new GoodsOrderPanel(sessionId, dataBase, ResourceBundle.getBundle("GoodsPanel").getString("basket_greeting"));

        headerOrderPanel = new HeaderOrderPanel(getSession());
        smartChooser = headerOrderPanel.getSmartPanel();
        smartSearchPanel = smartChooser.getSmartSearchPanel();
        smartSearchPanel.addUIEventListener(getSmartSearchPanelListener());

        smartAddButton = smartSearchPanel.getAddButton();
        smartAddButton.addUIEventListener(getSmartAddButtonListener());

        headerOrderPanel.addUIEventListener(getHeaderUIEventListener());

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("0%", "0");
        map.put("5%", "5");
        map.put("10%", "10");
        map.put("15%", "15");
        map.put("20%", "20");
        map.put("25%", "25");
        cboDiscounts = new ComboBox(getSession(), map);
        cboDiscounts.setLabel("Сделать скидку");
        cboDiscounts.setLabelStyle("font-size:80%;");
        cboDiscounts.setCssClass("cboTotalDiscount");
        cboDiscounts.addUIEventListener(getComboDiscountUIEventListener());

        orderButton = new Button(getSession(), "Оформить");
        orderButton.addUIEventListener(getActionButtonUIEventListener());

        Button clearButton = new Button(getSession(), "Очистить");
        clearButton.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                goodsOrderPanel.clearBasket();
                headerOrderPanel.setSelectedCustomerId(12);
                headerOrderPanel.setCustomerInfo(((Agent) core.getAgent(12).getObject()).getShortName());
                JSMediator.hideRightPanel(getSession());
            }
        });

        footerOrderPanel = new FooterOrderPanel(sessionId, cboDiscounts);
        footerOrderPanel.addComponent(orderButton);
        footerOrderPanel.addComponent(clearButton);

        chkPrintPos = new CheckBox(getSession(), "Печатать POS чек", "printPOS", "printPOS");
        chkPrintPos.setStyleLabel("font-size:80%;");
        footerOrderPanel.addComponent(chkPrintPos);

        smartEditorPanel = new SmartEditorPanel(sessionId, null);
        smartEditorTable = new MacTableModel(getSession());
        smartEditorTable.setCssClass("display");
        smartEditorTable.setId("smartEditorTable");
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Name", String.class, true));
        mth.addHeaderColumn(new MacHeaderColumn("Value", String.class, true));
        smartEditorTable.setHeader(mth);

        btnSmartEditorBack = smartEditorPanel.getButtonBack();
        btnSmartEditorBack.addUIEventListener(getSmartEditorButtonBackListener());

        goodsOrderPanel.macTableModel.getMacTableRemoveButton().addUIEventListener(getMactTableRemoveButtonListener());
        goodsOrderPanel.macTableModel.addUIEventListener(getMacTableEventListener());

        init();
    }

    public UIEventListener getLiveSearchPanelEventListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    if (goodsOrderPanel.getOrder() == null) {
                        goodsOrderPanel.newOrder().setDescription(evt.getJSONObject().getString("value"));
                    } else {
                        goodsOrderPanel.getOrder().setDescription(evt.getJSONObject().getString("value"));
                    }
                } catch (Exception e) {
                }
            }
        };
        return listener;
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();
        RuleItem ruleItem = RuleItem.newRuleItemBoolean("canSetDiscountAtOrderItem", "Имеет право предоставлять скидку на любой товар / услугу?", true);

        listRulesItem.add(ruleItem);

        ruleItem = RuleItem.newRuleItemBoolean("canSetIndividualPriceForService", "Имеет право устанавливать цену услуги со свободной ценой?", true);

        listRulesItem.add(ruleItem);

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Работу");
        selectorRuleItem.setKey("toWork");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Отложенные");
        selectorRuleItem.setKey("toDiffered");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Предоставлять выбор");
        selectorRuleItem.setKey("toShowDestionation");
        selectorRuleItemList.add(selectorRuleItem);
        ruleItem = RuleItem.newRuleItemRadio("moveAfterCompliteTo", "После оформления отправлять заказа в:", selectorRuleItemList, 0);

        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("addZeroQuantityGoodsToOrderDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("addZeroQuantityGoodsToOrderAllow");
        selectorRuleItemList.add(selectorRuleItem);

        ruleItem = RuleItem.newRuleItemRadio("canAddZeroQuantityGoodsToOrder", "Добавление в заказ товара которого нет на складе:", selectorRuleItemList, 0);

        listRulesItem.add(ruleItem);

        ruleItem = RuleItem.newRuleItemBoolean("afterComplitePrintPos", "После оформления печатать POS чек", true);

        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    private void init() {
        setSmartPanelData("");
        setTableEditMode();

        Result r = core.getDefaultSupplier();
        if (!r.isError()) {
            isDefaultSupplierDefined.set(true);
            Agent a = (Agent) r.getObject();
            setSupplier(a);
        }

        r = core.getAgent("Частное лицо");
        setCustomer((Agent) r.getObject());
        headerOrderPanel.setSelectedCustomerId(12);
    }

    private void createDefaultSupplier() {
        TextField txtSupplier = new TextField(getSession());
        txtSupplier.setStyle("width:96%;");

        TextField txtAddress = new TextField(getSession());
        txtAddress.setStyle("width:96%;");

        TextField txtPhone = new TextField(getSession());
        txtPhone.setStyle("width:96%;");

        TextField txtOwner = new TextField(getSession());
        txtOwner.setStyle("width:96%;");

        PopupPanel popupPanel = new PopupPanel(getSession());
        popupPanel.setTitle("Создание поставщика");
        String panel = "Для формирования заказа необходимо указать<br/>поставщика товаров/услуг по умолчанию.<br/>Здесь имеется ввиду ваша компания.<br/>В полях расположенном ниже введите <br/>краткое название свой компании,<br/>адрес и телефон.<br/><span style='font-size:70%'>В дальнейшем эту информацию можно будет отредактировать в разделе <strong>Справочники</strong></span><br/><br/>Краткое название<br/>" + txtSupplier.getModel() + "<br/>" + "Адрес<br/>" + txtAddress.getModel() + "<br/>" + "Телефон<br/>" + txtPhone.getModel() + "<br/>" + "Владелец<br/>" + txtOwner.getModel();

        popupPanel.setPanel(panel);

        boolean isAdded = false;
        while (!isAdded) {
            popupPanel.showPanel();
            if (!txtSupplier.getText().equals("")) {
                isAdded = true;
                continue;
            }
            //panel = "Для формирования заказа необходимо указать<br/>поставщика товаров/услуг по умолчанию.<br/>Здесь имеется ввиду ваша компания.<br/>В полях расположенном ниже введите <br/>краткое название свой компании,<br/>адрес и телефон.<br/><span style='font-size:70%'>В дальнейшем эту информацию можно будет отредактировать в разделе <strong>Справочники</strong></span><br/><br/>Краткое название <span style='color:red;'>Не может быть пустым!</span><br/>" + txtSupplier.getModel() + "<br/>" + "Адрес<br/>" + txtAddress.getModel() + "<br/>" + "Телефон<br/>" + txtPhone.getModel() + "<br/>" + "Владелец<br/>" + txtOwner.getModel();
        }

        Result r = core.addAgent(txtSupplier.getText(), txtSupplier.getText(), txtAddress.getText(), txtPhone.getText());

        if (!r.isError()) {
            Agent a = (Agent) r.getObject();
            core.setDefaultSupplier(a);
            setSupplier(a);
            isDefaultSupplierDefined.set(true);

            Employee owner = new Employee();
            owner.setRole("owner");
            if (txtOwner.getText().equals("")) {
                owner.setShortName("Владелец");
                owner.setFullName("Владелец");
            } else {
                owner.setShortName(txtOwner.getText());
                owner.setFullName(txtOwner.getText());
            }

            core.addEmployee(owner);

        } else {
            JSMediator.alert(getSession(), r.getReason());
        }
    }

    public FooterOrderPanel getFooterOrderPanel() {
        return footerOrderPanel;
    }

    private UIEventListener getMacTableEventListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSONObject jsonObject = evt.getJSONObject();
                    if (!Validator.isValid(jsonObject, new String[]{"row", "column", "value"})) {
                        return;
                    }

                    if (jsonObject.getString("eventType").equals("stopCellEditing")) {
                        int row = jsonObject.getInt("row");
                        int column = jsonObject.getInt("column");
                        String value = jsonObject.getString("value");
                        CopyOnWriteArraySet<OrderRow> set = goodsOrderPanel.getOrder().getOrderRows();
                        int r = 0;
                        for (OrderRow orderRow : set) {
                            if (r != row) {
                                r++;
                                continue;
                            }

                            if (column == 2) {
                                orderRow.setPrice(StringToNumber.formatToMoney(value));
                            }

                            if (column == 3) {
                                orderRow.setCount(StringToNumber.formatToQuantity(value));
                            }

                            if (column == 4) {
                                orderRow.setDiscount(StringToNumber.formatToMoney(value));
                            }
                            r++;
                        }

                        refreshBottomPanel();

                        JSMediator.refreshElement(
                                getSession(), 
                                "orderPanelTableContainer", 
                                goodsOrderPanel.macTableModel.getModel());
                        
                        JSMediator.refreshElement(
                                getSession(), 
                                "orderPanelFooterPanel", 
                                footerOrderPanel.getModel());

//                        String s = getDescriptions();
//                        if (!s.equals("")) {
//                            JSMediator.setOrderDescription(getSession(), s);
//                        }
//
//                        s = getCustomrsList();
//                        if (!s.equals("")) {
//                            JSMediator.setCustomersSelector(getSession(), s);
//                        }
                    }

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        };
        return listener;
    }

    private UIEventListener getMactTableRemoveButtonListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                if (goodsOrderPanel.macTableModel.getNumberCheckedRows().isEmpty()) {
                    return;
                }

                ArrayList<Integer> checkedList = goodsOrderPanel.macTableModel.getNumberCheckedRows();
                CopyOnWriteArraySet<OrderRow> set = goodsOrderPanel.getOrder().getOrderRows();
                ArrayList<OrderRow> tmpList = new ArrayList<>();
                tmpList.addAll(set);
                ArrayList<OrderRow> removeList = new ArrayList<>();
                for (int i = 0; i < tmpList.size(); i++) {
                    if (checkedList.contains(i)) {
                        removeList.add(tmpList.get(i));
                    }
                }

                boolean success = set.removeAll(removeList);
                if (success) {
                    goodsOrderPanel.macTableModel.removeCheckedRows();
                    goodsOrderPanel.refresh();
                    refreshBottomPanel();
                    try {
                        JSMediator.setRightPanel(getSession(), getModel());

                    } catch (Exception e) {
                        JSMediator.alert(getSession(), e.toString());
                    }
                }
            }
        };
        return listener;
    }

    private UIEventListener getSmartAddButtonListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    Agent agent = new Agent();
                    agent.setShortName(evt.getJSONObject().getString("data"));
                    agent.setFullName(evt.getJSONObject().getString("data"));
                    dataBase.addObject(agent);

                    setSmartPanelData(smartChooser.getFilter());

                    JSMediator.refreshSmartChooserList(getSession(), smartChooser.getList().getModel());

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        };
        return listener;
    }

    private UIEventListener getSmartEditorButtonBackListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSONArray jsonArray = evt.getJSONObject().getJSONArray("data");

                    int rows = 0;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        int row = jsonArray.getJSONObject(i).getInt("row");
                        if (row > rows) {
                            rows = row;
                        }
                    }

                    for (int r = 0; r <= rows; r++) {
                        String key = "";
                        String value = "";
                        for (int i = 0; i < jsonArray.length(); i++) {
                            int row = jsonArray.getJSONObject(i).getInt("row");
                            int column = jsonArray.getJSONObject(i).getInt("column");
                            if ((column == 0) && (row == r)) {
                                key = jsonArray.getJSONObject(i).getString("value");
                            }

                            if ((column == 1) && (row == r)) {
                                value = jsonArray.getJSONObject(i).getString("value");
                            }
                        }
                    }

                    setSmartPanelData(smartChooser.getFilter());
                    JSMediator.refreshSmartChooserList(getSession(), smartChooser.getList().getModel());

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        };
        return listener;
    }

    private UIEventListener getSmartSearchPanelListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    setSmartPanelData(evt.getJSONObject().getString("text"));
                    JSMediator.refreshSmartChooserList(getSession(), smartChooser.getList().getModel());

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        };
        return listener;
    }

    private UIEventListener getComboDiscountUIEventListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    goodsOrderPanel.getOrder().setTotalPercentDiscount(new BigDecimal(cboDiscounts.getSelectedValue()));

                    refreshBottomPanel();
                    
//                    JSMediator.refreshElement(
//                                getSession(), 
//                                "orderPanelFooterPanel", 
//                                footerOrderPanel.getModel());

                    JSMediator.setRightPanel(getSession(), getModel());

                    String s = getDescriptions();
                    if (!s.equals("")) {
                        JSMediator.setOrderDescription(getSession(), s);
                    }

                    s = getCustomrsList();
                    if (!s.equals("")) {
                        JSMediator.setCustomersSelector(getSession(), s);
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        };
        return listener;
    }

    private UIEventListener getActionButtonUIEventListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                WebKitEventBridge.getInstance().unregisterWebKitComponent(getSession(), rbDeffered);
                WebKitEventBridge.getInstance().unregisterWebKitComponent(getSession(), rbWork);

                try {
                    JSMediator.showLockPanel(getSession());

                    goodsOrderPanel.getOrder().setDescription(headerOrderPanel.getDescription());

                    String sEmployeeId = core.getLoggedUser(getSession()).getExtraInfoByKey("employeeId");
                    Result rEmployee = core.getEmployeeById(Long.parseLong(sEmployeeId));
                    if (rEmployee.isError()) {
                        JSMediator.alert(getSession(), rEmployee.getReason());
                        return;
                    }
                    goodsOrderPanel.getOrder().setMaster((Employee) rEmployee.getObject());

                    boolean isAddNewCustomer = false;
                    Agent selectedAgent = null;
                    if (headerOrderPanel.getSelectedCustomerId() != -1) {
                        Result r = core.getAgent(headerOrderPanel.getSelectedCustomerId());
                        if (r.isError()) {
                            JSMediator.alert(getSession(), r.getReason());
                            return;
                        }
                        Agent a = (Agent) r.getObject();
                        if (!a.getShortName().equals(headerOrderPanel.getCustomerName())) {
                            isAddNewCustomer = true;
                        } else {
                            selectedAgent = a;
                        }
                    } else {
                        selectedAgent = (Agent) core.getAgent(12).getObject();
                    }

                    if (!isAddNewCustomer) {
                        goodsOrderPanel.getOrder().setCustomer(selectedAgent);

                    } else {
                        Agent a = new Agent();
                        a.setShortName(headerOrderPanel.getCustomerName());
                        a.setFullName(headerOrderPanel.getCustomerName());

                        Address address = new Address();
                        a.setAddress("default", address);

                        Contacts contact = new Contacts();
                        a.setContacts("default", contact);

                        Account account = new Account();
                        a.setAccount("default", account);

                        Result r = core.addAgent(a);
                        if (!r.isError()) {
                            goodsOrderPanel.getOrder().setCustomer((Agent) r.getObject());
                        } else {
                            JSMediator.alert(getSession(), r.getReason());
                            return;
                        }
                    }

                    Result result = core.getRulesItemByKey(getSession(), "moveAfterCompliteTo");
                    if (result.isError()) {
                        JSMediator.alert(getSession(), result.getReason());

                    } else {
                        String dst = "";
                        RuleItem ruleItem = (RuleItem) result.getObject();
                        for (SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()) {
                            if (item.isSelected()) {
                                switch (item.getKey()) {
                                    case "toWork":
                                        dst = "OrdersInWork";
                                        break;
                                    case "toDiffered":
                                        dst = "OrdersDeffered";
                                        break;
                                    case "toShowDestionation":
                                        PopupPanel popupPanel = new PopupPanel(getSession());
                                        popupPanel.setTitle("Куда положить заказ?");

                                        rbDeffered = new RadioButton(getSession(), "where", "В отложенные", false);
                                        rbDeffered.addUIEventListener(new UIEventListener() {
                                            @Override
                                            public void event(UIEvent evt) {
                                                if (rbDeffered.isChecked()) {
                                                    rbWork.setChecked(false);
                                                } else {
                                                    rbWork.setChecked(true);
                                                }
                                            }
                                        });

                                        rbWork = new RadioButton(getSession(), "where", "В работу", true);
                                        rbWork.addUIEventListener(new UIEventListener() {
                                            @Override
                                            public void event(UIEvent evt) {
                                                if (rbWork.isChecked()) {
                                                    rbDeffered.setChecked(false);
                                                } else {
                                                    rbDeffered.setChecked(true);
                                                }
                                            }
                                        });
                                        String panel = rbWork.getModel() + "<br/>" + rbDeffered.getModel();
                                        popupPanel.setPanel(panel);
                                        popupPanel.showPanel("getUICore().showPopupPanel");

                                        if (rbWork.isChecked()) {
                                            dst = "OrdersInWork";
                                        } else {
                                            dst = "OrdersDeffered";
                                        }

                                }

                                Result r = core.addOrder(getSession(), goodsOrderPanel.getOrder(), dst);
                                if (r.isError()) {
                                    JSMediator.alert(getSession(), r.getReason());

                                } else {
                                    if ((chkPrintPos != null)
                                            && (chkPrintPos.isChecked())) {
                                        Executors.newSingleThreadExecutor().
                                                execute(printPOS(goodsOrderPanel.getOrder()));
                                    }

                                    goodsOrderPanel.clearBasket();
                                    //headerOrderPanel.setCustomerInfo("");
                                    headerOrderPanel.setDescription("");
                                    headerOrderPanel.setSelectedCustomerId(12);
                                    headerOrderPanel.setCustomerInfo(((Agent) core.getAgent(12).getObject()).getShortName());
                                    refreshBottomPanel();
                                    goodsOrderPanel.refresh();
                                    JSMediator.setRightPanel(getSession(), getModel());
                                    String message;
                                    if (dst.equals("OrdersInWork")) {
                                        message = "<p><img src='img/icons/worker.png' align='left' hspace='5'>Заказ отправлен в <b>Работу</b> и помечен как неоплаченный.</p>";
                                    } else {
                                        message = "<p><img src='img/icons/worker.png' align='left' hspace='5'>Заказ отправлен в <b>Отложенные</b> и помечен как неоплаченный.</p>";
                                    }

                                    PopupPanel popupPanel = new PopupPanel(getSession());
                                    popupPanel.setTitle("Информация");
                                    popupPanel.setPanel(message);
                                    popupPanel.setAutoHideTimeout(1500);
                                    popupPanel.showPanel();

                                    JSMediator.hideRightPanel(getSession());
                                    panelStatus = PANEL_CLOSE;
                                    cboDiscounts.setSelectedIndex(0);
                                }
                            }
                        }
                    }

                } catch (NumberFormatException e) {
                    JSMediator.hideLockPanel(getSession());
                    String dst;
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }

                JSMediator.hideLockPanel(getSession());
            }
        };
        return listener;
    }

    private Runnable printPOS(final Order order) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Printer printer = Printer.getInstance(Paths.get(System.getProperty("user.home"), new String[]{".saas", "app", "config", "system.xml"}));
                    core.printDocument(printer.getPosPrinter(), order, "bill_pos.html", 1, MediaFormat.POS_72x160());

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        };
        return r;
    }

    private UIEventListener getHeaderUIEventListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
            }
        };
        return listener;
    }

    private void setSmartPanelData(String filter) {
        smartChooser.setFilter(filter);

        List list = new List(getSession());
        Map m = dataBase.getAllObjects(Agent.class.getName());
        Iterator it = m.keySet().iterator();
        while (it.hasNext()) {
            long id = ((Long) it.next()).longValue();
            Agent a = (Agent) m.get(Long.valueOf(id));

            if ((filter.trim().length() > 0)
                    && (a.getShortName().toLowerCase().indexOf(filter.toLowerCase(), 0) == -1)) {
                continue;
            }

            Label lblAgent = new Label(getSession(), a.getName());
            lblAgent.setStyle("font-size:80%; text-decoration:underline; cursor:pointer; color:darkblue");
            lblAgent.setAttribute("className", Agent.class.getName());
            lblAgent.setAttribute("dbid", Long.valueOf(id));
            lblAgent.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        String className = evt.getJSONObject().getString("className");
                        long id = evt.getJSONObject().getLong("dbid");
                        Agent agent = (Agent) dataBase.getObject(className, id);
                        if (headerOrderPanel.getSmartPanelInvoiker() == HeaderOrderPanel.SUPPLIER) {
                            setSupplier(agent);
                        }

                        if (headerOrderPanel.getSmartPanelInvoiker() == HeaderOrderPanel.CUSTOMER) {
                            setCustomer(agent);
                        }

                        JSMediator.setRightPanel(getSession(), getModel());

                    } catch (JSONException e) {
                        JSMediator.alert(getSession(), e.toString());
                    }
                }
            });

            Label lblAgentEdit = new Label(getSession(), "<img src='img/subbuttons/edit.png'>");
            lblAgentEdit.setStyle("font-size:80%; text-decoration:underline; cursor:pointer; color:darkblue");
            lblAgentEdit.setAttribute("className", Agent.class.getName());
            lblAgentEdit.setAttribute("dbid", Long.valueOf(id));
            lblAgentEdit.setCssClass("smartChooserEditButton");
            lblAgentEdit.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        ArrayList list = new ArrayList();
                        Agent agent = (Agent) dataBase.getObject(evt.getJSONObject().getString("className"), evt.getJSONObject().getLong("dbid"));

                        smartEditorPanel.setEditableObject(agent);
                        smartEditorTable.setData(list);
                        smartEditorPanel.setTable(smartEditorTable);

                        JSMediator.showSmartChooserEditor(getSession(), smartEditorPanel.getModel());

                    } catch (JSONException e) {
                        Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
                        JSMediator.alert(getSession(), e.toString());
                    }
                }
            });

            ListItem listItem = new ListItem(getSession(), new WebKitComponent[]{lblAgent, lblAgentEdit}, new String[]{"float:left; width:400px;", "float:left; width:30px;"});

            list.addItem(listItem);
        }

        smartChooser.setList(list);
    }

    private void setSupplier(Agent agent) {
        goodsOrderPanel.setSupplier(agent);
        headerOrderPanel.setSupplierInfo(agent.getShortName());
    }

    private void setCustomer(Agent agent) {
        goodsOrderPanel.setCustomer(agent);
        headerOrderPanel.setCustomerInfo(agent.getShortName());
    }

    private void setDescription(String description) {
        headerOrderPanel.setDescription(description);
    }

    public void setOrder(Order order) {
        goodsOrderPanel.setOrder(order);
        setSupplier(order.getSupplier());
        setCustomer(order.getCustomer());
        setDescription(order.getDescription());
        refreshBottomPanel();
    }

    public void addToOrder(ArrayList<TreeLeaf> dropList) {
        if (!isDefaultSupplierDefined.get()) {
            Result r = core.getDefaultSupplier();
            if (r.isError()) {
                createDefaultSupplier();
            } else {
                setSupplier((Agent) r.getObject());
                isDefaultSupplierDefined.set(true);
            }
        }

        ArrayList<TreeLeaf> removeList = new ArrayList<>();
        ArrayList<String> errorList = new ArrayList<>();
        for (TreeLeaf tl : dropList) {
            BigDecimal price;
            if (tl.getContainer().getClassName().indexOf("Service") != -1) {
                Result r = core.getService(tl.getContainer().getId());
                if (!r.isError()) {
                    Service s = (Service) r.getObject();
                    price = s.getSalePrice(dataBase);
                    if ((price.doubleValue() == 0.0D) && (!s.isIndividualPrice())) {
                        errorList.add(s.getShortName());
                    }
                }
            } else if (tl.getContainer().getClassName().indexOf("Goods") != -1) {
                Result r = core.getGoods(tl.getContainer().getId());
                if (!r.isError()) {
                    Goods g = (Goods) r.getObject();
                    price = g.getSalePrice(dataBase);
                    if (price.doubleValue() == 0.0D) {
                        errorList.add(g.getShortName());
                    }

                    if (core.isRadioButtonRuleAllow(getSession(), "canAddZeroQuantityGoodsToOrder", "addZeroQuantityGoodsToOrderDeny")) {
                        if (core.getGoodsCountOnAllStorages(g).doubleValue() <= 0.0D) {
                            errorList.add(g.getShortName());
                            removeList.add(tl);
                        }
                    }
                }
            }
        }

        if (!removeList.isEmpty()) {
            PopupPanel popupPanel = new PopupPanel(getSession());
            popupPanel.setTitle("Предупреждение");
            String panel = "Следующие товары / услуги имеют нулевую цену (или отсутствуют на складе), поэтому не могут быть добавлены в заказ:<ul>";

            for (String str : errorList) {
                panel = panel + "<li>" + str + "</li>";
            }
            panel = panel + "</ul><hr/>Вы можете установить цену на товар / услуги в разделе <strong>«Справочники»</strong>, там же можно изменить складские остатки";

            popupPanel.setPanel(panel);
            popupPanel.showPanel();
            dropList.removeAll(removeList);
        }

        if (dropList.isEmpty()) {
            return;
        }

        goodsOrderPanel.addTreeLeaves(dropList);
        goodsOrderPanel.macTableModel.getModel();
        try {
            refreshBottomPanel();

            JSMediator.setRightPanel(getSession(), getModel(), 0.58);

            String s = getDescriptions();
            if (!s.equals("")) {
                JSMediator.setOrderDescription(getSession(), s);
            }

            s = getCustomrsList();
            if (!s.equals("")) {
                JSMediator.setCustomersSelector(getSession(), s);
            }

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    public void setEditable(boolean editable) {
        goodsOrderPanel.setEditable(editable);
        if (editable) {
            setTableEditMode();
        } else {
            setTableViewMode();
        }

        headerOrderPanel.setEditable(editable);
        footerOrderPanel.setEditable(editable);
    }

    public void setTableViewMode() {
        goodsOrderPanel.setTableViewMode();
    }

    public void setTableEditMode() {
        goodsOrderPanel.setTableEditMode();
    }

    public Order getOrder() {
        return goodsOrderPanel.getOrder();
    }

    private GoodsBasketPanel getMe() {
        return this;
    }

    @Override
    public String getName() {
        return "Корзина";
    }

    public void setHelpDropMessage(String message) {
        helpDropMessage = message;
    }

    @Override
    public String getModel() {
        Result result = core.getRulesItemByKey(getSession(), "afterComplitePrintPos");
        if (!result.isError()) {
            chkPrintPos.setChecked(((Boolean) ((RuleItem) result.getObject()).getValue()).booleanValue());
        }

        String model;
        if (goodsOrderPanel.getOrder() == null) {
            model = "<div style='width:100%; height:100%; overflow:hidden;' class='rightPanel' identificator='"
                    + getIdentificator()
                    + "'>"
                    + "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<div style='width:50%;height:50%;border-radius:18px;border-color:gray;"
                    + "border-style:dotted;border-width:3px;' align='center'>"
                    + "<table width='100%' height='100%'><tr>"
                    + "<td align='center' valign='middle' "
                    + "style='background-image: url(img/dragdrop/target.png);"
                    + "background-position: center;background-repeat: no-repeat;'>"
                    + helpDropMessage + "</td></tr></table>"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>";
        } else {

            for (WebKitComponent c : footerOrderPanel.getComponents()) {
                if (c instanceof Button) {
                    if (core.getLoggedUser(getSession()).getLogin().equals("admin")) {
                        ((Button) c).setEnabled(false);
                    }
                }
            }

            model = "<div class='rightPanel' identificator='"
                    + getIdentificator()
                    + "'>"
                    + "<div style='width:100%;heigth:150px;'>"
                    + headerOrderPanel.getModel()
                    + "</div>"
                    + goodsOrderPanel.getHTMLModel()
                    + "<div style='width:100%;heigth:140px;' id='orderPanelFooterPanel'>"
                    + footerOrderPanel.getModel()
                    + "</div>"
                    + "</div>";
        }

        return model;
    }

    @Override
    public String getIdentificator() {
        return getClass().getName();
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

            if ((jsonObject.getString("eventType").equals("click"))
                    && (jsonObject.getString("action").equals("hideRightPanel"))) {
                panelStatus = PANEL_CLOSE;
            }

            if ((jsonObject.getString("eventType").equals("push"))
                    && (jsonObject.getString("action").equals("showRightPanel"))) {
                dropHandler(jsonObject.getJSONArray("data"));
                panelStatus = PANEL_OPEN;

            }

            if ((jsonObject.getString("eventType").equals("click"))
                    && (jsonObject.getString("action").equals("showRightPanel"))) {
                JSMediator.setRightPanel(getSession(), getModel(), 0.58);

                String s = getDescriptions();
                if (!s.equals("")) {
                    JSMediator.setOrderDescription(getSession(), s);
                }

                s = getCustomrsList();
                if (!s.equals("")) {
                    JSMediator.setCustomersSelector(getSession(), s);
                }

                panelStatus = PANEL_OPEN;
            }

            if (jsonObject.getString("eventType").equals("drop")) {
                dropHandler(jsonObject.getJSONArray("data"));
            }
        } catch (JSONException ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    private void dropHandler(JSONArray jsonArray) {
        try {
            ArrayList<TreeLeaf> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject.getString("className").indexOf("TreeLeaf") != -1) {
                    TreeLeaf treeLeaf = (TreeLeaf) dataBase.getObject(
                            jsonObject.getString("className"),
                            jsonObject.getLong("dbid"));

                    list.add(treeLeaf);
                }

                if (jsonObject.getString("className").indexOf("TreeFolder") != -1) {
                    TreeFolder treeFolder = (TreeFolder) dataBase.getObject(
                            jsonObject.getString("className"),
                            jsonObject.getLong("dbid"));

                    list.addAll(treeFolder.getAllDescendTreeLeaves());
                }
            }

            addToOrder(list);

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, Objects.toString(jsonArray), e);
        }
    }

    private void refreshBottomPanel() {
        if (goodsOrderPanel.getOrder() != null) {
            String txt = "Общая скидка " + goodsOrderPanel.getOrder().getTotalPercentDiscount() + "%. " + "(" + goodsOrderPanel.getOrder().getTotalDiscountSum() + ") руб.";

            footerOrderPanel.getLabelTotalDiscountSum().setText(txt);
            BigDecimal total = goodsOrderPanel.getOrder().getTotalWithTotalDiscount();
            BigDecimal r = Money.SUBSTRACT(total.toString(), "" + total.intValue());
            int rest = Money.MULTIPLY(r.toString(), "100").intValue();
            txt = "Итого к оплате: " + total + ", (" + NumberToWords.convert(total.intValue()) + ") " + "руб. " + rest + " коп.";

            footerOrderPanel.getLabelTotalString().setText(txt);
            goodsOrderPanel.refresh();
            
        } else {
            footerOrderPanel.getLabelTotalDiscountSum().setText("");
            footerOrderPanel.getLabelTotalString().setText("");
        }
    }

    public String getDescriptions() {
        JSONArray jsa = new JSONArray();
        Set<String> descriptionSet = new HashSet<>();
        ArrayList<Order> list = dataBase.getAllObjectsList(Order.class.getName());
        for (Order order : list) {
            if (order.getDescription().trim().equals("")) {
                continue;
            }
            descriptionSet.add(order.getDescription().trim());
            jsa.put(order.getDescription().trim());
        }

        String s = "";
        try {
            JSONObject json = new JSONObject();
            json.put("data", descriptionSet);
            s = json.toString();

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
        }

        return s;
    }

    public String getCustomrsList() {
        JSONArray jsonArray = new JSONArray();
        Result r = core.getAgents();
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
            return "";
        }

        for (Agent a : (ArrayList<Agent>) r.getObject()) {
            Map<String, String> m = new HashMap<>();
            m.put("id", "" + a.getId());
            m.put("name", a.getShortName().replaceAll("\"", "\\\\\""));
            jsonArray.put(m);
        }

        if (jsonArray.length() == 0) {
            return "";
        }

        String s = "";
        try {
            JSONObject json = new JSONObject();
            json.put("data", jsonArray);
            s = json.toString();

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
        }

        return s;
    }

    public void clearBasket() {
        goodsOrderPanel.clearBasket();
    }

    public HeaderOrderPanel getHeaderOrderPanel() {
        return headerOrderPanel;
    }

    public GoodsOrderPanel getGoodsOrderPanel() {
        return goodsOrderPanel;
    }
}
