/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 20.12.2010 (C) Copyright by Zubanov Dmitry
 */
package ru.sibek.plugin.goods;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.proxy.KnowsId;
import org.ubo.goods.Goods;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.partner.Agent;
import org.ubo.rules.RuleItem;
import org.ubo.rules.SelectorRuleItem;
import org.ubo.service.Service;
import org.ubo.tree.*;
import org.ubo.utils.Result;
import org.uui.component.*;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerFolder;
import org.uui.explorer.ExplorerLeaf;
import org.uui.explorer.ExplorerPanel;
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
import ru.sibek.core.ui.PopupPanel;


public class GoodsPanel extends ExplorerPanel implements HasWorkPanelToolbars, HasRules {

    private ArrayList<RibbonButton> toolbarButtons;
    private CarssierCore core;
    private DataBase dataBase;
    private AtomicBoolean isLinkToService = new AtomicBoolean(true);
    private AtomicLong selectedTreeLeafId = new AtomicLong();
    private GoodsExchange goodsExchange;

    public GoodsPanel(String sessionId, DataBase dataBase) {
        super(sessionId, dataBase);
        goodsExchange = GoodsExchange.getInstance();
        this.dataBase = dataBase;
        core = CarssierCore.getInstance();
        init();
        initToolbar();
        setFilter(ExplorerPanel.FILTER_HIDE, Service.class.getName());
    }

    @Override
    public String treeWalker(TreeFolder treeFolder) {
        goodsExchange.setCurrentTreeFolder((TreeFolderBasic)treeFolder);
        
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
                                                    GoodsCard.class.getName(),
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
    public ArrayList<RuleItem> getRuleItemsTemplate(int userSystemId) {
        ArrayList<RuleItem> listRulesItem = new ArrayList<>();

        LinkedList<SelectorRuleItem> selectorRuleItemList = new LinkedList<>();
        SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("deleteGoodsDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("deleteGoodsAllow");
        selectorRuleItemList.add(selectorRuleItem);
        int select = userSystemId == 0 ? 1 : 0;
        RuleItem ruleItem = RuleItem.newRuleItemRadio("canDeleteGoods",
                "Удаление товаров:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        selectorRuleItemList = new LinkedList<>();
        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Запрещено");
        selectorRuleItem.setKey("storageEasyModeDeny");
        selectorRuleItemList.add(selectorRuleItem);

        selectorRuleItem = new SelectorRuleItem();
        selectorRuleItem.setDescription("Разрешено");
        selectorRuleItem.setKey("storageEasyModeAllow");
        selectorRuleItemList.add(selectorRuleItem);
        select = userSystemId == 0 ? 1 : 0;
        ruleItem = RuleItem.newRuleItemRadio("canEasyWorkWithStorage",
                "Прием / списание товаров со склада:", selectorRuleItemList, select);
        listRulesItem.add(ruleItem);

        return listRulesItem;
    }

    private void init() {
        Result r = core.getTreeGoodsAndService();
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());

        } else {
            currentTreeFolder = ((TreeBasic) r.getObject()).getRootFolder();
        }
    }

    @Override
    public String getLeafMenu(final TreeLeaf leaf) {
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
                        jsonObject.put("dbid", ((TreeLeafBasic) leaf).getId());
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
                        jsonObject.put("dbid", ((TreeLeafBasic) leaf).getId());
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
                        if (button == ConfirmPanel.YES) {
                            TreeLeafBasic tl = (TreeLeafBasic) leaf;
                            String json = "{data:[{dbid:" + tl.getId() + ", className:" + TreeLeafBasic.class.getName() + "}]}";
                            try {
                                delete(new JSONObject(json));
                            } catch (JSONException ex) {
                                Logger.getLogger(GoodsPanel.class.getName()).log(Level.SEVERE, null, ex);
                                JSMediator.alert(getSession(), ex.toString());
                            }
                        }
                    }
                };
                confirmPanel.setTitle("Вопрос");
                confirmPanel.setMessage("Удалить безвозвратно?");
                confirmPanel.showPanel();
            }
        });

        if (!core.isRadioButtonRuleAllow(getSession(), "canDeleteGoods", "deleteGoodsDeny")) {
            model += itemDelete.getModel();
        }

        MenuItem mnuToProp = new MenuItem(getSession(), "img/subbuttons/settings.png", "Свойства");
        mnuToProp.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                String json = "{eventType:push, session:"+getSession()+", action:showRightPanel, ";
                TreeLeafBasic tl = (TreeLeafBasic) leaf;
                json = json + "data:[{dbid:" + tl.getId() + ", className:" + TreeLeafBasic.class.getName() + "}]}";
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                        GoodsCard.class.getName(), json);
            }
        });
        model += mnuToProp.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    @Override
    public String getFolderMenu(final TreeFolder node) {
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
                        if (button == ConfirmPanel.YES) {
                            TreeFolderBasic tf = (TreeFolderBasic) node;
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

        if (!core.isRadioButtonRuleAllow(getSession(), "canDeleteGoods", "deleteGoodsDeny")) {
            model += itemDelete.getModel();
        }

        MenuItem mnuToProp = new MenuItem(getSession(), "img/subbuttons/settings.png", "Свойства");
        mnuToProp.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(org.uui.event.UIEvent evt) {
                changeFolderImage(node);
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

        try {
            JSMediator.showImageChooser(getSession(), imageChooserPanel.getModel());

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            JSMediator.alert(getSession(), e.toString());
        }
    }

    @Override
    public String getPanelMenu() {
        String model = "";

        MenuItem itemAddGoods = new MenuItem(getSession(), "img/subbuttons/goods_new.png", "Добавить товар");
        itemAddGoods.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                addGoods();
            }
        });
        model += itemAddGoods.getModel();

        MenuItem itemAddFolder = new MenuItem(getSession(), "img/subbuttons/folder_new.png", "Добавить папку");
        itemAddFolder.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                addFolder();
            }
        });
        model += itemAddFolder.getModel();

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

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    private void createDefaultSupplier() {
        TextField txtSupplier = new TextField(getSession());
        txtSupplier.setStyle("width:96%;");

        TextField txtAddress = new TextField(getSession());
        txtAddress.setStyle("width:96%;");

        TextField txtPhone = new TextField(getSession());
        txtPhone.setStyle("width:96%;");

        PopupPanel popupPanel = new PopupPanel(getSession());
        popupPanel.setTitle("Создание поставщика");
        String panel = "<p>Для работы с товарами необходимо указать "
                + "поставщика товаров/услуг по умолчанию.</p>"
                + "<p>Здесь имеется в виду ваша компания. "
                + "В полях расположенном ниже введите "
                + "краткое название свой компании, "
                + "адрес и телефон.</p>"
                + "<span style='font-size:70%'>"
                + "В дальнейшем эту информацию можно будет отредактировать "
                + "в разделе <strong>«Справочники»</strong>"
                + "</span><br/><br/>"
                + "Краткое название<br/>"
                + txtSupplier.getModel() + "<br/>"
                + "Адрес<br/>"
                + txtAddress.getModel() + "<br/>"
                + "Телефон<br/>"
                + txtPhone.getModel();
        popupPanel.setPanel(panel);

        boolean isAdded = false;
        while (!isAdded) {
            popupPanel.showPanel();
            if (!txtSupplier.getText().equals("")) {
                isAdded = true;
            } else {
                panel = "<p>Для работы с товарами необходимо указать "
                        + "поставщика товаров/услуг по умолчанию.</p>"
                        + "<p>Здесь имеется в виду ваша компания. "
                        + "В полях расположенном ниже введите "
                        + "краткое название свой компании,"
                        + "адрес и телефон.</p>"
                        + "<span style='font-size:70%'>"
                        + "В дальнейшем эту информацию можно будет отредактировать "
                        + "в разделе <strong>«Справочники»</strong>"
                        + "</span><br/><br/>"
                        + "Краткое название <span style='color:red; font-size:60%;'>Не может быть пустым!</span><br/>"
                        + txtSupplier.getModel() + "<br/>"
                        + "Адрес<br/>"
                        + txtAddress.getModel() + "<br/>"
                        + "Телефон<br/>"
                        + txtPhone.getModel();
                popupPanel.setPanel(panel);
            }
        }

        Result r = core.addAgent(txtSupplier.getText(), txtSupplier.getText(),
                txtAddress.getText(), txtPhone.getText());
        if (!r.isError()) {
            Agent a = (Agent) r.getObject();
            core.setDefaultSupplier(a);

        } else {
            try {
                JSMediator.alert(getSession(), r.getReason());
            } catch (Exception e) {
            }
        }
    }

    private void addGoods() {
        Result r = core.getDefaultSupplier();
        if (r.isError()) {
            createDefaultSupplier();
        }

        try {
            r = core.addGoods(currentTreeFolder.getPath(),
                    "Новый товар", "img/icons/goods.png");
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
                return;

            }

            Goods goods = (Goods) r.getObject();
            r = core.addGoodsToStorage(goods.getId(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
                return;
            }
            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    @Override
    public void addFolder() {
        try {
            Result r = core.addTreeFolder(currentTreeFolder, "Новая папка");
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());

            } else {
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
            }

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
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

    private void initToolbar() {
        toolbarButtons = new ArrayList<>();

        // Add goods
        RibbonButton rbAddGoods = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/icons/add_goods.png",
                ResourceBundle.getBundle("GoodsPanel").getString("Add_goods"),
                "addGoods");
        rbAddGoods.addRibbonButtonEventListener(new RibbonButtonEventListener() {
            @Override
            public void event(RibbonEvent evt) {
                addGoods();
            }
        });

        toolbarButtons.add(rbAddGoods);

        // Add folder
        RibbonButton rbAddFolder = RibbonButton.createDefaultRibbonButton(getSession(), 
                "img/subbuttons/folder_new.png",
                ResourceBundle.getBundle("GoodsPanel").getString("Add_folder"),
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
                ResourceBundle.getBundle("GoodsPanel").getString("paste"),
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
                ResourceBundle.getBundle("GoodsPanel").getString("delete"),
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

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return toolbarButtons;
    }

    @Override
    public String getIdentificator() {
        return GoodsPanel.class.getName();
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
                    + "В этой папке товаров нет. Вы можети их добавить кнопкой"
                    + "<br/>"
                    + "<img src='img/icons/goods24.png'>"
                    + "<br/>"
                    + "<strong>«Добавить товар»</strong>"
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
                if (jsonObject.getString("action").equals("showRightPanel")) {
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            GoodsCard.class.getName(), json);
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("updateWorkPanel")) {
                    JSMediator.setWorkPanel(getSession(), getModel());
                }
            }

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showProperties")) {
                    isLinkToService.set(false);
                    currentTreeFolder = (TreeFolderBasic) dataBase.getObject(
                            TreeFolderBasic.class.getName(),
                            jsonObject.getLong("treeFolderId"));

                    JSMediator.setWorkPanel(getSession(), getModel());
                    JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());

                    String jsonPush = "{eventType:push, session:"+getSession()+", action:showRightPanel, ";
                    jsonPush = jsonPush + "data:[{dbid:" + jsonObject.getLong("treeLeafId") + ", "
                            + "className:" + TreeLeafBasic.class.getName() + "}]}";
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            GoodsCard.class.getName(), jsonPush);
                    selectedTreeLeafId.set(jsonObject.getLong("treeLeafId"));
                }
            }

            if (jsonObject.getString("eventType").equals("rightclick")) {
                JSMediator.setContextMenu(getSession(), getPanelMenu());
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Товары");
                
                if (isLinkToService.get()) {
                    TreeFolderBasic serviceTree = (TreeFolderBasic) WebKitEventBridge.getInstance().lookupInvoke(
                            getSession(),
                            "ru.sibek.plugin.service.ServicesPanel",
                            "getCurrentTreeFolder",
                            new Object[]{});
                    currentTreeFolder = (TreeFolderBasic) dataBase.getObject(
                            TreeFolderBasic.class.getName(),
                            serviceTree.getId());
                }


                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());

                if (!isLinkToService.get()) {
                    JSMediator.selectedTreeLeaf(getSession(),
                            "org.ubo.tree.TreeLeafBasic",
                            selectedTreeLeafId.get());
                    isLinkToService.set(true);
                }

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
                    && !core.isRadioButtonRuleAllow(getSession(), "canDeleteGoods", "deleteGoodsDeny")) {
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
    public TreeFolder getRoot() {
        TreeFolder rootFolder = null;

        Result r = core.getTreeGoodsAndService();
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
        } else {
            rootFolder = ((TreeBasic) r.getObject()).getRootFolder();
        }

        return rootFolder;
    }
}
