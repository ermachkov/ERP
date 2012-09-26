/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.rpc;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.unet.rpc.client.NIOClient;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class JRPC {

    public static InetSocketAddress newClientAddress(String clientBindHost) {
        double port = Math.random() * 64000d;
        if (port < 10240d) {
            port = port + 10240d;
        }

        return new InetSocketAddress(clientBindHost, (int) port);
    }

    public static Object getResult(InetSocketAddress clientAddress,
            InetSocketAddress serverAddress, Mediator mediator, int timeout) {
        Object result = null;

        mediator.setClientAddress(clientAddress);
        NIOClient client = new NIOClient(clientAddress);
        if (!client.isDeviceUp()) {
            return result;
        }

        try {
            Mediator b = (Mediator) client.send(mediator, serverAddress, timeout);
            if (b == null) {
                return result;
            }

            Mediator requestMediator = new Mediator(mediator.getMark());
            Mediator res = (Mediator) client.send(requestMediator, serverAddress, timeout);
            if (res.getStatus() == Mediator.INVOKED) {
                result = res.getResult();
            }
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.err.println(e);
            Logger.getGlobal().log(Level.INFO, Objects.toString(e));
            return result;   
        }
        
        return result;
    }
}
