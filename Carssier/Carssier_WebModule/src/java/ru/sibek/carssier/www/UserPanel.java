/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.carssier.www;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.partner.Agent;
import org.ubo.partner.Contacts;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.Component;
import org.uui.component.PasswordTextField;
import org.uui.component.TextField;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import org.uui.webkit.WebKitUtil;

/**
 *
 * @author developer
 */
public class UserPanel extends Component {

    private TextField txtLogin, txtNewLogin, txtNewPassword, txtContactPhone,
            txtContactEmail, txtName, txtCaptcha;
    private PasswordTextField txtPassword;
    private Button btnLogin, btnNewRegister, btnLoginRegister, btnUserInfo,
            btnUserOrders, btnEditUserInfo, btnBack;
    private final String realPath, requestURI, login;
    private HttpSession httpSession;
    private Core core;
    private Path pRealPath;
    private Agent loggedAgent;
    private MacTableModel macTableOrders, macTableBasket;

    public UserPanel(HttpSession httpSession, String session, String realPath, String requestURI) {
        super(session);
        this.httpSession = httpSession;
        this.realPath = realPath;
        this.pRealPath = Paths.get(realPath);
        this.requestURI = requestURI;
        Path p = Paths.get(requestURI);
        login = p.getName(p.getNameCount() - 1).toString();

        txtLogin = new TextField(getSession());
        txtPassword = new PasswordTextField(getSession());
        txtNewLogin = new TextField(getSession());
        txtNewPassword = new TextField(getSession());
        txtContactPhone = new TextField(getSession());
        txtContactEmail = new TextField(getSession());
        txtName = new TextField(getSession());
        txtCaptcha = new TextField(getSession());

        btnLogin = new Button(getSession(), "Ok");
        btnLogin.addUIEventListener(getButtonLoginEventListener());

        btnLoginRegister = new Button(getSession(), "Вход / Регистрация");
        btnLoginRegister.setStyle("font-size:70%");
        btnLoginRegister.addUIEventListener(getLoginRegisterEventListener());

        btnNewRegister = new Button(getSession(), "Регистрация");
        btnNewRegister.addUIEventListener(getNewRegisterEventListener());

        btnUserInfo = new Button(getSession(), "Инфо");
        btnUserInfo.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showUserInfoPanel("");
            }
        });

        btnUserOrders = new Button(getSession(), "Заказы");
        btnUserOrders.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showUserOrdersPanel();
            }
        });

        btnEditUserInfo = new Button(getSession(), "Сохранить");
        btnEditUserInfo.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                modifyUserInfo();
            }
        });
        
        btnBack = new Button(getSession(), "<< Назад");
        btnBack.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showUserOrdersPanel();
            }
        });

        core = Core.getInstance();

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Дата", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Описание", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Сумма", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Статус", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Детали", String.class, false));
        macTableOrders = new MacTableModel(session, true);
        macTableOrders.setHeader(mth);
        macTableOrders.setCssClass("macTable");
        macTableOrders.setId("ordersTable");
        
        MacTableHeaderModel mthBasket = new MacTableHeaderModel();
        mthBasket.addHeaderColumn(new MacHeaderColumn("Наименование", String.class, false));
        mthBasket.addHeaderColumn(new MacHeaderColumn("Цена", Number.class, false));
        mthBasket.addHeaderColumn(new MacHeaderColumn("Кол-во", Number.class, false));
        mthBasket.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, false));
        macTableBasket = new MacTableModel(getSession(), true, new MacTableSummator(4));
        macTableBasket.setHeader(mthBasket);
        macTableBasket.setCssClass("macTableBasket");
        macTableBasket.setId("basketTable");
    }

    public Agent getLoggedAgent() {
        return loggedAgent;
    }

    private void modifyUserInfo() {
        loggedAgent.setAdditionInfo("login", txtNewLogin.getText().trim());
        loggedAgent.setAdditionInfo("pasword", txtNewPassword.getText().trim());
        loggedAgent.setShortName(txtName.getText().trim());
        Contacts contacts = loggedAgent.getDefaultContacts();
        contacts.setDefaultPhone(txtContactPhone.getText().trim());
        contacts.setDefaultEmail(txtContactEmail.getText().trim());
        loggedAgent.setDefaultContacts(contacts);
        Result result = core.modifyPartner(pRealPath, login, loggedAgent);
        if (result.isError()) {
            WebSocketBundle.getInstance().send(session,
                    "getUICore().setWorkPanel('"
                    + WebKitUtil.prepareToJS(result.getReason())
                    + "');");
        } else {
            showUserInfoPanel("Сохранено!");
        }
    }

    private void showUserInfoPanel(String extraMessage) {

        txtNewLogin.setText("" + loggedAgent.getAdditionInfoByKey("login"));
        txtNewPassword.setText("" + loggedAgent.getAdditionInfoByKey("password"));
        Contacts contacts = loggedAgent.getDefaultContacts();
        txtContactPhone.setText(contacts.getDefaultPhone());
        txtContactEmail.setText(contacts.getDefaultEmail());
        txtName.setText("" + loggedAgent.getShortName());

        String extraRow = "";
        if (!extraMessage.equals("")) {
            extraRow = "<tr><td colspan='2' align='center'>" + extraMessage + "</td></tr>";
        }

        String panel = "<div style='font-size:80%;'>"
                + "<table align='center'>"
                + extraRow
                + "<tr>"
                + "<td>Логин</td>"
                + "<td>"
                + txtNewLogin.getModel()
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td>Пароль</td>"
                + "<td>"
                + txtNewPassword.getModel()
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td>Контактный телефон</td>"
                + "<td>"
                + txtContactPhone.getModel()
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td>Контактный email</td>"
                + "<td>"
                + txtContactEmail.getModel()
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td>Ф.И.О.</td>"
                + "<td>"
                + txtName.getModel()
                + "</td>"
                + "</tr>"
                + "<tr><td colspan='2' align='center'>"
                + btnEditUserInfo.getModel()
                + "</td></tr>"
                + "</table>"
                + "</div>";

        WebSocketBundle.getInstance().send(session,
                "getUICore().setWorkPanel('"
                + WebKitUtil.prepareToJS(panel)
                + "');");
    }

    private void showUserOrdersPanel() {
        ArrayList<Order> orders = core.getOrdersForAgent(pRealPath, login, loggedAgent);
        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (final Order order : orders) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), 
                    "<div align='center'>"
                    + DateTime.getFormatedDate("dd.MM.yy HH:mm", order.getDate())
                    + "</div>",
                    false));
            row.addCell(new MacTableCell(getSession(), "" + order.getDescription(), false));
            row.addCell(new MacTableCell(getSession(), order.getTotalWithTotalDiscount(), false));
            String syncStatus = "";
            switch (order.getSyncStatus()) {
                case Order.SYNC_NOT_SENDED:
                    syncStatus = "Не отправлен";
                    break;

                case Order.SYNC_SENDED:
                    syncStatus = "Отправлен";
                    break;

                case Order.SYNC_CONFIRMED:
                    syncStatus = "Принят";
                    break;

                case Order.SYNC_COMPLETE:
                    syncStatus = "Выполнен";
                    break;

            }
            row.addCell(new MacTableCell(getSession(), "<div align='center'>" + syncStatus + "</div>", false));
            
            Button btnInfo = new Button(getSession(), "Детали заказа");
            row.addCell(new MacTableCell(getSession(), "<div align='center'>" + btnInfo.getModel() + "</div>", false));
            btnInfo.addUIEventListener(new UIEventListener() {

                @Override
                public void event(UIEvent evt) {
                    showOrderDetails(order);
                }
            });
            
            rows.add(row);
        }
        
        macTableOrders.setData(rows);
        
        String model = ""
                + "<div style='font-size:80%; font-weight:bold;'>"
                + "Мои заказы"
                + "</div>"
                + macTableOrders.getModel();
        WebSocketBundle.getInstance().send(session,
                        "getUICore().setWorkPanel('"
                        + WebKitUtil.prepareToJS(model)
                        + "');");
    }
    
    private void showOrderDetails(Order order){
        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (OrderRow orderRow : order.getOrderRows()) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), orderRow.getSalesItem(
                    core.getDataBase(pRealPath, login)).getShortName(), false));
            row.addCell(new MacTableCell(getSession(), orderRow.getSalesItem(
                    core.getDataBase(pRealPath, login)).getSalePrice(core.getDataBase(pRealPath, login)), false));
            row.addCell(new MacTableCell(getSession(), orderRow.getCount(), true));
            row.addCell(new MacTableCell(getSession(), orderRow.getSumWithDiscount(), false));
            row.setValue(orderRow);

            rows.add(row);
        }
        
        macTableBasket.setData(rows);
        
        String model = ""
                + "<div>"
                + btnBack.getModel()
                + "</div>"
                + macTableBasket.getModel();
        WebSocketBundle.getInstance().send(session,
                        "getUICore().setWorkPanel('"
                        + WebKitUtil.prepareToJS(model)
                        + "');");
    }

    private UIEventListener getNewRegisterEventListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                registerNewUser();
            }
        };

        return listener;
    }

    private UIEventListener getLoginRegisterEventListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                WebSocketBundle.getInstance().send(session,
                        "getUICore().setWorkPanel('"
                        + WebKitUtil.prepareToJS(getLoginRegisterPanel(""))
                        + "');");
            }
        };

        return listener;
    }

    private UIEventListener getButtonLoginEventListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                doLogin();
            }
        };

        return listener;
    }

    private void doLogin() {
        Agent partner = core.getAgent(pRealPath, login,
                txtLogin.getText().trim(), txtPassword.getPassword());
        if (partner == null) {
            WebSocketBundle.getInstance().send(session,
                    "getUICore().setWorkPanel('"
                    + WebKitUtil.prepareToJS("Неправильный логин или пароль")
                    + "');");
        } else {
            loggedAgent = partner;
            WebSocketBundle.getInstance().send(session,
                    "getUICore().setWorkPanel('"
                    + WebKitUtil.prepareToJS("Добро пожаловать - " + partner.getShortName())
                    + "');");

            WebSocketBundle.getInstance().send(session,
                    "getUICore().setUserPanel('"
                    + WebKitUtil.prepareToJS(getUserPanel(partner))
                    + "');");
        }
    }

    private String getUserPanel(Agent agent) {
        String model = ""
                + "<div style='font-size:80%;'>"
                + agent.getShortName()
                + "&nbsp;"
                + btnUserInfo.getModel()
                + btnUserOrders.getModel()
                + "</div>";

        return model;
    }

    private void registerNewUser() {
        String errorMessage = "";

        if (txtNewLogin.getText().trim().equals("")) {
            errorMessage += "Поле <i><strong>логин</strong></i> должно быть заполнено<br/>";
        }

        if (txtNewPassword.getText().trim().equals("")) {
            errorMessage += "Поле <i><strong>пароль</strong></i> должно быть заполнено<br/>";
        }

        if (txtContactPhone.getText().trim().equals("")) {
            errorMessage += "Поле <i><strong>контактный телефон</strong></i> должно быть заполнено<br/>";
        }

        if (txtContactEmail.getText().trim().equals("")) {
            errorMessage += "Поле <i><strong>контактный email</strong></i> должно быть заполнено<br/>";
        }

        if (txtName.getText().trim().equals("")) {
            errorMessage += "Поле <i><strong>Ф.И.О.</strong></i> должно быть заполнено<br/>";
        }

        if (txtCaptcha.getText().trim().equals("")) {
            errorMessage += "Неправильно указан проверочный код<br/>";
        } else if (!httpSession.getAttribute("captcha").equals(txtCaptcha.getText().trim())) {
            errorMessage += "Неправильно указан проверочный код<br/>";
        }

        if (!errorMessage.equals("")) {
            WebSocketBundle.getInstance().send(session,
                    "getUICore().setWorkPanel('"
                    + WebKitUtil.prepareToJS(getLoginRegisterPanel(errorMessage))
                    + "');");
            return;
        }

        Map<String, String> map = new HashMap<>();
        map.put("login", txtNewLogin.getText().trim());
        map.put("password", txtNewPassword.getText().trim());
        map.put("phone", txtContactPhone.getText().trim());
        map.put("email", txtContactEmail.getText().trim());
        map.put("name", txtName.getText().trim());

        Result r = core.addAgent(pRealPath, login, map);
        if (r.isError()) {
            WebSocketBundle.getInstance().send(session,
                    "getUICore().setWorkPanel('"
                    + WebKitUtil.prepareToJS(r.getReason())
                    + "');");

        } else {
            WebSocketBundle.getInstance().send(session,
                    "getUICore().setWorkPanel('"
                    + WebKitUtil.prepareToJS("Регистрация успешно завершена.")
                    + "');");
        }
    }

    private String getLoginRegisterPanel(String errorMessage) {

        String errorBlock = "";
        if (!errorMessage.equals("")) {
            errorBlock = ""
                    + "<tr><td colspan='2'>"
                    + "<span style='color:red;'>При заполнении формы были допущены ошибки:</span><br>"
                    + errorMessage
                    + "</td></tr>";
        }


        String panel = ""
                + "<div style='font-size:80%;'>"
                + "<table align='center'>"
                + "<tr>"
                + "<td colspan='2' align='center' style='background-color:gray;'>Я уже зарегистрирован</td>"
                + "</tr>"
                + "<tr>"
                + "<td width='50%' align='right'>логин</td>"
                + "<td>" + txtLogin.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td width='50%' align='right'>пароль</td>"
                + "<td>" + txtPassword.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td colspan='2' align='center'>" + btnLogin.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td colspan='2' align='center' style='background-color:gray;'>Регистрация</td>"
                + "</tr>"
                + errorBlock
                + "<tr>"
                + "<td width='50%' align='right'>логин*</td>"
                + "<td>" + txtNewLogin.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td width='50%' align='right'>пароль*</td>"
                + "<td>" + txtNewPassword.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td width='50%' align='right'>Контактный телефон*</td>"
                + "<td>" + txtContactPhone.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td width='50%' align='right'>Контактный email*</td>"
                + "<td>" + txtContactEmail.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td width='50%' align='right'>Ф.И.О.*</td>"
                + "<td>" + txtName.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td colspan='2' align='center'>"
                + "Докажите что Вы не робот<br/>"
                + "<img src='/carssier/Captcha' /><br/>"
                + "<span style='font-size:70%;'>введите код указанный на картинке</span><br/>"
                + txtCaptcha.getModel()
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td colspan='2' align='center'>" + btnNewRegister.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td colspan='2' align='center'>"
                + "<span style='font-size:70%;'>Поля отмеченные звездочкой обязательны для заполнения</span>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</div>";

        return panel;
    }

    @Override
    public String getModel() {
        String model = ""
                + "<div style='font-size:75%;'>"
                + "Логин&nbsp;" + txtLogin.getModel()
                + "Пароль&nbsp;" + txtPassword.getModel()
                + btnLogin.getModel()
                + "</div>";

        model = btnLoginRegister.getModel();

        return model;
    }
}