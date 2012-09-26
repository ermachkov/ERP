/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;
import org.ubo.datetime.DateTime;
import org.ubo.document.Order;
import org.ubo.document.OrderRow;
import org.ubo.document.ServiceCommandment;
import org.ubo.employee.Crew;
import org.ubo.employee.Employee;
import org.ubo.goods.Goods;
import org.ubo.money.Money;
import org.ubo.partner.Address;
import org.ubo.partner.Agent;
import org.ubo.partner.Contacts;
import org.ubo.print.MediaFormat;
import org.ubo.service.Service;
import org.ubo.storage.Storage;
import org.ubo.tree.Tree;
import org.ubo.tree.TreeNodeVisitResult;
import org.ubo.tree.TreeBasic;
import org.ubo.tree.TreeFolder;
import org.ubo.tree.TreeLeaf;
import org.ubo.tree.TreeNodeWalker;
import org.ubo.tree.Trees;
import org.ucm.cashmachine.CashMachineException;
import org.ups.print.Printer;
import org.uui.db.Condition;
import org.uui.db.DataBase;
import ru.sibek.business.core.Carssier;
import ru.sibek.business.core.CarssierCore;
import org.ubo.utils.Result;
import ru.sibek.business.report.ReportFactory;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ScenarioSimply {

    private CarssierCore core;
    private DataBase dataBase;
    //private static final Class[] parameters = new Class[]{URL.class};
    //private String coreLib = "";
    private Properties properties = new Properties();

    public ScenarioSimply(Path p) {
        try {
            properties.load(Files.newInputStream(p, StandardOpenOption.READ));

        } catch (IOException ex) {
            System.err.println(ex);
            System.exit(-1);
        }

//        try {
//            coreLib = properties.getProperty("pathToCoreGUI");
//            URL u = new File(coreLib).toURI().toURL();
//            URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
//            Class sysclass = URLClassLoader.class;
//            Method method = sysclass.getDeclaredMethod("addURL", parameters);
//            method.setAccessible(true);
//            method.invoke(sysloader, new Object[]{u});
//        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | MalformedURLException ex) {
//            Logger.getGlobal().log(Level.SEVERE, null, ex);
//        }
    }

    private void simpleScenario() {
        if (properties.getProperty("cleanDataBase").equals("true")) {
            cleanDataBase();
        }

        if (properties.getProperty("initCrews").equals("true")) {
            initCrews();
        }

        if (properties.getProperty("initEmployee").equals("true")) {
            initEmployee();
        }
        
        if (properties.getProperty("agentsCreator").equals("true")) {
            agentsCreator();
        }

        if (properties.getProperty("storagesCreator").equals("true")) {
            storagesCreator();
        }

        if (properties.getProperty("makeGoodsServiceTree").equals("true")) {
            makeGoodsServiceTree();
        }

        if (properties.getProperty("putGoodsInStorage").equals("true")) {
            putGoodsInStorage();
        }

        if (properties.getProperty("getRestGoods").equals("true")) {
            getRestGoods();
        }

        if (properties.getProperty("setPriceForSevices").equals("true")) {
            setPriceForSevices();
        }

        if (properties.getProperty("registerPerson").equals("true")) {
            registerPerson();
        }

        if (properties.getProperty("createOrders").equals("true")) {
            createOrders();
        }

        if (properties.getProperty("printZReport").equals("true")) {
            printZReport();
        }

        if (properties.getProperty("cancelReceipt").equals("true")) {
            cancelReceipt();
        }

        if (properties.getProperty("payOrder").equals("true")) {
            payOrder();
        }

        if (properties.getProperty("payOrderWithoutCashMachine").equals("true")) {
            payOrderWithoutCashMachine();
        }

        if (properties.getProperty("printOrder").equals("true")) {
            printOrder();
        }

        if (properties.getProperty("closeOrder").equals("true")) {
            closeOrder();
        }

        if (properties.getProperty("closeShift").equals("true")) {
            closeShift();
        }

        if (properties.getProperty("getSalaryReport").equals("true")) {
            getSalaryReport();
        }

        System.exit(0);
    }

    public void getRestGoods() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        System.out.println("================== getRestGoods ===============");
        ArrayList<Goods> list = dataBase.getAllObjectsList(Goods.class.getName());
        for (Goods goods : list) {
            String s = goods.getName() + " = " + core.getGoodsCountOnAllStorages(goods);
            System.out.println(s);
        }

        System.out.println("===============================================");
    }

    private void registerPerson() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        ArrayList<Employee> employeeList = core.getEmployeeList();
        for (Employee employee : employeeList) {
            Result r = core.putEmployeeToCrew(employee.getId(), employee.getDefaultCrew().getId());
            if (r.isError()) {
                System.err.println(r);
            } else {
                System.out.println(r);
            }
        }

    }

    private void initEmployee() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        String[] emloyees = new String[]{"Иванов", "Петров", "Сидоров", "Карпов",
            "Редькин", "Плотников", "Семенов", "Деревянко", "Гудбаев", "Неверкин"};
        //String[] emloyees = new String[]{"Иванов", "Иванов", "Иванов", "Иванов", "Иванов"};
        for (String str : emloyees) {
            Employee employee = new Employee();
            employee.setName(str);
            employee.setINN("" + (Math.random() * 100000));
            employee.setPFR("" + (Math.random() * 100000));
            Result r = core.addEmployee(employee);
            if (r.isError()) {
                System.err.println(r);
            } else {
                System.out.println(r);
            }
        }

        for (Employee e : core.getEmployeeList()) {
            System.out.println("Added Employee " + e);
        }
    }

    private void getSalaryReport() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        try {
            Result result = core.getReport(
                    ReportFactory.newReportFactory(
                    ReportFactory.REPORT_SALARY,
                    12),
                    true);

            if (result.isError()) {
                System.err.println(result);
            } else {
                System.out.println(result);
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void closeShift() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        Result result = core.closeShift();

        if (result.isError()) {
            System.err.println(result);
        } else {
            System.out.println(result);
        }
    }

    private void cancelReceipt() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        try {
            System.out.println(core.cancelReceipt(48));

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            System.err.println(ex);
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
    }

    private void printZReport() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        try {
            System.out.println(core.printZReport(48));

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            System.err.println(ex);
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
    }

    private void closeOrder() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        TreeBasic tree = (TreeBasic) Trees.getTree("OrdersInWork", dataBase,
                TreeBasic.class.getName());
        Set<TreeLeaf> setTreeLeaf = tree.getRootFolder().getSetTreeLeaf();
        for (TreeLeaf tl : setTreeLeaf) {
            Order order = (Order) dataBase.getObject(tl.getContainer().getClassName(),
                    tl.getContainer().getId());
            Result result = core.closeOrder(order);

            if (!result.isError()) {
                System.err.println(result);

            } else {
                System.out.println(result);
            }
        }
    }

    private void printOrder() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

//        TreeBasic tree = (TreeBasic) Trees.getTree("OrdersUnpayed", dataBase,
//                TreeBasic.class.getName());
//        Set<TreeLeaf> setTreeLeaf = tree.getRootFolder().getSetTreeLeaf();
//        Order lastOrder = null;
//        for (TreeLeaf tl : setTreeLeaf) {
//            lastOrder = (Order) dataBase.getObject(tl.getContainer().getClassName(),
//                    tl.getContainer().getId());
//        }
        Order lastOrder = (Order) dataBase.getObject(Order.class.getName(), 10);
        for (OrderRow row : lastOrder.getOrderRows()) {
            row.setDiscount(new BigDecimal("10"));
        }

        lastOrder.setTotalPercentDiscount(new BigDecimal("10"));

        Printer printer = Printer.getInstance(Paths.get(System.getProperty("user.home"),
                ".saas", "app", "config", "system.xml"));

//        System.out.println(">>>>>>>>" + core.printDocument(
//                printer.getDefaultPrinter(),
//                lastOrder,
//                Carssier.ACT,
//                1,
//                MediaFormat.A4Portrait()));
//
//        System.out.println(">>>>>>>>" + core.printDocument(
//                printer.getDefaultPrinter(),
//                lastOrder,
//                Carssier.BILL,
//                1,
//                MediaFormat.A4Portrait()));

        System.out.println(">>>>>>>>" + core.printDocument(
                printer.getPosPrinter(),
                lastOrder,
                Carssier.BILL_POS,
                1,
                MediaFormat.POS_72x160()));
    }

    private void payOrderWithoutCashMachine() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        TreeBasic tree = (TreeBasic) Trees.getTree("OrdersUnpayed", dataBase,
                TreeBasic.class.getName());
        Set<TreeLeaf> setTreeLeaf = tree.getRootFolder().getSetTreeLeaf();
        for (TreeLeaf tl : setTreeLeaf) {
            Order order = (Order) dataBase.getObject(tl.getContainer().getClassName(),
                    tl.getContainer().getId());
            Result r = core.paidWithoutCashMachine(order);
            if (r.isError()) {
                System.err.println(r);
            } else {
                System.out.println("Paid without cashmachine " + r);
            }
        }
    }

    private void payOrder() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        TreeBasic tree = (TreeBasic) Trees.getTree("OrdersUnpayed", dataBase,
                TreeBasic.class.getName());
        Set<TreeLeaf> setTreeLeaf = tree.getRootFolder().getSetTreeLeaf();
        for (TreeLeaf tl : setTreeLeaf) {
            Order order = (Order) dataBase.getObject(tl.getContainer().getClassName(),
                    tl.getContainer().getId());
            order.setTotalPercentDiscount(new BigDecimal("15"));
            dataBase.updateObject(order);
            BigDecimal cash = Money.ADD(
                    order.getTotalWithoutTotalDiscount().toString(),
                    "100");
            System.out.println("Try pay for Order");
            System.out.println("Total sum = " + order.getTotalWithoutTotalDiscount());
            System.out.println("Total discount = " + order.getTotalDiscountSum());
            System.out.println("Cash = " + cash);
            System.out.println("--------------------------------");

            try {
                Result result = core.paidCash(order, cash, 48, "");
                if (result.isError()) {
                    System.err.println(result);

                } else {
                    System.out.println(result);
                }

            } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }

            break;
        }


    }

    private void createOrders() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        Result r = core.getTreeGoodsAndService();
        if(r.isError()){
            System.err.println(r.getReason());
            System.exit(-1);
        }
        
        TreeBasic tree = (TreeBasic)r.getObject();
        tree.getRootFolder().removeAll();
        
        r = core.getTreeOrdersUnpayed();
        if(r.isError()){
            System.err.println(r.getReason());
            System.exit(-1);
        }
        
        tree = (TreeBasic) r.getObject();
        tree.getRootFolder().removeAll();

        final ArrayList<Service> services = new ArrayList<>();
        final ArrayList<Goods> goodses = new ArrayList<>();
        TreeNodeWalker walker = new TreeNodeWalker(
                Trees.getTree(
                "GoodsAndService",
                dataBase,
                TreeBasic.class.getName()).getRootFolder()) {

            @Override
            public TreeNodeVisitResult visitFolder(TreeFolder folder) {
                return TreeNodeVisitResult.CONTINUE;
            }

            @Override
            public TreeNodeVisitResult visitLeaf(TreeLeaf leaf) {
                if (leaf.getContainer().getClassName().equals(Service.class.getName())) {
                    Service service = (Service) dataBase.getObject(Service.class.getName(),
                            leaf.getContainer().getId());
                    services.add(service);
                }

                if (leaf.getContainer().getClassName().equals(Goods.class.getName())) {
                    Goods goods = (Goods) dataBase.getObject(Goods.class.getName(),
                            leaf.getContainer().getId());
                    goodses.add(goods);
                }

                return TreeNodeVisitResult.CONTINUE;
            }
        };
        walker.start();

        BigDecimal totalSum = BigDecimal.ZERO;
        for (int i = 0; i < 10; i++) {
            BigDecimal serviceSum = BigDecimal.ZERO;
            BigDecimal goodsSum = BigDecimal.ZERO;

            Order order = Order.newOrder();

            if (core.getDefaultSupplier().isError()) {
                System.err.println(core.getDefaultSupplier());
                continue;
            }

            order.setSupplier((Agent) core.getDefaultSupplier().getObject());

            if (core.getAgents().isError()) {
                System.err.println(core.getAgents());
                continue;

            } else {
                ArrayList<Agent> agentList = (ArrayList<Agent>) core.getAgents().getObject();
                double index = Math.random() * (double) agentList.size();
                if (agentList.get((int) index).getShortName().equals("Формула-1")) {
                    index++;
                }
                order.setCustomer(agentList.get((int) index));
            }

            int val, countOfSalesItems;
            // add services
            val = (int) (Math.random() * 15d);
            countOfSalesItems = val == 0 ? 1 : val;
            Set<String> setServices = new HashSet();
            for (int j = 0; j < countOfSalesItems; j++) {
                Service s = services.get((int) (Math.random() * services.size()));
                while (setServices.contains(s.getShortName())) {
                    s = services.get((int) (Math.random() * services.size()));
                }
                setServices.add(s.getShortName());

                int count = (int) (Math.random() * 10);
                count = count == 0 ? 1 : count;

                int discount = (int) (Math.random() * 25);
                discount = (discount % 5) * 4;

                BigDecimal bsum = Money.MULTIPLY(s.getSalePrice(dataBase).toString(), "" + count);
                BigDecimal d = Money.MULTIPLY(bsum.toString(), "" + discount);
                d = Money.DIVIDE(d.toString(), "100");
                bsum = Money.SUBSTRACT(bsum.toString(), d.toString());

                System.out.println(s.getName() + " " + s.getSalePrice(dataBase) + " x "
                        + count + " - " + discount + "(" + d + ") = "
                        + bsum);

                OrderRow c = order.addOrderItem(s, s.getSalePrice(dataBase), new BigDecimal(count), new BigDecimal(discount));

                serviceSum = Money.ADD(serviceSum.toString(), bsum.toString());
                totalSum = Money.ADD(totalSum.toString(), bsum.toString());

            }

            // add goods
            val = (int) (Math.random() * 5d);
            countOfSalesItems = val == 0 ? 1 : val;
            Set<String> setGoodses = new HashSet();
            for (int j = 0; j < countOfSalesItems; j++) {
                Goods g = goodses.get((int) (Math.random() * goodses.size()));
                while (setGoodses.contains(g.getShortName())) {
                    g = goodses.get((int) (Math.random() * goodses.size()));
                }
                setGoodses.add(g.getShortName());

                int count = (int) (Math.random() * 10);
                count = count == 0 ? 1 : count;

                int discount = (int) (Math.random() * 25);
                discount = (discount % 5) * 4;

                BigDecimal bsum = Money.MULTIPLY(g.getSalePrice(dataBase).toString(), "" + count);
                BigDecimal d = Money.MULTIPLY(bsum.toString(), "" + discount);
                d = Money.DIVIDE(d.toString(), "100");
                bsum = Money.SUBSTRACT(bsum.toString(), d.toString());

                System.out.println(g.getName() + " " + g.getSalePrice(dataBase) + " x "
                        + count + " - " + discount + "(" + d + ") = "
                        + bsum);

                OrderRow c = order.addOrderItem(g, g.getSalePrice(dataBase), new BigDecimal(count), new BigDecimal(discount));

                goodsSum = Money.ADD(goodsSum.toString(), bsum.toString());
                totalSum = Money.ADD(totalSum.toString(), bsum.toString());
            }

            order.setName(DateTime.getFormatedDate("yyyy-MM-dd HH:mm:ss", new Date()));
            order.setDescription("Description " + DateTime.getFormatedDate("HH:mm:ss", new Date()));
//            order.setImage(ImageLoader.getInstance().getFromFile(
//                    Paths.get(System.getProperty("user.home"), ".saas",
//                    "app", "images", "icons", "document.png").toString()));

            r = core.addOrder("", order, "OrdersInWork");
            if (!r.isError()) {
                System.out.println("---------------------------------------------------");
                System.out.println(i + ". Goods sum " + goodsSum
                        + " + Service sum " + serviceSum + " = "
                        + Money.ADD(goodsSum.toString(), serviceSum.toString())
                        + ", sum from Order =  " + order.getTotalWithTotalDiscount()
                        + ", Total = " + totalSum);
                System.out.println(order.getTotalDiscountSum());
                System.out.println("---------------------------------------------------");

                for (OrderRow row : order.getOrderRows()) {
                    System.out.println(row.getShortName(dataBase) + " "
                            + row.getPrice() + " x " + row.getCount()
                            + " - " + row.getDiscount() + " = "
                            + row.getSumWithDiscount());
                }

                System.out.println("----------------\n");

            } else {
                System.err.println(i + ". " + r);
            }
        }

        // check order from in work
        System.out.println("----------------------------------");
        System.out.println("Check total sum orders in work, must be " + totalSum);
        System.out.println("----------------------------------");
        BigDecimal totalSumCheck = BigDecimal.ZERO;
        tree = (TreeBasic) Trees.getTree("OrdersInWork", dataBase, TreeBasic.class.getName());
        List<TreeLeaf> list = tree.getRootFolder().getAllDescendTreeLeaves();
        for (TreeLeaf tl : list) {
            Order o = (Order) dataBase.getObject(Order.class.getName(), tl.getContainer().getId());
            totalSumCheck = Money.ADD(
                    o.getTotalWithTotalDiscount().toString(),
                    totalSumCheck.toString());
            System.out.println("Order sum = " + o.getTotalWithTotalDiscount()
                    + ", Total = " + totalSumCheck);
        }

        if (totalSum.doubleValue() == totalSumCheck.doubleValue()) {
            System.out.println("Result checked success!");
        } else {
            System.err.println("Result checked false! Must be " + totalSum + ", but present " + totalSumCheck);
        }


        // check order from unpayed
        totalSumCheck = BigDecimal.ZERO;
        tree = (TreeBasic) Trees.getTree("OrdersUnpayed", dataBase, TreeBasic.class.getName());
        list = tree.getRootFolder().getAllDescendTreeLeaves();
        for (TreeLeaf tl : list) {
            Order o = (Order) dataBase.getObject(Order.class.getName(), tl.getContainer().getId());
            totalSumCheck = Money.ADD(
                    o.getTotalWithTotalDiscount().toString(),
                    totalSumCheck.toString());
        }

        System.out.println("----------------------------------");
        System.out.println("Check total sum orders in unpayed, must be " + totalSum);
        System.out.println("----------------------------------");
        if (totalSum.doubleValue() == totalSumCheck.doubleValue()) {
            System.out.println("Result checked success!");
        } else {
            System.err.println("Result checked false! Must be " + totalSum + ", but present " + totalSumCheck);
        }
    }

    private void setPriceForSevices() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        System.out.println("----------------------------------");
        System.out.println("Scan service...");
        System.out.println("----------------------------------");
        Tree goodsAndServiceTree = Trees.getTree("GoodsAndService", dataBase, TreeBasic.class.getName());
        Walker walker = new Walker(goodsAndServiceTree.getRootFolder());
        walker.start();

        System.out.println("----------------------------------");
        System.out.println("Set price for services...");
        System.out.println("----------------------------------");
        ArrayList<Service> list = new ArrayList<>();
        for (TreeLeaf leaf : walker.getLeafs()) {
            if (leaf.getContainer().getClassName().equals(Service.class.getName())) {
                Service service = (Service) dataBase.getObject(
                        leaf.getContainer().getClassName(),
                        leaf.getContainer().getId());
                list.add(service);
            }
        }

        Result r = core.createServiceCommandment("Test reason " + new Date());
        ServiceCommandment serviceCommandment;
        if (!r.isError()) {
            serviceCommandment = (ServiceCommandment) r.getObject();
            System.out.println("Create ServiceCommandment success!");

        } else {
            System.err.println(r);
            return;
        }

        for (Service service : list) {
            double price = Math.random() * 300d;
            if (price < 50) {
                price = 50d;
            }

            BigDecimal sum = new BigDecimal("" + (int) price);
            r = core.setServicePrice(service, sum, serviceCommandment);
            if (!r.isError()) {
                System.out.println("Set price (" + sum + " р.) for Service "
                        + service.getName() + " success!");

            } else {
                System.err.println(r);
                continue;
            }
        }
    }

    private void putGoodsInStorage() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        System.out.println("----------------------------------");
        System.out.println("Scan goods...");
        System.out.println("----------------------------------");
        //Tree goodsAndServiceTree = Trees.getTree("GoodsAndService", dataBase, TreeBasic.class.getName());
        Result r = core.getTreeGoodsAndService();
        if(r.isError()){
            System.err.println(r.getReason());
            System.exit(-1);
        }
        
        TreeBasic goodsAndServiceTree = (TreeBasic)r.getObject();
        Walker walker = new Walker(goodsAndServiceTree.getRootFolder());
        walker.start();
        System.out.println("----------------------------------");
        System.out.println("Take goods for place on storage...");
        System.out.println("----------------------------------");
        ArrayList<Goods> goodsList = new ArrayList<>();
        for (TreeLeaf leaf : walker.getLeafs()) {
            if (leaf.getContainer().getClassName().equals(Goods.class.getName())) {
                System.out.println(leaf);
                goodsList.add((Goods) dataBase.getObject(leaf.getContainer().getClassName(),
                        leaf.getContainer().getId()));
            }
        }
        moveToStorage(goodsList);
    }

    private void moveToStorage(ArrayList<Goods> goodsList) {
        // goods : count : sum : storage

        BigDecimal totalSum = BigDecimal.ZERO;
        if (core.getDefaultSupplier().isError()) {
            System.err.println(core.getDefaultSupplier());
            System.exit(-1);
        }

//        Result rShippingList = core.createShippingList(
//                (Agent) core.getDefaultSupplier().getObject(),
//                (Agent) core.getDefaultSupplier().getObject(),
//                "Test shipping list from " + new Date().toString(),
//                new Date(),
//                "Test-" + new Date().getTime(),
//                BigDecimal.ZERO);
//        ShippingList shippingList;
//        if (!rShippingList.isError()) {
//            System.out.println("Try create ShippingList" + rShippingList);
//            shippingList = (ShippingList) rShippingList.getObject();
//
//        } else {
//            System.err.println(rShippingList);
//            return;
//        }
//
//        Tree treeStorage = Trees.getTree("Warehouses", dataBase, TreeBasic.class.getName());
//        Set<TreeFolder> storages = treeStorage.getRootFolder().getSetTreeFolder();

        for (Goods goods : goodsList) {
            int count = (int) (Math.random() * 1000d);
            if (count == 0) {
                count = 10;
            }

            double p = Math.random() * 100d;
            if (p < 1) {
                p = 10d;
            }
            NumberFormat nf = NumberFormat.getInstance();
            nf.setGroupingUsed(false);
            nf.setMaximumFractionDigits(2);
            nf.setMinimumFractionDigits(2);
            BigDecimal price = new BigDecimal(nf.format(p).replaceAll(",", "."));

            BigDecimal sum = Money.MULTIPLY(price.toString(), "" + count);

//            int storageIndex = (int) (Math.random() * ((double) storages.size() + 1d));
//            if (storageIndex >= storages.size()) {
//                storageIndex = 0;
//            }
//
//            String storage = ((TreeFolder) storages.toArray()[storageIndex]).getName();
//
//            System.out.println("Try move to Storages -> Goods = " + goods + ", "
//                    + "count = " + count + ", sum = " + sum + ", "
//                    + "storage = " + storage);
//            ShippingListItem shippingListItem = new ShippingListItem();
//            shippingListItem.setCount(new BigDecimal("" + count));
//            shippingListItem.setStorage(storage);
//            shippingListItem.setGoods(goods);
//            shippingListItem.setSum(sum);
//            shippingList.addItem(shippingListItem);
            //totalSum = Money.ADD(totalSum.toString(), sum.toString());
            
            Result r = core.getDefaultStorage(goods.getId());
            if(r.isError()){
                System.out.println(r.getReason());
                System.exit(-1);
            }
            
//            r = core.addGoodsToStorage(
//                    ((Storage)r.getObject()).getShortName(), 
//                    goods.getId(), 
//                    new BigDecimal(count), 
//                    sum);
            
        if (r.isError()) {
            System.err.println(r);
            System.exit(-1);

        } else {
            System.out.println(r);
        }
        }

        //shippingList.setSupplierSum(totalSum);
        //dataBase.updateObject(shippingList);
    }

    private void storagesCreator() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        /*
        Result r = core.addAgent("Формула", "Формула", "г. Омск, 7-Северная", "8-905-9233448");
        if(r.isError()){
            System.err.println(r.getReason());
            System.exit(-1);
        }
        
        Agent a = (Agent)r.getObject();
        r = core.setDefaultSupplier(a);
        if(r.isError()){
            System.err.println(r.getReason());
            System.exit(-1);
        }
         * 
         */

        System.out.println("----------------------------------");
        System.out.println("Try add three storages...");
        System.out.println("----------------------------------");

        String[] storages = new String[]{"Основной склад", "Холодный склад",
            "Подсобка"};
        for (String storage : storages) {
            //Result result = core.addStorage(storage);
            if(core.getDefaultSupplier().isError()){
                System.err.println(core.getDefaultSupplier());
                System.exit(-1);
            }
            Result result = core.addStorage(
                    storage, 
                    storage, 
                    (Agent)core.getDefaultSupplier().getObject(), 
                    ((Agent)core.getDefaultSupplier().getObject()).getDefaultAddress(), 
                    ((Agent)core.getDefaultSupplier().getObject()).getDefaultContacts(),
                    true);
            System.out.println("Added storage " + storage + " result: "
                    + Objects.toString(result));
        }

        System.out.println("----------------------------------");
        System.out.println("Test storages...");
        System.out.println("----------------------------------");
        Result result = core.getStorages();
        System.out.println("Get all storages result = " + result);
        if (((ArrayList) result.getObject()).size() == 3) {
            System.out.println("Success! Storages count = "
                    + ((ArrayList) result.getObject()).size());
        }

        for (String storage : storages) {
            result = core.getStorage(storage);
            System.out.println("Get storage " + storage + " result = " + result);
        }
    }

    private void agentsCreator() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        System.out.println("----------------------------------");
        System.out.println("Create our Agents ...");
        System.out.println("----------------------------------");

        String str = null;
        try {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(
                            System.getProperty("user.dir"), "agents.csv"),
                            Charset.defaultCharset())) {
                while ((str = br.readLine()) != null) {
                    String arr[] = str.split(";");
                    Agent agent = new Agent();
                    agent.setName(arr[0].trim());
                    agent.setFullName(arr[1].trim());

                    Address address = new Address();
                    address.setCountry(arr[2].trim());
                    address.setCity(arr[3].trim());
                    address.setStreet(arr[4].trim());
                    address.setApartment(arr[5].trim());
                    agent.setDefaultAddress(address);

                    Contacts contacts = new Contacts();
                    contacts.setDefaultPhone(arr[6].trim());
                    contacts.setDefaultURL(arr[7].trim());
                    contacts.setDefaultEmail(arr[8].trim());
                    agent.setDefaultContacts(contacts);

                    if (!arr[9].trim().equals("")) {
                        address = new Address();
                        address.setCountry(arr[9].trim());
                        address.setCity(arr[10].trim());
                        address.setStreet(arr[11].trim());
                        address.setApartment(arr[12].trim());
                        agent.setAddress("Частный офис", address);

                        contacts = new Contacts();
                        contacts.setDefaultPhone(arr[13].trim());
                        contacts.setDefaultURL(arr[14].trim());
                        contacts.setDefaultEmail(arr[15].trim());
                        agent.setContacts("Частный офис", contacts);
                    }

                    Result result = core.addAgent(agent);
                    System.out.println("Result for create Agent: " + result.getReason());
                }
            }

            System.out.println("----------------------------------");
            System.out.println("Check couple Agents ...");
            System.out.println("----------------------------------");

            boolean isCheck = true;
            ArrayList<Agent> checkList = dataBase.getFilteredResultList(
                    Agent.class.getName(),
                    "getShortName",
                    Condition.newCondition(Condition.EQUAL, "Формула-1"));

            if (!checkList.get(0).getDefaultAddress().getCountry().equals("Россия")) {
                System.err.println("getCountry()");
                isCheck = false;
            }

            if (!checkList.get(0).getDefaultAddress().getCity().equals("Омск")) {
                System.err.println("getCity()");
                isCheck = false;
            }

            if (!checkList.get(0).getDefaultAddress().getStreet().equals("ул. 7-я Северная")) {
                System.err.println("getStreet()");
                isCheck = false;
            }

            if (!checkList.get(0).getAddressByKey("Частный офис").getStreet().equals("ул. 1-я Линия")) {
                System.err.println("getAddressByKey");
                isCheck = false;
            }

            if (!checkList.get(0).getContactByKey("Частный офис").getDefaultPhone().equals("+79031111010")) {
                System.err.println("getContactByKey");
                isCheck = false;
            }

            if (isCheck) {
                System.out.println("Agent Формула-1 checked Ok!");
            } else {
                System.err.println("Agent Формула-1 checked false!");
            }

            isCheck = true;
            checkList = dataBase.getFilteredResultList(
                    Agent.class.getName(),
                    "getShortName",
                    Condition.newCondition(Condition.EQUAL, "Стройсервис"));
            if (!checkList.get(0).getDefaultAddress().getCity().equals("Калачинск")) {
                System.err.println("getCity()");
                isCheck = false;
            }

            if (!checkList.get(0).getDefaultContacts().getDefaultEmail().equals("info@stroyservice.ru")) {
                System.err.println("getCity()");
                isCheck = false;
            }

            if (isCheck) {
                System.out.println("Agent Стройсервис checked Ok!");
            } else {
                System.err.println("Agent Стройсервис checked false!");
            }

            System.out.println("Try set agent Формула-1 as default supplier");
            checkList = dataBase.getFilteredResultList(
                    Agent.class.getName(),
                    "getShortName",
                    Condition.newCondition(Condition.EQUAL, "Формула-1"));
            core.setDefaultSupplier(checkList.get(0));

            if (core.getDefaultSupplier().isError()) {
                System.err.println(core.getDefaultSupplier());
                return;
            }

            Agent aCheck = (Agent) core.getDefaultSupplier().getObject();
            if (aCheck.getShortName().equals("Формула-1")) {
                System.out.println("Confirm agent Формула-1 is default supplier!");
            } else {
                System.err.println("Try set agent Формула-1 as default supplier - FALSE");
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, str, e);
        }
    }

    private class Walker extends TreeNodeWalker {

        private int count;
        private ArrayList<TreeLeaf> leafList = new ArrayList<>();

        public Walker(TreeFolder treeFolder) {
            super(treeFolder);
        }

        public ArrayList<TreeLeaf> getLeafs() {
            return leafList;
        }

        @Override
        public TreeNodeVisitResult visitFolder(TreeFolder folder) {
            System.out.println("FOLDER = " + folder);
            return TreeNodeVisitResult.CONTINUE;
        }

        @Override
        public TreeNodeVisitResult visitLeaf(TreeLeaf leaf) {
            System.out.println(count + ". LEAF = " + leaf);
            count++;
            leafList.add(leaf);
            return TreeNodeVisitResult.CONTINUE;
        }
    }

    private void makeGoodsServiceTree() {
        System.out.println("----------------------------------");
        System.out.println("Make goods/service tree");
        System.out.println("fill from real data base");
        System.out.println("----------------------------------");
        try {
            DataBaseFiller filler = new DataBaseFiller();
            filler.start();
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }
    }

    private void cleanDataBase() {
        System.out.println("----------------------------------");
        System.out.println("Clean data base");
        System.out.println("----------------------------------");
        String homePath = System.getProperty("user.home") + File.separator
                + ".saas" + File.separator + "app" + File.separator;
        Path p = Paths.get(homePath, "db");
        try {
            DeleteDir.deleteDir(p, new String[]{"ru.sibek.core.auth.Group",
                        "ru.sibek.core.auth.User", "ru.sibek.core.preferences.ProgPreferences"});
            System.out.println("Data base was cleaned");
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }

        dataBase = CarssierDataBase.getDataBase();
        core = CarssierCore.getInstance();
    }

    private void initCrews() {
        if (dataBase == null) {
            dataBase = CarssierDataBase.getDataBase();
        }

        if (core == null) {
            core = CarssierCore.getInstance();
        }

        Result r = core.initDefaultCrew();
        if (r.isError()) {
            System.err.println(r);
        } else {
            System.out.println(r);
            for (Crew crew : core.getCrewsList()) {
                System.out.println("Added crew " + crew);
            }
        }
    }

    public static void main(String args[]) {

        try {
            String osName = System.getProperty("os.name");
            String osArch = System.getProperty("os.arch");
            System.out.println("os name = " + osName);
            System.out.println("os architecture = " + osArch);

            String newLibPath = System.getProperty("java.library.path");

            Path pPDF = Paths.get(System.getProperty("user.home"), ".saas",
                    "app", "print", "renderer", osName);
            newLibPath = pPDF.toString() + File.pathSeparator + newLibPath;
            System.out.println("Path with pdf renderer = " + newLibPath);

            if (args != null) {
                for (String arg : args) {
                    if (arg.indexOf("=") != -1) {
                        String arr[] = arg.split("=");
                        System.setProperty(arr[0], arr[1]);
                    }
                }
            }

            if (osName.indexOf("Windows") != -1) {
                osName = "Windows";
            }

            Path cashMachineLibPath = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "rxtx", osName, osArch);
            newLibPath = newLibPath + File.pathSeparator + cashMachineLibPath.toString();

            System.setProperty("java.library.path", newLibPath);
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            if (fieldSysPath != null) {
                fieldSysPath.set(System.class.getClassLoader(), null);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }

        if (args.length == 0) {
            System.err.println("set path to test.properties");
            System.exit(-100);
        }
        ScenarioSimply st = new ScenarioSimply(Paths.get(args[0]));
        st.simpleScenario();
    }
}
