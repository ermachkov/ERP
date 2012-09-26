package ru.sibek.plugin.neworder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.ResourceBundle;
import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.document.SalesItem;
import org.ubo.goods.Goods;
import org.ubo.money.Money;
import org.ubo.partner.Agent;
import org.ubo.quantity.Quantity;
import org.ubo.rules.RuleItem;
import org.ubo.service.Service;
import org.ubo.tree.TreeLeaf;
import org.ubo.tree.TreeLeafBasic;
import org.ubo.utils.Result;
import org.uui.component.OrderPanel;
import org.uui.db.DataBase;
import org.uui.table.*;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.core.ui.PopupPanel;


public class GoodsOrderPanel extends OrderPanel {

    private DataBase dataBase;
    private CarssierCore core = CarssierCore.getInstance();
    private String session;

    public GoodsOrderPanel(String sessionId, DataBase dataBase, String lockedText) {
        super(dataBase, lockedText);
        super.setEditable(true);
        this.session = sessionId;
        this.dataBase = dataBase;

        this.macTableModel = new MacTableModel(sessionId, true, new MacTableSummator(new int[]{5}));
        MacTableHeaderModel mth = new MacTableHeaderModel();
        MacHeaderColumn column = new MacHeaderColumn(
                ResourceBundle.getBundle("GoodsPanel").getString("name"),
                String.class, false);
        column.setColumnWidth("30%");
        mth.addHeaderColumn(column);

        mth.addHeaderColumn(new MacHeaderColumn(ResourceBundle.getBundle("GoodsPanel").getString("price"), Double.class, false));
        MacHeaderColumn columnCount = new MacHeaderColumn(ResourceBundle
                .getBundle("GoodsPanel").getString("count"), Double.class, false);
        columnCount.setColumnWidth("20%");
        mth.addHeaderColumn(columnCount);
        mth.addHeaderColumn(new MacHeaderColumn(ResourceBundle.getBundle("GoodsPanel").getString("discount"), Double.class, false));
        mth.addHeaderColumn(new MacHeaderColumn(ResourceBundle.getBundle("GoodsPanel").getString("sum"), Double.class, false));

        macTableModel.setHeader(mth);
        macTableModel.setCssClass("macTable");
        macTableModel.setId("orderTable");
        macTableModel.setNavigatorEnable(false);
        macTableModel.setNavigatorDateSelectorEnabled(false);
        macTableModel.setNavigatorShowingAlways(false);
    }

    public void setSession(String session) {
        this.session = session;
    }

    public void refresh() {
        setOrder(getOrder());
    }

    @Override
    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            parseOrder();
            updateTotalDiscount();

        } else {
            setDescription(null);
        }
    }

    public Order newOrder() {
        if (order == null) {
            order = Order.newOrder();
            order.setSupplier(supplier);
        }

        return order;
    }

    @Override
    public void addTreeLeaves(ArrayList<TreeLeaf> dropList) {
        treeLeafList.addAll(dropList);

        if (order == null) {
            order = Order.newOrder();
            order.setSupplier(supplier);

            Result r = core.getAgent(12); // 12 is private person
            if (!r.isError()) {
                customer = (Agent) r.getObject();
                order.setCustomer(customer);
            }
        }

        parseOrder();
    }

    private void parseOrder() {
        WebKitEventBridge.getInstance().cleanMacTable(getSession(), macTableModel);

        for (TreeLeaf tl : treeLeafList) {
            if ((tl instanceof TreeLeafBasic)) {
                TreeLeafBasic treeLeaf = (TreeLeafBasic) tl;

                Object sObj = treeLeaf.getObject();
                if ((sObj instanceof SalesItem)) {
                    SalesItem salesItem = (SalesItem) treeLeaf.getObject();

                    BigDecimal count = new BigDecimal("1");
                    boolean isModify = false;
                    for (OrderRow o : order.getOrderRows()) {
                        if ((o.getSalesItem(dataBase).getType() == salesItem.getType()) && (o.getSalesItem(dataBase).getDbId() == salesItem.getDbId())) {
                            count = o.getCount().add(new BigDecimal("1"));
                            o.setCount(count);
                            isModify = true;
                            break;
                        }
                    }

                    if (!isModify) {
                        order.addOrderItem(salesItem, getPrice(salesItem), count, getDiscount(salesItem));
                    }
                }
            }
        }
        treeLeafList.clear();

        ArrayList<MacTableRow> list = new ArrayList<>();
        for (OrderRow orderRow : order.getOrderRows()) {
            MacTableRow macTableRow = new MacTableRow();
            ArrayList<MacTableCell> macTableCells = new ArrayList<>();
            macTableCells.add(new MacTableCell(getSession(), orderRow.getShortName(dataBase), false));

            if (orderRow.getSalesItem(dataBase).getType() == SalesItem.SERVICE) {
                Service s = (Service) dataBase.getObject(Service.class.getName(), orderRow.getSalesItem(dataBase).getId());

                if (s.isIndividualPrice()) {
                    macTableCells.add(new MacTableCell(getSession(), new BigDecimal(Money.formatToMoney(orderRow.getPrice().doubleValue())), true));
                } else {
                    macTableCells.add(new MacTableCell(getSession(), new BigDecimal(Money.formatToMoney(orderRow.getPrice().doubleValue())), false));
                }
            } else {
                macTableCells.add(new MacTableCell(getSession(), new BigDecimal(Money.formatToMoney(orderRow.getPrice().doubleValue())), false));
            }

            if (orderRow.getSalesItem(dataBase).getType() == SalesItem.SERVICE) {
                MacTableCell cell = new MacTableCell(getSession(), new BigDecimal(orderRow.getCount().intValue()), true, true);
                cell.setValue("spinnerStyle", "width:120px;");
                macTableCells.add(cell);

            } else {
                Result rg = core.getGoods(orderRow.getSalesItem(this.dataBase).getId());
                if (rg.isError()) {
                    JSMediator.alert(session, rg.getReason());
                    continue;
                }

                if (((Goods) rg.getObject()).isSeparable()) {
                    MacTableCell cell = new MacTableCell(getSession(), new BigDecimal(Quantity.format(orderRow.getCount())), true, true);
                    cell.setValue("spinnerStyle", "width:120px;");
                    macTableCells.add(cell);

                } else {
                    MacTableCell cell = new MacTableCell(getSession(), new BigDecimal(orderRow.getCount().intValue()), true, true);
                    cell.setValue("spinnerStyle", "width:120px;");
                    macTableCells.add(cell);
                }
            }

            boolean isCanSetDiscount = false;
            Result result = core.getRulesItemByKey(session, "canSetDiscountAtOrderItem");
            if (!result.isError()) {
                isCanSetDiscount = ((Boolean) ((RuleItem) result.getObject()).getValue()).booleanValue();
            } else {
                JSMediator.alert(session, result.getReason());
            }

            macTableCells.add(new MacTableCell(getSession(), orderRow.getDiscount(), isCanSetDiscount));
            macTableCells.add(new MacTableCell(getSession(), orderRow.getSumWithDiscount(), false));

            macTableRow.setRowData(macTableCells);
            macTableRow.setValue(orderRow);

            result = core.getRulesItemByKey(session, "canSetIndividualPriceForService");
            if (!result.isError()) {
                if (((Boolean) ((RuleItem) result.getObject()).getValue()).booleanValue()) {
                    list.add(macTableRow);
                } else {
                    PopupPanel popupPanel = new PopupPanel(session);
                    popupPanel.setTitle("<span style='color:red;'>Предупреждение</span>");
                    popupPanel.setPanel("Извините, но вам запрещено оформлять заказы на услуги с индивидульной ценой.");

                    popupPanel.showPanel();
                }

            } else {
                JSMediator.alert(session, result.getReason());
            }
        }

        this.macTableModel.setData(list);
    }

    private BigDecimal getPrice(SalesItem salesItem) {
        return salesItem.getSalePrice(dataBase);
    }

    private BigDecimal getDiscount(SalesItem salesItem) {
        BigDecimal discount = new BigDecimal("0");
        return discount;
    }
    
    public String getSession(){
        return session;
    }
}
