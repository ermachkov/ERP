package ru.sibek.plugin.editor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.utils.Result;
import org.uui.component.Button;
import org.uui.component.WorkPanel;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.ui.MainFrame;
import ru.sibek.core.ui.ConfirmPanel;
import ru.sibek.core.ui.PopupPanel;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class EditorPanel extends WorkPanel {

    private Button btnImages, btnAbout, btnService, btnShop, btnAddress, btnContacts, btnRemoveImage;
    private Path selectedFileImage;
    private String selectedPage = "";
    private CarssierCore core = CarssierCore.getInstance();

    public EditorPanel(String sessionId) {
        super(sessionId);
        btnImages = new Button(getSession(), "Картинки");
        btnImages.setStyle("width:98%;");
        btnImages.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                JSMediator.setImageBrowser(getSession(), getImageBrowser());
            }
        });

        btnAbout = new Button(getSession(), "О нас");
        btnAbout.setStyle("width:98%;");
        btnAbout.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                selectedPage = "about";
                JSMediator.showEditor(getSession(), getModel());
                JSMediator.setEditorContent(getSession(), getPage(selectedPage));
            }
        });

        btnShop = new Button(getSession(), "Магазин");
        btnShop.setStyle("width:98%;");
        btnShop.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                selectedPage = "shop";
                JSMediator.showEditor(getSession(), getModel());
                JSMediator.setEditorContent(getSession(), getPage(selectedPage));
            }
        });

        btnService = new Button(getSession(), "Услуги");
        btnService.setStyle("width:98%;");
        btnService.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                selectedPage = "service";
                JSMediator.showEditor(getSession(), getModel());
                JSMediator.setEditorContent(getSession(), getPage(selectedPage));
            }
        });

        btnAddress = new Button(getSession(), "Адрес");
        btnAddress.setStyle("width:98%;");
        btnAddress.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                selectedPage = "address";
                JSMediator.showEditor(getSession(), getModel());
                JSMediator.setEditorContent(getSession(), getPage(selectedPage));
            }
        });

        btnContacts = new Button(getSession(), "Контакты");
        btnContacts.setStyle("width:98%;");
        btnContacts.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                selectedPage = "contacts";
                JSMediator.showEditor(getSession(), getModel());
                JSMediator.setEditorContent(getSession(), getPage(selectedPage));
            }
        });

        btnRemoveImage = new Button(getSession(), "Удалить картинку");
        btnRemoveImage.addUIEventListener(new UIEventListener() {
            @Override
            public void event(UIEvent evt) {
                if (selectedFileImage != null) {
                    ConfirmPanel confirmPanel = new ConfirmPanel(getSession()) {
                        @Override
                        public void pressed(int button) {
                            if(button == ConfirmPanel.YES){
                                selectedFileImage.toFile().delete();
                            buildJSImagesList();
                            try {
                                JSMediator.setImageBrowser(getSession(), getImageBrowser());
                                selectedFileImage = null;

                            } catch (Exception e) {
                                JSMediator.alert(getSession(), e.toString());
                                Logger.getGlobal().log(Level.WARNING, null, e);
                                selectedFileImage = null;
                            }
                            }
                        }
                    };
                    confirmPanel.setTitle("Предупреждение");
                    confirmPanel.setMessage("Удалить выбранную картинку?");
                    confirmPanel.showPanel();
                }
            }
        });
    }

    private String getPage(String path) {
        return core.getWWWPage(path);
    }

    private void savePage(String page) {
        PopupPanel popupPanel = new PopupPanel(getSession());
        popupPanel.setTitle("Сообщение");
        popupPanel.setPanel("Сохранено");

        if (core.isPageExist(selectedPage)) {
            Result r = core.modifyWWWPage(selectedPage, page);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
            } else {
                popupPanel.showPanel();
            }

        } else {
            Result r = core.addWWWPage(selectedPage, page);
            if (r.isError()) {
                JSMediator.alert(getSession(), r.getReason());
            } else {
                popupPanel.showPanel();
            }
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

            setSession(jsonObject.getString("session"));

            if (jsonObject.getString("eventType").equals("click")) {
                MainFrame.getInstance().setSelectedOperationButton(getSession(), "WWW редактор");
                JSMediator.showEditor(getSession(), getModel());
                JSMediator.hideNavigationPanel(getSession());
            }

            if (jsonObject.getString("eventType").equals("save")) {
                savePage(jsonObject.getString("value"));
            }

            if (jsonObject.getString("eventType").equals("fileUploaded")) {
                JSONObject jsObj = new JSONObject(jsonObject.getString("response"));
                if (jsObj.getString("status").equals("success")) {
                    try {
                        Path src = Paths.get(jsObj.getString("file"));
                        Path dst = Paths.get(System.getProperty("user.home"),
                                ".saas", "app", "ui", "img", "www",
                                src.getName(src.getNameCount() - 1).toString());
                        Files.move(src, dst);
                        buildJSImagesList();
                        JSMediator.setImageBrowser(getSession(), getImageBrowser());

                    } catch (IOException ex) {
                        Logger.getLogger(EditorPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

            if (jsonObject.getString("eventType").equals("selectImage")) {
                selectedFileImage = Paths.get(System.getProperty("user.home"),
                        ".saas", "app", "ui", "img", "www",
                        jsonObject.getString("image"));
            }

        } catch (JSONException ex) {
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    private String getImageBrowser() {
        String model = ""
                + "<table width='100%' height='100%' cellpadding='0' cellspacing='0'>"
                + "<tr>"
                + "<td width='20%' valign='top'>"
                + "<div style='width:100%;'>"
                + getPages()
                + "</td>"
                + "<td valign='top'>"
                + "<button id='btnImageUpload' onclick='return getUICore().imageUpload();'>"
                + "Добавить картинку"
                + "</button>"
                + btnRemoveImage.getModel()
                + "</div>"
                + getImages()
                + "</td>"
                + "</tr>"
                + "</table>";
        return model;
    }

    private String getImages() {
        String images = "<div style='width:100%;' id='imageBrowser'>";
        Path dir = Paths.get(System.getProperty("user.home"), ".saas", "app", "ui", "img", "www");
        for (File f : dir.toFile().listFiles()) {
            images += "<div style='position:relative;float:left;margin-right:5px;padding:3px;' "
                    + "class='imageView' file='" + f.getName() + "'>"
                    + "<img src='img/www/" + f.getName() + "'/>"
                    + "</div>";
        }
        return images + "</div>";
    }

    private void buildJSImagesList() {
        String str = "var tinyMCEImageList = new Array(\n";
        Path dir = Paths.get(System.getProperty("user.home"), ".saas", "app", "ui", "img", "www");
        boolean isFirst = true;
        for (File f : dir.toFile().listFiles()) {
            if (f.isDirectory()) {
                continue;
            }

            String s = "[\"" + f.getName() + "\", \"img/www/" + f.getName() + "\"]";
            if (isFirst) {
                str += s;
                isFirst = false;
            } else {
                str += ",\n" + s;
            }
        }

        str += "\n);";

        Path f = Paths.get(System.getProperty("user.home"), ".saas", "app", "ui", "js", "image_list.js");
        try {
            Files.write(f, str.getBytes());
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, str, ex);
            JSMediator.alert(getSession(), ex.toString());
        }
    }

    @Override
    public String getModel() {
        String model = ""
                + "<table width='100%' height='100%' cellpadding='0' cellspacing='0'>"
                + "<tr>"
                + "<td width='20%' valign='top'>"
                + getPages()
                + "</td>"
                + "<td valign='top'>"
                + "<form action='javascript:getUICore().savePage();'>"
                + "<textarea id='editor' name='editor' "
                + "style='width:100%;height:100%;' identificator='" + getIdentificator() + "'>"
                + "</textarea>"
                + "</form>"
                + "</td>"
                + "</tr>"
                + "</table>";
        return model;
    }

    private String getPages() {

        String pages = ""
                + btnImages.getModel()
                + "<br/>" + btnAbout.getModel()
                + "<br/>" + btnService.getModel()
                + "<br/>" + btnShop.getModel()
                + "<br/>" + btnAddress.getModel()
                + "<br/>" + btnContacts.getModel();

        return pages;
    }

    @Override
    public String getIdentificator() {
        return "ru.granplat.plugin.editor.EditorPanel";
    }
}
