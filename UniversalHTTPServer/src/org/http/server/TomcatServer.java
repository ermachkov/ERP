package org.http.server;

import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public abstract class TomcatServer {

    private String wwwRootPath, indexPage;
    private int port;

    public TomcatServer(String wwwRootPath, String indexPage, int port) {
        this.wwwRootPath = wwwRootPath;
        this.indexPage = indexPage;
        this.port = port;
    }
    
    public abstract void registeredNewSession(String sessionId);

    public void start() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Tomcat tomcat = new Tomcat();
                    tomcat.setPort(port);

                    Context ctxCore = tomcat.addContext("/", wwwRootPath);
                    Tomcat.addServlet(ctxCore, "coreServlet", new CoreServlet(wwwRootPath, indexPage) {

                        @Override
                        public void registeredNewSession(String sessionId) {
                            TomcatServer.this.registeredNewSession(sessionId);
                        }
                    });
                    ctxCore.addServletMapping("/", "coreServlet");

                    tomcat.start();
                    Thread.sleep(2000);

                    //runWebSocket();
                    runStreamSocket();

                    tomcat.getServer().await();

                } catch (LifecycleException | InterruptedException e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                }
            }
        };

        Executors.newSingleThreadExecutor().execute(r);
    }

    private void runWebSocket() {
        Runnable rWs;
        rWs = new Runnable() {
            @Override
            public void run() {
                try {
                    Tomcat tomcat = new Tomcat();
                    tomcat.setPort(port + 1);

                    Context ctxWebSocket = tomcat.addContext("/", wwwRootPath);
                    Tomcat.addServlet(ctxWebSocket, "websocketServlet", new TomcatWebSocket());
                    ctxWebSocket.addServletMapping("/", "websocketServlet");

                    tomcat.start();
                    Thread.sleep(2000);
                    runStreamSocket();
                    
                    tomcat.getServer().await();

                } catch (LifecycleException | InterruptedException e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                }
            }
        };

        Executors.newSingleThreadExecutor().execute(rWs);
    }
    
    private void runStreamSocket() {
        Runnable rWs;
        rWs = new Runnable() {
            @Override
            public void run() {
                try {
                    Tomcat tomcat = new Tomcat();
                    tomcat.setPort(port + 2);

                    Context ctx = tomcat.addContext("/", wwwRootPath);
                    Tomcat.addServlet(ctx, "streamServlet", new StreamServlet(wwwRootPath));
                    ctx.addServletMapping("/", "streamServlet");

                    tomcat.start();
                    started();
                    tomcat.getServer().await();

                } catch (LifecycleException e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                }
            }
        };

        Executors.newSingleThreadExecutor().execute(rWs);
    }
    
    public abstract void started();
}