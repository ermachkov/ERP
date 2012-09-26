/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.tester;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;
import org.unet.rpc.JRPC;
import org.unet.rpc.Mediator;
import org.unet.rpc.server.NIOServer;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class LocalTester {
    
    public LocalTester(){
        runServer();
    }
    
    private void runServer(){
        NIOServer server = new NIOServer("127.0.0.1", 18800);
        server.startServer();
        while(!server.isStarted()){
            System.out.println("wait...");
            LockSupport.parkNanos(100000000);
        }
        
        InetSocketAddress clientAddress = JRPC.newClientAddress("127.0.0.1");
        
        Map m = new HashMap();
        m.put("key", "value");
        Mediator mediator = new Mediator("org.unet.tester.Test", "setInfo", new Object[]{m});
        Object result = JRPC.getResult(clientAddress, server.getServerAddress(), mediator, 3);
        System.out.println("Result 1 = " + result);
        
        Mediator mediator2 = new Mediator("org.unet.tester.Test", "setInfo", new Object[]{m, "Fucker"});
        result = JRPC.getResult(clientAddress, server.getServerAddress(), mediator2, 3);
        System.out.println("Result 2 = " + result);
    }
    
    public static void main(String args[]){
        LocalTester localTester = new LocalTester();
    }
    
}
