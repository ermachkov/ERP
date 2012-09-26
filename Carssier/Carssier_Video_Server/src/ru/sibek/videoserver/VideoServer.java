/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.videoserver;

import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

/**
 *
 * @author developer
 */
public class VideoServer {

    private int port;
    private String wwwRootPath, url;

    public VideoServer(String wwwRootPath, String url, int port) {
        this.wwwRootPath = wwwRootPath;
        this.port = port;
        this.url = url;
    }

    public void startInputServer() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Tomcat tomcat = new Tomcat();
                    tomcat.setPort(port);
                    Context ctxVideoInput = tomcat.addContext("/", wwwRootPath);
                    
                    Tomcat.addServlet(ctxVideoInput, "videoInput", new VideoInputServlet(wwwRootPath));
                    ctxVideoInput.addServletMapping("/", "videoInput");
                    
                    tomcat.start();

                    Thread.sleep(2000);
                    startOutputServer();

                    tomcat.getServer().await();
                } catch (LifecycleException | InterruptedException e) {
                    Logger.getGlobal().log(Level.SEVERE, wwwRootPath, e);
                }

            }
        };

        Executors.newSingleThreadExecutor().execute(r);
    }

    private void startOutputServer() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Tomcat tomcat = new Tomcat();
                    tomcat.setPort(port + 1);
                    Context ctxVideoOutput = tomcat.addContext("/", wwwRootPath);
                    Tomcat.addServlet(ctxVideoOutput, "videoOutput", new VideoOutputServlet(wwwRootPath, url));
                    ctxVideoOutput.addServletMapping("/", "videoOutput");
                    tomcat.start();

                    tomcat.getServer().await();
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.SEVERE, wwwRootPath, e);
                }

            }
        };

        Executors.newSingleThreadExecutor().execute(r);
    }
}
