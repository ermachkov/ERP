/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.server;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.catalina.websocket.WsOutbound;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class WebSocketBundle {

    private static WebSocketBundle self = null;
    private ConcurrentHashMap<String, WsOutbound> wsMap = new ConcurrentHashMap<>();

    private WebSocketBundle() {
        //
    }

    public synchronized static WebSocketBundle getInstance() {
        if (self == null) {
            self = new WebSocketBundle();
        }

        return self;
    }

    public void addWebSocket(String webSocketName, WsOutbound outbound) {
        wsMap.put(webSocketName, outbound);
    }

    public void send(String webSocketName, String message) {
        System.out.println("Try send ws message to: " + webSocketName + ", " + message);
        try {
            if (!wsMap.containsKey(webSocketName)) {
                System.out.println("WebSocket with name " + webSocketName + " is absent.");
                return;
            }

            wsMap.get(webSocketName).writeTextMessage(CharBuffer.wrap(message));
            
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, message, ex);
        }
    }
}
