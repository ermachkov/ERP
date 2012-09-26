/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uui.db.net;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.uui.db.DataBase;
import org.uui.db.KnowsDB;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Client {

    private static Client self = null;
    private InetSocketAddress serverSocketAddress;

    private Client(String serverHost, int serverPort) {
        serverSocketAddress = new InetSocketAddress(serverHost, serverPort);
    }

    public static synchronized Client getInstance(String serverHost, int serverPort) {
        if (self == null) {
            self = new Client(serverHost, serverPort);
        }

        return self;
    }

    public Object getResult(String methodName, Object[] params) {
        Object result = null;

        Request request = new Request();
        request.setMethod(methodName);
        request.setParams(params);

        Socket socket = new Socket();
        OutputStream out = null;
        InputStream is = null;
        try {
            //socket.setTcpNoDelay(true);
            socket.connect(serverSocketAddress);

            out = socket.getOutputStream();
            is = socket.getInputStream();

            out.write(request.getSerailData());
            out.flush();
            socket.shutdownOutput();

            byte[] b = new byte[1024];
            int len;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((len = is.read(b)) != -1) {
                baos.write(Arrays.copyOfRange(b, 0, len));
            }

            out.close();
            is.close();
            socket.close();

            if (baos.toByteArray().length > 0) {
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                Request response = (Request) new ObjectInputStream(bais).readObject();
                Logger.getGlobal().log(Level.INFO,
                        "Extracted response from node {0} = {1}",
                        new Object[]{socket, request});
                result = response.getResult();
                
                if(result instanceof Arrays){
                    initTransients((Arrays)result);
                    
                } else if(result instanceof Map){
                    initTransients((Map<Long, Object>) result);
                    
                } else {
                    initTransients(result);
                }
                
            }

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
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }


        return result;
    }
    
    private void initTransients(Map<Long, Object> objs) {
        initTransients(new ArrayList(objs.values()));
    }

    private void initTransients(ArrayList<Object> objs) {
        for (int i = 0; i < objs.size(); i++) {
            initTransients(objs.get(i));
        }
    }

    private void initTransients(Object obj) {
        if (obj instanceof KnowsDB) {
            ((KnowsDB) obj).setDataBase(DataBase.getDataBase());
        }
    }
}
