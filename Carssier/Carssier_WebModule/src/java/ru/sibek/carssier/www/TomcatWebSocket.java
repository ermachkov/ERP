/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.carssier.www;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;

/**
 *
 * @author developer
 */
public class TomcatWebSocket extends WebSocketServlet {

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
            result = Integer.parseInt(val);
        } catch (Exception x) {
        }
        return result;
    }

    @Override
    protected StreamInbound createWebSocketInbound(String protocol, HttpServletRequest request) {
        System.out.println("Protocol " + protocol + ", HttpServletRequest " + request.getRequestedSessionId());
        return new WSMessageInbound(request.getRequestedSessionId(), byteBufSize, charBufSize);
    }
}
