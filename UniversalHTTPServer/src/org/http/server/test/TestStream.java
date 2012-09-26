/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.http.server.test;

import org.http.stream.StreamSocket;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class TestStream {
    
    public static void main(String args[]){
        TestStream testStream = new TestStream();
        testStream.start();
    }
    
    private void start(){
        StreamSocket ss = new StreamSocket(8989, 10);
        ss.start();
    }
    
}
