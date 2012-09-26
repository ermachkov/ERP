package ru.sibek.plugin.agents;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.partner.Account;
import org.ubo.partner.Address;
import org.ubo.partner.Agent;
import org.ubo.partner.Contacts;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.CheckBox;
import org.uui.component.RightPanel;
import org.uui.component.TextField;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.database.CarssierDataBase;

public class AgentsPropertiesPanel extends RightPanel {

    private Agent selectedAgent;
    private DataBase dataBase;
    private TextField txtShortName, txtFullName, txtINN, txtCountry, txtZip, txtCity,
            txtStreet, txtHouse, txtApartment, txtPhone, txtEmail, txtURL,
            txtContactPerson, txtBank, txtAccount, txtKs, txtBik, txtInn;
    private Button btnApply;
    private CheckBox chkBox;
    private CarssierCore core = CarssierCore.getInstance();

    public AgentsPropertiesPanel(String sessionId) {
        super(sessionId);
        dataBase = CarssierDataBase.getDataBase();

        txtZip = new TextField(getSession(), "");
        txtZip.setStyle("width:98%;");

        txtShortName = new TextField(getSession(), "");
        txtShortName.setStyle("width:98%;");

        txtFullName = new TextField(getSession(), "");
        txtFullName.setStyle("width:98%;");

        txtINN = new TextField(getSession(), "");
        txtINN.setStyle("width:98%;");

        chkBox = new CheckBox(getSession(), "Использовать как поставщика по умолчанию", "isDefaultSupplier", "defaultSupplier");
        chkBox.setStyleLabel("font-size:80%");

        btnApply = new Button(getSession(), "Применить");
        btnApply.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    updateAgent();
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        });
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

        txtURL = new TextField(getSession(), "");
        txtURL.setStyle("width:98%;");

        txtContactPerson = new TextField(getSession(), "");
        txtContactPerson.setStyle("width:98%;");

        // Bank
        txtBank = new TextField(getSession(), "");
        txtBank.setStyle("width:98%;");

        txtAccount = new TextField(getSession(), "");
        txtAccount.setStyle("width:98%;");

        txtKs = new TextField(getSession(), "");
        txtKs.setStyle("width:98%;");

        txtBik = new TextField(getSession(), "");
        txtBik.setStyle("width:98%;");

        txtInn = new TextField(getSession(), "");
        txtInn.setStyle("width:98%;");
    }

    private void updateAgent() {
        Address address = selectedAgent.getDefaultAddress();
        address = address == null ? new Address() : address;
        address.setZip(txtZip.getText());
        address.setCountry(txtCountry.getText());
        address.setCity(txtCity.getText());
        address.setStreet(txtStreet.getText());
        address.setHouse(txtHouse.getText());
        address.setApartment(txtApartment.getText());

        Contacts contact = selectedAgent.getDefaultContacts();
        contact = contact == null ? new Contacts() : contact;
        contact.setDefaultEmail(txtEmail.getText());
        contact.setDefaultPhone(txtPhone.getText());
        contact.setDefaultURL(txtURL.getText());
        contact.setDefaultContactPerson(txtContactPerson.getText());

        Account account = selectedAgent.getDefaultAccount();
        account = account == null ? new Account() : account;
        account.setBank(txtBank.getText());
        account.setAccount(txtAccount.getText());
        account.setKs(txtKs.getText());
        account.setInn(txtInn.getText());
        account.setBik(txtBik.getText());

        Result r = core.modifyAgent(selectedAgent, txtShortName.getText(),
                txtFullName.getText(), txtINN.getText(),
                chkBox.isChecked(), address, contact, account);

        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());

        } else {
            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                    AgentsPanel.class.getName(),
                    "{eventType:push, session:\"" + getSession() + "\", action:showWorkPanel}");
        }
    }

    @Override
    public String getName() {
        return "Свойства";
    }

    private void setPanel() {
        txtShortName.setText(selectedAgent.getShortName());
        txtFullName.setText(selectedAgent.getFullName());
        txtINN.setText(selectedAgent.getINN());

        Address address = selectedAgent.getDefaultAddress();
        if (address != null) {
            txtZip.setText(address.getZip());
            txtCountry.setText(address.getCountry());
            txtCity.setText(address.getCity());
            txtStreet.setText(address.getStreet());
            txtHouse.setText(address.getHouse());
            txtApartment.setText(address.getApartment());
        } else {
            txtZip.setText("");
            txtCountry.setText("");
            txtCity.setText("");
            txtStreet.setText("");
            txtHouse.setText("");
            txtApartment.setText("");
        }

        Contacts contact = selectedAgent.getDefaultContacts();
        if (contact != null) {
            txtPhone.setText(contact.getDefaultPhone());
            txtEmail.setText(contact.getDefaultEmail());
            txtURL.setText(contact.getDefaultURL());
            txtContactPerson.setText(contact.getDefaultContactPerson());
        } else {
            txtPhone.setText("");
            txtEmail.setText("");
            txtURL.setText("");
            txtContactPerson.setText("");
        }

        Account account = selectedAgent.getDefaultAccount();
        if (account != null) {
            txtBank.setText(account.getBank());
            txtAccount.setText(account.getAccount());
            txtKs.setText(account.getKs());
            txtBik.setText(account.getBik());
            txtInn.setText(account.getInn());

        } else {
            txtBank.setText("");
            txtAccount.setText("");
            txtKs.setText("");
            txtBik.setText("");
            txtInn.setText("");
        }

        chkBox.setChecked(selectedAgent.isDefaultSupplier());
    }

    @Override
    public String getModel() {
        String _model;
        if (selectedAgent == null) {
            _model = ""
                    + "<div style='width:100%;height:100%;overflow:hidden;' identificator='"
                    + getIdentificator()
                    + "' class='rightPanel'>"
                    + "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<div style='width:50%;height:50%;border-radius:18px;border-color:gray;"
                    + "border-style:dotted;border-width:3px;' align='center'>"
                    + "<table width='100%' height='100%'><tr><td align='center' valign='middle'>"
                    + "Для редактирования свойств партнера сделайте двойной щелчок на строчке таблицы"
                    + "</td></tr></table>"
                    + "</div>"
                    + "<td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>";
        } else {
            _model = "<div style='width:98%;height:100%;overflow:auto;' identificator='"
                    + getIdentificator()
                    + "' class='rightPanel'>"
                    + "<div style='padding:5px;border-radius:8px;"
                    + "border-color:gray;border-style:dotted;border-width:1px;"
                    + "background-color: #CACACA;margin:3px;'>"
                    + "<div align='right'>"
                    + btnApply.getModel()
                    + "</div>"
                    + "<div style='width:100%;font-size:80%;'><strong>Название кратко</strong></div>"
                    + txtShortName.getModel()
                    + "<div style='width:100%;font-size:80%;'><strong>Название полностью</strong></div>"
                    + txtFullName.getModel()
                    + "<div style='width:100%;font-size:80%;'><strong>ИНН</strong></div>"
                    + txtINN.getModel()
                    + "</div>"
                    // 
                    + "<div style='width:97%;font-size:80%;padding:5px;border-radius:8px;"
                    + "border-color:gray;border-style:dotted;border-width:1px;"
                    + "background-color: #CACACA;margin:3px;'>"
                    + "Банк<br/>"
                    + txtBank.getModel()
                    + "р/с<br/>"
                    + txtAccount.getModel()
                    + "корр.счет<br/>"
                    + txtKs.getModel()
                    + "БИК<br/>"
                    + txtBik.getModel()
                    + "ИНН<br/>"
                    + txtInn.getModel()
                    + "</div>"
                    //
                    + "<div style='width:97%;font-size:80%;padding:5px;border-radius:8px;"
                    + "border-color:gray;border-style:dotted;border-width:1px;"
                    + "background-color: #CACACA;margin:3px;'>"
                    + "<b>Адрес:</b><br/>"
                    + "Страна<br/>"
                    + txtCountry.getModel()
                    + "<br/>"
                    + "Индекс:<br/>"
                    + txtZip.getModel()
                    + "<br/>"
                    + "Город:<br/>"
                    + txtCity.getModel()
                    + "<br/>"
                    + "Улица<br/>"
                    + txtStreet.getModel()
                    + "<br/>"
                    + "Дом<br/>"
                    + txtHouse.getModel()
                    + "<br/>"
                    + "Офис<br/>"
                    + txtApartment.getModel()
                    + "<br/>"
                    + "<hr/>"
                    + "<b>Контакты:</b><br/>"
                    + "Контактное лицо<br/>"
                    + txtContactPerson.getModel()
                    + "<br/>"
                    + "Телефон<br/>"
                    + txtPhone.getModel()
                    + "<br/>"
                    + "E-mail<br/>"
                    + txtEmail.getModel()
                    + "<br/>"
                    + "WWW<br/>"
                    + txtURL.getModel()
                    + "<br/>"
                    + chkBox.getModel()
                    + "<br/>"
                    + "<div align='right'>"
                    + btnApply.getModel()
                    + "</div>"
                    + "</div>"
                    + "</div>";
        }

        return _model;
    }

    @Override
    public String getIdentificator() {
        return AgentsPropertiesPanel.class.getName();
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

            if ((jsonObject.getString("eventType").equals("push"))
                    && (jsonObject.getString("action").equals("showRightPanel"))) {
                selectedAgent = ((Agent) dataBase.getObject(
                        Agent.class.getName(), jsonObject.getLong("dbid")));

                setPanel();
                JSMediator.setRightPanel(getSession(), getModel());
            }

            if ((jsonObject.getString("eventType").equals("click"))
                    && (jsonObject.getString("action").equals("showRightPanel"))) {
                JSMediator.setRightPanel(getSession(), getModel());
            }

        } catch (JSONException ex) {
            JSMediator.alert(getSession(), ex.toString());
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }
}
