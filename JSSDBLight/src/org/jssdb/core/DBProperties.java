/*
 *  Copyright (C) 2010 Zubanov Dmitry
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jssdb.core;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.proxy.ProxyHolder;
import org.jssdb.core.proxy.ProxyIO;
import org.jssdb.event.*;
import org.jssdb.handler.FileStorageHandler;
import org.jssdb.handler.Handler;
import org.jssdb.replicator.TCPNode;
import org.jssdb.utils.InitException;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DBProperties implements Serializable{

    private static final Logger logger = Logger.getLogger(DBProperties.class.getName());
    private String externalJarFiles[];
    private String pathToDB, bindHost = "localhost",
            networkDevice = "", superNodeHost, nodeHost, rsyncPath, rsyncModule,
            serverHost, superNodeBindHost, nodeBindHost;
    private int bindPort = 5500, autoStart = 0, autoIncrement = 0, autoOffset = 1,
            replicationPort = 7000,
            transferBlockSize = 49152, memoryModel = 1, nodePort = -1,
            superNodePort = -1, heartbeat = 20, serverPort;
    public static int RAM = 1, STORAGE = 2;
    public boolean isInit = false;
    private ConcurrentHashMap<String, Long> imageHolderMap = new ConcurrentHashMap<>();
    private boolean isDebug = false, isSuperNode = false, isNetworkMode = false, isServerMode = false;
    private EventListenerList listenerList = new EventListenerList();
    private ArrayList<String> replicationDeny = new ArrayList<>();
    private static DBProperties self = null;

    private DBProperties() {
    }

    public void forceInitMe() {
        isInit = false;
        initMe();
    }

    public boolean isDebug() {
        return isDebug;
    }

    public ConcurrentHashMap<String, Long> getImageHolderMap() {
        return imageHolderMap;
    }

    public void initMe() {

        if (isInit) {
            return;
        }

        for (String jar : getExternalJarFiles()) {
            ExternalClassLoader.getInstance().addJarFile(jar);
        }

        Path path = Paths.get(pathToDB + "lastestid");
        if (!path.toFile().exists()) {
            try {
                try (FileWriter fw = new FileWriter(path.toString())) {
                    new Properties().store(fw, "");
                }
            } catch (IOException ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
            }
        }

        File[] files = Paths.get(pathToDB).toFile().listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }

            if (f.toString().indexOf("_lock") != -1) {
                f.delete();
            }
        }

        Path proxyHolderDir = Paths.get(pathToDB + ProxyHolder.class.getName());
        if (!proxyHolderDir.toFile().isDirectory()) {
            proxyHolderDir.toFile().mkdir();
        }

        Path proxyHolderPath = Paths.get(pathToDB + ProxyHolder.class.getName() + File.separator + "1");
        if (!proxyHolderPath.toFile().exists()) {
            ProxyIO.write(proxyHolderPath.toString(), new ProxyHolder());

        } else {
            ProxyHolder proxyHolder = (ProxyHolder) ProxyIO.read(pathToDB
                    + ProxyHolder.class.getName() + File.separator + "1");
            imageHolderMap.putAll(proxyHolder.getProxyMap());

        }

        FileStorageHandler.getDefault().setIdOffset(getAutoStart(),
                getAutoIncrement(), getAutoOffset());

        if (isReplicationEnabled()) {
            //
        }

        if (getMemoryModel() == DBProperties.RAM) {
            //ramHandler = RamHandler.getInstance();
            //RamHandler.getInstance().initDB();
            //Query.getInstance().init();

        }

        if (isReplicationEnabled()) {
            if (isSuperNode) {
                //UDPSuperNode.getInstance();
                TCPNode.getSuperNode();
                System.out.println("Try start SUPER NODE...");

            } else {
                TCPNode.getNode();
//                UDPNode.getInstance();
//                UDPNode.getInstance().addServiceEventListener(new ServiceEventListener() {
//
//                    @Override
//                    public void serviceEvent(ServiceEvent evt) {
//                        fireServiceEvent(evt);
//                    }
//                });

                System.out.println("Try start NODE...");
            }
        }

        Handler.getInstance().addJSSDBEventListener(new EventAdapter() {

            @Override
            public void allEvent(Event evt) {
                fireCustomEvent(evt);
            }

            @Override
            public void sync(Event evt) {
                fireCustomEvent(evt);
            }
        });
    }

    public String getNodeBindHost() {
        return nodeBindHost;
    }

    public String getSuperNodeBindHost() {
        return superNodeBindHost;
    }

    public boolean isReplicationEnabled() {
        if(superNodeBindHost == null){
            return false;
        } else if(superNodeBindHost.equals("")){
            return false;
        } else {
            return true;
        }
    }

    public void setProperties(String file) {
        Properties p = new Properties();
        FileReader fileReader;
        try {
            fileReader = new FileReader(new File(file));
            p.load(fileReader);
            String val = p.getProperty("external_jar_files");
            if (val == null) {
                throw new InitException("Path to external jar files can't be is null");
            } else {
                String arr[] = val.split(",");
                externalJarFiles = new String[arr.length];
                int i = 0;
                for (String s : arr) {
                    externalJarFiles[i] = s;
                    i++;
                }
            }

            val = p.getProperty("path_to_db");
            if (val == null) {
                throw new InitException("Path to db folder can't be is null");
            } else {
                pathToDB = val;
                Handler.getInstance().initWatcher(val);
            }

            val = p.getProperty("network_device");
            if (val != null) {
                networkDevice = val;
            }

            val = p.getProperty("port");
            if (val != null) {
                if (val.equals("")) {
                    throw new InitException("Bind port can't be is empty string");
                } else {
                    try {
                        bindPort = Integer.parseInt(val);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, val, new InitException("Port number invalid"));
                    }

                }
            }

            val = p.getProperty("replication_port");
            if (val != null) {
                if (val.equals("")) {
                    throw new InitException("Bind port can't be is empty string");
                } else {
                    try {
                        replicationPort = Integer.parseInt(val);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, val, new InitException("Port number invalid"));
                    }

                }
            }

            val = p.getProperty("memory_model");
            if (val != null) {
                if (val.equals("")) {
                    throw new InitException("Memory model can't be is empty");
                } else {
                    if (val.trim().equals("ram")) {
                        memoryModel = RAM;
                    } else {
                        memoryModel = STORAGE;
                    }
                }
            }

            val = p.getProperty("auto_start");
            if (val == null) {
                throw new InitException("Auto start value can't be is null");
            } else {
                try {
                    autoStart = Integer.parseInt(val);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, val, new InitException("Auto start value invalid"));
                }
            }

            val = p.getProperty("auto_increment");
            if (val == null) {
                throw new InitException("Auto increment value can't be is null");
            } else {
                try {
                    autoIncrement = Integer.parseInt(val);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, val, new InitException("Auto increment value invalid"));
                }
            }

            val = p.getProperty("auto_offset");
            if (val == null) {
                throw new InitException("Auto offset value can't be is null");
            } else {
                try {
                    autoOffset = Integer.parseInt(val);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, val, new InitException("Auto offset value invalid"));
                }
            }

            val = p.getProperty("transfer_block_size");
            if (val != null) {
                if (val.equals("")) {
                    throw new InitException("Transfer block size value can't be is empty");
                } else {
                    try {
                        transferBlockSize = Integer.parseInt(val);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, val, new InitException("Transfer block size value invalid"));
                    }
                }
            }

            val = p.getProperty("node_host");
            if (val != null) {
                nodeHost = val.trim();
            }

            val = p.getProperty("node_port");
            if (val != null) {
                try {
                    nodePort = Integer.parseInt(val.trim());
                } catch (Exception e) {
                }

            }

            val = p.getProperty("super_node_host");
            if (val != null) {
                superNodeHost = val.trim();
            }

            val = p.getProperty("super_node_port");
            if (val != null) {
                try {
                    superNodePort = Integer.parseInt(val.trim());
                } catch (Exception e) {
                }
            }

            val = p.getProperty("super_node_bind_host");
            if (val == null) {
                superNodeBindHost = "";
            } else {
                superNodeBindHost = val;
            }

            val = p.getProperty("node_bind_host");
            if (val == null) {
                nodeBindHost = "";
            } else {
                nodeBindHost = val;
            }

            val = p.getProperty("is_super_node");
            if (val != null) {
                if (val.equals("true")) {
                    isSuperNode = true;
                }
            }

            val = p.getProperty("is_debug");
            if (val != null) {
                if (val.equals("true")) {
                    isDebug = true;
                } else {
                    isDebug = false;
                }
            }

            val = p.getProperty("heartbeat");
            if (val != null) {
                try {
                    heartbeat = Integer.parseInt(val);
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, val, e);
                }
            }

            val = p.getProperty("rsync_module");
            if (val != null) {
                try {
                    rsyncModule = val;
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, val, e);
                }
            }

            val = p.getProperty("rsync_path");
            if (val != null) {
                try {
                    rsyncPath = val;
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, val, e);
                }
            }

            val = p.getProperty("replication_deny");
            if (val != null) {
                String arr[] = val.split(",");
                for (String s : arr) {
                    replicationDeny.add(s.trim());
                }
            }

            val = p.getProperty("is_server_mode");
            if (val != null) {
                isNetworkMode = true;
                if (val.trim().equals("true")) {
                    isServerMode = true;
                }
            }

            val = p.getProperty("server_host");
            if (val != null) {
                serverHost = val.trim();
            }

            val = p.getProperty("server_port");
            if (val != null) {
                serverPort = Integer.parseInt(val.trim());
            }

            fileReader.close();

        } catch (IOException | InitException ex) {
            logger.log(Level.SEVERE, file, ex);
        }
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public boolean isNetworkMode() {
        return isNetworkMode;
    }

    public boolean isServerMode() {
        return isServerMode;
    }

    public String getRsyncPath() {
        return rsyncPath;
    }

    public void setRsyncPath(String rsyncPath) {
        this.rsyncPath = rsyncPath;
    }

    public String getRsyncModule() {
        return rsyncModule;
    }

    public void setRsyncModule(String rsyncModule) {
        this.rsyncModule = rsyncModule;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void setMemoryModel(int model) {
        memoryModel = model;
    }

    public int getMemoryModel() {
        return memoryModel;
    }

    public int getAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(int autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public int getAutoOffset() {
        return autoOffset;
    }

    public void setAutoOffset(int autoOffset) {
        this.autoOffset = autoOffset;
    }

    public int getAutoStart() {
        return autoStart;
    }

    public void setAutoStart(int autoStart) {
        this.autoStart = autoStart;
    }

    public String getBindHost() {
        return bindHost;
    }

    public void setBindHost(String bindHost) {
        this.bindHost = bindHost;
    }

    public int getBindPort() {
        return bindPort;
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public String[] getExternalJarFiles() {
        return externalJarFiles;
    }

    public void setExternalJarFiles(String externalJarFiles[]) {
        this.externalJarFiles = externalJarFiles;
    }

    public String getPathToDB() {
        return pathToDB;
    }

    public void setPathToDB(String pathToDB) {
        this.pathToDB = pathToDB;
    }

    public int getReplicationPort() {
        return replicationPort;
    }

    public void setReplicationPort(int replicationPort) {
        this.replicationPort = replicationPort;
    }

    public int getTransferBlockSize() {
        return transferBlockSize;
    }

    public void setTransferBlockSize(int transferBlockSize) {
        this.transferBlockSize = transferBlockSize;
    }

    public String getNetworkDevice() {
        return networkDevice;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public int getNodePort() {
        return nodePort;
    }

    public String getSuperNodeHost() {
        return superNodeHost;
    }

    public int getSuperNodePort() {
        return superNodePort;
    }

    public boolean isSuperNode() {
        return isSuperNode;
    }

    public synchronized static DBProperties getInstance() {
        if (self == null) {
            self = new DBProperties();
        }

        return self;
    }

    public void addJSSDBEventListener(JSSDBEventListener listener) {
        listenerList.add(JSSDBEventListener.class, listener);
    }

    public void addServiceEventListener(ServiceEventListener listener) {
        listenerList.add(ServiceEventListener.class, listener);
    }

    public void fireServiceEvent(ServiceEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ServiceEventListener.class) {
                ((ServiceEventListener) listeners[i + 1]).serviceEvent(evt);
            }
        }
    }

    private void fireCustomEvent(Event evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == JSSDBEventListener.class) {

                if (evt.getAction() == 0) {
                    ((JSSDBEventListener) listeners[i + 1]).objectAdded(evt);
                }

                if (evt.getAction() == 1) {
                    ((JSSDBEventListener) listeners[i + 1]).objectModifyed(evt);
                }

                if (evt.getAction() == 2) {
                    ((JSSDBEventListener) listeners[i + 1]).objectDeleted(evt);
                }

                ((JSSDBEventListener) listeners[i + 1]).allEvent(evt);
            }
        }
    }

    public ArrayList<String> getReplicationDeny() {
        ArrayList<String> _replicationDeny = new ArrayList<>();
        _replicationDeny.addAll(replicationDeny);
        return _replicationDeny;
    }
}
