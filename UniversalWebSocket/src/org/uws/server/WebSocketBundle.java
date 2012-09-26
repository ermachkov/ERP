/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uws.server;

import java.util.concurrent.ConcurrentHashMap;
import org.java_websocket.WebSocket;

/**
 *
 * @author developer
 */
public class WebSocketBundle {

    private static WebSocketBundle self = null;
    private ConcurrentHashMap<String, WebSocket> wsMap = new ConcurrentHashMap<>();

    private WebSocketBundle() {
        //
    }

    public synchronized static WebSocketBundle getInstance() {
        if (self == null) {
            self = new WebSocketBundle();
        }

        return self;
    }

    public void addWebSocket(String webSocketName, WebSocket outbound) {
        wsMap.put(webSocketName, outbound);
    }
    
    public void removeWebSocket(WebSocket outbound){
        //
    }

    public void send(String webSocketName, String message) {
        System.out.println("Try send ws message to: " + webSocketName + ", " + message);
        if (!wsMap.containsKey(webSocketName)) {
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWebSocket with name " + webSocketName + " is absent.");
            return;
        }

        wsMap.get(webSocketName).send(message);
    }
}
