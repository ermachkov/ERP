/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.net;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uui.db.DataBase;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Server {

    private InetSocketAddress socketAddress;
    private boolean hasMore = true;
    private static Server self = null;
    private Map<String, Class> primitiveMap;
    private ExecutorService es;

    private Server(String host, int port) {
        es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        primitiveMap = new HashMap<>();
        primitiveMap.put(Byte.class.getName(), byte.class);
        primitiveMap.put(Short.class.getName(), short.class);
        primitiveMap.put(Integer.class.getName(), int.class);
        primitiveMap.put(Long.class.getName(), long.class);
        primitiveMap.put(Double.class.getName(), double.class);
        primitiveMap.put(Float.class.getName(), float.class);

        socketAddress = new InetSocketAddress(host, port);
        Executors.newSingleThreadExecutor().execute(runServer());
    }

    public static synchronized void startServer(String host, int port) {
        if (self == null) {
            self = new Server(host, port);
        }
    }

    private Runnable runServer() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket();
                    serverSocket.bind(socketAddress);
                    System.out.println("Data base server running " + socketAddress);

                    while (hasMore) {
                        Socket socket = serverSocket.accept();
                        Logger.getGlobal().log(Level.INFO, 
                                "New connection accepted {0}:{1}", 
                                new Object[]{socket.getInetAddress(), socket.getPort()});
                        es.execute(requestExtractor(socket));
                    }
                } catch (IOException e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                }
            }
        };

        return r;
    }

    private Runnable requestExtractor(final Socket socket) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                InputStream is = null;
                OutputStream out = null;
                try {
                    is = socket.getInputStream();
                    out = socket.getOutputStream();

                    byte[] b = new byte[1024];
                    int len;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while ((len = is.read(b)) != -1) {
                        baos.write(Arrays.copyOfRange(b, 0, len));
                    }

                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    Request request = (Request) new ObjectInputStream(bais).readObject();
                    Logger.getGlobal().log(Level.INFO, "Extracted request from node {0} = {1}",
                            new Object[]{socket, request});

                    Request response = requestHandler(request);
                    if (response == null) {
                        out.write(new byte[0]);
                        Logger.getGlobal().log(Level.WARNING, "Response for request {0} is NULL", request);
                    } else {
                        out.write(response.getSerailData());
                    }

                    out.flush();
                    socket.shutdownOutput();

                    is.close();
                    out.close();
                    socket.close();

                } catch (IOException | ClassNotFoundException e) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception ex) {
                        }
                    }

                    if (out != null) {
                        try {
                            out.close();
                        } catch (Exception ex) {
                        }
                    }

                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Exception ex) {
                        }
                    }

                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        };

        return r;
    }

    private Request requestHandler(Request request) {
        Request result = new Request();
        Method m = null;

        for (Method method : DataBase.class.getMethods()) {
            if (method.toString().equals(request.getMethod())) {
                m = method;
                break;
            }
        }

        if (m == null) {
            return null;
        }

        try {
            result.setResult(m.invoke(DataBase.getDataBase(), request.getParams()));

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return result;
        }

        return result;
    }
}
