/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.accountbook.trialbalance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.accountbook.AnalyticFilter;
import org.ubo.accountbook.AnalyticFilterItem;
import org.ubo.accountbook.SyntheticAccount;
import org.ubo.document.OrderRow;
import org.ubo.employee.Crew;
import org.ubo.json.JSONObject;
import org.uui.table.*;
import ru.sibek.business.core.CarssierCore;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ReportBulder {

    private String model, sessionId;
    private CarssierCore core;

    public ReportBulder(String sessionId) {
        this.sessionId = sessionId;
        core = CarssierCore.getInstance();
    }

    public void buildReport(ArrayList<SyntheticAccount> syntheticAccounts, AnalyticFilter analyticFilter) {
        model = "";

        if (syntheticAccounts == null) {
            return;
        }

        if (syntheticAccounts.isEmpty()) {
            return;
        }

        if (analyticFilter == null) {
            return;
        }

        if (analyticFilter.getAnalyticFilterItems().isEmpty()) {
            return;
        }

        Map<String, AccountSum> crewMap = new HashMap<>();
        Map<String, AccountSum> serviceMap = new HashMap<>();
        Map<String, AccountSum> goodsMap = new HashMap<>();

        for (AnalyticFilterItem analyticFilterItem : analyticFilter.getAnalyticFilterItems()) {
            MacTableModel table = new MacTableModel(sessionId, true);
            table.setCssClass("macTable");
            table.setHeader(getMacTableHeaderModel(analyticFilterItem.getFilterType()));

            for (SyntheticAccount syntheticAccount : syntheticAccounts) {
                try {
                    JSONObject json = new JSONObject(syntheticAccount.getAnalyticsData());
                    switch (analyticFilterItem.getFilterType()) {
                        case FilterAnalytic.CREWS:
                            if (!json.has("crewId")) {
                                continue;
                            }

                            long crewId = json.getLong("crewId");
                            Crew crew = core.getCrewById(crewId);
                            AccountSum accountSum;
                            if (crewMap.containsKey(crew.getName())) {
                                accountSum = crewMap.get(crew.getName());
                            } else {
                                accountSum = new AccountSum();
                            }

                            if (syntheticAccount.isCredit()) {
                                accountSum.addCredit(syntheticAccount.getValue());
                                crewMap.put(crew.getName(), accountSum);

                            } else {
                                accountSum.addDebet(syntheticAccount.getValue());
                                crewMap.put(crew.getName(), accountSum);
                            }
                            break;

                        case FilterAnalytic.SERVICE:
                            if (!json.has("orderId")) {
                                continue;
                            }

                            long orderId = json.getLong("orderId");
                            for (OrderRow orderRow : core.getServiceRows(orderId)) {
                                String name = orderRow.getSalesItem(core.getDataBase()).getShortName();
                                if (serviceMap.containsKey(name)) {
                                    accountSum = serviceMap.get(name);
                                } else {
                                    accountSum = new AccountSum();
                                }

                                if (syntheticAccount.isCredit()) {
                                    accountSum.addCredit(syntheticAccount.getValue());
                                    accountSum.addCreditCount(orderRow.getCount());
                                    serviceMap.put(name, accountSum);

                                } else {
                                    accountSum.addDebet(syntheticAccount.getValue());
                                    accountSum.addDebetCount(orderRow.getCount());
                                    serviceMap.put(name, accountSum);
                                }
                            }
                            break;

                        case FilterAnalytic.GOODS:
                            if (!json.has("orderId")) {
                                continue;
                            }

                            orderId = json.getLong("orderId");
                            for (OrderRow orderRow : core.getGoodsRows(orderId)) {
                                String name = orderRow.getSalesItem(core.getDataBase()).getShortName();
                                if (goodsMap.containsKey(name)) {
                                    accountSum = goodsMap.get(name);
                                } else {
                                    accountSum = new AccountSum();
                                }

                                if (syntheticAccount.isCredit()) {
                                    accountSum.addCredit(syntheticAccount.getValue());
                                    accountSum.addCreditCount(orderRow.getCount());
                                    goodsMap.put(name, accountSum);

                                } else {
                                    accountSum.addDebet(syntheticAccount.getValue());
                                    accountSum.addDebetCount(orderRow.getCount());
                                    goodsMap.put(name, accountSum);
                                }
                            }
                            break;
                    }

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                    continue;
                }
            }

            String sTable = "";
            switch (analyticFilterItem.getFilterType()) {
                case FilterAnalytic.CREWS:
                    sTable = getTableModel(crewMap, table, analyticFilterItem.getFilterType());
                    break;

                case FilterAnalytic.SERVICE:
                    sTable = getTableModel(serviceMap, table, analyticFilterItem.getFilterType());
                    break;

                case FilterAnalytic.GOODS:
                    sTable = getTableModel(goodsMap, table, analyticFilterItem.getFilterType());
                    break;
            }

            model += ""
                    + "<div style='width:100%'>"
                    + "<div>" + analyticFilterItem.getFilterType() + "</div>"
                    + sTable
                    + "</div>";
        }
    }

    private MacTableHeaderModel getMacTableHeaderModel(String reportType) {
        MacTableHeaderModel mth = new MacTableHeaderModel();


        switch (reportType) {
            case FilterAnalytic.CREWS:
                mth.addHeaderColumn(new MacHeaderColumn("Название", String.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Дебет", BigDecimal.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Кредит", BigDecimal.class, false));
                break;

            case FilterAnalytic.SERVICE:
                mth.addHeaderColumn(new MacHeaderColumn("Название", String.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Кол-во дебет", BigDecimal.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Кол-во кредит", BigDecimal.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Дебет", BigDecimal.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Кредит", BigDecimal.class, false));
                break;

            case FilterAnalytic.GOODS:
                mth.addHeaderColumn(new MacHeaderColumn("Название", String.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Кол-во дебет", BigDecimal.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Кол-во кредит", BigDecimal.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Дебет", BigDecimal.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Кредит", BigDecimal.class, false));
                break;

            case FilterAnalytic.PARTNERS:
                mth.addHeaderColumn(new MacHeaderColumn("Название", String.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Дебет", BigDecimal.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Кредит", BigDecimal.class, false));
                break;

            case FilterAnalytic.EMPLOYEES:
                mth.addHeaderColumn(new MacHeaderColumn("Название", String.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Дебет", BigDecimal.class, false));
                mth.addHeaderColumn(new MacHeaderColumn("Кредит", BigDecimal.class, false));
                break;
        }

        return mth;
    }

    private String getTableModel(Map<String, AccountSum> map, MacTableModel macTable, String reportType) {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            MacTableRow row = new MacTableRow();
            String key = it.next();
            row.addCell(new MacTableCell(getSession(), key, false));
            if (reportType.equals(FilterAnalytic.SERVICE) || reportType.equals(FilterAnalytic.GOODS)) {
                row.addCell(new MacTableCell(getSession(), map.get(key).getDebetCount(), false));
                row.addCell(new MacTableCell(getSession(), map.get(key).getCreditCount(), false));
                row.addCell(new MacTableCell(getSession(), map.get(key).getDebetSum(), false));
                row.addCell(new MacTableCell(getSession(), map.get(key).getCreditSum(), false));
                rows.add(row);

            } else {
                row.addCell(new MacTableCell(getSession(), map.get(key).getDebetSum(), false));
                row.addCell(new MacTableCell(getSession(), map.get(key).getCreditSum(), false));
                rows.add(row);
            }
        }
        macTable.setData(rows);

        return macTable.getModel();
    }

    public String getModel() {
        return model;
    }
    
    public String getSession(){
        return sessionId;
    }
}
