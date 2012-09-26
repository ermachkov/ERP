/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.shifts;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.document.SalesItem;
import org.ubo.employee.Employee;
import org.ubo.goods.Goods;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.money.Money;
import org.ubo.quantity.Quantity;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.service.Service;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.plugin.HasRules;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.table.*;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.business.users.User;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class ShiftPanel extends WorkPanel implements HasWorkPanelToolbars,
        HasRightPanels, HasRules {

    private Button btnClose, btnShow, btnPrint;
    private RadioButton rbtOrderView, rbtDetailsView;
    private MacTableModel macTableModel, macTableModelDetails;
    private CarssierCore core = CarssierCore.getInstance();
    private DataBase dataBase;

    public ShiftPanel(String sessionId) {
        super(sessionId);
        dataBase = CarssierDataBase.getDataBase();

        btnClose = new Button(getSession(), "Закрыть смену");
        btnClose.addUIEventListener(getButtonCloseEventListener());

        btnShow = new Button(getSession(), "Показать");
        btnShow.addUIEventListener(getButtonShowListener());

        rbtOrderView = new RadioButton(sessionId, "view", "Заказы", true);
        rbtDetailsView = new RadioButton(sessionId, "view", "Детализация", false);//9136171250
        RadioButtonGroup rbg = new RadioButtonGroup(rbtOrderView, rbtDetailsView);

        btnPrint = new Button(getSession(), "Печать");
        btnPrint.addUIEventListener(getButtonPrintEventListener());

        macTableModel = new MacTableModel(getSession(), true, new MacTableSummator(3));
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("trialBalanceAccountTable");
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Дата", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Заказ", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Статус", String.class, false));
        macTableModel.setHeader(mth);
        macTableModel.addUIEventListener(getTableListener());

        macTableModelDetails = new MacTableModel(getSession(), true, new MacTableSummator(3));
        macTableModelDetails.setCssClass("leftMacTable");
        macTableModelDetails.setId("trialBalanceAccountTableDetails");
        MacTableHeaderModel mthDetails = new MacTableHeaderModel();
        mthDetails.addHeaderColumn(new MacHeaderColumn("Наименование", String.class, false));
        mthDetails.addHeaderColumn(new MacHeaderColumn("Кол-во", Number.class, false));
        mthDetails.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, false));
        macTableModelDetails.setHeader(mthDetails);
    }

    private UIEventListener getButtonPrintEventListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                String model;
                if (rbtOrderView.isChecked()) {
                    model = macTableModel.getModel();
                } else {
                    model = macTableModelDetails.getModel();
                }

                model = "<div style='margin: 15mm;'>" + model + "</div>";
                JSMediator.print(getSession(), model);

            }
        };

        return listener;
    }

    private UIEventListener getButtonShowListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSMediator.setWorkPanel(getSession(), getModel());
                    JSMediator.hideNavigationPanel(getSession());

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    private UIEventListener getButtonCloseEventListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                String errorMsg = "";
                User user = core.getLoggedUser(getSession());
                String strEmployeeId = Objects.toString(user.getExtraInfoByKey("employeeId"), "");
                if (strEmployeeId.equals("")) {
                    errorMsg = "Закрыть смену может только мастер";

                } else {
                    Result r = core.getEmployeeById(Long.parseLong(strEmployeeId));
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        return;

                    } else {
                        Employee e = (Employee) r.getObject();
                        if (e.getRole().indexOf("foreman") == -1) {
                            errorMsg = "Закрыть смену может только мастер";
                        }
                    }
                }

                if (!errorMsg.equals("")) {
                    PopupPanel popupPanel = new PopupPanel(getSession());
                    popupPanel.setTitle("Предупреждение");
                    popupPanel.setPanel(errorMsg);
                    popupPanel.showPanel();
                    return;
                }

                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                    @Override
                    public void pressed(int button) {
                        if (button == ConfirmPanel.YES) {
                            closeShift();
                        }
                    }
                };
                confirmPanel.setTitle("Вопрос");
                confirmPanel.setMessage("Закрыть смену?");
                confirmPanel.showPanel();
            }
        };

        return listener;
    }

    private void closeShift() {
        JSMediator.showLockPanel(ShiftPanel.this.getSession());
        Result r = core.closeShift();
        if (r.isError()) {
            JSMediator.alert(ShiftPanel.this.getSession(), r.getReason());
        }
        JSMediator.setWorkPanel(ShiftPanel.this.getSession(), getModel());
        JSMediator.hideLockPanel(ShiftPanel.this.getSession());
    }

    private UIEventListener getTableListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                //
            }
        };

        return listener;
    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("useShiftsDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("useShiftsAllow");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canUseShifts",
                "Управление сменой:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
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

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Смена");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.hideNavigationPanel(getSession());
            }

        } catch (JSONException ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    @Override
    public String getModel() {
        ArrayList<Order> ordersList = core.getOrdersFromCurrentShift();
        if (ordersList.isEmpty()) {
            btnClose.setEnabled(false);
        } else {
            btnClose.setEnabled(true);
        }

        String employees = "<table width='100%' border='0' cellpadding='0' cellspacing='0'>";
        for (Employee employee : core.getAllEmployeeAtWork()) {
            employees += "<tr>";
            if (employee.getRole().equals("foreman")) {
                employees += "<td width='26' style='border-bottom: 1px dotted gray;'>"
                        + "<img src='img/icons/master24.png' />"
                        + "</td>"
                        + "<td style='font-size:80%; border-bottom: 1px dotted gray;'>"
                        + employee.getName() + "</td>";
            } else {
                employees += "<td width='26' style='border-bottom: 1px dotted gray;'>"
                        + "<img src='img/icons/employee24.png' />"
                        + "</td>"
                        + "<td style='font-size:80%; border-bottom: 1px dotted gray;'>"
                        + employee.getName() + "</td>";
            }
            employees += "</tr>";
        }
        employees += "</table>";

        String t = "<table width='100%'>"
                + "<tr>"
                + "<td width='50%' style='font-size:70%;'>"
                + rbtOrderView.getModel()
                + rbtDetailsView.getModel()
                + "&nbsp;"
                + btnShow.getModel()
                + btnPrint.getModel()
                + "</td>"
                + "<td align='right'>"
                + btnClose.getModel()
                + "</td>"
                + "</tr>"
                + "</table>";

        String top = "<table width='100%' height='100%' border='0' cellpadding='0' cellspacing='0'>"
                + "<tr height='30'>"
                + "<td style='border-bottom: 1px solid black; padding-left:5px; "
                + "font-size:70%; font-weight: bold; border-right: 1px solid black;"
                + "background-color: lightgray;'>"
                + "Смена"
                + "</td>"
                + "<td align='right' style='border-bottom: 1px solid black; background-color: lightgray;'>"
                + t
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td width='20%' valign='top' style='padding:5px; border-right: 1px solid black;'>"
                + "<div style='width:100%; height:100%; overflow:auto;'>"
                + employees
                + "</div>"
                + "</td>"
                + "<td valign='top'>";

        if (rbtOrderView.isChecked()) {
            top += getOrdersTable();
        } else {
            top += getDetailsTable();
        }

        top += "</td>"
                + "</tr>"
                + "</table>";

        String model = top + "";
        return model;
    }

    private String getDetailsTable() {
        Map<Long, JSONObject> serviceMap = new HashMap<>();
        Map<Long, JSONObject> goodsMap = new HashMap<>();
        BigDecimal discountSum = BigDecimal.ZERO;

        ArrayList<Order> ordersList = core.getOrdersFromCurrentShift();
        for (Order order : ordersList) {

            discountSum = Money.ADD(order.getTotalDiscountSum().toString(), discountSum.toString());

            for (OrderRow orderRow : order.getOrderRows()) {
                SalesItem salesItem = orderRow.getSalesItem(dataBase);
                if (salesItem.getType() == SalesItem.SERVICE) {
                    Result r = core.getService(salesItem.getDbId());
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        continue;
                    }

                    Service service = (Service) r.getObject();
                    if (serviceMap.containsKey(service.getId())) {
                        try {
                            JSONObject json = serviceMap.get(service.getId());
                            String sCount = json.getString("count");
                            String sSum = json.getString("sum");
                            BigDecimal bCount = Quantity.ADD(sCount, orderRow.getCount().toString());
                            BigDecimal bSum = Money.ADD(sSum, orderRow.getSumWithDiscount().toString());
                            json.put("count", bCount.toString());
                            json.put("sum", bSum.toString());
                            serviceMap.put(service.getId(), json);

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                            Logger.getGlobal().log(Level.WARNING, null, e);
                            continue;
                        }

                    } else {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("count", orderRow.getCount().toString());
                            json.put("sum", orderRow.getSumWithDiscount().toString());
                            serviceMap.put(service.getId(), json);

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                            Logger.getGlobal().log(Level.WARNING, null, e);
                            continue;
                        }

                    }

                } else {
                    Result r = core.getGoods(salesItem.getDbId());
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        continue;
                    }

                    Goods goods = (Goods) r.getObject();
                    if (goodsMap.containsKey(goods.getId())) {
                        try {
                            JSONObject json = goodsMap.get(goods.getId());
                            String sCount = json.getString("count");
                            String sSum = json.getString("sum");
                            BigDecimal bCount = Quantity.ADD(sCount, orderRow.getCount().toString());
                            BigDecimal bSum = Money.ADD(sSum, orderRow.getSumWithDiscount().toString());
                            json.put("count", bCount.toString());
                            json.put("sum", bSum.toString());
                            goodsMap.put(goods.getId(), json);

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                            Logger.getGlobal().log(Level.WARNING, null, e);
                            continue;
                        }

                    } else {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("count", orderRow.getCount().toString());
                            json.put("sum", orderRow.getSumWithDiscount().toString());
                            goodsMap.put(goods.getId(), json);

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                            Logger.getGlobal().log(Level.WARNING, null, e);
                            continue;
                        }
                    }
                }
            }
        }

        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Long> serviceList = new ArrayList<>();
        serviceList.addAll(serviceMap.keySet());

        Iterator<Long> it = serviceList.iterator();
        while (it.hasNext()) {
            try {
                MacTableRow macTableRow = new MacTableRow();
                long id = it.next();
                JSONObject json = serviceMap.get(id);
                Service s = (Service) core.getService(id).getObject();
                macTableRow.addCell(new MacTableCell(getSession(), s.getName(), false));
                macTableRow.addCell(new MacTableCell(getSession(), new BigDecimal(json.getString("count")), false));
                macTableRow.addCell(new MacTableCell(getSession(), new BigDecimal(json.getString("sum")), false));
                rows.add(macTableRow);

            } catch (Exception e) {
                JSMediator.alert(getSession(), e.toString());
                Logger.getGlobal().log(Level.WARNING, null, e);
                continue;
            }
        }

        //
        ArrayList<Long> goodsList = new ArrayList<>();
        goodsList.addAll(goodsMap.keySet());

        Iterator<Long> itGoods = goodsList.iterator();
        while (itGoods.hasNext()) {
            try {
                MacTableRow macTableRow = new MacTableRow();
                long id = itGoods.next();
                JSONObject json = goodsMap.get(id);
                Goods g = (Goods) core.getGoods(id).getObject();
                macTableRow.addCell(new MacTableCell(getSession(), g.getName(), false));
                macTableRow.addCell(new MacTableCell(getSession(), new BigDecimal(json.getString("count")), false));
                macTableRow.addCell(new MacTableCell(getSession(), new BigDecimal(json.getString("sum")), false));
                rows.add(macTableRow);

            } catch (Exception e) {
                JSMediator.alert(getSession(), e.toString());
                Logger.getGlobal().log(Level.WARNING, null, e);
                continue;
            }
        }

        Collections.sort(rows, new Comparator<MacTableRow>() {
            @Override
            public int compare(MacTableRow o1, MacTableRow o2) {
                return ("" + o1.getCells().get(0).getValue()).compareTo("" + o2.getCells().get(0).getValue());
            }
        });

        MacTableRow macTableRow = new MacTableRow();
        macTableRow.addCell(new MacTableCell(getSession(), "<div align='right'><strong>Общая скидка</strong></div>", false));
        macTableRow.addCell(new MacTableCell(getSession(), new BigDecimal("1"), false));
        macTableRow.addCell(new MacTableCell(getSession(), Money.MULTIPLY("-1", discountSum.toString()), false));
        rows.add(macTableRow);
        macTableModelDetails.setData(rows);

        return macTableModelDetails.getModel();
    }

    public String getOrdersTable() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Order> ordersList = core.getOrdersFromCurrentShift();
        for (Order order : ordersList) {
            MacTableRow macTableRow = new MacTableRow();
            macTableRow.addCell(new MacTableCell(getSession(), 
                    DateTime.getFormatedDate("dd.MM.yyyy", order.getDate()), false));
            macTableRow.addCell(new MacTableCell(getSession(), order.getName(), false));
            macTableRow.addCell(new MacTableCell(getSession(), order.getTotalWithTotalDiscount(), false));

            String workStatus = "<img src='img/subbuttons/move_to_complite.png' />";
            if (order.getWorkStatus() == Order.WORK_INWORK) {
                workStatus = "<img src='img/subbuttons/move_to_work.png' />";
            } else if (order.getWorkStatus() == Order.WORK_DEFERRED) {
                workStatus = "<img src='img/subbuttons/move_to_deferred.png' />";
            }

            String paidStatus = "<img src='img/subbuttons/coins.png' />";
            if (order.getPaidStatus() == Order.UNPAID || order.getPaidStatus() == Order.CHUNK_PAID) {
                paidStatus = "<img src='img/subbuttons/unpaid.png' />";
            }
            String img = "<div align='center'>"
                    + workStatus
                    + "&nbsp;"
                    + paidStatus
                    + "</div>";
            macTableRow.addCell(new MacTableCell(getSession(), img, false));
            rows.add(macTableRow);
        }

        macTableModel.setData(rows);
        return macTableModel.getModel();
    }
}
