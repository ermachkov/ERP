/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.replicator;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class Replica implements Serializable {

    public static final int REGISTER_REQUEST = 10;
    public static final int REGISTER_OK_RESPONSE = 20;
    public static final int PING_REQUEST = 30;
    public static final int PING_OK_RESPONSE = 40;
    public static final int ADD = 110;
    public static final int MODIFY = 120;
    public static final int DELETE = 130;
    public static final int DELETE_LIST = 135;
    public static final int CHECKSUM_EQUAL = 140;
    public static final int CHECKSUM_NOT_EQUAL = 150;
    public static final int SYNC = 155;
    public static final int FIRST_SYNC = 160;
    public static final int FIRST_SYNC_ENDED = 170;
    public static final int INCREMENTAL_FILE_LIST = 180;
    public static final int REQUEST_INCREMENTAL_FILE_LIST_FROM_SUPER = 190;
    public static final int INCREMENTAL_REPLICA = 200;
    private int type = 0, action = 0;
    private byte[] data;
    private long id, revision, timestamp, checkSum;
    private String className;
    public static final long serialVersionUID = 1L;
    private SocketAddress socketAddress;
    private ArrayList<Long> deletedList;
    private IncrementalFileList incrementalFileList;
    private ArrayList<IncrementalFile> requestFilesFromSuper;
    private Map<String, Long> checkSumsMap;

    public Replica() {
        //
    }
    
    public void setCheckSums(Map<String, Long> checkSumsMap){
        this.checkSumsMap = checkSumsMap;
    }
    
    public Map<String, Long> getCheckSums(){
        return checkSumsMap;
    }
    
    public void setRequestIncrementalFiles(ArrayList<IncrementalFile> requestFilesFromSuper){
        this.requestFilesFromSuper = requestFilesFromSuper;
    }
    
    public ArrayList<IncrementalFile> getRequestIncrementalFiles(){
        return requestFilesFromSuper;
    }

    public int getAction() {
        return action;
    }

    public void setDeleteList(ArrayList<Long> deletedList) {
        this.deletedList = deletedList;
    }

    public ArrayList<Long> getDeletedList() {
        return deletedList;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
    public void setIncrementalFileList(IncrementalFileList incrementalFileList){
        this.incrementalFileList = incrementalFileList;
    }
    
    public IncrementalFileList getIncrementalFileList(){
        return incrementalFileList;
    }

    public byte[] getSerailData() {
        byte buf[] = null;
        try {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(this);
                oos.flush();
                baos.flush();
                buf = baos.toByteArray();
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);

        } finally {
            return buf;
        }
    }

    public long getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(long checkSum) {
        this.checkSum = checkSum;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    @Override
    public String toString() {
        return "Replica{" + "type=" + type + ", action=" + action + ", id=" + id + ", revision=" + revision + ", timestamp=" + timestamp + ", checkSum=" + checkSum + ", className=" + className + ", socketAddress=" + socketAddress + ", deletedList=" + deletedList + '}';
    }
}
