package ru.sibek.plugin.accountbook.trialbalance;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.accountbook.SyntheticAccount;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.money.Money;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.utils.Result;
import org.uui.component.HasRightPanels;
import org.uui.component.WorkPanel;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.plugin.HasRules;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.accountbook.AccountBookHandler;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;

public class TrialBalancePanel extends WorkPanel
        implements HasWorkPanelToolbars, HasRightPanels, HasRules {

    private AccountBookHandler accountBookHandler = AccountBookHandler.getInstance();
    private CarssierCore core = CarssierCore.getInstance();
    private MacTableModel macTableModel;
    private FilterPanel filterPanel;

    public TrialBalancePanel(String sessionId) {
        super(sessionId);
        macTableModel = new MacTableModel(sessionId, true);
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("trialBalanceAccountTable");
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Дата", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Описание", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Дебет", Number.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Кредит", Number.class, false));
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

        initAccountsList();
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

    private UIEventListener getTableListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                //
            }
        };
        return listener;
    }

    private String getAccountTable(ArrayList<SyntheticAccount> listAccount,
            String dateStart, String dateEnd) {
        ArrayList<MacTableRow> list = new ArrayList();

        BigDecimal[] startTrialBalance = accountBookHandler.getTrialBalance(
                filterPanel.getAccounts().getSelectedValue(), dateStart);
        MacTableRow macTableRowStart = new MacTableRow();
        macTableRowStart.addCell(new MacTableCell(getSession(), "", false));
        macTableRowStart.addCell(new MacTableCell(getSession(), "<div align='right' style='font-weight:bold;'>Входящий остаток:</div>", false));

        MacTableCell mtc = new MacTableCell(getSession(), startTrialBalance[0], false);
        mtc.setStyle("font-weight:bold;");
        macTableRowStart.addCell(mtc);

        mtc = new MacTableCell(getSession(), startTrialBalance[1], false);
        mtc.setStyle("font-weight:bold;");
        macTableRowStart.addCell(mtc);
        list.add(macTableRowStart);

        BigDecimal totalDebet = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        
        for (SyntheticAccount account : listAccount) {
            MacTableRow macTableRow = new MacTableRow();
            macTableRow.setValue(account);
            macTableRow.addCell(new MacTableCell(getSession(), DateTime.getFormatedDate("dd.MM.yyyy HH:mm", account.getDate()), false));

            String description = account.getDescription() + ", " + parseAnalyticsData(account.getAnalyticsData());

            macTableRow.addCell(new MacTableCell(getSession(), description, false));
            if (account.isDebet()) {
                macTableRow.addCell(new MacTableCell(getSession(), account.getValue(), false));
                macTableRow.addCell(new MacTableCell(getSession(), Double.valueOf(0.0D), false));
                totalDebet = Money.ADD(totalDebet.toString(), account.getValue().toString());
            } else {
                macTableRow.addCell(new MacTableCell(getSession(), Double.valueOf(0.0D), false));
                macTableRow.addCell(new MacTableCell(getSession(), account.getValue(), false));
                totalCredit = Money.ADD(totalCredit.toString(), account.getValue().toString());
            }
            
            list.add(macTableRow);
        }

        MacTableRow macTableRowPeriod = new MacTableRow();
        macTableRowPeriod.addCell(new MacTableCell(getSession(), "", false));
        macTableRowPeriod.addCell(new MacTableCell(getSession(), "<div align='right' style='font-weight:bold;'>Итого за период:</div>", false));
        mtc = new MacTableCell(getSession(), totalDebet, false);
        mtc.setStyle("font-weight:bold;");
        macTableRowPeriod.addCell(mtc);

        mtc = new MacTableCell(getSession(), totalCredit, false);
        mtc.setStyle("font-weight:bold;");
        macTableRowPeriod.addCell(mtc);
        list.add(macTableRowPeriod);

        MacTableRow macTableRowTotal = new MacTableRow();
        macTableRowTotal.addCell(new MacTableCell(getSession(), "", false));
        macTableRowTotal.addCell(new MacTableCell(getSession(), "<div align='right' style='font-weight:bold;'>ИТОГО:</div>", false));

        mtc = new MacTableCell(getSession(), Money.ADD(totalDebet.toString(), startTrialBalance[0].toString()), false);
        mtc.setStyle("font-weight:bold;");
        macTableRowTotal.addCell(mtc);

        mtc = new MacTableCell(getSession(), Money.ADD(totalCredit.toString(), startTrialBalance[1].toString()), false);
        mtc.setStyle("font-weight:bold;");
        macTableRowTotal.addCell(mtc);
        list.add(macTableRowTotal);

        macTableModel.setData(list);
        return macTableModel.getModel();
    }

    private String parseAnalyticsData(String json) {
        String result = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            if ((jsonObject.has("class")) && (jsonObject.has("id"))
                    && (jsonObject.getString("class").equals(Order.class.getName()))) {
                Result r = core.getOrder(jsonObject.getLong("id"));
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                } else {
                    Order order = (Order) r.getObject();
                    result = order.getDescription();
                    result = result + ", " + order.getCustomer().getShortName();
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, json, e);
        }

        return result;
    }

    private boolean hasOrder(String json) {
        boolean result = false;
        try {
            JSONObject jsonObject = new JSONObject(json);
            result = jsonObject.has("class");
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return result;
    }

    private long getIdOrder(String json) {
        long id = -1L;
        try {
            JSONObject jsonObject = new JSONObject(json);
            if ((jsonObject.has("class")) && (jsonObject.has("id"))
                    && (jsonObject.getString("class").equals(Order.class.getName()))) {
                id = jsonObject.getLong("id");
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return id;
    }

    private void initAccountsList() {
        LinkedHashMap map = accountBookHandler.getUsedAccount();
        if (filterPanel.isAccountsDataEmpty()) {
            filterPanel.getAccounts().setItems(map);

        } else {
            int index = filterPanel.getAccounts().getSelectedIndex();
            if (!filterPanel.getAccounts().getItems().equals(map)) {
                filterPanel.getAccounts().setItems(map);
                filterPanel.getAccounts().setSelectedIndex(index);
            }
        }
    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return new ArrayList();
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList listRulesItem = new ArrayList();

        LinkedList selectorRuleItemList = new LinkedList();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("useTrialBalanceDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("useTrialBalanceAllow");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canUseTrialBalance", "Работа с оборотной ведомостью:", selectorRuleItemList, select);

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
            filterPanel.setSession(jsonObject.getString("session"));
            
            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Оборотная ведомость");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.hideNavigationPanel(getSession());
            }

        } catch (JSONException ex) {
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    @Override
    public String getIdentificator() {
        return TrialBalancePanel.class.getName();
    }

    @Override
    public String getModel() {
        accountBookHandler.loadSyntheticAccounts();
        initAccountsList();
        Date dateStart = macTableModel.getMacTableNavigator().getDateStart();
        Date dateEnd = macTableModel.getMacTableNavigator().getDateEnd();
        ArrayList<SyntheticAccount> listAccount = accountBookHandler.getAccountsByDates(
                filterPanel.getAccounts().getSelectedValue(),
                DateTime.getFormatedDate("yyyy-MM-dd HH:mm:ss", dateStart),
                DateTime.getFormatedDate("yyyy-MM-dd HH:mm:ss", dateEnd));
        
        WebKitEventBridge.getInstance().lookupInvoke(
                getSession(), 
                TrialBalanceRightPanel.class.getName(), 
                "setSyntheticAccounts", 
                new Object[]{listAccount});
        
        WebKitEventBridge.getInstance().lookupInvoke(
                getSession(), 
                TrialBalanceRightPanel.class.getName(), 
                "setAccount", 
                new Object[]{filterPanel.getAccounts().getSelectedValue()});

        return getAccountTable(
                listAccount,
                DateTime.getFormatedDate("yyyy-MM-dd HH:mm:ss", dateStart),
                DateTime.getFormatedDate("yyyy-MM-dd HH:mm:ss", dateEnd));
    }
}
