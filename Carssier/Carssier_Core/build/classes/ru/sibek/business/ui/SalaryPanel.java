/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Crew;
import org.ubo.goods.Goods;
import org.ubo.json.JSONException;
import org.ubo.service.Service;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.Component;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import ru.sibek.business.core.CarssierCore;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class SalaryPanel extends Component {

    private Goods goods;
    private Service service;
    private CarssierCore core;
    private MacTableModel macTableSalary, macTableCrews;
    private Button btnAddCrew, btnBack, btnApply;
    private Map<Long, BigDecimal> distributionMap;

    public SalaryPanel(String sessionId) {
        super(sessionId);
        core = CarssierCore.getInstance();
        macTableSalary = new MacTableModel(sessionId, true);
        MacTableHeaderModel mthSalary = new MacTableHeaderModel();
        MacHeaderColumn col = new MacHeaderColumn("Бригада", String.class, false);
        col.setColumnWidth("60%");
        mthSalary.addHeaderColumn(col);
        mthSalary.addHeaderColumn(new MacHeaderColumn("Вознаграждение %", BigDecimal.class, false));
        macTableSalary.setHeader(mthSalary);
        macTableSalary.setCssClass("macTable");
        macTableSalary.setId("macTableSalary");
        macTableSalary.setMode(MacTableModel.MODE_EDIT);
        macTableSalary.getMacTableRemoveButton().addUIEventListener(getRemoveCrewListener());
        macTableSalary.addUIEventListener(getMacTableSalaryEvent());

        macTableCrews = new MacTableModel(sessionId, true);
        MacTableHeaderModel mthCrews = new MacTableHeaderModel();
        mthCrews.addHeaderColumn(new MacHeaderColumn("Бригада", String.class, false));
        macTableCrews.setHeader(mthCrews);
        macTableCrews.setCssClass("macTable");
        macTableCrews.setId("macTableCrews");
        macTableCrews.setMode(MacTableModel.MODE_EDIT);
        macTableCrews.setRemoveButtonEnable(false);

        btnAddCrew = new Button(getSession(), "Добавить бригаду");
        btnAddCrew.addUIEventListener(getAddCrewListener());

        btnBack = new Button(getSession(), "Назад");
        btnBack.addUIEventListener(getBackListener());

        btnApply = new Button(getSession(), "Применить");
        btnApply.addUIEventListener(getApplyListener());
    }

    public void setService(Service service) {
        this.service = service;
        init();
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
        init();
    }

    private UIEventListener getMacTableSalaryEvent() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    MacTableRow row = macTableSalary.getRow(evt.getJSONObject().getInt("row"));
                    if (evt.getJSONObject().has("value")) {
                        try {
                            BigDecimal val = new BigDecimal(evt.getJSONObject().getString("value").trim().replace(',', '.'));
                            distributionMap.put((long) row.getValue(), val);
                            
                        } catch (NumberFormatException e) {
                        }
                    }
                    
                } catch (JSONException ex) {
                    Logger.getLogger(SalaryPanel.class.getName()).log(Level.WARNING, null, ex);
                }

            }
        };

        return listener;
    }

    private UIEventListener getRemoveCrewListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                ArrayList<MacTableRow> rows = macTableSalary.getCheckedRows();
                for (MacTableRow row : rows) {
                    distributionMap.remove((long) row.getValue());
                }
                macTableSalary.removeCheckedRows();
                JSMediator.refreshElement(getSession(), "salaryPanel", getTableSalaryModel());
            }
        };

        return listener;
    }

    private UIEventListener getApplyListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                ArrayList<MacTableRow> rows = macTableCrews.getCheckedRows();
                for (MacTableRow row : rows) {
                    long crewId = (long) row.getValue();
                    distributionMap.put(crewId, BigDecimal.ZERO);
                }
                macTableCrews.removeCheckedRows();

                JSMediator.refreshElement(getSession(), "salaryPanel", getSelectCrewsPanel());
            }
        };

        return listener;
    }

    private UIEventListener getBackListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                JSMediator.refreshElement(getSession(), "salaryPanel", getTableSalaryModel());
            }
        };

        return listener;
    }

    private UIEventListener getAddCrewListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                JSMediator.refreshElement(getSession(), "salaryPanel", getSelectCrewsPanel());
            }
        };

        return listener;
    }

    private String getSelectCrewsPanel() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Crew> crews = core.getCrewsList();
        Iterator<Long> it = distributionMap.keySet().iterator();

        for (Crew crew : crews) {
            if (distributionMap.keySet().contains(crew.getId())) {
                continue;
            }
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), crew.getName(), false));
            row.setValue(crew.getId());
            rows.add(row);
        }
        macTableCrews.setData(rows);

        String model = ""
                + "<div style='width:100%;'>"
                + "<div style='float:left'>"
                + btnBack.getModel()
                + "</div>"
                + "<div style='float:right'>"
                + btnApply.getModel()
                + "</div>"
                + macTableCrews.getModel()
                + "</div>";

        return model;
    }

    private String getTableSalaryModel() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        Iterator<Long> it = distributionMap.keySet().iterator();
        while (it.hasNext()) {
            long key = it.next();
            Crew crew = core.getCrewById(key);
            if (crew == null) {
                continue;
            }

            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), crew.getName(), false));
            row.addCell(new MacTableCell(getSession(), distributionMap.get(key), true));
            row.setValue(crew.getId());
            rows.add(row);
        }
        macTableSalary.setData(rows);

        String model = ""
                + "<div style='width:100%;' align='right'>"
                + btnAddCrew.getModel()
                + "</div>"
                + macTableSalary.getModel();

        return model;
    }

    @Override
    public String getModel() {
        String model = ""
                + "<div style='width:100%;' id='salaryPanel'>"
                + getTableSalaryModel()
                + "</div>";

        return model;
    }

    private void init() {
        if (goods == null) {
            Map<String, Object> m = service.getAdditionInfo();
            if (m.containsKey("salaryDistribution")) {
                distributionMap = (Map<Long, BigDecimal>) m.get("salaryDistribution");

            } else {
                Map<Long, BigDecimal> md = new HashMap<>();
                Result r = core.getDefaultCrew();
                if (r.isError()) {
                    return;
                }

                md.put(((Crew) r.getObject()).getId(), new BigDecimal("33"));
                m.put("salaryDistribution", md);

                Crew c = core.getCrewByName("Кассиры");
                if (c != null) {
                    md.put(c.getId(), new BigDecimal("2.5"));
                    m.put("salaryDistribution", md);
                }

                service.setAdditionInfo(m);
                core.modifyService(service);
                distributionMap = md;
            }

        } else {
            Map<String, Object> m = goods.getAdditionInfo();
            if (m.containsKey("salaryDistribution")) {
                distributionMap = (Map<Long, BigDecimal>) m.get("salaryDistribution");

            } else {
                Map<Long, BigDecimal> md = new HashMap<>();
                Result r = core.getDefaultCrew();
                if (r.isError()) {
                    return;
                }

                md.put(((Crew) r.getObject()).getId(), new BigDecimal("33"));
                m.put("salaryDistribution", md);

                Crew c = core.getCrewByName("Кассиры");
                if (c != null) {
                    md.put(c.getId(), new BigDecimal("2.5"));
                    m.put("salaryDistribution", md);
                }

                goods.setAdditionInfo(m);
                core.modifyGoods(goods);
                distributionMap = md;
            }
        }
    }

    public Map<Long, BigDecimal> getSalaryDistribution() {
        return distributionMap;
    }
}
