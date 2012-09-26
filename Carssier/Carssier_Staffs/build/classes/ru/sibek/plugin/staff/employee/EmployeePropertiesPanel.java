/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.staff.employee;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.employee.Crew;
import org.ubo.employee.Employee;
import org.ubo.partner.Address;
import org.ubo.partner.Contacts;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.ComboBox;
import org.uui.component.PropertiesPanel;
import org.uui.component.TextField;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class EmployeePropertiesPanel extends PropertiesPanel {

    private Employee employee;
    private TextField txtShortName, txtFullName, txtInn, txtPfr,
            txtPassportSerialNumber, txtPassportNumber, txtPassportIssued,
            txtCountry, txtCity, txtStreet, txtHouse, txtApartment, txtPhone,
            txtEmail, txtMSV;
    private ComboBox cboCrews, cboRoles;
    private CarssierCore core = CarssierCore.getInstance();
    private MacTableModel macTableModel, macTableModelCrewSelector;
    private Button btnAddCrew, btnApplyCrew, btnApplyCrewBack;

    public EmployeePropertiesPanel(String sessionId, DataBase db) {
        super(sessionId, db);

        txtShortName = new TextField(getSession(), "");
        txtShortName.setStyle("width:96%;");

        txtFullName = new TextField(getSession(), "");
        txtFullName.setStyle("width:96%;");

        txtInn = new TextField(getSession(), "");
        txtInn.setStyle("width:98%;");

        txtPfr = new TextField(getSession(), "");
        txtPfr.setStyle("width:98%;");

        txtPassportSerialNumber = new TextField(getSession(), "");
        txtPassportSerialNumber.setStyle("width:98%;");

        txtPassportNumber = new TextField(getSession(), "");
        txtPassportNumber.setStyle("width:98%;");

        txtPassportIssued = new TextField(getSession(), "");
        txtPassportIssued.setStyle("width:98%;");

        txtCountry = new TextField(getSession(), "");
        txtCountry.setStyle("width:98%;");

        txtCity = new TextField(getSession(), "");
        txtCity.setStyle("width:98%;");

        txtStreet = new TextField(getSession(), "");
        txtStreet.setStyle("width:98%;");

        txtHouse = new TextField(getSession(), "");
        txtHouse.setStyle("width:98%;");

        txtApartment = new TextField(getSession(), "");
        txtApartment.setStyle("width:98%;");

        txtPhone = new TextField(getSession(), "");
        txtPhone.setStyle("width:98%;");

        txtEmail = new TextField(getSession(), "");
        txtEmail.setStyle("width:98%;");

        txtMSV = new TextField(getSession(), "0");
        txtMSV.setStyle("width:98%;");

        cboCrews = new ComboBox(sessionId, new LinkedHashMap<String, String>());
        cboCrews.setStyle("width:98%");

        btnApply.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                modifyEmployee();
            }
        });

        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("Работник", "employee");
        m.put("Бригадир", "foreman");
        m.put("Бригадир/Кассир", "foreman/cashier");
        m.put("Кассир", "cashier");
        m.put("Бухгалтер", "accountant");
        m.put("Менеджер", "manager");
        m.put("Владелец", "owner");
        cboRoles = new ComboBox(sessionId, m);

        macTableModel = new MacTableModel(sessionId, true);
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Бригада", String.class, false));
        macTableModel.setHeader(mth);
        macTableModel.setCssClass("macTable");
        macTableModel.setId("employeesCrewsTable");
        macTableModel.setMode(MacTableModel.MODE_EDIT);
        macTableModel.getMacTableRemoveButton().addUIEventListener(getRemoveButtonListener());

        macTableModelCrewSelector = new MacTableModel(sessionId, true);
        macTableModelCrewSelector.setHeader(mth);
        macTableModelCrewSelector.setCssClass("macTable");
        macTableModelCrewSelector.setId("employeesCrewsSelectorTable");
        macTableModelCrewSelector.setMode(MacTableModel.MODE_EDIT);
        macTableModelCrewSelector.setRemoveButtonEnable(false);
        macTableModelCrewSelector.setEditButonEnabled(false);

        btnAddCrew = new Button(getSession(), "Добавить бригаду");
        btnAddCrew.addUIEventListener(getAddCrewEventListener());

        btnApplyCrew = new Button(getSession(), "Применить");
        btnApplyCrew.addUIEventListener(getApplyCrewEventListener());

        btnApplyCrewBack = new Button(getSession(), "Назад");
        btnApplyCrewBack.addUIEventListener(getApplyCrewBackEventListener());
    }
    
    @Override
    public void setSession(String session) {
        super.setSession(session);
    }

    private UIEventListener getRemoveButtonListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                ArrayList<MacTableRow> rows = macTableModel.getCheckedRows();
                Object obj = employee.getAdditionInfoByKey("crewList");
                if (obj != null) {
                    ArrayList<Long> crewsId = (ArrayList<Long>) obj;
                    for (MacTableRow row : rows) {
                        crewsId.remove((Long) row.getValue());
                    }

                    employee.setAdditionInfo("crewList", crewsId);
                }
                macTableModel.removeCheckedRows();

                JSMediator.refreshElement(getSession(), "crewsPanel", getCrewTable());
            }
        };

        return listener;
    }

    private UIEventListener getApplyCrewBackEventListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                    JSMediator.setSliderPanel(getSession(), "crewsPanel", getCrewTable(), "left");
            }
        };
        return listener;
    }

    private UIEventListener getApplyCrewEventListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                ArrayList<MacTableRow> rows = macTableModelCrewSelector.getCheckedRows();
                ArrayList<Long> crewsIds = new ArrayList<>();
                for (MacTableRow row : rows) {
                    crewsIds.add((Long) row.getValue());
                }

                ArrayList<Long> resultList = new ArrayList<>();
                Object obj = employee.getAdditionInfoByKey("crewList");
                if (obj != null) {
                    ArrayList<Long> list = (ArrayList<Long>) obj;
                    for (long id : list) {
                        resultList.add(id);
                    }
                }

                for (long id : crewsIds) {
                    if (!resultList.contains(id)) {
                        resultList.add(id);
                    }
                }

                employee.setAdditionInfo("crewList", resultList);

                JSMediator.refreshElement(getSession(), "crewsPanel", getSelectCrewsPanel());
            }
        };

        return listener;
    }

    private UIEventListener getAddCrewEventListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                JSMediator.setSliderPanel(getSession(), "crewsPanel", getSelectCrewsPanel(), "right");
            }
        };

        return listener;
    }

    private String getSelectCrewsPanel() {
        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<Crew> crews = core.getCrewsList();
        ArrayList<Long> employeeCrews = new ArrayList<>();
        Object obj = employee.getAdditionInfoByKey("crewList");
        if (obj != null) {
            employeeCrews = (ArrayList<Long>) obj;
        }

        for (Crew crew : crews) {
            boolean isContinue = false;
            for (long id : employeeCrews) {
                if (id == crew.getId()) {
                    isContinue = true;
                }
            }

            if (isContinue) {
                continue;
            }

            if (employee.getDefaultCrew() != null) {
                if (employee.getDefaultCrew().getId() == crew.getId()) {
                    continue;
                }
            }

            MacTableRow row = new MacTableRow();
            row.setValue(crew.getId());
            row.addCell(new MacTableCell(getSession(), crew.getName(), false));
            rows.add(row);
        }
        macTableModelCrewSelector.removeCheckedRows();
        macTableModelCrewSelector.setData(rows);

        String selectModel = ""
                + "<div style='width:100%;'>"
                + "<div style='float:left;'>" + btnApplyCrewBack.getModel() + "</div>"
                + "<div style='float:right;'>" + btnApplyCrew.getModel() + "</div>"
                + "</div>"
                + macTableModelCrewSelector.getModel();

        return selectModel;
    }

    private String getCrewsTablePanel() {
        String header = ""
                + "<div style='width:100%;'>"
                + "<div style='width:100%;'>Также состоит в бригадах:</div>"
                + "</div>";

        return "<div style='width:100%'>"
                + header
                + "<div id='crewsPanel'>"
                + getCrewTable()
                + "</div>"
                + "<div>";
    }

    private String getCrewTable() {
        String tableModel;
        ArrayList<MacTableRow> rows = new ArrayList<>();
        Object obj = employee.getAdditionInfoByKey("crewList");
        if (obj != null) {
            WebKitEventBridge.getInstance().cleanMacTable(getSession(), macTableModel);
            ArrayList<Long> crewIds = (ArrayList<Long>) obj;
            for (long id : crewIds) {
                MacTableRow row = new MacTableRow();
                Crew c = core.getCrewById(id);
                if (c == null) {
                    continue;
                }
                row.setValue(id);
                row.addCell(new MacTableCell(getSession(), c.getName(), false));
                rows.add(row);
            }

            macTableModel.setData(rows);
        }

        tableModel = ""
                + "<div style='width:100%;' align='right'>"
                + btnAddCrew.getModel()
                + "</div>"
                + macTableModel.getModel();

        return tableModel;
    }

    private void modifyEmployee() {
        try {
            employee.setShortName(txtShortName.getText());
            employee.setFullName(txtFullName.getText());
            employee.setINN(txtInn.getText());
            employee.setPFR(txtPfr.getText());

            Address address = new Address();
            address.setCountry(txtCountry.getText());
            address.setCity(txtCity.getText());
            address.setStreet(txtStreet.getText());
            address.setHouse(txtHouse.getText());
            address.setApartment(txtApartment.getText());
            employee.setDefaultAddress(address);

            Contacts contact = new Contacts();
            contact.setDefaultEmail(txtEmail.getText());
            contact.setDefaultPhone(txtPhone.getText());
            employee.setDefaultContacts(contact);

            employee.setRole(cboRoles.getSelectedValue());
            try {
                BigDecimal msv = new BigDecimal(txtMSV.getText().replaceAll(",", ".").trim());
                employee.setMsv(msv);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, txtMSV.getText(), e);
            }

            try {
                Crew crew = core.getCrewById(Long.parseLong(cboCrews.getSelectedValue().trim()));
                if (crew != null) {
                    employee.setDefaultCrew(crew);
                }
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "" + cboCrews, e);
            }


            Result r = core.modifyEmployee(employee);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
            } else {
                String json = "{eventType:push, session:"+getSession()+", action:showWorkPanel}";
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        EmployeePanel.class.getName(), json);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        txtShortName.setText(employee.getShortName());
        txtFullName.setText(employee.getFullName());
        txtInn.setText(employee.getINN());
        txtPfr.setText(employee.getPFR());
        txtMSV.setText(employee.getMsv().toString());

        Address address = employee.getDefaultAddress();
        if (address != null) {
            txtCountry.setText(Objects.toString(address.getCountry(), ""));
            txtCity.setText(Objects.toString(address.getCity(), ""));
            txtStreet.setText(Objects.toString(address.getStreet(), ""));
            txtHouse.setText(Objects.toString(address.getHouse(), ""));
            txtApartment.setText(Objects.toString(address.getApartment(), ""));
        }

        Contacts contact = employee.getDefaultContacts();
        if (contact != null) {
            txtPhone.setText(Objects.toString(contact.getDefaultPhone(), ""));
            txtEmail.setText(Objects.toString(contact.getDefaultEmail(), ""));
        }

        ArrayList<Crew> list = CarssierCore.getInstance().getCrewsList();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        int index = 0, selectedIndex = -1;
        for (Crew crew : list) {
            map.put(crew.getName(), "" + crew.getId());
            if (employee.getDefaultCrew().getId() == crew.getId()) {
                selectedIndex = index;
            }
            index++;
        }

        cboCrews.setItems(map);
        cboCrews.setSelectedIndex(selectedIndex);

        macTableModel.setData(new ArrayList<MacTableRow>());
        macTableModelCrewSelector.setData(new ArrayList<MacTableRow>());
    }

    @Override
    public String getModel() {
        String _model;
        if (employee == null) {
            _model = "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<div style='width:50%;height:50%;border-radius:18px;border-color:gray;"
                    + "border-style:dotted;border-width:3px;' align='center'>"
                    + "<table width='100%' height='100%'><tr><td align='center' valign='middle'>"
                    + "Для просмотра и редактирования свойств просто бросайте сюда персону"
                    + "</td></tr></table>"
                    + "</div>"
                    + "<td>"
                    + "</tr>"
                    + "</table>";
        } else {
            String imageFileName = Paths.get("img", "icons", "user64.png").toString();
            if (!employee.getImageFileName().equals("")) {
                imageFileName = employee.getImageFileName();
            }

            cboRoles.setSelectedValue(employee.getRole());

            _model = "<div style='width:98%;height:99%;overflow:auto;padding:5px;font-size:80%'>"
                    + "<div><strong>Роль:</strong></div>"
                    + "<div>" + cboRoles.getModel() + "</div>"
                    + "<div><strong>Наименование кратко:</strong></div>"
                    + "<div>" + txtShortName.getModel() + "</div>"
                    + "<div><strong>Наименование полностью:</strong></div>"
                    + "<div>" + txtFullName.getModel() + "</div>"
                    + "<div><strong>МРОТ:</strong></div>"
                    + "<div>" + txtMSV.getModel() + "</div>"
                    + "<hr/>"
                    + "<div>"
                    + "<div style='width:19%; float:left;' align='center'>"
                    + "<img src='" + imageFileName + "'/>"
                    + "</div>"
                    + "<div style='width:76%;float:left;border-radius:8px;"
                    + "border-color:gray;border-style:dotted;border-width:1px"
                    + ";background-color: #CACACA;' align='center'>"
                    + "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='left' valign='middle'>"
                    + "<strong>ИНН</strong><br/>"
                    + txtInn.getModel() + "<br/>"
                    + "<strong>ПФР</strong><br/>"
                    + txtPfr.getModel() + "<br/>"
                    + "<strong>Бригада (основная):</strong><br/>"
                    + cboCrews.getModel() + "<br/>"
                    + getCrewsTablePanel()
                    + "<hr/>"
                    + "<b>Паспорт:</b><br/>"
                    + "Серия<br/>"
                    + txtPassportSerialNumber.getModel() + "<br/>"
                    + "Номер<br>"
                    + txtPassportSerialNumber.getModel() + "<br/>"
                    + "Кем и когда выдан:<br/>"
                    + txtPassportIssued.getModel() + "<br/>"
                    + "<b>Адрес места жительства:</b><br/>"
                    + "Страна<br/>"
                    + txtCountry.getModel() + "<br/>"
                    + "Город:<br/>"
                    + txtCity.getModel() + "<br/>"
                    + "Улица<br/>"
                    + txtStreet.getModel() + "<br/>"
                    + "Дом<br/>"
                    + txtHouse.getModel() + "<br/>"
                    + "Квартира<br/>"
                    + txtApartment.getModel() + "<br/>"
                    + "<hr/>"
                    + "<b>Контакты:</b><br/>"
                    + "Телефон<br/>"
                    + txtPhone.getModel() + "<br/>"
                    + "E-mail<br/>"
                    + txtEmail.getModel() + "<br/>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>"
                    + "</div>"
                    + "<div style='padding:10px;'>" + btnApply.getModel() + "</div>"
                    + "</div>";
        }

        return _model;
    }
}
