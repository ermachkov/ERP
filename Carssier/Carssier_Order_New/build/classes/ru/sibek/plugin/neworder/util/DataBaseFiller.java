package ru.sibek.plugin.neworder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.ubo.document.ServiceCommandment;
import org.ubo.employee.Crew;
import org.ubo.employee.Employee;
import org.ubo.goods.Goods;
import org.ubo.money.Money;
import org.ubo.partner.Account;
import org.ubo.partner.Address;
import org.ubo.partner.Agent;
import org.ubo.partner.Contacts;
import org.ubo.service.Service;
import org.ubo.storage.Storage;
import org.ubo.tree.Trees;
import org.ubo.utils.ImageLoader;
import org.ubo.utils.Result;
import org.uui.component.TextField;
import org.uui.db.DataBase;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.core.ui.PopupPanel;

import ru.sibek.database.CarssierDataBase;
import ru.sibek.plugin.neworder.GoodsPanel;

public class DataBaseFiller {

    private ArrayList<Map<String, String>> treeList = new ArrayList();
    private ArrayList<Map<String, String>> treeGoodsList = new ArrayList();
    private ArrayList<Map<String, String>> treeGoodsPriceList = new ArrayList();
    private AtomicBoolean isFind = new AtomicBoolean(false);
    private AtomicInteger pos = new AtomicInteger(0);
    private AtomicInteger lastPos = new AtomicInteger(-1);
    private String parent;
    private Path path;
    private boolean isReturn = false;
    private DataBase dataBase;
    private CarssierCore core;
    private ImageIcon goodsIcon;
    private ImageIcon serviceIcon;
    private String iconsPath, session;
    private GoodsPanel goodsPanel;

    public DataBaseFiller(GoodsPanel goodsPanel) {
        this.goodsPanel = goodsPanel;
        this.dataBase = CarssierDataBase.getDataBase();
        this.core = CarssierCore.getInstance();

        String homePath = System.getProperty("user.home") + File.separator + ".saas" + File.separator + "app" + File.separator;

        this.iconsPath = (homePath + "images" + File.separator + "icons" + File.separator);

        this.serviceIcon = ImageLoader.getInstance().getFromFile(this.iconsPath + "service.png");

        this.goodsIcon = ImageLoader.getInstance().getFromFile(this.iconsPath + "goods.png");
    }

    public void setSession(String session) {
        this.session = session;
    }

    public void start() throws IOException {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    JSMediator.showLoggerPanel(session);
                    JSMediator.showLockPanel(session);

                    initPriceTable();
                    initGoodsList();
                    convert();

                    JSMediator.hideLoggerPanel(session);
                    JSMediator.hideLockPanel(session);

                    DataBaseFiller.this.goodsPanel.showWorkPanel();
                } catch (IOException e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };
        Executors.newSingleThreadExecutor().execute(r);
    }

    private void initPriceTable() throws IOException {
        Path p = Paths.get(System.getProperty("user.home"), new String[]{".saas", "app", "config", "gs", "goods_in_out.csv"});
        BufferedReader br = Files.newBufferedReader(p, Charset.defaultCharset());
        String str;
        while ((str = br.readLine()) != null) {
            Map map = new LinkedHashMap();

            String[] arr = str.split(",");
            map.put("id", arr[0]);
            map.put("id_goods", arr[1]);
            map.put("doc_name", arr[2]);
            map.put("count_in", arr[3]);
            map.put("sum_in", arr[4]);
            map.put("count_out", arr[5]);
            this.treeGoodsPriceList.add(map);
        }
    }

    private void initGoodsList() {
        Result r = this.core.getDefaultSupplier();
        if (r.isError()) {
            createDefaultSupplier();
        }
        try {
            Path p = Paths.get(System.getProperty("user.home"), new String[]{".saas", "app", "config", "gs", "goods.csv"});
            BufferedReader br = Files.newBufferedReader(p, Charset.defaultCharset());
            String str;
            while ((str = br.readLine()) != null) {
                Map map = new LinkedHashMap();
                String[] arr = str.split(",");
                map.put("idGoods", arr[0]);
                map.put("idTree", arr[1]);
                map.put("idGoodsType", arr[2]);
                map.put("shortName", arr[3].replaceAll("\"", ""));
                this.treeGoodsList.add(map);
            }
        } catch (Exception e) {
            System.err.println(e);
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void convert() throws IOException {
        Path p = Paths.get(System.getProperty("user.home"), new String[]{".saas", "app", "config", "gs", "tree.csv"});
        BufferedReader br = Files.newBufferedReader(p, Charset.defaultCharset());
        String str;
        while ((str = br.readLine()) != null) {
            Map map = new LinkedHashMap();
            String[] arr = str.split(",");
            map.put("id", arr[0]);
            map.put("subid", arr[1]);
            map.put("pos", arr[2]);
            map.put("name", arr[3].replaceAll("\"", ""));
            this.treeList.add(map);
        }

        parseTree();
    }

    private void parseTree() {
        System.out.println(this.treeList);
        for (Map map : this.treeList) {
            if (((String) map.get("subid")).equals("0")) {
                this.path = Paths.get("/" + (String) map.get("name"), new String[0]);
                System.out.println("Path = " + this.path);
                JSMediator.setLoggerInfo(session, new StringBuilder().append("Path = ").append(this.path).toString());
                createFolder(this.path);
                this.isReturn = false;
                this.pos.set(0);
                this.lastPos.set(-1);
                treeWalker((String) map.get("id"));
            }
        }
    }

    private void createFolder(Path p) {
        JSMediator.setLoggerInfo(session, new StringBuilder().append("Добавляю ").append(p.toString()).append("...").toString());

        Trees.createTreeFolderByPath("/GoodsAndService" + p.toString(), this.dataBase);
    }

    private void addGoodsService(Path p, Map<String, String> m) {
        JSMediator.setLoggerInfo(session, new StringBuilder().append("Добавляю ").append(p.toString()).append(" ").append((String) m.get("shortName")).append("...").toString());

        if (((String) m.get("idGoodsType")).equals("1")) {
            Result result = this.core.addGoods("/GoodsAndService" + p.toString(), p.getName(p.getNameCount() - 1) + " " + (String) m.get("shortName"), "img/icons/goods.png");

            if (result.isError()) {
                System.err.println(result);
            } else {
                System.out.println(result);
                JSMediator.setLoggerInfo(session, new StringBuilder().append("Результат ").append(result.getReason()).toString());

                Result r = this.core.setGoodsSalaryPercent((Goods) result.getObject(), new BigDecimal("10"));
                if (r.isError()) {
                    System.err.println(r);
                } else {
                    System.out.println(r);
                    int lastId = 0;
                    BigDecimal sum = BigDecimal.ZERO;
                    BigDecimal sumIn = BigDecimal.ZERO;
                    BigDecimal count = BigDecimal.ZERO;
                    String reason = "";
                    for (Map mapPrice : this.treeGoodsPriceList) {
                        if ((((String) mapPrice.get("id_goods")).equals(m.get("idGoods")))
                                && (Integer.parseInt((String) mapPrice.get("id")) > lastId)
                                && (Integer.parseInt((String) mapPrice.get("count_in")) > 0)) {
                            count = new BigDecimal((String) mapPrice.get("count_in"));
                            sum = new BigDecimal((String) mapPrice.get("sum_in"));
                            sumIn = new BigDecimal((String) mapPrice.get("sum_in"));
                            lastId = Integer.parseInt((String) mapPrice.get("id"));
                            reason = (String) mapPrice.get("doc_name");
                        }

                    }

                    Goods goods = (Goods) result.getObject();
                    String c;
                    if (count.toString() == null) {
                        c = "1";

                    } else if (count.toString().equals("0")) {
                        c = "1";

                    } else if (count.toString().equals("")) {
                        c = "1";
                    } else {
                        c = count.toString();
                    }
                    goods.setSalePrice(Money.DIVIDE(sum.toString(), c), this.dataBase);
                    this.core.modifyGoods(goods);
                    this.core.addGoodsToStorage(((Goods) result.getObject()).getId(), count, sumIn, sum);
                    Storage storage = (Storage) this.core.getStorage(((Goods) result.getObject()).getDefaultStorage()).getObject();
                    storage.setLastReason(((Goods) result.getObject()).getId(), reason);
                    this.core.modifyStorage(storage);
                }
            }
        } else {
            Result r = this.core.getDefaultCrew();
            if (r.isError()) {
                JSMediator.alert(session, r.getReason());
                return;
            }

            Result result = this.core.addService("/GoodsAndService" + p.toString(), p.getName(p.getNameCount() - 1) + " " + (String) m.get("shortName"), "img/icons/service.png", ((Crew) r.getObject()).getId());

            if (result.isError()) {
                System.err.println(result);
            } else {
                System.out.println(result);
                r = this.core.setServiceSalaryPercent((Service) result.getObject(), new BigDecimal("10"));
                if (r.isError()) {
                    System.err.println(r);
                } else {
                    System.out.println(r);
                    JSMediator.setLoggerInfo(session, new StringBuilder().append("Результат ").append(result.getReason()).toString());

                    Result rIcon = Result.newResult(true, "Default");

                    String pIcons = "img/icons/";
                    Service s = (Service) r.getObject();

                    if (s.getShortName().toLowerCase().indexOf("балансировка") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "balancing.png");
                    } else if (s.getShortName().toLowerCase().indexOf("подкачка") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "inflating.png");
                    } else if (s.getShortName().toLowerCase().indexOf("прокол") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "c_repair.png");
                    } else if (s.getShortName().toLowerCase().indexOf("камер") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "c_repair.png");
                    } else if (s.getShortName().toLowerCase().indexOf("покраска") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "painting.png");
                    } else if (s.getShortName().toLowerCase().indexOf("бортовка") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "stripping.png");
                    } else if (s.getShortName().toLowerCase().indexOf("дисков") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "w_repair.png");
                    } else if (s.getShortName().toLowerCase().indexOf("покрыш") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "t_repair.png");
                    } else if (s.getShortName().toLowerCase().indexOf("монтаж") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "w_fitting.png");
                    } else if (s.getShortName().toLowerCase().indexOf("съём") != -1) {
                        rIcon = this.core.setIconService(s, pIcons + "w_fitting.png");
                    }

                    if (rIcon.isError()) {
                        System.err.println(rIcon);
                    } else {
                        System.out.println(rIcon);
                        for (Map mapPrice : this.treeGoodsPriceList) {
                            if ((((String) mapPrice.get("id_goods")).equals(m.get("idGoods")))
                                    && (!((String) mapPrice.get("count_in")).equals("0"))) {
                                Result rServiceCommandment = this.core.createServiceCommandment((String) mapPrice.get("doc_name"));
                                if (((String) mapPrice.get("sum_in")).equals("-1")) {
                                    s.setIndividualPrice(true);
                                    this.core.setServicePrice(s, BigDecimal.ZERO, (ServiceCommandment) rServiceCommandment.getObject());
                                } else {
                                    this.core.setServicePrice(s, new BigDecimal((String) mapPrice.get("sum_in")), (ServiceCommandment) rServiceCommandment.getObject());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void treeWalker(String id) {
        for (Map map : this.treeList) {
            String subid = (String) map.get("subid");
            if (id.equals(subid)) {
                this.pos.set(this.pos.get() + 3);
                if (this.lastPos.get() != this.pos.get()) {
                    if (this.lastPos.get() < this.pos.get()) {
                        this.path = Paths.get(this.path.toString(), new String[]{((String) map.get("name")).replaceAll("/", "\\\\")});
                        System.out.println("Path = " + this.path);
                        createFolder(this.path);
                        for (Map m : getGoodsList((String) map.get("id"))) {
                            System.out.println(m);
                            addGoodsService(this.path, m);
                        }
                    } else {
                        if (this.isReturn) {
                            this.isReturn = false;
                            Path pp = this.path;
                            for (int i = 0; i < (this.lastPos.get() - this.pos.get()) / 3 + (this.lastPos.get() - this.pos.get()) / 3; i++) {
                                pp = pp.getParent();
                            }
                            this.path = Paths.get("" + pp, new String[]{((String) map.get("name")).replaceAll("/", "\\\\")});
                        } else {
                            this.path = Paths.get(this.path.getParent().toString(), new String[]{((String) map.get("name")).replaceAll("/", "\\\\")});
                        }

                        System.out.println("Path = " + this.path);
                        createFolder(this.path);
                        for (Map m : getGoodsList((String) map.get("id"))) {
                            System.out.println(m);
                            addGoodsService(this.path, m);
                        }
                    }
                } else {
                    this.path = Paths.get(this.path.getParent().toString(), new String[]{((String) map.get("name")).replaceAll("/", "\\\\")});
                    System.out.println("Path = " + this.path);
                    createFolder(this.path);
                    for (Map m : getGoodsList((String) map.get("id"))) {
                        System.out.println(m);
                        addGoodsService(this.path, m);
                    }
                    this.isReturn = true;
                }

                this.lastPos.set(this.pos.get());
                treeWalker((String) map.get("id"));
            }
        }

        this.pos.set(this.pos.get() - 3);
    }

    private ArrayList<Map<String, String>> getGoodsList(String idTree) {
        ArrayList list = new ArrayList();
        for (Map map : this.treeGoodsList) {
            if (((String) map.get("idTree")).equals(idTree)) {
                list.add(map);
            }
        }
        return list;
    }

    private void createDefaultSupplier() {
        TextField txtSupplier = new TextField(getSession());
        txtSupplier.setStyle("width:96%;");

        TextField txtAddress = new TextField(getSession());
        txtAddress.setStyle("width:96%;");

        TextField txtPhone = new TextField(getSession());
        txtPhone.setStyle("width:96%;");

        TextField txtOwner = new TextField(getSession());
        txtOwner.setStyle("width:96%;");

        PopupPanel popupPanel = new PopupPanel(session);
        popupPanel.setTitle("Создание поставщика");
        String panel = "<p>Для работы с товарами необходимо указать поставщика товаров/услуг по умолчанию.</p><p>Здесь имеется в виду ваша компания. В полях расположенном ниже введите краткое название свой компании, адрес и телефон.</p><span style='font-size:70%'>В дальнейшем эту информацию можно будет отредактировать в разделе <strong>«Справочники»</strong></span><br/><br/>Краткое название<br/>" + txtSupplier.getModel() + "<br/>" + "Адрес<br/>" + txtAddress.getModel() + "<br/>" + "Телефон<br/>" + txtPhone.getModel() + "<br/>" + "Владелец<br/>" + txtOwner.getModel();

        popupPanel.setPanel(panel);

        boolean isAdded = false;
        while (!isAdded) {
            popupPanel.showPanel();
            if (!txtSupplier.getText().equals("")) {
                isAdded = true;
                continue;
            }
            panel = "<p>Для работы с товарами необходимо указать поставщика товаров/услуг по умолчанию.</p><p>Здесь имеется в виду ваша компания. В полях расположенном ниже введите краткое название свой компании,адрес и телефон.</p><span style='font-size:70%'>В дальнейшем эту информацию можно будет отредактировать в разделе <strong>«Справочники»</strong></span><br/><br/>Краткое название <span style='color:red; font-size:60%;'>Не может быть пустым!</span><br/>" + txtSupplier.getModel() + "<br/>" + "Адрес<br/>" + txtAddress.getModel() + "<br/>" + "Телефон<br/>" + txtPhone.getModel() + "<br/>" + "Владелец<br/>" + txtOwner.getModel();

            popupPanel.setPanel(panel);
        }

        Agent a = new Agent();
        a.setShortName(txtSupplier.getText());
        a.setFullName(txtSupplier.getText());

        Address address = new Address();
        address.setSimplyAddress(txtAddress.getText());
        a.setDefaultAddress(address);

        Contacts contact = new Contacts();
        contact.setPhone("default", txtPhone.getText());
        a.setDefaultContacts(contact);

        Account account = new Account();
        a.setAccount("default", account);
        a.setDefaultAccount(account);

        Result r = core.addAgent(a);

        if (!r.isError()) {
            a = (Agent) r.getObject();
            this.core.setDefaultSupplier(a);

            Employee owner = new Employee();
            owner.setRole("owner");
            if (txtOwner.getText().equals("")) {
                owner.setShortName("Владелец");
                owner.setFullName("Владелец");
            } else {
                owner.setShortName(txtOwner.getText());
                owner.setFullName(txtOwner.getText());
            }

            this.core.addEmployee(owner);
        } else {
            try {
                JSMediator.alert(session, r.getReason());
            } catch (Exception e) {
            }
        }
    }
    
    public String getSession(){
        return session;
    }
}