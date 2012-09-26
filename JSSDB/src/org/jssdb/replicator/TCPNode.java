/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.replicator;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;
import org.jssdb.event.Event;
import org.jssdb.event.EventListenerList;
import org.jssdb.event.JSSDBEventListener;
import org.jssdb.handler.Handler;
import org.jssdb.handler.MetaInfo;
import org.jssdb.revision.Revision;
import org.jssdb.utils.MetaInfoData;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class TCPNode {

    private boolean hasMore = true;
    private String host;
    private int port;
    public static final int SUPER = 0, NODE = 1;
    private int nodeMode = -1;
    private InetSocketAddress socketAddress, superSocketAddress, socketBindAddress;
    private static final int STATE_UNREGISTERED = 0, STATE_REGISTERED = 1;
    private int state = STATE_UNREGISTERED;
    private long startRegisterTime, registerTime;
    private CopyOnWriteArraySet<SocketAddress> nodesAddressSet = new CopyOnWriteArraySet();
    protected EventListenerList listenerList = new EventListenerList();
    private static TCPNode self = null;

    public synchronized static TCPNode getSuperNode() {
        if (self == null) {
            self = new TCPNode(TCPNode.SUPER);
        }

        return self;
    }

    public synchronized static TCPNode getNode() {
        if (self == null) {
            self = new TCPNode(TCPNode.NODE);
        }

        return self;
    }

    public void addJSSDBEventListener(JSSDBEventListener listener) {
        listenerList.add(JSSDBEventListener.class, listener);
    }

    private void fireCustomEvent(Event evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == JSSDBEventListener.class) {

                if (evt.getAction() == 3 || evt.getAction() == 4) {
                    ((JSSDBEventListener) listeners[i + 1]).sync(evt);
                }
            }
        }
    }

    private TCPNode(int mode) {
        nodeMode = mode;

        if (nodeMode == TCPNode.SUPER) {
            host = DBProperties.getInstance().getSuperNodeHost();
            port = DBProperties.getInstance().getSuperNodePort();
            socketAddress = new InetSocketAddress(host, port);
            socketBindAddress = new InetSocketAddress(DBProperties.getInstance().getSuperNodeBindHost(), port);
        }

        if (nodeMode == TCPNode.NODE) {
            host = DBProperties.getInstance().getNodeHost();
            port = DBProperties.getInstance().getNodePort();
            socketAddress = new InetSocketAddress(host, port);
            socketBindAddress = new InetSocketAddress(DBProperties.getInstance().getNodeBindHost(), port);

            superSocketAddress = new InetSocketAddress(
                    DBProperties.getInstance().getSuperNodeHost(),
                    DBProperties.getInstance().getSuperNodePort());

            Runnable register = new Runnable() {

                @Override
                public void run() {
                    startRegisterTime = new Date().getTime();
                    Replica r = new Replica();

                    if (registerTime == 0) {
                        state = TCPNode.STATE_UNREGISTERED;

                    } else {
                        long t = Math.abs(registerTime - startRegisterTime);
                        t = Math.abs((DBProperties.getInstance().getHeartbeat() * 1000) - t);
                        if (t > 3000) {
                            state = TCPNode.STATE_UNREGISTERED;
                        } else {
                            state = TCPNode.STATE_REGISTERED;
                        }
                    }

                    if (state == TCPNode.STATE_UNREGISTERED) {
                        r.setType(Replica.REGISTER_REQUEST);
                    } else {
                        r.setType(Replica.PING_REQUEST);
                    }
                    r.setSocketAddress(socketAddress);
                    sender(r, superSocketAddress);
                }
            };
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                    register,
                    0,
                    DBProperties.getInstance().getHeartbeat(),
                    TimeUnit.SECONDS);
        }

        Executors.newSingleThreadExecutor().execute(runServer());
    }

    private Runnable runServer() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket();
                    serverSocket.bind(socketBindAddress);
                    Logger.getGlobal().log(Level.INFO, "Replica server running {0}", socketBindAddress);
                    Logger.getGlobal().log(Level.INFO, "Replica server mode  {0}", socketBindAddress);

                    while (hasMore) {
                        Socket socket = serverSocket.accept();
                        Logger.getGlobal().log(Level.INFO, "New connection accepted {0}:{1}", new Object[]{socket.getInetAddress(), socket.getPort()});
                        try {
                            Executors.newSingleThreadExecutor().execute(replicaExtractor(socket));
                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.WARNING, null, e);
                        }
                    }
                } catch (IOException e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                }
            }
        };

        return r;
    }

    private Runnable replicaExtractor(final Socket socket) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                InputStream is = null;
                try {
                    is = socket.getInputStream();
                    byte[] b = new byte[1024];
                    int len;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while ((len = is.read(b)) != -1) {
                        baos.write(Arrays.copyOfRange(b, 0, len));
                    }
                    is.close();
                    socket.close();

                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    Replica replica = (Replica) new ObjectInputStream(bais).readObject();
                    Logger.getGlobal().log(Level.INFO, "Extracted replica from node {0} = {1}", new Object[]{socket, replica});
                    replicaHandler(replica);

                } catch (IOException | ClassNotFoundException e) {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception ex) {
                        }
                    }

                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (Exception ex) {
                        }
                    }

                    Logger.getGlobal().log(Level.WARNING, host, e);
                }
            }
        };

        return r;
    }

    private void replicaHandler(Replica replica) {
        if (nodeMode == TCPNode.SUPER) {
            switch (replica.getType()) {
                case Replica.REGISTER_REQUEST:
                    nodesAddressSet.add(replica.getSocketAddress());
                    Replica response = new Replica();
                    response.setSocketAddress(socketAddress);
                    response.setType(Replica.REGISTER_OK_RESPONSE);
                    //response.setCheckSum(getCheckSum());
                    response.setCheckSums(getCheckSums());
                    sender(response, replica.getSocketAddress());
                    break;

                case Replica.PING_REQUEST:
                    response = new Replica();
                    response.setSocketAddress(socketAddress);
                    response.setType(Replica.PING_OK_RESPONSE);
                    sender(response, replica.getSocketAddress());
                    break;

                case Replica.CHECKSUM_NOT_EQUAL:
                    //Executors.newSingleThreadExecutor().execute(fullNodeUpdate(replica.getSocketAddress()));
                    incrementalUpdate(replica);
                    break;

                case Replica.REQUEST_INCREMENTAL_FILE_LIST_FROM_SUPER:
                    Logger.getGlobal().log(Level.INFO, "Get request from node "
                            + "for incremental file list {0} {1}",
                            new Object[]{replica.getClassName(),
                                replica.getRequestIncrementalFiles()});
                    for (IncrementalFile incrementalFile : replica.getRequestIncrementalFiles()) {
                        if (incrementalFile.getAction() == IncrementalFile.DELETE) {
                            System.out.println("DELETE!!! " + incrementalFile);

                        } else {
                            try {
                                Replica incrementalReplica = new Replica();
                                incrementalReplica.setType(Replica.INCREMENTAL_REPLICA);
                                incrementalReplica.setAction(incrementalFile.getAction());
                                incrementalReplica.setClassName(replica.getClassName());
                                incrementalReplica.setId(Long.parseLong(incrementalFile.getName()));
                                incrementalReplica.setRevision(
                                        Revision.getInstance().getRevision(
                                        replica.getClassName(),
                                        Long.parseLong(incrementalFile.getName())));
                                Path p = Paths.get(
                                        DBProperties.getInstance().getPathToDB(),
                                        replica.getClassName(),
                                        incrementalFile.getName());
                                incrementalReplica.setTimestamp(p.toFile().lastModified());
                                incrementalReplica.setData(Files.readAllBytes(p));
                                incrementalReplica.setSocketAddress(superSocketAddress);

                                sender(incrementalReplica, replica.getSocketAddress());
                                Logger.getGlobal().log(Level.INFO,
                                        "Try send INCREMENTAL_REPLICA {0}",
                                        incrementalReplica);

                            } catch (NumberFormatException | IOException e) {
                                Logger.getGlobal().log(Level.WARNING, null, e);
                            }

                        }
                    }
                    break;

                case Replica.FIRST_SYNC:
                    if (replica.getAction() == Replica.ADD) {
                        Path pDir = Paths.get(DBProperties.getInstance().getPathToDB(),
                                replica.getClassName());
                        if (!pDir.toFile().exists()) {
                            boolean success = pDir.toFile().mkdir();
                            if (!success) {
                                Logger.getGlobal().log(Level.WARNING, "Can't create dir {0}", pDir);
                            }
                        }

                        Path p = Paths.get(DBProperties.getInstance().getPathToDB(),
                                replica.getClassName(), "" + replica.getId());
                        try {
                            long rev = Revision.getInstance().getRevision(replica.getClassName(), replica.getId());
                            if (rev < replica.getRevision()) {
                                Files.write(p, replica.getData());
                                Files.setLastModifiedTime(p, FileTime.fromMillis(replica.getTimestamp()));
                                Revision.getInstance().setRevision(
                                        replica.getClassName(),
                                        replica.getId(),
                                        replica.getRevision());
                            } else if (rev == replica.getRevision()) {
                                MetaInfoData mid = MetaInfo.getDefault().getMetaInfo(replica.getId(), replica.getClassName());
                                if (mid.getLastModified() < replica.getTimestamp()) {
                                    Files.write(p, replica.getData());
                                    Files.setLastModifiedTime(p, FileTime.fromMillis(replica.getTimestamp()));
                                    Revision.getInstance().setRevision(
                                            replica.getClassName(),
                                            replica.getId(),
                                            replica.getRevision());
                                }
                            }

                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.WARNING, null, e);
                        }

                    } else if (replica.getAction() == Replica.DELETE_LIST) {
                        for (long id : replica.getDeletedList()) {
                            Path p = Paths.get(DBProperties.getInstance().getPathToDB(),
                                    replica.getClassName(), "" + id);
                            if (p.toFile().exists()) {
                                p.toFile().delete();
                                Revision.getInstance().setDeleted(p.toString());
                            }
                        }
                    }

                    break;

                case Replica.FIRST_SYNC_ENDED:
                    if (getCheckSum() != replica.getCheckSum()) {
                        //Executors.newSingleThreadExecutor().execute(fullNodeUpdate(replica.getSocketAddress()));
                    }
                    break;

                case Replica.SYNC:
                    Path p = Paths.get(DBProperties.getInstance().getPathToDB(),
                            replica.getClassName(), "" + replica.getId());

                    Path pDir = Paths.get(DBProperties.getInstance().getPathToDB(),
                            replica.getClassName());
                    if (!pDir.toFile().exists()) {
                        boolean success = pDir.toFile().mkdir();
                        if (!success) {
                            Logger.getGlobal().log(Level.WARNING, "Can't create dir {0}", pDir);
                        }
                    }

                    if (replica.getAction() == Replica.DELETE) {
                        if (p.toFile().exists()) {
                            p.toFile().delete();
                            Revision.getInstance().setDeleted(p.toString());
                        }

                    } else if (replica.getAction() == Replica.ADD) {
                        if (!p.toFile().exists()) {
                            try {
                                Files.write(p, replica.getData());
                                Revision.getInstance().setRevision(
                                        replica.getClassName(),
                                        replica.getId(), replica.getRevision());

                            } catch (Exception e) {
                                Logger.getGlobal().log(Level.WARNING, null, e);
                            }
                        }

                    } else if (replica.getAction() == Replica.MODIFY) {
                        try {
                            long rev = Revision.getInstance().getRevision(
                                    replica.getClassName(), replica.getId());
                            if (rev < replica.getRevision()) {
                                Files.write(p, replica.getData());
                                Revision.getInstance().setRevision(
                                        replica.getClassName(),
                                        replica.getId(), replica.getRevision());

                            } else if (rev == replica.getRevision()) {
                                MetaInfoData mid = MetaInfo.getDefault().getMetaInfo(replica.getId(), replica.getClassName());
                                if (mid.getLastModified() < replica.getTimestamp()) {
                                    Files.write(p, replica.getData());
                                    Files.setLastModifiedTime(p, FileTime.fromMillis(replica.getTimestamp()));
                                    Revision.getInstance().setRevision(
                                            replica.getClassName(),
                                            replica.getId(),
                                            replica.getRevision());
                                }
                            }

                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.WARNING, null, e);
                        }
                    }

                    int action = -1;
                    switch (replica.getAction()) {
                        case Replica.ADD:
                            action = 0;
                            break;

                        case Replica.MODIFY:
                            action = 1;
                            break;

                        case Replica.DELETE:
                            action = 2;
                            break;
                    }
                    circuitReplication(
                            p.toFile(),
                            replica.getClassName(),
                            replica.getId(),
                            replica.getRevision(),
                            action);

                    break;

            }
        }

        if (nodeMode == TCPNode.NODE) {
            switch (replica.getType()) {
                case Replica.REGISTER_OK_RESPONSE:
                    registerTime = new Date().getTime();
                    Replica response = new Replica();
                    response.setSocketAddress(socketAddress);
                    if (getCheckSum() != replica.getCheckSums().get("totalCheckSum").longValue()) {
                        fireCustomEvent(new Event(Event.SYNC_START));
                        response.setType(Replica.CHECKSUM_NOT_EQUAL);
                        response.setCheckSums(getCheckSums());

                    } else {
                        fireCustomEvent(new Event(Event.SYNC_COMPLITE));
                        response.setType(Replica.CHECKSUM_EQUAL);
                    }
                    sender(response, superSocketAddress);
                    break;

                case Replica.INCREMENTAL_REPLICA:
                    try {
                        long rev = Revision.getInstance().getRevision(
                                replica.getClassName(),
                                replica.getId());
                        if (replica.getAction() == Replica.ADD) {
                            Path pDir = Paths.get(
                                    DBProperties.getInstance().getPathToDB(),
                                    replica.getClassName());
                            if (!pDir.toFile().exists()) {
                                pDir.toFile().mkdir();
                            }

                            Path p = Paths.get(
                                    DBProperties.getInstance().getPathToDB(),
                                    replica.getClassName(),
                                    "" + replica.getId());
                            Files.write(p, replica.getData());
                            Revision.getInstance().setRevision(
                                    replica.getClassName(),
                                    replica.getId(),
                                    replica.getRevision());
                            Logger.getGlobal().log(Level.INFO, "Write incremental replica {0}", replica);
                            
                        } else {
                            if (rev < replica.getRevision()) {
                                Path pDir = Paths.get(
                                        DBProperties.getInstance().getPathToDB(),
                                        replica.getClassName());
                                if (!pDir.toFile().exists()) {
                                    pDir.toFile().mkdir();
                                }

                                Path p = Paths.get(
                                        DBProperties.getInstance().getPathToDB(),
                                        replica.getClassName(),
                                        "" + replica.getId());
                                Files.write(p, replica.getData());
                                Revision.getInstance().setRevision(
                                        replica.getClassName(),
                                        replica.getId(),
                                        replica.getRevision());
                                Logger.getGlobal().log(Level.INFO, "Write incremental replica {0}", replica);
                            }
                        }

                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, null, e);
                    }

                    break;

                case Replica.INCREMENTAL_FILE_LIST:
                    nodeIncrementalFileListHandler(replica);
                    break;

                case Replica.PING_OK_RESPONSE:
                    registerTime = new Date().getTime();
                    break;

                case Replica.FIRST_SYNC:
                    if (replica.getAction() == Replica.ADD) {
                        Path pDir = Paths.get(DBProperties.getInstance().getPathToDB(),
                                replica.getClassName());
                        if (!pDir.toFile().exists()) {
                            boolean success = pDir.toFile().mkdir();
                            if (!success) {
                                Logger.getGlobal().log(Level.WARNING, "Can't create dir {0}", pDir);
                            }
                        }

                        Path p = Paths.get(DBProperties.getInstance().getPathToDB(),
                                replica.getClassName(), "" + replica.getId());
                        try {
                            long rev = Revision.getInstance().getRevision(replica.getClassName(), replica.getId());
                            if (rev < replica.getRevision()) {
                                Files.write(p, replica.getData());
                                Files.setLastModifiedTime(p, FileTime.fromMillis(replica.getTimestamp()));
                                Revision.getInstance().setRevision(
                                        replica.getClassName(),
                                        replica.getId(),
                                        replica.getRevision());

                            } else if (rev == replica.getRevision()) {
                                MetaInfoData mid = MetaInfo.getDefault().getMetaInfo(replica.getId(), replica.getClassName());
                                if (mid == null) {
                                    Files.write(p, replica.getData());
                                    Files.setLastModifiedTime(p, FileTime.fromMillis(replica.getTimestamp()));
                                    Revision.getInstance().setRevision(
                                            replica.getClassName(),
                                            replica.getId(),
                                            replica.getRevision());

                                } else if (mid.getLastModified() < replica.getTimestamp()) {
                                    Files.write(p, replica.getData());
                                    Files.setLastModifiedTime(p, FileTime.fromMillis(replica.getTimestamp()));
                                    Revision.getInstance().setRevision(
                                            replica.getClassName(),
                                            replica.getId(),
                                            replica.getRevision());
                                }
                            }

                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.WARNING, null, e);
                        }
                    } else if (replica.getAction() == Replica.DELETE_LIST) {
                        for (long id : replica.getDeletedList()) {
                            Path p = Paths.get(DBProperties.getInstance().getPathToDB(),
                                    replica.getClassName(), "" + id);
                            if (p.toFile().exists()) {
                                p.toFile().delete();
                                Revision.getInstance().setDeleted(p.toString());
                            }
                        }
                    }

                    break;

                case Replica.FIRST_SYNC_ENDED:
                    if (getCheckSum() != replica.getCheckSum()) {
                        //Executors.newSingleThreadExecutor().execute(fullNodeUpdate(superSocketAddress));
                    }
                    break;

                case Replica.SYNC:
                    Path p = Paths.get(DBProperties.getInstance().getPathToDB(),
                            replica.getClassName(), "" + replica.getId());

                    Path pDir = Paths.get(DBProperties.getInstance().getPathToDB(),
                            replica.getClassName());
                    if (!pDir.toFile().exists()) {
                        boolean success = pDir.toFile().mkdir();
                        if (!success) {
                            Logger.getGlobal().log(Level.WARNING, "Can't create dir {0}", pDir);
                        }
                    }

                    if (replica.getAction() == Replica.DELETE) {
                        if (p.toFile().exists()) {
                            p.toFile().delete();
                            Revision.getInstance().setDeleted(p.toString());
                        }

                    } else if (replica.getAction() == Replica.ADD) {
                        if (!p.toFile().exists()) {
                            try {
                                Files.write(p, replica.getData());
                                Revision.getInstance().setRevision(
                                        replica.getClassName(),
                                        replica.getId(), replica.getRevision());

                            } catch (Exception e) {
                                Logger.getGlobal().log(Level.WARNING, null, e);
                            }
                        }

                    } else if (replica.getAction() == Replica.MODIFY) {
                        try {
                            long rev = Revision.getInstance().getRevision(
                                    replica.getClassName(), replica.getId());
                            if (rev < replica.getRevision()) {
                                Files.write(p, replica.getData());
                                Revision.getInstance().setRevision(
                                        replica.getClassName(),
                                        replica.getId(), replica.getRevision());

                            } else if (rev == replica.getRevision()) {
                                MetaInfoData mid = MetaInfo.getDefault().getMetaInfo(replica.getId(), replica.getClassName());
                                if (mid.getLastModified() < replica.getTimestamp()) {
                                    Files.write(p, replica.getData());
                                    Files.setLastModifiedTime(p, FileTime.fromMillis(replica.getTimestamp()));
                                    Revision.getInstance().setRevision(
                                            replica.getClassName(),
                                            replica.getId(),
                                            replica.getRevision());
                                }
                            }

                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.WARNING, null, e);
                        }
                    }
                    break;
            }
        }
    }

    private synchronized void nodeIncrementalFileListHandler(Replica replica) {
        IncrementalFileList superIncrementalFileList = replica.getIncrementalFileList();

        // find is absent, find not equal
        IncrementalFileList nodeIncrementalFileList = getIncrementalFileList(
                superIncrementalFileList.getClassName());

        ArrayList<IncrementalFile> listFromSuperNode = Collections.list(
                Collections.enumeration(superIncrementalFileList.getFiles()));
        Collections.sort(listFromSuperNode);

        ArrayList<IncrementalFile> listFromNode = Collections.list(
                Collections.enumeration(nodeIncrementalFileList.getFiles()));
        Collections.sort(listFromNode);

        ArrayList<IncrementalFile> requestFilesFromSuper = new ArrayList<>();
        for (IncrementalFile incrementalFileSuper : listFromSuperNode) {
            int pos = Collections.binarySearch(listFromNode, incrementalFileSuper, new Comparator<IncrementalFile>() {

                @Override
                public int compare(IncrementalFile o1, IncrementalFile o2) {
                    return o1.compareTo(o2);
                }
            });

            if (pos < 0) {
                long revision = Revision.getInstance().getRevision(
                        superIncrementalFileList.getClassName(),
                        Long.parseLong(incrementalFileSuper.getName()));
                if (revision == -1) {
                    requestFilesFromSuper.add(incrementalFileSuper.setAction(IncrementalFile.DELETE));
                } else {
                    requestFilesFromSuper.add(incrementalFileSuper.setAction(IncrementalFile.ADD));
                }

            } else {
                if (listFromNode.get(pos).getFileSize() != incrementalFileSuper.getFileSize()) {
                    requestFilesFromSuper.add(incrementalFileSuper.setAction(IncrementalFile.MODIFY));
                    continue;
                }

                if (listFromNode.get(pos).getLastModify() != incrementalFileSuper.getLastModify()) {
                    requestFilesFromSuper.add(incrementalFileSuper.setAction(IncrementalFile.MODIFY));
                    continue;
                }

                if (listFromNode.get(pos).getRevision() != incrementalFileSuper.getRevision()) {
                    requestFilesFromSuper.add(incrementalFileSuper.setAction(IncrementalFile.MODIFY));
                    continue;
                }
            }
        }

        if (!requestFilesFromSuper.isEmpty()) {
            Replica response = new Replica();
            response.setType(Replica.REQUEST_INCREMENTAL_FILE_LIST_FROM_SUPER);
            response.setClassName(superIncrementalFileList.getClassName());
            response.setRequestIncrementalFiles(requestFilesFromSuper);
            response.setSocketAddress(socketAddress);
            Logger.getGlobal().log(Level.INFO, "Try send to super node "
                    + "{0} request {1} for get incremental file list {2}",
                    new Object[]{superSocketAddress, response, requestFilesFromSuper});
            sender(response, superSocketAddress);
        }

    }

    private IncrementalFileList getIncrementalFileList(String _className) {
        IncrementalFileList incrementalFileList = new IncrementalFileList(_className);
        Path dbDir = Paths.get(DBProperties.getInstance().getPathToDB());
        for (File dir : dbDir.toFile().listFiles()) {
            if (!dir.isDirectory()) {
                continue;
            }

            String className = dir.getName();

            if (!className.equals(_className)) {
                continue;
            }

            boolean isDeny = false;
            for (String s : DBProperties.getInstance().getReplicationDeny()) {
                if (className.equals(s)) {
                    Logger.getGlobal().log(Level.INFO,
                            "Replication for class {0} deny", className);
                    isDeny = true;
                    continue;
                }
            }

            if (isDeny) {
                continue;
            }

            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }

                incrementalFileList.addFileInfo(
                        file.getName(),
                        file.length(),
                        file.lastModified(),
                        Revision.getInstance().getRevision(
                        className, Long.parseLong(file.getName())));
            }

        }
        return incrementalFileList;
    }

    private void incrementalUpdate(Replica replicaFromNode) {
        ArrayList<String> incrementalList = new ArrayList<>();
        Map<String, Long> superCheckSumMap = getCheckSums();
        Iterator<String> it = superCheckSumMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (!replicaFromNode.getCheckSums().containsKey(key)) {
                incrementalList.add(key);

            } else {
                if (replicaFromNode.getCheckSums().get(key).longValue() != superCheckSumMap.get(key).longValue()) {
                    incrementalList.add(key);
                }
            }
        }

        incrementalList.remove("totalCheckSum");

        for (String className : incrementalList) {
            Replica response = new Replica();
            response.setType(Replica.INCREMENTAL_FILE_LIST);
            response.setSocketAddress(socketAddress);
            IncrementalFileList incrementalFileList = getIncrementalFileList(className);
            response.setIncrementalFileList(incrementalFileList);
            Logger.getGlobal().log(Level.INFO, "Try send incremnetal "
                    + "file list to node {0} for class {1}",
                    new Object[]{replicaFromNode.getSocketAddress(), className});
            sender(response, replicaFromNode.getSocketAddress());
        }

    }

    private Runnable fullNodeUpdate(final SocketAddress nodeAddress) {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                Path dbDir = Paths.get(DBProperties.getInstance().getPathToDB());
                for (File dir : dbDir.toFile().listFiles()) {
                    if (!dir.isDirectory()) {
                        continue;
                    }

                    String className = dir.getName();
                    boolean isDeny = false;
                    for (String s : DBProperties.getInstance().getReplicationDeny()) {
                        if (className.equals(s)) {
                            Logger.getGlobal().log(Level.INFO,
                                    "Replication for class {0} deny", className);
                            isDeny = true;
                            continue;
                        }
                    }

                    if (isDeny) {
                        continue;
                    }

                    Replica response = new Replica();
                    response.setType(Replica.INCREMENTAL_FILE_LIST);
                    response.setSocketAddress(socketAddress);
                    IncrementalFileList incrementalFileList = getIncrementalFileList(className);
                    response.setIncrementalFileList(incrementalFileList);
                    Logger.getGlobal().log(Level.INFO, "Try send incremnetal "
                            + "file list to node {0} for class {1}", new Object[]{nodeAddress, className});
                    sender(response, nodeAddress);

//                    response = new Replica();
//                    response.setType(Replica.FIRST_SYNC);
//                    response.setAction(Replica.DELETE_LIST);
//                    response.setClassName(className);
//                    response.setDeleteList(Revision.getInstance().getDeletedList(className));
//                    response.setSocketAddress(socketAddress);
//                    sender(response, nodeAddress);
                }

                Replica response = new Replica();
                response.setType(Replica.FIRST_SYNC_ENDED);
                response.setSocketAddress(socketAddress);
                response.setCheckSum(getCheckSum());
                sender(response, nodeAddress);
            }
        };

        return r;
    }

    private synchronized void sender(Replica replica, SocketAddress address) {

        Socket socket = new Socket();
        OutputStream outputStream = null;
        try {
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(5000);
            socket.setReuseAddress(true);
            socket.connect(address);
            outputStream = socket.getOutputStream();
            outputStream.write(replica.getSerailData());
            outputStream.flush();
            outputStream.close();
            socket.close();

        } catch (Exception e) {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception ex) {
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ex) {
                }
            }
            Logger.getGlobal().log(Level.WARNING, replica.toString() + ", connect to " + address, e);
        }

    }

    private Map<String, Long> getCheckSums() {
        Map<String, Long> map = new HashMap<>();

        long checkSum = 0;
        Path p = Paths.get(DBProperties.getInstance().getPathToDB());
        for (File dir : p.toFile().listFiles()) {
            if (dir.isFile()) {
                continue;
            }

            boolean isDeny = false;
            for (String s : DBProperties.getInstance().getReplicationDeny()) {
                if (s.equals(dir.getName())) {
                    isDeny = true;
                    break;
                }
            }
            if (isDeny) {
                continue;
            }

            Logger.getGlobal().log(Level.INFO, "Scan {0}", dir);

            long len = 0;
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    continue;
                }

                len += f.length() + f.lastModified();
            }

            map.put(dir.getName(), len);

            Logger.getGlobal().log(Level.INFO, "Checksum for class {0} = {1}", new Object[]{dir.getName(), len});
            checkSum += len;
        }

        map.put("totalCheckSum", checkSum);

        return map;
    }

    private long getCheckSum() {
        long checkSum = 0;
        Date startDate = new Date();
        Path p = Paths.get(DBProperties.getInstance().getPathToDB());

        for (File dir : p.toFile().listFiles()) {
            if (dir.isFile()) {
                continue;
            }

            boolean isDeny = false;
            for (String s : DBProperties.getInstance().getReplicationDeny()) {
                if (s.equals(dir.getName())) {
                    isDeny = true;
                    break;
                }
            }
            if (isDeny) {
                continue;
            }

            Logger.getGlobal().log(Level.INFO, "Scan {0}", dir);

            long len = 0;
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    continue;
                }

                len += f.length() + f.lastModified();
            }

            Logger.getGlobal().log(Level.INFO, "Checksum for class {0} = {1}", new Object[]{dir.getName(), len});
            checkSum += len;
        }

        String s = "Checksum compute time for Node mode " + nodeMode + " = "
                + ((new Date().getTime() - startDate.getTime()) / 1000) + "s, "
                + "Checksum = " + checkSum;
        Logger.getGlobal().log(Level.INFO, s);

        return checkSum;
    }

    public void circuitReplication(File file, String className, long id, long revision, int action) {
        for (String s : DBProperties.getInstance().getReplicationDeny()) {
            if (s.equals(className)) {
                Logger.getGlobal().log(
                        Level.INFO,
                        "Replication for class {0} is deny",
                        className);
                return;
            }
        }

        for (SocketAddress sa : nodesAddressSet) {
            Replica replica = new Replica();
            replica.setType(Replica.SYNC);
            replica.setClassName(className);
            replica.setId(id);

            if (action == Handler.DELETE) {
                replica.setAction(Replica.DELETE);
                sender(replica, sa);

            } else {
                try {
                    replica.setData(Files.readAllBytes(file.toPath()));
                    replica.setRevision(revision);
                    replica.setTimestamp(file.lastModified());
                    switch (action) {
                        case 0:
                            replica.setAction(Replica.ADD);
                            break;

                        case 1:
                            replica.setAction(Replica.MODIFY);
                            break;
                    }
                    sender(replica, sa);

                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                }
            }
        }
    }

    public void replicationToSuperNode(File file, String className, long id,
            long revision, int action) {

        for (String s : DBProperties.getInstance().getReplicationDeny()) {
            if (s.equals(className)) {
                Logger.getGlobal().log(
                        Level.INFO,
                        "Replication for class {0} is deny",
                        className);
                return;
            }
        }

        Replica replica = new Replica();
        replica.setType(Replica.SYNC);
        replica.setClassName(className);
        replica.setId(id);

        if (action == Handler.DELETE) {
            replica.setAction(Replica.DELETE);
            sender(replica, superSocketAddress);

        } else {
            try {
                replica.setData(Files.readAllBytes(file.toPath()));
                replica.setRevision(revision);
                replica.setTimestamp(file.lastModified());
                switch (action) {
                    case 0:
                        replica.setAction(Replica.ADD);
                        break;

                    case 1:
                        replica.setAction(Replica.MODIFY);
                        break;
                }
                sender(replica, superSocketAddress);

            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }

        }
    }
}
