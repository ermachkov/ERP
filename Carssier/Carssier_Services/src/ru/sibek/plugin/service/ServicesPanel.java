/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.proxy.KnowsId;
import org.ubo.employee.Crew;
import org.ubo.goods.Goods;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.tree.TreeFolder;
import org.ubo.tree.TreeFolderBasic;
import org.ubo.tree.TreeLeaf;
import org.ubo.tree.TreeLeafBasic;
import org.ubo.utils.Result;
import org.uui.component.ImageChooserPanel;
import org.uui.component.MenuItem;
import org.uui.component.RightPanel;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerFolder;
import org.uui.explorer.ExplorerLeaf;
import org.uui.explorer.ExplorerPanel;
import org.uui.explorer.TreeExplorerPanel;
import org.uui.plugin.HasRules;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.ribbon.RibbonButtonEventListener;
import org.uui.ribbon.RibbonEvent;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.core.Clipboard;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.Callback;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com (C)
 * Copyright by Pechenko Anton, created 16.03.2011
 */
public class ServicesPanel extends TreeExplorerPanel implements HasWorkPanelToolbars, HasRules {

    private DataBase dataBase;
    private ServiceCard sc;
    private ArrayList<RibbonButton> toolbarButtons;
    private CarssierCore core;
    private AtomicBoolean isLinkToGoods = new AtomicBoolean(true);
    private AtomicLong selectedTreeLeafId = new AtomicLong();
    private ServiceExchange serviceExchange;

    public ServicesPanel(String sessionId) {
        super(sessionId, CarssierDataBase.getDataBase(), "GoodsAndService");
        serviceExchange = ServiceExchange.getInstance();
        setFilter(ExplorerPanel.FILTER_HIDE, Goods.class.getName());
        dataBase = CarssierDataBase.getDataBase();
        core = CarssierCore.getInstance();
        initToolbar();
    }

    @Override
    public String treeWalker(TreeFolder treeFolder) {
        serviceExchange.setCurrentTreeFolder((TreeFolderBasic)treeFolder);
        
        htmlModel = "";

        List<TreeFolder> folders = Arrays.asList(treeFolder.getSetTreeFolder()
                .toArray(new TreeFolder[treeFolder.getSetTreeFolder().size()]));
        Collections.sort(folders, new Comparator<TreeFolder>() {
            @Override
            public int compare(TreeFolder o1, TreeFolder o2) {
                return o1.getText().compareTo(o2.getText());
            }
        });

        List<TreeLeaf> treeLeafs = Arrays.asList(treeFolder.getSetTreeLeaf()
                .toArray(new TreeLeaf[treeFolder.getSetTreeLeaf().size()]));
        Collections.sort(treeLeafs, new Comparator<TreeLeaf>() {
            @Override
            public int compare(TreeLeaf o1, TreeLeaf o2) {
                return o1.getText().compareTo(o2.getText());
            }
        });

        Set<ExplorerFolder> eFolders = new LinkedHashSet<>();
        Set<ExplorerLeaf> eLabels = new LinkedHashSet<>();

        for (TreeFolder tf : folders) {
            ExplorerFolder folder = new ExplorerFolder(getSession(), 
                    tf.getClass().getName(),
                    ((KnowsId) tf).getId(),
                    tf.getName(),
                    tf.getImageFileName());
            folder.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    System.out.println("ExplorerEvent evt " + evt);
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            TreeFolder selectedTreeFolder = (TreeFolder) dataBase.getObject(
                                    evt.getJSONObject().getString("className"),
                                    evt.getJSONObject().getLong("dbid"));
                            JSMediator.setContextMenu(getSession(), getFolderMenu(selectedTreeFolder));
                        }

                        if (evt.getJSONObject().getString("eventType").equals("click")) {
                            TreeFolder selectedTreeFolder = (TreeFolder) dataBase.getObject(
                                    evt.getJSONObject().getString("className"),
                                    evt.getJSONObject().getLong("dbid"));
                            moveDown(selectedTreeFolder);

                            JSMediator.setWorkPanel(getSession(), getModel());
                        }

                        if (evt.getJSONObject().getString("eventType").equals("drop")) {
                            cutAndPaste(evt.getJSONObject());
                            JSMediator.setWorkPanel(getSession(), getModel());
                        }

                        if (evt.getJSONObject().getString("eventType").equals("keypress")) {
                            rename(evt.getJSONObject());
                        }

                    } catch (JSONException ex) {
                        Logger.getGlobal().log(Level.WARNING, evt.toString(), ex);
                    }
                }
            });

            eFolders.add(folder);
        }

        for (TreeLeaf tl : treeLeafs) {
            boolean isContinue = false;
            if (filterClasses != null) {
                if (isFilterEnable) {
                    String contClass = tl.getContainer().getClassName();
                    for (String cls : filterClasses) {
                        if (cls.equals(contClass)) {
                            isContinue = true;
                        }
                    }
                }
            }

            if (isContinue) {
                continue;
            }

            String img = "img/icons/service.png";
            if (!tl.getImageFileName().trim().equals("")) {
                img = tl.getImageFileName();
            }

            ExplorerLeaf label = new ExplorerLeaf(getSession(), 
                    tl.getClass().getName(),
                    ((KnowsId) tl).getId(),
                    tl.getName(), img);
            label.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    System.out.println("ExplorerEvent evt " + evt);
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            TreeLeaf selectedTreeLeaf = (TreeLeaf) dataBase.getObject(
                                    evt.getJSONObject().getString("className"),
                                    evt.getJSONObject().getLong("dbid"));
                            JSMediator.setContextMenu(getSession(), getLeafMenu(selectedTreeLeaf));
                        }

                        if (evt.getJSONObject().getString("eventType").equals("keypress")) {
                            rename(evt.getJSONObject());
                            Callback callback = new Callback(getSession()) {
                                @Override
                                public void callback(String json) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(json);
                                        if (jsonObject.getBoolean("isRightPanelOpen")) {
                                            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                                                    ServiceCard.class.getName(),
                                                    "{eventType:push, session:"+getSession()+", action:updateRightPanel}");
                                        }

                                    } catch (Exception e) {
                                        JSMediator.alert(getSession(), e.toString());
                                        Logger.getGlobal().log(Level.WARNING, json, e);
                                    }
                                }
                            };

                            callback.request("getUICore().isRightPanelOpen();");
                        }
                    } catch (JSONException e) {
                        Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
                    }

                }
            });

            eLabels.add(label);
        }

        Logger.getGlobal().log(Level.INFO, "htmlModel = {0}", htmlModel);

        for (ExplorerFolder ef : eFolders) {
            htmlModel += ef.getModel();
        }

        for (ExplorerLeaf el : eLabels) {
            htmlModel += el.getModel();
        }

        setNavigatorPanel();
        return htmlModel;
    }

    @Override
    public void cutAndPaste(JSONObject jsonObject) {
        try {
            JSONArray array = jsonObject.getJSONArray("data");
            ArrayList<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                boolean isFind = false;
                for (JSONObject jo : list) {
                    if (jo.getString("className").equals(json.getString("className"))
                            && jo.getLong("dbid") == json.getLong("dbid")) {
                        isFind = true;
                        break;
                    }
                }

                if (isFind) {
                    continue;
                }

                list.add(json);
            }

            for (JSONObject json : list) {
                Result rCopy = core.cutTreeNode(json.getString("className"), json.getLong("dbid"));
                if (rCopy.isError()) {
                    JSMediator.alert(getSession(), rCopy.getReason());
                    continue;
                }


                Result rPaste = core.pasteTreeNode(
                        jsonObject.getString("className"),
                        "" + jsonObject.getLong("dbid"));
                if (rPaste.isError()) {
                    JSMediator.alert(getSession(), rPaste.getReason());
                    continue;
                }
            }

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    @Override
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("deleteServiceDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("deleteServiceAllow");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canDeleteService",
                "Удаление услуг:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("changeServicePriceDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("changeServicePriceAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canChangeServicePrice",
                "Установливать цены на услуги:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();

        // Add service
        RibbonButton rbAddService = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/add_service.png",
                "Добавить услугу",
                "addService");
        rbAddService.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                addService();
            }
        });
        toolbarButtons.add(rbAddService);

        // Add folder
        RibbonButton rbAddFolder = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/folder_new.png",
                "Добавить папку",
                "addFolder");
        rbAddFolder.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                addFolder();
            }
        });
        toolbarButtons.add(rbAddFolder);

        // Cut
        RibbonButton rbCut = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/cut.png",
                "Вырезать",
                "cut",
                "getUICore().getAllSelectedNodes();");
        rbCut.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                cut(evt.getJSONObject());
            }
        });
        toolbarButtons.add(rbCut);

        // Copy
        RibbonButton rbCopy = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/copy.png",
                "Копировать",
                "copy",
                "getUICore().getAllSelectedNodes();");
        rbCopy.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                copy(evt.getJSONObject());
            }
        });
        toolbarButtons.add(rbCopy);

        // Paste
        RibbonButton rbPaste = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/paste.png",
                "Вставить",
                "paste");
        rbPaste.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                paste(currentTreeFolder);
            }
        });
        toolbarButtons.add(rbPaste);

        // Delete
        RibbonButton rbDelete = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/delete.png",
                "Удалить",
                "delete",
                "getUICore().getAllSelectedNodes();");
        rbDelete.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(final RibbonEvent evt) {
                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {

                    @Override
                    public void pressed(int button) {
                        if(button == ConfirmPanel.YES){
                            delete(evt.getJSONObject());
                        }
                    }
                }; 
                confirmPanel.setTitle("Вопрос");
                confirmPanel.setMessage("Удалить безвозвратно?");
                confirmPanel.showPanel();
            }
        });
        toolbarButtons.add(rbDelete);

    }

    private void cut(JSONObject o) {
        try {
            JSONArray jsonArray = o.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                String className = jsonArray.getJSONObject(i).getString("className");
                long id = jsonArray.getJSONObject(i).getLong("dbid");
                Result r = core.cutTreeNode(className, id);
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                }
            }

            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void copy(JSONObject o) {
        try {
            JSONArray jsonArray = o.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                String className = jsonArray.getJSONObject(i).getString("className");
                long id = jsonArray.getJSONObject(i).getLong("dbid");
                Result r = core.copyTreeNode(className, id);
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                }
            }

            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, Objects.toString(o), e);
        }
    }

    private void paste(TreeFolder containerFolder) {
        try {
            Result r = core.pasteTreeNode(containerFolder);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());

            } else {
                JSMediator.showLockPanel(getSession());
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.hideLockPanel(getSession());
            }

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
        }
    }

    private void delete(JSONObject o) {
        try {
            JSONArray jsonArray = o.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                String className = jsonArray.getJSONObject(i).getString("className");
                long id = jsonArray.getJSONObject(i).getLong("dbid");
                Result r = core.removeTreeNode(className, id);
                if (r.isError()) {
                    JSMediator.alert(getSession(), r.getReason());
                }
            }

            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    public void addService() {
        Result r = core.getDefaultCrew();
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
            return;
        }

        r = core.addService(currentTreeFolder.getPath(),
                "Новая услуга", "img/icons/service.png", ((Crew) r.getObject()).getId());
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
        } else {
            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
        }

    }

    @Override
    public void addFolder() {
        Result r = core.addTreeFolder(currentTreeFolder, "Новая папка");
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
        } else {
            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
        }
    }

    public void addRightPanel(RightPanel panel) {
        sc = (ServiceCard) panel;
    }

    public void removeRightPanel(RightPanel panel) {
        //if (panel instanceof ServicesCommandmentsPanel) {
        //    scp = null;
        //}
    }

    @Override
    public String getLeafMenu(final TreeLeaf treeLeaf) {
        String model = "";
        MenuItem itemCut = new MenuItem(getSession(), "img/subbuttons/cut.png", "Вырезать");
        itemCut.setAction("getUICore().getAllSelectedNodes();");
        itemCut.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                try {
                    if (!evt.getJSONObject().has("data")) {
                        return;
                    }

                    if (evt.getJSONObject().getJSONArray("data").length() == 0) {
                        JSONArray array = new JSONArray();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("className", TreeLeafBasic.class.getName());
                        jsonObject.put("dbid", ((TreeLeafBasic) treeLeaf).getId());
                        array.put(jsonObject);
                        cut(evt.getJSONObject().put("data", array));

                    } else {
                        cut(evt.getJSONObject());
                    }
                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, htmlModel, e);
                }

            }
        });
        model += itemCut.getModel();

        MenuItem itemCopy = new MenuItem(getSession(), "img/subbuttons/copy.png", "Копировать");
        itemCopy.setAction("getUICore().getAllSelectedNodes();");
        itemCopy.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                try {
                    if (!evt.getJSONObject().has("data")) {
                        return;
                    }

                    if (evt.getJSONObject().getJSONArray("data").length() == 0) {
                        JSONArray array = new JSONArray();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("className", TreeLeafBasic.class.getName());
                        jsonObject.put("dbid", ((TreeLeafBasic) treeLeaf).getId());
                        array.put(jsonObject);
                        copy(evt.getJSONObject().put("data", array));

                    } else {
                        copy(evt.getJSONObject());
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                    Logger.getGlobal().log(Level.WARNING, htmlModel, e);
                }
            }
        });
        model += itemCopy.getModel();

        MenuItem itemPaste = new MenuItem(getSession(), "img/subbuttons/paste.png", "Вставить");
        itemPaste.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                paste(currentTreeFolder);
            }
        });
        if (!Clipboard.getInstance().isEmpty()) {
            model += itemPaste.getModel();
        }

        MenuItem itemDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {

                    @Override
                    public void pressed(int button) {
                        if(button == ConfirmPanel.YES){
                            TreeLeafBasic tl = (TreeLeafBasic) treeLeaf;
                    String json = "{data:[{dbid:" + tl.getId() + ", className:"
                            + TreeLeafBasic.class.getName() + "}]}";

                    try {
                        delete(new JSONObject(json));

                    } catch (Exception e) {
                        JSMediator.alert(getSession(), e.toString());
                    }
                        }
                    }
                }; 
                confirmPanel.setTitle("Вопрос");
                confirmPanel.setMessage("Удалить безвозвратно?");
                confirmPanel.showPanel();
            }
        });

        if (!core.isRadioButtonRuleAllow(getSession(), "canDeleteService", "deleteServiceDeny")) {
            model += itemDelete.getModel();
        }

        MenuItem mnuToProp = new MenuItem(getSession(), "img/subbuttons/settings.png", "Свойства");
        mnuToProp.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                String json = "{eventType:push, session:"+getSession()+", action:showRightPanel, ";
                TreeLeafBasic tl = (TreeLeafBasic) treeLeaf;
                json = json + "data:[{dbid:" + tl.getId() + ", className:" + TreeLeafBasic.class.getName() + "}]}";
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        ServiceCard.class.getName(), json);
            }
        });
        model += mnuToProp.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    @Override
    public String getFolderMenu(final TreeFolder folder) {
        String model = "";
        MenuItem itemCut = new MenuItem(getSession(), "img/subbuttons/cut.png", "Вырезать");
        itemCut.setAction("getUICore().getAllSelectedNodes();");
        itemCut.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                cut(evt.getJSONObject());
            }
        });
        model += itemCut.getModel();

        MenuItem itemCopy = new MenuItem(getSession(), "img/subbuttons/copy.png", "Копировать");
        itemCopy.setAction("getUICore().getAllSelectedNodes();");
        itemCopy.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                copy(evt.getJSONObject());
            }
        });
        model += itemCopy.getModel();

        MenuItem itemPaste = new MenuItem(getSession(), "img/subbuttons/paste.png", "Вставить");
        itemPaste.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                paste(currentTreeFolder);
            }
        });
        if (!Clipboard.getInstance().isEmpty()) {
            model += itemPaste.getModel();
        }

        MenuItem itemDelete = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemDelete.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {

                    @Override
                    public void pressed(int button) {
                        if(button == ConfirmPanel.YES){
                            TreeFolderBasic tf = (TreeFolderBasic) folder;
                    String json = "{data:[{dbid:" + tf.getId() + ", className:"
                            + TreeFolderBasic.class.getName() + "}]}";

                    try {
                        delete(new JSONObject(json));

                    } catch (Exception e) {
                        JSMediator.alert(getSession(), e.toString());
                    }
                        }
                    }
                }; 
                confirmPanel.setTitle("Вопрос");
                confirmPanel.setMessage("Удалить безвозвратно?");
                confirmPanel.showPanel();
            }
        });

        if (!core.isRadioButtonRuleAllow(getSession(), "canDeleteService", "deleteServiceDeny")) {
            model += itemDelete.getModel();
        }

        MenuItem mnuToProp = new MenuItem(getSession(), "img/subbuttons/settings.png", "Сменить картинку");
        mnuToProp.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                changeFolderImage(folder);
            }
        });
        model += mnuToProp.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    private void changeFolderImage(final TreeFolder treeFolder) {
        Path p = Paths.get(System.getProperty("user.home"), ".saas");
        ImageChooserPanel imageChooserPanel = new ImageChooserPanel(p.toString(), "img", "icons");
        imageChooserPanel.setTitle("Сменить картинку папки");
        imageChooserPanel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    treeFolder.setImageFileName(evt.getJSONObject().getString("src"));
                    Result r = core.createCompositeFolderImage(evt.getJSONObject().getString("src"));
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        return;
                    }
                    ((TreeFolderBasic) treeFolder).setCompositeImage("" + r.getObject());

                    r = core.modifyTreeFolderBasic((TreeFolderBasic) treeFolder);
                    if (r.isError()) {
                        JSMediator.alert(getSession(), r.getReason());
                        return;
                    }

                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

                } catch (JSONException e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });

        JSMediator.showImageChooser(getSession(), imageChooserPanel.getModel());
    }

    @Override
    public String getPanelMenu() {
        String model = "";
        MenuItem itemAddService = new MenuItem(getSession(), "img/subbuttons/add_service.png", "Добавить услугу");
        itemAddService.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                addService();
            }
        });
        model += itemAddService.getModel();

        MenuItem itemAddFolder = new MenuItem(getSession(), "img/subbuttons/folder_new.png", "Добавить папку");
        itemAddFolder.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                addFolder();
            }
        });
        model += itemAddFolder.getModel();

        MenuItem itemCopy = new MenuItem(getSession(), "img/subbuttons/copy.png", "Копировать");
        itemCopy.setAction("getUICore().getAllSelectedNodes();");
        itemCopy.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                copy(evt.getJSONObject());
            }
        });
        model += itemCopy.getModel();

        MenuItem itemPaste = new MenuItem(getSession(), "img/subbuttons/paste.png", "Вставить");
        itemPaste.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                paste(currentTreeFolder);
            }
        });
        //model += itemPaste.getModel();
        if (!Clipboard.getInstance().isEmpty()) {
            model += itemPaste.getModel();
        }


        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    @Override
    public String getIdentificator() {
        return ServicesPanel.class.getName();
    }

    @Override
    public void setIdentificator(String identificator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void moveDown(TreeFolder treeFolder) {
        currentTreeFolder = treeFolder;
        setNavigatorPanel();
        JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());

    }

    @Override
    public void navigatorButtonAction(UIEvent evt) {
        try {
            TreeFolder tfb = (TreeFolder) dataBase.getObject(
                    evt.getJSONObject().getString("className"),
                    evt.getJSONObject().getLong("dbid"));
            currentTreeFolder = tfb;
            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
            setNavigatorPanel();
            JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());

        } catch (JSONException e) {
            Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
        }
    }

    @Override
    public String getModel() {
        String model = refreshAndGetHTMLModel();
        if (model.trim().equals("")) {
            model = "<div identificator='" + getIdentificator() + "'>"
                    + "<table width='100%' height='100%'><tr><td valign='middle' align='center'>"
                    + "<div style='-webkit-border-radius: 16px; border: 1px dashed black; width:33%; font-size:80%; padding:15px;'>"
                    + "В этой папке услуг нет. Вы можети их добавить кнопкой"
                    + "<br/>"
                    + "<img src='img/icons/add_service.png'>"
                    + "<br/>"
                    + "<strong>«Добавить услугу»</strong>"
                    + "</div></td></tr></table>"
                    + "</div>";
        }
        return model;
    }

    @Override
    public synchronized void fireEvent(String json) {
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

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("updateWorkPanel")) {
                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.has("action")) {
                    if (jsonObject.getString("action").equals("showRightPanel")) {
                        WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                                ServiceCard.class.getName(), json);
                    }
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showProperties")) {
                    isLinkToGoods.set(false);
                    currentTreeFolder = (TreeFolderBasic) dataBase.getObject(
                            TreeFolderBasic.class.getName(),
                            jsonObject.getLong("treeFolderId"));
                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                    JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());

                    String jsonPush = "{eventType:push, session:"+getSession()+", action:showRightPanel, ";
                    jsonPush = jsonPush + "data:[{dbid:" + jsonObject.getLong("treeLeafId") + ", "
                            + "className:" + TreeLeafBasic.class.getName() + "}]}";
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            ServiceCard.class.getName(), jsonPush);

                    selectedTreeLeafId.set(jsonObject.getLong("treeLeafId"));
                }
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Услуги");
                if (isLinkToGoods.get()) {
                    TreeFolderBasic goodsTreeFolder = (TreeFolderBasic) WebKitEventBridge.getInstance().lookupInvoke(
                            getSession(),
                            "ru.sibek.plugin.goods.GoodsPanel",
                            "getCurrentTreeFolder",
                            new Object[]{});
                    currentTreeFolder = (TreeFolderBasic) dataBase.getObject(
                            TreeFolderBasic.class.getName(),
                            goodsTreeFolder.getId());
                }

                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());

                if (!isLinkToGoods.get()) {
                    JSMediator.selectedTreeLeaf(getSession(), "org.ubo.tree.TreeLeafBasic", selectedTreeLeafId.get());
                    isLinkToGoods.set(true);
                }
            }

            if (jsonObject.getString("eventType").equals("rightclick")) {
                JSMediator.setContextMenu(getSession(), getPanelMenu());
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

            if (rb.getActionName().equals("delete")
                    && !core.isRadioButtonRuleAllow(getSession(), "canDeleteService", "deleteServiceDeny")) {
                model += "<td align='left' valign='middle'>" + rb.getModel() + "</td>";

            } else {
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
