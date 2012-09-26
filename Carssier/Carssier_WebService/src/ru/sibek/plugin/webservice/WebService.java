/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.webservice;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.document.Order;
import org.ubo.document.ServiceCommandment;
import org.ubo.goods.Goods;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.ubo.service.Service;
import org.ubo.tree.*;
import org.ubo.utils.Result;
import org.ubo.www.ExchangeSettings;
import org.ubo.www.Mediator;
import org.ubo.www.PriceACL;
import org.uui.webkit.WebKitEventBridge;
import ru.sibek.business.core.CarssierCore;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class WebService {

    private CarssierCore core;
    private ScheduledExecutorService service;
    private AtomicBoolean isExchangeBusy = new AtomicBoolean(false);
    private ExchangeSettings exchangeSettings;
    private String session;

    public WebService(String sessionId) {
        this.session = sessionId;
        core = CarssierCore.getInstance();
        init();
    }

    private void init() {
        Result result = core.getExchangeSettings();
        if (!result.isError()) {
            exchangeSettings = (ExchangeSettings) result.getObject();
            if (exchangeSettings.isEnabled()) {
                startWebService();
            }
        }
    }

    public void reInit(ExchangeSettings es) {
        exchangeSettings = es;

        if (!exchangeSettings.isEnabled()) {
            stopWebService();
        } else {
            stopWebService();
            startWebService();
        }
    }

    public void startWebService() {
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(exchange(), 0, exchangeSettings.getExchangeTimeOut(), TimeUnit.MINUTES);
    }

    public void stopWebService() {
        if (service != null) {
            service.shutdown();
        }
    }

    private Runnable exchange() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    if (isExchangeBusy.get()) {
                        return;
                    }

                    isExchangeBusy.set(true);
                    
                    pushImages();
                    
                    push();
                    
                    pull();
                    
                    isExchangeBusy.set(false);

                } catch (Exception e) {
                    isExchangeBusy.set(false);
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return runnable;
    }
    
    private void pushImages(){
        try {
            URL url = new URL(exchangeSettings.getRemoteHost());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setReadTimeout(15000);
            
            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                Mediator mediator = new Mediator();
                Map<String, Object> m = new HashMap<>();
                m.put("login", exchangeSettings.getLogin());
                m.put("password", exchangeSettings.getLogin());
                m.put("action", "pushImages");
                
                Path pImagesRoot = Paths.get(System.getProperty("user.home"), 
                        ".saas","app", "ui", "img", "www");
                for(File f : pImagesRoot.toFile().listFiles()){
                    if(f.isDirectory()){
                        continue;
                    }
                    byte b[] = Files.readAllBytes(f.toPath());
                    m.put("image:" + f.getName(), b);
                }
                
                mediator.setAttributes(m);
                out.write(mediator.getSerailData());
                out.flush();
            }
            
            InputStream is = connection.getInputStream();
            byte b[] = new byte[8096];
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((len = is.read(b)) != -1){
                baos.write(Arrays.copyOf(b, len));
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Mediator mediatorResponse = (Mediator)ois.readObject();

            JSONObject json = new JSONObject();
            json.putOnce("eventType", "push");
            json.putOnce("action", "setLog");
            json.put("data", mediatorResponse.getAttributes().get("statusHuman"));
            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                    "ru.sibek.plugin.webservice.WebServicePanel",
                    json.toString());
            Logger.getGlobal().log(Level.INFO, "{0}", mediatorResponse.getAttributes().get("statusHuman"));
            
        } catch (IOException | ClassNotFoundException | JSONException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void push() {
        try {
            URL url = new URL(exchangeSettings.getRemoteHost());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setReadTimeout(15000);

            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                Mediator mediator = new Mediator();
                Map<String, Object> m = new HashMap<>();
                ArrayList<PriceACL> list = CarssierDataBase.getDataBase().getAllObjectsList(PriceACL.class.getName());
                m.put("PriceACL", list);
                Result r = core.getTreeGoodsAndService();
                if (!r.isError()) {
                    TreeBasic treeBasic = (TreeBasic) r.getObject();
                    m.put("TreeGoodsAndService", treeBasic);

                    ArrayList<TreeLeafBasic> treeLeafs = new ArrayList<>();
                    for (TreeLeaf treeLeaf : treeBasic.getRootFolder().getAllDescendTreeLeaves()) {
                        treeLeafs.add((TreeLeafBasic) treeLeaf);
                    }
                    m.put("TreeLeafBasic", treeLeafs);

                    final ArrayList<TreeFolderBasic> treeFolders = new ArrayList<>();
                    TreeNodeWalker tnw = new TreeNodeWalker(treeBasic.getRootFolder()) {

                        @Override
                        public TreeNodeVisitResult visitFolder(TreeFolder folder) {
                            treeFolders.add((TreeFolderBasic) folder);
                            return TreeNodeVisitResult.CONTINUE;
                        }

                        @Override
                        public TreeNodeVisitResult visitLeaf(TreeLeaf leaf) {
                            return TreeNodeVisitResult.CONTINUE;
                        }
                    };

                    tnw.start();

                    treeFolders.add((TreeFolderBasic) treeBasic.getRootFolder());
                    m.put("TreeFolderBasic", treeFolders);
                    m.put("WWWPages", core.getWWWPages());
                }

                m.put("Goods", CarssierDataBase.getDataBase().getAllObjectsList(Goods.class.getName()));
                m.put("Service", CarssierDataBase.getDataBase().getAllObjectsList(Service.class.getName()));
                m.put("ServiceCommandment", CarssierDataBase.getDataBase().getAllObjectsList(ServiceCommandment.class.getName()));

                m.put("login", exchangeSettings.getLogin());
                m.put("password", exchangeSettings.getLogin());
                m.put("action", "fullSynchronize");

                mediator.setAttributes(m);
                out.write(mediator.getSerailData());
                out.flush();
            }

            InputStream is = connection.getInputStream();
            byte b[] = new byte[8096];
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((len = is.read(b)) != -1){
                baos.write(Arrays.copyOf(b, len));
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Mediator mediatorResponse = (Mediator)ois.readObject();

            JSONObject json = new JSONObject();
            json.putOnce("eventType", "push");
            json.putOnce("action", "setLog");
            json.put("data", mediatorResponse.getAttributes().get("statusHuman"));
            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                    "ru.sibek.plugin.webservice.WebServicePanel",
                    json.toString());
            Logger.getGlobal().log(Level.INFO, "{0}", mediatorResponse.getAttributes().get("statusHuman"));

        } catch (IOException | ClassNotFoundException | JSONException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void pull() {
        try {
            URL url = new URL(exchangeSettings.getRemoteHost());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setReadTimeout(15000);
            try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                Mediator mediator = new Mediator();
                Map<String, Object> map = new HashMap<>();
                map.put("action", "getSetOrders");
                map.put("login", exchangeSettings.getLogin());
                map.put("password", exchangeSettings.getLogin());
                map.put("OrdersConfirmed", core.getWWWOrdersConfirmed());
                mediator.setAttributes(map);

                out.write(mediator.getSerailData());
                out.flush();
            }
            
            InputStream is = connection.getInputStream();
            byte b[] = new byte[8096];
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((len = is.read(b)) != -1){
                baos.write(Arrays.copyOf(b, len));
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Mediator mediatorResponse = (Mediator)ois.readObject();
            Map<String, Object> mapResponse = mediatorResponse.getAttributes();
            Result r;
            if((int)mapResponse.get("status") == 0){
                ArrayList<Order> orders = (ArrayList<Order>) mapResponse.get("notSyncOrders");
                r = core.addWWWOrders(orders);
            } else {
                r = Result.newResultError("" + mapResponse.get("statusHuman"));
            }

            JSONObject json = new JSONObject();
            json.putOnce("eventType", "push");
            json.putOnce("action", "setLog");
            json.put("data", r.getReason());
            WebKitEventBridge.getInstance().pushEventToComponent(getSession(), 
                    "ru.sibek.plugin.webservice.WebServicePanel",
                    json.toString());
            Logger.getGlobal().log(Level.INFO, "{0}", r.getReason());

        } catch (IOException | ClassNotFoundException | JSONException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }
    
    public String getSession(){
        return session;
    }
}
