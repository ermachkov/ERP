/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.cashmachine.opeartion;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.utils.Result;
import org.ubo.utils.SystemXML;
import org.ucm.cashmachine.CashMachineException;
import org.ucm.cashmachine.CashMachineResponse;
import org.ucm.cashmachine.CashMachineService;
import org.ucm.cashmachine.ResponseItem;
import org.ucm.cashmachine.pos.PosResponseItem;
import org.uui.component.WorkPanel;
import org.uui.plugin.HasRules;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.ribbon.RibbonButtonEventListener;
import org.uui.ribbon.RibbonEvent;
import org.uui.table.*;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;


/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class CashMachineOperationPanel extends WorkPanel implements HasWorkPanelToolbars, HasRules {

    private ArrayList<RibbonButton> toolbarButtons;
    private CarssierCore core = CarssierCore.getInstance();
    private MacTableModel macTableModel;

    public CashMachineOperationPanel(String sessionId) {
        super(sessionId);
        macTableModel = new MacTableModel(getSession());
        MacTableHeaderModel mth = new MacTableHeaderModel();
        macTableModel.setCssClass("leftMacTable");
        mth.addHeaderColumn(new MacHeaderColumn("Параметр", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Значение", String.class, false));
        macTableModel.setHeader(mth);
        initToolbar();
    }

    private void zReport() {
        ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
            @Override
            public void pressed(int button) {
                if (button == ConfirmPanel.YES) {
                    Path p = Paths.get(System.getProperty("user.home"), ".saas", "app", "config", "system.xml");
                    CashMachineService cms = CashMachineService.getInstance(p);
                    try {
                        Result r = core.printZReport(cms.getPassword(cms.getDefaultCashMachine()));
                        if (r.isError()) {
                            String errorMessage = "";
                            CashMachineResponse cmr = (CashMachineResponse) r.getObject();
                            for (ResponseItem responseItem : cmr.getResponseItemList()) {
                                if (responseItem.isError()) {
                                    errorMessage += "Команда: " + responseItem.getHumanCommand() + "<br/>";
                                    errorMessage += "Вызвала ошибку: " + responseItem.getHumanError() + "<br/><br/>";
                                }
                            }
                            PopupPanel popupPanel = new PopupPanel(getSession());
                            popupPanel.setTitle("Ошибка!");
                            popupPanel.setPanel("<div>" + errorMessage + "</div>");
                            popupPanel.showPanel();

                        } else {
                            JSMediator.showStatusInfo(getSession(), 1, "Кассовый аппарат: Ошибок нет");

                            String path = null;
                            CashMachineResponse cmr = (CashMachineResponse) r.getObject();
                            for (ResponseItem responseItem : cmr.getResponseItemList()) {

                                if (responseItem instanceof PosResponseItem) {
                                    if (("" + responseItem.getValue()).indexOf(".saas/app/ui/tmp/") != -1) {
                                        path = "" + responseItem.getValue();
                                    }
                                }
                            }

                            if (path != null) {
                                try {
                                    JSMediator.print(getSession(), new String(Files.readAllBytes(Paths.get(path))));
                                } catch (IOException ex) {
                                    JSMediator.alert(getSession(), ex.toString());
                                    Logger.getGlobal().log(Level.WARNING, path, ex);
                                }
                            }

                            r = core.getMoneyInCashBox(48);
                            if (r.getObject() instanceof BigDecimal) {
                                JSMediator.setCashBox(getSession(), "" + r.getObject());
                            }
                            
                            PopupPanel popupPanel = new PopupPanel(getSession());
                            popupPanel.setTitle("Сообщение");
                            popupPanel.setPanel("<div>Кассовый аппарат: Ошибок нет</div>");
                            popupPanel.showPanel();
                        }

                    } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
                        JSMediator.alert(getSession(), e.toString());
                    }

                }
            }
        };
        confirmPanel.setMessage("Распечатать Z-отчет?");
        confirmPanel.showPanel("getUICore().showConfirmPanel");
    }

    private void xReport() {
        ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
            @Override
            public void pressed(int button) {
                if (button == ConfirmPanel.YES) {
                    Path p = Paths.get(System.getProperty("user.home"), ".saas", "app", "config", "system.xml");
                    CashMachineService cms = CashMachineService.getInstance(p);
                    try {
                        Result r = core.printXReport(cms.getPassword(cms.getDefaultCashMachine()));
                        if (r.isError()) {
                            String errorMessage = "";
                            CashMachineResponse cmr = (CashMachineResponse) r.getObject();
                            for (ResponseItem responseItem : cmr.getResponseItemList()) {
                                if (responseItem.isError()) {
                                    errorMessage += "Команда: " + responseItem.getHumanCommand() + "<br/>";
                                    errorMessage += "Вызвала ошибку: " + responseItem.getHumanError() + "<br/><br/>";
                                }
                            }
                            PopupPanel popupPanel = new PopupPanel(getSession());
                            popupPanel.setTitle("Ошибка!");
                            popupPanel.setPanel("<div>" + errorMessage + "</div>");
                            popupPanel.showPanel();

                        } else {
                            JSMediator.showStatusInfo(getSession(), 1, "Кассовый аппарат: Ошибок нет");
                            
                            String path = null;
                            CashMachineResponse cmr = (CashMachineResponse) r.getObject();
                            for (ResponseItem responseItem : cmr.getResponseItemList()) {

                                if (responseItem instanceof PosResponseItem) {
                                    if (("" + responseItem.getValue()).indexOf(".saas/app/ui/tmp/") != -1) {
                                        path = "" + responseItem.getValue();
                                    }
                                }
                            }

                            if (path != null) {
                                try {
                                    JSMediator.print(getSession(), new String(Files.readAllBytes(Paths.get(path))));
                                } catch (IOException ex) {
                                    JSMediator.alert(getSession(), ex.toString());
                                    Logger.getGlobal().log(Level.WARNING, path, ex);
                                }
                            }
                        }

                    } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
                        JSMediator.alert(getSession(), e.toString());
                    }
                }
            }
        };
        confirmPanel.setMessage("Распечатать X-отчет?");
        confirmPanel.showPanel("getUICore().showConfirmPanel");

    }

    private void moneyIn() {
        try {
            Path p = Paths.get(System.getProperty("user.home"), ".saas", "app", "config", "system.xml");
            CashMachineService cms = CashMachineService.getInstance(p);
            BigDecimal sum = new BigDecimal("10");
            Result r = core.depositionMoneyInCashBox(sum, cms.getPassword(cms.getDefaultCashMachine()));
            if (r.isError()) {
                String errorMessage = "";
                CashMachineResponse cmr = (CashMachineResponse) r.getObject();
                for (ResponseItem responseItem : cmr.getResponseItemList()) {
                    if (responseItem.isError()) {
                        errorMessage += "Команда: " + responseItem.getHumanCommand() + "<br/>";
                        errorMessage += "Вызвала ошибку: " + responseItem.getHumanError() + "<br/><br/>";
                    }
                }
                PopupPanel popupPanel = new PopupPanel(getSession());
                popupPanel.setTitle("Ошибка!");
                popupPanel.setPanel("<div>" + errorMessage + "</div>");
                popupPanel.showPanel();

            } else {
                JSMediator.showStatusInfo(getSession(), 1, "Кассовый аппарат: Ошибок нет");
                r = core.getMoneyInCashBox(48);
                if (r.getObject() instanceof BigDecimal) {
                    JSMediator.setCashBox(getSession(), "" + r.getObject());
                }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void moneyOut() {
        try {
            Path p = Paths.get(System.getProperty("user.home"), ".saas", "app", "config", "system.xml");
            CashMachineService cms = CashMachineService.getInstance(p);
            BigDecimal sum = new BigDecimal("10");
            Result r = core.outMoneyFromCashBox(sum, cms.getPassword(cms.getDefaultCashMachine()));
            if (r.isError()) {
                String errorMessage = "";
                CashMachineResponse cmr = (CashMachineResponse) r.getObject();
                for (ResponseItem responseItem : cmr.getResponseItemList()) {
                    if (responseItem.isError()) {
                        errorMessage += "Команда: " + responseItem.getHumanCommand() + "<br/>";
                        errorMessage += "Вызвала ошибку: " + responseItem.getHumanError() + "<br/><br/>";
                    }
                }
                PopupPanel popupPanel = new PopupPanel(getSession());
                popupPanel.setTitle("Ошибка!");
                popupPanel.setPanel("<div>" + errorMessage + "</div>");
                popupPanel.showPanel();

            } else {
                JSMediator.showStatusInfo(getSession(), 1, "Кассовый аппарат: Ошибок нет");
                r = core.getMoneyInCashBox(48);
                if (r.getObject() instanceof BigDecimal) {
                    JSMediator.setCashBox(getSession(), "" + r.getObject());
                }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void zeroCoupon() {
    }

    @Override
    public String getModel() {
        SystemXML systemXML = SystemXML.newSystemXML(
                Paths.get(System.getProperty("user.home"), ".saas", "app",
                "config", "system.xml"));
        String defaultCashMachine = systemXML.getValue(
                "/root/default_cashmachine/@name", false);
        Map<String, String> values = systemXML.getValues(
                "//root/cashmachine[@name='" + defaultCashMachine + "']/@*");

        ArrayList<MacTableRow> rows = new ArrayList<>();
        Iterator<String> it = values.keySet().iterator();
        while (it.hasNext()) {
            MacTableRow row = new MacTableRow();
            String key = it.next();
            row.addCell(new MacTableCell(getSession(), key, false));
            row.addCell(new MacTableCell(getSession(), values.get(key), false));
            rows.add(row);
        }
        macTableModel.setData(rows);

        String model = ""
                + "<div style='margin:15px; width:100%;'>"
                + "<div style='font-size:80%; font-weight:bold;'>Кассовый аппарат: " + defaultCashMachine + "</div>"
                + macTableModel.getModel()
                + "</div>";

        return model;
    }

    @Override
    public String getIdentificator() {
        return CashMachineOperationPanel.class.getName();
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

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Касса");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.hideNavigationPanel(getSession());

            }
        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    private String getSubOperationButtonModel() {
        int cols = getWorkpanelToolbars().size() / 2;
        int col = 0;
        String model = "<table class='subButtonsTable'><tr>";

        for (org.uui.ribbon.RibbonButton rb : getWorkpanelToolbars()) {
            if (col == cols) {
                model += "</tr><tr>";
            }

            if (core.isRadioButtonRuleAllow(getSession(), "canPrintXReport", "printXReportDeny")
                    && rb.getActionName().equals("xReport")) {
                col++;
                continue;

            } else if (core.isRadioButtonRuleAllow(getSession(), "canPrintZReport", "printZReportDeny")
                    && rb.getActionName().equals("zReport")) {
                col++;
                continue;

            } else if (core.isRadioButtonRuleAllow(getSession(), "canInputMoney", "inputMoneyDeny")
                    && rb.getActionName().equals("moneyIn")) {
                col++;
                continue;

            } else if (core.isRadioButtonRuleAllow(getSession(), "canOutputMoney", "outputMoneyDeny")
                    && rb.getActionName().equals("moneyOut")) {
                col++;
                continue;

            } else {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            }

            col++;
        }

        model += "</tr></table>";
        return model;
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();

        RibbonButton rbZReport = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/z-report.png",
                "Z-отчет",
                "zReport");
        rbZReport.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                zReport();
            }
        });
        toolbarButtons.add(rbZReport);

        RibbonButton rbXReport = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/x-report.png",
                "X-отчет",
                "xReport");
        rbXReport.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                xReport();
            }
        });
        toolbarButtons.add(rbXReport);

        RibbonButton rbMoneyIn = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/money_in.png",
                "Внесение",
                "moneyIn");
        rbMoneyIn.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                moneyIn();
            }
        });
        toolbarButtons.add(rbMoneyIn);

        RibbonButton rbMoneyOut = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/money_out.png",
                "Выплата",
                "moneyOut");
        rbMoneyOut.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                moneyOut();
            }
        });
        toolbarButtons.add(rbMoneyOut);

        RibbonButton rbZero = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/empty.png",
                "Пустой чек",
                "zeroCoupon");
        rbZero.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                zeroCoupon();
            }
        });
        toolbarButtons.add(rbZero);
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("printXReportDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("printXReportAllow");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canPrintXReport",
                "Cнимать Х-отчет:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("printZReportDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("printZReportAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canPrintZReport",
                "Cнимать Z-отчет:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("inputMoneyDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("inputMoneyAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canInputMoney",
                "Вносить деньги в кассу:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("outputMoneyDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("outputMoneyAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canOutputMoney",
                "Выплачивать деньги из кассы:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return toolbarButtons;
    }
}
