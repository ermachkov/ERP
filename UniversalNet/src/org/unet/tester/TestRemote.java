/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.tester;

import java.net.InetSocketAddress;
import org.unet.rpc.JRPC;
import org.unet.rpc.Mediator;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class TestRemote {

    public static void main(String args[]) {
        TestRemote tr = new TestRemote();
        //tr.testData();
        tr.testXML();
    }
    
    private void testXML() {
        InetSocketAddress serverAddress = new InetSocketAddress("172.16.0.1", 20000);
        InetSocketAddress clientAddress = JRPC.newClientAddress("172.16.0.1");
        Mediator mediator = new Mediator(
                "ru.granplat.terminal.node.rpc.TerminalNodeHandler",
                "getOperatorsXML",
                new Object[]{59, 235});

            Object res = JRPC.getResult(clientAddress, serverAddress, mediator, 30);
            System.out.println(res);
    }

    private void testData() {
        InetSocketAddress serverAddress = new InetSocketAddress("172.16.0.1", 20000);
        InetSocketAddress clientAddress = JRPC.newClientAddress("172.16.0.1");
        Mediator mediator = new Mediator(
                "ru.granplat.terminal.node.rpc.TerminalNodeHandler",
                "getServiceData",
                new Object[]{10, 20});

        for (int i = 0; i < 10; i++) {
            Object res = JRPC.getResult(clientAddress, serverAddress, mediator, 3);
            System.out.println(i + ". " + res);
        }

    }
}
