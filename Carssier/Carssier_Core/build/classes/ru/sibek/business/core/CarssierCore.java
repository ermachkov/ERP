/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintException;
import javax.xml.xpath.XPathExpressionException;
import org.ubo.accountbook.AccountPost;
import org.ubo.accountbook.AccountRule;
import org.ubo.accountbook.AnalyticFilter;
import org.ubo.datetime.DateTime;
import org.ubo.document.*;
import org.ubo.document.journal.LogRecordLevel;
import org.ubo.document.journal.MoneyRecord;
import org.ubo.employee.Crew;
import org.ubo.employee.Employee;
import org.ubo.goods.Goods;
import org.ubo.goods.GoodsAnalytics;
import org.ubo.json.JSONObject;
import org.ubo.money.Money;
import org.ubo.partner.*;
import org.ubo.print.MediaFormat;
import org.ubo.print.PDFMaker;
import org.ubo.quantity.Quantity;
import org.ubo.report.ReportException;
import org.ubo.report.ReportHandler;
import org.ubo.report.ReportShift;
import org.ubo.rules.*;
import org.ubo.service.Service;
import org.ubo.storage.Storage;
import org.ubo.tree.*;
import org.ubo.utils.ImageLoader;
import org.ubo.utils.Result;
import org.ubo.utils.XPathUtil;
import org.ubo.www.ExchangeSettings;
import org.ubo.www.Page;
import org.ubo.www.PriceACL;
import org.ucm.cashmachine.*;
import org.ucm.cashmachine.pos.PosResponseItem;
import org.ups.print.Print;
import org.ups.print.PrintResult;
import org.ups.print.Printer;
import org.uui.component.RemoveOrderPanel.GoodsOrderRow;
import org.uui.component.RemoveOrderPanel.ServiceOrderRow;
import org.uui.component.RightPanel;
import org.uui.component.WorkPanel;
import org.uui.db.Condition;
import org.uui.db.DataBase;
import org.uui.event.EventListenerList;
import org.uui.event.UIEvent;
import org.uui.event.UIEventListener;
import org.uui.plugin.HasRules;
import org.uui.plugin.Plugin;
import org.uui.plugin.RightPanelPlugin;
import org.uui.plugin.WorkPanelPlugin;
import ru.sibek.business.accountbook.AccountBookHandler;
import ru.sibek.business.print.HTMLDocHandler;
import ru.sibek.business.print.HTMLDocHandlerFactory;
import ru.sibek.business.print.OrderToHTML;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.business.users.User;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class CarssierCore implements Carssier, Serializable {

    private static DataBase dataBase;
    private Path pathToSystemXML = Paths.get(System.getProperty("user.home"),
            ".saas", "app", "config", "system.xml");
    private String homePath = System.getProperty("user.home") + File.separator
            + ".saas" + File.separator + "app" + File.separator;
    private static CarssierCore carssierCore = null;
    private PluginLoader pluginLoader;
    private Map<String, User> loggedUser = new HashMap<>();
    private static final String STORAGE_IN_WORK = "В работе",
            STORAGE_RELEASED = "Отпущено", STORAGE_DEFFERED = "Отложенные";
    private EventListenerList listenerList = new EventListenerList();
    private CashMachine cashMachine = null;
    public final static int GOODS = 0, SERVICE = 1;

    public static synchronized CarssierCore getInstance() {
        if (carssierCore == null) {
            dataBase = CarssierDataBase.getDataBase();
            carssierCore = new CarssierCore();
        }

        return carssierCore;
    }

    private CarssierCore() {
        Path p = Paths.get(dataBase.getPathToDB(), GlobalRules.class.getName());
        if (!p.toFile().exists()) {
            LinkedList<SelectorRuleItem> list = new LinkedList<>();
            SelectorRuleItem selectorRuleItem = new SelectorRuleItem();
            selectorRuleItem.setDescription("Все выполненные");
            selectorRuleItem.setKey("closeShiftCompliteOnly");
            list.add(selectorRuleItem);

            selectorRuleItem = new SelectorRuleItem();
            selectorRuleItem.setDescription("Все выполненные и оплаченные");
            selectorRuleItem.setKey("closeShiftCompliteAndPaid");
            list.add(selectorRuleItem);

            GlobalRuleItem globalRuleItem = GlobalRuleItem.newGlobalRuleItemRadio(
                    "closeShift",
                    "При закрытии смены закрывать заказы: ",
                    list, 0);
            GlobalRules globalRules = new GlobalRules();
            globalRules.addGlobalRule(globalRuleItem);

            dataBase.addObject(globalRules);

        } else {
            // check global rules
        }

        if (dataBase.getDataBaseProperties().isReplicationEnabled()) {
            if (dataBase.getDataBaseProperties().isSuperNode()) {
                createPrivateCustomer();
                initCrewEmployee();
                setDefaultWithdrawal();
            }

        } else {
            createPrivateCustomer();
            initCrewEmployee();
            setDefaultWithdrawal();
        }
    }

    private void setDefaultWithdrawal() {
        Crew crewUniversals = getCrewByName("Универсалы");
        long crewUniversalsId = crewUniversals.getId();

        Crew crewCashMasters = getCrewByName("Кассиры");
        // hack
        if (crewCashMasters == null) {
            crewCashMasters = new Crew();
            crewCashMasters.setName("Кассиры");
            dataBase.addObject(crewCashMasters);
        }
        long crewCashMastersId = crewCashMasters.getId();

        ArrayList<Service> services = dataBase.getAllObjectsList(Service.class.getName());
        for (Service service : services) {
            Map<Long, BigDecimal> m = (Map<Long, BigDecimal>) service.getAdditionInfo().get("salaryDistribution");
            if (m == null) {
                m = new HashMap<>();
                long crewId = service.getExecutorCrewId();
                if (crewId == -1) {
                    m.put(crewUniversalsId, new BigDecimal("33"));
                } else {
                    m.put(crewId, new BigDecimal("33"));
                }

                m.put(crewCashMastersId, new BigDecimal("2.5"));
                service.getAdditionInfo().put("salaryDistribution", m);
                dataBase.updateObject(service);
            }
        }

        ArrayList<Goods> goodses = dataBase.getAllObjectsList(Goods.class.getName());
        for (Goods goods : goodses) {
            Map<Long, BigDecimal> m = (Map<Long, BigDecimal>) goods.getAdditionInfo().get("salaryDistribution");
            if (m == null) {
                m = new HashMap<>();
                m.put(crewCashMastersId, new BigDecimal("2.5"));
                goods.getAdditionInfo().put("salaryDistribution", m);
                dataBase.updateObject(goods);
            }
        }
    }

    private void createPrivateCustomer() {
        Result r = getAgent("Частное лицо");
        if (r.isError()) {
            Agent a = new Agent();
            a.setFullName("Частное лицо");
            a.setShortName("Частное лицо");

            Address address = new Address();
            a.setAddress("default", address);

            Contacts contact = new Contacts();
            a.setContacts("default", contact);

            Account account = new Account();
            a.setAccount("default", account);

            addAgent(a);
        }
    }

    private void initCrewEmployee() {
        Path p = Paths.get(dataBase.getPathToDB(), Crew.class.getName());
        if (!p.toFile().exists()) {
            Result r = initDefaultCrew();
            if (r.isError()) {
                Logger.getGlobal().log(Level.SEVERE, r.getReason());
                System.exit(-1000);
            }
        }

        p = Paths.get(dataBase.getPathToDB(), Employee.class.getName());
        if (!p.toFile().exists()) {
            Employee employee = new Employee();
            employee.setFullName("Иванов Иван Иваныч");
            employee.setShortName("Иванов Иван Иваныч");
            employee.setImageFileName("img/icons/master.png");
            Result r = getDefaultCrew();
            if (!r.isError()) {
                employee.setDefaultCrew((Crew) r.getObject());
            }
            employee.setRole("foreman");
            addEmployee(employee);
        }
    }

    public Result addCrew(String newCrewName) {
        ArrayList<Crew> crews = getCrewsList();
        boolean isFind = false;
        for (Crew crew : crews) {
            if (crew.getName().trim().toLowerCase().equals(newCrewName.trim().toLowerCase())) {
                isFind = true;
            }
        }

        if (isFind) {
            return Result.newResult(false, "Бригада с таким названием уже есть.");
        }

        Crew crew = new Crew();
        crew.setName(newCrewName);
        if (dataBase.addObject(crew) > 0) {
            return Result.newResultSuccess(crew);
        } else {
            return Result.newResult(false, "Can not add new crew");
        }
    }

    /**
     *
     * @param checkCrewId
     * @return CrewUsedInfo or null
     */
    public CrewUsedInfo isCrewUsed(long checkCrewId) {
        Crew crew = getCrewById(checkCrewId);
        if (crew == null) {
            return null;
        }

        ArrayList<Service> services = dataBase.getAllObjectsList(Service.class.getName());
        ArrayList<Goods> goodses = dataBase.getAllObjectsList(Goods.class.getName());

        ArrayList<Service> usedServices = new ArrayList<>();
        for (Service service : services) {
            Map<Long, BigDecimal> md = (Map<Long, BigDecimal>) service.getAdditionInfo().get("salaryDistribution");
            if (md == null) {
                continue;
            }

            if (md.isEmpty()) {
                continue;
            }

            long crewId = md.keySet().toArray(new Long[md.keySet().size()])[0];
            if (checkCrewId == crewId) {
                usedServices.add(service);
            }
        }

        ArrayList<Goods> usedGoodses = new ArrayList<>();
        for (Goods goods : goodses) {
            Map<Long, BigDecimal> md = (Map<Long, BigDecimal>) goods.getAdditionInfo().get("salaryDistribution");
            if (md == null) {
                continue;
            }

            if (md.isEmpty()) {
                continue;
            }

            long crewId = md.keySet().toArray(new Long[md.keySet().size()])[0];
            if (checkCrewId == crewId) {
                usedGoodses.add(goods);
            }
        }


        return new CrewUsedInfo(crew, usedServices, usedGoodses);
    }

    public Result removeCrew(long removeCrewId) {
        Crew crew = getCrewById(removeCrewId);
        if (crew == null) {
            return Result.newResultError("Can not find Crew with id " + removeCrewId);
        }

        CrewUsedInfo crewUsedInfo = isCrewUsed(removeCrewId);
        if (crewUsedInfo.isCrewUsed()) {
            return Result.newResult(
                    false,
                    "Force remove need",
                    crewUsedInfo);
        } else {
            if (dataBase.deleteObject(Crew.class.getName(), removeCrewId)) {
                return Result.newEmptySuccess();
            } else {
                return Result.newResultError("Can not delete Crew with id = " + removeCrewId);
            }
        }
    }

    public Result getGlobalRuleItemByKey(String key) {
        ArrayList<GlobalRules> list = dataBase.getAllObjectsList(GlobalRules.class.getName());
        if (list.isEmpty()) {
            return Result.newResultError("Can not find any GlobalRules");

        } else {
            GlobalRules globalRules = list.get(0);
            for (GlobalRuleItem item : globalRules.getGloabalRules()) {
                if (item.getType() == GlobalRuleItem.RADIO) {
                    LinkedList<SelectorRuleItem> l = (LinkedList<SelectorRuleItem>) item.getValue();
                    for (SelectorRuleItem sri : l) {
                        if (sri.getKey().equals(key)) {
                            return Result.newResultSuccess(sri.isSelected());
                        }
                    }
                }
            }
        }

        return Result.newResultError("Can not find any GlobalRules");
    }

    public Result addTreeFolder(TreeFolder rootTreeFolder, String newFolderName) {
        String folderName = newFolderName;
        int count = 0;
        while (rootTreeFolder.isDuplicateNodeName(folderName)) {
            folderName = newFolderName + "(" + count + ")";
            count++;
        }

        TreeFolder tf = new TreeFolderBasic(dataBase, folderName);
        rootTreeFolder.addTreeFolder(tf);

        return Result.newEmptySuccess();
    }

    public Result cutTreeNode(String className, String id) {
        long _id;
        try {
            _id = Long.parseLong(id);
        } catch (Exception e) {
            return Result.newStackTraceResultError(e.getMessage(), Thread.currentThread());
        }

        return cutTreeNode(className, _id);
    }

    public Result cutTreeNode(String className, long id) {
        TreeNode treeNode = (TreeNode) dataBase.getObject(className, id);
        if (treeNode == null) {
            return Result.newStackTraceResultError("Tree node is null", Thread.currentThread());
        }

        Clipboard.getInstance().addObject(Clipboard.CUT, treeNode);

        return Result.newEmptySuccess();
    }

    public void clearClipboard() {
        Clipboard.getInstance().clear();
    }

    public Result copyTreeNode(String className, String id) {
        long _id;
        try {
            _id = Long.parseLong(id);
        } catch (Exception e) {
            return Result.newStackTraceResultError(e.getMessage(), Thread.currentThread());
        }

        return copyTreeNode(className, _id);
    }

    public Result copyTreeNode(String className, long id) {
        TreeNode treeNode = (TreeNode) dataBase.getObject(className, id);
        if (treeNode == null) {
            return Result.newStackTraceResultError("Tree node is null", Thread.currentThread());
        }

        Clipboard.getInstance().addObject(Clipboard.COPY, treeNode);

        return Result.newEmptySuccess();
    }

    public Result pasteTreeNode(String treeFolderClassName, String id) {
        long _id;
        try {
            _id = Long.parseLong(id);
        } catch (Exception e) {
            return Result.newStackTraceResultError(e.getMessage(), Thread.currentThread());
        }

        TreeFolder rootTreeFolder = (TreeFolder) dataBase.getObject(treeFolderClassName, _id);
        return pasteTreeNode(rootTreeFolder);
    }

    public Result pasteTreeNode(TreeFolder rootTreeFolder) {
        if (Clipboard.getInstance().getObjects().isEmpty()
                || Clipboard.getInstance().getMode() == -1) {
            //return Result.newStackTraceResultError("Clipboard object is invalid ["
            //        + Clipboard.getInstance() + "]", Thread.currentThread());
            return Result.newEmptySuccess();
        }

        if (Clipboard.getInstance().getMode() == Clipboard.CUT) {
            for (Object obj : Clipboard.getInstance().getAndClear()) {
                if (obj instanceof TreeNode) {
                    if (obj instanceof TreeFolderBasic) {
                        TreeFolderBasic parent = (TreeFolderBasic) ((TreeFolderBasic) obj).getParent();
                        parent.removeTreeFolder((TreeFolderBasic) obj);
                        rootTreeFolder.addTreeFolder((TreeFolder) obj);
                    }

                    if (obj instanceof TreeLeafBasic) {
                        TreeFolderBasic parent = (TreeFolderBasic) ((TreeLeafBasic) obj).getParent();
                        parent.removeTreeLeaf((TreeLeafBasic) obj);
                        rootTreeFolder.addTreeLeaf((TreeLeaf) obj);
                    }

                } else {
                    return Result.newStackTraceResultError(
                            "Clipboard object is not TreeNode",
                            Thread.currentThread());
                }
            }

        } else if (Clipboard.getInstance().getMode() == Clipboard.COPY) {
            for (Object obj : Clipboard.getInstance().getAndClear()) {
                if (obj instanceof TreeNode) {
                    if (obj instanceof TreeFolderBasic) {
                        TreeFolderBasic tfb = (TreeFolderBasic) obj;
                        TreeFolderBasic tfbClone = (TreeFolderBasic) tfb.clone();
                        tfbClone.setName(tfbClone.getName() + " (Копия)");
                        rootTreeFolder.addTreeFolder(tfbClone);
                    }

                    if (obj instanceof TreeLeafBasic) {
                        TreeLeafBasic tlb = (TreeLeafBasic) obj;
                        TreeLeafBasic tlbClone = (TreeLeafBasic) tlb.clone();
                        tlbClone.setName(tlbClone.getName() + " (Копия)");
                        rootTreeFolder.addTreeLeaf(tlbClone);
                    }

                } else {
                    return Result.newStackTraceResultError(
                            "Clipboard object is not TreeNode", Thread.currentThread());
                }
            }
        }

        return Result.newEmptySuccess();
    }

    public Result removeTreeNode(String className, String id) {
        long _id;
        try {
            _id = Long.parseLong(id);
        } catch (Exception e) {
            return Result.newStackTraceResultError(e.getMessage(), Thread.currentThread());
        }

        return removeTreeNode(className, _id);
    }

    public Result removeTreeNode(String className, long id) {
        TreeNode treeNode = (TreeNode) dataBase.getObject(className, id);
        if (treeNode instanceof TreeFolder) {
            TreeFolder parent = treeNode.getParent();
            parent.removeTreeFolder((TreeFolder) treeNode);
        }

        if (treeNode instanceof TreeLeaf) {
            ((TreeLeaf) treeNode).getParent().removeTreeLeaf((TreeLeaf) treeNode);
        }

        return Result.newEmptySuccess();
    }

    /**
     *
     * @param treeFolderPath
     * @param goodsName
     * @param iconFilePath
     * @return If Result is true, Result will return new Goods
     */
    public Result addGoods(String treeFolderPath, String goodsName, String iconFilePath) {
        if (treeFolderPath == null || goodsName == null || iconFilePath == null) {
            return Result.newStackTraceResultError("Can not add servive because tree path = "
                    + treeFolderPath + ", goodsName = " + goodsName + ", "
                    + "iconFilePath = " + iconFilePath, Thread.currentThread());
        }

        TreeFolderBasic treeFolder = Trees.getTreeFolderBasic(treeFolderPath, dataBase);
        if (treeFolder == null) {
            return Result.newStackTraceResultError(
                    "Can not find tree node by path " + treeFolderPath,
                    Thread.currentThread());
        }

        int count = 0;
        String goodsNameEnd = goodsName;
        while (treeFolder.isDuplicateNodeName(goodsNameEnd)) {
            goodsNameEnd = goodsName + "(" + count + ")";
            count++;
        }

        Goods goods;
        String tfImage = treeFolder.getOriginalImage();
        if (tfImage == null) {
            goods = Goods.newGoods(goodsNameEnd, iconFilePath, dataBase);

        } else {
            if (tfImage.indexOf("folder_empty.png") != -1
                    || tfImage.indexOf("folder_full.png") != -1
                    || tfImage.indexOf("folder.png") != -1) {
                goods = Goods.newGoods(goodsNameEnd, iconFilePath, dataBase);

            } else {
                goods = Goods.newGoods(goodsNameEnd, tfImage, dataBase);
            }
        }

        TreeLinkContainer tlc = new TreeLinkContainer(Goods.class.getName(), goods.getId());
        TreeLeafBasic leaf = new TreeLeafBasic(dataBase, tlc);
        treeFolder.addTreeLeaf(leaf);

        Storage defaultStorage = null;
        ArrayList<Storage> list = dataBase.getFilteredResultList(
                Storage.class.getName(),
                "isVirtual",
                Condition.newConditionEquial("false"));
        if (list.isEmpty()) {
            Result rs = getDefaultSupplier();
            if (rs.isError()) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
            defaultStorage = createDefaultStorage((Agent) rs.getObject());

        } else {
            boolean isFind = false;
            for (Storage s : list) {
                if (s.getShortName().equals("Склад")) {
                    isFind = true;
                    defaultStorage = s;
                    break;
                }

                if (!isFind) {
                    defaultStorage = list.get(0);
                }
            }
        }

        if (defaultStorage == null) {
            return Result.newStackTraceResultError(Thread.currentThread());
        }
        goods.setDefaultStorage(defaultStorage.getId());
        if (!dataBase.updateObject(goods)) {
            return Result.newStackTraceResultError("Can not update goods " + goods, Thread.currentThread());
        }

        return Result.newResultSuccess(goods);
    }

    public Result setGoodsSalaryPercent(Goods goods, BigDecimal percent) {
        Result result;

        try {
            goods.setSalaryPercent(percent);
            if (dataBase.updateObject(goods)) {
                result = Result.newResultSuccess(goods);
            } else {
                result = Result.newStackTraceResultError("Can not update Godds " + goods, Thread.currentThread());
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, Objects.toString(goods), e);
            result = Result.newStackTraceResultError(e.getMessage(), Thread.currentThread());
        }

        return result;
    }

    public Result setIconGoods(Goods goods, String pathToImageIcon) {
        Path p = Paths.get(pathToImageIcon);
        if (!p.toFile().exists()) {
            return Result.newStackTraceResultError(
                    "Can not set image, because icon by path "
                    + pathToImageIcon + " is not exist", Thread.currentThread());
        }

        String image = Paths.get("img", p.getName(p.getNameCount() - 2).toString(),
                p.getName(p.getNameCount() - 1).toString()).toString();
        Logger.getGlobal().log(Level.INFO, "Set image for goods = {0}", image);
        goods.setImageFileName(image);

        if (dataBase.updateObject(goods)) {
            return Result.newResultSuccess(goods);

        } else {
            return Result.newStackTraceResultError("Can not update Goods " + goods,
                    Thread.currentThread());

        }
    }

    public Result setIconService(Service service, String pathToImageIcon) {
        Path p = Paths.get(System.getProperty("user.home"), ".saas", "app", "ui", pathToImageIcon);
        if (!p.toFile().exists()) {
            return Result.newStackTraceResultError(
                    "Can not set image, because icon by path "
                    + pathToImageIcon + " is not exist",
                    Thread.currentThread());
        }

        String image = Paths.get("img", p.getName(p.getNameCount() - 2).toString(),
                p.getName(p.getNameCount() - 1).toString()).toString();
        Logger.getGlobal().log(Level.INFO, "Set image for goods = {0}", image);
        service.setImageFileName(image);

        if (dataBase.updateObject(service)) {
            return Result.newResultSuccess(service);

        } else {
            return Result.newStackTraceResultError("Can not update Service " + service,
                    Thread.currentThread());

        }
    }

    public Result addService(String treeFolderPath, String serviceName, String iconFilePath, long executorCrewId) {
        if (treeFolderPath == null || serviceName == null || iconFilePath == null) {
            return Result.newStackTraceResultError("Can not add servive because tree path = "
                    + treeFolderPath + ", serviceName = " + serviceName + ", "
                    + "iconFilePath = " + iconFilePath, Thread.currentThread());
        }

        TreeFolderBasic treeFolder = Trees.getTreeFolderBasic(treeFolderPath, dataBase);
        if (treeFolder == null) {
            return Result.newStackTraceResultError(
                    "Can not find tree node by path " + treeFolderPath,
                    Thread.currentThread());
        }

        int count = 0;
        String serviceNameEnd = serviceName;
        while (treeFolder.isDuplicateNodeName(serviceNameEnd)) {
            serviceNameEnd = serviceName + "(" + count + ")";
            count++;
        }

        Service service;
        String tfImage = treeFolder.getOriginalImage();
        if (tfImage == null) {
            service = Service.newService(serviceNameEnd, iconFilePath, dataBase);
        } else {
            if (tfImage.indexOf("folder_empty.png") != -1
                    || tfImage.indexOf("folder_full.png") != -1
                    || tfImage.indexOf("folder.png") != -1) {
                service = Service.newService(serviceNameEnd, iconFilePath, dataBase);

            } else {
                service = Service.newService(serviceNameEnd, tfImage, dataBase);
            }
        }

        TreeLinkContainer tlc = new TreeLinkContainer(Service.class.getName(), service.getId());
        TreeLeafBasic leaf = new TreeLeafBasic(dataBase, tlc);
        treeFolder.addTreeLeaf(leaf);

        return Result.newResultSuccess(service);
    }

    /**
     *
     * @param partner
     * @return If seccess will be return stored Agent
     */
    public Result addAgent(Agent partner) {
        long id = dataBase.addObject(partner);
        Result result = id > 0
                ? Result.newResultSuccess(partner)
                : Result.newStackTraceResultError(
                "Can not add object " + partner + " to data base",
                Thread.currentThread());

        return result;
    }

    /**
     *
     * @param shortName
     * @param fullName
     * @param simplyAddress
     * @param defaultContactPhone
     * @return If success will be return Agent
     */
    public Result addAgent(String shortName, String fullName, String simplyAddress, String defaultContactPhone) {
        Agent agent = new Agent();
        agent.setShortName(shortName);
        agent.setFullName(fullName);

        Address address = new Address();
        address.setSimplyAddress(simplyAddress);
        agent.setDefaultAddress(address);

        Contacts contacts = new Contacts();
        contacts.setDefaultPhone(defaultContactPhone);
        agent.setDefaultContacts(contacts);

        if (dataBase.addObject(agent) > 0) {
            return Result.newResultSuccess(agent);
        } else {
            return Result.newStackTraceResultError("Can not add Agent " + agent, Thread.currentThread());
        }
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public boolean canDeleteEmployee(Employee employee) {
        return true;
    }

    public boolean canDeleteAgent(Agent a) {
        boolean result = true;

        if (a.isDefaultSupplier()) {
            return false;
        }

        if (a.getShortName().equals("Частное лицо")) {
            return false;
        }

        ArrayList<Order> list = dataBase.getAllObjectsList(Order.class.getName());
        for (Order order : list) {
            Agent _a = order.getCustomer();
            if (a.getId() == _a.getId()) {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     *
     * @param a
     * @return If success will be return empty success Result
     */
    public Result removeAgent(Agent a) {
        if (dataBase.deleteObject(a)) {
            return Result.newEmptySuccess();

        } else {
            return Result.newResultError("Can not delete Agent " + a);
        }
    }

    public Result removeEmployee(Employee employee) {
        if (dataBase.deleteObject(employee)) {
            return Result.newEmptySuccess();

        } else {
            return Result.newResultError("Can not delete Employee " + employee);
        }
    }

    /**
     *
     * @param goodsId
     * @return If success will be return Storage
     */
    public Result getDefaultStorage(long goodsId) {
        Result result;

        Goods goods = (Goods) dataBase.getObject(Goods.class.getName(), goodsId);
        if (goods == null) {
            return Result.newStackTraceResultError("Can not find Goods with id = " + goodsId, Thread.currentThread());
        }

        if (goods.getDefaultStorage() == -1) {
            Result r = getStorages();
            if (r.isError()) {
                return Result.newStackTraceResultError("Can not get default Storage for "
                        + "Goods id = " + goodsId + " because " + r.getReason(),
                        Thread.currentThread());
            } else {
                Storage s = ((ArrayList<Storage>) r.getObject()).get(0);
                goods.setDefaultStorage(s.getId());
                if (!dataBase.updateObject(goods)) {
                    return Result.newStackTraceResultError("Can not update Goods " + goods,
                            Thread.currentThread());

                } else {
                    result = Result.newResultSuccess(s);
                }
            }

        } else {
            Storage storage = (Storage) dataBase.getObject(Storage.class.getName(), goods.getDefaultStorage());
            if (storage == null) {
                return Result.newStackTraceResultError("Can not find Storage with id "
                        + goods.getDefaultStorage(), Thread.currentThread());
            }
            result = Result.newResultSuccess(storage);
        }

        return result;
    }

    /**
     *
     * @return Is success will be return default supplier Agent
     */
    public Result getDefaultSupplier() {
        Agent agent = null;
        ArrayList<Agent> list = dataBase.getFilteredResultList(
                Agent.class.getName(),
                "isDefaultSupplier",
                Condition.newCondition(Condition.EQUAL, "true"));
        if (!list.isEmpty()) {
            agent = list.get(0);
        }

        if (agent != null) {
            return Result.newResultSuccess(agent);

        } else {
            return Result.newStackTraceResultError("Can not find default supplier",
                    Thread.currentThread());
        }
    }

    /**
     *
     * @param agent
     * @return If succes will be return Agent
     */
    public Result setDefaultSupplier(Agent agent) {
        ArrayList<Agent> agents = dataBase.getAllObjectsList(Agent.class.getName());

        if (agents.isEmpty()) {
            return Result.newStackTraceResultError("Can not find any Agent",
                    Thread.currentThread());
        }

        for (Agent a : agents) {
            if (a.isDefaultSupplier()) {
                a.setAsDefaultSupplier(false);
                dataBase.updateObject(a);
            }
        }

        agent.setAsDefaultSupplier(true);
        if (dataBase.updateObject(agent)) {
            createVirtualStoarges(agent);
            createDefaultStorage(agent);
            return Result.newResultSuccess(agent);

        } else {
            return Result.newStackTraceResultError("Can not update Agent " + agent,
                    Thread.currentThread());
        }
    }

    private void createVirtualStoarges(Agent agent) {
        Result r = getStorage(CarssierCore.STORAGE_DEFFERED);
        if (r.isError()) {
            addStorage(
                    CarssierCore.STORAGE_DEFFERED,
                    CarssierCore.STORAGE_DEFFERED,
                    agent, agent.getDefaultAddress(),
                    agent.getDefaultContacts(), true);
        }

        r = getStorage(CarssierCore.STORAGE_IN_WORK);
        if (r.isError()) {
            addStorage(
                    CarssierCore.STORAGE_IN_WORK,
                    CarssierCore.STORAGE_IN_WORK,
                    agent, agent.getDefaultAddress(),
                    agent.getDefaultContacts(), true);
        }

        r = getStorage(CarssierCore.STORAGE_RELEASED);
        if (r.isError()) {
            addStorage(
                    CarssierCore.STORAGE_RELEASED,
                    CarssierCore.STORAGE_RELEASED,
                    agent, agent.getDefaultAddress(),
                    agent.getDefaultContacts(), true);
        }
    }

    public Result createCompositeFolderImage(String originalImagePath) {
        try {
            Path src = Paths.get(System.getProperty("user.home"), ".saas", "app",
                    "ui", originalImagePath);
            Path dst = Paths.get(System.getProperty("user.home"), ".saas", "app",
                    "ui", "img", "composite", src.getName(src.getNameCount() - 1).toString());
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);

            if (ImageLoader.createCompositeFolderImage(dst.toString())) {
                return Result.newResultSuccess("img/composite/" + dst.getName(dst.getNameCount() - 1));
            } else {
                return Result.newStackTraceResultError(
                        "Can not create composite folder image", Thread.currentThread());
            }

        } catch (Exception e) {
            return Result.newStackTraceResultError(
                    "Can not create composite folder image", Thread.currentThread());
        }
    }

    private Storage createDefaultStorage(Agent agent) {
        Storage storage = null;
        Result r = getStorage("Склад");
        if (r.isError()) {
            r = addStorage("Склад", "Склад", agent, agent.getDefaultAddress(),
                    agent.getDefaultContacts(), false);
            if (!r.isError()) {
                storage = (Storage) r.getObject();
            }
        }

        return storage;
    }

    public Result getAgent(String shortName) {
        Result result;
        Agent agent = (Agent) dataBase.getObject(Agent.class.getName(), "getShortName", shortName);

        if (agent == null) {
            result = Result.newStackTraceResultError("Can not find Agent with short name = " + shortName,
                    Thread.currentThread());

        } else {
            result = Result.newResultSuccess(agent);
        }

        return result;
    }

    /**
     *
     * @param id
     * @return
     */
    public Result getAgent(long id) {
        Result result;
        Agent agent = (Agent) dataBase.getObject(Agent.class.getName(), id);

        if (agent == null) {
            result = Result.newStackTraceResultError("Can not find Agent with ide = " + id,
                    Thread.currentThread());

        } else {
            result = Result.newResultSuccess(agent);
        }

        return result;
    }

    /**
     *
     * @return If success will be return Agents list
     */
    public Result getAgents() {
        Result result;
        ArrayList<Agent> list = dataBase.getAllObjectsList(Agent.class.getName());

        if (list.isEmpty()) {
            result = Result.newStackTraceResultError("Agents list is empty",
                    Thread.currentThread());

        } else {
            result = Result.newResultSuccess(list);
        }

        return result;

    }

    public ArrayList<Agent> getAgentsList() {
        return dataBase.getAllObjectsList(Agent.class.getName());
    }

    /**
     *
     * @param shortStorageName
     * @param fullStorageName
     * @param owner
     * @param storageAddress
     * @param storageContacts
     * @return If success will be return Storage
     */
    public Result addStorage(String shortStorageName, String fullStorageName,
            Partner owner, Address storageAddress, Contacts storageContacts, boolean isVirtual) {

        Result r = getStorage(shortStorageName);
        if (r.isError()) {
            Storage storage = Storage.newStorage(shortStorageName, fullStorageName,
                    owner, storageAddress, storageContacts);
            storage.setIsVirtual(isVirtual);

            if (dataBase.addObject(storage) > 0) {
                return Result.newResultSuccess(storage);

            } else {
                return Result.newStackTraceResultError("Can not add Storage " + storage,
                        Thread.currentThread());
            }

        } else {
            return Result.newResultSuccess((Storage) r.getObject());
        }
    }

    public Result getStorage(long storageId) {
        Storage storage = (Storage) dataBase.getObject(Storage.class.getName(), storageId);
        if (storage == null) {
            return Result.newStackTraceResultError("Can not find stoarge with id = " + storageId, Thread.currentThread());
        } else {
            return Result.newResultSuccess(storage);
        }
    }

    /**
     *
     * @param shortStorageName
     * @return If Storage was find will be return Result where method getObject
     * contained Storage
     */
    public Result getStorage(String shortStorageName) {
        ArrayList<Storage> list = dataBase.getFilteredResultList(
                Storage.class.getName(),
                "getShortName",
                Condition.newConditionEquial(shortStorageName));

        if (list.isEmpty()) {
            return Result.newStackTraceResultError(
                    "Can not find Storage with name " + shortStorageName,
                    Thread.currentThread());
        } else {
            return Result.newResultSuccess(list.get(0));
        }
    }

    /**
     *
     * @return If success will be return ArrayList<Storage>
     */
    public Result getStorages() {
        ArrayList<Storage> list = dataBase.getAllObjectsList(Storage.class.getName());
        if (list.isEmpty()) {
            return Result.newResult(false, "Stoarges list is empty");
        }
        return Result.newResultSuccess(list);
    }

    /**
     *
     * @param goodsId
     * @param quantity
     * @param sum
     * @return
     */
    public Result addGoodsToStorage(long goodsId, BigDecimal quantity, BigDecimal sumIn, BigDecimal sum) {
        Goods goods = (Goods) dataBase.getObject(Goods.class.getName(), goodsId);
        if (goods == null) {
            return Result.newStackTraceResultError(
                    "Can not find Goods with id = " + goodsId,
                    Thread.currentThread());
        }

        return addGoodsToStorage(goods.getDefaultStorage(), goodsId, quantity, sumIn, sum);
    }

    public Result addGoodsToStorage(long storageId, long goodsId, BigDecimal quantity,
            BigDecimal sumIn, BigDecimal sum) {
        Storage s = (Storage) dataBase.getObject(Storage.class.getName(), storageId);
        if (s == null) {
            return Result.newStackTraceResultError(
                    "Can not find Storage with id = " + storageId,
                    Thread.currentThread());
        }

        return addGoodsToStorage(s.getShortName(), goodsId, quantity, sumIn, sum);
    }

    public Result addGoodsToStorage(String storageNameTo, long goodsId,
            BigDecimal quantity, BigDecimal sumIn, BigDecimal sum) {
        Result r = getStorage(storageNameTo);
        if (r.isError()) {
            return r;
        }

        Storage storage = (Storage) r.getObject();
        if (quantity.doubleValue() != 0) {
            storage.setPrice(goodsId, Money.DIVIDE(sum.toString(), quantity.toString()));
        } else {
            storage.setPrice(goodsId, BigDecimal.ZERO);
        }

        storage.addGoodsOnStorage(goodsId, quantity);

        if (dataBase.updateObject(storage)) {
            BigDecimal newPrice = storage.getPrice(goodsId);
            ArrayList<Storage> list = dataBase.getFilteredResultList(
                    Storage.class.getName(),
                    "getShortName",
                    Condition.newCondition(Condition.NOT_EQUIAL, storage.getShortName()));
            for (Storage s : list) {
                if (!s.isGoodsPresent(goodsId)) {
                    continue;
                }

                s.setPrice(goodsId, newPrice);
                if (!dataBase.updateObject(s)) {
                    return Result.newStackTraceResultError("Can not update price "
                            + newPrice + " for goods with id = "
                            + goodsId + " on storage " + s, Thread.currentThread());
                }
            }

            if (GoodsAnalytics.newGoodsAnalytics(goodsId, quantity, sumIn, new Date(), dataBase) != null) {
                // Приход товара на склад
                String accountRuleName = "Приход товара";
                AccountRule accountRule = AccountBookHandler.getInstance().
                        getAccountRuleByName(accountRuleName);
                String analyticsData = "{class:" + Goods.class.getName() + ", id:" + goodsId + "}";
                Goods g = (Goods) dataBase.getObject(Goods.class.getName(), goodsId);
                AccountBookHandler.getInstance().getAccountBook().accountPosting(
                        accountRule, sumIn, "Приход товара " + g.getShortName()
                        + " (" + quantity.toString() + " " + g.getMeasure() + ")",
                        analyticsData, new Date(), dataBase);

                return Result.newEmptySuccess();

            } else {
                return Result.newStackTraceResultError(
                        "Can not add GoodsAnalytics ", Thread.currentThread());
            }

        } else {
            return Result.newStackTraceResultError(
                    "Can not update storage " + storage, Thread.currentThread());
        }
    }

    /**
     *
     * @param reason
     * @return If success will be return ServiceCommandment
     */
    public Result createServiceCommandment(String reason) {
        if (reason == null) {
            return Result.newStackTraceResultError(
                    "Can not create ServiceCommandment because "
                    + "reason is null", Thread.currentThread());
        }
        ServiceCommandment serviceCommandment = new ServiceCommandment();
        serviceCommandment.setReason(reason);
        serviceCommandment.setDate(new Date());
        if (dataBase.addObject(serviceCommandment) > 0) {
            return Result.newResultSuccess(serviceCommandment);
        } else {
            return Result.newResultError("Can not add ServiceCommandment in data base");
        }

    }

    public Result setServicePrice(Service service, BigDecimal sum, ServiceCommandment serviceCommandment) {
        ServiceCommandmentItem serviceCommandmentItem = new ServiceCommandmentItem();
        serviceCommandmentItem.setService(service.getId());
        serviceCommandmentItem.setSum(sum);
        serviceCommandment.getItems().add(serviceCommandmentItem);
        service.getServiceCommandmentsIds().add(serviceCommandment.getId());
        service.setIdServiceCommandment(serviceCommandment.getId());
        dataBase.updateObject(service);
        dataBase.updateObject(serviceCommandment);

        return Result.newResultSuccess(serviceCommandment);
    }

    public Result setServiceSalaryPercent(Service service, BigDecimal percent) {
        Result result;
        try {
            service.setSalaryPercent(percent);
            boolean r = dataBase.updateObject(service);
            if (r) {
                result = Result.newResultSuccess(service);

            } else {
                result = Result.newStackTraceResultError(
                        "Can not update Service " + service, Thread.currentThread());
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, Objects.toString(service), e);
            result = Result.newStackTraceResultError(e.getMessage(), Thread.currentThread());
        }

        return result;
    }

    /**
     *
     * @param order
     * @return If success will be return update Order
     */
    public Result modifyOrder(Order order) {
        if (dataBase.updateObject(order)) {
            return Result.newResultSuccess(order);
        } else {
            return Result.newStackTraceResultError(Thread.currentThread());
        }
    }

    public Result addOrder(String session, Order order, String treeDestinationName) {
        Result result;

        try {
            if (order.getId() == 0) {
                if (dataBase.addObject(order) < 0) {
                    return Result.newStackTraceResultError(
                            "Can not add order " + order + " to data base",
                            Thread.currentThread());
                }
            }

            // add creator
            order.setCreatedBy(loggedUser.get(session).getName());
            if (treeDestinationName.equals("OrdersInWork")) {
                order.setWorkStatus(Order.WORK_INWORK);
            }

            if (treeDestinationName.equals("OrdersDeffered")) {
                order.setWorkStatus(Order.WORK_DEFERRED);
            }

            if (order.getDescription().equals("")) {
                order.setDescription("Заказ (" + order.getId() + ")");

            } else {
                order.setDescription(order.getDescription() + " (" + order.getId() + ")");
            }

            if (!dataBase.updateObject(order)) {
                return Result.newStackTraceResultError(
                        "Can not update order " + order + " to data base",
                        Thread.currentThread());
            }

//            TreeBasic ordersInWorkTree = (TreeBasic) Trees.getTree(treeDestinationName, dataBase, TreeBasic.class.getName());
//            if (ordersInWorkTree == null) {
//                ordersInWorkTree = (TreeBasic) Trees.createTree(treeDestinationName, dataBase, TreeBasic.class.getName());
//            }
//            TreeLinkContainer tlc = new TreeLinkContainer(Order.class.getName(), order.getId());
//            TreeLeafBasic leaf = new TreeLeafBasic(dataBase, tlc);
//            TreeFolderBasic tfb = (TreeFolderBasic) ordersInWorkTree.getRootFolder();
//            //tfb.addTreeLeaf(leaf);
//            tfb.addTreeLeaf(leaf, false);
//
//            TreeBasic ordersUnpayedTree = (TreeBasic) Trees.getTree("OrdersUnpayed", dataBase, TreeBasic.class.getName());
//            if (ordersUnpayedTree == null) {
//                ordersUnpayedTree = (TreeBasic) Trees.createTree("OrdersUnpayed", dataBase, TreeBasic.class.getName());
//            }
//            TreeLinkContainer tlcUnpaid = new TreeLinkContainer(Order.class.getName(), order.getId());
//            TreeLeafBasic leafUnpaid = new TreeLeafBasic(dataBase, tlcUnpaid);
//            tfb = (TreeFolderBasic) ordersUnpayedTree.getRootFolder();
//            //tfb.addTreeLeaf(leafUnpaid);
//            tfb.addTreeLeaf(leafUnpaid, false);
//            result = Result.newResultSuccess(order);

            // add record to logger
            String place = treeDestinationName.equals("OrdersInWork") ? "В работу" : "В отложенные";
            addLogRecord("Добавлен новый заказ "
                    + order
                    + " в " + place,
                    LogRecordLevel.INFO);

            // add record to storage 
            for (OrderRow orderRow : order.getOrderRows()) {
                SalesItem salesItem = orderRow.getSalesItem(dataBase);
                if (salesItem.getType() == SalesItem.SERVICE) {
                    continue;
                }

                String toStorageName;
                if (treeDestinationName.equals("OrdersDeffered")) {
                    toStorageName = CarssierCore.STORAGE_DEFFERED;
                } else {
                    toStorageName = CarssierCore.STORAGE_IN_WORK;
                }

                Result r = getStorage(toStorageName);
                if (r.isError()) {
                    if (getDefaultSupplier().isError()) {
                        return getDefaultSupplier();
                    }

                    Agent a = (Agent) getDefaultSupplier().getObject();

                    r = addStorage(toStorageName, toStorageName, a, a.getDefaultAddress(), a.getDefaultContacts(), true);
                    if (r.isError()) {
                        return Result.newStackTraceResultError("method addOrder " + r.getReason(), Thread.currentThread());

                    } else {
                        Goods goods = (Goods) dataBase.getObject(Goods.class.getName(), salesItem.getDbId());
                        Result rs = getDefaultStorage(goods.getId());

                        if (rs.isError()) {
                            return rs;
                        }

                        Storage storageFrom = (Storage) rs.getObject();
                        storageFrom.takeGoodsFromStorage(goods.getId(), orderRow.getCount());
                        if (!dataBase.updateObject(storageFrom)) {
                            return Result.newStackTraceResultError(
                                    "Can not update Storage " + storageFrom,
                                    Thread.currentThread());
                        }

                        Storage storageTo = (Storage) r.getObject();
                        storageTo.addGoodsOnStorage(goods.getId(), orderRow.getCount());
                        if (!dataBase.updateObject(storageTo)) {
                            return Result.newStackTraceResultError(
                                    "Can not update Storage " + storageTo,
                                    Thread.currentThread());
                        }

                        result = addStorageRecord(
                                goods,
                                orderRow.getCount(),
                                storageFrom,
                                storageTo,
                                order.getSupplier(),
                                order.getCustomer());
                    }

                } else {
                    Goods goods = (Goods) dataBase.getObject(Goods.class.getName(), salesItem.getDbId());

                    Result rs = getDefaultStorage(goods.getId());
                    if (rs.isError()) {
                        return rs;
                    }

                    Storage storageFrom = (Storage) rs.getObject();
                    storageFrom.takeGoodsFromStorage(goods.getId(), orderRow.getCount());
                    if (!dataBase.updateObject(storageFrom)) {
                        return Result.newStackTraceResultError(
                                "Can not update Storage " + storageFrom,
                                Thread.currentThread());
                    }

                    Storage storageTo = (Storage) r.getObject();
                    storageTo.addGoodsOnStorage(goods.getId(), orderRow.getCount());
                    if (!dataBase.updateObject(storageTo)) {
                        return Result.newStackTraceResultError(
                                "Can not update Storage " + storageTo,
                                Thread.currentThread());
                    }

                    result = addStorageRecord(
                            goods,
                            orderRow.getCount(),
                            storageFrom,
                            storageTo,
                            order.getSupplier(),
                            order.getCustomer());
                }
            }

        } catch (Exception e) {
            result = Result.newStackTraceResultError(e.getMessage(), Thread.currentThread());
        }

        return Result.newResultSuccess(order);
    }

    public Result initDefaultCrew() {
        LinkedList<String> crewList = new LinkedList<>();
        crewList.add("Универсалы");
        crewList.add("Монтаж колес");
        crewList.add("Мойка колес");
        crewList.add("Слесари по ремонту");
        crewList.add("Бригада по ремонту дисков");
        crewList.add("Бригада по шиповке");
        crewList.add("Цеховая бригада по ремонту дисков");
        crewList.add("Сход, развал");
        crewList.add("Мелкосрочка");
        crewList.add("Маляры");
        crewList.add("Двигателисты");
        crewList.add("Электрики");
        crewList.add("Кассиры");
        return initCrews(crewList, 0, "img/icons/crew64.png");
    }

    public Result initCrews(LinkedList<String> crewList, int defaultCrewIndex, String iconFilePath) {
        ArrayList<Crew> list = dataBase.getAllObjectsList(Crew.class.getName());
        if (!list.isEmpty()) {
            return Result.newEmptySuccess();
        }

        int index = 0;
        for (String name : crewList) {
            Crew crew = new Crew();
            crew.setName(name);
            if (index == defaultCrewIndex) {
                crew.setDefaultCrew(true);
            } else {
                crew.setDefaultCrew(false);
            }

            crew.setImageFileName(iconFilePath);
            index++;

            long id;
            if ((id = dataBase.addObject(crew)) < 0) {
                return Result.newResult(false, "Can not add " + crew);
            }
            Logger.getGlobal().log(Level.INFO, "Added crew: {0} id={1}", new Object[]{crew, id});
        }

        return Result.newEmptySuccess();
    }

    public Result paidWithoutCashMachine(Order order) {
        if (order.getPaidStatus() == Order.PAID) {
            return Result.newStackTraceResultError("Order was paid previosly", Thread.currentThread());
        }

        Result result = Result.newEmptySuccess();
        try {
            order.setPaidStatus(Order.PAID);
            order.setPaidDate(new Date());
            dataBase.updateObject(order);
            TreeBasic treeSrc = (TreeBasic) Trees.getOrCreateTree(
                    "OrdersUnpayed", dataBase, TreeBasic.class.getName());
            TreeBasic treeDst = (TreeBasic) Trees.getOrCreateTree(
                    "OrdersPayed", dataBase, TreeBasic.class.getName());
            moveOrder(order, treeSrc, treeDst);

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, Objects.toString(order), e);
            return Result.newStackTraceResultError(e.getMessage(), Thread.currentThread());
        }

        return result;

    }

    /**
     * Make pay through cash machine and moving Order from unpayed tree to paid
     * tree
     *
     * @param order
     * @param cashSum
     * @param cashMasterPassword
     * @return
     * @throws XPathExpressionException
     * @throws NullPointerException
     * @throws CashMachineException
     */
    public Result paidCash(Order order, BigDecimal cashSum, int cashMasterPassword, String sessionId) throws XPathExpressionException,
            NullPointerException, CashMachineException {

        if (order.getPaidStatus() == Order.PAID) {
            return Result.newStackTraceResultError(
                    "Order was paid previously..", Thread.currentThread());
        }

        if (order.getTotalWithTotalDiscount().doubleValue() > cashSum.doubleValue()) {
            return Result.newStackTraceResultError("Can not paid because total sum "
                    + "Order more than sum of cash "
                    + "[" + order.getTotalWithTotalDiscount() + "] "
                    + "[" + cashSum + "]", Thread.currentThread());
        }

        Receipt reciept = new Receipt();
        for (OrderRow orderRow : order.getOrderRows()) {
            BigDecimal discount = Money.MULTIPLY(
                    orderRow.getDiscount().toString(),
                    orderRow.getPrice().toString());
            discount = Money.DIVIDE(
                    discount.toString(),
                    "100");
            BigDecimal price = Money.SUBSTRACT(
                    orderRow.getPrice().toString(),
                    discount.toString());

            reciept.addReceiptRow(orderRow.getShortName(dataBase), orderRow.getCount(), price);
        }

        reciept.setTotalDiscount(order.getTotalDiscountSum());

        CashMachineResponse cmr = cashMachine.printFiscalReceipt(reciept, cashSum,
                1, 1, cashMasterPassword);
        Logger.getGlobal().log(Level.INFO, cmr.toString());

        if (cmr.isError()) {
            addLogRecord("Error from cashmachine: " + cmr.getErrorInfo(), LogRecordLevel.ERROR);
            return Result.newResult(false, "errorFromCashMachine", cmr);

        } else {
            String path = null;
            for (ResponseItem responseItem : cmr.getResponseItemList()) {

                if (responseItem instanceof PosResponseItem) {
                    if (responseItem.getValue() == null) {
                        break;
                    }

                    if (("" + responseItem.getValue()).indexOf(".saas/app/ui/tmp/receipt_") != -1) {
                        path = "" + responseItem.getValue();
                    }
                }
            }

            if (path != null) {
                try {
                    JSMediator.print(sessionId, new String(Files.readAllBytes(Paths.get(path))));
                } catch (IOException ex) {
                    Logger.getGlobal().log(Level.WARNING, path, ex);
                    return Result.newResultError(ex.toString());

                }
            }

            order.setPaidStatus(Order.PAID);
            order.setPaidDate(new Date());
            order.setPaidType(Order.CASH);
            if (!dataBase.updateObject(order)) {
                addLogRecord("Can not update order " + order, LogRecordLevel.ERROR);
                return Result.newStackTraceResultError("Can not update order " + order,
                        Thread.currentThread());

            }

//            TreeBasic treeSrc = (TreeBasic) Trees.getOrCreateTree(
//                    "OrdersUnpayed", dataBase, TreeBasic.class.getName());
//            TreeBasic treeDst = (TreeBasic) Trees.getOrCreateTree(
//                    "OrdersPayed", dataBase, TreeBasic.class.getName());
//            moveOrder(order, treeSrc, treeDst);

            // is Order consist of Goods only?
            if (order.isConsistGoodsOnly(dataBase) && order.getWorkStatus() != Order.WORK_COMPLETE) {
                // delete from DefferedTree
                TreeBasic[] trees = new TreeBasic[2];
                Result r = getTreeOrdersDeffered();
                if (r.isError()) {
                    return r;
                }
                trees[0] = (TreeBasic) r.getObject();

                // delete from InWorkTree
                r = getTreeOrdersInWork();
                if (r.isError()) {
                    return r;
                }
                trees[1] = (TreeBasic) r.getObject();
                r = removeOrderFromTrees(order, trees);
                if (r.isError()) {
                    return r;
                }

                // move to CompliteTree
                order.setWorkStatus(Order.WORK_COMPLETE);
                if (!dataBase.updateObject(order)) {
                    return Result.newStackTraceResultError("Can not update Order " + order,
                            Thread.currentThread());
                }

                r = getTreeOrdersComplete();
                if (r.isError()) {
                    return r;
                }
                TreeBasic treeBasic = (TreeBasic) r.getObject();
                TreeLinkContainer tlc = new TreeLinkContainer(
                        Order.class.getName(), order.getId());
                TreeLeafBasic leaf = new TreeLeafBasic(dataBase, tlc);
                treeBasic.getRootFolder().addTreeLeaf(leaf);

                // move between storages
                r = getStorage(CarssierCore.STORAGE_IN_WORK);
                if (r.isError()) {
                    return Result.newStackTraceResultError(Thread.currentThread());
                }
                Storage storageInWork = (Storage) r.getObject();

                r = getStorage(CarssierCore.STORAGE_RELEASED);
                if (r.isError()) {
                    return Result.newStackTraceResultError(Thread.currentThread());
                }
                Storage stoargeReleased = (Storage) r.getObject();

                for (OrderRow orderRow : order.getOrderRows()) {
                    long goodsId = orderRow.getSalesItem(dataBase).getDbId();
                    storageInWork.takeGoodsFromStorage(
                            goodsId,
                            orderRow.getCount());
                    if (!dataBase.updateObject(storageInWork)) {
                        return Result.newStackTraceResultError(Thread.currentThread());
                    }

                    stoargeReleased.addGoodsOnStorage(goodsId, orderRow.getCount());
                    if (!dataBase.updateObject(stoargeReleased)) {
                        return Result.newStackTraceResultError(Thread.currentThread());
                    }
                }

                closeOrderThroughAccount(order);
            }

            addMoneyRecord(MoneyRecord.PAY_BY_CASH, order, "Оплата заказа",
                    order.getSupplier(), order.getCustomer(),
                    order.getTotalWithTotalDiscount());
            addLogRecord("Оплата заказа " + order + " через кассу", LogRecordLevel.INFO);

            String accountRuleName = "Оплата наличными";
            AccountRule accountRule = AccountBookHandler.getInstance().getAccountRuleByName(accountRuleName);
            String analyticsData = "{class:" + Order.class.getName() + ", id:" + order.getId() + "}";
            AccountPost accountPost = AccountBookHandler.getInstance().getAccountBook().accountPosting(
                    accountRule,
                    order.getTotalWithTotalDiscount(),
                    "Оплата наличными",
                    analyticsData,
                    new Date(),
                    dataBase);
            return Result.newEmptySuccess();
        }
    }

    public Result paidBankAccount(String sessionId, Order order) {
        if (order.getPaidStatus() == Order.PAID) {
            return Result.newStackTraceResultError(
                    "Order was paid previously..", Thread.currentThread());
        }

        order.setPaidStatus(Order.PAID);
        order.setPaidDate(new Date());
        if (!dataBase.updateObject(order)) {
            addLogRecord("Can not update order " + order, LogRecordLevel.ERROR);
            return Result.newStackTraceResultError("Can not update order " + order,
                    Thread.currentThread());
        }

        String sEmployeeId = getLoggedUser(sessionId).getExtraInfoByKey("employeeId");
        Result rEmployee = getEmployeeById(Long.parseLong(sEmployeeId));
        if (rEmployee.isError()) {
            return Result.newResultError(rEmployee.getReason());
        }
        order.setCashmaster((Employee) rEmployee.getObject());

        //TreeBasic treeSrc = (TreeBasic) Trees.getOrCreateTree(
        //        "OrdersUnpayed", dataBase, TreeBasic.class.getName());
        //TreeBasic treeDst = (TreeBasic) Trees.getOrCreateTree(
        //        "OrdersPayed", dataBase, TreeBasic.class.getName());
        //moveOrder(order, treeSrc, treeDst);

        // is Order consist of Goods only?
        if (order.isConsistGoodsOnly(dataBase)) {
            // move between storages
            Result r = getStorage(CarssierCore.STORAGE_IN_WORK);
            if (r.isError()) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
            Storage storageInWork = (Storage) r.getObject();

            r = getStorage(CarssierCore.STORAGE_RELEASED);
            if (r.isError()) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
            Storage stoargeReleased = (Storage) r.getObject();

            for (OrderRow orderRow : order.getOrderRows()) {
                long goodsId = orderRow.getSalesItem(dataBase).getDbId();
                storageInWork.takeGoodsFromStorage(
                        goodsId,
                        orderRow.getCount());
                if (!dataBase.updateObject(storageInWork)) {
                    return Result.newStackTraceResultError(Thread.currentThread());
                }

                stoargeReleased.addGoodsOnStorage(goodsId, orderRow.getCount());
                if (!dataBase.updateObject(stoargeReleased)) {
                    return Result.newStackTraceResultError(Thread.currentThread());
                }
            }

            closeOrderThroughAccount(order);
        }

        addMoneyRecord(MoneyRecord.PAY_BY_BANK, order, "Оплата заказа",
                order.getSupplier(), order.getCustomer(),
                order.getTotalWithTotalDiscount());
        addLogRecord("Оплата заказа " + order + " через банк", LogRecordLevel.INFO);

        modifyOrder(order);
        String accountRuleName = "Оплата б/наличными";
        AccountRule accountRule = AccountBookHandler.getInstance().getAccountRuleByName(accountRuleName);
        String analyticsData = "{class:" + Order.class.getName() + ", id:" + order.getId() + "}";
        AccountPost accountPost = AccountBookHandler.getInstance().getAccountBook().accountPosting(
                accountRule, order.getTotalWithTotalDiscount(), "Оплата б/наличными",
                analyticsData, new Date(), dataBase);
        return Result.newEmptySuccess();
    }

    public String getHTMLDocument(Order order, String documentName) {
        Path p = Paths.get(
                System.getProperty("user.home"),
                ".saas",
                "app",
                "print",
                "templates",
                documentName);
        return OrderToHTML.convert(order, p);
    }

    public Result printDocument(String printerName, Document doc, final String documentName,
            int copyCount, MediaFormat mediaFormat) {
        HTMLDocHandler handler = HTMLDocHandlerFactory.getHandler(documentName, doc);
        String html = handler.getHTML(doc, Paths.get(
                System.getProperty("user.home"),
                ".saas",
                "app",
                "print",
                "templates",
                documentName).toString());

        Path pathToHTML = Paths.get(System.getProperty("user.home"), ".saas",
                "app", "tmp", "print_tmp.html");
        try {
            Files.write(pathToHTML, html.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);

            Path templateDir = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "print");
            Path pathToFOPConfig = Paths.get(System.getProperty("user.home"),
                    ".saas", "app", "config", "fop.xconf");

            Path pathToXSL = templateDir.resolve(Paths.get("xsl-fo", "transform.xsl"));
            Path pathToFO = templateDir.resolve(Paths.get("xsl-fo", "out.fo"));
            Path pathToPDF = Paths.get(System.getProperty("user.home"), ".saas",
                    "app", "tmp", "print_tmp.pdf");

            XPathUtil.setValue(pathToXSL.toString(),
                    "/xs:stylesheet/xs:template/fo:root/fo:layout-master-set/fo:simple-page-master",
                    mediaFormat.getXSLPageFormat());

            Map<String, String> m = new HashMap<>();
            m.put("master-reference", mediaFormat.getPageFormat());
            XPathUtil.setValue(pathToXSL.toString(),
                    "/xs:stylesheet/xs:template/fo:root/fo:page-sequence",
                    m);

            m = new HashMap<>();
            double bodyWidth = mediaFormat.getWidth()
                    - (mediaFormat.getMarginLeft() + mediaFormat.getMarginRight());
            m.put("select", Objects.toString(bodyWidth));
            XPathUtil.setValue(pathToXSL.toString(), "/xs:stylesheet/xs:variable", m);

            boolean makePDFResult = PDFMaker.html2PDF(
                    pathToXSL.toString(),
                    pathToFO.toString(),
                    pathToHTML.toString(),
                    pathToPDF.toString(),
                    pathToFOPConfig.toString());

            if (!makePDFResult) {
                return Result.newStackTraceResultError("Can not create pdf file.",
                        Thread.currentThread());
            }

            Printer printer = Printer.getInstance(pathToSystemXML);
            PrintResult pResult = printer.print(printerName,
                    pathToPDF, copyCount, Print.INPUT_DOC_PDF);
            if (!pResult.isError()) {
                return Result.newStackTraceResultError(pResult.getDescription(),
                        Thread.currentThread());
            }

        } catch (IOException | PrintException e) {
            Logger.getGlobal().log(Level.WARNING, pathToHTML.toString(), e);
            return Result.newStackTraceResultError("Can not create pdf file. " + e.getMessage(),
                    Thread.currentThread());
        }


        return Result.newResultSuccess(html);
    }

    public Result modifyOrderPayStatus(long orderId, int payStatus) {
        Result r = getOrder(orderId);
        if (r.isError()) {
            return r;
        }

        Order order = (Order) r.getObject();
        order.setPaidStatus(payStatus);
        if (!dataBase.updateObject(order)) {
            return Result.newStackTraceResultError(Thread.currentThread());
        } else {
            return Result.newEmptySuccess();
        }
    }

    /**
     *
     * @param orderId
     * @param workStatus
     * @return Simply return success
     */
    public Result modifyOrderWorkStatus(long orderId, int workStatus) {
        Result r = getOrder(orderId);
        if (r.isError()) {
            return r;
        }

        Order order = (Order) r.getObject();
        order.setWorkStatus(workStatus);
        if (!dataBase.updateObject(order)) {
            return Result.newStackTraceResultError(Thread.currentThread());
        } else {
            return Result.newEmptySuccess();
        }
    }

    public synchronized Result moveOrder(Order order, Tree src, Tree dst) {
        Result result = Result.newEmptySuccess();

        try {
            TreeLinkContainer tlc = new TreeLinkContainer(
                    Order.class.getName(), order.getId());
            TreeLeafBasic leaf = new TreeLeafBasic(dataBase, tlc);
            //dst.addTreeLeaf(leaf);
            ((TreeFolderBasic) dst.getRootFolder()).addTreeLeaf(leaf, false);

            TreeLeaf tl = src.findTreeLeafWithObject(order);
            if (tl != null) {
                TreeFolder tf = tl.getParent();
                if (tf != null) {
                    tf.removeTreeLeaf(tl);
                }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, Objects.toString(order), e);
            return Result.newStackTraceResultError(e.getMessage(), Thread.currentThread());
        }

        return result;
    }

    /**
     *
     * @param employee
     * @return If success will be return Employee
     */
    public Result addEmployee(Employee employee) {
        Result r;
        if (employee.getImageFileName() == null) {
            employee.setImageFileName(Paths.get("img", "icons", "employee64.png").toString());

        } else if (employee.getImageFileName().trim().equals("")) {
            employee.setImageFileName(Paths.get("img", "icons", "employee64.png").toString());
        }

        employee.setDefaultCrew((Crew) dataBase.getObject(Crew.class.getName(), "isDefaultCrew", true));
        if (dataBase.addObject(employee) > 0) {
            r = Result.newResultSuccess(employee);
        } else {
            r = Result.newStackTraceResultError("Can not add new Employee " + employee,
                    Thread.currentThread());
        }

        return r;
    }

    public synchronized Result removeEmployeeFromCrew(long idEmployee, long idCrew) {
        Employee employee = (Employee) dataBase.getObject(Employee.class.getName(), idEmployee);
        if (employee == null) {
            return Result.newStackTraceResultError(
                    "Emplyee with id = " + idEmployee + " is null",
                    Thread.currentThread());
        }

        Crew crew = (Crew) dataBase.getObject(Crew.class.getName(), idCrew);
        if (crew == null) {
            return Result.newStackTraceResultError(
                    "Crew with id = " + idCrew + " is null", Thread.currentThread());
        }

        if (crew.removeEmployee(idEmployee)) {
            if (!dataBase.updateObject(crew)) {
                return Result.newResultError("Can not update Crew " + crew);
            }
            return Result.newEmptySuccess();

        } else {
            return Result.newResultError("Can not remove employee " + employee + " from crew " + crew);
        }
    }

    public synchronized Result putEmployeeToCrew(long idEmployee, long idCrew) {
        Employee employee = (Employee) dataBase.getObject(Employee.class.getName(), idEmployee);
        if (employee == null) {
            return Result.newStackTraceResultError(
                    "Emplyee with id = " + idEmployee + " is null",
                    Thread.currentThread());
        }

        Crew crew = (Crew) dataBase.getObject(Crew.class.getName(), idCrew);
        if (crew == null) {
            return Result.newStackTraceResultError(
                    "Crew with id = " + idCrew + " is null", Thread.currentThread());
        }

        if (crew.getIdsEmployees().contains(idEmployee)) {
            return Result.newEmptySuccess();
        }

        crew.addEmployee(idEmployee);
        if (!dataBase.updateObject(crew)) {
            return Result.newStackTraceResultError(Thread.currentThread());
        }

        employee.setBegunWorking(new Date());
        if (!dataBase.updateObject(employee)) {
            return Result.newStackTraceResultError(Thread.currentThread());
        }

        return Result.newEmptySuccess();
    }

    public Result modifyEmployee(Employee employee) {
        if (dataBase.updateObject(employee)) {
            return Result.newResultSuccess(employee);
        } else {
            return Result.newStackTraceResultError(
                    "Can not update Employee " + employee, Thread.currentThread());
        }
    }

    public Result removeStorage(Storage storage) {
        if (dataBase.deleteObject(storage)) {
            return Result.newEmptySuccess();
        } else {
            return Result.newStackTraceResultError(
                    "Can not remove storage " + storage, Thread.currentThread());
        }
    }

    /**
     *
     * @param treeFolder
     * @return If success will be return updated TreeFolderBasic
     */
    public Result modifyTreeFolderBasic(TreeFolderBasic treeFolder) {
        if (treeFolder == null) {
            return Result.newStackTraceResultError("Tree folder is null", Thread.currentThread());
        }

        if (dataBase.updateObject(treeFolder)) {
            return Result.newResultSuccess(treeFolder);
        } else {
            return Result.newStackTraceResultError(
                    "Can not update TreeFolderBasic " + treeFolder,
                    Thread.currentThread());
        }
    }

    /**
     *
     * @param storage
     * @return If success will be return modifyed Storage
     */
    public Result modifyStorage(Storage storage) {
        if (storage == null) {
            return Result.newStackTraceResultError("Storage is null", Thread.currentThread());
        }

        if (dataBase.updateObject(storage)) {
            return Result.newResultSuccess(storage);
        } else {
            return Result.newResult(false, "Can not update Goods " + storage);
        }
    }

    /**
     *
     * @param goods
     * @return If success will be return updated Goods
     */
    public Result modifyGoods(Goods goods) {
        if (goods == null) {
            return Result.newStackTraceResultError("Goods is null", Thread.currentThread());
        }

        dataBase.updateObject(goods);
        return Result.newResultSuccess(goods);
    }

    /**
     *
     * @param goods
     * @param newShortName
     * @param newFullName
     * @param measure
     * @param description
     * @param price
     * @param quantity
     * @param salaryPercent
     * @param reason
     * @param isReturnable
     * @param applySalaryPercentToNeighbours
     * @param isSeparable
     * @return If success will be return updated Goods
     */
    public Result modifyGoods(Goods goods, String newShortName, String newFullName,
            String measure, String description,
            BigDecimal priceIn,
            BigDecimal price,
            BigDecimal quantity,
            BigDecimal salaryPercent, String reason,
            boolean isReturnable,
            boolean applySalaryPercentToNeighbours,
            boolean isSeparable) {

        goods.setShortName(newShortName);
        goods.setFullName(newFullName);
        goods.setMeasure(measure);
        goods.setDescription(description);
        goods.setSalaryPercent(salaryPercent);
        goods.setReturnable(isReturnable);
        goods.setSalePrice(price, dataBase);
        goods.setSeparable(isSeparable);

        Result r = modifyGoods(goods);
        if (r.isError()) {
            return Result.newStackTraceResultError(Thread.currentThread());
        }

        // set new price for goods on all storages
        r = getStorages();
        if (r.isError()) {
            return Result.newStackTraceResultError(
                    "Can not modify Goods properties "
                    + "because " + r.getReason(), Thread.currentThread());
        }

        ArrayList<Storage> storageList = (ArrayList<Storage>) r.getObject();
        for (Storage s : storageList) {
            if (s.isGoodsPresent(goods.getId())) {
                if (s.getPrice(goods.getId()).doubleValue() == price.doubleValue()) {
                    continue;
                }

                s.setPrice(goods.getId(), price);
                s.setLastReason(goods.getId(), reason);
                if (!dataBase.updateObject(s)) {
                    return Result.newStackTraceResultError(
                            "Can not update storage " + s, Thread.currentThread());
                }
            }
        }

        // if new count of goods > count good on storages
        if (quantity.doubleValue() > 0) {
            r = getDefaultStorage(goods.getId());
            if (r.isError()) {
                return Result.newStackTraceResultError(
                        "Can not modify Goods properties "
                        + "because Can not find " + r.getReason(), Thread.currentThread());
            }

            Storage defaultStorage = (Storage) r.getObject();
            defaultStorage.addGoodsOnStorage(goods.getId(), quantity);
            if (!dataBase.updateObject(defaultStorage)) {
                return Result.newStackTraceResultError(
                        "Can not update storage " + defaultStorage,
                        Thread.currentThread());
            }

            GoodsAnalytics.newGoodsAnalytics(goods.getId(), quantity,
                    Money.MULTIPLY(quantity, priceIn),
                    new Date(), dataBase);
            // Приход товара на склад
            String accountRuleName = "Приход товара";
            AccountRule accountRule = AccountBookHandler.getInstance().
                    getAccountRuleByName(accountRuleName);
            String analyticsData = "{class:" + Goods.class.getName() + ", id:" + goods.getId() + "}";
            Goods g = (Goods) dataBase.getObject(Goods.class.getName(), goods.getId());
            AccountBookHandler.getInstance().getAccountBook().accountPosting(
                    accountRule, Money.MULTIPLY(quantity.toString(), priceIn.toString()),
                    "Приход товара " + g.getShortName()
                    + " (" + quantity.toString() + " " + g.getMeasure() + ")",
                    analyticsData, new Date(), dataBase);

            addStorageRecord(goods, quantity, null, defaultStorage, null, null);
        }

        // if new count of goods < count good on storages
        if (quantity.doubleValue() < 0) {
            if ((quantity.doubleValue() * -1) > getGoodsCountOnAllStorages(goods).doubleValue()) {
                return Result.newResultError(
                        "Невозможно уменьшить кол-во товара на складе, больше чем его есть.");
            }

            boolean isTaked = false;
            for (Storage s : storageList) {
                if (s.isGoodsPresent(goods.getId())) {
                    if (s.getGoodsQuantity(goods.getId()).doubleValue() >= quantity.doubleValue()) {
                        // !!!!!!!!!!!!!!!!!!!!!!!
                        s.takeGoodsFromStorage(goods.getId(), quantity);
                        if (!dataBase.updateObject(s)) {
                            return Result.newStackTraceResultError(
                                    "Can not update storage " + s,
                                    Thread.currentThread());
                        }
                        isTaked = true;
                        break;
                    }
                }
            }

            if (!isTaked) {
                ArrayList<Map<Storage, BigDecimal>> list = new ArrayList<>();
                for (Storage s : storageList) {
                    if (s.isGoodsPresent(goods.getId())) {
                        Map<Storage, BigDecimal> map = new HashMap<>();
                        map.put(s, s.getGoodsQuantity(goods.getId()));
                        list.add(map);
                    }
                }

                Collections.sort(list, new Comparator<Map<Storage, BigDecimal>>() {
                    @Override
                    public int compare(Map<Storage, BigDecimal> o1, Map<Storage, BigDecimal> o2) {
                        Storage s1 = o1.keySet().toArray(new Storage[o1.keySet().size()])[0];
                        Storage s2 = o2.keySet().toArray(new Storage[o2.keySet().size()])[0];
                        return ((Double) o1.get(s1).doubleValue()).compareTo(o2.get(s2).doubleValue());
                    }
                });

                BigDecimal rest = new BigDecimal(quantity.toString());
                for (Map<Storage, BigDecimal> m : list) {
                    Storage s = m.keySet().toArray(new Storage[m.keySet().size()])[0];
                    BigDecimal c = m.get(s);
                    if (c.doubleValue() == rest.doubleValue()) {
                        s.takeGoodsFromStorage(goods.getId(), rest);
                        s.removeGoodsFromStorage(goods.getId());

                        if (!dataBase.updateObject(s)) {
                            return Result.newStackTraceResultError(
                                    "Can not update storage " + s,
                                    Thread.currentThread());
                        }
                        break;

                    } else if (c.doubleValue() < rest.doubleValue()) {
                        //rest = Quantity.SUBSTRACT(rest.toString(), c.toString());
                        s.takeGoodsFromStorage(goods.getId(), c);
                        s.removeGoodsFromStorage(goods.getId());

                        if (!dataBase.updateObject(s)) {
                            return Result.newStackTraceResultError(
                                    "Can not update storage " + s, Thread.currentThread());
                        }
                        break;

                    } else if (c.doubleValue() > rest.doubleValue()) {
                        s.takeGoodsFromStorage(goods.getId(), rest);

                        if (!dataBase.updateObject(s)) {
                            return Result.newStackTraceResultError(
                                    "Can not update storage " + s,
                                    Thread.currentThread());
                        }

                        break;
                    }
                }
            }
        }

        return Result.newEmptySuccess();
    }

    public BigDecimal getGoodsCountOnStorage(Goods goods, String storageShortName) {
        ArrayList<Storage> list = dataBase.getFilteredResultList(
                Storage.class.getName(),
                "getShortName",
                Condition.newConditionEquial(storageShortName));
        if (!list.isEmpty()) {
            return list.get(0).getGoodsQuantity(goods.getId());
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getGoodsCountOnAllStorages(Goods goods) {
        BigDecimal quantity = BigDecimal.ZERO;
        ArrayList<Storage> list = dataBase.getAllObjectsList(Storage.class.getName());
        for (Storage storage : list) {
            if (storage.getShortName().equals(CarssierCore.STORAGE_RELEASED)) {
                continue;
            }
            quantity = Quantity.ADD(quantity.toString(), storage.getGoodsQuantity(goods.getId()).toString());
        }

        return quantity;
    }

    public LinkedList<Goods> getGoodsOnStorage(long storageId) {
        Result r = getStorage(storageId);
        if (r.isError()) {
            return new LinkedList<>();
        } else {
            Storage storage = (Storage) r.getObject();
            LinkedList<Goods> list = storage.getGoodsOnStorage(dataBase);
            if (list == null) {
                list = new LinkedList<>();
            }

            return list;
        }
    }

    public ArrayList<Goods> getAllGoods() {
        ArrayList<Goods> list = dataBase.getAllObjectsList(Goods.class.getName());
        if (list == null) {
            return new ArrayList<>();
        } else {
            return list;
        }
    }

    /**
     *
     * @param goodsId
     * @return If success will be return service
     */
    public Result getGoods(long goodsId) {
        Goods goods = (Goods) dataBase.getObject(Goods.class.getName(), goodsId);
        if (goods == null) {
            return Result.newStackTraceResultError(
                    "Can not find goods with id " + goodsId,
                    Thread.currentThread());
        } else {
            return Result.newResultSuccess(goods);
        }
    }

    public Result modifyService(Service service) {
        if (service == null) {
            return Result.newStackTraceResultError("Service is null", Thread.currentThread());
        }

        if (dataBase.updateObject(service)) {
            return Result.newResultSuccess(service);
        } else {
            return Result.newResult(false, "Can not update Service " + service);
        }
    }

    public Result modifyService(Service service, String newShortName, String newFullName,
            BigDecimal price, BigDecimal salaryPercent, String reason,
            String description, boolean applySalaryPercentToNeighbours,
            boolean isIndividualPrice, long executorCrewId) {

        service.setShortName(newShortName);
        service.setFullName(newFullName);
        service.setDescription(description);
        service.setSalaryPercent(salaryPercent);
        service.setIndividualPrice(isIndividualPrice);
        service.setExecutorCrewId(executorCrewId);

        Result rs = createServiceCommandment(reason);
        if (rs.isError()) {
            return rs;
        }

        ServiceCommandment serviceCommandment = (ServiceCommandment) rs.getObject();

        Result rp = setServicePrice(service, price, serviceCommandment);
        if (rp.isError()) {
            return rp;
        }

        if (applySalaryPercentToNeighbours) {
            // TODO
        }

        return Result.newEmptySuccess();
    }

    public Result removeService(String treePath, Service service) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BigDecimal getServicePrice(Service service) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPriceCountForSales(TreeLeafBasic treeLeafBasic) {
        String info;
        if (treeLeafBasic.getContainer().getClassName().indexOf("Goods") != -1) {
            Result r = getGoods(treeLeafBasic.getContainer().getId());
            if (r.isError()) {
                return r.getReason();
            }

            BigDecimal count = getGoodsCountOnAllStorages((Goods) r.getObject());
            double f = Math.floor(count.doubleValue());
            String strCount;
            if (count.doubleValue() - f == 0.0D) {
                strCount = "" + count.intValue();
            } else {
                strCount = count.toString();
            }
            info = ((Goods) r.getObject()).getSalePrice(dataBase).toString() + "р./" + strCount + ((Goods) r.getObject()).getMeasure();

        } else {
            Result r = getService(treeLeafBasic.getContainer().getId());
            if (r.isError()) {
                return r.getReason();
            }

            info = ((Service) r.getObject()).getSalePrice(dataBase).toString() + "р.";
        }

        return info;
    }

    public ArrayList<Service> getAllService() {
        ArrayList<Service> list = dataBase.getAllObjectsList(Service.class.getName());
        if (list == null) {
            return new ArrayList<>();
        } else {
            return list;
        }
    }

    /**
     *
     * @param serviceId
     * @return If success will be return Service
     */
    public Result getService(long serviceId) {
        Service service = (Service) dataBase.getObject(Service.class.getName(), serviceId);
        if (service == null) {
            return Result.newStackTraceResultError(
                    "Can not find Service with id = " + serviceId,
                    Thread.currentThread());
        } else {
            return Result.newResultSuccess(service);
        }
    }

    public Result modifyAgent(Agent agent, String shortName, String fullName,
            String INN, boolean isDefaultSupplier, Address defaultAddress,
            Contacts defaultContacts, Account defaultAccount) {

        Result r;
        agent.setShortName(shortName);
        agent.setFullName(fullName);
        agent.setINN(INN);
        agent.setDefaultAddress(defaultAddress);
        agent.setDefaultContacts(defaultContacts);
        agent.setDefaultAccount(defaultAccount);

        if (isDefaultSupplier) {
            r = setDefaultSupplier(agent);
        } else {
            if (dataBase.updateObject(agent)) {
                r = Result.newEmptySuccess();
            } else {
                r = Result.newStackTraceResultError("Can not update Agent " + agent,
                        Thread.currentThread());
            }
        }

        return r;
    }

    /**
     * Удаление оплаченного заказа, с возвратом на склад непроданных товаров, и
     * установкой отметки на возврат денег
     *
     * @param orderId
     * @param goodsSet
     * @param serviceSet
     * @return
     */
    public Result removeOrderCompliteChunkPayed(long orderId,
            LinkedHashSet<GoodsOrderRow> goodsSet, LinkedHashSet<ServiceOrderRow> serviceSet) {
        Result r = getOrder(orderId);
        if (r.isError()) {
            return Result.newStackTraceResultError(r.getReason(), Thread.currentThread());
        }

        Order order = (Order) r.getObject();
        if (order.getWorkStatus() != Order.WORK_COMPLETE) {
            return Result.newResultError("Order not have status WORK_COMPLITE");
        }

        r = getStorage(CarssierCore.STORAGE_IN_WORK);
        if (r.isError()) {
            return r;
        }
        Storage storageInWork = (Storage) r.getObject();

        r = getStorage(CarssierCore.STORAGE_RELEASED);
        if (r.isError()) {
            return r;
        }
        Storage storageReleased = (Storage) r.getObject();

        // unreturned goods to released storage
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.SERVICE) {
                continue;
            }

            r = getGoods(salesItem.getDbId());
            if (r.isError()) {
                return r;
            }
            Goods goods = (Goods) r.getObject();
            if (goods.isReturnable()) {
                continue;
            }

            storageInWork.takeGoodsFromStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageInWork)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            storageReleased.addGoodsOnStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageReleased)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        // returnable
        ArrayList<OrderRow> listRemoveOrderRow = new ArrayList<>();
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.SERVICE) {
                continue;
            }

            r = getGoods(salesItem.getDbId());
            if (r.isError()) {
                return r;
            }
            Goods goods = (Goods) r.getObject();
            if (!goods.isReturnable()) {
                continue;
            }

            storageInWork.takeGoodsFromStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageInWork)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            BigDecimal returnToStorage = BigDecimal.ZERO;
            for (GoodsOrderRow goodsOrderRow : goodsSet) {
                if (goodsOrderRow.orderRow.getSalesItem(dataBase).getDbId() == salesItem.getDbId()) {
                    if (goodsOrderRow.isSelected) {
                        returnToStorage = goodsOrderRow.orderRow.getCount();
                        listRemoveOrderRow.add(orderRow);
                    } else {
                        returnToStorage = goodsOrderRow.count;
                        orderRow.setCount(Quantity.SUBSTRACT(orderRow.getCount().toString(), goodsOrderRow.count.toString()));
                    }
                    break;
                }
            }
            storageReleased.addGoodsOnStorage(salesItem.getDbId(),
                    Quantity.SUBSTRACT(orderRow.getCount().toString(),
                    returnToStorage.toString()));
            if (!dataBase.updateObject(storageReleased)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            r = getStorage(goods.getDefaultStorage());
            if (r.isError()) {
                return r;
            }
            Storage goodsStorage = (Storage) r.getObject();
            goodsStorage.addGoodsOnStorage(goods.getId(), returnToStorage);
            if (!dataBase.updateObject(goodsStorage)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }
        order.getOrderRows().removeAll(listRemoveOrderRow);

        // services
        listRemoveOrderRow.clear();
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.GOODS) {
                continue;
            }

            r = getService(salesItem.getDbId());
            if (r.isError()) {
                return r;
            }
            Service service = (Service) r.getObject();

            for (ServiceOrderRow serviceOrderRow : serviceSet) {
                if (serviceOrderRow.orderRow.getSalesItem(dataBase).getDbId() == service.getId()) {
                    if (serviceOrderRow.isSelected) {
                        listRemoveOrderRow.add(orderRow);
                    } else {
                        orderRow.setCount(Quantity.SUBSTRACT(orderRow.getCount().toString(), serviceOrderRow.count.toString()));
                    }
                }
            }
        }
        order.getOrderRows().removeAll(listRemoveOrderRow);

        order.setPaidStatus(Order.RETURN_ALL_PAID);
        if (!dataBase.updateObject(order)) {
            return Result.newStackTraceResultError(Thread.currentThread());
        }

        TreeBasic treeUnpayed;
        r = getTreeOrdersUnpayed();
        if (r.isError()) {
            return r;
        }
        treeUnpayed = (TreeBasic) r.getObject();

        TreeLinkContainer tlc = new TreeLinkContainer(Order.class.getName(), orderId);
        treeUnpayed.getRootFolder().addTreeLeaf(new TreeLeafBasic(dataBase, tlc));

        return Result.newEmptySuccess();
    }

    /**
     * Удаление оплаченного заказа, с возвратом на склад непроданных товаров, и
     * установкой отметки на возврат денег и переносом в Выполненные
     *
     * @param orderId
     * @param goodsSet
     * @param serviceSet
     * @return
     */
    public Result removeOrderInWorkChunkPayed(long orderId,
            LinkedHashSet<GoodsOrderRow> goodsSet, LinkedHashSet<ServiceOrderRow> serviceSet) {
        Result r = getOrder(orderId);
        if (r.isError()) {
            return Result.newStackTraceResultError(r.getReason(), Thread.currentThread());
        }

        Order order = (Order) r.getObject();
        if (order.getWorkStatus() != Order.WORK_INWORK) {
            return Result.newResultError("Order not have status WORK_INWORK");
        }

        r = getStorage(CarssierCore.STORAGE_IN_WORK);
        if (r.isError()) {
            return r;
        }
        Storage storageInWork = (Storage) r.getObject();

        r = getStorage(CarssierCore.STORAGE_RELEASED);
        if (r.isError()) {
            return r;
        }
        Storage storageReleased = (Storage) r.getObject();

        // unreturned goods to released storage
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.SERVICE) {
                continue;
            }

            r = getGoods(salesItem.getDbId());
            if (r.isError()) {
                return r;
            }
            Goods goods = (Goods) r.getObject();
            if (goods.isReturnable()) {
                continue;
            }

            storageInWork.takeGoodsFromStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageInWork)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            storageReleased.addGoodsOnStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageReleased)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        // returnable
        ArrayList<OrderRow> listRemoveOrderRow = new ArrayList<>();
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.SERVICE) {
                continue;
            }

            r = getGoods(salesItem.getDbId());
            if (r.isError()) {
                return r;
            }
            Goods goods = (Goods) r.getObject();
            if (!goods.isReturnable()) {
                continue;
            }

            storageInWork.takeGoodsFromStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageInWork)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            BigDecimal returnToStorage = BigDecimal.ZERO;
            for (GoodsOrderRow goodsOrderRow : goodsSet) {
                if (goodsOrderRow.orderRow.getSalesItem(dataBase).getDbId() == salesItem.getDbId()) {
                    if (goodsOrderRow.isSelected) {
                        returnToStorage = goodsOrderRow.orderRow.getCount();
                        listRemoveOrderRow.add(orderRow);
                    } else {
                        returnToStorage = goodsOrderRow.count;
                        orderRow.setCount(Quantity.SUBSTRACT(orderRow.getCount().toString(), goodsOrderRow.count.toString()));
                    }
                    break;
                }
            }
            storageReleased.addGoodsOnStorage(salesItem.getDbId(),
                    Quantity.SUBSTRACT(orderRow.getCount().toString(),
                    returnToStorage.toString()));
            if (!dataBase.updateObject(storageReleased)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            r = getStorage(goods.getDefaultStorage());
            if (r.isError()) {
                return r;
            }
            Storage goodsStorage = (Storage) r.getObject();
            goodsStorage.addGoodsOnStorage(goods.getId(), returnToStorage);
            if (!dataBase.updateObject(goodsStorage)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }
        order.getOrderRows().removeAll(listRemoveOrderRow);

        // services
        listRemoveOrderRow.clear();
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.GOODS) {
                continue;
            }

            r = getService(salesItem.getDbId());
            if (r.isError()) {
                return r;
            }
            Service service = (Service) r.getObject();

            for (ServiceOrderRow serviceOrderRow : serviceSet) {
                if (serviceOrderRow.orderRow.getSalesItem(dataBase).getDbId() == service.getId()) {
                    if (serviceOrderRow.isSelected) {
                        listRemoveOrderRow.add(orderRow);
                    } else {
                        orderRow.setCount(Quantity.SUBSTRACT(orderRow.getCount().toString(), serviceOrderRow.count.toString()));
                    }
                }
            }
        }
        order.getOrderRows().removeAll(listRemoveOrderRow);

        order.setWorkStatus(Order.WORK_COMPLETE);
        order.setPaidStatus(Order.RETURN_ALL_PAID);
        if (!dataBase.updateObject(order)) {
            return Result.newStackTraceResultError(Thread.currentThread());
        }

        TreeBasic treeInWork, treeUnpayed;
        r = getTreeOrdersInWork();
        if (r.isError()) {
            return r;
        }
        treeInWork = (TreeBasic) r.getObject();

        r = getTreeOrdersUnpayed();
        if (r.isError()) {
            return r;
        }
        treeUnpayed = (TreeBasic) r.getObject();

        r = moveOrder(order, treeInWork, treeUnpayed);
        if (r.isError()) {
            return r;
        }

        r = getTreeOrdersComplete();
        if (r.isError()) {
            return r;
        }
        TreeBasic treeComplite = (TreeBasic) r.getObject();
        TreeLinkContainer tlc = new TreeLinkContainer(Order.class.getName(), orderId);
        treeComplite.getRootFolder().addTreeLeaf(new TreeLeafBasic(dataBase, tlc));

        return Result.newEmptySuccess();
    }

    /**
     * Удаление неоплаченного заказа, с возвратом на склад непроданных товаров
     *
     * @param orderId
     * @param goodsSet
     * @param serviceSet
     * @return
     */
    public Result removeOrderInWorkChunk(long orderId,
            LinkedHashSet<GoodsOrderRow> goodsSet, LinkedHashSet<ServiceOrderRow> serviceSet) {
        Result r = getOrder(orderId);
        if (r.isError()) {
            return Result.newStackTraceResultError(r.getReason(), Thread.currentThread());
        }

        Order order = (Order) r.getObject();
        if (order.getWorkStatus() != Order.WORK_INWORK) {
            return Result.newResultError("Order not have status WORK_INWORK");
        }

        r = getStorage(CarssierCore.STORAGE_IN_WORK);
        if (r.isError()) {
            return r;
        }
        Storage storageInWork = (Storage) r.getObject();

        r = getStorage(CarssierCore.STORAGE_RELEASED);
        if (r.isError()) {
            return r;
        }
        Storage storageReleased = (Storage) r.getObject();

        // unreturned goods to released storage
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.SERVICE) {
                continue;
            }

            r = getGoods(salesItem.getDbId());
            if (r.isError()) {
                return r;
            }
            Goods goods = (Goods) r.getObject();
            if (goods.isReturnable()) {
                continue;
            }

            storageInWork.takeGoodsFromStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageInWork)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            storageReleased.addGoodsOnStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageReleased)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        // returnable
        ArrayList<OrderRow> listRemoveOrderRow = new ArrayList<>();
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.SERVICE) {
                continue;
            }

            r = getGoods(salesItem.getDbId());
            if (r.isError()) {
                return r;
            }
            Goods goods = (Goods) r.getObject();
            if (!goods.isReturnable()) {
                continue;
            }

            storageInWork.takeGoodsFromStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageInWork)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            BigDecimal returnToStorage = BigDecimal.ZERO;
            for (GoodsOrderRow goodsOrderRow : goodsSet) {
                if (goodsOrderRow.orderRow.getSalesItem(dataBase).getDbId() == salesItem.getDbId()) {
                    if (goodsOrderRow.isSelected) {
                        returnToStorage = goodsOrderRow.orderRow.getCount();
                        listRemoveOrderRow.add(orderRow);
                    } else {
                        returnToStorage = goodsOrderRow.count;
                        orderRow.setCount(Quantity.SUBSTRACT(orderRow.getCount().toString(), goodsOrderRow.count.toString()));
                    }
                    break;
                }
            }
            storageReleased.addGoodsOnStorage(salesItem.getDbId(),
                    Quantity.SUBSTRACT(orderRow.getCount().toString(),
                    returnToStorage.toString()));
            if (!dataBase.updateObject(storageReleased)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            r = getStorage(goods.getDefaultStorage());
            if (r.isError()) {
                return r;
            }
            Storage goodsStorage = (Storage) r.getObject();
            goodsStorage.addGoodsOnStorage(goods.getId(), returnToStorage);
            if (!dataBase.updateObject(goodsStorage)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }
        order.getOrderRows().removeAll(listRemoveOrderRow);

        // services
        listRemoveOrderRow.clear();
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.GOODS) {
                continue;
            }

            r = getService(salesItem.getDbId());
            if (r.isError()) {
                return r;
            }
            Service service = (Service) r.getObject();

            for (ServiceOrderRow serviceOrderRow : serviceSet) {
                if (serviceOrderRow.orderRow.getSalesItem(dataBase).getDbId() == service.getId()) {
                    if (serviceOrderRow.isSelected) {
                        listRemoveOrderRow.add(orderRow);
                    } else {
                        orderRow.setCount(Quantity.SUBSTRACT(orderRow.getCount().toString(), serviceOrderRow.count.toString()));
                    }
                }
            }
        }
        order.getOrderRows().removeAll(listRemoveOrderRow);

        order.setWorkStatus(Order.WORK_CHUNKED_RETURN);
        if (!dataBase.updateObject(order)) {
            return Result.newStackTraceResultError(Thread.currentThread());
        }

        return Result.newEmptySuccess();
    }

    public Result removeOrderInWorkAll(long orderId) {
        Result r = getOrder(orderId);
        if (r.isError()) {
            return Result.newStackTraceResultError(r.getReason(), Thread.currentThread());
        }

        Order order = (Order) r.getObject();
        if (order.getWorkStatus() != Order.WORK_INWORK) {
            return Result.newResultError("Order not have status WORK_INWORK");
        }

        // move between storages
        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.SERVICE) {
                continue;
            }

            r = getStorage(CarssierCore.STORAGE_IN_WORK);
            if (r.isError()) {
                return r;
            }
            Storage storageFrom = (Storage) r.getObject();
            storageFrom.takeGoodsFromStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageFrom)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            r = getStorage(CarssierCore.STORAGE_RELEASED);
            if (r.isError()) {
                return r;
            }
            Storage storageTo = (Storage) r.getObject();
            storageTo.addGoodsOnStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageTo)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        //r = getTreeOrdersInWork();
        //if (r.isError()) {
        //    return r;
        //}
        //TreeBasic treeInWork = (TreeBasic) r.getObject();

        //r = getTreeOrdersUnpayed();
        //if (r.isError()) {
        //    return r;
        //}
        //TreeBasic treeUnpayed = (TreeBasic) r.getObject();

        //r = removeOrderFromTrees(order, treeInWork, treeUnpayed);
        //if (r.isError()) {
        //    return r;
        //}

        dataBase.deleteObject(Order.class.getName(), orderId);

        return Result.newEmptySuccess();
    }

    public Result removeOrderDeffered(long orderId) {
        Result r = getOrder(orderId);
        if (r.isError()) {
            return Result.newStackTraceResultError(r.getReason(), Thread.currentThread());
        }

        Order order = (Order) r.getObject();
        if (order.getWorkStatus() != Order.WORK_DEFERRED) {
            return Result.newResultError("Order not have status IN_DEFFERED");
        }

        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.SERVICE) {
                continue;
            }

            r = getStorage(CarssierCore.STORAGE_DEFFERED);
            if (r.isError()) {
                return r;
            }
            Storage storageFrom = (Storage) r.getObject();
            storageFrom.takeGoodsFromStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageFrom)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            r = getGoods(salesItem.getDbId());
            if (r.isError()) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }

            r = getStorage(((Goods) r.getObject()).getDefaultStorage());
            if (r.isError()) {
                return r;
            }
            Storage storageTo = (Storage) r.getObject();
            storageTo.addGoodsOnStorage(salesItem.getDbId(), orderRow.getCount());
            if (!dataBase.updateObject(storageTo)) {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        // remove from trees
        TreeBasic trees[] = new TreeBasic[2];
        r = getTreeOrdersDeffered();
        if (r.isError()) {
            return r;
        }
        trees[0] = (TreeBasic) r.getObject();

        r = getTreeOrdersUnpayed();
        if (r.isError()) {
            return r;
        }
        trees[1] = (TreeBasic) r.getObject();

        r = removeOrderFromTrees(order, trees);
        if (r.isError()) {
            return r;
        }

        return Result.newEmptySuccess();
    }

//    public Result removeOrder(long orderId, LinkedHashSet<GoodsOrderRow> goodsSet,
//            LinkedHashSet<ServiceOrderRow> serviceSet) {
//
//        Result r = getOrder(orderId);
//        if (r.isError()) {
//            return r;
//        }
//        Order order = (Order) r.getObject();
//
//        // Goods
//        for (GoodsOrderRow goodsOrderRow : goodsSet) {
//            if (goodsOrderRow.isSelected) {
//                continue;
//            }
//
//            // find storage
//            long goodsId = goodsOrderRow.orderRow.getSalesItem(dataBase).getId();
//            r = getGoods(goodsId);
//            if (r.isError()) {
//                return Result.newStackTraceResultError(
//                        "Can not find Goods with id = " + goodsId, 
//                        Thread.currentThread());
//            }
//            Goods goods = (Goods) r.getObject();
//            r = getStorage(goods.getDefaultStorage());
//            if (r.isError()) {
//                return Result.newStackTraceResultError(
//                        "Can not find Storage with id = " + goods.getDefaultStorage(), 
//                        Thread.currentThread());
//            }
//            Storage storage = (Storage) r.getObject();
//
//            // return to storage
//            storage.addGoodsOnStorage(goods.getId(), goodsOrderRow.count);
//            if (!dataBase.updateObject(storage)) {
//                return Result.newStackTraceResultError(
//                        "Can not update Storage " + storage, Thread.currentThread());
//            }
//
//            // remove from work storage
//            if (order.getWorkStatus() == Order.WORK_INWORK) {
//                r = getStorage("В работе");
//                if (r.isError()) {
//                    return Result.newStackTraceResultError(
//                            "Can not find Storage with id = " + goods.getDefaultStorage(), 
//                            Thread.currentThread());
//                }
//                Storage workStorage = (Storage) r.getObject();
//                workStorage.takeGoodsFromStorage(goods.getId(), goodsOrderRow.count);
//                if (!dataBase.updateObject(workStorage)) {
//                    return Result.newStackTraceResultError(
//                            "Can not update Storage " + workStorage, 
//                            Thread.currentThread());
//                }
//            }
//
//            // remove from release storage
//            if (order.getWorkStatus() == Order.WORK_COMPLETE) {
//                r = getStorage("Отпущено");
//                if (r.isError()) {
//                    return Result.newStackTraceResultError(
//                            "Can not find Storage with id = " + goods.getDefaultStorage(), 
//                            Thread.currentThread());
//                }
//                Storage workStorage = (Storage) r.getObject();
//                workStorage.takeGoodsFromStorage(goods.getId(), goodsOrderRow.count);
//                if (!dataBase.updateObject(workStorage)) {
//                    return Result.newStackTraceResultError(
//                            "Can not update Storage " + workStorage, 
//                            Thread.currentThread());
//                }
//            }
//
//            // remove from order count
//            OrderRow orderRow = goodsOrderRow.orderRow;
//            BigDecimal newCount = Quantity.SUBSTRACT(
//                    orderRow.getCount().toString(),
//                    goodsOrderRow.count.toString());
//            if (newCount.doubleValue() <= 0) {
//                order.getOrderRows().remove(orderRow);
//            } else {
//                orderRow.setCount(newCount);
//            }
//        }
//
//        // Service
//        for (ServiceOrderRow serviceOrderRow : serviceSet) {
//            if (serviceOrderRow.isSelected) {
//                continue;
//            }
//            OrderRow orderRow = serviceOrderRow.orderRow;
//            BigDecimal newCount = Quantity.SUBSTRACT(
//                    orderRow.getCount().toString(),
//                    serviceOrderRow.count.toString());
//            if (newCount.doubleValue() <= 0) {
//                order.getOrderRows().remove(orderRow);
//
//            } else {
//                orderRow.setCount(newCount);
//            }
//        }
//
//        if (order.getOrderRows().isEmpty()) {
//            // Any trees wich have this Order must be cleaned from it
//            // Check all trees
//            r = getTreeOrdersDeffered();
//            if (r.isError()) {
//                return r;
//            }
//            TreeBasic treeBasic = (TreeBasic) r.getObject();
//            List<TreeLeaf> list = treeBasic.getRootFolder().getAllDescendTreeLeaves();
//            for (TreeLeaf tl : list) {
//                if (tl.getContainer().getId() == orderId) {
//                    treeBasic.getRootFolder().removeTreeLeaf(tl);
//                    break;
//                }
//            }
//
//            r = getTreeOrdersInWork();
//            if (r.isError()) {
//                return r;
//            }
//            treeBasic = (TreeBasic) r.getObject();
//            list = treeBasic.getRootFolder().getAllDescendTreeLeaves();
//            for (TreeLeaf tl : list) {
//                if (tl.getContainer().getId() == orderId) {
//                    treeBasic.getRootFolder().removeTreeLeaf(tl);
//                    break;
//                }
//            }
//
//            r = getTreeOrdersComplete();
//            if (r.isError()) {
//                return r;
//            }
//            treeBasic = (TreeBasic) r.getObject();
//            list = treeBasic.getRootFolder().getAllDescendTreeLeaves();
//            for (TreeLeaf tl : list) {
//                if (tl.getContainer().getId() == orderId) {
//                    treeBasic.getRootFolder().removeTreeLeaf(tl);
//                    break;
//                }
//            }
//
//            r = getTreeOrdersUnpayed();
//            if (r.isError()) {
//                return r;
//            }
//            treeBasic = (TreeBasic) r.getObject();
//            list = treeBasic.getRootFolder().getAllDescendTreeLeaves();
//            for (TreeLeaf tl : list) {
//                if (tl.getContainer().getId() == orderId) {
//                    treeBasic.getRootFolder().removeTreeLeaf(tl);
//                    break;
//                }
//            }
//
//            // remove Order
//            dataBase.deleteObject(order);
//
//        } else {
//            if (!dataBase.updateObject(order)) {
//                return Result.newStackTraceResultError(
//                        "Can not update Order with id = " + orderId, 
//                        Thread.currentThread());
//            }
//        }
//
//        return Result.newEmptySuccess();
//    }
    /**
     *
     * @param order
     * @param removeFromTrees
     * @return Result success or not
     */
    public Result removeOrderFromTrees(Order order, TreeBasic... removeFromTrees) {
        if (order == null) {
            return Result.newStackTraceResultError("Order is null", Thread.currentThread());
        }
        return removeOrderFromTrees(order.getId(), removeFromTrees);
    }

    /**
     *
     * @param orderId
     * @param removeFromTrees
     * @return Result success or not
     */
    public Result removeOrderFromTrees(long orderId, TreeBasic... removeFromTrees) {
        Order order = (Order) dataBase.getObject(Order.class.getName(), orderId);

        if (order == null) {
            return Result.newStackTraceResultError(
                    "Can not find Order width id " + orderId,
                    Thread.currentThread());
        }

        for (TreeBasic tree : removeFromTrees) {
            List<TreeLeaf> list = tree.getRootFolder().getAllDescendTreeLeaves();
            for (TreeLeaf treeLeaf : list) {
                if (treeLeaf.getContainer().getClassName().equals(Order.class.getName())
                        && treeLeaf.getContainer().getId() == orderId) {
                    tree.getRootFolder().removeTreeLeaf(treeLeaf);
                    break;
                }
            }
        }

        return Result.newEmptySuccess();
    }

    public Result paidWOCash(Order order, BigDecimal pay) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Result paidLeft(Order order, BigDecimal pay) {
        //order.setPaidType(Order.LEFT);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Result paidCard(Order order, BigDecimal pay) {
        //order.setPaidType(Order.CARD);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Result returnPaid(Order order) {
        Result result = null;
        try {
            CashMachineResponse cmr = cashMachine.paymentMoney(order.getTotalWithTotalDiscount(), 48);
            result = Result.newResult(true, cmr.toString());

        } catch (XPathExpressionException | NullPointerException | CashMachineException ex) {
            result = Result.newStackTraceResultError(Thread.currentThread());

        } finally {
            return result;
        }
    }

    /**
     *
     * @param orderId
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return Order work status (int)
     */
    public Result getOrderWorkStatus(long orderId) {
        Order order = (Order) dataBase.getObject(Order.class.getName(), orderId);
        if (order == null) {
            return Result.newStackTraceResultError(
                    "Can not find Order with id = " + orderId,
                    Thread.currentThread());
        } else {
            return Result.newResultSuccess(order.getWorkStatus());
        }
    }

    public ArrayList<Order> getNotClosedComplitedOrders() {
        Map<Long, Object> map = dataBase.getFilteredResultMap(
                Order.class.getName(),
                "getWorkStatus",
                Condition.newConditionEquial("" + Order.WORK_COMPLETE));
        ArrayList<Order> list = dataBase.applyFilterList(
                map,
                "getStatus",
                Condition.newConditionEquial("" + Order.EDITABLE));

        return list;
    }

    /**
     *
     * @param orderId
     * @return If success then will be return Order
     */
    public Result getOrder(long orderId) {
        Order order = (Order) dataBase.getObject(Order.class.getName(), orderId);
        if (order == null) {
            return Result.newStackTraceResultError(
                    "Can not find Order with id = " + orderId,
                    Thread.currentThread());
        } else {
            return Result.newResultSuccess(order);
        }
    }

    public ArrayList<Order> getOrdersFromCurrentShift() {
        Map<Long, Object> map = dataBase.getFilteredResultMap(
                Order.class.getName(),
                "getReportBundleId",
                Condition.newConditionEquial("0"));

        ArrayList<Order> list = dataBase.applyFilterList(
                map,
                "getWorkStatus",
                Condition.newCondition(Condition.EQUAL,
                "" + Order.WORK_COMPLETE));

        if (list == null) {
            list = new ArrayList<>();
        }

        return list;
    }

    public ArrayList<Order> getOrdersDone() {
        Map<Long, Object> map = dataBase.getFilteredResultMap(
                Order.class.getName(),
                "getReportBundleId",
                Condition.newConditionEquial("0"));

        ArrayList<Order> list = dataBase.applyFilterList(
                map,
                "getWorkStatus",
                Condition.newConditionEquial("" + Order.WORK_COMPLETE));

        if (list == null) {
            list = new ArrayList<>();
        }

        return list;
    }

    public ArrayList<Order> getOrdersComplete() {
        ArrayList<Order> orders;
        orders = dataBase.getAllObjectsList(Order.class.getName());

        if (orders == null) {
            return new ArrayList<>();

        } else {
            ArrayList<Order> removeList = new ArrayList<>();
            for (Order order : orders) {
                if (order.getWorkStatus() != Order.WORK_COMPLETE) {
                    removeList.add(order);
                }
            }

            orders.removeAll(removeList);
            return orders;
        }
    }

    public ArrayList<Order> getOrdersInWork() {
        ArrayList<Order> orders;
        orders = dataBase.getAllObjectsList(Order.class.getName());

        if (orders == null) {
            return new ArrayList<>();

        } else {
            ArrayList<Order> removeList = new ArrayList<>();
            for (Order order : orders) {
                if (order.getWorkStatus() != Order.WORK_INWORK) {
                    removeList.add(order);
                }
            }

            orders.removeAll(removeList);
            return orders;
        }
    }

    public ArrayList<Order> getOrdersUnpaid() {
        ArrayList<Order> orders = dataBase.getAllObjectsList(Order.class.getName());

        if (orders == null) {
            return new ArrayList<>();
        }

        ArrayList<Order> resultSet = new ArrayList<>();
        for (Order order : orders) {
            if (order.getPaidStatus() == Order.UNPAID || order.getPaidStatus() == Order.WAIT_PAY_BANK) {
                resultSet.add(order);
            }
        }

        Collections.sort(resultSet, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return ((Integer) o1.getPaidStatus()).compareTo(o2.getPaidStatus());
            }
        });

        return resultSet;
    }

    public ArrayList<Order> getOrdersPaid() {
        ArrayList<Order> orders = dataBase.getAllObjectsList(Order.class.getName());

        if (orders == null) {
            return new ArrayList<>();
        }

        ArrayList<Order> resultSet = new ArrayList<>();
        for (Order order : orders) {
            if (order.getPaidStatus() == Order.PAID) {
                resultSet.add(order);
            }
        }

        Collections.sort(resultSet, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        return resultSet;
    }

    public Result closeShift() {
        ArrayList<Order> listResult = (ArrayList<Order>) getOrdersDone();
        if (listResult.isEmpty()) {
            return Result.newStackTraceResultError(
                    "Add nothing to report bundle, because "
                    + "orders list is empty.",
                    Thread.currentThread());
        }

        ArrayList<Order> list = new ArrayList<>();
        Result r = getGlobalRuleItemByKey("closeShiftCompliteAndPaid");
        if (r.isError()) {
            return Result.newStackTraceResultError(Thread.currentThread());

        } else {
            if ((Boolean) r.getObject()) {
                for (Order order : listResult) {
                    if (order.getPaidStatus() == Order.PAID) {
                        list.add(order);
                    }
                }
            } else {
                list.addAll(listResult);
            }
        }

        ReportShift reportShift = new ReportShift();
        reportShift.setDate(new Date());
        reportShift.setOrderSet(list);
        reportShift.setSalaryStatus(ReportShift.SALARY_NOT_DISTRIBUTED);
        reportShift.setImageFileName("img/icons/salary64.png");

        HashSet<Employee> employess = new HashSet<>();
        ArrayList<Crew> crews = dataBase.getAllObjectsList(Crew.class.getName());
        for (Crew crew : crews) {
            for (long idEmployee : crew.getIdsEmployees()) {
                employess.add((Employee) dataBase.getObject(Employee.class.getName(), idEmployee));
            }
        }

        reportShift.setEmployeeSet(employess);
        dataBase.addObject(reportShift);
        clearCrews();

        for (Order order : list) {
            order.setReportBundleId(reportShift.getId());
            dataBase.updateObject(order);
        }

        Date closeDate = new Date();
        AccountBookHandler.getInstance().loadSyntheticAccounts();
        BigDecimal[] saldo_90_1 = AccountBookHandler.getInstance().getTrialBalance(
                "90.1", DateTime.getFormatedDate("yyyy-MM-dd HH:mm:ss", closeDate));
        BigDecimal profit = Money.SUBSTRACT(saldo_90_1[1].toString(), saldo_90_1[0].toString());
        // 90.1 - 90.9
        AccountRule accountRule = AccountBookHandler.getInstance().getAccountRuleByName("Закрытие смены 90.1");
        String desc = "Закрытие смены по счету 90.1";
        String json = "{reportShiftId:" + reportShift + "}";
        AccountBookHandler.getInstance().getAccountBook().accountPosting(
                accountRule, profit, desc, json, closeDate, dataBase);

        BigDecimal[] saldo_90_2 = AccountBookHandler.getInstance().getTrialBalance(
                "90.2", DateTime.getFormatedDate("yyyy-MM-dd HH:mm:ss", closeDate));
        BigDecimal costs = Money.SUBSTRACT(saldo_90_2[0].toString(), saldo_90_2[1].toString());
        // 90.9 - 90.2
        accountRule = AccountBookHandler.getInstance().getAccountRuleByName("Закрытие смены 90.2");
        desc = "Закрытие смены по счету 90.2";
        json = "{reportShiftId:" + reportShift + "}";
        AccountBookHandler.getInstance().getAccountBook().accountPosting(
                accountRule, costs, desc, json, closeDate, dataBase);

        Date date = DateTime.getDateAfterRoll(new Date(), Calendar.HOUR, 1);
        BigDecimal[] saldo_90_9 = AccountBookHandler.getInstance().getTrialBalance(
                "90.9", DateTime.getFormatedDate("yyyy-MM-dd HH:mm:ss", date));
        profit = Money.SUBSTRACT(saldo_90_9[1].toString(), saldo_90_9[0].toString());
        // 90.9 - 84
        // 84 - 75.2
        accountRule = AccountBookHandler.getInstance().getAccountRuleByName("Закрытие смены 90.9");
        desc = "Закрытие смены по счету 90.9";
        json = "{reportShiftId:" + reportShift + "}";
        AccountBookHandler.getInstance().getAccountBook().accountPosting(
                accountRule, profit, desc, json, closeDate, dataBase);
        reportShift.setOwnerProfit(profit);
        dataBase.updateObject(reportShift);

        return Result.newEmptySuccess();
    }

    /**
     *
     * @return Closed shifts as ArrayList<ReportShift> sorted by date
     */
    public ArrayList<ReportShift> getClosedShifts() {
        ArrayList<ReportShift> list = dataBase.getAllObjectsList(ReportShift.class.getName());
        if (list == null) {
            list = new ArrayList<>();
        }

        Collections.sort(list, new Comparator<ReportShift>() {
            @Override
            public int compare(ReportShift o1, ReportShift o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

        return list;
    }

    private void clearCrews() {
        Iterator i = dataBase.getObjects(Crew.class.getName()).iterator();
        while (i.hasNext()) {
            Crew crew = (Crew) i.next();
            crew.removeAllEmployees();
            dataBase.updateObject(crew);
        }
    }

    /**
     * Order will be close and move from in worke tree to complite tree
     *
     * @param order
     * @return
     */
    public Result closeOrder(Order order) {
        if (order.getStatus() == Order.CLOSED) {
            return Result.newStackTraceResultError(
                    "Document was previosly closed", Thread.currentThread());
        }

        order.setWorkStatus(Order.WORK_COMPLETE);
        order.setStatus(Order.CLOSED);
        order.setClosedDate(new Date());
        if (!dataBase.updateObject(order)) {
            return Result.newStackTraceResultError(
                    "Can not update order " + order, Thread.currentThread());
        }

        Storage storageFrom, storageTo;
        Result r = getStorage(CarssierCore.STORAGE_IN_WORK);
        if (r.isError()) {
            Agent a = (Agent) getDefaultSupplier().getObject();
            r = addStorage(CarssierCore.STORAGE_IN_WORK, CarssierCore.STORAGE_IN_WORK,
                    a, a.getDefaultAddress(), a.getDefaultContacts(), true);
        }
        storageFrom = (Storage) r.getObject();

        r = getStorage(CarssierCore.STORAGE_RELEASED);
        if (r.isError()) {
            Agent agent = (Agent) getDefaultSupplier().getObject();
            if (agent == null) {
                return Result.newStackTraceResultError(
                        "Can not find default supplier", Thread.currentThread());
            }
            r = addStorage(CarssierCore.STORAGE_RELEASED, CarssierCore.STORAGE_RELEASED,
                    agent, agent.getDefaultAddress(),
                    agent.getDefaultContacts(), true);
            if (r.isError()) {
                return r;
            }

            storageTo = (Storage) r.getObject();

        } else {
            storageTo = (Storage) r.getObject();
        }

        for (OrderRow orderRow : order.getOrderRows()) {
            SalesItem salesItem = orderRow.getSalesItem(dataBase);
            if (salesItem.getType() == SalesItem.SERVICE) {
                continue;
            }

            Goods goods = (Goods) dataBase.getObject(Goods.class.getName(),
                    salesItem.getDbId());

            storageFrom.takeGoodsFromStorage(goods.getId(), orderRow.getCount());
            if (!dataBase.updateObject(storageFrom)) {
                return Result.newStackTraceResultError(
                        "Can not update Storage " + storageFrom, Thread.currentThread());
            }

            storageTo.addGoodsOnStorage(goods.getId(), orderRow.getCount());
            if (!dataBase.updateObject(storageTo)) {
                return Result.newStackTraceResultError(
                        "Can not update Storage " + storageTo, Thread.currentThread());
            }

            r = addStorageRecord(goods, orderRow.getCount(), storageFrom,
                    storageTo, order.getSupplier(), order.getCustomer());

            if (r.isError()) {
                return r;
            }
        }

        closeOrderThroughAccount(order);

        return Result.newEmptySuccess();
    }

    private void closeOrderThroughAccount(Order order) {
        ArrayList<GoodsAnalytics> listGoodsAnalytics = dataBase.getAllObjectsList(
                GoodsAnalytics.class.getName());
        String accountRuleName = "Отпуск товара";
        AccountRule accountRule = AccountBookHandler.getInstance().
                getAccountRuleByName(accountRuleName);
        for (OrderRow orderRow : order.getOrderRows()) {
            if (orderRow.getSalesItem(dataBase).getType() == SalesItem.GOODS) {

                ArrayList<GoodsAnalytics> sortedList = new ArrayList<>();
                for (GoodsAnalytics goodsAnalytics : listGoodsAnalytics) {
                    if (goodsAnalytics.getGoodsId() != orderRow.getSalesItem(dataBase).getDbId()) {
                        continue;
                    }

                    if (goodsAnalytics.getRest().doubleValue() <= 0) {
                        continue;
                    }

                    sortedList.add(goodsAnalytics);
                }

                Collections.sort(sortedList, new Comparator<GoodsAnalytics>() {
                    @Override
                    public int compare(GoodsAnalytics o1, GoodsAnalytics o2) {
                        return o1.getDate().compareTo(o2.getDate());
                    }
                });

                BigDecimal quantityForOut = BigDecimal.ZERO;
                for (GoodsAnalytics goodsAnalytics : sortedList) {
                    if (goodsAnalytics.getRest().doubleValue() >= orderRow.getCount().doubleValue()) {
                        quantityForOut = Quantity.ADD(orderRow.getCount().toString(),
                                quantityForOut.toString());
                        goodsAnalytics.takeGoods(order.getId(), orderRow.getCount(), dataBase);

                        String analyticsData = "{orderId:" + order.getId() + ", "
                                + "goodsId:" + goodsAnalytics.getGoodsId() + ","
                                + "goodsAnalyticsId:" + goodsAnalytics.getId() + "}";
                        Goods g = (Goods) dataBase.getObject(Goods.class.getName(),
                                goodsAnalytics.getGoodsId());

                        String info = "Отпуск товара " + g.getShortName() + " ("
                                + orderRow.getCount() + " " + g.getMeasure() + ")";
                        AccountPost accountPost = AccountBookHandler.getInstance().
                                getAccountBook().accountPosting(
                                accountRule,
                                Money.MULTIPLY(goodsAnalytics.getPrice(), orderRow.getCount()),
                                info,
                                analyticsData,
                                new Date(),
                                dataBase);

                        break;

                    } else {
                        BigDecimal rest = goodsAnalytics.getRest();
                        quantityForOut = Quantity.ADD(rest.toString(), quantityForOut.toString());
                        goodsAnalytics.takeGoods(order.getId(), rest, dataBase);

                        String analyticsData = "{orderId:" + order.getId() + ", "
                                + "goodsId:" + goodsAnalytics.getGoodsId() + ","
                                + "goodsAnalyticsId:" + goodsAnalytics.getId() + "}";
                        Goods g = (Goods) dataBase.getObject(Goods.class.getName(),
                                goodsAnalytics.getGoodsId());

                        String info = "Отпуск товара " + g.getShortName() + " ("
                                + rest + " " + g.getMeasure() + ")";
                        AccountPost accountPost = AccountBookHandler.getInstance().
                                getAccountBook().accountPosting(
                                accountRule,
                                Money.MULTIPLY(goodsAnalytics.getPrice(), rest),
                                info,
                                analyticsData,
                                new Date(),
                                dataBase);
                    }
                }

                if (!sortedList.isEmpty()) {
                    if (quantityForOut.doubleValue() < orderRow.getCount().doubleValue()) {
                        GoodsAnalytics goodsAnalytics = sortedList.get(sortedList.size() - 1);
                        BigDecimal count = Quantity.SUBSTRACT(orderRow.getCount().toString(), quantityForOut.toString());
                        goodsAnalytics.takeGoods(
                                order.getId(),
                                count,
                                dataBase);

                        String analyticsData = "{orderId:" + order.getId() + ", "
                                + "goodsId:" + goodsAnalytics.getGoodsId() + ","
                                + "goodsAnalyticsId:" + goodsAnalytics.getId() + "}";
                        Goods g = (Goods) dataBase.getObject(Goods.class.getName(),
                                goodsAnalytics.getGoodsId());

                        String info = "Отпуск товара " + g.getShortName() + " ("
                                + count + " " + g.getMeasure() + ")";
                        AccountPost accountPost = AccountBookHandler.getInstance().
                                getAccountBook().accountPosting(
                                accountRule,
                                Money.MULTIPLY(goodsAnalytics.getPrice(), count),
                                info,
                                analyticsData,
                                new Date(),
                                dataBase);

                    }
                }

                for (GoodsAnalytics goodsAnalytics : sortedList) {
                    System.out.println(">>>>>>>>>>>> goodsAnalytics.getRest() " + goodsAnalytics.getRest());
                    System.out.println(">>>>>>>>>>>> goodsAnalytics.getQuantity() " + goodsAnalytics.getQuantity());
                    System.out.println(">>>>>>>>>>>> goodsAnalytics.getAnalyticMap() " + goodsAnalytics.getAnalyticMap());
                }
            }
        }

        // Начислить зарплату
        accountRuleName = "Начисление зарплаты";
        accountRule = AccountBookHandler.getInstance().getAccountRuleByName(accountRuleName);
        BigDecimal totalPercentDiscount = order.getTotalPercentDiscount();
        for (OrderRow orderRow : order.getOrderRows()) {
            if (orderRow.getSalesItem(dataBase).getType() == SalesItem.SERVICE) {
                Service s = (Service) dataBase.getObject(Service.class.getName(),
                        orderRow.getSalesItem(dataBase).getDbId());
                Map<Long, BigDecimal> md = (Map<Long, BigDecimal>) s.getAdditionInfo().get("salaryDistribution");
                if (md != null) {
                    Iterator<Long> it = md.keySet().iterator();
                    while (it.hasNext()) {
                        long crewId = it.next();
                        BigDecimal salaryPercent = md.get(crewId);

                        BigDecimal discountSum = Money.MULTIPLY(totalPercentDiscount, orderRow.getSumWithDiscount());
                        discountSum = Money.DIVIDE(discountSum, new BigDecimal("100"));
                        discountSum = Money.SUBSTRACT(orderRow.getSumWithDiscount(), discountSum);

                        BigDecimal sum = Money.DIVIDE(
                                Money.MULTIPLY(discountSum, salaryPercent),
                                new BigDecimal("100"));

                        String info = "Начисление зарплаты за услугу "
                                + s.getShortName() + " бригаде "
                                + getCrewById(crewId).getName();
                        String analyticsData = "{orderId:" + order.getId() + ", "
                                + "serviceId:" + s.getId() + ", crewId:" + crewId + "}";
                        AccountPost accountPost = AccountBookHandler.getInstance().
                                getAccountBook().accountPosting(
                                accountRule, sum,
                                info, analyticsData,
                                new Date(), dataBase);
                    }
                }

            } else {
                Goods g = (Goods) dataBase.getObject(Goods.class.getName(),
                        orderRow.getSalesItem(dataBase).getDbId());
                Map<Long, BigDecimal> md = (Map<Long, BigDecimal>) g.getAdditionInfo().get("salaryDistribution");
                if (md != null) {
                    Iterator<Long> it = md.keySet().iterator();
                    while (it.hasNext()) {
                        long crewId = it.next();
                        BigDecimal salaryPercent = md.get(crewId);

                        BigDecimal discountSum = Money.MULTIPLY(totalPercentDiscount, orderRow.getSumWithDiscount());
                        discountSum = Money.DIVIDE(discountSum, new BigDecimal("100"));
                        discountSum = Money.SUBSTRACT(orderRow.getSumWithDiscount(), discountSum);

                        BigDecimal sum = Money.DIVIDE(
                                Money.MULTIPLY(discountSum, salaryPercent),
                                new BigDecimal("100"));

                        String info = "Начисление зарплаты за реализацию товара "
                                + g.getShortName() + " бригаде "
                                + getCrewById(crewId).getName();
                        String analyticsData = "{orderId:" + order.getId() + ", "
                                + "goodsId:" + g.getId() + ", crewId:" + crewId + "}";
                        AccountPost accountPost = AccountBookHandler.getInstance().
                                getAccountBook().accountPosting(
                                accountRule, sum,
                                info, analyticsData,
                                new Date(), dataBase);
                    }
                }
            }
        }

        // Закрытие заказа
        accountRuleName = "Закрытие заказа";
        accountRule = AccountBookHandler.getInstance().getAccountRuleByName(accountRuleName);
        AccountPost accountPost = AccountBookHandler.getInstance().
                getAccountBook().accountPosting(
                accountRule, order.getTotalWithTotalDiscount(),
                "Закрытие заказа " + order.getShortInfo(),
                "{orderId:" + order.getId() + "}",
                new Date(), dataBase);
    }

    public Result printXReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = cashMachine.printXReport(password);
        return Result.newResult(!cmr.isError(), "Cashmachine response", cmr);
    }

    public Result printZReport(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = cashMachine.printZReport(password);
        return Result.newResult(!cmr.isError(), "Cashmachine response", cmr);
    }

    public Result cancelReceipt(int password) throws XPathExpressionException, NullPointerException, CashMachineException {
        CashMachineResponse cmr = cashMachine.cancelReceipt(password);
        return Result.newResult(cmr.isError(), cmr.getErrorInfo());
    }

    public Result getReport(ReportHandler reportHandler, boolean isForce) throws ReportException {
        if (isForce) {
            reportHandler.regenerateReport();
        }

        Path pHtml = reportHandler.getHTMLReport();
        Path pPDF = reportHandler.getPDFReport();

        boolean success = true;
        if (pHtml == null || pPDF == null) {
            success = false;
        }

        String infoString = "Path to html report = " + pHtml + ", path to pdf report = " + pPDF;
        Path p[] = new Path[]{pHtml, pPDF};

        Result r = Result.newResult(success, infoString, p);
        return r;
    }

    /*
     * public Result getWorkSummary() { ArrayList<Order> orders =
     * (ArrayList<Order>) getOrdersDone();
     *
     * WorkSummary curWork = new WorkSummary();
     *
     * Iterator<Order> i = orders.iterator(); while (i.hasNext()) { Order order
     * = i.next(); for (OrderRow orderRow : order.getOrderRows()) { SalesItem
     * salesItem = orderRow.getSalesItem(dataBase); if (salesItem instanceof
     * Goods) { //TODO need fill WorkerManager fields curWork.add(order,
     * salesItem, orderRow.getCount().longValue(),
     * orderRow.getSumWithDiscount(), new WorkerManager(null, null)); } else if
     * (salesItem instanceof Service) { Service s = (Service) salesItem;
     * WorkerCrew wcrew = new WorkerCrew(); Crew crew = (Crew)
     * dataBase.getObject(Crew.class.getName(), s.getExecutorCrewId());
     * wcrew.setCrew(dataBase, crew); curWork.add(order, salesItem,
     * orderRow.getCount().longValue(), orderRow.getSumWithDiscount(), wcrew); }
     * } }
     *
     * return Result.newResult(true, "", curWork); }
     *
     */
    /**
     *
     * @return If success will be return Crew
     */
    public Result getDefaultCrew() {
        ArrayList<Crew> list = dataBase.getFilteredResultList(Crew.class.getName(),
                "isDefaultCrew", Condition.newConditionEquial("true"));
        if (list.isEmpty()) {
            return Result.newResultError("Can not find default crew");
        } else {
            return Result.newResultSuccess(list.get(0));
        }
    }

//    //ReportBundle
//    public Result reportShiftPaySalary(ReportShift reportShift, SalaryInfo salaryInfo) {
//        //place SalaryInfo into ReportShift
//        reportShift.setSalaryInfo(salaryInfo);
//        //update pay status
//        reportShift.setSalaryStatus(ReportShift);
//        //set paid date
//        reportShift.setDatePaid(new Date());
//        dataBase.updateObject(reportShift);
//        //move between trees
//        Tree unpaid = (Tree) getTreeSalaryUnpaid().getObject();
//        TreeLeaf tl = unpaid.findTreeLeafWithObject(reportShift);
//        tl.getParent().removeTreeLeaf(tl);
//        Tree paid = (Tree) getTreeSalaryPaid().getObject();
//        paid.addTreeLeaf(tl);
//        return Result.newResult(true, "ok", null);
//    }
    //Trees
    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeBasic
     */
    public Result getTreeGoodsAndService() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "GoodsAndService");
        if (tb == null) {
            tb = new TreeBasic("GoodsAndService", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        } else {
            return Result.newResult(true, "", tb);
        }
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeDateSorted
     */
    public Result getTreeOrdersComplete() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "OrdersComplete");
        if (tb == null) {
            tb = new TreeBasic("OrdersComplete", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        } else {
            return Result.newResult(true, "", tb);
        }
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeBasic
     */
    public Result getTreeOrdersPaid() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "OrdersPayed");
        if (tb == null) {
            tb = new TreeBasic("OrdersComplete", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        } else {
            return Result.newResult(true, "", tb);
        }
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeBasic
     */
    public Result getTreeOrdersInWork() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "OrdersInWork");
        if (tb == null) {
            tb = new TreeBasic("OrdersInWork", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        return Result.newResult(true, "", tb);
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeBasic
     */
    public Result getTreeOrdersDeffered() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "OrdersDeffered");
        if (tb == null) {
            tb = new TreeBasic("OrdersDeffered", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        return Result.newResult(true, "", tb);
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeBasic
     */
    public Result getTreeOrdersUnpayed() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "OrdersUnpayed");
        if (tb == null) {
            tb = new TreeBasic("OrdersUnpayed", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        return Result.newResult(true, "", tb);
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeBasic
     */
    public Result getTreeSalaryUnpaid() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "SalaryUnpaid");
        if (tb == null) {
            tb = new TreeBasic("SalaryUnpaid", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        return Result.newResult(true, "", tb);
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeDateSorted
     */
    public Result getTreeSalaryPaid() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "SalaryPaid");
        if (tb == null) {
            tb = new TreeBasic("SalaryUnpaid", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        return Result.newResult(true, "", tb);
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeBasic
     */
    public Result getTreeStaffPerson() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "StaffPerson");
        if (tb == null) {
            tb = new TreeBasic("StaffPerson", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        return Result.newResult(true, "", tb);
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeBasic
     */
    public Result getTreeStaffPersonWorktime() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "StaffPersonWorktime");
        if (tb == null) {
            tb = new TreeBasic("StaffPersonWorktime", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        return Result.newResult(true, "", tb);
    }

    /**
     *
     * @return Result <br/> If Result is not contains error, then getObject()
     * method will return TreeBasic
     */
    public Result getTreeWarehouses() {
        TreeBasic tb = (TreeBasic) dataBase.getObject(TreeBasic.class.getName(), "getName", "Warehouses");
        if (tb == null) {
            tb = new TreeBasic("StaffPersonWorktime", dataBase);
            if (dataBase.addObject(tb) > 0) {
                return Result.newResult(true, "", tb);
            } else {
                return Result.newStackTraceResultError(Thread.currentThread());
            }
        }

        return Result.newResult(true, "", tb);
    }

    public Result getPrinter() {
        Result r;
        Printer printer = Printer.getInstance(Paths.get(System.getProperty("user.home"),
                ".saas", "app", "config", "system.xml"));
        if (printer == null) {
            r = Result.newStackTraceResultError(
                    "Printer is null", Thread.currentThread());

        } else {
            r = Result.newResult(true, "Printer was finded", printer);
        }

        return r;
    }

    public ArrayList<Crew> getCrewsList() {
        ArrayList<Crew> list = dataBase.getAllObjectsList(Crew.class.getName());
        if (list == null) {
            return new ArrayList<>();

        } else if (!list.isEmpty()) {
            Collections.sort(list, new Comparator<Crew>() {
                @Override
                public int compare(Crew o1, Crew o2) {
                    String s1 = Objects.toString(o1.getName(), "");
                    String s2 = Objects.toString(o2.getName(), "");
                    return s1.compareToIgnoreCase(s2);
                }
            });
        }

        return list;
    }

    /**
     *
     * @param id
     * @return Crew or null
     */
    public Crew getCrewById(long id) {
        return (Crew) dataBase.getObject(Crew.class.getName(), id);
    }

    /**
     *
     * @param name
     * @return Crew or null
     */
    public Crew getCrewByName(String name) {
        Crew crew = null;
        ArrayList<Crew> list = dataBase.getAllObjectsList(Crew.class.getName());
        if (list == null) {
            return null;
        }

        if (list.isEmpty()) {
            return null;
        }

        for (Crew c : list) {
            if (c.getName().equals(name)) {
                crew = c;
                break;
            }
        }
        return crew;
    }

    /**
     *
     * @return Always will be return ArrayList<Employee>
     */
    public ArrayList<Employee> getEmployeeList() {
        ArrayList<Employee> list = dataBase.getAllObjectsList(Employee.class.getName());
        if (list == null) {
            return new ArrayList<>();

        } else if (!list.isEmpty()) {
            Collections.sort(list, new Comparator<Employee>() {
                @Override
                public int compare(Employee o1, Employee o2) {
                    String s1 = Objects.toString(o1.getFullName(), "");
                    if (s1.equals("")) {
                        s1 = Objects.toString(o1.getShortName());
                    }

                    String s2 = Objects.toString(o2.getFullName(), "");
                    if (s1.equals("")) {
                        s1 = Objects.toString(o2.getShortName());
                    }

                    return s1.compareToIgnoreCase(s2);
                }
            });
        }

        return list;
    }

    public ArrayList<Employee> getEmployeeByName(String name) {
        return dataBase.getFilteredResultList(
                Employee.class.getName(),
                "getShortName",
                Condition.newCondition(Condition.EQUAL, name));
    }

    public Result getEmployeeById(long employeeId) {
        Employee employee = (Employee) dataBase.getObject(Employee.class.getName(), employeeId);
        if (employee == null) {
            return Result.newResultError("Can not find Employee with id " + employeeId);
        } else {
            return Result.newResultSuccess(employee);
        }
    }

    public ArrayList<Employee> getAllEmployeeNotAtWork() {
        ArrayList<Employee> listAll = getEmployeeList();
        ArrayList<Employee> listAtWork = getAllEmployeeAtWork();
        ArrayList<Employee> notAtWorkList = new ArrayList<>();

        for (Employee e : listAll) {
            boolean isFind = false;
            if (Objects.toString(e.getRole(), "").equals("owner")) {
                continue;
            }

            for (Employee ee : listAtWork) {
                if (e.getId() == ee.getId()) {
                    isFind = true;
                    continue;
                }
            }

            if (!isFind) {
                notAtWorkList.add(e);
            }
        }

        return notAtWorkList;
    }

    public ArrayList<Employee> getAllEmployeeAtWork() {
        Set<Employee> set = new HashSet<>();
        ArrayList<Crew> _list = dataBase.getAllObjectsList(Crew.class.getName());
        for (Crew crew : _list) {
            for (long id : crew.getIdsEmployees()) {
                Employee e = (Employee) dataBase.getObject(Employee.class.getName(), id);
                set.add(e);
            }
        }

        ArrayList<Employee> list = new ArrayList<>();
        list.addAll(set);

        Collections.sort(list, new Comparator<Employee>() {
            @Override
            public int compare(Employee o1, Employee o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return list;
    }

    public Result depositionMoneyInCashBox(BigDecimal inSum, int password) {
        try {
            CashMachineResponse cmr = cashMachine.depositionMoney(inSum, password);
            return Result.newResult(!cmr.isError(), "Cashmachine response", cmr);

        } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
            return Result.newEmptySuccess();
        }
    }

    public Result outMoneyFromCashBox(BigDecimal outSum, int password) {
        try {
            CashMachineResponse cmr = cashMachine.paymentMoney(outSum, password);
            return Result.newResult(!cmr.isError(), "Cashmachine response", cmr);

        } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
            return Result.newEmptySuccess();

        }
    }

    /**
     *
     * @param password
     * @return If success will retirn BigDecimal sum in cashbox
     */
    public Result getMoneyInCashBox(int password) {
        try {
            BigDecimal sum = cashMachine.getMoneyInCashBox(password);
            return Result.newResult(true, "Success", sum);

        } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
            return Result.newResult(false, "getMoneyInCashBox " + e.getMessage());
        }
    }

    // ########################## LOGED USER #################################
    public void setLoggedUser(String sessionId, User user) {
        if (user == null) {
            return;
        }

        loggedUser.put(sessionId, user);

        String sEmployeeId = Objects.toString(user.getExtraInfoByKey("employeeId"), "0");
        sEmployeeId = sEmployeeId.equals("") ? "0" : sEmployeeId;
        Result r = getEmployeeById(Long.parseLong(sEmployeeId));
        if (!r.isError()) {
            Employee employee = (Employee) r.getObject();
            if (employee.getRole().indexOf("foreman") != -1) {
                r = getDefaultCrew();
                if (!r.isError()) {
                    boolean isFind = false;
                    Crew crew = (Crew) r.getObject();
                    for (long id : crew.getIdsEmployees()) {
                        if (employee.getId() == id) {
                            isFind = true;
                            break;
                        }
                    }

                    if (!isFind) {
                        employee.setBegunWorking(new Date());
                        crew.addEmployee(employee.getId());
                        dataBase.updateObject(crew);
                    }
                }
            }

            if (employee.getRole().indexOf("cashier") != -1) {
                if (cashMachine == null) {
                    CashMachineService service = CashMachineService.getInstance(pathToSystemXML);
                    cashMachine = CashMachineFactory.getInstance().getCashMachine(
                            pathToSystemXML,
                            service.getDefaultCashMachine(),
                            service.getConfigDir(service.getDefaultCashMachine()));
                }
            }
        }
    }

    public User getLoggedUser(String sessionId) {
        return loggedUser.get(sessionId);
    }

    // ########################## USERS, PERMISSIONS, RULES ###################
    public Result isUserExist(String login) {
        ArrayList<User> list = dataBase.getFilteredResultList(
                User.class.getName(),
                "getLogin",
                Condition.newConditionEquial(login));
        if (list.isEmpty()) {
            return Result.newStackTraceResultError(
                    "User with login " + login + " not exist",
                    Thread.currentThread());
        } else {
            return Result.newResult(true, "User with login " + login + " exist", list.get(0));
        }
    }

    public Result addUser(String name, String login, String password, String employeeId) {
        Result result;
        if (name.toLowerCase().equals("admin") || login.toLowerCase().equals("admin")) {
            result = Result.newResultError("You Can not add user with name or login "
                    + "'admin'. This user reserved for system use only.' ");
            return result;
        }

        User user = new User();
        user.setName(name);
        user.setLogin(login);
        user.setPassword(password);
        user.setImageFileName("img/icons/user64.png");
        user.addExtraInfo("employeeId", employeeId);

        if (dataBase.addObject(user) < 0) {
            result = Result.newStackTraceResultError(
                    "Can not add user " + user, Thread.currentThread());

        } else {
            result = initModulesSetForUser(user);
            if (!result.isError()) {
                result = Result.newResult(true, "Success", user);
            }
        }

        return result;
    }

    public Result initModulesSetForUser(User user) {
        Result r;

        Result rRules = getRulesForUser(user.getId());
        Rules rules;
        if (rRules.isError()) {
            rules = new Rules();
            rules.setUserId(user.getId());
            if (dataBase.addObject(rules) < 0) {
                return Result.newStackTraceResultError(
                        "Can not add Rules for user " + user, Thread.currentThread());
            }

        } else {
            rules = (Rules) rRules.getObject();
        }

        for (Plugin plugin : pluginLoader.getFoundPlugins()) {
            Rule rule = new Rule();
            rule.setModuleName(plugin.getPluginName());
            rule.setModuleClassName(plugin.getClass().getName());

            if (plugin instanceof WorkPanelPlugin) {
                rule.setModuleWorkPanelClassName(((WorkPanelPlugin) plugin).getWorkPanelClassName());

                WorkPanel wpp = ((WorkPanelPlugin) plugin).getWorkPanel();
                if (wpp instanceof HasRules) {
                    HasRules hasRules = (HasRules) wpp;
                    ArrayList<RuleItem> list = hasRules.getRuleItemsTemplate(1000);
                    rule.setRuleItems(list);
                }
            }

            if (plugin instanceof RightPanelPlugin) {
                RightPanelPlugin rightPanelPlugin = (RightPanelPlugin) plugin;
                for (RightPanel rightPanel : rightPanelPlugin.getRightPanels()) {
                    if (rightPanel instanceof HasRules) {
                        HasRules hasRules = (HasRules) rightPanel;
                        ArrayList<RuleItem> list = hasRules.getRuleItemsTemplate(1000);
                        boolean isAdded = rule.getRuleItems().addAll(list);
                    }
                }
            }

            rule.setModuleDescription(plugin.getPluginDescription());
            rule.setAllowToUse(false);

            if (rules.addRule(rule)) {
                Logger.getGlobal().log(Level.INFO, "Added rule {0} for user "
                        + "'" + user.getName() + "' {1}", new Object[]{rule, user});
            }
        }

        if (dataBase.updateObject(rules)) {
            user.setRulesId(rules.getId());
            if (dataBase.updateObject(user)) {
                r = Result.newResult(true, "Success", rules);
            } else {
                r = Result.newStackTraceResultError("Can not update user " + user,
                        Thread.currentThread());
            }

        } else {
            r = Result.newStackTraceResultError(
                    "Can not update Rules for user " + user,
                    Thread.currentThread());
        }

        return r;
    }

    public Result modifyUser(String name, String login, String password, long id) {
        Result result;

        if (name.toLowerCase().equals("admin") || login.toLowerCase().equals("admin")) {
            result = Result.newStackTraceResultError(
                    "You Can not add user with name or login "
                    + "'admin'. This user reserved for system use only.' ",
                    Thread.currentThread());
            return result;
        }

        User user = (User) dataBase.getObject(User.class.getName(), id);
        if (user == null) {
            result = Result.newStackTraceResultError(
                    "Can not find user with id = " + id, Thread.currentThread());

        } else {
            user.setLogin(login);
            user.setPassword(password);
            user.setName(name);
            if (dataBase.updateObject(user)) {
                result = Result.newResult(true, "Success", user);

            } else {
                result = Result.newStackTraceResultError(
                        "Can not update user " + user, Thread.currentThread());
            }
        }

        return result;
    }

    public Result removeUser(User user) {
        Result result;

        if (user.getName().equals("admin") || user.getLogin().equals("admin")) {
            result = Result.newStackTraceResultError(
                    "You Can not remove user with name or login "
                    + "'admin'. This user reserved for system use only.' ",
                    Thread.currentThread());
            return result;
        }

        if (dataBase.deleteObject(user)) {
            result = Result.newEmptySuccess();

        } else {
            result = Result.newStackTraceResultError(
                    "Can not delete user " + user, Thread.currentThread());
        }

        return result;
    }

    public Result removeUser(long idUser) {
        Result r = getUser(idUser);
        if (r.isError()) {
            return Result.newStackTraceResultError(
                    "Can not find User with id = " + idUser, Thread.currentThread());
        }

        return removeUser((User) r.getObject());
    }

    public ArrayList<User> getUsers() {
        ArrayList<User> users = dataBase.getAllObjectsList(User.class.getName());
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getLogin().compareToIgnoreCase(o2.getLogin());
            }
        });

        return users;
    }

    public Result getUser(long idUser) {
        User user = (User) dataBase.getObject(User.class.getName(), idUser);

        if (user == null) {
            return Result.newStackTraceResultError(
                    "Can not find user with id = " + idUser, Thread.currentThread());
        } else {
            return Result.newResult(true, "Success", user);
        }
    }

    public Result addAdmin() {
        Result result;
        ArrayList<User> list = dataBase.getFilteredResultList(
                User.class.getName(),
                "getLogin",
                Condition.newConditionEquial("admin"));
        if (list.isEmpty()) {
            User user = new User();
            user.setLogin("admin");
            user.setName("admin");
            user.setPassword("admin");
            user.setImageFileName("img/icons/admin64.png");
            if (dataBase.addObject(user) > 0) {
                result = Result.newResult(true, "Success", user);
            } else {
                result = Result.newStackTraceResultError(
                        "Can not add user to data base", Thread.currentThread());
            }


        } else {
            result = Result.newStackTraceResultError(
                    "User 'admin' was added previously", Thread.currentThread());
        }

        return result;

    }

    public Result getAdmin() {
        Result result;
        ArrayList<User> list = dataBase.getFilteredResultList(
                User.class.getName(),
                "getLogin",
                Condition.newConditionEquial("admin"));
        if (list.isEmpty()) {
            result = Result.newStackTraceResultError(
                    "Can not find user 'admin'", Thread.currentThread());
        } else {
            result = Result.newResult(true, "Success", list.get(0));
        }

        return result;
    }

    public Result updateAdmin() {
        Result r = getAdmin();
        if (r.isError()) {
            r = addAdmin();
            if (r.isError()) {
                return Result.newStackTraceResultError(
                        "Can not update user 'admin'", Thread.currentThread());
            }
        }

        User admin = (User) r.getObject();
        Result rRules = getRulesForUser(admin.getId());
        Rules rules;
        if (rRules.isError()) {
            rules = new Rules();
            rules.setUserId(admin.getId());
            if (dataBase.addObject(rules) < 0) {
                return Result.newStackTraceResultError(
                        "Can not add Rules for user 'admin'", Thread.currentThread());
            }

        } else {
            rules = (Rules) rRules.getObject();
        }

        for (Plugin plugin : pluginLoader.getFoundPlugins()) {
            Rule rule = new Rule();
            rule.setModuleName(plugin.getPluginName());
            rule.setModuleClassName(plugin.getClass().getName());

            if (plugin instanceof WorkPanelPlugin) {
                rule.setModuleWorkPanelClassName(((WorkPanelPlugin) plugin).getWorkPanelClassName());

                WorkPanel wpp = ((WorkPanelPlugin) plugin).getWorkPanel();
                if (wpp instanceof HasRules) {
                    HasRules hasRules = (HasRules) wpp;
                    ArrayList<RuleItem> list = hasRules.getRuleItemsTemplate(0);
                    rule.setRuleItems(list);
                }
            }

            if (plugin instanceof RightPanelPlugin) {
                RightPanelPlugin rightPanelPlugin = (RightPanelPlugin) plugin;
                for (RightPanel rightPanel : rightPanelPlugin.getRightPanels()) {
                    if (rightPanel instanceof HasRules) {
                        HasRules hasRules = (HasRules) rightPanel;
                        ArrayList<RuleItem> list = hasRules.getRuleItemsTemplate(0);
                        boolean isAdded = rule.getRuleItems().addAll(list);
                    }
                }
            }

            rule.setModuleDescription(plugin.getPluginDescription());
            rule.setAllowToUse(true);


            if (rules.addRule(rule)) {
                Logger.getGlobal().log(Level.INFO, "Added rule {0} for user "
                        + "'admin' {1}", new Object[]{rule, admin});
            }
        }

        if (dataBase.updateObject(rules)) {
            admin.setRulesId(rules.getId());
            if (dataBase.updateObject(admin)) {
                r = Result.newResult(true, "Success", rules);
            } else {
                r = Result.newResult(false, "Can not update user 'admin' " + admin);
            }

        } else {
            r = Result.newResultError("Can not update Rules for user 'admin' " + admin);
        }

        return r;
    }

    // #################### USER RULSE ########################################
    /**
     *
     * @param userId
     * @return If success will be return Rules
     */
    public Result getRulesForUser(long userId) {
        Result result;
        ArrayList<Rules> list = dataBase.getFilteredResultList(
                Rules.class.getName(),
                "getUserId",
                Condition.newConditionEquial("" + userId));

        if (list.isEmpty()) {
            result = Result.newStackTraceResultError(
                    "Can not find Rules for user with id " + userId,
                    Thread.currentThread());
        } else {
            result = Result.newResult(true, "Success", list.get(0));
        }

        return result;
    }

    public Result getRulesItemByKey(String sessionId, String key) {
        Result result = getRulesForUser(getLoggedUser(sessionId).getId());
        if (!result.isError()) {
            Rules rules = (Rules) result.getObject();
            for (Rule rule : rules.getRules()) {
                for (RuleItem ruleItem : rule.getRuleItems()) {
                    if (ruleItem.getKey().equals(key)) {
                        return Result.newResult(true, "Success", ruleItem);
                    }
                }
            }

        } else {
            result = Result.newStackTraceResultError(
                    "Can not find RuleItem for key " + key, Thread.currentThread());
        }

        return result;
    }

    public boolean isRadioButtonRuleAllow(String sessionId, String ruleKey, String ruleItemKey) {
        boolean isAllow = false;
        Result result = getRulesItemByKey(sessionId, ruleKey);
        RuleItem ruleItem = (RuleItem) result.getObject();
        for (SelectorRuleItem item : (LinkedList<SelectorRuleItem>) ruleItem.getValue()) {
            if (item.isSelected()) {
                if (item.getKey().equals(ruleItemKey)) {
                    isAllow = true;
                    break;
                }
            }
        }

        return isAllow;
    }

    public Result modifyRules(Rules rules) {
        if (dataBase.updateObject(rules)) {
            return Result.newEmptySuccess();
        } else {
            return Result.newStackTraceResultError(
                    "Can not update rules " + rules, Thread.currentThread());
        }
    }

    public void addUIEventListener(UIEventListener listener) {
        listenerList.add(UIEventListener.class, listener);
    }

    private void fireEvent(UIEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == UIEventListener.class) {
                ((UIEventListener) listeners[i + 1]).event(evt);
            }
        }
    }

    // #################### MODULES ###########################################
    public synchronized PluginLoader getPluginLoader() {
        if (pluginLoader == null) {
            pluginLoader = new PluginLoader();
            pluginLoader.addUIEventListener(new UIEventListener() {
                @Override
                public void event(UIEvent evt) {
                    fireEvent(evt);
                }
            });
            pluginLoader.findPlugins();
        }

        return pluginLoader;
    }

    // ########################### JOURNALS OPERATIONS ########################
    public Result addMoneyRecord(int payType, Object docReasonForPay, String description, Partner partnerFrom,
            Partner partnerTo, BigDecimal sum) {
//        MoneyRecord moneyRecord = MoneyRecord.newMoneyRecord(
//                payType,
//                docReasonForPay,
//                description,
//                partnerFrom,
//                partnerTo,
//                sum, new Date());
//
//        if (dataBase.addObject(moneyRecord) > 0) {
//            return Result.newResult(true, "Success", moneyRecord);
//
//        } else {
//            return Result.newStackTraceResultError(
//                    "Can not add MoneyRecord " + moneyRecord,
//                    Thread.currentThread());
//        }

        return Result.newEmptySuccess();
    }

    public Result addStorageRecord(Goods goods, BigDecimal quantity,
            Storage storageFrom, Storage storageTo,
            Partner partnerFrom, Partner partnerTo) {

//        long storageFromId = storageFrom == null ? -1L : storageFrom.getId();
//        long storageToId = storageTo == null ? -1L : storageTo.getId();
//
//        StorageRecord storageRecord = StorageRecord.newStorageRecord(
//                goods,
//                quantity,
//                storageFromId,
//                storageToId,
//                partnerFrom,
//                partnerTo, new Date());
//
//        if (dataBase.addObject(storageRecord) > 0) {
//            return Result.newResult(true, "Success", storageRecord);
//
//        } else {
//            return Result.newStackTraceResultError(
//                    "Can not add StorageRecord " + storageRecord,
//                    Thread.currentThread());
//        }

        return Result.newEmptySuccess();
    }

    public Result addLogRecord(String message, LogRecordLevel level) {
//        LogRecord logRecord = LogRecord.newLogRecord(message, new Date(), level);
//
//        if (dataBase.addObject(logRecord) > 0) {
//            return Result.newResult(true, "Success", logRecord);
//
//        } else {
//            return Result.newStackTraceResultError(
//                    "Can not add LogRecord " + logRecord, Thread.currentThread());
//        }

        return Result.newEmptySuccess();
    }

    /**
     *
     * @return If all right will be return success
     */
    public synchronized Result moveAllOrderInWorkToComplite(final String session) {
        ArrayList<Order> list = new ArrayList<>();
        ArrayList<Order> orders = dataBase.getAllObjectsList(Order.class.getName());
        for (Order order : orders) {
            if (order.getWorkStatus() == Order.WORK_INWORK) {
                list.add(order);
            }
        }

        double p = 100d / (double) list.size();
        double counter = 0;
        for (Order order : list) {
            double val = p * counter;
            String msg = "<span style='font-weight:bolder; font-size:2505'>"
                    + "Пожалуйста подождите, выполнено: "
                    + ((int) val)
                    + "%</span>";
            JSMediator.setLockPanelProgressBar(session, msg);
            closeOrder(order);
            counter++;
        }
        JSMediator.setLockPanelProgressBar(session, "");

        return Result.newEmptySuccess();
    }

    public BigDecimal getLastPriceIn(Goods goods) {
        BigDecimal result = BigDecimal.ZERO;
        Date checkDate = DateTime.getDateFromString("yyyy-MM-dd", "1970-01-01");
        ArrayList<GoodsAnalytics> list = dataBase.getAllObjectsList(GoodsAnalytics.class.getName());
        for (GoodsAnalytics goodsAnalytics : list) {
            if (goodsAnalytics.getGoodsId() != goods.getId()) {
                continue;
            }

            if (goodsAnalytics.getDate().getTime() > checkDate.getTime()) {
                if (goodsAnalytics.getQuantity().doubleValue() != 0) {
                    result = Money.DIVIDE(goodsAnalytics.getSumIn().toString(), goodsAnalytics.getQuantity().toString());
                }
            }
        }

        return result;
    }

    public ArrayList<ReportShift> getReportShifts(int status) {
        ArrayList<ReportShift> list = null;
        try {
            list = dataBase.getFilteredResultList(
                    ReportShift.class.getName(),
                    "getSalaryStatus",
                    Condition.newConditionEquial("" + status));

            Collections.sort(list, new Comparator<ReportShift>() {
                @Override
                public int compare(ReportShift o1, ReportShift o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });

        } finally {
            if (list == null) {
                list = new ArrayList<>();
            }

            return list;
        }
    }

    public ArrayList<ReportShift> getNotPaidProfitReportShifts() {
        ArrayList<ReportShift> result = new ArrayList<>();
        ArrayList<ReportShift> shifts = dataBase.getAllObjectsList(ReportShift.class.getName());
        for (ReportShift shift : shifts) {
            if ((shift.getOwnerProfit().doubleValue() - shift.getTotalGotOwnerProfit().doubleValue()) > 0) {
                result.add(shift);
            }
        }

        return result;
    }

    public ArrayList<ReportShift> getPaidProfitReportShifts() {
        ArrayList<ReportShift> result = new ArrayList<>();
        ArrayList<ReportShift> shifts = dataBase.getAllObjectsList(ReportShift.class.getName());
        for (ReportShift shift : shifts) {
            if ((shift.getOwnerProfit().doubleValue() - shift.getTotalGotOwnerProfit().doubleValue()) <= 0) {
                result.add(shift);
            }
        }

        return result;
    }

    public BigDecimal getSalarySumForReportShift(long reportShiftId) {
        BigDecimal sum = BigDecimal.ZERO;
        ReportShift reportShift = (ReportShift) dataBase.getObject(
                ReportShift.class.getName(), reportShiftId);
        if (reportShift != null) {
            reportShift.clearSalarySum(); // !!!!!!!!!!!!!!!!!!!!!!!!
            sum = reportShift.getSalarySum(dataBase);
        }

        return sum;
    }

    /**
     *
     * @param reportShiftId
     * @return If success will be return ReportShift in Result container
     * (getObject())
     */
    public Result getReportShift(long reportShiftId) {
        ReportShift reportShift = (ReportShift) dataBase.getObject(ReportShift.class.getName(), reportShiftId);
        if (reportShift == null) {
            return Result.newResultError("Can not find ReportShift with id = " + reportShiftId);
        } else {
            return Result.newResultSuccess(reportShift);
        }
    }

    public Result paidSalary(ReportShift reportShift, boolean isSinglePay, int cashMasterPassword) {
        AccountRule accountRuleSalaryPaid = AccountBookHandler.getInstance().getAccountRuleByName("Выплата зарплаты");
        AccountRule accountRuleRetained = AccountBookHandler.getInstance().getAccountRuleByName("Штраф");
        AccountRule accountRuleWithdrawal = AccountBookHandler.getInstance().getAccountRuleByName("Начисление зарплаты");

        if (isSinglePay) {
            try {
                BigDecimal sumToPay = Money.ADD(
                        reportShift.getSalarySum(dataBase).toString(),
                        reportShift.getWithdrawal().toString());
                CashMachineResponse cmr = cashMachine.paymentMoney(
                        sumToPay, cashMasterPassword);
                if (cmr.isError()) {
                    return Result.newResult(true, "cashMachineResponse", cmr);
                }

                reportShift.setSalaryStatus(ReportShift.SALARY_DISTRIBUTED_PAID);
                reportShift.setDatePaid(new Date());
                for (Employee employee : reportShift.getEmployeeSet()) {
                    BigDecimal salary = Money.ADD(
                            employee.getAccruedWages().toString(),
                            employee.getRetained().toString());

                    Map<BigDecimal, Date> m = new HashMap<>();
                    m.put(salary, new Date());
                    employee.addGotSalary(m);

                    AccountBookHandler.getInstance().getAccountBook().accountPosting(
                            accountRuleSalaryPaid,
                            salary,
                            "Выплата з/п " + employee.getName(),
                            "{reportShiftId:" + reportShift.getId() + ", employeeId:" + employee.getId() + "}",
                            new Date(),
                            dataBase);

                    if (employee.getRetained().doubleValue() > 0) {
                        AccountBookHandler.getInstance().getAccountBook().accountPosting(
                                accountRuleRetained,
                                employee.getRetained(),
                                "Удержание из з/п " + employee.getName(),
                                "{reportShiftId:" + reportShift.getId()
                                + ", employeeId:" + employee.getId() + "}",
                                new Date(),
                                dataBase);
                    }
                }
            } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
                return Result.newResultError(e.toString());
            }

        } else {
            try {
                reportShift.setSalaryStatus(ReportShift.SALARY_DISTRIBUTED_PAID);
                reportShift.setDatePaid(new Date());
                for (Employee employee : reportShift.getEmployeeSet()) {
                    BigDecimal salary = Money.ADD(
                            employee.getAccruedWages().toString(),
                            employee.getRetained().toString());

                    CashMachineResponse cmr = cashMachine.paymentMoney(
                            salary, cashMasterPassword);
                    if (cmr.isError()) {
                        return Result.newResult(true, "cashMachineResponse", cmr);
                    }

                    Map<BigDecimal, Date> m = new HashMap<>();
                    m.put(salary, new Date());
                    employee.addGotSalary(m);

                    String employeeName = Objects.toString(employee.getFullName(), "");
                    employeeName = employeeName.equals("") ? Objects.toString(employee.getShortName(), "") : employeeName;

                    AccountBookHandler.getInstance().getAccountBook().accountPosting(
                            accountRuleSalaryPaid,
                            salary,
                            "Выплата з/п " + employeeName,
                            "{reportShiftId:" + reportShift.getId()
                            + ", employeeId:" + employee.getId() + "}",
                            new Date(),
                            dataBase);

                    if (employee.getRetained().doubleValue() > 0) {
                        AccountBookHandler.getInstance().getAccountBook().accountPosting(
                                accountRuleRetained,
                                employee.getRetained(),
                                "Удержание из з/п " + employee.getName(),
                                "{reportShiftId:" + reportShift.getId()
                                + ", employeeId:" + employee.getId() + "}",
                                new Date(),
                                dataBase);
                    }
                }
            } catch (XPathExpressionException | NullPointerException | CashMachineException e) {
                return Result.newResultError(e.toString());
            }
        }

        if (reportShift.getWithdrawal().doubleValue() != 0) {
            if (reportShift.getWithdrawal().doubleValue() > 0) {
                AccountBookHandler.getInstance().getAccountBook().accountPosting(
                        accountRuleWithdrawal,
                        reportShift.getWithdrawal(),
                        "Премия для всей смены",
                        "{reportShiftId:" + reportShift.getId() + "}",
                        new Date(),
                        dataBase);

            } else {
                String w = Money.formatToMoney(Math.abs(reportShift.getWithdrawal().doubleValue()));
                AccountBookHandler.getInstance().getAccountBook().accountPosting(
                        accountRuleRetained,
                        new BigDecimal(w),
                        "Удержание из з/п для всей смены",
                        "{reportShiftId:" + reportShift.getId() + "}",
                        new Date(),
                        dataBase);
            }
        }

        dataBase.updateObject(reportShift);
        return Result.newEmptySuccess();
    }

    public Result modifyReportShift(ReportShift reportShift) {
        if (dataBase.updateObject(reportShift)) {
            return Result.newEmptySuccess();
        } else {
            return Result.newStackTraceResultError(Thread.currentThread());
        }
    }

    public Map<Long, BigDecimal> getSalaryForCrews(String session, ReportShift reportShift) {
        if (!reportShift.getSalaryForCrews().isEmpty()) {
            return reportShift.getSalaryForCrews();
        }

        Map<Long, BigDecimal> map = new HashMap<>();
        ArrayList<org.ubo.accountbook.Account> list = AccountBookHandler.getInstance().getAccounts("70");

        Set<Order> orders = reportShift.getOrderSet();
        double p = 100d / (double) orders.size();
        double counter = 0;
        for (Order order : orders) {
            double val = p * counter;
            String msg = "<span style='font-weight:bolder; font-size:2505'>"
                    + "Построение отчета, выполнено: "
                    + ((int) val)
                    + "%</span>";
            JSMediator.setLockPanelProgressBar(session, msg);

            for (org.ubo.accountbook.Account account : list) {
                try {
                    JSONObject json = new JSONObject(account.getAnalyticsData());
                    if (!json.has("orderId")) {
                        continue;
                    }

                    if (!json.has("crewId")) {
                        continue;
                    }

                    if (account.getDescription().indexOf("бригаде") == -1) {
                        continue;
                    }

                    if (order.getId() == json.getLong("orderId")) {
                        if (map.containsKey(json.getLong("crewId"))) {
                            BigDecimal sum = Money.ADD(
                                    map.get(json.getLong("crewId")).toString(),
                                    account.getValue().toString());
                            map.put(json.getLong("crewId"), sum);
                        } else {
                            map.put(json.getLong("crewId"), account.getValue());
                        }
                    }
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }

            counter++;
        }

        reportShift.setSalaryForCrews(map);
        dataBase.updateObject(reportShift);

        return map;
    }

    public ArrayList<Page> getWWWPages() {
        ArrayList<Page> pages = dataBase.getAllObjectsList(Page.class.getName());
        if (pages == null) {
            return new ArrayList<>();
        } else {
            return pages;
        }
    }

    public String getWWWPage(String path) {
        String content = "Empty";

        ArrayList<Page> pages = dataBase.getAllObjectsList(Page.class.getName());
        if (pages == null) {
            return content;

        } else {
            for (Page page : pages) {
                if (page.getPath().equals(path)) {
                    return page.getContent();
                }
            }
        }

        return content;
    }

    public boolean isPageExist(String path) {
        ArrayList<Page> pages = dataBase.getAllObjectsList(Page.class.getName());
        if (pages == null) {
            return false;

        } else {
            for (Page page : pages) {
                if (page.getPath().equals(path)) {
                    return true;
                }
            }
        }

        return false;
    }

    public Result addWWWPage(String path, String content) {
        if (!getWWWPage(path).equals("Empty")) {
            return Result.newResult(false, "Page alredy exist");
        }

        Page page = new Page();
        page.setPath(path);
        page.setContent(content);

        if (dataBase.addObject(page) > 0) {
            return Result.newResultSuccess(page);
        } else {
            return Result.newResultError("Can not add page " + page);
        }
    }

    public Result modifyWWWPage(String path, String content) {
        ArrayList<Page> pages = dataBase.getAllObjectsList(Page.class.getName());
        if (pages == null) {
            return Result.newResultError("Page with path " + path + " not finded");

        } else {
            for (Page page : pages) {
                if (page.getPath().equals(path)) {
                    page.setContent(content);
                    if (dataBase.updateObject(page)) {
                        return Result.newResultSuccess(page);
                    } else {
                        return Result.newResultError("Can not update page " + page + " with path " + path);
                    }
                }
            }
        }
        return Result.newResultError("Page with path " + path + " not finded");
    }

    public Result getPriceACL(long treeLeafId) {
        ArrayList<PriceACL> list = dataBase.getAllObjectsList(PriceACL.class.getName());
        for (PriceACL priceACL : list) {
            if (priceACL.getTreeLeafId() == treeLeafId) {
                return Result.newResultSuccess(priceACL);
            }
        }
        return Result.newResultError("Can not find PriceACL with treeLeafId = " + treeLeafId);
    }

    public Result setPriceACLShow(long treeLeafId, boolean isAllowToShow) {
        Result r = getPriceACL(treeLeafId);
        if (r.isError()) {
            PriceACL priceACL = new PriceACL();
            priceACL.setTreeLeafId(treeLeafId);
            priceACL.setAllowShow(isAllowToShow);
            if (dataBase.addObject(priceACL) > 0) {
                return Result.newResultSuccess(priceACL);
            } else {
                return Result.newResultError("Can not add PriceACL = " + priceACL);
            }

        } else {
            PriceACL priceACL = (PriceACL) r.getObject();
            priceACL.setAllowShow(isAllowToShow);
            if (dataBase.updateObject(priceACL)) {
                return Result.newResultSuccess(priceACL);
            } else {
                return Result.newResultError("Can not update PriceACL with treeLeafId = " + treeLeafId);
            }
        }
    }

    public Result setPriceACLUse(long treeLeafId, boolean isAllowToUse) {
        Result r = getPriceACL(treeLeafId);
        if (r.isError()) {
            PriceACL priceACL = new PriceACL();
            priceACL.setTreeLeafId(treeLeafId);
            priceACL.setAllowUse(isAllowToUse);
            if (dataBase.addObject(priceACL) > 0) {
                return Result.newResultSuccess(priceACL);
            } else {
                return Result.newResultError("Can not add PriceACL = " + priceACL);
            }

        } else {
            PriceACL priceACL = (PriceACL) r.getObject();
            priceACL.setAllowUse(isAllowToUse);
            if (dataBase.updateObject(priceACL)) {
                return Result.newResultSuccess(priceACL);
            } else {
                return Result.newResultError("Can not update PriceACL with treeLeafId = " + treeLeafId);
            }
        }
    }

    public Result getExchangeSettings() {
        ArrayList<ExchangeSettings> list = dataBase.getAllObjectsList(ExchangeSettings.class.getName());
        if (list == null) {
            return Result.newResultError("Can not find ExchangeSettings");
        } else if (list.isEmpty()) {
            return Result.newResultError("Can not find ExchangeSettings");
        } else {
            return Result.newResultSuccess(list.get(0));
        }
    }

    public Result modifyExchangeSettings(ExchangeSettings exchangeSettings) {
        Result r = getExchangeSettings();
        if (r.isError()) {
            if (dataBase.addObject(exchangeSettings) > 0) {
                return Result.newResultSuccess(exchangeSettings);
            } else {
                return Result.newResultError("Can not modify ExchangeSettings " + exchangeSettings);
            }

        } else {
            ExchangeSettings _exchangeSettings = (ExchangeSettings) r.getObject();
            _exchangeSettings.setEnabled(exchangeSettings.isEnabled());
            _exchangeSettings.setExchangeTimeOut(exchangeSettings.getExchangeTimeOut());
            _exchangeSettings.setRemoteHost(exchangeSettings.getRemoteHost());
            _exchangeSettings.setLogin(exchangeSettings.getLogin());
            _exchangeSettings.setPassword(exchangeSettings.getPassword());
            if (dataBase.updateObject(_exchangeSettings)) {
                return Result.newResultSuccess(_exchangeSettings);
            } else {
                return Result.newResultError("Can not modify ExchangeSettings " + exchangeSettings);
            }
        }
    }

    public Result addWWWOrders(ArrayList<Order> remoteOrders) {
        System.out.println(">>>>>>>>>>>>>>> " + remoteOrders);
        ArrayList<String> resultInfo = new ArrayList<>();
        ArrayList<Order> localOrders = dataBase.getAllObjectsList(Order.class.getName());
        Collections.sort(localOrders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return Long.valueOf(o1.getRemoteId()).compareTo(Long.valueOf(o2.getRemoteId()));
            }
        });

        for (Order remoteOrder : remoteOrders) {
            int pos = Collections.binarySearch(localOrders, remoteOrder, new Comparator<Order>() {
                @Override
                public int compare(Order o1, Order o2) {
                    return Long.valueOf(o1.getRemoteId()).compareTo(Long.valueOf(o2.getRemoteId()));
                }
            });

            if (pos < 0) {
                remoteOrder.setSyncStatus(Order.SYNC_CONFIRMED);
                remoteOrder.setWorkStatus(Order.WORK_DEFERRED);
                if (dataBase.addObject(remoteOrder) < 0) {
                    return Result.newResultError("Can not add remote Order " + remoteOrder);
                }
            }
        }
        return Result.newResult(true, resultInfo.toString());
    }

    public ArrayList<Order> getWWWOrdersConfirmed() {
        ArrayList<Order> orders = dataBase.getFilteredResultList(
                Order.class.getName(),
                "getSyncStatus",
                Condition.newConditionEquial("" + Order.SYNC_CONFIRMED));

        if (orders == null) {
            orders = new ArrayList<>();
        }

        return orders;
    }

    public ArrayList<Order> getOrdersByWorkStatus(int workStatus) {
        ArrayList<Order> resultSet = new ArrayList<>();
        ArrayList<Order> list = dataBase.getAllObjectsList(Order.class.getName());
        if (list == null) {
            return resultSet;
        }

        for (Order order : list) {
            if (order.getWorkStatus() == workStatus) {
                resultSet.add(order);
            }
        }

        return resultSet;
    }

    public ArrayList<AnalyticFilter> getAnalyticFilters() {
        return dataBase.getAllObjectsList(AnalyticFilter.class.getName());
    }

    public ArrayList<AnalyticFilter> getAnalyticFilters(String account) {
        if (account == null) {
            return new ArrayList<>();
        }

        ArrayList<AnalyticFilter> list = dataBase.getAllObjectsList(AnalyticFilter.class.getName());
        ArrayList<AnalyticFilter> resultSet = new ArrayList<>();
        for (AnalyticFilter analyticFilter : list) {
            if (!analyticFilter.getAccount().equals(account.trim())) {
                continue;
            }

            resultSet.add(analyticFilter);
        }
        return resultSet;
    }

    public Result addAnalyticFilter(AnalyticFilter analyticFilter) {
        if (dataBase.addObject(analyticFilter) > 0) {
            return Result.newEmptySuccess();

        } else {
            return Result.newResultError("Can not add AnalyticFilter " + analyticFilter + " to database");
        }
    }

    public Result removeAnalyticFilter(AnalyticFilter analyticFilter) {
        if (dataBase.deleteObject(analyticFilter)) {
            return Result.newEmptySuccess();

        } else {
            return Result.newResult(false, "Can not remove AnalyticFilter " + analyticFilter);
        }
    }

    public Result modifyAnalyticFilter(long id, AnalyticFilter analyticFilter) {
        if (dataBase.updateObject(id, analyticFilter)) {
            return Result.newEmptySuccess();
        } else {
            return Result.newResult(false, "Can not update AnalyticFilter " + analyticFilter);
        }
    }

    public ArrayList<OrderRow> getServiceRows(long orderId) {
        Result r = getOrder(orderId);
        if (r.isError()) {
            return new ArrayList<>();

        } else {
            ArrayList<OrderRow> resultSet = new ArrayList<>();
            Order order = (Order) r.getObject();
            for (OrderRow orderRow : order.getOrderRows()) {
                if (orderRow.getSalesItem(dataBase).getType() == SalesItem.GOODS) {
                    continue;
                }

                resultSet.add(orderRow);
            }

            return resultSet;
        }
    }

    public ArrayList<OrderRow> getGoodsRows(long orderId) {
        Result r = getOrder(orderId);
        if (r.isError()) {
            return new ArrayList<>();

        } else {
            ArrayList<OrderRow> resultSet = new ArrayList<>();
            Order order = (Order) r.getObject();
            for (OrderRow orderRow : order.getOrderRows()) {
                if (orderRow.getSalesItem(dataBase).getType() == SalesItem.SERVICE) {
                    continue;
                }

                resultSet.add(orderRow);
            }

            return resultSet;
        }
    }

    public DataBase getDataBase() {
        return dataBase;
    }

    public Result setServiceSalaryDistribution(TreeFolderBasic treeFolder, Map<String, Object> m) {
        Set<TreeLeaf> set = treeFolder.getSetTreeLeaf();
        for (TreeLeaf treeLeaf : set) {
            Object obj = dataBase.getObject(treeLeaf.getContainer().getClassName(),
                    treeLeaf.getContainer().getId());
            if (obj == null) {
                continue;
            }

            if (obj instanceof Service) {
                Service service = (Service) obj;
                service.setAdditionInfo(m);
                if (!dataBase.updateObject(service)) {
                    return Result.newResultError("Can not update Service " + service);
                }
            }
        }

        return Result.newEmptySuccess();
    }

    public Result setGoodsSalaryDistribution(TreeFolderBasic treeFolder, Map<String, Object> m) {
        Set<TreeLeaf> set = treeFolder.getSetTreeLeaf();
        for (TreeLeaf treeLeaf : set) {
            Object obj = dataBase.getObject(treeLeaf.getContainer().getClassName(),
                    treeLeaf.getContainer().getId());
            if (obj == null) {
                continue;
            }

            if (obj instanceof Goods) {
                Goods goods = (Goods) obj;
                goods.setAdditionInfo(m);
                if (!dataBase.updateObject(goods)) {
                    return Result.newResultError("Can not update Goods " + goods);
                }
            }
        }

        return Result.newEmptySuccess();
    }
}
