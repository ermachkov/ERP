/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.order.unpaid;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.proxy.KnowsId;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.tree.Tree;
import org.ubo.tree.TreeFolder;
import org.ubo.tree.TreeLeaf;
import org.ubo.tree.TreeLeafBasic;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.db.DataBase;
import org.uui.db.event.DataBaseEvent;
import org.uui.db.event.DataBaseEventAdapter;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerFolder;
import org.uui.explorer.ExplorerLeaf;
import org.uui.explorer.ExplorerPanel;
import org.uui.explorer.TreeExplorerPanel;
import org.uui.plugin.HasRules;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.ribbon.RibbonButtonEventListener;
import org.uui.ribbon.RibbonEvent;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.Callback;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class UnpaidPanel extends TreeExplorerPanel implements HasWorkPanelToolbars, HasRightPanels, HasRules {

    private ArrayList<RibbonButton> toolbarButtons;
    private DataBase dataBase;
    private CarssierCore core = CarssierCore.getInstance();
    private RemoveOrderPanel removeOrderPanel;
    private MacTableModel macTableModel;
    private long selectedOrderId = -1;
    private FilterPanel filterPanel;
    private Order eventOrder;

    public UnpaidPanel(String sessionId, DataBase dataBase) {
        super(sessionId, dataBase, (Tree) CarssierCore.getInstance().getTreeOrdersUnpayed().getObject());
        this.dataBase = dataBase;

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Дата оформления", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Описание", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Заказчик", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Статус", String.class, false));

        macTableModel = new MacTableModel(getSession(), true, new MacTableSummator(4));
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("orderUnpaidTable");
        macTableModel.setHeader(mth);

        macTableModel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    Order o = (Order) macTableModel.getRow(macTableModel.getSelectedRow()).getValue();
                    selectedOrderId = o.getId();
                    if (evt.getJSONObject().getString("eventType").equals("click")) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("eventType", "push");
                        jsonObject.put("action", "showOrder");
                        jsonObject.put("className", Order.class.getName());
                        jsonObject.put("dbid", o.getId());
                        jsonObject.put("session", getSession());
                        WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                                UnpaidRightPanel.class.getName(), jsonObject.toString());
                    }

                    if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                        JSMediator.setContextMenu(getSession(), getLeafMenu());
                    }

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });

        switchView(ExplorerPanel.PANEL_TABLE);
        initToolbar();

        dataBase.addDataBaseEventListener(new DataBaseEventAdapter() {
            @Override
            public void allEvent(DataBaseEvent evt) {
                if (evt.getClassName().equals(Order.class.getName())) {
                    Logger.getGlobal().log(Level.INFO, "allEvent {0}", Objects.toString(evt));
                    String panelName = MainFrame.getInstance().getSelectedOperationButton(UnpaidPanel.this.getSession());
                    if (!panelName.equals("Неоплачено")) {
                        return;
                    }

                    macTableModel.getMacTableNavigator().resetPage();
                    //JSMediator.showLockPanel(UnpaidPanel.this.getSession());
                    JSMediator.setWorkPanel(UnpaidPanel.this.getSession(), refreshAndGetHTMLModel());
                    //JSMediator.hideLockPanel(UnpaidPanel.this.getSession());
                    if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                        setMacTableFilterVisible(true);
                    }
                }
            }
        });

        macTableModel.addNavigatorChangeListener(new NavigatorChangeListener() {
            @Override
            public void event(int event) {
                try {
                    if (event == MacTableNavigator.FILTER) {
                        setMacTableFilterVisible(true);

                    } else if (event == MacTableNavigator.REFRESH) {
                        macTableModel.getMacTableNavigator().resetPage();

                        JSMediator.showLockPanel(getSession());
                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                        JSMediator.hideLockPanel(getSession());

                        if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                            setMacTableFilterVisible(true);
                        }

                    } else if (event == MacTableNavigator.CALENDAR) {
                        //
                    } else {
                        JSMediator.showLockPanel(getSession());
                        JSMediator.setWorkPanel(getSession(), macTableModel.getModel());
                        JSMediator.hideLockPanel(getSession());

                        if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                            setMacTableFilterVisible(true);
                        }
                    }

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });

        macTableModel.setNavigatorShowingAlways(true);
        macTableModel.setNavigatorDateSelectorEnabled(true);
        filterPanel = new FilterPanel(getSession()) {
            @Override
            public void dateSearchEnable(boolean isEnabled) {
                try {
                    macTableModel.setNavigatorDateSelectorEnabled(isEnabled);

                    JSMediator.showLockPanel(getSession());
                    JSMediator.setWorkPanel(getSession(), macTableModel.getModel());
                    JSMediator.hideLockPanel(getSession());

                    if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                        setMacTableFilterVisible(true);
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        };
        macTableModel.getMacTableNavigator().setFilterPanel(filterPanel);
    }

    private void refreshEvent() {
        for (MacTableRow row : macTableModel.getRows()) {
            Order order = (Order) row.getValue();
            if (order == null) {
                continue;
            }

            if (order.getId() == eventOrder.getId()) {
                return;
            }
        }

        try {
            macTableModel.getMacTableNavigator().resetPage();

            JSMediator.showLockPanel(getSession());
            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
            JSMediator.hideLockPanel(getSession());

            if (macTableModel.getMacTableNavigator().isFilterVisible()) {
                setMacTableFilterVisible(true);
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }

    private void setMacTableFilterVisible(boolean isVisible) {
        try {
            if (isVisible) {
                LockSupport.parkNanos(1000000000); // !!!!!!!!!!!! HACK
                String filterModel = macTableModel.getMacTableNavigator().getFilterPanel().getModel();
                String identificator = macTableModel.getMacTableNavigator().getIdentificator();
                JSMediator.showFilterPanel(getSession(), filterModel, identificator);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("deleteOrdersDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Только Отложенные");
        selectorRuleItem.setKey("onlyDefferedOrders");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Любые");
        selectorRuleItem.setKey("anyOrders");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 2 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canDeleteUnpaidOrder",
                "Удаление заказов:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    @Override
    public String treeTableWalker(final TreeFolder treeFolder) {
        WebKitEventBridge.getInstance().cleanMacTable(getSession(), macTableModel);

        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Order> orders = core.getOrdersUnpaid();

        for (Order order : orders) {
            if (filterPanel.isDateSearchEnable()) {
                if (order.getDate().getTime() > macTableModel.getMacTableNavigator().getDateEnd().getTime()) {
                    continue;
                }

                if (order.getDate().getTime() < macTableModel.getMacTableNavigator().getDateStart().getTime()) {
                    continue;
                }
            }

            if (filterPanel.getSum().doubleValue() > 0) {
                if (order.getTotalWithTotalDiscount().doubleValue() != filterPanel.getSum().doubleValue()) {
                    continue;
                }
            }

            if (!filterPanel.getCustomerName().equals("")) {
                if (order.getCustomer().getShortName().toLowerCase().indexOf(filterPanel.getCustomerName().toLowerCase()) == -1) {
                    continue;
                }
            }

            if (!filterPanel.getDescription().equals("")) {
                if (order.getDescription().toLowerCase().indexOf(filterPanel.getDescription().toLowerCase()) == -1) {
                    continue;
                }
            }

            if (filterPanel.getPaidStatus() > 0) {
                if (order.getPaidStatus() != filterPanel.getPaidStatus()) {
                    continue;
                }
            }

            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), DateTime.getFormatedDate("dd.MM.yyyy HH:mm", order.getDate()), false));
            row.addCell(new MacTableCell(getSession(), "<img src='img/icons/order_payment24.png' "
                    + "style='vertical-align: middle;'/>&nbsp;"
                    + "№ " + order.getId() + ", " + order.getDescription(),
                    false));

            String parnterName = order.getCustomer() == null ? "" : order.getCustomer().getShortName();
            row.addCell(new MacTableCell(getSession(), parnterName, false));
            row.addCell(new MacTableCell(getSession(), order.getTotalWithTotalDiscount(), false));

            String status = "";
            if (order.getWorkStatus() == Order.WORK_INWORK) {
                status = "<img src='img/subbuttons/move_to_work.png'>";
            }

            if (order.getWorkStatus() == Order.WORK_DEFERRED) {
                status = "<img src='img/subbuttons/move_to_deferred.png'>";
            }

            if (order.getWorkStatus() == Order.WORK_COMPLETE) {
                status = "<img src='img/subbuttons/move_to_complite.png'>";
            }

            if (order.getWorkStatus() == Order.WORK_CHUNKED_RETURN) {
                status = "<img src='img/subbuttons/delete.png'><img src='img/subbuttons/money_out.png'>";
            }

            if (order.getPaidStatus() == Order.RETURN_ALL_PAID) {
                status = "<img src='img/subbuttons/money_out.png'>";
            }

            if (order.getPaidStatus() == Order.WAIT_PAY_BANK) {
                status += "<img src='img/subbuttons/unpaid.png' alt='Ожидает оплаты по б/н'>";
            }

            row.addCell(new MacTableCell(getSession(), "<div align='center'>" + status + "</div>", false));
            row.setValue(order);
            rows.add(row);
        }

        macTableModel.setData(rows);
        htmlModel = macTableModel.getModel();
        return htmlModel;
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();
        RibbonButton rbPay = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/coins.png", "Оплатить", "pay");
        rbPay.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                if (macTableModel.getSelectedRow() == -1) {
                    PopupPanel popupPanel = new PopupPanel(getSession());
                    popupPanel.setTitle("Подсказка");
                    popupPanel.setPanel("Для оплаты заказа сначала выделите его в таблице");
                    popupPanel.showPanel();

                } else {
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                            UnpaidRightPanel.class.getName(),
                            "{eventType:push, session:" + getSession() + ", action:showOrder, "
                            + "dbid:" + macTableModel.getSelectedRow() + "}");
                }
            }
        });
        toolbarButtons.add(rbPay);

        RibbonButton rbDelete = RibbonButton.createDefaultRibbonButton(getSession(),
                "img/subbuttons/delete.png", "Удалить", "delete");
        rbDelete.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                removeOrder();
            }
        });

        toolbarButtons.add(rbDelete);
    }

    private void removeOrder() {
        Result result = core.getRulesItemByKey(getSession(), "canDeleteUnpaidOrder");
        if (result.isError()) {
            JSMediator.alert(getSession(), result.getReason());

        } else {
            RuleItem ruleItem = (RuleItem) result.getObject();
            for (SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()) {
                if (item.isSelected()) {
                    if (item.getKey().equals("onlyDefferedOrders")) {
                        Result r = core.getOrderWorkStatus(selectedOrderId);
                        if (r.isError()) {
                            JSMediator.alert(getSession(), r.getReason());

                        } else {
                            if ((int) r.getObject() == Order.WORK_DEFERRED) {
                                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                                    @Override
                                    public void pressed(int button) {
                                        if (button == ConfirmPanel.YES) {
                                            Result r = core.removeOrderDeffered(selectedOrderId);
                                            if (r.isError()) {
                                                JSMediator.alert(getSession(), r.getReason());

                                            } else {
                                                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                            }
                                        }
                                    }
                                };
                                confirmPanel.setTitle("Вопрос");
                                confirmPanel.setMessage(
                                        "<span style='color:red;'>Удалить заказ безвозвратно?</span>");
                                confirmPanel.showPanel();

                            } else {
                                PopupPanel popupPanel = new PopupPanel(getSession());
                                popupPanel.setTitle("<span style='color:red;'>Предупреждение!<span style='color:red;'>");
                                popupPanel.setPanel("У вас нет "
                                        + "права на удаление заказа который "
                                        + "находиться в работе");
                                popupPanel.showPanel("getUICore().showPopupPanel");
                            }
                        }
                    }

                    if (item.getKey().equals("anyOrders")) {
                        Result r = core.getOrderWorkStatus(selectedOrderId);
                        if (r.isError()) {
                            JSMediator.alert(getSession(), r.getReason());
                            return;

                        } else {
                            if ((int) r.getObject() == Order.WORK_DEFERRED) {
                                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                                    @Override
                                    public void pressed(int button) {
                                        if (button == ConfirmPanel.YES) {
                                            Result r = core.removeOrderDeffered(selectedOrderId);
                                            if (r.isError()) {
                                                JSMediator.alert(getSession(), r.getReason());

                                            } else {
                                                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                            }
                                        }
                                    }
                                };
                                confirmPanel.setTitle("Вопрос");
                                confirmPanel.setMessage(
                                        "<span style='color:red;'>"
                                        + "Удалить заказ безвозвратно?"
                                        + "</span>");

                                confirmPanel.showPanel();

                            } else if ((int) r.getObject() == Order.WORK_INWORK
                                    || (int) r.getObject() == Order.WORK_COMPLETE) {

                                final RadioButton rbNothing = new RadioButton(
                                        getSession(),
                                        "select", "Ничего не делать, оставить все как есть", true);
                                final RadioButton rbAllPay = new RadioButton(
                                        getSession(),
                                        "select", "Клиент заплатит за все", false);
                                final RadioButton rbChunkPay = new RadioButton(
                                        getSession(),
                                        "select", "Клиент оплатит частично", false);
                                final RadioButton rbNoPay = new RadioButton(
                                        getSession(),
                                        "select", "Клиент не оплатит ничего", false);
                                RadioButtonGroup radioButtonGroup =
                                        new RadioButtonGroup(rbNothing, rbAllPay, rbChunkPay, rbNoPay);

                                String status = "имеет статус <b>«В работе»</b>";
                                if ((int) r.getObject() == Order.WORK_COMPLETE) {
                                    status = "имеет статус <b>«Выполнен»</b>";
                                }

                                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                                    @Override
                                    public void pressed(int button) {
                                        Result r = core.getOrder(selectedOrderId);
                                        if (r.isError()) {
                                            JSMediator.alert(getSession(), r.getReason());
                                            return;
                                        }

                                        if (rbNothing.isChecked()) {
                                            JSMediator.showLockPanel(getSession());
                                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                            JSMediator.hideLockPanel(getSession());

                                        } else if (rbAllPay.isChecked()) {
                                            WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                                                    UnpaidRightPanel.class.getName(),
                                                    "{eventType:push, session:" + getSession() + ", action:pay, "
                                                    + "orderId:" + selectedOrderId + "}");

                                        } else if (rbChunkPay.isChecked()) {
                                            removeOrderPanel = new RemoveOrderPanel(getSession(), (Order) r.getObject(), dataBase) {
                                                @Override
                                                public void close(LinkedHashSet<GoodsOrderRow> goodsSet,
                                                        LinkedHashSet<ServiceOrderRow> serviceSet) {
                                                    Result r = core.removeOrderInWorkChunk(
                                                            selectedOrderId, goodsSet, serviceSet);
                                                    try {
                                                        if (r.isError()) {
                                                            JSMediator.alert(getSession(), r.getReason());
                                                        } else {
                                                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                                        }

                                                    } catch (Exception e) {
                                                        Logger.getGlobal().log(Level.WARNING, null, e);
                                                    }
                                                }
                                            };

                                            JSMediator.showRemoveOrderPanel(getSession(), removeOrderPanel.getModel());

                                        } else if (rbNoPay.isChecked()) {
                                            r = core.removeOrderInWorkAll(selectedOrderId);
                                            if (r.isError()) {
                                                JSMediator.alert(getSession(), r.getReason());
                                                JSMediator.showLockPanel(getSession());
                                                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                                JSMediator.hideLockPanel(getSession());

                                            } else {
                                                JSMediator.showLockPanel(getSession());
                                                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                                                JSMediator.hideLockPanel(getSession());
                                            }
                                        }
                                    }
                                };

                                confirmPanel.setTitle("Вопрос");
                                confirmPanel.setMessage("Вы "
                                        + "действительно желаете удалить заказ, "
                                        + "который " + status + "?<br/>"
                                        + "<b>Предупреждение!</b><br/>"
                                        + "1. Не все товары можно вернуть на склад.<br/>"
                                        + "2. Вам будет необходимо определить, "
                                        + "будет ли клиент оплачивать товары, которые "
                                        + "можно вернуть на склад.<br/>"
                                        + "3. Вам также будет необходимо определить, "
                                        + "за какие из уже оказанных услуг с клиента "
                                        + "необходимо получить оплату.<br/>"
                                        + "4. Все не оплаченные, но использованные "
                                        + "товары/услуги будут списаны в убытки<br/>"
                                        + "<hr/>"
                                        + "<b>Для продолжения процедуры удаления выберите "
                                        + "один из следующих вариантов:</b><br>"
                                        + rbNothing.getModel() + "<br/>"
                                        + rbAllPay.getModel() + "<br/>"
                                        + rbChunkPay.getModel() + "<br/>"
                                        + rbNoPay.getModel() + "<br/>");
                                confirmPanel.showPanel();

                            } else if ((int) r.getObject() == Order.WORK_CHUNKED_RETURN) {
                                PopupPanel popupPanel = new PopupPanel(getSession());
                                popupPanel.setTitle("Предупреждение!");
                                String panel = "<span style='color:red;'>"
                                        + "Этот заказ уже помечен как "
                                        + "удаляемый с частичной оплатой реализованных "
                                        + "клиенту товаров/услуг. "
                                        + "Вам осталось лишь оплатить этот заказ."
                                        + "</span>";
                                popupPanel.setPanel(panel);
                                popupPanel.showPanel();
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return toolbarButtons;
    }

    @Override
    public String getLeafMenu(final TreeLeaf leaf) {
        return "";
    }

    public String getLeafMenu() {
        String model = "";
        MenuItem itemPay = new MenuItem(getSession(), "img/subbuttons/coins.png", "Оплатить");
        itemPay.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(),
                        UnpaidRightPanel.class.getName(),
                        "{eventType:push, session:" + getSession() + ", action:showOrder, "
                        + "dbid:" + selectedOrderId + "}");
            }
        });
        model += itemPay.getModel();

        MenuItem itemDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                removeOrder();
            }
        });

        if (isCanDelete()) {
            model += itemDelete.getModel();
        }

        return model;
    }

    @Override
    public String getFolderMenu(TreeFolder folder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPanelMenu() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean isCanDelete() {
        boolean isShow = true;
        Result result = core.getRulesItemByKey(getSession(), "canDeleteUnpaidOrder");
        if (result.isError()) {
            JSMediator.alert(getSession(), result.getReason());

        } else {
            RuleItem ruleItem = (RuleItem) result.getObject();
            for (SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()) {
                if (item.isSelected()) {
                    if (item.getKey().equals("deleteOrdersDeny")) {
                        isShow = false;
                        break;
                    }
                }
            }
        }

        return isShow;
    }

    private String getSubOperationButtonModel() {
        int cols = getWorkpanelToolbars().size() / 2;
        int col = 0;
        String model = "<table class='subButtonsTable'><tr>";

        for (org.uui.ribbon.RibbonButton rb : getWorkpanelToolbars()) {
            if (col == cols) {
                model += "</tr><tr>";
            }

            if (rb.getActionName().equals("delete")) {
                if (isCanDelete()) {
                    model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
                }
            } else {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            }

            col++;
        }

        model += "</tr></table>";

        return model;
    }

    @Override
    public String getIdentificator() {
        return UnpaidPanel.class.getName();
    }

    @Override
    public void moveDown(TreeFolder treeFolder) {
        currentTreeFolder = treeFolder;
        setNavigatorPanel();
        JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
    }

    @Override
    public void navigatorButtonAction(UIEvent evt) {
        try {
            TreeFolder tfb = (TreeFolder) dataBase.getObject(
                    evt.getJSONObject().getString("className"),
                    evt.getJSONObject().getLong("dbid"));
            currentTreeFolder = tfb;
            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

            setNavigatorPanel();
            JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());

        } catch (JSONException e) {
            Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
        }
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

            if (jsonObject.getString("eventType").equals("showWorkPanel")) {
                super.switchView(ExplorerPanel.PANEL_TABLE);
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("switchToEdit")) {
                super.switchView(ExplorerPanel.PANEL_ICON);
                super.replaceTree((Tree) CarssierCore.getInstance().getTreeGoodsAndService().getObject());

                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
            }

            if (jsonObject.getString("eventType").equals("switchToView")) {
                super.replaceTree((Tree) CarssierCore.getInstance().getTreeOrdersUnpayed().getObject());
                super.switchView(ExplorerPanel.PANEL_TABLE);
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("rightclick")) {
                JSMediator.setContextMenu(getSession(), getPanelMenu());
            }

            if (jsonObject.getString("eventType").equals("click")) {
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.hideNavigationPanel(getSession());
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Неоплачено");
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showWorkPanel")) {
                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                }
            }

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    @Override
    public String treeWalker(TreeFolder treeFolder) {
        StringBuilder sb = new StringBuilder();

        Set<TreeFolder> folders = treeFolder.getSetTreeFolder();
        Set<TreeLeaf> leaves = treeFolder.getSetTreeLeaf();

        Set<ExplorerFolder> eFolders = new LinkedHashSet<>();
        Set<ExplorerLeaf> eLabels = new LinkedHashSet<>();

        for (TreeFolder tf : folders) {
            ExplorerFolder folder = new ExplorerFolder(getSession(), tf.getClass().getName(),
                    ((KnowsId) tf).getId(), tf.getName(), tf.getImageFileName());

            folder.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            TreeFolder selectedTreeFolder = (TreeFolder) db.getObject(
                                    evt.getJSONObject().getString("className"),
                                    evt.getJSONObject().getLong("dbid"));

                            JSMediator.setContextMenu(getSession(), getFolderMenu(selectedTreeFolder));
                        }

                        if (evt.getJSONObject().getString("eventType").equals("click")) {
                            TreeFolder selectedTreeFolder = (TreeFolder) db.getObject(
                                    evt.getJSONObject().getString("className"),
                                    evt.getJSONObject().getLong("dbid"));

                            moveDown(selectedTreeFolder);

                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                        }

                        if (evt.getJSONObject().getString("eventType").equals("drop")) {
                            cutAndPaste(evt.getJSONObject());
                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                        }

                        if (evt.getJSONObject().getString("eventType").equals("keypress")) {
                            rename(evt.getJSONObject());
                        }
                    } catch (JSONException ex) {
                        Logger.getGlobal().log(Level.WARNING, evt.toString(), ex);
                    }
                }
            });
            eFolders.add(folder);
        }

        for (TreeLeaf tl : leaves) {
            boolean isContinue = false;
            if ((this.filterClasses != null)
                    && (this.isFilterEnable)) {
                String contClass = tl.getContainer().getClassName();
                for (String cls : this.filterClasses) {
                    if (cls.equals(contClass)) {
                        isContinue = true;
                    }
                }

            }

            if (isContinue) {
                continue;
            }
            String img = "img/icons/service.png";
            if (!tl.getImageFileName().trim().equals("")) {
                img = tl.getImageFileName();
            }

            String info = core.getPriceCountForSales((TreeLeafBasic) tl);
            ExplorerLeaf label = new ExplorerLeaf(getSession(), tl.getClass().getName(), ((KnowsId) tl).getId(), tl.getName(), img);

            label.setExtraText(info);
            label.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            TreeLeaf selectedTreeLeaf = (TreeLeaf) db.getObject(evt.getJSONObject().getString("className"), evt.getJSONObject().getLong("dbid"));

                            JSMediator.setContextMenu(getSession(), getLeafMenu(selectedTreeLeaf));
                        }

                        if (evt.getJSONObject().getString("eventType").equals("keypress")) {
                            rename(evt.getJSONObject());
                        }
                    } catch (JSONException e) {
                        Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
                    }
                }
            });
            eLabels.add(label);
        }

        Logger.getGlobal().log(Level.INFO, "htmlModel = {0}", this.htmlModel);

        for (ExplorerFolder ef : eFolders) {
            sb.append(ef.getModel());
        }

        for (ExplorerLeaf el : eLabels) {
            sb.append(el.getModel());
        }

        setNavigatorPanel();

        return sb.toString();
    }
}
