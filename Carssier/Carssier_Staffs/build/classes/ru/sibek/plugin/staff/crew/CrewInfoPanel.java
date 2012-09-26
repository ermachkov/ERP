/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.staff.crew;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Crew;
import org.ubo.employee.Employee;
import org.ubo.goods.Goods;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.service.Service;
import org.uui.component.RightPanel;
import org.uui.table.*;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.core.CrewUsedInfo;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CrewInfoPanel extends RightPanel {

    private long selectedCrewId;
    private CarssierCore core;

    public CrewInfoPanel(String sessionId) {
        super(sessionId);
        core = CarssierCore.getInstance();
    }

    @Override
    public String getIdentificator() {
        return "ru.sibek.plugin.staff.crew.CrewInfoPanel";
    }

    @Override
    public String getModel() {
        Crew crew = core.getCrewById(selectedCrewId);
        if (crew == null) {
            return "<table width='100%' height='100%'><tr>"
                    + "<td style='font-size:80%;' valign='middle' align='center'>"
                    + "Для просмотра свойств щелкните на бригаде"
                    + "</td>"
                    + "</tr></table>";
        }

        CrewUsedInfo crewUsedInfo = core.isCrewUsed(selectedCrewId);
        if (!crewUsedInfo.isCrewUsed()) {
            return "<table width='100%' height='100%'><tr>"
                    + "<td style='font-size:80%;' valign='middle' align='center'>"
                    + "В бригаде <strong>"
                    + crewUsedInfo.getCrew().getName()
                    + "</strong> никто не зарегистрирован"
                    + "</td>"
                    + "</tr></table>";
        }

        String model = "<div style='overflow:auto; width:100%; height:100%;'>"
                + "<div style='font-size:70%; font-weight:bold; margin-left:5px;'>"
                + "Бригада: " + crew.getName() + "</div>";
        ArrayList<Employee> employees = crewUsedInfo.getEmployees();
        if (!employees.isEmpty()) {
            model += "<br/><div style='font-size:70%;margin-left:5px;'>В бригаде " 
                    + crew.getName() + " зарегистрировны работники:</div>";

//            ArrayList<Employee> employees = new ArrayList<>();
//            for (long employeeId : crew.getIdsEmployees()) {
//                Result result = core.getEmployeeById(employeeId);
//                if (result.isError()) {
//                    Logger.getGlobal().log(Level.WARNING, result.getReason());
//                }
//
//                employees.add((Employee) result.getObject());
//            }
//            Collections.sort(employees, new Comparator<Employee>() {
//
//                @Override
//                public int compare(Employee o1, Employee o2) {
//                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
//                }
//            });

            String empTable = "<table width='100%'cellpadding='0' cellspacing='0'>";
            for (Employee employee : employees) {
                empTable += "<tr>"
                        + "<td style='border-bottom: 1px solid gray;' width='64'>"
                        + "<img src='" + employee.getImageFileName() + "'>"
                        + "</td>"
                        + "<td valign='middle' style='padding-left:5px;"
                        + "border-bottom: 1px solid gray;font-size:70%;'>"
                        + employee.getName()
                        + "</td>"
                        + "</tr>";
            }
            empTable += "</table>";

            model += empTable;
        }

        if (!crewUsedInfo.getUsedServices().isEmpty()) {
            model += "<br/><div style='font-size:70%; margin-left:5px; font-weight:bold;'>Бригаде "
                    + crew.getName()
                    + " начисляется з/п за услуги:</div>";
            ArrayList<Service> services = crewUsedInfo.getUsedServices();
            Collections.sort(services, new Comparator<Service>() {

                @Override
                public int compare(Service o1, Service o2) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
            });

            model += getServiceTable(services).getModel();
        }

        if (!crewUsedInfo.getUsedGoodses().isEmpty()) {
            model += "<br/><div style='font-size:70%; margin-left:5px; font-weight:bold;'>"
                    + "Бригаде "
                    + crew.getName()
                    + " начисляется з/п за реализованные товары:</div>";
            ArrayList<Goods> goodses = crewUsedInfo.getUsedGoodses();
            Collections.sort(goodses, new Comparator<Goods>() {

                @Override
                public int compare(Goods o1, Goods o2) {
                    return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                }
            });

            model += getGoodsTable(goodses).getModel();
        }

        return model + "</div>";
    }

    private MacTableModel getServiceTable(ArrayList<Service> services) {
        MacTableModel macTableModel = new MacTableModel(getSession());
        macTableModel.setCssClass("");
        macTableModel.setCssClass("macTable");
        macTableModel.setId("serviceInfoTable");

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Наименование", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Вознаграждение", BigDecimal.class, false));
        macTableModel.setHeader(mth);

        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (Service service : services) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), service.getName(), false));
            Map<Long, BigDecimal> md = (Map<Long, BigDecimal>) service.getAdditionInfo().get("salaryDistribution");
            row.addCell(new MacTableCell(getSession(), md.get(selectedCrewId), false));
            rows.add(row);
        }

        macTableModel.setData(rows);

        return macTableModel;
    }

    private MacTableModel getGoodsTable(ArrayList<Goods> goodses) {
        MacTableModel macTableModel = new MacTableModel(getSession());
        macTableModel.setCssClass("");
        macTableModel.setCssClass("macTable");
        macTableModel.setId("serviceInfoTable");

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Наименование", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Вознаграждение", BigDecimal.class, false));
        macTableModel.setHeader(mth);

        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (Goods goods : goodses) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), goods.getName(), false));
            Map<Long, BigDecimal> md = (Map<Long, BigDecimal>) goods.getAdditionInfo().get("salaryDistribution");
            row.addCell(new MacTableCell(getSession(), md.get(selectedCrewId), false));
            rows.add(row);
        }

        macTableModel.setData(rows);

        return macTableModel;
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

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    selectedCrewId = jsonObject.getLong("dbid");
                    JSMediator.setRightPanel(getSession(), getModel());
                }

                if (jsonObject.getString("action").equals("updateRightPanel")) {
                            JSMediator.setRightPanel(getSession(), getModel());
                }
            }

            if (jsonObject.getString("eventType").equals("click")) {
                if (jsonObject.getString("action").equals("showRightPanel")) {
                            JSMediator.setRightPanel(getSession(), getModel());
                }
            }

        } catch (JSONException  ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    @Override
    public String getName() {
        return "Свойства";
    }
}
