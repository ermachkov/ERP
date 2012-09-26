/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.goods;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.goods.Goods;
import org.ubo.storage.Storage;
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
public class GoodsPropertiesPanel extends PropertiesPanel {

    private DataBase dataBase;
    private Goods goods;
    private TextField txtPrice, txtPriceIn, txtDescription, txtSalaryPercent, txtShortName,
            txtFullName, txtМeasure, txtQuantity, txtMinRestQuantity;
    private CheckBox chkIsReturnbale, chkIsSeparable, chkSalaryAllInGroup;
    private Button btnImageChooser;
    private CarssierCore core = CarssierCore.getInstance();
    private SalaryPanel salaryPanel;

    public GoodsPropertiesPanel(String sessionId, DataBase dataBase) {
        super(sessionId, dataBase);
        this.dataBase = dataBase;

        txtPrice = new TextField(getSession(), "");
        txtPrice.setStyle("text-align:center; width:15%;");

        txtPriceIn = new TextField(getSession(), "");
        txtPriceIn.setStyle("text-align:center; width:15%;");

        txtDescription = new TextField(getSession(), "");
        txtDescription.setStyle("width:98%;");

        txtSalaryPercent = new TextField(getSession(), "");
        txtSalaryPercent.setStyle("text-align:center; width:15%;");

        txtShortName = new TextField(getSession(), "");
        txtShortName.setStyle("width:98%;");

        txtFullName = new TextField(getSession(), "");
        txtFullName.setStyle("width:98%;");

        txtМeasure = new TextField(getSession(), "");
        txtМeasure.setStyle("width:98%;");

        txtQuantity = new TextField(getSession(), "0");
        txtQuantity.setStyle("width:20%;text-align:center;");

        txtMinRestQuantity = new TextField(getSession());
        txtMinRestQuantity.setStyle("width:20%;text-align:center;");

        chkSalaryAllInGroup = new CheckBox(getSession(), "Применить вознаграждение ко всем товарам группы", "salaryPercent", "0");
        chkSalaryAllInGroup.setStyleLabel("font-size:80%;");

        btnImageChooser = new Button(getSession(), "Изменить картинку");
        btnImageChooser.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                Path p = Paths.get(System.getProperty("user.home"), ".saas");
                ImageChooserPanel imageChooserPanel = new ImageChooserPanel(p.toString(), "img", "icons");
                imageChooserPanel.setTitle("Смена иконки");
                imageChooserPanel.addUIEventListener(new UIEventListener() {
                    @Override
                    public void event(UIEvent evt) {
                        try {
                            goods.setImageFileName(evt.getJSONObject().getString("src"));
                            Result r = core.modifyGoods(goods);
                            if (r.isError()) {
                                JSMediator.alert(getSession(), r.getReason());
                                return;
                            }

                            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                                    GoodsPanel.class.getName(),
                                    "{eventType:push, session:" + getSession() + ", action:updateWorkPanel}");

                            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                                    GoodsCard.class.getName(),
                                    "{eventType:push, session:" + getSession() + ", action:updateRightPanel}");

                        } catch (Exception e) {
                            JSMediator.alert(getSession(), e.toString());
                        }
                    }
                });

                JSMediator.showImageChooser(getSession(), imageChooserPanel.getModel());
            }
        });

        super.btnApply.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    JSMediator.showLockPanel(getSession());
                    changeGoods();
                    JSMediator.hideLockPanel(getSession());

                } catch (Exception ex) {
                    Logger.getGlobal().log(Level.WARNING, null, ex);
                    JSMediator.alert(getSession(), ex.toString());
                }
            }
        });

        chkIsReturnbale = new CheckBox(getSession(), "Товар может быть возвращен на склад?");
        chkIsSeparable = new CheckBox(getSession(), "Товар допускает отпуск дробных количеств?<br/>"
                + "Например:<br>"
                + "- гвозди на вес 2.46 кг.(допускает)<br/>"
                + "- карбюратор 3 шт. (не допускает)");

        salaryPanel = new SalaryPanel(getSession());
    }

    private void changeGoods() {
        JSMediator.showLockPanel(getSession());

        String errorMsg = "";
        if (txtShortName.getText().equals("")) {
            errorMsg += "Краткое название товара не может быть пустым<br/>";
        }

        switch (txtPrice.getText()) {
            case "":
                errorMsg += "Цена товара не может быть пустой<br/>";
                break;
            case "0":
                errorMsg += "Цена товара не может быть равна 0<br/>";
                break;
        }

        switch (txtPriceIn.getText()) {
            case "":
                errorMsg += "Цена товара не может быть пустой<br/>";
                break;
            case "0":
                errorMsg += "Цена товара не может быть равна 0<br/>";
                break;
        }

        try {
            Double.parseDouble(txtPrice.getText().replaceAll(",", "."));
        } catch (Exception e) {
            errorMsg += "Цена товара не может быть не цифровым значением<br/>";
        }

        if (txtSalaryPercent.getText().equals("")) {
            errorMsg += "Процент вознаграждения не может быть пустым<br/>";
        }

        try {
            Double.parseDouble(txtSalaryPercent.getText().replaceAll(",", "."));
        } catch (Exception e) {
            errorMsg += "Процент вознаграждения не может быть не цифровым значением<br/>";
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
            try {
                BigDecimal v = new BigDecimal(txtMinRestQuantity.getText().replaceAll(",", "."));
                goods.setMinRestQuantity(v);
            } catch (Exception e) {
                JSMediator.alert(getSession(), e.toString());
                JSMediator.hideLockPanel(getSession());
                return;
            }

            Map<String, Object> m = goods.getAdditionInfo();
            m.put("salaryDistribution", salaryPanel.getSalaryDistribution());
            goods.setAdditionInfo(m);

            if (chkSalaryAllInGroup.isChecked()) {
                Result uResult = core.setGoodsSalaryDistribution(GoodsExchange.getInstance().getCurrentTreeFolder(), m);
                if (uResult.isError()) {
                    JSMediator.alert(getSession(), uResult.getReason());
                    JSMediator.hideLockPanel(getSession());
                    return;
                }
            }

            Result r = core.modifyGoods(
                    goods,
                    txtShortName.getText(),
                    txtFullName.getText(),
                    txtМeasure.getText(),
                    txtDescription.getText(),
                    new BigDecimal(txtPriceIn.getText().replaceAll(",", ".")),
                    new BigDecimal(txtPrice.getText().replaceAll(",", ".")),
                    new BigDecimal(txtQuantity.getText().replaceAll(",", ".")),
                    new BigDecimal(txtSalaryPercent.getText().replaceAll(",", ".")),
                    "Ручное изменение свойств товара от "
                    + DateTime.getFormatedDate("dd.MM.yy HH:mm", new Date()),
                    chkIsReturnbale.isChecked(),
                    //chkSalaryAllInGroup.isChecked(),
                    false,
                    chkIsSeparable.isChecked());

            if (r.isError()) {
                errorMsg = "<div style='font-size:80%;'>"
                        + "<img src='img/info/warning.png' align='left' hspace='5'>"
                        + r.getReason()
                        + "</div>";

                if (r.getReason().equals("Can't find default supplier")) {
                    errorMsg = "<div style='font-size:80%;'>"
                            + "<img src='img/info/warning.png' align='left' hspace='5'>"
                            + "В данный момент не выбран поставщик товаров и "
                            + "услуг по умолчанию (ваше преприятие). "
                            + "Сейчас нужно его создать. "
                            + "Щелкните на кнопке <b>«Партнеры»</b>, создайте свое "
                            + "предприятие и поставьте галочку <b>«Использвать по умолчанию»</b>"
                            + "</div>";
                }

                PopupPanel popupPanel = new PopupPanel(getSession());
                popupPanel.setTitle("Предупреждение");
                popupPanel.setPanel(errorMsg);
                popupPanel.showPanel();

            } else {
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        GoodsCard.class.getName(),
                        "{eventType:push, session:" + getSession() + ", action:updateRightPanel}");
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        GoodsPanel.class.getName(),
                        "{eventType:push, session:" + getSession() + ", action:updateWorkPanel}");
            }
        }
        JSMediator.hideLockPanel(getSession());
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
        txtPrice.setText("" + goods.getSalePrice(dataBase));
        txtPriceIn.setText("" + core.getLastPriceIn(goods));
        txtDescription.setText(goods.getDescription());
        txtSalaryPercent.setText("" + goods.getSalaryPercent());
        txtShortName.setText(goods.getShortName());
        txtFullName.setText(goods.getFullName());
        txtМeasure.setText(goods.getMeasure());
        txtQuantity.setText("" + core.getGoodsCountOnAllStorages(goods));
    }

    public void refresh() {
        if (goods == null) {
            return;
        }

        Result r = core.getGoods(goods.getId());
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
            Logger.getGlobal().log(Level.WARNING, r.getReason());

        } else {
            setGoods((Goods) r.getObject());
        }
    }

    @Override
    public String getModel() {
        String _model;
        if (goods == null) {
            _model = "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<div style='width:50%;height:50%;border-radius:18px;border-color:gray;"
                    + "border-style:dotted;border-width:3px;' align='center'>"
                    + "<table width='100%' height='100%'><tr><td align='center' valign='middle'>"
                    + "Для просмотра и редактирования свойств просто бросайте сюда товар"
                    + "</td></tr></table>"
                    + "</div>"
                    + "<td>"
                    + "</tr>"
                    + "</table>";
        } else {

            Result r = core.getDefaultStorage(goods.getId());
            String reason = "";
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
            } else {
                reason = ((Storage) r.getObject()).getLastReason(goods.getId());
            }

            BigDecimal restCount = core.getGoodsCountOnAllStorages(goods);
            txtQuantity.setText("0");

            txtPrice.setText(((Storage) r.getObject()).getPrice(goods.getId()).toString());
            chkIsReturnbale.setChecked(goods.isReturnable());
            chkIsSeparable.setChecked(goods.isSeparable());

            if (core.isRadioButtonRuleAllow(getSession(), "canEasyWorkWithStorage", "storageEasyModeAllow")) {
                txtQuantity.setEnabled(true);
            } else {
                txtQuantity.setEnabled(false);
            }

            txtMinRestQuantity.setText(goods.getMinRestQuantity().toString());

            salaryPanel.setGoods(goods);

            _model = "<div style='width:96%;height:98%;overflow:auto;padding:5px;font-size:80%'>"
                    + "<div><strong>Наименование кратко:</strong></div>"
                    + "<div>" + txtShortName.getModel() + "&nbsp;</div>"
                    + "<div><strong>Наименование полностью:</strong></div>"
                    + "<div>" + txtFullName.getModel() + "&nbsp;</div>"
                    + "<div><strong>Единицы измерения:</strong></div>"
                    + "<div>" + txtМeasure.getModel() + "&nbsp;</div>"
                    + "<hr/>"
                    + "<div>"
                    + "<div style='width:19%; float:left;' align='center'>"
                    + "<img src='" + goods.getImageFileName() + "'/>"
                    + btnImageChooser.getModel()
                    + "</div>"
                    + "<div style='width:79%;float:left;border-radius:8px;"
                    + "border-color:gray;border-style:dotted;border-width:1px"
                    + ";background-color: #CACACA;' align='center'>"
                    + "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<strong>Входящая стоимость товара, в руб.</strong><br/>"
                    + txtPriceIn.getModel() + "<br/>"
                    + "<strong>Стоимость товара, в руб.</strong><br/>"
                    + txtPrice.getModel() + "<br/>"
                    + "<strong>Вознаграждение (отчисления на з/п) за реализованный товар в % </strong><br/>"
                    //+ txtSalaryPercent.getModel() + "<br/>"
                    + salaryPanel.getModel() + "<br/>"
                    + "<strong>Основание для установки стоимости товара:</strong><br/>"
                    + reason + "<br/>"
                    + "Остаток на всех складах&nbsp;" + restCount + "&nbsp;" + goods.getMeasure() + "<br/>"
                    + "Уменьшить / Увеличать кол-во на " + goods.getMeasure() + "<br/>"
                    + txtQuantity.getModel() + "<br/>"
                    + "Предупреждать когда кол-во товара на складе меньше<br/>"
                    + txtMinRestQuantity.getModel() + "<br/>"
                    + chkIsReturnbale.getModel() + "<br/>"
                    + chkIsSeparable.getModel() + "<br/>"
                    + "<strong>Примечания:</strong><br/>"
                    + txtDescription.getModel()
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>"
                    + "</div>"
                    + "<div style='height:30px;'>" + btnApply.getModel()
                    + "&nbsp;" + chkSalaryAllInGroup.getModel() + "</div>"
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
