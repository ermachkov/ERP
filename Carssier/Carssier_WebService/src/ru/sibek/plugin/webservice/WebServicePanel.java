/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.webservice;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.utils.Result;
import org.ubo.www.ExchangeSettings;
import org.uui.component.Button;
import org.uui.component.CheckBox;
import org.uui.component.TextField;
import org.uui.component.WorkPanel;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.Callback;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class WebServicePanel extends WorkPanel {

    private TextField txtRemoteServiceHost, txtTimeout, txtLogin, txtPassword;
    private Button btnSave;
    private CheckBox chkEnable;
    private CarssierCore core;
    private WebService webService;
    private StringBuilder log = new StringBuilder();

    public WebServicePanel(String sessionId) {
        super(sessionId);
        core = CarssierCore.getInstance();

        txtRemoteServiceHost = new TextField(getSession());
        txtRemoteServiceHost.setText("");

        txtTimeout = new TextField(getSession());
        txtTimeout.setText("");

        txtLogin = new TextField(getSession());
        txtLogin.setText("");

        txtPassword = new TextField(getSession());
        txtPassword.setText("");

        chkEnable = new CheckBox(getSession(), "");

        btnSave = new Button(getSession(), "Сохранить");
        btnSave.addUIEventListener(getSaveListener());

        webService = new WebService(sessionId);
    }

    private UIEventListener getSaveListener() {
        UIEventListener listener = new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                ExchangeSettings es = new ExchangeSettings();
                es.setEnabled(chkEnable.isEnabled());
                int timeout = 1;
                try {
                    timeout = Integer.parseInt(txtTimeout.getText().trim());
                } catch (Exception e) {
                }
                es.setExchangeTimeOut(timeout);
                es.setRemoteHost(txtRemoteServiceHost.getText());
                es.setLogin(txtLogin.getText());
                es.setPassword(txtPassword.getText());

                Result r = core.modifyExchangeSettings(es);
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                } else {
                    webService.reInit(es);
                }
            }
        };

        return listener;
    }

    @Override
    public String getModel() {
        Result result = core.getExchangeSettings();
        if (result.isError()) {
            chkEnable.setChecked(false);
            txtRemoteServiceHost.setText("");
            txtTimeout.setText("1");
            txtLogin.setText("");
            txtPassword.setText("");

        } else {
            ExchangeSettings es = (ExchangeSettings) result.getObject();
            chkEnable.setChecked(es.isEnabled());
            txtRemoteServiceHost.setText(es.getRemoteHost());
            txtTimeout.setText("" + es.getExchangeTimeOut());
            txtLogin.setText(es.getLogin());
            txtPassword.setText(es.getPassword());
        }

        String model = ""
                + "<div style='font-size:80%;'>"
                + "<table cellpadding='0' cellspacing='0'>"
                + "<tr><td>Логин</td><td>" + txtLogin.getModel() + "</td></tr>"
                + "<tr><td>Пароль</td><td>" + txtPassword.getModel() + "</td></tr>"
                + "<tr>"
                + "<td align='right'>"
                + "Сервис включить / выключить"
                + "</td>"
                + "<td>"
                + chkEnable.getModel()
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td align='right'>"
                + "Удаленный хост"
                + "</td>"
                + "<td>"
                + txtRemoteServiceHost.getModel()
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td align='right'>"
                + "Период обновления (минут)"
                + "</td>"
                + "<td>"
                + txtTimeout.getModel()
                + "</td>"
                + "</tr>"
                + "</table>"
                + btnSave.getModel()
                + "<div style='width:50%; border: 1px solid gray;'>"
                + "<div style='width:100%; border-bottom: 1px dotted gray;'>Логи</div>"
                + "<div>"
                + log.toString()
                + "</div>"
                + "</div>"
                + "</div>";

        return model;
    }

    @Override
    public String getIdentificator() {
        return "ru.sibek.plugin.webservice.WebServicePanel";
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

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "WWW служба");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.setWorkPanelSelectable(getSession(), false);
                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("setLog")) {
                    if (log.length() > 200) {
                        log.delete(0, (log.length() - 200));
                    }
                    log.append(jsonObject.getString("data"));
                    log.append("<br/>");
                    Callback callback = new Callback(getSession()) {
                        @Override
                        public void callback(String json) {
                            if (json.indexOf("WWW служба") == -1) {
                                return;
                            }

                            refresh();
                        }
                    };

                    callback.request("getUICore().getSelectedWorkPanel()");
                }
            }
        } catch (JSONException ex) {
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    private void refresh() {
        JSMediator.setWorkPanel(getSession(), getModel());
        JSMediator.setWorkPanelSelectable(getSession(), false);
    }
}
