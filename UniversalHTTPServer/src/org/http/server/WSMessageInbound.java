/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.http.json.JSONException;
import org.http.json.JSONObject;
import org.uui.webkit.WebKitEventBridge;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class WSMessageInbound extends MessageInbound {

    //private WsOutbound outbound;
    private String sessionId;

    public WSMessageInbound(String sessionId, int byteBufferMaxSize, int charBufferMaxSize) {
        super();
        this.sessionId = sessionId;
        setByteBufferMaxSize(byteBufferMaxSize);
        setCharBufferMaxSize(charBufferMaxSize);
    }

    @Override
    protected synchronized void onOpen(WsOutbound outbound) {
        try {
            System.out.println("onOpen " + outbound);
        WebSocketBundle.getInstance().addWebSocket(sessionId, outbound);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "" + outbound, e);
        }
    }

    @Override
    protected void onBinaryMessage(ByteBuffer bb) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected synchronized void onTextMessage(CharBuffer cb) {
        try {
            System.out.println("WSMessageInbound onTextMessage: " + URLDecoder.decode(cb.toString(), "UTF-8"));
            JSONObject json = new JSONObject(URLDecoder.decode(cb.toString(), "UTF-8"));
            if (json.has("sess") && json.has("command")) {
                switch (json.getString("command")) {
//                    case "register":
//                        WebSocketBundle.getInstance().addWebSocket(json.getString("sess"), outbound);
//                        Logger.getGlobal().log(Level.INFO, "Added WsOutbound: "
//                                + "{0} with key: {1}",
//                                new Object[]{outbound, json.getString("sess")});
//                        break;

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
            Logger.getGlobal().log(Level.WARNING, "onTextMessage: " + cb, e);
        }

    }
}
