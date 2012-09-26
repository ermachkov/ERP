/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.json.JSONArray;
import org.ubo.json.JSONException;
import org.ubo.json.JSONObject;
import org.uui.component.Component;
import org.uui.db.DataBase;
import org.uui.db.event.DataBaseServiceEvent;
import org.uui.db.event.DataBaseServiceEventListener;
import ru.sibek.business.ui.JSMediator;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DataBaseHandler extends Component {

    private CopyOnWriteArrayList<String> restoreClasses = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<String> restoredClasses = new CopyOnWriteArrayList<>();
    private int progressBarValue = 0;
    private ScheduledExecutorService ses;
    //private DataBase dataBase;
    private LoginHandler loginHandler;
    private boolean isInitLogin = false;
    
    private DataBaseHandler(String sessionId){
        super(sessionId);
        loginHandler = new LoginHandler(sessionId);
        setIdentificator(sessionId);
    }
    
    public static void newDataBaseHandler(String session){
        DataBaseHandler dataBaseHandler = new DataBaseHandler(session);
        dataBaseHandler.setSession(session);
        dataBaseHandler.setLoginHandler(new LoginHandler(session), session);
    }
    
    public void setLoginHandler(LoginHandler loginHandler, String session){
        this.loginHandler = loginHandler;
        this.loginHandler.setSession(session);
        setIdentificator(session);
    }

    @Override
    public String getModel() {
        String model = ""
                + "<div id='dataBaseHandlerPanel'>"
                + "<table width='100%' height='100%'>"
                + "<tr>"
                + "<td valign='middle' align='center'>"
                + "<img src='img/logo/logo.jpg' />"
                + "<div id='dbEventDescrition'></div>"
                + "<div id='progressBarDBContainer' align='left'>"
                + "<div id='progressBarDB'></div>"
                + "</div>"
                + "<div id='dbEventFullDescrition'></div>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</div>";

        return model;
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

            if (jsonObject.getString("eventType").equals("push")) {
                if (jsonObject.getString("action").equals("showSplashScreen")) {
                    JSMediator.showSplashPanel(getSession(), getModel());
                    initDataBase();
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(LoginHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setIntermedate(boolean isEnabled) {
        if (isEnabled) {
            ses = Executors.newSingleThreadScheduledExecutor();
            ses.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    progressBarValue++;
                    progressBarValue = progressBarValue > 100 ? 0 : progressBarValue;
                    JSMediator.setSplashProgressBar(getSession(), progressBarValue);
                }
            }, 0, 100, TimeUnit.MILLISECONDS);
        } else {
            if (ses != null) {
                ses.shutdown();
            }
        }

    }

    private void updateProgressBar() {
        double value = (double) restoredClasses.size() / (double) restoreClasses.size();
        value = value * 100;
        JSMediator.setSplashProgressBar(getSession(), (int) value);
    }

    public void initDataBase() {
        if(DataBaseInstance.getInstance().getDataBase() != null){
            JSMediator.initLogin(getSession(), loginHandler.getIdentificator());
            return;
        }
        
        final DataBase dataBase = CarssierDataBase.getDataBase();
        dataBase.addDataBaseServiceEventListener(new DataBaseServiceEventListener() {
            @Override
            public void serviceEvent(DataBaseServiceEvent evt) {
                System.out.println("DataBaseServiceEvent: " + evt);
                try {
                    JSONObject json = new JSONObject(evt.getEvent());
                    if (json.has("message")) {
                        JSMediator.refreshElement(getSession(), "dbEventDescrition", json.getString("message"));

//                        if (json.getString("message").equals("database broken")) {
//                            JSMediator.refreshElement(getSession(), "body", "database broken");
//                        }
//                        
//                        if (json.getString("message").equals("Switch to restore mode")) {
//                            setIntermedate(false);
//                            JSMediator.refreshElement(
//                                    getSession(),
//                                    "dbEventFullDescrition",
//                                    "Восстановление базы данных, пожалуйста проявите терпение...");
//                        }
                        
                        if (json.getString("message").indexOf("restore cache for") != -1) {
                            JSMediator.refreshElement(getSession(), "dbEventDescrition", json.getString("message"));
                        }
                        
                        if (json.getString("message").indexOf("checking objects") != -1) {
                            JSMediator.refreshElement(getSession(), "dbEventDescrition", json.getString("message"));
                            JSMediator.refreshElement(
                                    getSession(),
                                    "dbEventFullDescrition",
                                    "Проверка базы данных...");
                        }
                        
                        if (json.getString("message").indexOf("result for checking objects") != -1) {
                            JSONObject values = json.getJSONObject("value");
                            JSMediator.refreshElement(getSession(), "dbEventDescrition", json.getString("message"));
                            restoredClasses.add(values.getString("class"));
                            updateProgressBar();
                        }
                        
                        if (json.getString("message").indexOf("reset progress bar") != -1) {
                            restoredClasses.clear();
                        }

                        if (json.getString("message").equals("try load from snapshot")) {
                            setIntermedate(false);
                            JSMediator.refreshElement(
                                    getSession(),
                                    "dbEventFullDescrition",
                                    "Загрузка базы данных...");
                        }

                        if (json.getString("message").equals("load from snapshot complete")) {
                            setIntermedate(false);
                            //JSMediator.initLogin(getSession(), loginHandler.getIdentificator());
                            //initLogin();
                        }

                        if (json.getString("message").equals("Restore complete")) {
                            //JSMediator.initLogin(getSession(), loginHandler.getIdentificator());
                            //initLogin();
                        }

                        if (json.getString("message").equals("restoreClassList")) {
                            JSONArray array = json.getJSONObject("value").getJSONArray("list");
                            for (int i = 0; i < array.length(); i++) {
                                restoreClasses.add(array.getString(i));
                            }
                        }

                        //{message:"init cache complete for org.ubo.accountbook.AccountBook", value:{time:103, class:"org.ubo.accountbook.AccountBook"}}}
                        if (json.getString("message").startsWith("init cache complete for")) {
                            JSONObject values = json.getJSONObject("value");
                            restoredClasses.add(values.getString("class"));
                            updateProgressBar();
                            JSMediator.refreshElement(
                                    getSession(),
                                    "dbEventFullDescrition",
                                    "Загрузка объектов базы данных...");
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(DataBaseHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        Runnable r = new Runnable() {
            @Override
            public void run() {
                dataBase.init();
                DataBaseInstance.getInstance().setDataBase(dataBase);
                JSMediator.initLogin(getSession(), loginHandler.getIdentificator());
            }
        };

        Executors.newSingleThreadExecutor().execute(r);
    }
    
    private void initLogin(){
        if(!isInitLogin){
            isInitLogin = true;
            JSMediator.initLogin(getSession(), loginHandler.getIdentificator());
        }
    }
}
