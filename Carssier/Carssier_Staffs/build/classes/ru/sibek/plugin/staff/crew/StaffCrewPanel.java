/*
 * UsersPanel.java
 *
 * Created on 16.12.2010, 17:22:35
 */
package ru.sibek.plugin.staff.crew;

import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.proxy.KnowsId;
import org.ubo.employee.Crew;
import org.ubo.employee.CrewFolder;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.tree.TreeFolder;
import org.ubo.tree.TreeFolderVirtual;
import org.ubo.tree.TreeLeaf;
import org.ubo.utils.Result;
import org.uui.component.MenuItem;
import org.uui.db.DataBase;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.explorer.ExplorerFolder;
import org.uui.explorer.ExplorerLeaf;
import org.uui.explorer.ExplorerPanel;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.core.CrewUsedInfo;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;
import ru.sibek.database.CarssierDataBase;

public class StaffCrewPanel extends ExplorerPanel {

    private DataBase dataBase;
    private TreeFolderVirtual rootFolder;
    private CrewItemPanel crewPane;
    private CarssierCore core;

    public StaffCrewPanel(String sessionId) {
        super(sessionId, CarssierDataBase.getDataBase());
        core = CarssierCore.getInstance();
        dataBase = CarssierDataBase.getDataBase();
        initRoot();
    }

    @Override
    public String treeWalker(TreeFolder treeFolder) {
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
            CrewFolder cf = (CrewFolder) tf;
            Crew crew = cf.getCrew();
            if (crew == null) {
                continue;
            }

            CrewUsedInfo crewUsedInfo = core.isCrewUsed(crew.getId());
            String image = Paths.get("img", "icons", "crew64.png").toString();
            if (!crewUsedInfo.isCrewUsed()) {
                image = Paths.get("img", "icons", "crew_grey_64.png").toString();
            }

            ExplorerFolder folder = new ExplorerFolder(getSession(), 
                    tf.getClass().getName(),
                    crewUsedInfo.getCrew().getId(),
                    crewUsedInfo.getCrew().getName(),
                    image);
            folder.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    try {
                        if (evt.getJSONObject().getString("eventType").equals("rightclick")) {
                            JSMediator.setContextMenu(getSession(), getFolderMenu(evt.getJSONObject().getLong("dbid")));
                        }

                        if (evt.getJSONObject().getString("eventType").equals("click")) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("session", getSession());
                            jsonObject.put("dbid", evt.getJSONObject().getLong("dbid"));
                            jsonObject.put("eventType", "push");
                            jsonObject.put("action", "showRightPanel");
                            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                                    CrewInfoPanel.class.getName(),
                                    jsonObject.toString());
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
    public void setNavigatorPanel() {
        //
    }

    @Override
    public void rename(JSONObject o) {
        try {
            if (o.getString("className").equals(CrewFolder.class.getName())) {
                Crew crew = (Crew) dataBase.getObject(Crew.class.getName(), o.getLong("dbid"));
                crew.setName(o.getString("data"));
                dataBase.updateObject(crew);
            }

            initRoot();
            setFolder(rootFolder);

            JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

        } catch (JSONException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }

    private void initRoot() {
        rootFolder = new TreeFolderVirtual("root");
        ArrayList<Object> crews = dataBase.getObjects(Crew.class.getName());
        for (int i = 0; i < crews.size(); i++) {
            Crew crew = (Crew) crews.get(i);
            CrewFolder fol = new CrewFolder(dataBase, crew);
            rootFolder.addTreeFolder(fol);
        }

        currentTreeFolder = rootFolder;
    }

    private void addCrew(String name) {
        Result r = core.addCrew(name);
        if (r.isError()) {
            JSMediator.alert(getSession(), r.getReason());
            return;
        }

        CrewFolder fol = new CrewFolder(dataBase, (Crew) r.getObject());
        rootFolder.addTreeFolder(fol);

        initRoot();
        repaint();
    }

    private void repaint() {
        JSMediator.setExplorerEditableMode(getSession(), true);
        JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());
    }

    public void addCrew() {
        addCrew("New crew");
    }

    @Override
    public TreeFolder getRoot() {
        return rootFolder;
    }

    @Override
    public String getLeafMenu(TreeLeaf leaf) {
        return "";
    }

    public String getFolderMenu(final long crewId) {
        String model = "";
        MenuItem itemRemoveCrew = new MenuItem(getSession(), "img/subbuttons/delete.png", "Удалить");
        itemRemoveCrew.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                    @Override
                    public void pressed(int button) {
                        if (button == ConfirmPanel.YES) {
                            Result r = core.removeCrew(crewId);
                            if (r.isError()) {
                                if (r.getReason().equals("Force remove need")) {
                                    PopupPanel popupPanel = new PopupPanel(getSession());
                                    popupPanel.setTitle("Предупреждение!");
                                    popupPanel.setPanel("Удаляемая бригада содержит "
                                            + "в себе работников, а также участвует в расчетах по "
                                            + "начислению зарплаты за оказанные услуги и реализованные "
                                            + "товары.<br/>"
                                            + "Удалить ее можно лишь после того, как все "
                                            + "ссылки на эту бригаду будут удалены.");
                                    popupPanel.showPanel();

                                } else {
                                    JSMediator.alert(getSession(), r.getReason());
                                }
                            } else {
                                initRoot();
                                repaint();
                            }
                        }
                    }
                };
                confirmPanel.setTitle("Вопрос");
                confirmPanel.setMessage("Удалить безвозвратно?");
                confirmPanel.showPanel();
            }
        });
        model += itemRemoveCrew.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    @Override
    public String getPanelMenu() {
        String model = "";
        MenuItem itemAddCrew = new MenuItem(getSession(), "img/subbuttons/add_crew.png", "Добавить бригаду");
        itemAddCrew.addMenuEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                addCrew();
            }
        });
        model += itemAddCrew.getModel();

        MenuItem itemCancel = new MenuItem(getSession(), "img/subbuttons/cancel.png", "Отмена");
        model += itemCancel.getModel();

        return model;
    }

    @Override
    public String getModel() {
        return refreshAndGetHTMLModel();
    }

    @Override
    public String getIdentificator() {
        return StaffCrewPanel.class.getName();
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

            if (jsonObject.getString("eventType").equals("rightclick")) {
                JSMediator.setContextMenu(getSession(), getPanelMenu());
            }

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "Бригады");
                JSMediator.setWorkPanel(getSession(), refreshAndGetHTMLModel());

            }
        } catch (JSONException e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    @Override
    public String getFolderMenu(TreeFolder folder) {
        return "";
    }
}
