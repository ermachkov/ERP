/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.service;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.employee.Crew;
import org.ubo.service.Service;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.SalaryPanel;
import ru.sibek.core.ui.PopupPanel;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class ServicePropertiesPanel extends PropertiesPanel {

    private Service service;
    private TextField txtPrice, txtDescription, txtShortName, txtFullName;
    private CheckBox chkIndividualPrice, chkSalaryPercent; //txtSalaryPercent
    private ComboBox cboCrews;
    private CarssierCore core = CarssierCore.getInstance();
    private DataBase dataBase;
    private Button btnImageChooser;
    private SalaryPanel salaryPanel;

    public ServicePropertiesPanel(String sessionId, DataBase dataBase) {
        super(sessionId, dataBase);
        this.dataBase = dataBase;

        chkIndividualPrice = new CheckBox(getSession(), "Цена на услугу устанавливается вручную при оформлении заказа");
        chkIndividualPrice.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                service.setIndividualPrice(chkIndividualPrice.isChecked());
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        ServiceCard.class.getName(),
                        "{eventType:push, session:" + getSession() + ", action:showRightPanel}");
            }
        });

        txtPrice = new TextField(getSession(), "");
        txtPrice.setStyle("text-align:center; width:15%;");

        txtDescription = new TextField(getSession(), "");
        txtDescription.setStyle("width:98%;");

        txtShortName = new TextField(getSession(), "");
        txtShortName.setStyle("width:98%;");

        txtFullName = new TextField(getSession(), "");
        txtFullName.setStyle("width:98%;");

        chkSalaryPercent = new CheckBox(getSession(), "Применить вознаграждение ко всем услугам группы", "salaryPercent", "0");
        chkSalaryPercent.setStyleLabel("font-size:80%;");

        super.btnApply.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                changeService();
            }
        });

        btnImageChooser = new Button(getSession(), "Изменить картинку");
        btnImageChooser.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                Path p = Paths.get(System.getProperty("user.home"), ".saas");
                ImageChooserPanel imageChooserPanel = new ImageChooserPanel(p.toString(), "img", "icons");
                imageChooserPanel.setTitle("Сменить картинку");
                imageChooserPanel.addUIEventListener(new UIEventListener() {
                    @Override
                    public void event(UIEvent evt) {
                        try {
                            service.setImageFileName(evt.getJSONObject().getString("src"));
                            Result r = core.modifyService(service);
                            if (r.isError()) {
                                JSMediator.alert(getSession(), r.getReason());
                                return;
                            }

                            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                                    ServicesPanel.class.getName(),
                                    "{eventType:push, session:" + getSession() + ", action:updateWorkPanel}");

                            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                                    ServiceCard.class.getName(),
                                    "{eventType:push, session:" + getSession() + ", action:updateRightPanel}");

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                        }
                    }
                });

                JSMediator.showImageChooser(getSession(), imageChooserPanel.getModel());
            }
        });

        salaryPanel = new SalaryPanel(getSession());
    }

    private void changeService() {
        JSMediator.showLockPanel(getSession());
        
        String errorMsg = "";
        if (txtShortName.getText().equals("")) {
            errorMsg += "Краткое название услуги не может быть пустым<br/>";
        }

        switch (txtPrice.getText()) {
            case "":
                errorMsg += "Цена услуги не может быть пустой<br/>";
                break;

            case "0":
                if (!chkIndividualPrice.isChecked()) {
                    errorMsg += "Цена услуги не может быть равна 0<br/>";
                }
                break;
        }

        try {
            Double.parseDouble(txtPrice.getText().replaceAll(",", "."));
        } catch (Exception e) {
            errorMsg += "Цена услуги не может быть не цифровым значением<br/>";
        }

        if (errorMsg.length() > 0) {
            errorMsg = "<div style='font-size:80%;'>"
                    + "<img src='img/info/warning.png' align='left' hspace='5'>"
                    + errorMsg + "</div>";
            PopupPanel popupPanel = new PopupPanel(getSession());
            popupPanel.setTitle("<span style='color:red;'>Ошибка</span>");
            popupPanel.setPanel(errorMsg);
            popupPanel.showPanel();

        } else {
            Map<String, Object> m = service.getAdditionInfo();
            m.put("salaryDistribution", salaryPanel.getSalaryDistribution());
            service.setAdditionInfo(m);
            
            if(chkSalaryPercent.isChecked()){
                Result uResult = core.setServiceSalaryDistribution(ServiceExchange.getInstance().getCurrentTreeFolder(), m);
                if(uResult.isError()){
                    JSMediator.alert(getSession(), uResult.getReason());
                    JSMediator.hideLockPanel(getSession());
                    return;
                }
            }

            Result r = core.modifyService(
                    service,
                    txtShortName.getText(),
                    txtFullName.getText(),
                    new BigDecimal(txtPrice.getText().replaceAll(",", ".")),
                    BigDecimal.ZERO,
                    "Ручное изменение свойств услуги от "
                    + DateTime.getFormatedDate("dd.MM.yy HH:mm", new Date()),
                    txtDescription.getText(),
                    //chkSalaryPercent.isChecked(),
                    false,
                    chkIndividualPrice.isChecked(),
                    Long.parseLong(cboCrews.getSelectedValue()));

            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());

            } else {
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        ServiceCard.class.getName(),
                        "{eventType:push, session:" + getSession() + ", action:updateRightPanel}");
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        ServicesPanel.class.getName(),
                        "{eventType:push, session:" + getSession() + ", action:updateWorkPanel}");
            }
        }
        
        JSMediator.hideLockPanel(getSession());
    }

    public void setService(Service service) {
        this.service = service;
        txtPrice.setText("" + service.getSalePrice(dataBase));
        txtDescription.setText(service.getDescription());
        //txtSalaryPercent.setText("" + service.getSalaryPercent());
        txtShortName.setText(service.getShortName());
        txtFullName.setText(service.getFullName());
        chkIndividualPrice.setChecked(service.isIndividualPrice());
    }

    public void refresh() {
        if (service == null) {
            return;
        }

        Result r = core.getService(service.getId());
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
            Logger.getGlobal().log(Level.WARNING, r.getReason());

        } else {
            setService((Service) r.getObject());
        }
    }

    public Button getBtnApply() {
        return btnApply;
    }

    public Service getService() {
        return service;
    }

    public TextField getTxtDescription() {
        return txtDescription;
    }

    public TextField getTxtPrice() {
        return txtPrice;
    }

    @Override
    public String getModel() {
        String _model;
        if (service == null) {
            _model = "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<div style='width:50%;height:50%;border-radius:18px;border-color:gray;"
                    + "border-style:dotted;border-width:3px;' align='center'>"
                    + "<table width='100%' height='100%'><tr><td align='center' valign='middle'>"
                    + "Для просмотра и редактирования свойств просто бросайте сюда услугу"
                    + "</td></tr></table>"
                    + "</div>"
                    + "<td>"
                    + "</tr>"
                    + "</table>";
        } else {

            if (core.isRadioButtonRuleAllow(getSession(), "canChangeServicePrice", "changeServicePriceAllow")) {
                txtPrice.setEnabled(true);
                chkIndividualPrice.setEnabled(true);

            } else {
                txtPrice.setEnabled(false);
                chkIndividualPrice.setEnabled(false);
            }

            String priceField;
            if (service.isIndividualPrice()) {
                priceField = "Цена на услугу устанавливается индивидуально";
            } else {
                priceField = txtPrice.getModel();
            }

            LinkedHashMap<String, String> data = new LinkedHashMap<>();
            int index = 0, i = 0;
            for (Crew crew : core.getCrewsList()) {
                data.put(crew.getName(), "" + crew.getId());
                if (service.getExecutorCrewId() == crew.getId()) {
                    index = i;
                }
                i++;
            }
            cboCrews = new ComboBox(getSession(), data);
            cboCrews.setSelectedIndex(index);

            salaryPanel.setService(service);

            _model = "<div style='width:96%;height:100%;overflow:auto;padding:5px;font-size:80%'>"
                    + "<div><strong>Наименование кратко:</strong></div>"
                    + "<div>" + txtShortName.getModel() + "&nbsp;</div>"
                    + "<div><strong>Наименование полностью:</strong></div>"
                    + "<div>" + txtFullName.getModel() + "&nbsp;</div>"
                    + "<hr/>"
                    + "<div>"
                    + "<div style='width:19%; float:left;' align='center'>"
                    + "<img src='" + service.getImageFileName() + "'/>"
                    + btnImageChooser.getModel()
                    + "</div>"
                    + "<div style='width:79%;float:left;border-radius:8px;"
                    + "border-color:gray;border-style:dotted;border-width:1px;"
                    + "background-color: #CACACA;' align='center'>"
                    + "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<strong>Стоимость услуги, в руб.</strong><br/>"
                    + priceField + "<br/><br/>"
                    + chkIndividualPrice.getModel() + "<br/><br/>"
                    + "<strong>Вознаграждение (отчисления на з/п) за оказанную услугу в % </strong><br/>"
                    + salaryPanel.getModel() + "<br/><br/>"
                    + "<strong>Основание для установки стоимости услуги:</strong><br/>"
                    + service.getPriceReason(dataBase) + "<br/>"
                    + "<strong>Примечания:</strong><br/>"
                    + txtDescription.getModel()
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>"
                    + "</div>"
                    + "<div>" + btnApply.getModel()
                    + "&nbsp;"
                    + chkSalaryPercent.getModel()
                    + "</div>"
                    + "<hr/>"
                    + "</div>";
        }

        return _model;
    }

    @Override
    public void setSession(String session) {
        super.setSession(session);
        salaryPanel.setSession(session);
    }
}
