/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.warehouse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.goods.Goods;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.partner.Agent;
import org.ubo.rules.Rule;
import org.ubo.rules.RuleItem;
import org.ubo.rules.Rules;
import org.ubo.storage.Storage;
import org.ubo.tree.TreeBasic;
import org.ubo.tree.TreeFolderBasic;
import org.ubo.tree.TreeLeafBasic;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerFolder;
import org.uui.explorer.NavigatorButton;
import org.uui.plugin.HasRules;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.ribbon.RibbonButtonEventListener;
import org.uui.ribbon.RibbonEvent;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class WarehousesPanel extends WorkPanel implements HasWorkPanelToolbars, HasRules {

    private CarssierCore core = CarssierCore.getInstance();
    private ArrayList<RibbonButton> toolbarButtons;
    private Storage enteredStorage = null;
    private NavigatorButton btnHome;
    private MacTableModel macTableModel;

    public WarehousesPanel(String sessionId) {
        super(sessionId);
        btnHome = new NavigatorButton(sessionId, "", -1, "Home");
        btnHome.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                JSMediator.setWorkPanel(getSession(), getModel());
            }
        });
        initToolbar();

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Наименование", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Кол-во", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Цена", BigDecimal.class, false));

        macTableModel = new MacTableModel(sessionId, true);
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("storageTable");
        macTableModel.setHeader(mth);
        macTableModel.setNavigatorShowingAlways(true);
        macTableModel.setNavigatorDateSelectorEnabled(false);

        macTableModel.addNavigatorChangeListener(new NavigatorChangeListener() {
            @Override
            public void event(int event) {
                try {
                    switch (event) {
                        case MacTableNavigator.FILTER:
                            //
                            break;

                        case MacTableNavigator.REFRESH:
                            //
                            break;

                        case MacTableNavigator.CALENDAR:
                            break;

                        default:
                        //
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();
        boolean isAllow = userSystemId == 0 ? true : false;
        RuleItem ruleItem = RuleItem.newRuleItemBoolean("canCreateStorages",
                "Разрешить создание новых складов", isAllow);
        listRulesItem.add(ruleItem);

        ruleItem = RuleItem.newRuleItemBoolean("canDeleteStorages",
                "Разрешить удаление складов", isAllow);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();

        RibbonButton rbAddStorage = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/add_warehouse.png",
                "Добавить склад",
                "createStorage");
        rbAddStorage.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                addStorage();
            }
        });
        toolbarButtons.add(rbAddStorage);

        RibbonButton rbRemoveStorage = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/delete.png",
                "Удалить склад",
                "deleteStorage");
        rbRemoveStorage.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                JSMediator.requestAllSelectedNodes(getSession(), "removeStorage", getIdentificator());
            }
        });

        toolbarButtons.add(rbRemoveStorage);
    }

    private String getPanelMenu() {
        String model = "";

        MenuItem mnuAddStorage = new MenuItem(getSession(), "img/icons/add_warehouse24.png", "Добавить склад");
        mnuAddStorage.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                addStorage();
            }
        });

        if (canCreateStorages()) {
            model += mnuAddStorage.getModel();
        }

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    private void addStorage() {
        Result r = core.getDefaultSupplier();
        if (r.isError()) {
            PopupPanel popupPanel = new PopupPanel(getSession()); popupPanel.setTitle("Информация");
            popupPanel.setPanel("Не могу найти поставщика "
                    + "услуг по умолчанию. Пожалуйста перейдите в "
                    + "раздел Справочники-Кто-то и добавьте кого то...");
            popupPanel.showPanel();

        } else {
            Agent agent = (Agent) r.getObject();

            PopupPanel popupPanel = new PopupPanel(getSession()); popupPanel.setTitle("Вопрос");
            TextField txtName = new TextField(getSession());
            txtName.setStyle("width:96%;");
            String panel = "<div>Имя нового склада</div>"
                    + txtName.getModel();
            popupPanel.setPanel(panel);
            popupPanel.showPanel();

            if (txtName.getText().equals("")) {
                JSMediator.alert(getSession(), "Имя склада не может быть пустым");
                return;
            }

            r = core.addStorage(txtName.getText(), txtName.getText(),
                    agent, agent.getDefaultAddress(),
                    agent.getDefaultContacts(), false);

            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
            } else {
                JSMediator.setWorkPanel(getSession(), getModel());

            }
        }

    }

    private void removeStorages(JSONArray jsonArray) {
        ArrayList<Storage> removeStorageList = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                if (jsonObject.getString("className").indexOf("Storage") != -1) {
                    Result result = core.getStorage(jsonObject.getLong("dbid"));
                    if (result.isError()) {
                        JSMediator.alert(getSession(), result.getReason());

                    } else {
                        removeStorageList.add((Storage) result.getObject());
                    }
                }
            }

            if (removeStorageList.isEmpty()) {
                PopupPanel popupPanel = new PopupPanel(getSession()); popupPanel.setTitle("Подсказка");
                popupPanel.setPanel("Чтобы удалить что "
                        + "то ненужное, нужно сначала выделить что то ненужое.");
                popupPanel.showPanel();

            } else {
                for (final Storage s : removeStorageList) {
                    if (s.isVirtual()) {
                        PopupPanel popupPanel = new PopupPanel(getSession()); popupPanel.setTitle("Предупреждение!");
                        popupPanel.setPanel("Виртуальные склады удалить не могу.");
                        popupPanel.showPanel();
                        continue;
                    }

                    if (!s.isStorageEmpty()) {
                        PopupPanel popupPanel = new PopupPanel(getSession()); popupPanel.setTitle("Предупреждение!");
                        popupPanel.setPanel("Непустые склады удалить не могу.");
                        popupPanel.showPanel();
                        continue;
                    }

                    ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {

                        @Override
                        public void pressed(int button) {
                            if(button == ConfirmPanel.YES){
                                core.removeStorage(s);
                            }
                        }
                    }; 
                    confirmPanel.setTitle("Вопрос");
                    confirmPanel.setMessage("Вы на самом деле желаете удалить склад?");
                    confirmPanel.showPanel();
                }

                JSMediator.setWorkPanel(getSession(), getModel());
            }

        } catch (JSONException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }
    }

    @Override
    public String getIdentificator() {
        return WarehousesPanel.class.getName();
    }

    @Override
    public String getModel() {
        String model = "";
        Result r = core.getStorages();
        if (r.isError()) {
            model = "<div style='width:100%; height:100%; overflow:hidden;' "
                    + "class='rightPanel' "
                    + "identificator='" + getIdentificator() + "'>"
                    + "<table width='100%' height='100%'>"
                    + "<tr>"
                    + "<td align='center' valign='middle'>"
                    + "<div style='width:50%;height:50%;border-radius:18px;border-color:gray;"
                    + "border-style:dotted;border-width:3px;' align='center'>"
                    + "<table width='100%' height='100%'><tr>"
                    + "<td align='center' valign='middle' "
                    + "style='background-image: url(img/dragdrop/target.png);"
                    + "background-position: center;background-repeat: no-repeat;'>"
                    + "Складов не обнаружено. Добавляйте склады кнопкой <b>«Добавить склад»</b>"
                    + "</td></tr></table>"
                    + "</div>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>";

        } else {
            ArrayList<Storage> list = (ArrayList<Storage>) r.getObject();
            for (Storage s : list) {
                ExplorerFolder folder = new ExplorerFolder(getSession(), 
                        Storage.class.getName(),
                        s.getId(),
                        s.getShortName(),
                        s.getImageFileName());

                folder.addUIEventListener(new UIEventListener() {
                    @Override
                    public void event(UIEvent evt) {
                        try {
                            if (evt.getJSONObject().getString("eventType").equals("click")) {
                                showStorageContent(evt.getJSONObject().getLong("dbid"));
                            }

                            if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                                JSMediator.setContextMenu(getSession(), getFolderMenu(evt.getJSONObject().getLong("dbid")));
                            }
                        } catch (JSONException e) {
                        }
                    }
                });

                model += folder.getModel();
            }
        }

        return model;
    }

    private void showStorageContent(long storageId) {
        WebKitEventBridge.getInstance().cleanMacTable(getSession(), macTableModel);

        Result r = core.getStorage(storageId);
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
            return;
        }

        Storage storage = (Storage) r.getObject();

        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (Goods goods : core.getGoodsOnStorage(storageId)) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), goods.getShortName(), false));

            BigDecimal rest = core.getGoodsCountOnStorage(goods, storage.getShortName());
            String style = "";

            if (rest.doubleValue() == 0) {
                style = " style='color:red;text-align:center;' ";

            } else if (rest.doubleValue() <= goods.getMinRestQuantity().doubleValue()) {
                style = " style='color:red;' ";
            }

            String sRest = "<div " + style + " align='center'>" + rest + "</div>";

            row.addCell(new MacTableCell(getSession(), sRest, false));
            row.addCell(new MacTableCell(getSession(), goods.getSalePrice(CarssierDataBase.getDataBase()), false));
            rows.add(row);
        }

        macTableModel.setData(rows);

        JSMediator.setWorkPanel(getSession(), macTableModel.getModel());

    }

    private String getFolderMenu(final long id) {
        String model = "";
        MenuItem mnuDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        mnuDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                String json = "[{className:" + Storage.class.getName() + ", dbid:" + id + "}]";
                try {
                    JSONArray array = new JSONArray(json);
                    removeStorages(array);

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, json, e);
                }
            }
        });

        if (canDeleteStorages()) {
            model += mnuDelete.getModel();
        }

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    private String getLeafMenu(final Goods goods) {
        String model = "";
        MenuItem mnuProperties = new MenuItem(getSession(), "img/subbuttons/settings.png", "Свойства");
        mnuProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    String module = "'Справочники', 'Товары'";
                    JSMediator.switchModule(getSession(), module);

                    Result r = core.getTreeGoodsAndService();
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        return;
                    }
                    TreeBasic treeBasic = (TreeBasic) r.getObject();
                    TreeLeafBasic tl = (TreeLeafBasic) treeBasic.getRootFolder().findTreeLeafWithObject(goods);

                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            "ru.sibek.plugin.goods.GoodsPanel",
                            "{"
                            + "eventType:push, "
                            + "action:showProperties, "
                            + "treeFolderId:" + ((TreeFolderBasic) tl.getParent()).getId() + ","
                            + "treeLeafId:" + tl.getId()
                            + "}");
                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });

        Result r = core.getRulesForUser(core.getLoggedUser(getSession()).getId());
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
            return model;
        }

        for (Rule rule : ((Rules) r.getObject()).getRules()) {
            if (rule.getModuleClassName().equals("ru.sibek.plugin.goods.GoodsUUIPlugin")) {
                if (rule.isAllowToUse()) {
                    model += mnuProperties.getModel();
                }
            }
        }

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
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
            
            JSMediator.setExplorerEditableMode(getSession(), true);

            if (jsonObject.getString("eventType").equals("removeStorage")) {
                removeStorages(jsonObject.getJSONArray("data"));
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Склады");
                JSMediator.setWorkPanel(getSession(), macTableModel.getModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
            }

            if (jsonObject.getString("eventType").equals("rightclick")) {
                JSMediator.setContextMenu(getSession(), getPanelMenu());
            }

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    private String getNavigatorModel() {

        NavigatorButton btnStorage = null;

        if (enteredStorage != null) {
            btnStorage = new NavigatorButton(
                    getSession(),
                    Storage.class.getName(),
                    enteredStorage.getId(),
                    enteredStorage.getShortName());
        }

        String model = btnHome.getModel();
        if (btnStorage != null) {
            model += btnStorage.getModel();
        }

        return model;
    }

    private boolean canDeleteStorages() {
        boolean canDeleteStorages = false;
        Result result = core.getRulesItemByKey(getSession(), "canDeleteStorages");
        if (!result.isError()) {
            RuleItem ruleItem = (RuleItem) result.getObject();
            canDeleteStorages = (Boolean) ruleItem.getValue();
        }

        return canDeleteStorages;
    }

    private boolean canCreateStorages() {
        boolean canCreateStorages = false;
        Result result = core.getRulesItemByKey(getSession(), "canCreateStorages");
        if (!result.isError()) {
            RuleItem ruleItem = (RuleItem) result.getObject();
            canCreateStorages = (Boolean) ruleItem.getValue();
        }

        return canCreateStorages;
    }

    private String getSubOperationButtonModel() {
        int cols = getWorkpanelToolbars().size() / 2;
        int col = 0;
        String model = "<table class='subButtonsTable'><tr>";

        for (org.uui.ribbon.RibbonButton rb : getWorkpanelToolbars()) {
            if (col == cols) {
                model += "</tr><tr>";
            }

            if (rb.getActionName().equals("createStorage") && canCreateStorages()) {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            }

            if (rb.getActionName().equals("deleteStorage") && canDeleteStorages()) {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";
            }
            col++;
        }

        model += "</tr></table>";
        return model;
    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return toolbarButtons;
    }
}
