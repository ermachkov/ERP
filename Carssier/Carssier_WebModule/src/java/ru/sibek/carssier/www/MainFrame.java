/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.carssier.www;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.goods.Goods;
import org.ubo.service.Service;
import org.ubo.tree.*;
import org.ubo.utils.Result;
import org.ubo.www.Page;
import org.ubo.www.PriceACL;
import org.uui.component.Button;
import org.uui.component.Label;
import org.uui.component.TextField;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.table.*;
import org.uui.webkit.WebKitUtil;

/**
 *
 * @author developer
 */
public class MainFrame {

    private String session, realPath, requestURI, login;
    private Label lblAbout, lblFullPrice, lblWebPriceList, lblAddress,
            lblContacts, lblService, lblShop;
    private MacTableModel macTablePrice, macTableShop, macTableBasket;
    private Core core;
    private Order order;
    private Path pRealPath;
    private TextField txtUserName, txtPhone, txtEmail;
    private Button btnOrder;
    private UserPanel userPanel;
    private HttpSession httpSession;
    private ArrayList<PriceACL> priceACLList;

    public MainFrame(HttpSession httpSession, String session, String realPath, String requestURI) {
        this.httpSession = httpSession;
        this.realPath = realPath;
        this.pRealPath = Paths.get(realPath);
        Path p = Paths.get(requestURI);
        login = p.getName(p.getNameCount() - 1).toString();
        this.session = session + "-" + login;
        this.requestURI = requestURI;

        core = Core.getInstance();

        userPanel = new UserPanel(httpSession, getSession(), realPath, requestURI);

        lblAbout = new Label(getSession(), "О нас");
        lblAbout.setCssClass("menu");
        lblAbout.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showPage("about");
            }
        });

        lblFullPrice = new Label(getSession(), "Прайс-лист");
        lblFullPrice.setCssClass("menu");
        lblFullPrice.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showFullPrice();
            }
        });

        lblWebPriceList = new Label(getSession(), "Заказать");
        lblWebPriceList.setCssClass("menu");
        lblWebPriceList.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showWebPrice();
            }
        });

        lblAddress = new Label(getSession(), "Адрес");
        lblAddress.setCssClass("menu");
        lblAddress.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showPage("address");
            }
        });

        lblContacts = new Label(getSession(), "Контакты");
        lblContacts.setCssClass("menu");
        lblContacts.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showPage("contacts");
            }
        });

        lblService = new Label(getSession(), "Услуги");
        lblService.setCssClass("menu");
        lblService.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showPage("service");
            }
        });

        lblShop = new Label(getSession(), "Магазин");
        lblShop.setCssClass("menu");
        lblShop.addUIEventListener(new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                showPage("shop");
            }
        });

        init();
    }

    private void init() {
        MacTableHeaderModel mth = new MacTableHeaderModel();
        mth.addHeaderColumn(new MacHeaderColumn("Наименование", String.class, false));
        mth.addHeaderColumn(new MacHeaderColumn("Цена", String.class, false));

        macTablePrice = new MacTableModel(getSession(), true);
        macTablePrice.setHeader(mth);
        macTablePrice.setCssClass("macTable");
        macTablePrice.setId("fullPriceTable");

        macTableShop = new MacTableModel(getSession(), true);
        macTableShop.setHeader(mth);
        macTableShop.setCssClass("macTable");
        macTableShop.setId("shopTable");
        macTableShop.addUIEventListener(getShopTableListener());

        MacTableHeaderModel mthBasket = new MacTableHeaderModel();
        mthBasket.addHeaderColumn(new MacHeaderColumn("Наименование", String.class, false));
        mthBasket.addHeaderColumn(new MacHeaderColumn("Цена", Number.class, false));
        mthBasket.addHeaderColumn(new MacHeaderColumn("Кол-во", Number.class, false));
        mthBasket.addHeaderColumn(new MacHeaderColumn("Сумма", Number.class, false));
        macTableBasket = new MacTableModel(getSession(), true, new MacTableSummator(4));
        macTableBasket.setHeader(mthBasket);
        macTableBasket.setCssClass("macTableBasket");
        macTableBasket.setId("basketTable");
        macTableBasket.setMode(MacTableModel.MODE_EDIT);
        macTableBasket.addUIEventListener(getBasketTableEventListener());
        macTableBasket.getMacTableRemoveButton().addUIEventListener(getBasketRemoveListener());

        txtUserName = new TextField(getSession());
        txtUserName.setStyle("width:98%");

        txtPhone = new TextField(getSession());
        txtPhone.setStyle("width:98%");

        txtEmail = new TextField(getSession());
        txtEmail.setStyle("width:98%");

        btnOrder = new Button(getSession(), "Оформить");
        btnOrder.addUIEventListener(getButtonOrderListener());

        order = new Order();
        order.setRemote(true);
    }

    private UIEventListener getButtonOrderListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                if (userPanel.getLoggedAgent() == null) {
                    WebSocketBundle.getInstance().send(getSession(),
                            "getUICore().refreshBasketPanel('"
                            + WebKitUtil.prepareToJS(""
                            + "<div style='font-size:80%;'>"
                            + "<table width='100%' height='100%'>"
                            + "<tr>"
                            + "<td align='center' valign='middle'>"
                            + "Для отправки заказа Вам необходимо войти в систему "
                            + "или зарегистрироваться.<br/>"
                            + "Воспользуйтесь кнопкой <b>Войти/Зарегистрироваться</b>."
                            + "</td>"
                            + "</tr>"
                            + "</table>"
                            + "</div>")
                            + "');");
                } else {
                    order.setDate(new Date());
                    order.setCustomer(userPanel.getLoggedAgent());
                    order.setDescription("Оформлен через сайт");
                    Result result = core.addOrder(pRealPath, login, order);
                    if (result.isError()) {
                        WebSocketBundle.getInstance().send(getSession(),
                                "getUICore().showAlert('"
                                + WebKitUtil.prepareToJS(result.getReason())
                                + "');");

                    } else {
                        order = new Order();
                        order.setRemote(true);

                        WebSocketBundle.getInstance().send(getSession(),
                                "getUICore().showAlert(\"Заказ отправлен\");");

                        WebSocketBundle.getInstance().send(getSession(),
                                "getUICore().refreshBasketPanel('"
                                + WebKitUtil.prepareToJS(getBasketPanel())
                                + "');");
                    }
                }
            }
        };

        return listener;
    }

    private UIEventListener getBasketRemoveListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                for (MacTableRow row : macTableBasket.getCheckedRows()) {
                    order.getOrderRows().remove((OrderRow) row.getValue());
                }

                macTableBasket.removeCheckedRows();

                WebSocketBundle.getInstance().send(getSession(),
                        "getUICore().refreshBasketPanel('"
                        + WebKitUtil.prepareToJS(getBasketPanel())
                        + "');");
            }
        };

        return listener;
    }

    private UIEventListener getBasketTableEventListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    if (evt.getJSONObject().getString("eventType").equals("stopCellEditing")) {
                        int row = 0;
                        for (OrderRow orderRow : order.getOrderRows()) {
                            if (row == evt.getJSONObject().getInt("row")) {
                                orderRow.setCount(new BigDecimal(evt.getJSONObject().getString("value")));
                                WebSocketBundle.getInstance().send(getSession(),
                                        "getUICore().refreshBasketPanel('"
                                        + WebKitUtil.prepareToJS(getBasketPanel())
                                        + "');");
                                break;
                            }
                            row++;
                        }
                    }

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return listener;
    }

    private UIEventListener getShopTableListener() {
        UIEventListener listener = new UIEventListener() {

            @Override
            public void event(UIEvent evt) {
                try {
                    int row = evt.getJSONObject().getInt("row");
                    MacTableRow macTableRow = macTableShop.getRows().get(row);
                    Object value = macTableRow.getValue();
                    if (value == null) {
                        return;
                    }

                    if (value instanceof Goods) {
                        Goods goods = (Goods) value;
                        order.addOrderItem(
                                goods,
                                goods.getSalePrice(core.getDataBase(pRealPath, login)),
                                BigDecimal.ONE, BigDecimal.ZERO);
                        System.out.println(goods);
                    }

                    if (value instanceof Service) {
                        Service service = (Service) value;
                        order.addOrderItem(
                                service,
                                service.getSalePrice(core.getDataBase(pRealPath, login)),
                                BigDecimal.ONE, BigDecimal.ZERO);
                        System.out.println(service);
                    }

                    WebSocketBundle.getInstance().send(getSession(),
                            "getUICore().refreshBasketPanel('"
                            + WebKitUtil.prepareToJS(getBasketPanel())
                            + "');");
                    WebSocketBundle.getInstance().send(getSession(),
                            "getUICore().hideToolTip();");

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, session, e);
                }
            }
        };

        return listener;
    }

    private String getBasketPanel() {
        String model;

        ArrayList<MacTableRow> rows = new ArrayList<>();
        for (OrderRow orderRow : order.getOrderRows()) {
            MacTableRow row = new MacTableRow();
            row.addCell(new MacTableCell(getSession(), orderRow.getSalesItem(
                    core.getDataBase(pRealPath, login)).getShortName(), false));
            row.addCell(new MacTableCell(getSession(), orderRow.getSalesItem(
                    core.getDataBase(pRealPath, login)).getSalePrice(core.getDataBase(pRealPath, login)), false));
            MacTableCell cellCount = new MacTableCell(getSession(), orderRow.getCount(), true);
            row.addCell(cellCount);
            row.addCell(new MacTableCell(getSession(), orderRow.getSumWithDiscount(), false));
            row.setValue(orderRow);

            rows.add(row);
        }

        macTableBasket.setData(rows);

        model = ""
                + "<div style='width:100%; font-size:80%;'>"
                + "<table width='100%'>"
                + "<tr>"
                + "<td>Ф.И.О.</td>"
                + "<td>" + txtUserName.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td>Email</td>"
                + "<td>" + txtEmail.getModel() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td>Телефон</td>"
                + "<td>" + txtPhone.getModel() + "</td>"
                + "</tr>"
                + "</table>"
                + "</div>";
        model = macTableBasket.getModel();
        model += "<div style='margin-top:15px;'>" + btnOrder.getModel() + "</div>";

        return model;
    }

    private void showWebPrice() {
        WebSocketBundle.getInstance().send(getSession(),
                "getUICore().setWaitPanelEnabled(true);");

        String model;
        Result r = core.getTreeGoodsAndService(pRealPath, login);
        if (r.isError()) {
            model = r.getReason();

        } else {
            Result rPriceACL = core.getPriceACL(pRealPath, login);
            priceACLList = new ArrayList<>();
            if(!rPriceACL.isError()){
                priceACLList = (ArrayList<PriceACL>)rPriceACL.getObject();
            }
            
            final ArrayList<MacTableRow> rows = new ArrayList<>();
            TreeBasic treeBasic = (TreeBasic) r.getObject();
            TreeNodeWalker tnw = new TreeNodeWalker(treeBasic.getRootFolder()) {

                @Override
                public TreeNodeVisitResult visitFolder(TreeFolder folder) {
                    MacTableRow row = new MacTableRow();
                    String arr[] = folder.getPath().replaceAll("//GoodsAndService", "").split("/");
                    row.addCell(new MacTableCell(getSession(), 
                            "<strong>" + arr[arr.length - 1] + "</strong>",
                            false));
                    row.addCell(new MacTableCell(getSession(), "", false));
                    rows.add(row);
                    return TreeNodeVisitResult.CONTINUE;
                }

                @Override
                public TreeNodeVisitResult visitLeaf(TreeLeaf leaf) {
                    PriceACL findPriceACL = null;
                    for(PriceACL priceACL : priceACLList){
                        if(((TreeLeafBasic)leaf).getId() == priceACL.getTreeLeafId()){
                            findPriceACL = priceACL;
                        }
                    }
                    
                    if(findPriceACL == null){
                        return TreeNodeVisitResult.CONTINUE;
                    }
                    
                    if(!findPriceACL.isAllowShow()){
                        return TreeNodeVisitResult.CONTINUE;
                    }
                    
                    if(!findPriceACL.isAllowUse()){
                        return TreeNodeVisitResult.CONTINUE;
                    }
                    
                    if (leaf.getContainer().getClassName().equals(Goods.class.getName())) {
                        Result r = core.getGoods(pRealPath, login, leaf.getContainer().getId());
                        if (r.isError()) {
                            return TreeNodeVisitResult.CONTINUE;
                        }
                        Goods goods = (Goods) r.getObject();

                        MacTableRow row = new MacTableRow();
                        row.addCell(new MacTableCell(getSession(), goods.getShortName(), false));
                        row.addCell(new MacTableCell(getSession(), 
                                "<div align='right' style='font-weight:bold;'>"
                                + goods.getSalePrice(core.getDataBase(pRealPath, login))
                                + "</div>",
                                false));
                        row.setValue(goods);
                        rows.add(row);

                    } else {
                        Result r = core.getService(pRealPath, login, leaf.getContainer().getId());
                        if (r.isError()) {
                            return TreeNodeVisitResult.CONTINUE;
                        }
                        Service service = (Service) r.getObject();

                        MacTableRow row = new MacTableRow();
                        row.addCell(new MacTableCell(getSession(), service.getShortName(), false));
                        row.addCell(new MacTableCell(getSession(), 
                                "<div align='right' style='font-weight:bold;'>"
                                + service.getSalePrice(core.getDataBase(pRealPath, login))
                                + "</div>",
                                false));
                        row.setValue(service);
                        rows.add(row);
                    }
                    return TreeNodeVisitResult.CONTINUE;
                }
            };
            tnw.start();

            macTableShop.setData(rows);
            model = ""
                    + "<div width='100%'>"
                    + "<div style='float:left; width:50%;'>"
                    + macTableShop.getModel()
                    + "</div>"
                    + "<div style='float:left; width:48%;' id='basketPanel'>"
                    + getBasketPanel()
                    + "</div>"
                    + "</div>";
        }

        WebSocketBundle.getInstance().send(getSession(),
                "getUICore().setWorkPanel('" + WebKitUtil.prepareToJS(model) + "');");

        WebSocketBundle.getInstance().send(getSession(),
                "getUICore().showToolTip('"
                + WebKitUtil.prepareToJS("Для добавления товара / услуги в "
                + "корзину, щелкните на строчке таблицы")
                + "');");

        WebSocketBundle.getInstance().send(getSession(),
                "getUICore().setWaitPanelEnabled(false);");
    }

    private void showFullPrice() {
        WebSocketBundle.getInstance().send(getSession(),
                "getUICore().setWaitPanelEnabled(true);");

        String model;
        Result r = core.getTreeGoodsAndService(pRealPath, login);
        if (r.isError()) {
            model = r.getReason();

        } else {
            Result rPriceACL = core.getPriceACL(pRealPath, login);
            priceACLList = new ArrayList<>();
            if(!rPriceACL.isError()){
                priceACLList = (ArrayList<PriceACL>)rPriceACL.getObject();
            }
            
            final ArrayList<MacTableRow> rows = new ArrayList<>();
            TreeBasic treeBasic = (TreeBasic) r.getObject();
            TreeNodeWalker tnw = new TreeNodeWalker(treeBasic.getRootFolder()) {

                @Override
                public TreeNodeVisitResult visitFolder(TreeFolder folder) {
                    MacTableRow row = new MacTableRow();
                    String arr[] = folder.getPath().replaceAll("//GoodsAndService", "").split("/");
                    row.addCell(new MacTableCell(getSession(), 
                            "<strong>" + arr[arr.length - 1] + "</strong>",
                            false));
                    row.addCell(new MacTableCell(getSession(), "", false));
                    rows.add(row);
                    return TreeNodeVisitResult.CONTINUE;
                }

                @Override
                public TreeNodeVisitResult visitLeaf(TreeLeaf leaf) {
                    PriceACL findPriceACL = null;
                    for(PriceACL priceACL : priceACLList){
                        if(((TreeLeafBasic)leaf).getId() == priceACL.getTreeLeafId()){
                            findPriceACL = priceACL;
                        }
                    }
                    
                    if(findPriceACL == null){
                        return TreeNodeVisitResult.CONTINUE;
                    }
                    
                    if(!findPriceACL.isAllowShow()){
                        return TreeNodeVisitResult.CONTINUE;
                    }
                    
                    if (leaf.getContainer().getClassName().equals(Goods.class.getName())) {
                        Result r = core.getGoods(pRealPath, login, leaf.getContainer().getId());
                        if (r.isError()) {
                            return TreeNodeVisitResult.CONTINUE;
                        }
                        Goods goods = (Goods) r.getObject();

                        MacTableRow row = new MacTableRow();
                        row.addCell(new MacTableCell(getSession(), goods.getShortName(), false));
                        row.addCell(new MacTableCell(getSession(), 
                                "<div align='right' style='font-weight:bold;'>"
                                + goods.getSalePrice(core.getDataBase(pRealPath, login))
                                + "</div>",
                                false));

                        rows.add(row);

                    } else {
                        Result r = core.getService(pRealPath, login, leaf.getContainer().getId());
                        if (r.isError()) {
                            return TreeNodeVisitResult.CONTINUE;
                        }
                        Service service = (Service) r.getObject();

                        MacTableRow row = new MacTableRow();
                        row.addCell(new MacTableCell(getSession(), service.getShortName(), false));
                        row.addCell(new MacTableCell(getSession(), 
                                "<div align='right' style='font-weight:bold;'>"
                                + service.getSalePrice(core.getDataBase(pRealPath, login))
                                + "</div>",
                                false));

                        rows.add(row);
                    }
                    return TreeNodeVisitResult.CONTINUE;
                }
            };
            tnw.start();

            macTablePrice.setData(rows);
            model = macTablePrice.getModel();
        }

        WebSocketBundle.getInstance().send(getSession(),
                "getUICore().setWorkPanel('" + WebKitUtil.prepareToJS(model) + "');");

        WebSocketBundle.getInstance().send(getSession(),
                "getUICore().setWaitPanelEnabled(false);");
    }

    private void showPage(String path) {
        Result rPage = core.getPageByName(pRealPath, login, path);
        String content = rPage.getReason();
        if (!rPage.isError()) {
            content = ((Page) rPage.getObject()).getContent();
            content = content.replaceAll("src=\"img/www", "src=\"sites/" + login + "/img/www");
            // img/www/me_small.png
            // sites/boris/img/www/me_small.png
        }
        WebSocketBundle.getInstance().send(getSession(),
                "getUICore().setWorkPanel('"
                + WebKitUtil.prepareToJS(content)
                + "');");
    }

    private String getSession() {
        return session;
    }

    public String getModel() {
        String s = getIndexFile(realPath, requestURI);
        String model = ""
                + "<div id='mainFrame'>"
                + "<div class='verticalBorder'></div>"
                + "<div class='menuHolder'>"
                + "<div align='center' class='btnMenuHolder'>" + lblAbout.getModel() + "</div>"
                + "<div align='center' class='btnMenuHolder'>" + lblService.getModel() + "</div>"
                + "<div align='center' class='btnMenuHolder'>" + lblShop.getModel() + "</div>"
                + "<div align='center' class='btnMenuHolder'>" + lblFullPrice.getModel() + "</div>"
                + "<div align='center' class='btnMenuHolder'>" + lblWebPriceList.getModel() + "</div>"
                + "<div align='center' class='btnMenuHolder'>" + lblContacts.getModel() + "</div>"
                + "<div align='center' class='btnMenuHolder'>" + lblAddress.getModel() + "</div>"
                + "</div>"
                + "<div class='verticalBorder'></div>"
                + "<div class='workPanelHolder'>"
                + "<div id='workPanel'></div>"
                + "</div>"
                + "</div>";
        s = s.replaceAll("\\{mainFrame\\}", model);

        s = s.replaceAll("\\{userPanel\\}", userPanel.getModel());

        s = s.replaceAll("\\{sess\\}", session);
        return s;
    }

    private String getIndexFile(String realPath, String requestURI) {
        String content;

        ///carssier/formula
        Path pURI = Paths.get(requestURI);
        Path p = Paths.get(realPath,
                pURI.getName(pURI.getNameCount() - 1).toString(),
                "index.html");
        try {
            content = new String(Files.readAllBytes(p));
        } catch (Exception e) {
            content = e.toString();
        }

        return content;
    }
}
