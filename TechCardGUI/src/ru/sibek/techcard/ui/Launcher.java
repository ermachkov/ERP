package ru.sibek.techcard.ui;

import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.http.server.TomcatServer;
import org.uws.server.UniversalWebSocket;

/*
 * import java.io.IOException; import java.nio.file.Paths; import
 * java.util.concurrent.Executors; import java.util.logging.Level; import
 * java.util.logging.Logger; import org.http.server.TomcatServer; import
 * org.uws.server.UniversalWebSocket;
 *
 * /*
 *
 * @author developer
 */
public class Launcher {

    public Launcher() {
        //start();
    }

    public static void main(String args[]) {
        Launcher launcher = new Launcher();
        launcher.start();
    }

    private void start() {
        String rootPath = Paths.get(System.getProperty("user.dir"), "app", "ui").toString();

//        WebKitHTTPServer.startSocket(rootPath, 1572);
//        WebKitWebSocketServer.startWebSocketServer(10081);
        startWebSocketServer(1573);

        TomcatServer tomcatServer = new TomcatServer(rootPath, "index.html", 1572) {

            @Override
            public void started() {
                System.out.println("Tomcat started");
                startChrome();
            }

            @Override
            public void registeredNewSession(final String sessionId) {
                ComponentsView componentsView = new ComponentsView(sessionId);
                System.out.println(componentsView.getIdentificator());
            }
        };
        tomcatServer.start();
    }

    private void startChrome() {
        // start Chrome like app
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {

                    Process p = Runtime.getRuntime().exec("/usr/bin/google-chrome --app=http://localhost:1572/index.html");

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                }
            }
        };

        Executors.newSingleThreadExecutor().execute(r);
    }

    private void startWebSocketServer(final int port) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                UniversalWebSocket uws = new UniversalWebSocket(port);
            }
        };

        Executors.newSingleThreadExecutor().execute(r);
    }
}
