/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.accountbook.trialbalance;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Crew;
import org.ubo.employee.Employee;
import org.ubo.goods.Goods;
import org.ubo.partner.Agent;
import org.ubo.service.Service;
import org.uui.component.Button;
import org.uui.component.Component;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class FilterAnalyticSelector extends Component {

    private Button btnBack;
    private MacTableModel macTableModel;
    private CarssierCore core = CarssierCore.getInstance();
    private int selectMode = FilterAnalytic.SELECT_NOTHING;

    public FilterAnalyticSelector(String sessionId) {
        super(sessionId);
        btnBack = new Button(getSession(), "<< Назад");
        btnBack.addUIEventListener(getBackEvent());

        macTableModel = new MacTableModel(sessionId, true);
        macTableModel.setMode(MacTableModel.MODE_EDIT);
        macTableModel.setCssClass("macTable");
        macTableModel.setId("tableFilterAnalyticSelector");
        macTableModel.setNavigatorShowingAlways(true);
        macTableModel.setRemoveButtonEnable(false);
        macTableModel.setEditButonEnabled(false);
        macTableModel.setRowCheckedButonEnabled(true);

        macTableModel.getMacTableAllRowCheckedButton().addUIEventListener(getAllRowCheckedEvent());
        macTableModel.getMacTableAllRowUncheckedButton().addUIEventListener(getAllRowUncheckedEvent());

        macTableModel.getMacTableNavigator().setRefreshButtonVisible(false);
        macTableModel.addNavigatorChangeListener(new NavigatorChangeListener() {
            @Override
            public void event(int event) {
                switch (event) {
                    case MacTableNavigator.FILTER:
                        //setMacTableFilterVisible(true);
                        break;

                    case MacTableNavigator.REFRESH:
                        //
                        break;

                    case MacTableNavigator.CALENDAR:
                        break;

                    default:
                        try {
                            JSMediator.showLockPanel(getSession());
                            refreshMacTable();
                            JSMediator.hideLockPanel(getSession());
                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.WARNING, null, e);
                        }
                }
            }
        });
    }

    private void refreshMacTable() {
        JSMediator.refreshElement(getSession(), "macTablePanel", macTableModel.getModel());
    }

    private UIEventListener getAllRowCheckedEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                macTableModel.setAllRowsChecked(true);
                selectMode = FilterAnalytic.SELECT_ALL;
                refreshMacTable();
            }
        };

        return listener;
    }

    private UIEventListener getAllRowUncheckedEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                macTableModel.setAllRowsChecked(false);
                selectMode = FilterAnalytic.SELECT_NOTHING;
                refreshMacTable();
            }
        };

        return listener;
    }

    private UIEventListener getBackEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                if (macTableModel.getRows().size() != macTableModel.getCheckedRows().size()
                        && macTableModel.getCheckedRows().size() > 0) {
                    selectMode = FilterAnalytic.SELECT_CHUNK;
                }

                backAction(selectMode, getSelectedIdList());
            }
        };

        return listener;
    }

    private ArrayList<Long> getSelectedIdList() {
        ArrayList<Long> list = new ArrayList<>();
        if (macTableModel.getRows().size() != macTableModel.getCheckedRows().size()
                && macTableModel.getCheckedRows().size() > 0) {
            for (MacTableRow row : macTableModel.getCheckedRows()) {
                list.add((long) row.getValue());
            }
        }
        return list;
    }

    public abstract void backAction(int selectedMode, ArrayList<Long> selectedId);

    public void setType(String type) {
        switch (type) {
            case FilterAnalytic.SERVICE:
                MacTableHeaderModel mth = new MacTableHeaderModel();
                mth.addHeaderColumn(new MacHeaderColumn("Услуга", String.class, false));
                macTableModel.setHeader(mth);
                setServiceTable();
                break;

            case FilterAnalytic.GOODS:
                mth = new MacTableHeaderModel();
                mth.addHeaderColumn(new MacHeaderColumn("Товар", String.class, false));
                macTableModel.setHeader(mth);
                setGoodsTable();
                break;

            case FilterAnalytic.CREWS:
                mth = new MacTableHeaderModel();
                mth.addHeaderColumn(new MacHeaderColumn("Бригада", String.class, false));
                macTableModel.setHeader(mth);
                setCrewsTable();
                break;

            case FilterAnalytic.EMPLOYEES:
                mth = new MacTableHeaderModel();
                mth.addHeaderColumn(new MacHeaderColumn("Работники", String.class, false));
                macTableModel.setHeader(mth);
                setEmployeesTable();
                break;

            case FilterAnalytic.PARTNERS:
                mth = new MacTableHeaderModel();
                mth.addHeaderColumn(new MacHeaderColumn("Партнер", String.class, false));
                macTableModel.setHeader(mth);
                setPartnersTable();
                break;
        }
    }

    private void setPartnersTable() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Agent> list = core.getAgentsList();
        for (Agent agent : list) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), agent.getName(), false));
            row.setValue(agent.getId());
            rows.add(row);
        }

        macTableModel.setData(rows);
    }

    private void setEmployeesTable() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Employee> list = core.getEmployeeList();
        for (Employee employee : list) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), employee.getName(), false));
            row.setValue(employee.getId());
            rows.add(row);
        }

        macTableModel.setData(rows);
    }

    private void setCrewsTable() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Crew> list = core.getCrewsList();
        for (Crew crew : list) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), crew.getName(), false));
            row.setValue(crew.getId());
            rows.add(row);
        }

        macTableModel.setData(rows);
    }

    private void setGoodsTable() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Goods> list = core.getAllGoods();
        for (Goods goods : list) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), goods.getName(), false));
            row.setValue(goods.getId());
            rows.add(row);
        }

        macTableModel.setData(rows);
    }

    private void setServiceTable() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Service> list = core.getAllService();
        for (Service service : list) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), service.getName(), false));
            row.setValue(service.getId());
            rows.add(row);
        }

        macTableModel.setData(rows);
    }

    public void setSelectedItems(ArrayList<Long> selectedId) {
        int index = 0;
        for (MacTableRow row : macTableModel.getRows()) {
            if (selectedId.contains((long) row.getValue())) {
                macTableModel.setRowChecked(index);
            }

            index++;
        }

        refreshMacTable();
    }

    public void setSelectedAllItems(boolean isChecked) {
        macTableModel.setAllRowsChecked(isChecked);
    }

    @Override
    public String getModel() {
        String model = ""
                + "<div style='width:100%; border-bottom: 1px solid darkgrey; height:30px;'>"
                + "<div style='float:left;'>" + btnBack.getModel() + "</div>"
                + "</div>"
                + "<div style='width:100%;' id='macTablePanel'>"
                + macTableModel.getModel()
                + "</div>";

        return model;
    }
}
