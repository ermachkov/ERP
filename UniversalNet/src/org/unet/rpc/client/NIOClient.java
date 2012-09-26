/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.rpc.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class NIOClient {

    private InetSocketAddress clientAddress;
    private Level DEBUG_LEVEL = Level.INFO;

    public NIOClient(InetSocketAddress clientAddress) {
        this.clientAddress = clientAddress;
    }
    
    public void setDebugLevel(Level level){
        DEBUG_LEVEL = level;
    }

    public boolean isDeviceUp() {
        boolean isDeviceEnable = false;

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.getHostAddress().equals(clientAddress.getHostString())) {
                        continue;
                    }

                    if (ni.isUp()) {
                        isDeviceEnable = true;
                        Logger.getGlobal().log(DEBUG_LEVEL, "interface {0} is up!", ni);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "" + clientAddress, e);
        }

        return isDeviceEnable;
    }

    public Callable<Object> sendable(final Object object, final InetSocketAddress serverAddress) {
        Callable call = new Callable() {

            @Override
            public Object call() throws Exception {
                boolean result;
                Object returnObject = null;
                Selector selector = null;
                SocketChannel socketChannel = null;
                try {
                    selector = Selector.open();
                    socketChannel = SocketChannel.open(serverAddress);
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    result = socketChannel.isConnected();
                    Logger.getGlobal().log(DEBUG_LEVEL, "Client isConnected = {0}", result);

                    while (true) {
                        try {
                            // Wait for an event
                            selector.select();
                            
                        } catch (IOException e) {
                            Logger.getGlobal().log(Level.WARNING, Objects.toString(object), e);
                            socketChannel.close();
                            selector.close();
                            break;
                        }

                        // Get list of selection keys with pending events
                        Iterator it = selector.selectedKeys().iterator();

                        // Process each key at a time
                        while (it.hasNext()) {
                            // Get the selection key
                            SelectionKey selKey = (SelectionKey) it.next();
                            Logger.getGlobal().log(Level.FINE, "selKey = {0}", selKey);

                            // Remove it from the list to indicate that it is being processed
                            it.remove();

                            try {
                                if (selKey.isValid() && selKey.isReadable()) {
                                    Logger.getGlobal().log(DEBUG_LEVEL, "Client SelectionKey isReadable");
                                    ByteBuffer totalByteBuffer;
                                    totalByteBuffer = ByteBuffer.allocate(10485760);
                                    ByteBuffer byteBufferRead = ByteBuffer.allocate(1024);
                                    int byteCount;
                                    while ((byteCount = socketChannel.read(byteBufferRead)) >= 0) {
                                        if (byteCount > 0) {
                                            byteBufferRead.flip();
                                            totalByteBuffer.put(byteBufferRead);
                                            byteBufferRead.clear();
                                        }
                                    }

                                    totalByteBuffer.flip();
                                    byte[] bytes = new byte[totalByteBuffer.remaining()];
                                    totalByteBuffer.get(bytes, 0, bytes.length);
                                    totalByteBuffer.clear();

                                    Logger.getGlobal().log(DEBUG_LEVEL, "GZIP object lenght = {0}", bytes.length);
                                    if (bytes.length > 0) {
                                        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                                        GZIPInputStream gs = new GZIPInputStream(bais);
                                        ObjectInputStream in = new ObjectInputStream(gs);
                                        try {
                                            returnObject = in.readObject();
                                            Logger.getGlobal().log(DEBUG_LEVEL, "Client object = {0}", returnObject);
                                            socketChannel.close();
                                            selector.close();
                                            return returnObject;

                                        } catch (IOException | ClassNotFoundException e) {
                                            // Handle error with channel and unregister
                                            Logger.getGlobal().log(Level.WARNING, null, e);
                                            selKey.cancel();
                                            socketChannel.close();
                                            selector.close();
                                        }
                                    }
                                }

                                if (selKey.isValid() && selKey.isWritable()) {
                                    Logger.getGlobal().log(DEBUG_LEVEL, "Client SelectionKey isWritable");
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    try (GZIPOutputStream gz = new GZIPOutputStream(baos);
                                            ObjectOutputStream oout = new ObjectOutputStream(gz)) {
                                        oout.writeObject(object);
                                        oout.flush();
                                        gz.flush();
                                        gz.close();
                                    }

                                    ByteBuffer byteBuffer = ByteBuffer.wrap(baos.toByteArray());
                                    socketChannel.write(byteBuffer);
                                    socketChannel.shutdownOutput();
                                    selKey.interestOps(SelectionKey.OP_READ);
                                }
                            } catch (IOException e) {
                                // Handle error with channel and unregister
                                Logger.getGlobal().log(Level.WARNING, null, e);
                                selKey.cancel();
                            }
                        }
                    }
                } catch (IOException ex) {
                    if(socketChannel != null){
                        socketChannel.close();
                    }
                    
                    if(selector != null){
                        selector.close();
                    }
                    Logger.getGlobal().log(Level.WARNING, "" + serverAddress, ex);

                } finally {
                    return returnObject;
                }
            }
        };

        return call;
    }

    public Object send(Object object, InetSocketAddress serverAddress, int timeout) throws InterruptedException, ExecutionException, TimeoutException {
        Future future = Executors.newSingleThreadExecutor().submit(sendable(object, serverAddress));
        return future.get(timeout, TimeUnit.SECONDS);
    }
}
