/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.server.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.http.server.TomcatServer;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Test {
    
    public static void main(String args[]){
        System.out.println(Arrays.toString(args));
        
        if(args.length < 2){
            System.out.println("use arguments /path/to/www/root port");
            System.exit(-1);
        }
        
        Path path = Paths.get(args[0]);
        TomcatServer tomcatServer = new TomcatServer(path.toString(), "index.html", Integer.parseInt(args[1])) {

            @Override
            public void started() {
                System.out.println("Server started");
            }

            @Override
            public void registeredNewSession(String sessionId) {
                System.out.println("registeredNewSession >>>>>>>>>>>>>>>>>>>>> " + sessionId);
            }
        };
        tomcatServer.start();
    }
    
}
