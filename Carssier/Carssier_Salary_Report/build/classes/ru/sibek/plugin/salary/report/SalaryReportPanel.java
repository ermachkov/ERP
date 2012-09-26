/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.salary.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.money.Money;
import org.ubo.report.ReportShift;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.utils.Result;
import org.ucm.cashmachine.CashMachineResponse;
import org.ucm.cashmachine.ResponseItem;
import org.uui.component.*;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.plugin.HasRules;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.PopupPanel;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class SalaryReportPanel extends WorkPanel implements HasWorkPanelToolbars,
        HasRightPanels, HasRules {

    private Button btnShow;
    private MacTableModel macTableModel;
    private CarssierCore core = CarssierCore.getInstance();
    private FilterPanel filterPanel;

    public SalaryReportPanel(String sessionId) {
        super(sessionId);
        macTableModel = new MacTableModel(sessionId, true);
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("trialBalanceAccountTable");
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Дата", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Описание", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Статус", String.class, false));
        macTableModel.setHeader(mth);
        macTableModel.addUIEventListener(getTableListener());
        macTableModel.setNavigatorShowingAlways(true);
        macTableModel.setNavigatorDateSelectorEnabled(true);
        filterPanel = new FilterPanel(sessionId);
        macTableModel.getMacTableNavigator().setFilterPanel(filterPanel);

        macTableModel.addNavigatorChangeListener(new NavigatorChangeListener() {
            @Override
            public void event(int event) {
                try {
                    if (event == MacTableNavigator.FILTER) {
                        setMacTableFilterVisible(true);

                    } else if (event == MacTableNavigator.REFRESH) {
                        macTableModel.getMacTableNavigator().resetPage();
                        JSMediator.showLockPanel(getSession());
                        JSMediator.setWorkPanel(getSession(), getModel());
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
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });
    }

    private void setMacTableFilterVisible(boolean isVisible) {
        if (isVisible) {
            LockSupport.parkNanos(1000000000); // !!!!!!!!!!!! HACK
            String filterModel = macTableModel.getMacTableNavigator().getFilterPanel().getModel();
            String identificator = macTableModel.getMacTableNavigator().getIdentificator();
            JSMediator.showFilterPanel(getSession(), filterModel, identificator);
        }
    }

    private UIEventListener getTableListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSONObject jsonObject = evt.getJSONObject();
                    if (jsonObject.getString("eventType").equals("click")) {
                        switch (filterPanel.getMode().getSelectedValue()) {
                            case "notDistributeSalary":
                                MacTableRow row = macTableModel.getRow(jsonObject.getInt("row"));
                                long reportShiftId = ((ReportShift) row.getValue()).getId();
                                String json = "{eventType:push, session:" + getSession() + ", "
                                        + "action:showSalaryDistributionPanel, "
                                        + "reportShiftId:" + reportShiftId + "}";
                                WebKitEventBridge.getInstance().pushEventToComponent(
                                        getSession(), SalaryReportRightPanel.class.getName(), json);
                                break;

                            case "notPaidSalary":
                                row = macTableModel.getRow(jsonObject.getInt("row"));
                                reportShiftId = ((ReportShift) row.getValue()).getId();
                                json = "{eventType:push, session:" + getSession() + ", "
                                        + "action:showSalaryInfoPanel, "
                                        + "reportShiftId:" + reportShiftId + "}";
                                WebKitEventBridge.getInstance().pushEventToComponent(
                                        getSession(), SalaryReportRightPanel.class.getName(), json);
                                break;

                            case "paidSalary":
                                row = macTableModel.getRow(jsonObject.getInt("row"));
                                reportShiftId = ((ReportShift) row.getValue()).getId();
                                json = "{eventType:push, session:" + getSession() + ", "
                                        + "action:showSalaryInfoPanel, "
                                        + "reportShiftId:" + reportShiftId + "}";
                                WebKitEventBridge.getInstance().pushEventToComponent(
                                        getSession(), SalaryReportRightPanel.class.getName(), json);
                                break;
                        }
                    }
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
                }

            }
        };

        return listener;
    }

    @Override
    public java.util.List<RibbonButton> getWorkpanelToolbars() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("distributeSalaryDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("distributeSalaryAllow");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canDistributeSalary",
                "Распределение зарплаты:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("viewUnpaidSalaryDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("viewUnpaidSalaryAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canViewUnpaidSalary",
                "Просмотр невыплаченной зарплаты:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("viewPaidSalaryDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("viewPaidSalaryAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canViewPaidSalary",
                "Просмотр выплаченной зарплаты:", selectorRuleItemList, select);
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
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Зарплата");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showFullPanel")) {
                    JSMediator.setWorkPanel(getSession(), getModel());
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("hideRightPanel")) {
                    JSMediator.hideRightPanel(getSession());
                }

                JSMediator.setWorkPanel(getSession(), getModel());
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("paySalary")) {
                    paidSalary(jsonObject.getLong("reportShiftId"));
                }
            }

        } catch (JSONException ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    public void paidSalary(long reportShiftId) {
        Result r = core.getReportShift(reportShiftId);
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());

        } else {
            RadioButton rbSingle = new RadioButton(getSession(), "type", "Сделать выплату одним чеком", true);
            RadioButton rbMulti = new RadioButton(getSession(), "type", "Сделать выплату по чеку на каждого", false);
            RadioButtonGroup rbg = new RadioButtonGroup(rbSingle, rbMulti);

            PopupPanel popupPanel = new PopupPanel(getSession());
            popupPanel.setTitle("Вопрос");
            popupPanel.setPanel(
                    "<div>"
                    + rbSingle.getModel()
                    + "<br/><br/>"
                    + rbMulti.getModel()
                    + "<br/>"
                    + "</div>");
            popupPanel.showPanel();

            r = core.paidSalary((ReportShift) r.getObject(), rbSingle.isChecked(), 48);

            if (r.isError()) {
                String errorMessage = "";
                if (r.getObject() instanceof CashMachineResponse) {
                    CashMachineResponse cmr = (CashMachineResponse) r.getObject();
                    for (ResponseItem responseItem : cmr.getResponseItemList()) {
                        if (responseItem.isError()) {
                            errorMessage += "Команда: " + responseItem.getHumanCommand() + "<br/>";
                            errorMessage += "Вызвала ошибку: " + responseItem.getHumanError() + "<br/><br/>";
                        }
                    }
                } else {
                    errorMessage = r.getReason();
                }

                popupPanel.setTitle("Ошибка!");
                popupPanel.setPanel("<div>" + errorMessage + "</div>");
                popupPanel.showPanel();

            } else {
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.hideNavigationPanel(getSession());
            }
        }
    }

    @Override
    public String getModel() {
        String table = "";
        switch (filterPanel.getMode().getSelectedValue()) {
            case "notDistributeSalary":
                table = getSalaryTable(ReportShift.SALARY_NOT_DISTRIBUTED);
                break;

            case "notPaidSalary":
                table = getSalaryTable(ReportShift.SALARY_DISTRIBUTED_UNPAID);
                break;

            case "paidSalary":
                table = getSalaryTable(ReportShift.SALARY_DISTRIBUTED_PAID);
                break;
        }

        return table;
    }

    private String getSalaryTable(int reportShiftSalaryType) {
        WebKitEventBridge.getInstance().cleanMacTable(getSession(), macTableModel);

        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<ReportShift> reportShifts = core.getReportShifts(reportShiftSalaryType);

        for (ReportShift reportShift : reportShifts) {
            if (reportShift.getDate() == null) {
                continue;
            }

            if (reportShift.getDate().getTime() > macTableModel.getMacTableNavigator().getDateEnd().getTime()) {
                continue;
            }

            if (reportShift.getDate().getTime() < macTableModel.getMacTableNavigator().getDateStart().getTime()) {
                continue;
            }

            MacTableRow macTableRow = new MacTableRow();
            macTableRow.setValue(reportShift);
            macTableRow.addCell(new MacTableCell(getSession(), 
                    DateTime.getFormatedDate("dd.MM.yyyy HH:mm:ss", reportShift.getDate()), false));
            macTableRow.addCell(new MacTableCell(getSession(), reportShift.getName(), false));

            switch (reportShiftSalaryType) {
                case ReportShift.SALARY_NOT_DISTRIBUTED:
                    macTableRow.addCell(new MacTableCell(getSession(), 
                            core.getSalarySumForReportShift(reportShift.getId()), false));
                    break;

                case ReportShift.SALARY_DISTRIBUTED_UNPAID:
                    BigDecimal val = Money.ADD(
                            core.getSalarySumForReportShift(reportShift.getId()).toString(),
                            reportShift.getWithdrawal().toString());
                    macTableRow.addCell(new MacTableCell(getSession(), 
                            val, false));
                    break;

                case ReportShift.SALARY_DISTRIBUTED_PAID:
                    val = Money.ADD(
                            core.getSalarySumForReportShift(reportShift.getId()).toString(),
                            reportShift.getWithdrawal().toString());
                    macTableRow.addCell(new MacTableCell(getSession(), 
                            val, false));
                    break;
            }

            String icon = "";
            switch (reportShiftSalaryType) {
                case ReportShift.SALARY_NOT_DISTRIBUTED:
                    icon = "unpaid.png";
                    break;

                case ReportShift.SALARY_DISTRIBUTED_UNPAID:
                    icon = "unpaid.png";
                    break;

                case ReportShift.SALARY_DISTRIBUTED_PAID:
                    icon = "coins.png";
                    break;
            }

            String img = "<div align='center'><img src='img/subbuttons/" + icon + "' /></div>";
            macTableRow.addCell(new MacTableCell(getSession(), img, false));

            rows.add(macTableRow);
        }

        macTableModel.setData(rows);

        return macTableModel.getModel();
    }

    @Override
    public String getIdentificator() {
        return SalaryReportPanel.class.getName();
    }
}
