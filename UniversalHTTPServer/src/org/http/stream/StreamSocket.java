/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class StreamSocket {
    
    private int port = 8989, connections = 10;
    
    public StreamSocket(int port, int connections){
        this.port = port;
        this.connections = connections;
    }
    
    public void start(){
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port, connections);
                    System.out.println("Socket await connections...");
                    Socket socket = serverSocket.accept();
                    System.out.println("New connection established");
                    Executors.newSingleThreadExecutor().execute(incomeSocket(socket));
                    
                } catch (IOException ex) {
                    Logger.getLogger(StreamSocket.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        
        Executors.newSingleThreadExecutor().execute(r);
    }
    
    private Runnable incomeSocket(final Socket socket){
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    InputStream is = socket.getInputStream();
                    byte b[] = new byte[8196];
                    int len = 0;
                    while((len = is.read(b)) != -1){
                        System.out.println(b);
                    }
                    
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                }
            }
        };
        
        return r;
    }
}
