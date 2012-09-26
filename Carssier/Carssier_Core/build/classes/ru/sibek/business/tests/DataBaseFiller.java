/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.tests;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.ubo.employee.Crew;
import org.ubo.goods.Goods;
import org.ubo.service.Service;
import org.ubo.tree.Trees;
import org.ubo.utils.ImageLoader;
import org.uui.db.DataBase;
import ru.sibek.business.core.CarssierCore;
import org.ubo.utils.Result;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DataBaseFiller {

    private ArrayList<Map<String, String>> treeList = new ArrayList<>();
    private ArrayList<Map<String, String>> treeGoodsList = new ArrayList<>();
    private AtomicBoolean isFind = new AtomicBoolean(false);
    private AtomicInteger pos = new AtomicInteger(0);
    private AtomicInteger lastPos = new AtomicInteger(-1);
    private String parent;
    private Path path;
    private boolean isReturn = false;
    private DataBase dataBase;
    private CarssierCore businessLogic;
    private ImageIcon goodsIcon, serviceIcon;
    private String iconsPath;

    public DataBaseFiller() {
        dataBase = CarssierDataBase.getDataBase();
        businessLogic = CarssierCore.getInstance();

        String homePath = System.getProperty("user.home") + File.separator
                + ".saas" + File.separator + "app" + File.separator;
        iconsPath = homePath + "images" + File.separator
                + "icons" + File.separator;
        serviceIcon = ImageLoader.getInstance().getFromFile(
                iconsPath + "service.png");

        goodsIcon = ImageLoader.getInstance().getFromFile(
                iconsPath + "goods.png");
    }

    public void start() throws IOException {
        initGoodsList();
        convert();
    }

//    public static void main(String args[]) throws IOException {
//        DataBaseFiller xml2JSSDB = new DataBaseFiller();
//    }
    private void convert() throws IOException {
        Path p = Paths.get(System.getProperty("user.dir"), "tree.csv");
        BufferedReader br = Files.newBufferedReader(p, Charset.defaultCharset());
        String str;
        while ((str = br.readLine()) != null) {
            Map<String, String> map = new LinkedHashMap<>();
            String arr[] = str.split(",");
            map.put("id", arr[0]);
            map.put("subid", arr[1]);
            map.put("pos", arr[2]);
            map.put("name", arr[3].replaceAll("\"", ""));
            treeList.add(map);
        }

        parseTree();
    }

    private void parseTree() {
        System.out.println(treeList);
        for (Map<String, String> map : treeList) {
            if (map.get("subid").equals("0")) {
                //System.out.println(map.get("name"));
                path = Paths.get("/" + map.get("name"));
                System.out.println("Path = " + path);
                createFolder(path);
                isReturn = false;
                pos.set(0);
                lastPos.set(-1);
                treeWalker(map.get("id"));
            }
        }
    }

    private void createFolder(Path p) {
        Trees.createTreeFolderByPath("/GoodsAndService" + p.toString(), dataBase);
    }

    private void addGoodsService(Path p, Map<String, String> m) {
        Result result;
        if (m.get("idGoodsType").equals("1")) {
            result = businessLogic.addGoods("/GoodsAndService" + p.toString(),
                    p.getName(p.getNameCount() - 1) + " " + m.get("shortName"), iconsPath + "goods.png");
            if (result.isError()) {
                System.err.println(result);
                
            } else {
                System.out.println(result);
                Result r = businessLogic.setGoodsSalaryPercent((Goods)result.getObject(), new BigDecimal("10"));
                
                if(r.isError()){
                    System.err.println(r);
                } else {
                    System.out.println(r);
                }
            }


        } else {
            result = businessLogic.getDefaultCrew();
            result = businessLogic.addService("/GoodsAndService" + p.toString(),
                    p.getName(p.getNameCount() - 1) + " " + m.get("shortName"), 
                    iconsPath + "service.png", ((Crew)result.getObject()).getId());
            if (result.isError()) {
                System.err.println(result);
                
            } else {
                System.out.println(result);
                Result r = businessLogic.setServiceSalaryPercent((Service)result.getObject(), new BigDecimal("10"));
                if(r.isError()){
                    System.err.println(r);
                    
                } else {
                    System.out.println(r);
                    
                    Result rIcon = Result.newResult(true, "Default");
                    String pIcons = Paths.get(System.getProperty("user.home"), 
                            ".saas", "app", "images", "icons").toString() + File.separator;
                    Service s = (Service)r.getObject();
                    
                    if(s.getShortName().toLowerCase().indexOf("балансировка") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "balancing.png");
                        
                    } else if(s.getShortName().toLowerCase().indexOf("подкачка") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "inflating.png");
                        
                    } else if(s.getShortName().toLowerCase().indexOf("прокол") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "c_repair.png");
                        
                    } else if(s.getShortName().toLowerCase().indexOf("камер") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "c_repair.png");
                        
                    } else if(s.getShortName().toLowerCase().indexOf("покраска") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "painting.png");
                        
                    } else if(s.getShortName().toLowerCase().indexOf("бортовка") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "stripping.png");
                        
                    } else if(s.getShortName().toLowerCase().indexOf("дисков") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "w_repair.png");
                        
                    } else if(s.getShortName().toLowerCase().indexOf("покрыш") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "t_repair.png");
                        
                    } else if(s.getShortName().toLowerCase().indexOf("монтаж") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "w_fitting.png");
                        
                    }  else if(s.getShortName().toLowerCase().indexOf("съём") != -1){
                        rIcon = businessLogic.setIconService(s, pIcons + "w_fitting.png");
                    }
                    
                    if(rIcon.isError()){
                        System.err.println(rIcon);
                    } else {
                        System.out.println(rIcon);
                    }
                }
            }
        }

    }

    private void treeWalker(String id) {
        for (Map<String, String> map : treeList) {
            String subid = map.get("subid");
            if (id.equals(subid)) {
                pos.set(pos.get() + 3);
                if (lastPos.get() != pos.get()) {
                    if (lastPos.get() < pos.get()) {
                        path = Paths.get(path.toString(), map.get("name").replaceAll("/", "\\\\"));
                        System.out.println("Path = " + path);
                        createFolder(path);
                        for (Map<String, String> m : getGoodsList(map.get("id"))) {
                            System.out.println(m);
                            addGoodsService(path, m);
                        }

                    } else {
                        if (isReturn) {
                            isReturn = false;
                            Path pp = path;
                            for (int i = 0; i < ((lastPos.get() - pos.get()) / 3) + ((lastPos.get() - pos.get()) / 3); i++) {
                                pp = pp.getParent();
                            }
                            path = Paths.get("" + pp, map.get("name").replaceAll("/", "\\\\"));

                        } else {
                            path = Paths.get(path.getParent().toString(), map.get("name").replaceAll("/", "\\\\"));
                        }

                        System.out.println("Path = " + path);
                        createFolder(path);
                        for (Map<String, String> m : getGoodsList(map.get("id"))) {
                            System.out.println(m);
                            addGoodsService(path, m);
                        }
                    }

                } else {
                    path = Paths.get(path.getParent().toString(), map.get("name").replaceAll("/", "\\\\"));
                    System.out.println("Path = " + path);
                    createFolder(path);
                    for (Map<String, String> m : getGoodsList(map.get("id"))) {
                        System.out.println(m);
                        addGoodsService(path, m);
                    }
                    isReturn = true;
                }

                lastPos.set(pos.get());
                treeWalker(map.get("id"));
            }
        }

        pos.set(pos.get() - 3);
    }

    private ArrayList<Map<String, String>> getGoodsList(String idTree) {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        for (Map<String, String> map : treeGoodsList) {
            if (map.get("idTree").equals(idTree)) {
                list.add(map);
            }
        }
        return list;
    }

    private void initGoodsList() {
        try {
            Path p = Paths.get(System.getProperty("user.dir"), "goods.csv");
            BufferedReader br = Files.newBufferedReader(p, Charset.defaultCharset());
            String str;// 9088072716 20
            while ((str = br.readLine()) != null) {
                Map<String, String> map = new LinkedHashMap<>();
                String arr[] = str.split(",");
                map.put("idGoods", arr[0]);
                map.put("idTree", arr[1]);
                map.put("idGoodsType", arr[2]);
                map.put("shortName", arr[3].replaceAll("\"", ""));
                treeGoodsList.add(map);
            }
        } catch (Exception e) {
            System.err.println(e);
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

    }
}
