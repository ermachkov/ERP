/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unet.rpc.server;

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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.unet.rpc.Mediator;
import org.unet.rpc.MediatorCollector;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class NIOServer {

    private String bindHost;
    private int port;
    private ServerSocketChannel serverSocketChannel;
    private InetSocketAddress serverAddress;
    private boolean isServerStarted = false;
    private Level DEBUG_LEVEL = Level.FINE;

    public NIOServer(String bindHost, int port) {
        this.bindHost = bindHost;
        this.port = port;
    }

    public void startServer() {
        Executors.newSingleThreadExecutor().execute(server());
    }

    public void setDebugLevel(Level level) {
        DEBUG_LEVEL = level;
    }

    public boolean isStarted() {
        return isServerStarted;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    private Runnable server() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    boolean isDeviceEnable = false;
                    while (!isDeviceEnable) {
                        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                        while (networkInterfaces.hasMoreElements()) {
                            NetworkInterface ni = networkInterfaces.nextElement();
                            Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                            while (inetAddresses.hasMoreElements()) {
                                InetAddress inetAddress = inetAddresses.nextElement();
                                Logger.getGlobal().log(DEBUG_LEVEL, 
                                        "find address = {0}, bind host = {1}", 
                                        new Object[]{inetAddress.getHostAddress(), bindHost});
                                
                                if (!inetAddress.getHostAddress().trim().equals(bindHost.trim())) {
                                    Logger.getGlobal().log(DEBUG_LEVEL, "continue scan interface...");
                                    continue;
                                }

                                if (ni.isUp()) {
                                    isDeviceEnable = true;
                                    Logger.getGlobal().log(DEBUG_LEVEL, "interface {0} is up!", ni);
                                }
                            }
                        }

                        LockSupport.parkNanos(1000000000);
                    }

                    Selector selector = Selector.open();
                    serverSocketChannel = ServerSocketChannel.open();
                    serverAddress = new InetSocketAddress(bindHost, port);
                    serverSocketChannel = serverSocketChannel.bind(serverAddress);
                    serverSocketChannel.configureBlocking(false);
                    serverSocketChannel.register(selector, serverSocketChannel.validOps());
                    isServerStarted = true;

                    while (true) {
                        try {
                            // Wait for an event
                            selector.select();
                        } catch (IOException e) {
                            // Handle error with selector
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

                            processSelectionKey(selKey);
                        }
                    }

                } catch (IOException ex) {
                    Logger.getGlobal().log(Level.WARNING, bindHost, ex);
                }
            }
        };

        return r;
    }

    private void processSelectionKey(SelectionKey selKey) {
        if (selKey.isValid() && selKey.isAcceptable()) {
            Logger.getGlobal().log(DEBUG_LEVEL, "Server isAcceptable");
            ServerSocketChannel ssChannel = (ServerSocketChannel) selKey.channel();
            SocketChannel sChannel;
            try {
                sChannel = ssChannel.accept();
                sChannel.configureBlocking(false);
                Logger.getGlobal().log(DEBUG_LEVEL, "Server SocketChannel {0}", sChannel);
                readHandler(sChannel);

            } catch (Exception e) {
                // Handle error with channel and unregister
                Logger.getGlobal().log(Level.WARNING, bindHost, e);
                selKey.cancel();
                return;
            }
        }
    }

    private void readHandler(SocketChannel socketChannel) {
        boolean hasMore = true;
        Mediator returnMediator = null;
        byte[] responseByte = null;
        int sendedBytesLen = 0;

        Selector selector = null;
        try {
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

            while (hasMore) {
                try {
                    // Wait for an event
                    selector.select();
                } catch (IOException e) {
                    Logger.getGlobal().log(Level.WARNING, Objects.toString(socketChannel), e);
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

                    if (selKey.isValid() && selKey.isReadable()) {
                        ByteBuffer totalByteBuffer;
                        totalByteBuffer = ByteBuffer.allocate(10485760);
                        ByteBuffer byteBufferRead = ByteBuffer.allocate(1024);
                        int byteCount = 0;
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

                        if (bytes.length > 0) {
                            Object object;
                            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                                    GZIPInputStream gs = new GZIPInputStream(bais);
                                    ObjectInputStream in = new ObjectInputStream(gs)) {
                                object = in.readObject();
                            }
                            Logger.getGlobal().log(DEBUG_LEVEL, "NIOServer object = {0}", object);

                            if (object instanceof Mediator) {
                                Mediator mediator = (Mediator) object;
                                if (mediator.getType() == Mediator.ADD) {
                                    MediatorCollector.getInstance().addMediator((Mediator) object);
                                    returnMediator = mediator;
                                }

                                if (mediator.getType() == Mediator.GET_RESULT) {
                                    Mediator resultMediator = MediatorCollector.getInstance().getResult(mediator.getMark());
                                    if (resultMediator == null) {
                                        returnMediator = mediator;
                                    } else {
                                        returnMediator = resultMediator;
                                    }
                                }
                            }
                            socketChannel.shutdownInput();
                            selKey.interestOps(SelectionKey.OP_WRITE);
                        }

                        Logger.getGlobal().log(DEBUG_LEVEL, "return Mediator  = {0}", returnMediator);
                        if (returnMediator == null) {
                            Logger.getGlobal().log(Level.FINE, "Readable returnMediator = null");
                        }
                    }

                    if (selKey.isValid() && selKey.isWritable()) {
                        if (returnMediator != null) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            try (GZIPOutputStream gz = new GZIPOutputStream(baos, true);
                                    ObjectOutputStream oout = new ObjectOutputStream(gz)) {
                                oout.writeObject(returnMediator);
                                oout.flush();
                                oout.close();
                                gz.flush();
                                gz.close();
                            }

                            responseByte = baos.toByteArray();

                            if (sendedBytesLen < responseByte.length) {
                                ByteBuffer byteBufferResponse =
                                        ByteBuffer.wrap(responseByte,
                                        sendedBytesLen,
                                        responseByte.length - sendedBytesLen);
                                sendedBytesLen += socketChannel.write(byteBufferResponse);
                                Logger.getGlobal().log(DEBUG_LEVEL, "Sended data "
                                        + "chunk size = {0} of data size = {1}",
                                        new Object[]{sendedBytesLen, responseByte.length});
                            } else {
                                Logger.getGlobal().log(DEBUG_LEVEL, "Send all data "
                                        + "size = {0} of data size = {1}",
                                        new Object[]{sendedBytesLen, responseByte.length});
                                socketChannel.close();
                                selector.close();
                                hasMore = false;
                            }
                        }

                        if (returnMediator == null) {
                            Logger.getGlobal().log(Level.FINE, "Writable returnMediator = null");
                        }
                    }
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            Logger.getGlobal().log(Level.WARNING, Objects.toString(socketChannel), e);
            try {
                if (socketChannel != null) {
                    socketChannel.close();
                }

                if (selector != null) {
                    selector.close();
                }

            } catch (Exception ex) {
                Logger.getGlobal().log(Level.WARNING, null, ex);
            }

        }
    }
}
