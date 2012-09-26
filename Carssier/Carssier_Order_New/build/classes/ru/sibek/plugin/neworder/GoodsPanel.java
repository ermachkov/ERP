package ru.sibek.plugin.neworder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.proxy.KnowsId;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.rules.Rule;
import org.ubo.rules.Rules;
import org.ubo.tree.*;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.MenuItem;
import org.uui.component.ViewSwitcher;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerFolder;
import org.uui.explorer.ExplorerLeaf;
import org.uui.explorer.ExplorerPanel;
import org.uui.explorer.TreeExplorerPanel;
import org.uui.plugin.HasWorkPanelToolbars;
import org.uui.ribbon.RibbonButton;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.plugin.neworder.util.DataBaseFiller;

public class GoodsPanel extends TreeExplorerPanel implements HasWorkPanelToolbars {

    private DataBase dataBase;
    private ArrayList<RibbonButton> toolbarButtons;
    private CarssierCore core;
    private GoodsBasketPanel goodsBasketPanel;
    private Button btnAutoFill;
    private ViewSwitcher viewSwitcher;
    private ArrayList<MacTableModel> macPanels = new ArrayList<>();
    private boolean macModeSilent = false;
    private MacTableModel macTableModelTableView;
    private long TIMESTAMP;

    public GoodsPanel(String sessionId, DataBase dataBase) {
        super(sessionId, dataBase, "GoodsAndService");
        TIMESTAMP = new Date().getTime();
        this.dataBase = dataBase;
        this.core = CarssierCore.getInstance();
        initToolbar();
        this.btnAutoFill = new Button(getSession(), "Заполнить базу данных");
        this.btnAutoFill.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    new DataBaseFiller(getMe()).start();
                } catch (IOException ex) {
                    JSMediator.alert(getSession(), ex.toString());
                }
            }
        });

        viewSwitcher = new ViewSwitcher(sessionId);
        viewSwitcher.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    switchView(viewSwitcher.getSelectedView());
                    if (viewSwitcher.getSelectedView() == ExplorerPanel.PANEL_MAC) {
                        Path p = Paths.get(currentTreeFolder.getPath());
                        currentTreeFolder = getRoot();
                        macPanels.clear();
                        macModeSilent = true;
                        refreshAndGetHTMLModel();

                        for (int i = 1; i < p.getNameCount(); i++) {
                            for (TreeFolder tf : currentTreeFolder.getSetTreeFolder()) {
                                if (tf.getName().equals(p.getName(i).toString())) {
                                    moveDown(tf);
                                    addMacTable();
                                    break;
                                }
                            }
                        }

                        refreshMacTable();
                        macModeSilent = false;

                    } else {
                        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                    }

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, htmlModel, e);
                }

            }
        });

        MacTableHeaderModel mth = new MacTableHeaderModel();
        MacHeaderColumn imgColumn = new MacHeaderColumn("", String.class, false);
        imgColumn.setColumnWidth("36");
        mth.addHeaderColumn(imgColumn);
        mth.addHeaderColumn(new MacHeaderColumn("Имя", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Цена / Кол-во", String.class, false));

        macTableModelTableView = new MacTableModel(getSession());
        macTableModelTableView.setNavigatorEnable(false);
        macTableModelTableView.setNavigatorShowingAlways(false);
        macTableModelTableView.setNavigatorDateSelectorEnabled(false);

        macTableModelTableView.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                Object obj = macTableModelTableView.getRow(macTableModelTableView.getSelectedRow()).getValue();
                if (obj instanceof TreeFolder) {
                    moveDown((TreeFolder) obj);
                    JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                    JSMediator.setViewSwitcher(getSession(), viewSwitcher.getModel());
                }

                if (obj instanceof TreeLeaf) {
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            GoodsBasketPanel.class.getName(),
                            "{eventType:push, session:\"" + getSession() + "\", action:showRightPanel, data:["
                            + "{className:" + TreeLeafBasic.class.getName() + ", "
                            + "dbid:" + ((TreeLeafBasic) obj).getId() + "}]}");
                }
            }
        });

        macTableModelTableView.setHeader(mth);
        macTableModelTableView.setCssClass("leftMacTable");
        macTableModelTableView.setId("orderNewTable");
    }

    private void addMacTable() {
        Set<TreeFolder> folders = getCurrentTreeFolder().getSetTreeFolder();
        Set<TreeLeaf> leaves = getCurrentTreeFolder().getSetTreeLeaf();

        final MacTableModel macTableModel = new MacTableModel(getSession());
        macTableModel.setNavigatorEnable(false);
        macTableModel.setNavigatorShowingAlways(false);
        macTableModel.setNavigatorDateSelectorEnabled(false);

        macTableModel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                Object obj = macTableModel.getRow(macTableModel.getSelectedRow()).getValue();
                if (obj instanceof TreeFolder) {
                    moveDown((TreeFolder) obj);

                    Path p = Paths.get(((TreeFolder) obj).getPath());
                    ArrayList<MacTableModel> removeList = new ArrayList<>();
                    for (int i = 0; i < macPanels.size(); i++) {
                        if (i > p.getNameCount() - 2) {
                            removeList.add(macPanels.get(i));
                        }
                    }
                    macPanels.removeAll(removeList);

                    addMacTable();
                }

                if (obj instanceof TreeLeaf) {
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            GoodsBasketPanel.class.getName(),
                            "{eventType:push, session:\"" + getSession() + "\", action:showRightPanel, data:["
                            + "{className:" + TreeLeafBasic.class.getName() + ", "
                            + "dbid:" + ((TreeLeafBasic) obj).getId() + "}]}");
                }
            }
        });

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Имя", String.class, false));

        macTableModel.setHeader(mth);
        macTableModel.setCssClass("leftMacTable");

        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (TreeFolder folder : folders) {
            MacTableRow row = new MacTableRow();
            row.setValue(folder);
            row.addCell(new MacTableCell(getSession(), "<img src='" + folder.getImageFileName() + "' "
                    + "style='vertical-align: middle;' width='32' height='32'/>"
                    + "&nbsp;"
                    + folder.getName(),
                    false));
            rows.add(row);
        }

        for (TreeLeaf leaf : leaves) {
            MacTableRow row = new MacTableRow();
            row.setValue(leaf);
            row.addCell(new MacTableCell(getSession(), "<img src='" + leaf.getImageFileName() + "' "
                    + "style='vertical-align: middle;' width='32' height='32'/>"
                    + "&nbsp;"
                    + leaf.getName(), false));
            rows.add(row);
        }

        macTableModel.setData(rows);
        macPanels.add(macTableModel);

        try {
            if (!macModeSilent) {
                refreshMacTable();
            }

        } catch (Exception e) {
            JSMediator.alert(getSession(), e.toString());
        }
    }

    private void refreshMacTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table id='scrollMacTable' width='100%' height='100%' "
                + "cellpadding='0' cellspacing='0'><tr>");
        for (MacTableModel s : macPanels) {
            sb.append("<td width='25%' valign='top' style='border-right: 1px solid black'>");
            sb.append(s.getModel());
            sb.append("</td>");
        }
        sb.append("</tr></table></div>");

        JSMediator.setWorkPanel(getSession(), sb.toString());
        JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
        setNavigatorPanel();
        JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
        JSMediator.setViewSwitcher(getSession(), viewSwitcher.getModel());
    }

    @Override
    public String treeMacWalker(TreeFolder treeFolder) {
        Set<TreeFolder> folders = treeFolder.getSetTreeFolder();
        Set<TreeLeaf> leaves = treeFolder.getSetTreeLeaf();

        final MacTableModel macTableModel = new MacTableModel(getSession());
        macTableModel.setNavigatorEnable(false);
        macTableModel.setNavigatorShowingAlways(false);
        macTableModel.setNavigatorDateSelectorEnabled(false);

        macTableModel.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                Object obj = macTableModel.getRow(macTableModel.getSelectedRow()).getValue();
                if (obj instanceof TreeFolder) {
                    moveDown((TreeFolder) obj);
                    Path p = Paths.get(((TreeFolder) obj).getPath());
                    ArrayList<MacTableModel> removeList = new ArrayList<>();
                    for (int i = 0; i < macPanels.size(); i++) {
                        if (i > p.getNameCount() - 2) {
                            removeList.add(macPanels.get(i));
                        }
                    }
                    macPanels.removeAll(removeList);
                    addMacTable();
                }

                if (obj instanceof TreeLeaf) {
                    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                            GoodsBasketPanel.class.getName(),
                            "{eventType:push, session:\"" + getSession() + "\", action:showRightPanel, data:["
                            + "{className:" + TreeLeafBasic.class.getName() + ", "
                            + "dbid:" + ((TreeLeafBasic) obj).getId() + "}]}");
                }
            }
        });

        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Имя", String.class, false));

        macTableModel.setHeader(mth);
        macTableModel.setCssClass("leftMacTable");
        macTableModel.setId("orderNewTable");

        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (TreeFolder folder : folders) {
            MacTableRow row = new MacTableRow();
            row.setValue(folder);
            row.addCell(new MacTableCell(getSession(), "<img src='" + folder.getImageFileName() + "' "
                    + "style='vertical-align: middle;' width='32' height='32'/>"
                    + "&nbsp;" + folder.getName(),
                    false));
            rows.add(row);
        }

        for (TreeLeaf leaf : leaves) {
            MacTableRow row = new MacTableRow();
            row.setValue(leaf);
            row.addCell(new MacTableCell(getSession(), "<img src='" + leaf.getImageFileName() + "' "
                    + "style='vertical-align: middle;' width='32' height='32'/>"
                    + "&nbsp;"
                    + leaf.getName(),
                    false));
            rows.add(row);
        }

        macTableModel.setData(rows);
        macPanels.clear();
        macPanels.add(macTableModel);

        return macTableModel.getModel();
    }

    @Override
    public String treeTableWalker(TreeFolder treeFolder) {
        WebKitEventBridge.getInstance().cleanMacTable(getSession(), macTableModelTableView);

        Set<TreeFolder> folders = treeFolder.getSetTreeFolder();
        Set<TreeLeaf> leaves = treeFolder.getSetTreeLeaf();

        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (TreeFolder folder : folders) {
            MacTableRow row = new MacTableRow();
            row.setValue(folder);
            row.addCell(new MacTableCell(getSession(), "<img src='" + folder.getImageFileName() + "' "
                    + "style='vertical-align: middle;' width='32' height='32'/>", false));
            row.addCell(new MacTableCell(getSession(), folder.getName(), false));
            row.addCell(new MacTableCell(getSession(), "", false));
            rows.add(row);
        }

        for (TreeLeaf leaf : leaves) {
            MacTableRow row = new MacTableRow();
            row.setValue(leaf);
            row.addCell(new MacTableCell(getSession(), "<img src='" + leaf.getImageFileName() + "' "
                    + "style='vertical-align: middle;' width='32' height='32'/>", false));
            row.addCell(new MacTableCell(getSession(), leaf.getName(), false));
            row.addCell(new MacTableCell(getSession(), "<div align='right'>"
                    + core.getPriceCountForSales((TreeLeafBasic) leaf)
                    + "</div>", false));
            rows.add(row);
        }

        macTableModelTableView.setData(rows);
        //1345654099598
        //1345654099598

        return macTableModelTableView.getModel();
    }

    @Override
    public String treeWalker(TreeFolder treeFolder) {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for(StackTraceElement s : ste){
            System.out.println(s);
        }
        
        System.out.println("+++++++++++++++++++++ " + TIMESTAMP + "++++++++++++++++++++++");
        
        StringBuilder sb = new StringBuilder();

        Set<TreeFolder> folders = treeFolder.getSetTreeFolder();
        Set<TreeLeaf> leaves = treeFolder.getSetTreeLeaf();

        Set<ExplorerFolder> eFolders = new LinkedHashSet<>();
        Set<ExplorerLeaf> eLabels = new LinkedHashSet<>();

        for (TreeFolder tf : folders) {
            ExplorerFolder folder = new ExplorerFolder(getSession(), tf.getClass().getName(),
                    ((KnowsId) tf).getId(), tf.getName(), tf.getImageFileName());

            folder.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            TreeFolder selectedTreeFolder = (TreeFolder) db.getObject(
                                    evt.getJSONObject().getString("className"),
                                    evt.getJSONObject().getLong("dbid"));

                            JSMediator.setContextMenu(getSession(), getFolderMenu(selectedTreeFolder));
                        }

                        if (evt.getJSONObject().getString("eventType").equals("click")) {
                            TreeFolder selectedTreeFolder = (TreeFolder) db.getObject(
                                    evt.getJSONObject().getString("className"),
                                    evt.getJSONObject().getLong("dbid"));

                            moveDown(selectedTreeFolder);

                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                            JSMediator.setViewSwitcher(getSession(), viewSwitcher.getModel());
                        }

                        if (evt.getJSONObject().getString("eventType").equals("drop")) {
                            cutAndPaste(evt.getJSONObject());
                            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
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

        for (TreeLeaf tl : leaves) {
            boolean isContinue = false;
            if ((this.filterClasses != null)
                    && (this.isFilterEnable)) {
                String contClass = tl.getContainer().getClassName();
                for (String cls : this.filterClasses) {
                    if (cls.equals(contClass)) {
                        isContinue = true;
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

            String info = core.getPriceCountForSales((TreeLeafBasic) tl);
            ExplorerLeaf label = new ExplorerLeaf(getSession(), tl.getClass().getName(), ((KnowsId) tl).getId(), tl.getName(), img);

            label.setExtraText(info);
            label.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            TreeLeaf selectedTreeLeaf = (TreeLeaf) db.getObject(evt.getJSONObject().getString("className"), evt.getJSONObject().getLong("dbid"));

                            JSMediator.setContextMenu(getSession(), getLeafMenu(selectedTreeLeaf));
                        }

                        if (evt.getJSONObject().getString("eventType").equals("keypress")) {
                            rename(evt.getJSONObject());
                        }
                    } catch (JSONException e) {
                        Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
                    }
                }
            });
            eLabels.add(label);
        }

        Logger.getGlobal().log(Level.INFO, "htmlModel = {0}", this.htmlModel);

        for (ExplorerFolder ef : eFolders) {
            sb.append(ef.getModel());
        }

        for (ExplorerLeaf el : eLabels) {
            sb.append(el.getModel());
        }

        setNavigatorPanel();

        return sb.toString();
    }

    private GoodsPanel getMe() {
        return this;
    }

    public void showWorkPanel() {
        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
    }

    public void showProperties() {
    }

    @Override
    public String getLeafMenu(final TreeLeaf node) {
        String model = "";

        MenuItem mnuToOrder = new MenuItem(getSession(), "img/subbuttons/import.png", "Добавить в заказ");
        mnuToOrder.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                String json = "{eventType:push, session:"+getSession()+", action:showRightPanel, ";
                if ((node instanceof TreeFolder)) {
                    TreeFolderBasic tf = (TreeFolderBasic) node;
                    json = json + "data:[{dbid:" + tf.getId() + ", className:" + TreeFolderBasic.class.getName() + "}]}";
                } else if ((node instanceof TreeLeaf)) {
                    TreeLeafBasic tl = (TreeLeafBasic) node;
                    json = json + "data:[{dbid:" + tl.getId() + ", className:" + TreeLeafBasic.class.getName() + "}]}";
                }
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), GoodsBasketPanel.class.getName(), json);
            }
        });
        model = model + mnuToOrder.getModel();

        MenuItem mnuProperties = new MenuItem(getSession(), "img/subbuttons/settings.png", "Свойства");
        mnuProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                try {
                    if ((node instanceof TreeLeaf)) {
                        String moduleClassName;
                        String module;
                        if (node.getContainer().getClassName().indexOf("Goods") != -1) {
                            module = "'Справочники', 'Товары'";
                            moduleClassName = "ru.sibek.plugin.goods.GoodsPanel";
                        } else {
                            module = "'Справочники', 'Услуги'";
                            moduleClassName = "ru.sibek.plugin.service.ServicesPanel";
                        }

                        //boolean result = WebKitFrame.getInstance().browserExecutor("getUICore().switchModule(" + module + ");");

                        //if (result) {
                        //    WebKitEventBridge.getInstance().pushEventToComponent(getSession(), moduleClassName, "{eventType:push, session:"+getSession()+", action:showProperties, treeFolderId:" + ((TreeFolderBasic) currentTreeFolder).getId() + "," + "treeLeafId:" + ((TreeLeafBasic) node).getId() + "}");
                        //}
                    }

                } catch (Exception e) {
                    JSMediator.alert(getSession(), e.toString());
                }
            }
        });
        Result r = this.core.getRulesForUser(this.core.getLoggedUser(getSession()).getId());
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
            return model;
        }

        if ((node instanceof TreeLeaf)) {
            for (Rule rule : ((Rules) r.getObject()).getRules()) {
                if (node.getContainer().getClassName().indexOf("Goods") != -1) {
                    if ((rule.getModuleClassName().equals("ru.sibek.plugin.goods.GoodsUUIPlugin"))
                            && (rule.isAllowToUse())) {
                        model = model + mnuProperties.getModel();
                    }

                } else if ((rule.getModuleClassName().equals("ru.sibek.plugin.service.ServicesUUIPlugin"))
                        && (rule.isAllowToUse())) {
                    model = model + mnuProperties.getModel();
                }

            }

        }

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model = model + itemCancel.getModel();

        return model;
    }

    public void addBasketPanel(GoodsBasketPanel goodsBasketPanel) {
        this.goodsBasketPanel = goodsBasketPanel;
    }

    private void addToOrder() {
        ArrayList list = new ArrayList();
        for (TreeNode treeNode : getSelectedNodes()) {
            if ((treeNode instanceof TreeLeaf)) {
                list.add((TreeLeaf) treeNode);
            }
        }
        this.goodsBasketPanel.addToOrder(list);
    }

    @Override
    public void drag(List<TreeNode> nodes) {
    }

    @Override
    public void doubleClicked(TreeNode node) {
        ArrayList list = new ArrayList();
        if ((node instanceof TreeLeaf)) {
            list.add((TreeLeaf) node);
            this.goodsBasketPanel.addToOrder(list);
        }
    }

    @Override
    public String getFolderMenu(TreeFolder node) {
        String model = "";
        MenuItem mnuProperties = new MenuItem(getSession(), "Свойства");

        mnuProperties.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                System.out.println("AAAAAAAAAAAAAAAAAAAAAA");
            }
        });
        model = model + mnuProperties.getModel();

        return model;
    }

    @Override
    public String getPanelMenu() {
        String model = "";

        return model;
    }

    private void addGoods() {
        try {
            Result r = this.core.addGoods(this.currentTreeFolder.getPath(), "Новый товар", "img/icons/goods.png");

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
    public void addFolder() {
        try {
            Result r = this.core.addTreeFolder(this.currentTreeFolder, "Новая папка");
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
    public void cutAndPaste(JSONObject o) {
        cut(o);
        try {
            TreeFolder f = (TreeFolder) this.db.getObject(o.getString("className"), o.getLong("dbid"));
            paste(f);
        } catch (Exception e) {
            this.core.clearClipboard();
            JSMediator.alert(getSession(), e.toString());
        }
    }

    private void cut(JSONObject o) {
        try {
            JSONArray jsonArray = o.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                String className = jsonArray.getJSONObject(i).getString("className");
                long id = jsonArray.getJSONObject(i).getLong("dbid");
                Result r = this.core.cutTreeNode(className, id);
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
                Result r = this.core.copyTreeNode(className, id);
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
            Result r = this.core.pasteTreeNode(containerFolder);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());

            } else {
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
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
                Result r = this.core.removeTreeNode(className, id);
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
        this.toolbarButtons = new ArrayList();
    }

    @Override
    public List<RibbonButton> getWorkpanelToolbars() {
        return this.toolbarButtons;
    }

    @Override
    public String getIdentificator() {
        return GoodsPanel.class.getName();
    }

    @Override
    public void moveDown(TreeFolder treeFolder) {
        this.currentTreeFolder = treeFolder;
        setNavigatorPanel();
        JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
    }

    @Override
    public void navigatorButtonAction(UIEvent evt) {
        try {
            TreeFolder tfb = (TreeFolder) dataBase.getObject(
                    evt.getJSONObject().getString("className"),
                    evt.getJSONObject().getLong("dbid"));

            Path p = Paths.get(tfb.getPath());
            ArrayList<MacTableModel> removeList = new ArrayList<>();
            for (int i = 0; i < macPanels.size(); i++) {
                if (i > p.getNameCount() - 2) {
                    removeList.add(macPanels.get(i));
                }
            }
            macPanels.removeAll(removeList);

            this.currentTreeFolder = tfb;

            if (viewMode == ExplorerPanel.PANEL_MAC) {
                p = Paths.get(currentTreeFolder.getPath());
                currentTreeFolder = getRoot();
                macPanels.clear();
                macModeSilent = true;
                refreshAndGetHTMLModel();
                for (int i = 1; i < p.getNameCount(); i++) {
                    for (TreeFolder tf : currentTreeFolder.getSetTreeFolder()) {
                        if (tf.getName().equals(p.getName(i).toString())) {
                            moveDown(tf);
                            addMacTable();
                            break;
                        }
                    }
                }

                macModeSilent = false;
                refreshMacTable();

            } else {
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
                setNavigatorPanel();
                JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
                JSMediator.setViewSwitcher(getSession(), viewSwitcher.getModel());
            }

        } catch (JSONException e) {
            Logger.getGlobal().log(Level.WARNING, evt.toString(), e);
        }
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

            super.setSession(jsonObject.getString("session"));

            JSMediator.setExplorerEditableMode(getSession(), false);

            if ((jsonObject.getString("eventType").equals("push"))
                    && (jsonObject.getString("action").equals("showRightPanel"))) {
                WebKitEventBridge.getInstance().pushEventToComponent(getSession(), GoodsBasketPanel.class.getName(), json);
            }

            if (jsonObject.getString("eventType").equals("rightclick")) {
                JSMediator.setContextMenu(getSession(), getPanelMenu());
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Оформить");
                
                String model = refreshAndGetHTMLModel();
                if ((this.tree.getRootFolder().getSetTreeFolder().isEmpty())
                        && (this.tree.getRootFolder().getSetTreeLeaf().isEmpty())) {
                    model = "<table width='100%' height='100%'><tr>"
                            + "<td align='center' valign='middle'>"
                            + "<div style='width:50%;height:50%;border-radius:18px;"
                            + "border-color:gray;border-style:dotted;border-width:3px;' "
                            + "align='center'><table width='100%' height='100%'>"
                            + "<tr><td align='center' valign='middle'>В данный момент "
                            + "база данных товаров и услуг не заполнена.<br/><br/>"
                            + "Что можно сделать?<br/>зайти в раздел <strong>«Справочники»</strong> "
                            + "и вручную заполнить базу данных.<br/><br/>или<br/><br/>"
                            + "заполнить базу данными по умолчанию<br/>"
                            + "<span style=font-size:70%;>(впоследствии их можно будет отредактировать)</span>"
                            + "<br/>"
                            + this.btnAutoFill.getModel() + "</td></tr></table>"
                            + "</div>"
                            + "</td>"
                            + "</tr>"
                            + "</table>";
                }

                if (viewMode == ExplorerPanel.PANEL_MAC) {
                    Path p = Paths.get(currentTreeFolder.getPath());
                    currentTreeFolder = getRoot();
                    macPanels.clear();
                    macModeSilent = true;
                    refreshAndGetHTMLModel();
                    for (int i = 1; i < p.getNameCount(); i++) {
                        for (TreeFolder tf : currentTreeFolder.getSetTreeFolder()) {
                            if (tf.getName().equals(p.getName(i).toString())) {
                                moveDown(tf);
                                addMacTable();
                                break;
                            }
                        }
                    }

                    macModeSilent = false;
                    refreshMacTable();

                } else {
                    JSMediator.setWorkPanel(getSession(), model);
                    JSMediator.setSubOperationButton(getSession(), getSubOperationButtonModel());
                    JSMediator.setNavigatorPanel(getSession(), getNavigatorModel());
                    JSMediator.setViewSwitcher(getSession(), viewSwitcher.getModel());
                }
            }

        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    private String getSubOperationButtonModel() {
        String model = "<div>";
        for (RibbonButton rb : getWorkpanelToolbars()) {
            model = model + rb.getModel();
        }
        model = model + "</div>";
        return model;
    }
}
