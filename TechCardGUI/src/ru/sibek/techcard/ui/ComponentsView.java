/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.techcard.ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.component.*;
import org.uui.db.DataBase;
import org.uui.event.NavigatorChangeListener;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import org.uui.detaillist.*;
import org.uui.webkit.WebKitUtil;
import ru.sibek.techcard.db.*;
import ru.sibek.tcr.core.ReportsCore;
import ru.sibek.tcr.reports.Specifications;

/**
 *
 * @author developer
 */
public class ComponentsView extends Component {

    private DataBase db;
    private DetailsList dl;
    private TextField txtTest;
    private PasswordTextField txtPassword;
    private ComboBox cboFormType;
    private ComboBox cboCardType;
    private CheckBox chkTest;
    private Button btnAddDevice;
    private Button btnSaveDevice;
    private Button btnAddTechCard;
    private Button btnSaveTechCard;
    private Label lblRemoveDevice;
    private Label lblShowSpecification;
    private Link linkDevice;
    private Link linkTechCard;
    private Link linkAddOperation;
    private Link tabLink;
    private Button btnEditDevice;
    private Calendar calendar;
    private MacTableModel tblDevices;
    private MacTableModel tblTechCards;
    private String formtype = "";
    private String cardtype = "";
    private boolean emptyTable = false;

    public ComponentsView(String sessionId) {
        super(sessionId);
        Path p = Paths.get(System.getProperty("user.dir"), "db.properties");
        db = DataBase.getInstance(p.toString());
        db.init();
        init();
    }

    private void init() {


        txtTest = new TextField(getSession());
        txtTest.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                System.out.println("txtTest >>> " + txtTest.getText());
            }
        });

       


        btnSaveTechCard = new Button(getSession(), "Сохранить");
        btnSaveTechCard.setCssClass("saveTechCard button btn-primary");
        btnSaveTechCard.setImage("icon-ok icon-white");
        //btnSaveDevice.setAttribute("data-dismiss", "modal");
        btnSaveTechCard.setIdentificator("saveTechCard");
        btnSaveTechCard.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                System.out.println("7897978");
                switch (cardtype) {
                    case "marshrut-card": {
                        try {
                            JSONObject jsonObject = evt.getJSONObject();
                            MarshrutCard marshrutCard = new MarshrutCard();
                            marshrutCard.setFormtype(formtype);
                            marshrutCard.setDeviceid(jsonObject.getString("deviceid"));
                            marshrutCard.setFirmname(jsonObject.getString("firmname"));
                            marshrutCard.setEn(jsonObject.getString("en"));
                            marshrutCard.setEv(jsonObject.getString("ev"));
                            marshrutCard.setKd(jsonObject.getString("kd"));
                            marshrutCard.setKim(jsonObject.getString("kim"));
                            marshrutCard.setKod(jsonObject.getString("kod"));
                            marshrutCard.setKodzagotovki(jsonObject.getString("kodzagotovki"));
                            marshrutCard.setMatherialname(jsonObject.getString("matname"));
                            marshrutCard.setMd(jsonObject.getString("md"));
                            marshrutCard.setMz(jsonObject.getString("mz"));
                            marshrutCard.setNrash(jsonObject.getString("nrash"));
                            marshrutCard.setNumber1(jsonObject.getString("number1"));
                            marshrutCard.setNumber2(jsonObject.getString("number2"));
                            marshrutCard.setPartname(jsonObject.getString("partname"));
                            marshrutCard.setProfile_size(jsonObject.getString("profile-size"));
                            // System.out.println(deviceNumber);              
                            JSONArray array = jsonObject.getJSONArray("contents");
                            String temp = array.getString(0);
                            String[] arr = temp.split("!!!");
                            for (int i = 0; i < arr.length; i++) {
                                JSONObject obj = new JSONObject(arr[i]);
                                Operations operation = new Operations();
                                Iterator iter = obj.keys();
                                while (iter.hasNext()) {
                                    String index = iter.next().toString();
                                    switch (index) {
                                        case "sm": {
                                            operation.setSm(obj.get(index).toString());
                                            break;
                                        }
                                        case "rm": {
                                            operation.setRm(obj.get(index).toString());
                                            break;
                                        }
                                        case "ut": {
                                            operation.setUt(obj.get(index).toString());
                                            break;
                                        }
                                        case "kr": {
                                            operation.setKr(obj.get(index).toString());
                                            break;
                                        }
                                        case "Tpz": {
                                            operation.setTpz(obj.get(index).toString());
                                            break;
                                        }
                                        case "docname": {
                                            operation.setDocname(obj.get(index).toString());
                                            break;
                                        }
                                        case "Kst": {
                                            operation.setKst(obj.get(index).toString());
                                            break;
                                        }
                                        case "prof": {
                                            operation.setProf(obj.get(index).toString());
                                            break;
                                        }
                                        case "Tsh": {
                                            operation.setTsh(obj.get(index).toString());
                                            break;
                                        }
                                        case "oper": {
                                            operation.setNumber(Long.valueOf(obj.get(index).toString()));
                                            break;
                                        }
                                        case "r": {
                                            operation.setR(obj.get(index).toString());
                                            break;
                                        }
                                        case "koid": {
                                            operation.setKoid(obj.get(index).toString());
                                            break;
                                        }
                                        case "uch": {
                                            operation.setUch(obj.get(index).toString());
                                            break;
                                        }
                                        case "devicename": {
                                            operation.setDevicename(obj.get(index).toString());
                                            break;
                                        }
                                        case "en": {
                                            operation.setEn(obj.get(index).toString());
                                            break;
                                        }
                                        case "ceh": {
                                            operation.setCeh(obj.get(index).toString());
                                            break;
                                        }
                                        case "opername": {
                                            operation.setOpername(obj.get(index).toString());
                                            break;
                                        }
                                    }

                                    System.out.println(index + "   " + obj.get(index));
                                }
                                long id = db.addObject(operation);
                                marshrutCard.addOperation(id);
//тут ложить в массив базы ид операции методом аддОператион
                                System.out.println("---------------");

                            }
                            long id = db.addObject(marshrutCard);
                            Documents document = new Documents();
                            document.setMarshrutnayaCardId(id);
                            document.setFirmname(jsonObject.getString("firmname"));
                            long docid = db.addObject(document);
                            TechnologyCard tcard = new TechnologyCard();
                            tcard.setName(jsonObject.getString("partname"));
                            tcard.addDocument(docid);
                            tcard.setNumber(jsonObject.getString("number1"));
                            long tcid = db.addObject(tcard);
                            String deviceid = jsonObject.getString("deviceid");
                            DocumentState checkstate= new DocumentState();
                            checkstate.setDocumentsId(docid);
                            checkstate.setState(true);
                            checkstate.setDeviceId(Long.valueOf(deviceid));
                            checkstate.setTechCardId(tcid);
                            db.addObject(checkstate);
                            MachinesCatalog mc = (MachinesCatalog) db.getObject(MachinesCatalog.class.getName(), Long.valueOf(deviceid));
                            mc.addTechCard(tcid);
                            db.updateObject(mc);
                            //--------------BLYAD!!!
                            Detail d = new Detail(getSession());
                            d.setSummary(jsonObject.getString("partname"));
                            linkTechCard.setCssClass("marshrut-card");
                            linkTechCard.setAttribute("id_mk", tcid);
                            linkTechCard.setAttribute("card", "m_card" + tcid);
                            linkTechCard.setHref("#m_card" + tcid + "");
                            d.addLabel(linkTechCard.getModel());
                            dl.addDetailItem(d);
                            if (emptyTable) {
                                emptyTable = false;//show NO TECH CArd menu
                            }

                            JSMediator.exec(getSession(),
                                    //WebKitFrame.getInstance().browserExecutor(
                                    "getUICore().refreshElement("
                                    + "'cardPanel" + deviceid + "', "
                                    + "'" + WebKitUtil.prepareToJS(dl.getModel()) + "');");

                            
                        } catch (JSONException ex) {
                            System.err.println(ex);
                        }
                        break;
                    }
                }


            }
        });

        cboCardType = new ComboBox(getSession());

        LinkedHashMap<String, String> data = new LinkedHashMap<>();
        data.put("Выберите тип карты", "insert-card");
        data.put("Операционная карта", "operation-card");
        data.put("Маршрутная карта", "marshrut-card");

        cboCardType.setItems(data);
        final GostFormModels formBuilder = new GostFormModels();

        linkAddOperation = new Link(getSession(), "добавить операцию");
        linkAddOperation.setCssClass("add-operation");
        linkAddOperation.setStyle("float: left; margin-top:10px;margin-right:10px");
        linkAddOperation.setHref("#");
        linkAddOperation.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {


                System.out.println("7897978");
                try {

                    //WebKitFrame.getInstance().browserExecutor(
                    JSMediator.exec(getSession(),
                            "getUICore().setAddOperationRow('"
                            + WebKitUtil.prepareToJS(formBuilder.getForm1operationRow(0))
                            + "');");

                } catch (Exception ex) {
                    System.err.println(ex);
                    Logger.getGlobal().log(Level.WARNING, btnSaveDevice.getModel(), ex);
                }
            }
        });
        cboFormType = new ComboBox(getSession());

        LinkedHashMap<String, String> data1 = new LinkedHashMap<>();
        data1.put("Выберите форму по ГОСТУ", "insert-form");
        data1.put("Форма 1 Гост 3.1118-84", "form1");
        data1.put("Форма 1б Гост 3.1118-84", "form1b");

        cboFormType.setItems(data1);
        cboCardType.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                //*******
                ArrayList<MarshrutCard> list1 = db.getAllObjectsList(MarshrutCard.class.getName());
                for (MarshrutCard wrk : list1) {
                    System.out.println(wrk.toString());
                }
                ArrayList<Operations> list2 = db.getAllObjectsList(Operations.class.getName());
                for (Operations wr : list2) {
                    System.out.println(wr.toString());
                }
                //******
                cardtype = cboCardType.getSelectedValue();
                switch (cboCardType.getSelectedValue()) {
                    case "marshrut-card": {
                        try {
                            //WebKitFrame.getInstance().browserExecutor(
                            JSMediator.exec(getSession(),
                                    "getUICore().setFormCmb('"
                                    + WebKitUtil.prepareToJS("<h4>Выберите форму:</h4>" + cboFormType.getModel() + "<div id='form-panel'></div>")
                                    + "');");
                        } catch (Exception ex) {
                            System.err.println(ex);

                        }
                        break;
                    }
                }

            }
        });
        cboFormType.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                formtype = cboFormType.getSelectedValue();
                switch (cboFormType.getSelectedValue()) {
                    case "form1": {
                        try {
                            //WebKitFrame.getInstance().browserExecutor(
                            JSMediator.exec(getSession(),
                                    "getUICore().setFormModel('"
                                    + WebKitUtil.prepareToJS(formBuilder.getForm1Model() + linkAddOperation.getModel()
                                    + "<input type='radio' id='radio1' name='optionsRadios' checked>по умолчанию<input type='radio' id='radio2' name='optionsRadios'>с номером <input id='oper-num' type='text' style='margin-top:2px;margin-left:3px;width:35px;' class='input-small'><hr>")
                                    + "');");
                        } catch (Exception ex) {
                            System.err.println(ex);


                        }
                        break;
                    }
                }
            }
        });
        chkTest = new CheckBox(getSession(), "CheckBox");
        chkTest.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                System.out.println("chkTest >>> " + chkTest.isChecked());
            }
        });
        btnAddTechCard = new Button(getSession(), "Добавить");
        btnAddTechCard.setCssClass("btn btn-primary");
        btnAddTechCard.setImage("icon-plus icon-white");
        btnAddTechCard.setAttribute("data-toggle", "modal");
        btnAddTechCard.setAttribute("data-target", "#make-tech-card");
        btnAddTechCard.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                //System.out.println("cboTest5555555555555");
                try {
                    //WebKitFrame.getInstance().browserExecutor(
                    JSMediator.exec(getSession(),
                            "getUICore().setTechCardModal('"
                            + WebKitUtil.prepareToJS("<h4>Выберите тип карты:</h4>" + cboCardType.getModel() + "<div id='cbo-panel'></div>" + "<div id='form-panel'></div>")
                            + "');");

                    // WebKitFrame.getInstance().browserExecutor(
                    JSMediator.exec(getSession(),
                            "getUICore().setAddTechCardButton('"
                            + WebKitUtil.prepareToJS(btnSaveTechCard.getModel())
                            + "');");
                } catch (Exception ex) {
                    System.err.println(ex);

                }
            }
        });

        btnAddDevice = new Button(getSession(), "Добавить");
        btnAddDevice.setCssClass("btn btn-primary");
        btnAddDevice.setImage("icon-plus icon-white");
        btnAddDevice.setAttribute("data-toggle", "modal");
        btnAddDevice.setAttribute("data-target", "#make-device");
        //<button class="btn btn-primary" data-toggle="modal" data-target="#make-device" >
        btnAddDevice.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                System.out.println("btnADD >>> pressed");
                btnSaveDevice = new Button(getSession(), "Сохранить");
                btnSaveDevice.setCssClass("saveDevice button btn-primary");
                btnSaveDevice.setImage("icon-ok icon-white");
                //btnSaveDevice.setAttribute("data-dismiss", "modal");
                btnSaveDevice.setIdentificator("saveDevice");
                btnSaveDevice.addUIEventListener(new UIEventListener() {

                    @Override
                    public void event(UIEvent evt) {
                        System.out.println("SaveDevice >>> pressed");
                        try {


                            JSONObject jsonObject = evt.getJSONObject();
                            String deviceNumber = jsonObject.getString("deviceNumber");
                            String deviceName = jsonObject.getString("deviceName");
                            //put to bd and add js to table
                            MachinesCatalog mac = new MachinesCatalog();

                            ArrayList<MacTableRow> rows2 = tblDevices.getData();
                            if (rows2.isEmpty()) {
                                emptyTable = true;
                            }
                            MacTableRow row2 = new MacTableRow();
                            mac.setName(deviceName);
                            mac.setPartNumber(deviceNumber);
                            db.addObject(mac);
                            linkDevice = new Link(getSession(), mac.getPartNumber());
                            linkDevice.setCssClass("device");
                            linkDevice.setAttribute("id_bd", mac.getId());
                            linkDevice.setHref("#tab" + mac.getId() + "");

                            //row2.addCell(new MacTableCell("<a class='device' id_bd=" + mac.getId() + " href\"#\">" + mac.getPartNumber() + "</a>", false));
                            row2.addCell(new MacTableCell(getSession(), linkDevice.getModel(), false));
                            row2.addCell(new MacTableCell(getSession(), mac.getName(), false));
                            row2.addCell(new MacTableCell(getSession(), lblRemoveDevice.getModel(), false));
                            rows2.add(row2);
                            tblDevices.setData(rows2);

                            if (emptyTable) {
                                emptyTable = false;
                                // WebKitFrame.getInstance().browserExecutor(
                                JSMediator.exec(getSession(),
                                        "getUICore().setWorkPanel('"
                                        + WebKitUtil.prepareToJS(getModel())
                                        + "');");

                            } else {
                                JSMediator.exec(getSession(),
                                        //WebKitFrame.getInstance().browserExecutor(
                                        "getUICore().refreshElement("
                                        + "'macTablePanel', "
                                        + "'" + WebKitUtil.prepareToJS(tblDevices.getModel()) + "');");

                            }
                        } catch (JSONException /*
                                 * | WebKitException
                                 */ ex) {
                            System.err.println(ex);

                        }
                    }
                });

                try {
                    // JSONObject jsonObject = new JSONObject("load");

                    //WebKitFrame.getInstance().browserExecutor(
                    JSMediator.exec(getSession(),
                            "getUICore().setAddDeviceButton('"
                            + WebKitUtil.prepareToJS(btnSaveDevice.getModel())
                            + "');");

                } catch (Exception ex) {
                    System.err.println(ex);
                    Logger.getGlobal().log(Level.WARNING, btnSaveDevice.getModel(), ex);
                }

            }
        });
        tblDevices = new MacTableModel(getSession(), false);
        MacTableHeaderModel mth = new MacTableHeaderModel();
        //+tblDevices.setRowClass("devicerow");
        mth.addHeaderColumn(new MacHeaderColumn("Cерийный номер", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Наименование", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Действие", String.class, false));
        tblDevices.setHeader(mth);
        tblDevices.setCssClass("macTable table table-striped table-bordered");
        //tblTest.setNavigatorShowingAlways(true);
        tblDevices.getMacTableNavigator().setFilterButtonVisible(false);
        tblDevices.getMacTableNavigator().setRefreshButtonVisible(false);
        tblDevices.addNavigatorChangeListener(new NavigatorChangeListener() {

            @Override
            public void event(int event) {
                try {
                    JSMediator.exec(getSession(),
                            //WebKitFrame.getInstance().browserExecutor(
                            "getUICore().refreshElement("
                            + "'macTablePanel', "
                            + "'" + WebKitUtil.prepareToJS(tblDevices.getModel()) + "');");

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        });

        tblTechCards = new MacTableModel(getSession(), false);
        MacTableHeaderModel cth = new MacTableHeaderModel();
        //tblTechCards.setRowClass("tech-card");
        cth.addHeaderColumn(new MacHeaderColumn("Номер карты", String.class, false));
        cth.addHeaderColumn(new MacHeaderColumn("Наименование детали", String.class, false));
        cth.addHeaderColumn(new MacHeaderColumn("Действие", String.class, false));
        tblTechCards.setHeader(cth);
        tblTechCards.setCssClass("macTable table table-striped table-bordered");
        //tblTest.setNavigatorShowingAlways(true);
        tblTechCards.getMacTableNavigator().setFilterButtonVisible(false);
        tblTechCards.getMacTableNavigator().setRefreshButtonVisible(false);
        tblTechCards.addNavigatorChangeListener(new NavigatorChangeListener() {

            @Override
            public void event(int event) {
                try {
                    JSMediator.exec(getSession(),
                            //WebKitFrame.getInstance().browserExecutor(
                            "getUICore().refreshElement("
                            + "'macTablePanel', "
                            + "'" + WebKitUtil.prepareToJS(tblTechCards.getModel()) + "');");

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        });

        lblShowSpecification = new Label(getSession(), "<i class=\"icon-file\"></i>");
        lblShowSpecification.setCssClass("operationicons");
        lblShowSpecification.setAttribute("action", "specification");
        lblShowSpecification.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    JSONObject jsonObject = evt.getJSONObject();

                    System.out.println("OLOLOL >>> " + jsonObject.getString("action") + jsonObject.getString("element"));

                    switch (jsonObject.getString("action")) {
                        case "specification": {
                       Specifications spec=new Specifications(getSession());
                       spec.ShowSpecification(jsonObject.getString("element"));
                   //ПЕРЕНЕСТИ В КОРЕ РЕПОРТСА
//                               JSMediator.exec(getSession(),
//                        //WebKitFrame.getInstance().browserExecutor(
//                        "getUICore().setTabHeader('"
//                        + WebKitUtil.prepareToJS("<li>" + "<a class='tab-link' data-toggle='tab' href='#spec"+jsonObject.getString("element")+"'>Specification</a>" + "</li>")
//                        + "');");
//                          
//                           JSMediator.exec(getSession(),
//                        //WebKitFrame.getInstance().browserExecutor(
//                        "getUICore().setTabBody('"
//                        + WebKitUtil.prepareToJS("<div class='tab-pane' id='spec" + jsonObject.getString("element") + "'>" + "specification" + "</div>")
//                        + "');");    
                            
                        }



                    }

                } catch (JSONException /*
                         * | WebKitException
                         */ ex) {
                    System.err.println(ex);

                }
                /*
                 *
                 */
            }
        });
                
        lblRemoveDevice = new Label(getSession(), "<i class=\"icon-remove\"></i>");
        lblRemoveDevice.setCssClass("operationicons");
        lblRemoveDevice.setAttribute("action", "remove");
        lblRemoveDevice.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    JSONObject jsonObject = evt.getJSONObject();

                    System.out.println("OLOLOL >>> " + jsonObject.getString("action") + jsonObject.getString("element"));

                    switch (jsonObject.getString("action")) {
                        case "remove": {
                            //delete from BD and js->delete from UI
                            //long q=Long.valueOf(jsonObject.getString("element"));
                            //int qw=Integer.valueOf(jsonObject.getString("rownumber"));

                            db.deleteObject(MachinesCatalog.class.getName(), Long.valueOf(jsonObject.getString("element")));
                            tblDevices.removeRow(Integer.valueOf(jsonObject.getString("rownumber")));
                            if (tblDevices.getRowCount() == 0) {
                                emptyTable = true;
                                JSMediator.exec(getSession(),
                                        //WebKitFrame.getInstance().browserExecutor(
                                        "getUICore().setWorkPanel('"
                                        + WebKitUtil.prepareToJS(getModel())
                                        + "');");
                            } else {
                                JSMediator.exec(getSession(),
                                        // WebKitFrame.getInstance().browserExecutor(
                                        "getUICore().refreshElement("
                                        + "'macTablePanel', "
                                        + "'" + WebKitUtil.prepareToJS(tblDevices.getModel()) + "');");
                            }
                        }



                    }

                } catch (JSONException /*
                         * | WebKitException
                         */ ex) {
                    System.err.println(ex);

                }
                /*
                 *
                 */
            }
        });

        ArrayList<MacTableRow> rows = new ArrayList<>();
        ArrayList<MachinesCatalog> machines = db.getAllObjectsList(MachinesCatalog.class.getName());
        if (!machines.isEmpty()) {
            if (emptyTable) {
                emptyTable = false;
            }
            for (MachinesCatalog mach : machines) {
                MacTableRow row = new MacTableRow();
                linkDevice = new Link(getSession(), mach.getPartNumber());
                linkDevice.setCssClass("device");
                linkDevice.setAttribute("id_bd", mach.getId());
                linkDevice.setHref("#tab" + mach.getId() + "");
                //row2.addCell(new MacTableCell("<a class='device' id_bd=" + mac.getId() + " href\"#\">" + mac.getPartNumber() + "</a>", false));
                row.addCell(new MacTableCell(getSession(), linkDevice.getModel(), false));
                //row.addCell(new MacTableCell("<a class='device' id_bd=" + mach.getId() + " href=#tab" + mach.getId() + ">" + mach.getPartNumber() + "</a>", false));
                row.addCell(new MacTableCell(getSession(), mach.getName(), false));
                row.addCell(new MacTableCell(getSession(), lblRemoveDevice.getModel()+lblShowSpecification.getModel(), false));
                rows.add(row);
                //System.out.println("Machine:" + mach.getName() + " id=" + mach.getId() + "\n Internal Part number: " + mach.getPartNumber());
            }
            tblDevices.setData(rows);
        } else {
            emptyTable = true;
        }

    }

    @Override
    public String getModel() {
        if (!emptyTable) {
            String model = ""
                    + "<div class='tab-pane active' id='tab1'>"
                    + "<div style='width:100%; height:750px; overflow:auto;' id='macTable'> "
                    + "<div style='width: 700px; margin:0 0 10px 50px' id=macTablePanel>" + tblDevices.getModel() + "</div>" + "<div style='padding-left:50px'>" + btnAddDevice.getModel() + "</div>" + "</div>"
                    //+ "<div>" + btnAddDevice.getModel() + "</div>"
                    + "</div>";

            //+ "</div>";

            return model;
        } else {
            String model = ""
                    + "<div class='tab-pane active' id='tab1'>"
                    + "<div style='width:100%; height:750px; overflow:auto;' id='macTable'> "
                    + "<div id=macTablePanel>" + "<div style='margin:50px 0 2px 50px; width:500px' class=\"alert alert-info\">В базе данных пока нету изделий. Вы можете добавить изделие с помощью кнопки добавить.</div>" + "</div>" + "<div style='padding-left:50px'>" + btnAddDevice.getModel() + "</div>" + "</div>"
                    + "</div>";

            return model;
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
            final JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.getString("eventType").equals("load")) {
                JSMediator.exec(getSession(),
                        // WebKitFrame.getInstance().browserExecutor(
                        "getUICore().setWorkPanel('"
                        + WebKitUtil.prepareToJS(getModel())
                        + "');");
            }

            if (jsonObject.getString("eventType").equals("tab-link")) {
                // System.out.println("IJHLHIKUGIKGJYG");
                //<button class="close">×</button>
                Button btnClose = new Button(getSession(), "×");
                btnClose.setCssClass("close");
                tabLink = new Link(getSession(), jsonObject.getString("text") + btnClose.getModel());
                tabLink.setHref("#tab" + jsonObject.getString("id_bd"));
                tabLink.setCssClass("tab-link");
                tabLink.setAttribute("content", jsonObject.getString("content"));
                tabLink.setAttribute("device_id_bd", jsonObject.getString("id_bd"));
                btnSaveTechCard.setAttribute("device_id_bd", jsonObject.getString("id_bd"));
                tabLink.setAttribute("data-toggle", "tab");
                JSMediator.exec(getSession(),
                        //WebKitFrame.getInstance().browserExecutor(
                        "getUICore().setTabHeader('"
                        + WebKitUtil.prepareToJS("<li>" + tabLink.getModel() + "</li>")
                        + "');");
                try {
                    //HERE!!!!!!  
                    MachinesCatalog mc = (MachinesCatalog) db.getObject(MachinesCatalog.class.getName(), Long.valueOf(jsonObject.getString("id_bd")));
                    ArrayList<Long> partids = mc.getTechCard();
                    //DIMA ETO DOPILIT ArrayList<TechnologyCard> technologyCard = db.getAllObjectsList(TechnologyCard.class.getName(), partids);
                    //KOSTYL'
                    ArrayList<TechnologyCard> technologyCard = new ArrayList();
                    for (Long p : partids) {
                        technologyCard.add((TechnologyCard) db.getObject(TechnologyCard.class.getName(), p));
                    }
                    //KOSTYL'
                    dl = new DetailsList(getSession());
                    if (!technologyCard.isEmpty()) {
                        if (emptyTable) {
                            emptyTable = false;
                        }
                        //создать модель Детаилс лист и далее вызывается функция которая его отобразит
                        linkTechCard = new Link(getSession(), "");

                        for (TechnologyCard tcard : technologyCard) {
                            Detail detail = new Detail(getSession());
                            detail.setSummary(tcard.getName());
                            if (tcard.getDocuments().isEmpty()) {
                                detail.addLabel("Add document");
                            } else {
                                ArrayList<Long> docs = tcard.getDocuments();
                                for (Long id : docs) {
                                    Documents doc = (Documents) db.getObject(Documents.class.getName(), id);
                                    if (doc.getMarshrutnayaCardId() != 0) {
                                        linkTechCard.setCssClass("marshrut-card");
                                        linkTechCard.setAttribute("id_mk", doc.getMarshrutnayaCardId());
                                        linkTechCard.setAttribute("card", "m_card" + doc.getMarshrutnayaCardId());
                                        linkTechCard.setHref("#m_card" + doc.getMarshrutnayaCardId() + "");
                                        detail.setAttribute("id_mk", doc.getMarshrutnayaCardId());
                                        linkTechCard.setText("Маршрутная карта");
                                        detail.addLabel(linkTechCard.getModel());
                                    }
                                }

                            }

                            dl.addDetailItem(detail);
                        }

                    } else {
                        emptyTable = true;
                    }


                    /*
                     * String model = "" + "<div class='tab-pane' id='tab" +
                     * jsonObject.getString("id_bd") + "'>" + "<h1>" +
                     * jsonObject.getString("text") +"</h1>" + "</div>";
                     */
                    JSMediator.exec(getSession(),
                            // WebKitFrame.getInstance().browserExecutor(
                            "getUICore().setTabBody('"
                            + WebKitUtil.prepareToJS(getTechCardsPanel(jsonObject))
                            + "');");
                } catch (Exception ex) {
                    System.err.println(ex);
                    Logger.getGlobal().log(Level.WARNING, json, ex);
                }



            }

            if (jsonObject.getString("eventType").equals("tab-card-link")) {
                Button btnClose = new Button(getSession(), "×");
                btnClose.setCssClass("close");
                tabLink = new Link(getSession(), jsonObject.getString("text").trim() + btnClose.getModel());
                tabLink.setHref("#m_card" + jsonObject.getString("id_mk"));
                tabLink.setCssClass("tab-link");
                tabLink.setAttribute("content", jsonObject.getString("content"));
                tabLink.setAttribute("mk_id_bd", jsonObject.getString("id_mk"));
                tabLink.setAttribute("data-toggle", "tab");
                JSMediator.exec(getSession(),
                        //WebKitFrame.getInstance().browserExecutor(
                        "getUICore().setTabHeader('"
                        + WebKitUtil.prepareToJS("<li>" + tabLink.getModel() + "</li>")
                        + "');");
                //TUT OTOBRAZENIE EBUCHEY FORMY PO GOSTU//
                MarshrutCard mcard = (MarshrutCard) db.getObject(MarshrutCard.class.getName(), Long.valueOf(jsonObject.getString("id_mk")));
                formtype = mcard.getFormtype();
                //DIMA      ArrayList<Operations> operations = db.getAllObjectsList(Operations.class.getName(), mcard.getOperations());
                //KOSTYL'
                ArrayList<Operations> operations = new ArrayList();
                ArrayList<Long> idsoper = mcard.getOperations();
                for (Long id : idsoper) {
                    operations.add((Operations) db.getObject(Operations.class.getName(), id));
                }
                //KOSTYL'
                String formheader = "", formbody = "", formfooter = "";
                switch (formtype) {
                    case "form1": {
                        formheader = "app/ui/cards/MK/mk_form1_header.html";
                        formbody = "app/ui/cards/MK/mk_form1_row.html";
                        formfooter = "app/ui/cards/MK/mk_form1_footer.html";
                        break;
                    }
                }

                String header = "", body = "", footer = "";
                try {
                    List<String> lines = Files.readAllLines(Paths.get(formheader),
                            Charset.defaultCharset());
                    for (String line : lines) {
                        header += line;//System.out.println(line);
                    }
                    // int tt=mcard.getOperations().size();
                    for (int c = 0; c < mcard.getOperations().size(); c++) {
                        lines = Files.readAllLines(Paths.get(formbody),
                                Charset.defaultCharset());
                        for (String line : lines) {
                            body += line;//System.out.println(line);
                        }
                        //Operations oper  = operations.get(c);//(Operations)db.getObject(Operations.class.getName(), operations.get(c));
                        body = body.replace("{ceh}", operations.get(c).getCeh()).replace("{uch}", operations.get(c).getUch());
                        body = body.replace("{rm}", operations.get(c).getRm()).replace("{oper}", String.valueOf(operations.get(c).getNumber()));
                        body = body.replace("{opername}", operations.get(c).getOpername()).replace("{sm}", operations.get(c).getSm());
                        body = body.replace("{prof}", operations.get(c).getProf()).replace("{r}", operations.get(c).getR());
                        body = body.replace("{ut}", operations.get(c).getUt()).replace("{kr}", operations.get(c).getKr());
                        body = body.replace("{koid}", operations.get(c).getKoid()).replace("{en}", operations.get(c).getEn());
                        body = body.replace("{Ksh}", operations.get(c).getKst()).replace("{Tpz}", operations.get(c).getTpz()).replace("{Tsh}", operations.get(c).getTsh());
                        body = body.replace("{devicename}", operations.get(c).getDevicename()).replace("{docname}", operations.get(c).getDocname());

                    }
                    lines = Files.readAllLines(Paths.get(formfooter),
                            Charset.defaultCharset());
                    for (String line : lines) {
                        footer += line;//System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // raskommentit header=header.replace("{znak}", mcard.getProfile_size().substring(0, mcard.getProfile_size().length()-(mcard.getProfile_size().length()-1)));
                header = header.replace("{digit}", mcard.getProfile_size());
                header = header.replace("{count}", "");
                header = header.replace("{partname}", mcard.getPartname()).replace("{number1}", mcard.getNumber1());
                header = header.replace("{number2}", mcard.getNumber2()).replace("{matname}", mcard.getMatherialname());
                header = header.replace("{firmname}", mcard.getFirmname()).replace("{kod}", mcard.getKod());
                header = header.replace("{ev}", mcard.getEv()).replace("{md}", mcard.getMd());
                header = header.replace("{en}", mcard.getEn()).replace("{nrash}", mcard.getNrash());
                header = header.replace("{kim}", mcard.getKim()).replace("{kodzagotovki}", mcard.getKodzagotovki());
                header = header.replace("{kd}", mcard.getKd()).replace("{mz}", mcard.getMz());
                String form = header.replace("{row}", body).replace("{footer}", footer);
                JSMediator.exec(getSession(),
                        //WebKitFrame.getInstance().browserExecutor(
                        "getUICore().setTabBody('"
                        + WebKitUtil.prepareToJS("<div class='tab-pane' id='m_card" + jsonObject.getString("id_mk") + "'>" + form + "</div>")
                        + "');");
            }

        } catch (JSONException /*
                 * | WebKitException
                 */ ex) {
            System.err.println(ex);
            Logger.getGlobal().log(Level.WARNING, json, ex);
        }
    }

    public String getTechCardsPanel(JSONObject jsonObject) {
        String model = "";
        try {
            if (!emptyTable) {


                model = ""
                        + "<div class='tab-pane' id='tab" + jsonObject.getString("id_bd") + "'>"
                        + "<div style='width:100%; height:750px; overflow:auto;' id='macTable'> "
                        + "<div style='width: 700px; margin:0 0 10px 50px' id=cardPanel" + jsonObject.getString("id_bd") + ">" + dl.getModel() + "</div>" + "<div style='padding-left:50px'>" + btnAddTechCard.getModel() + "</div>" + "</div>"
                        + "</div>";





            } else {
                model = ""
                        + "<div class='tab-pane' id='tab" + jsonObject.getString("id_bd") + "'>"
                        + "<div style='width:100%; height:750px; overflow:auto;' id='macTable'> "
                        + "<div id=cardPanel" + jsonObject.getString("id_bd") + ">" + "<div style='margin:50px 0 2px 50px; width:500px' class=\"alert alert-info\">В базе данных пока нету изделий. Вы можете добавить изделие с помощью кнопки добавить.</div>" + "</div>" + "<div style='padding-left:50px'>" + btnAddTechCard.getModel() + "</div>" + "</div>"
                        + "</div>";

            }

        } catch (JSONException ex) {
            System.err.println(ex);
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }

        return model;

    }

    @Override
    public String getIdentificator() {
        return ComponentsView.class.getName();
    }
}
