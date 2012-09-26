/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.accountbook.trialbalance;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.accountbook.AnalyticFilter;
import org.ubo.accountbook.AnalyticFilterItem;
import org.ubo.accountbook.SyntheticAccount;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.RightPanel;
import org.uui.component.TextField;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.core.ui.Callback;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class TrialBalanceRightPanel extends RightPanel {

    private CarssierCore core = CarssierCore.getInstance();
    private Button btnSetFilters, btnApplyFilter, btnSaveFilter, btnDeleteFilter;
    private TextField txtFilterName;
    private final FilterAnalyticPanel filterAnalyticPanel;
    private ArrayList<FilterAnalytic> selectedFilters = new ArrayList<>();
    private MacTableModel macTableFilters;
    private String account = "";
    private AnalyticFilter selectedAnalyticFilter;
    private ArrayList<SyntheticAccount> syntheticAccounts;
    private ReportBulder reportBuilder;
    private int workPanelHeight = 0;

    public TrialBalanceRightPanel(String sessionId) {
        super(sessionId);
        
        btnSetFilters = new Button(getSession(), "Создать/Редактировать");
        btnSetFilters.addUIEventListener(getSetFilterEvent());

        btnApplyFilter = new Button(getSession(), "Применить");
        btnApplyFilter.addUIEventListener(getApplyFilterEvent());

        btnSaveFilter = new Button(getSession(), "Сохранить фильтр");
        btnSaveFilter.addUIEventListener(getSaveFilterEvent());

        btnDeleteFilter = new Button(getSession(), "Удалить фильтр");
        btnDeleteFilter.addUIEventListener(getDeleteFilterEvent());

        filterAnalyticPanel = new FilterAnalyticPanel(sessionId) {
            @Override
            public void backAction(ArrayList<FilterAnalytic> filters) {
                selectedFilters = filters;
                JSMediator.setSliderPanel(getSession(), "trialBalanceAnalytic", getBasicModel(), "left");
            }
        };

        txtFilterName = new TextField(sessionId);
        txtFilterName.setStyle("width:200px;");

        macTableFilters = new MacTableModel(sessionId, true);
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Название", String.class, false));
        macTableFilters.setHeader(mth);
        macTableFilters.setCssClass("macTable");
        macTableFilters.setId("macTableFilters");
        macTableFilters.addUIEventListener(getMacTableFilterListener());

        reportBuilder = new ReportBulder(sessionId);
    }

    private UIEventListener getApplyFilterEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                reportBuilder.buildReport(syntheticAccounts, selectedAnalyticFilter);
                try {
                    JSMediator.refreshElement(getSession(), "reportPanel", reportBuilder.getModel());

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    private UIEventListener getMacTableFilterListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    int row = evt.getJSONObject().getInt("row");
                    selectedAnalyticFilter = (AnalyticFilter) macTableFilters.getRows().get(row).getValue();
                    TrialBalanceRightPanel.this.filterAnalyticPanel.setAnalyticFilter(selectedAnalyticFilter);
                    txtFilterName.setText(selectedAnalyticFilter.getName());
                    JSMediator.refreshElement(getSession(), "trialBalanceAnalytic", getBasicModel());

                } catch (JSONException e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    private UIEventListener getDeleteFilterEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                if (selectedAnalyticFilter != null) {
                    Result r = core.removeAnalyticFilter(selectedAnalyticFilter);
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());

                    } else {
                        selectedAnalyticFilter = null;
                        selectedFilters.clear();

                        refreshMacTableFilters();
                        txtFilterName.setText("");
                        TrialBalanceRightPanel.this.filterAnalyticPanel.clear();
                        JSMediator.setRightPanel(getSession(), getModel(), 0);
                    }
                }
            }
        };

        return listener;
    }

    private UIEventListener getSaveFilterEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                ArrayList<AnalyticFilterItem> items = new ArrayList<>();
                for (FilterAnalytic filterAnalytic : selectedFilters) {
                    AnalyticFilterItem analyticFilterItem = new AnalyticFilterItem();
                    analyticFilterItem.setFilterType(filterAnalytic.getFilterType());
                    analyticFilterItem.setSelectedMode(filterAnalytic.getSelectMode());
                    if (filterAnalytic.getSelectMode() == FilterAnalytic.SELECT_CHUNK) {
                        analyticFilterItem.setSelectedId(filterAnalytic.getSelectedId());
                    }

                    items.add(analyticFilterItem);
                }

                AnalyticFilter analyticFilter = new AnalyticFilter();
                analyticFilter.setAnalyticFilterItems(items);
                analyticFilter.setName(txtFilterName.getText().trim());
                analyticFilter.setAccount(account);

                Result r;
                if (selectedAnalyticFilter == null) {
                    r = core.addAnalyticFilter(analyticFilter);

                } else {
                    if (selectedAnalyticFilter.getName().equals(analyticFilter.getName())) {
                        analyticFilter.setId(selectedAnalyticFilter.getId());
                        r = core.modifyAnalyticFilter(selectedAnalyticFilter.getId(), analyticFilter);

                    } else {
                        r = core.addAnalyticFilter(analyticFilter);
                    }
                }

                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());

                } else {
                    txtFilterName.setText("");
                    selectedFilters.clear();
                    selectedAnalyticFilter = null;

                    refreshMacTableFilters();
                    JSMediator.setRightPanel(getSession(), getModel());
                }

            }
        };

        return listener;
    }

    private void refreshMacTableFiltersPanel() {
        refreshMacTableFilters();
        try {
            JSMediator.refreshElement(getSession(), "macTableFiltersPanel", macTableFilters.getModel());
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private UIEventListener getSetFilterEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSMediator.setSliderPanel(
                            getSession(),
                            "trialBalanceAnalytic",
                            TrialBalanceRightPanel.this.filterAnalyticPanel.getModel(),
                            "right");
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    @Override
    public String getName() {
        return "Аналитика";
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
            filterAnalyticPanel.setSession(jsonObject.getString("session"));

            Callback callback = new Callback(getSession()) {
                @Override
                public void callback(String json) {
                    try {
                        Object val = new JSONObject(json).get("value");
                        if (val == null) {
                            return;
                        }

                        String s = "" + val;
                        workPanelHeight = Integer.parseInt(s.replaceAll("px", ""));
                    } catch (JSONException | NumberFormatException e) {
                        Logger.getGlobal().log(Level.WARNING, json, e);
                    }

                }
            };

            callback.request("getUICore().getElementCssAttributeValue('workPanel', 'height')");

            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    JSMediator.setRightPanel(getSession(), getModel(), 0);
                }

                if (jsonObject.getString("action").equals("hideRightPanel")) {
                    WebKitEventBridge.getInstance().pushEventToComponent(
                            getSession(),
                            TrialBalancePanel.class.getName(),
                            "{eventType:push, session:" + getSession() + ", action:showSelector}");
                }
            }
        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
        }
    }

    @Override
    public String getIdentificator() {
        return TrialBalanceRightPanel.class.getName();
    }

    private void refreshMacTableFilters() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (AnalyticFilter analyticFilter : core.getAnalyticFilters(account)) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), analyticFilter.getName(), false));
            row.setValue(analyticFilter);
            rows.add(row);
        }

        macTableFilters.setData(rows);
    }

    @Override
    public String getModel() {
        refreshMacTableFilters();

        return ""
                + "<div identificator='" + getIdentificator() + "' id='trialBalanceAnalytic'>"
                + getBasicModel()
                + "</div>";
    }

    public String getBasicModel() {
        btnSetFilters.setText("Создать/Редактировать");
        String saveFilterButtonModel = "", filterNameModel = "", deleteFilterModel = "";
        if (selectedAnalyticFilter != null) {
            saveFilterButtonModel = btnSaveFilter.getModel();
            filterNameModel = txtFilterName.getModel();
            deleteFilterModel = btnDeleteFilter.getModel();

        } else if (!selectedFilters.isEmpty()) {
            saveFilterButtonModel = btnSaveFilter.getModel();
            filterNameModel = txtFilterName.getModel();
        }

        String model = ""
                + "<div style='width:100%; border-bottom: 1px solid darkgrey; height:30px;'>"
                + "<div style='float:right;'>"
                + filterNameModel
                + saveFilterButtonModel
                + deleteFilterModel
                + btnSetFilters.getModel()
                + "</div>"
                + "</div>"
                + "<div style='width:100%; height:100%;'>"
                + "<div style='width:30%; float:left; "
                + "border-right: 1px solid darkgray;'>"
                + "<div style='width:100%; " + (workPanelHeight - 60) + "px;"
                + " overflow: auto;' id='macTableFiltersPanel'>"
                + macTableFilters.getModel()
                + "</div>"
                + "</div>"
                + "<div style='width:68%; height:100%; float:left;'>"
                + "<div>"
                + btnApplyFilter.getModel()
                + "</div>"
                + "<div style='width:100%;" + (workPanelHeight - 60) + "px; overflow: auto;' id='reportPanel'>"
                + reportBuilder.getModel()
                + "</div>"
                + "</div>"
                + "</div>";
        return model;
    }

//    private String getHeight(String elementId, int k) {
//        sCallback = null;
//
//        Callback callback = new Callback(getSession()) {
//            @Override
//            public void callback(String json) {
//                sCallback = json;
//            }
//        };
//
//        callback.request("getUICore().getElementCssAttributeValue('" + elementId + "', 'height')");
//        int count = 0;
//        while (sCallback == null) {
//            LockSupport.parkNanos(100000000);
//            if (count > 10) {
//                break;
//            }
//
//            count++;
//        }
//
//        try {
//            Object val = new JSONObject(sCallback).get("value");
//            if (val == null) {
//                return "";
//            }
//
//            String s = "" + val;
//            int i = Integer.parseInt(s.replaceAll("px", "")) + k;
//            return "height:" + i + "px;";
//
//        } catch (JSONException | NumberFormatException e) {
//            Logger.getGlobal().log(Level.WARNING, sCallback, e);
//            return "";
//        }
//    }
    public void setSyntheticAccounts(ArrayList<SyntheticAccount> syntheticAccounts) {
        this.syntheticAccounts = syntheticAccounts;
    }
}
