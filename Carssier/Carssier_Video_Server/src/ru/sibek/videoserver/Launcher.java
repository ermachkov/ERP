/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.videoserver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author developer
 */
public class Launcher {
    
    public Launcher(){
        //
    }
    
    public static void main(String args[]){
        String usage = "Use java -jar VideoServer.jar port";
        Launcher launcher = new Launcher();
        try {
            launcher.requestCleaner();
            launcher.start(args[0].trim(), Integer.parseInt(args[1]));
            
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }
    }
    
    private void requestCleaner(){
        Runnable r = new Runnable() {

            @Override
            public void run() {
                Path pDir = Paths.get(System.getProperty("user.dir"), "app", "frames");
                for(File dir : pDir.toFile().listFiles()){
                    if(dir.isFile()){
                        continue;
                    }
                    
                    Path p = Paths.get(dir.getName(), "request");
                    Path pCheck = pDir.resolve(p);
                    if(pCheck.toFile().exists()){
                        pCheck.toFile().delete();
                    }
                }
            }
        };
        
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(r, 0, 1, TimeUnit.MINUTES);
    }
    
    private void start(String url, int port){
        Path p = Paths.get(System.getProperty("user.dir"), "app");
        VideoServer videoServer = new VideoServer(p.toString(), url, port);
        videoServer.startInputServer();
    }
    
}
