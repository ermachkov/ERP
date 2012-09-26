/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uws.server;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.uui.webkit.WebKitEventBridge;
import org.uws.json.JSONException;
import org.uws.json.JSONObject;

/**
 *
 * @author developer
 */
public class UniversalWebSocket extends WebSocketServer{
    
    private static UniversalWebSocket self = null;
    
    public UniversalWebSocket(int port){
        super(new InetSocketAddress(port));
        super.start();
        System.out.println("UniversalWebSocket started on port " + port);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("onOpen " + conn + ", " + handshake);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        WebSocketBundle.getInstance().removeWebSocket(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            System.out.println("WSMessageInbound onTextMessage: " + URLDecoder.decode(message, "UTF-8"));
            JSONObject json = new JSONObject(URLDecoder.decode(message, "UTF-8"));
            if (json.has("sess") && json.has("command")) {
                switch (json.getString("command")) {
                    case "register":
                        WebSocketBundle.getInstance().addWebSocket(json.getString("sess"), conn);
                        Logger.getGlobal().log(Level.INFO, "Added WsOutbound: "
                                + "{0} with key: {1}",
                                new Object[]{conn, json.getString("sess")});
                        break;

                    case "execute":
                        if(json.has("parameters")){
                            JSONObject jsonParameters = json.getJSONObject("parameters");
                            jsonParameters.put("session", json.getString("sess"));
                            WebKitEventBridge.getInstance().pushEventToComponent(
                                json.getString("sess"),    
                                jsonParameters.getString("identificator"),
                                jsonParameters.toString());
                        }
                }
            }

        } catch (JSONException | UnsupportedEncodingException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
