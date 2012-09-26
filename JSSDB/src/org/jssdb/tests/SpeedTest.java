/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.query.CacheData;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class SpeedTest {
    
    //private Set<CacheData> cacheList = new HashSet<>();
    private CopyOnWriteArraySet<CacheData> cacheList = new CopyOnWriteArraySet<>();
    
    public static void main(String args[]){
        SpeedTest st = new SpeedTest();
        st.start();
    }
    
    private void start(){
        long start = System.currentTimeMillis();
        Path pDir = Paths.get(System.getProperty("user.home"), ".saas", "app", 
                "db", "org.ubo.accountbook.SyntheticAccount"); //SyntheticAccount AccountPost
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for(File f : pDir.toFile().listFiles()){
            if(f.isDirectory()){
                continue;
            }
            
            try {
                cacheList.add(CacheData.createCacheData(
                            "org.ubo.accountbook.SyntheticAccount", 
                            Long.parseLong(f.getName()), 
                            Files.readAllBytes(f.toPath())));
            } catch (NumberFormatException | IOException e) {
                Logger.getGlobal().log(Level.SEVERE, null, e);
            }
        }
        
        System.out.println(">>>>>>>>>> " + cacheList.toArray()[0]);
        System.out.println("Time: " + (System.currentTimeMillis() - start));
    }
    
}
