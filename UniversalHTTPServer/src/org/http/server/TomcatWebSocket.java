/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.server;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class TomcatWebSocket extends WebSocketServlet {
    
    final static long serialVersionUID = 1L;
    
    private volatile int byteBufSize;
    private volatile int charBufSize;
    
    @Override
    public void init() throws ServletException {
        super.init();
        byteBufSize = getInitParameterIntValue("byteBufferMaxSize", 2097152);
        charBufSize = getInitParameterIntValue("charBufferMaxSize", 2097152);
    }
    
    public int getInitParameterIntValue(String name, int defaultValue) {
        String val = this.getInitParameter(name);
        int result = defaultValue;
        try {
            if(val != null){
                result = Integer.parseInt(val);
            }
        } catch (NumberFormatException e) {
            Logger.getGlobal().log(Level.WARNING, val, e);
        }
        return result;
    }

    @Override
    protected StreamInbound createWebSocketInbound(String protocol, HttpServletRequest request) {
        System.out.println("Protocol " + protocol + ", HttpServletRequest " + request.getRequestedSessionId());
        return new WSMessageInbound(request.getRequestedSessionId(), byteBufSize, charBufSize);
    }
}
