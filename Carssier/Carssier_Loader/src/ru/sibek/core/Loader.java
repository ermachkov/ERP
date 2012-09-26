/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.http.server.TomcatServer;
import org.uws.server.UniversalWebSocket;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Loader {

    //private Instance instance;

    public Loader() {
        //instance = new Instance();
    }

    private void init() {
        try {
            String osName = System.getProperty("os.name");
            String osArch = System.getProperty("os.arch");
            System.out.println("os name = " + osName);
            System.out.println("os architecture = " + osArch);

            String newLibPath = System.getProperty("java.library.path");
            Path rxtxPath = Paths.get(System.getProperty("user.home"), ".saas", "app", "rxtx", osName, osArch);
            //Path panelsPath = Paths.get(System.getProperty("user.home"), ".saas", "app", "panels");
            newLibPath = rxtxPath + File.pathSeparator + newLibPath;// + File.pathSeparator + panelsPath;
            Path videoPath = Paths.get(System.getProperty("user.home"), ".saas", "app", "video", osName, osArch);
            newLibPath = videoPath + File.pathSeparator + newLibPath;
            System.out.println("Path = " + newLibPath);

//            if (args != null) {
//                for (String arg : args) {
//                    if (arg.indexOf("=") != -1) {
//                        String arr[] = arg.split("=");
//                        System.setProperty(arr[0], arr[1]);
//                    }
//                }
//            }

            System.setProperty("java.library.path", newLibPath);
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            if (fieldSysPath != null) {
                fieldSysPath.set(System.class.getClassLoader(), null);
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }

    }

    public static void main(String args[]) {
        String usage = "Use java -jar Carssier_Loader.jar [client port app|kiosk] [server port]";

        if (args.length < 2) {
            System.out.println(usage);
            System.exit(0);
        }

        Loader loader = new Loader();

        switch (args[0].trim()) {
            case "client":
                try {
                    loader.init();
                    int port = Integer.valueOf(args[1]);

                    String mode;
                    if (args.length > 2) {
                        if (args[2].equals("")) {
                            mode = "app";
                        } else {
                            mode = args[2];
                        }
                    } else {
                        mode = "app";
                    }

                    loader.startWebSocketServer(port + 1);
                    loader.startServerWithGUI(port, true, mode);
                    break;

                } catch (Exception e) {
                    System.out.println(usage);
                    System.err.println(e);
                    System.exit(0);
                }

            case "server":
                try {
                    int port = Integer.valueOf(args[1]);
                    loader.startWebSocketServer(port + 1);
                    loader.startServer(port, false);
                    break;

                } catch (Exception e) {
                    System.out.println(usage);
                    System.err.println(e);
                    System.exit(0);
                }

            default:
                System.out.println(usage);
                System.exit(0);
        }
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
    
    private void startServerWithGUI(final int port, final boolean isClientMode, final String mode) {
        Path path = Paths.get(System.getProperty("user.home"), ".saas", "app", "ui");
        TomcatServer tomcatServer = new TomcatServer(path.toString(), "index.html", port) {
            @Override
            public void started() {
                System.out.println("Tomcat started");

                if (isClientMode) {
                    Loader.this.showFrame(port, mode);
                }
            }

            @Override
            public void registeredNewSession(final String sessionId) {
                startNewInstance(sessionId);
            }
        };
        tomcatServer.start();

    }

    private void startServer(final int port, final boolean isClientMode) {
        Path path = Paths.get(System.getProperty("user.home"), ".saas", "app", "ui");
        TomcatServer tomcatServer = new TomcatServer(path.toString(), "index.html", port) {
            @Override
            public void started() {
                System.out.println("Tomcat started");

                if (isClientMode) {
                    showFrame(port, null);
                }
            }

            @Override
            public void registeredNewSession(final String sessionId) {
                startNewInstance(sessionId);
            }
        };
        tomcatServer.start();

    }

    private void startNewInstance(final String sessionId) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                DataBaseHandler.newDataBaseHandler(sessionId);
                //Loader.this.instance.startNewInstance(sessionId);
            }
        });
    }

    private void showFrame(final int port, final String mode) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    String cmd = Paths.get(System.getProperty("user.home"),
                            ".saas", "app", "bin", "startui.sh").toString();
                    cmd += " " + mode + " " + port;
                    Process p = Runtime.getRuntime().exec(cmd);

                    InputStream is = p.getInputStream();
                    int len;
                    byte b[] = new byte[1024];
                    while ((len = is.read(b)) > 0) {
                        System.out.println(new String(Arrays.copyOf(b, len)));
                    }

//                    System.out.println(">>> END CLIENT SESSION <<<");
//                    System.out.println(">>> EXIT <<<");
//                    System.exit(0);

                } catch (IOException e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                }
            }
        };

        Executors.newSingleThreadExecutor().execute(r);
    }
}
