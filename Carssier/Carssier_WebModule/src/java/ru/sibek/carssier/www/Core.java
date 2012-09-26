/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.carssier.www;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.document.Order;
import org.ubo.document.ServiceCommandment;
import org.ubo.goods.Goods;
import org.ubo.partner.Agent;
import org.ubo.partner.Contacts;
import org.ubo.service.Service;
import org.ubo.tree.TreeBasic;
import org.ubo.tree.TreeFolderBasic;
import org.ubo.tree.TreeLeafBasic;
import org.ubo.utils.Result;
import org.ubo.www.Page;
import org.ubo.www.PriceACL;
import org.uui.db.DataBase;

/**
 *
 * @author developer
 */
public class Core {

    private ConcurrentHashMap<String, DataBase> dataBases = new ConcurrentHashMap<>();
    private static Core self = null;

    private Core() {
        //
    }

    public synchronized static Core getInstance() {
        if (self == null) {
            self = new Core();
        }

        return self;
    }
    
    public ArrayList<Order> getOrdersForAgent(Path rootPath, String login, Agent agent){
        ArrayList<Order> orders = new ArrayList<>();
        
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return orders;
        }
        
        ArrayList<Order> list = db.getAllObjectsList(Order.class.getName());
        if(orders == null){
            return new ArrayList<>();
        } else {
            for(Order order : list){
                if(order.getCustomer() == null){
                    continue;
                }
                
                if(order.getCustomer().getAdditionInfoByKey("login") == null){
                    continue;
                }
                String agentLogin = "" + order.getCustomer().getAdditionInfoByKey("login");
                if(agentLogin.equals(agent.getAdditionInfoByKey("login"))){
                    orders.add(order);
                }
            }
            return orders;
        }
    }
    
    public Result modifyPartner(Path rootPath, String login, Agent agent){
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return Result.newResultError("Can't find DataBase for login " + login);
        }
        
        if(db.updateObject(agent)){
            return Result.newResultSuccess(agent);
        } else {
            return Result.newResultError("Can't update Partner " + agent);
        }
    }
    
    public Agent getAgent(Path rootPath, String login, String userLogin, String userPassword){
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return null;
        }
        
        ArrayList<Agent> agents = db.getAllObjectsList(Agent.class.getName());
        if(agents == null){
            agents = new ArrayList<>();
        }
        
        for(Agent agent : agents){
            if(("" + agent.getAdditionInfoByKey("login")).equals(userLogin)
                    && ("" + agent.getAdditionInfoByKey("password")).equals(userPassword)){
                return agent;
            }
        }
        
        return null;
    }
    
    public Result addAgent(Path rootPath, String login, Map<String, String> userInfo){
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return Result.newResultError("Can't find DataBase for login " + login);
        }
        
        ArrayList<Agent> agents = db.getAllObjectsList(Agent.class.getName());
        if(agents == null){
            agents = new ArrayList<>();
        }
        
        for(Agent agent : agents){
            if(("" + agent.getAdditionInfoByKey("login")).equals(userInfo.get("login"))){
                return Result.newResultError("login already used");
            }
        }
        
        Agent newAgent = new Agent();
        newAgent.setAdditionInfo("login", userInfo.get("login"));
        newAgent.setAdditionInfo("password", userInfo.get("password"));
        newAgent.setShortName(userInfo.get("name"));
        Contacts contacts = new Contacts();
        contacts.setDefaultContactPerson(userInfo.get("name"));
        contacts.setDefaultEmail(userInfo.get("email"));
        contacts.setDefaultPhone(userInfo.get("phone"));
        newAgent.setDefaultContacts(contacts);
        
        if(db.addObject(newAgent) > 0){
            return Result.newResultSuccess(newAgent);
        } else {
            return Result.newResultError("Can't add Partner " + newAgent);
        }
    }

    public void syncServiceCommandment(Path rootPath, String login,
            ArrayList<ServiceCommandment> serviceCommandments) {
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return;
        }

        Path p = Paths.get(db.getDataBaseProperties().getPathToDB(), ServiceCommandment.class.getName());
        if (!p.toFile().exists()) {
            p.toFile().mkdir();
        }

        for (ServiceCommandment serviceCommandment : serviceCommandments) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream ous = new ObjectOutputStream(baos);
                ous.writeObject(serviceCommandment);
                ous.flush();
                ous.close();
                baos.close();

                Files.write(p.resolve("" + serviceCommandment.getId()), baos.toByteArray());

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
    }

    public void syncGoods(Path rootPath, String login, ArrayList<Goods> goodses) {
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return;
        }

        Path p = Paths.get(db.getDataBaseProperties().getPathToDB(), Goods.class.getName());
        if (!p.toFile().exists()) {
            p.toFile().mkdir();
        }

        for (Goods goods : goodses) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream ous = new ObjectOutputStream(baos);
                ous.writeObject(goods);
                ous.flush();
                ous.close();
                baos.close();

                Files.write(p.resolve("" + goods.getId()), baos.toByteArray());

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
    }

    public void syncService(Path rootPath, String login, ArrayList<Service> services) {
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return;
        }

        Path p = Paths.get(db.getDataBaseProperties().getPathToDB(), Service.class.getName());
        if (!p.toFile().exists()) {
            p.toFile().mkdir();
        }

        for (Service service : services) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream ous = new ObjectOutputStream(baos);
                ous.writeObject(service);
                ous.flush();
                ous.close();
                baos.close();

                Files.write(p.resolve("" + service.getId()), baos.toByteArray());

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
    }

    public void syncTreeLeafBasic(Path rootPath, String login, ArrayList<TreeLeafBasic> treeLeafs) {
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return;
        }

        Path p = Paths.get(db.getDataBaseProperties().getPathToDB(), TreeLeafBasic.class.getName());
        if (!p.toFile().exists()) {
            p.toFile().mkdir();
        }

        for (TreeLeafBasic treeLeafBasic : treeLeafs) {
            treeLeafBasic.setDataBase(db);

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream ous = new ObjectOutputStream(baos);
                ous.writeObject(treeLeafBasic);
                ous.flush();
                ous.close();
                baos.close();

                Files.write(p.resolve("" + treeLeafBasic.getId()), baos.toByteArray());

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
    }

    public void syncWWWPages(Path rootPath, String login, ArrayList<Page> pages) {
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return;
        }

        Path p = Paths.get(db.getDataBaseProperties().getPathToDB(), Page.class.getName());
        if (!p.toFile().exists()) {
            p.toFile().mkdir();
        }

        for (Page page : pages) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream ous = new ObjectOutputStream(baos);
                ous.writeObject(page);
                ous.flush();
                ous.close();
                baos.close();

                Files.write(p.resolve("" + page.getId()), baos.toByteArray());

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
    }

    public void syncTreeFolderBasic(Path rootPath, String login, ArrayList<TreeFolderBasic> treeFolders) {
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return;
        }

        Path p = Paths.get(db.getDataBaseProperties().getPathToDB(), TreeFolderBasic.class.getName());
        if (!p.toFile().exists()) {
            p.toFile().mkdir();
        }

        for (TreeFolderBasic treeFolderBasic : treeFolders) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream ous = new ObjectOutputStream(baos);
                ous.writeObject(treeFolderBasic);
                ous.flush();
                ous.close();
                baos.close();

                Files.write(p.resolve("" + treeFolderBasic.getId()), baos.toByteArray());

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
    }

    public void syncTreeBasic(Path rootPath, String login, TreeBasic treeBasic) {
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return;
        }

        Path p = Paths.get(db.getDataBaseProperties().getPathToDB(), TreeBasic.class.getName());
        if (!p.toFile().exists()) {
            p.toFile().mkdir();
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream ous = new ObjectOutputStream(baos);
            ous.writeObject(treeBasic);
            ous.flush();
            ous.close();
            baos.close();

            Files.write(p.resolve("" + treeBasic.getId()), baos.toByteArray());
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    public void syncPageACL(Path rootPath, String login, ArrayList<PriceACL> data) {
        DataBase db = getDataBase(rootPath, login);
        if (db == null) {
            Logger.getGlobal().log(Level.WARNING, "DataBase for login {0} is null", login);
            return;
        }

        Path p = Paths.get(db.getDataBaseProperties().getPathToDB(), PriceACL.class.getName());
        if (!p.toFile().exists()) {
            p.toFile().mkdir();
        }

        for (PriceACL priceACL : data) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream ous = new ObjectOutputStream(baos);
                ous.writeObject(priceACL);
                ous.flush();
                ous.close();
                baos.close();

                Files.write(p.resolve("" + priceACL.getId()), baos.toByteArray());

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
    }

    public DataBase getDataBase(Path rootPath, String login) {
        DataBase db;
        if (dataBases.get(login) == null) {
            Path p = rootPath.resolve(login + "/config/db.properties");
            if (!p.toFile().exists()) {
                return null;
            }
            db = DataBase.getInstance(p.toString());
            db.init();
            dataBases.put(login, db);
            
        } else {
            db = dataBases.get(login);
        }

        return db;
    }
    
    public Result getPriceACL(Path rootPath, String login){
        ArrayList<PriceACL> prices;
        
        DataBase dataBase = getDataBase(rootPath, login);
        if (dataBase == null) {
            return Result.newResultError("DataBase is null");
        }
        
        prices = dataBase.getAllObjectsList(PriceACL.class.getName());
        if(prices == null){
            prices = new ArrayList<>();
        }
        
        return Result.newResultSuccess(prices);
    }

    public Result getTreeGoodsAndService(Path rootPath, String login) {
        DataBase dataBase = getDataBase(rootPath, login);
        if (dataBase == null) {
            return Result.newResultError("DataBase is null");
        }

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

    public Result getGoods(Path rootPath, String login, long goodsId) {
        DataBase dataBase = getDataBase(rootPath, login);
        if (dataBase == null) {
            return Result.newResultError("DataBase is null");
        }

        Goods goods = (Goods) dataBase.getObject(Goods.class.getName(), goodsId);
        if (goods == null) {
            return Result.newStackTraceResultError(
                    "Can't find goods with id " + goodsId,
                    Thread.currentThread());
        } else {
            return Result.newResultSuccess(goods);
        }
    }

    public Result getService(Path rootPath, String login, long serviceId) {
        DataBase dataBase = getDataBase(rootPath, login);
        if (dataBase == null) {
            return Result.newResultError("DataBase is null");
        }

        Service service = (Service) dataBase.getObject(Service.class.getName(), serviceId);
        if (service == null) {
            return Result.newStackTraceResultError(
                    "Can't find Service with id = " + serviceId,
                    Thread.currentThread());
        } else {
            return Result.newResultSuccess(service);
        }
    }

    public Result addOrder(Path rootPath, String login, Order order) {
        DataBase dataBase = getDataBase(rootPath, login);
        if (dataBase == null) {
            return Result.newResultError("DataBase is null");
        }

        if (dataBase.addObject(order) > 0) {
            order.setRemoteId(order.getId());
            if (dataBase.updateObject(order)) {
                return Result.newResultSuccess(order);
            } else {
                return Result.newResultError("Can't modify Order " + order + " when try add it");
            }

        } else {
            return Result.newResultError("Can't add Order " + order);
        }
    }
    
    public ArrayList<Order> getOrdersByRemoteId(Path rootPath, String login, ArrayList<Order> remoteOrders){
        ArrayList<Order> resultSet = new ArrayList<>();
        
        DataBase dataBase = getDataBase(rootPath, login);
        if (dataBase == null) {
            return resultSet;
        }
        
        Collections.sort(remoteOrders, new Comparator<Order>() {

            @Override
            public int compare(Order o1, Order o2) {
                return Long.valueOf(o1.getRemoteId()).compareTo(Long.valueOf(o2.getRemoteId()));
            }
        });
        
        ArrayList<Order> localOrders = dataBase.getAllObjectsList(Order.class.getName());
        Collections.sort(localOrders, new Comparator<Order>() {

            @Override
            public int compare(Order o1, Order o2) {
                return Long.valueOf(o1.getRemoteId()).compareTo(Long.valueOf(o2.getRemoteId()));
            }
        });
        
        for(Order localOrder : localOrders){
            int pos = Collections.binarySearch(remoteOrders, localOrder, new Comparator<Order>() {

                @Override
                public int compare(Order o1, Order o2) {
                    return Long.valueOf(o1.getRemoteId()).compareTo(Long.valueOf(o2.getRemoteId()));
                }
            });
            
            if(pos > 0){
                resultSet.add(localOrder);
            }
        }
        
        return resultSet;
    }

    public Result getOrders(Path rootPath, String login, int syncStatus) {
        DataBase dataBase = getDataBase(rootPath, login);
        if (dataBase == null) {
            return Result.newResultError("DataBase is null");
        }

        ArrayList<Order> orders = dataBase.getAllObjectsList(Order.class.getName());
        if (orders == null) {
            return Result.newResultSuccess(new ArrayList<>());
        } else {
            ArrayList<Order> resultSet = new ArrayList<>();
            for(Order order : orders){
                if(order.getSyncStatus() == syncStatus){
                    resultSet.add(order);
                }
            }
            return Result.newResultSuccess(resultSet);
        }
    }
    
    public Result setOrdersStatus(Path rootPath, String login, ArrayList<Order> orders, int syncStatus){

        DataBase dataBase = getDataBase(rootPath, login);
        if (dataBase == null) {
            return Result.newResultError("DataBase is null");
        }
        
        for(Order order : orders){
            order.setSyncStatus(syncStatus);
            if(!dataBase.updateObject(order)){
                return Result.newResultError("Can't update Order " + order);
            }
        }
        
        return Result.newEmptySuccess();
    }
    
    public Result getPageByName(Path rootPath, String login, String path){
        DataBase dataBase = getDataBase(rootPath, login);
        if (dataBase == null) {
            return Result.newResultError("DataBase is null");
        }
        
        ArrayList<Page> list = dataBase.getAllObjectsList(Page.class.getName());
        for(Page page : list){
            if(page.getPath().equals(path)){
                return Result.newResultSuccess(page);
            }
        }
        
        return Result.newResultError("Can't find page with path " + path);
    }
    
    public Result syncImage(Path rootPath, String login, String imageName, byte[] data){
        Path p = rootPath.resolve(login + "/img/www/" + imageName);
        try {
            if(p.toFile().exists()){
                p.toFile().delete();
            }
            Files.write(p, data);
            return Result.newEmptySuccess();
            
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return Result.newResultError("Can't sync image " + imageName);
        }
        
    }
}
