/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.db.service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.datetime.DateTime;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.utils.SystemXML;
import org.uui.component.*;
import org.uui.db.utils.DBDump;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.ribbon.RibbonButton;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.PopupPanel;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DataBaseServicePanel extends WorkPanel {//implements HasWorkPanelToolbars {

    private ArrayList<RibbonButton> toolbarButtons;
    private TextField txtExportPath;
    private Path pathToSystemXML = Paths.get(System.getProperty("user.home"),
            ".saas", "app", "config", "system.xml");
    private SystemXML systemXML;
    private Button btnSaveExportPath, btnExport, btnImport;

    public DataBaseServicePanel(String sessionId) {
        super(sessionId);
        
        systemXML = SystemXML.newSystemXML(pathToSystemXML);
        txtExportPath = new TextField(getSession());
        txtExportPath.setStyle("width:70%;");
        txtExportPath.setText(getExportPath());

        btnSaveExportPath = new Button(getSession(), "Применить");
        btnSaveExportPath.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                if (!checkAndSavePath()) {
                    showPathError();
                }
            }
        });

        btnImport = new Button(getSession(), "Накатить");
        btnImport.setCssClass("btnDumpUpload");
        btnImport.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    JSMediator.dbDumpUpload(getSession());

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                    JSMediator.alert(getSession(), e.toString());
                }

            }
        });

        btnExport = new Button(getSession(), "Слить");
        btnExport.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    if (!Paths.get(txtExportPath.getText()).toFile().exists()) {
                        showPathError();
                        return;
                    }

                    JSMediator.showLockPanel(getSession());
                    DBDump.exportDB(
                            Paths.get(System.getProperty("user.home"),
                            ".saas", "app", "db").toString(),
                            txtExportPath.getText(),
                            "carssier_dump_"
                            + DateTime.getFormatedDate("dd-MM-yy-HH-mm", new Date())
                            + ".zip");
                    JSMediator.hideLockPanel(getSession());

                    PopupPanel popupPanel = new PopupPanel(getSession()); popupPanel.setTitle("Информация");
                    popupPanel.setPanel("<div>Дамп базы данных создан и сохранен в "
                            + txtExportPath.getText() + File.separator
                            + "carssier_dump_"
                            + DateTime.getFormatedDate("dd-MM-yy-HH-mm", new Date())
                            + ".zip");
                    popupPanel.showPanel();


                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        });

        //initToolbar();
    }

    private void showPathError() {
        PopupPanel popupPanel = new PopupPanel(getSession()); popupPanel.setTitle("Ошибка!");
        popupPanel.setPanel("<div>Пути "
                + txtExportPath.getText()
                + " не существует.");
        popupPanel.showPanel();
    }

    private boolean checkAndSavePath() {
        if (!Paths.get(txtExportPath.getText()).toFile().exists()) {
            return false;
        } else {
            return systemXML.setValue("/root/database/@export_path", txtExportPath.getText());
        }
    }

    private String getExportPath() {
        String path = systemXML.getValue("/root/database/@export_path", false);
        if (path.equals("undefined")) {
            String p = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "temp").toString();
            if (systemXML.setValue("/root/database/@export_path", p)) {
                return p;
            } else {
                return "fucking error!!!!!!!!!!!!!!!!!!!!!!";
            }


        } else {
            return path;
        }
    }

    private void importDB(String pathToDump) {
        DBDump.importDB(
                pathToDump,
                Paths.get(System.getProperty("user.home"),
                ".saas", "app", "db").toString());
        PopupPanel popupPanel = new PopupPanel(getSession()); popupPanel.setTitle("Сообщение");
        popupPanel.setPanel("<div>База данных того... Нужно перезапустить "
                + "программу или дождаться отстоя пены...</div>");
        popupPanel.showPanel();

        try {
            JSMediator.showProgressBar(getSession(), 0);
            
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, pathToDump, e);
        }

    }

//    @Override
//    public List<RibbonButton> getWorkpanelToolbars() {
//        return toolbarButtons;
//    }
    @Override
    public String getIdentificator() {
        return DataBaseServicePanel.class.getName();
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
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "База данных");
                JSMediator.setWorkPanel(getSession(), getModel());
                JSMediator.hideNavigationPanel(getSession());

            }

            if (jsonObject.getString("eventType").equals("fileSelected")) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + jsonObject.getString("filename"));
                JSMediator.showProgressBar(getSession(), 100);
            }

            if (jsonObject.getString("eventType").equals("fileUploaded")) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + jsonObject.getString("response"));
                JSONObject jsObj = new JSONObject(jsonObject.getString("response"));
                if (jsObj.getString("status").equals("success")) {
                    importDB(jsObj.getString("file").trim());
                }

            }

        } catch (JSONException  e) {
            JSMediator.alert(getSession(), e.toString());
            Logger.getGlobal().log(Level.WARNING, json, e);
        }
    }

    @Override
    public String getModel() {
        String model = ""
                + "<div style='width:50%;'>"
                + txtExportPath.getModel()
                + btnSaveExportPath.getModel()
                + btnExport.getModel()
                + "<br/>"
                + "<span style='font-size:70%'>Необходимо прописать правильный "
                + "путь для слива дампа like /home/master/dbdumps</span>"
                + "<br/>"
                + "<br/>"
                + "<button id='btnDumpUpload' onclick='return getUICore().dbDumpUpload();'>Накатить</button>"
                + "<br/>"
                + "<span style='font-size:70%'>Нажать на кнопку, выбрать дамп, накатить."
                + "</div>"
                + "<br/><br/>"
                + getCleaner();
        return model;
    }
    
    private String getCleaner(){
        final ArrayList<CheckBox> checkBoxList = new ArrayList<>();
        String model = "<div style='font-size:80%;'>"
                + "<div style='font-weight:bold;'>"
                + "Удаление объектов из базы данных"
                + "</div>";
        
        String root = CarssierDataBase.getDataBase().getPathToDB();
        File dir = new File(root);
        for(File folder : dir.listFiles()){
            if(folder.isFile()){
                continue;
            }
            
            CheckBox checkBox = new CheckBox(getSession(), folder.toPath()
                    .getName(folder.toPath().getNameCount() - 1)
                    .toString());
            checkBox.setValue(folder.toPath()
                    .getName(folder.toPath().getNameCount() - 1)
                    .toString());
            checkBoxList.add(checkBox);
            
            model += "<div>"+checkBox.getModel()+"</div>";
        }
        
        Button remove = new Button(getSession(), "Удалить");
        remove.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                ArrayList<String> removeList = new ArrayList<>();
                for(CheckBox chkBox : checkBoxList){
                    if(chkBox.isChecked()){
                        removeList.add(chkBox.getValue());
                    }
                    
                    clearDB(removeList);
                }
            }
        });
        
        model += remove.getModel() + "</div>";
        
        return model;
    }
    
    private void clearDB(ArrayList<String> removeList){
        System.out.println(removeList);
        
        String root = CarssierDataBase.getDataBase().getPathToDB();
        for(String folder : removeList){
            Path p = Paths.get(root, folder);
            for(File f : p.toFile().listFiles()){
                if(f.isDirectory()){
                    continue;
                }
                
                f.delete();
            }
        }
    }
}
