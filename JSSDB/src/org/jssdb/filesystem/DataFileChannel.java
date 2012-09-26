/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.filesystem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;
import org.jssdb.core.proxy.KnowsId;
import org.jssdb.handler.Handler;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DataFileChannel {

    // id | start | end | timestamp | service
    private FileChannel fileChannelData, fileChannelIndex;
    private String className;
    private AtomicBoolean isLock = new AtomicBoolean(false);
    private AtomicBoolean isSyncLock = new AtomicBoolean(false);
    private ByteBuffer byteBufferMarker;
    private LinkedHashMap<Long, Object> mapCollection = new LinkedHashMap<>();
    private ConcurrentLinkedQueue<DataQueue> queue = new ConcurrentLinkedQueue<>();
    private Timer timer;
    private boolean isInit = false;
    private Handler handler = Handler.getInstance();
    private long nextId = -1, autoStart, autoIncrement, autoOffset;

    public DataFileChannel(String className) throws FileNotFoundException {
        this.className = className;
        Path p = Paths.get(DBProperties.getInstance().getPathToDB(), className, "data");
        if (!p.toFile().exists()) {
            p.toFile().mkdirs();
        }

        fileChannelData = new RandomAccessFile(p.resolve("data.jsdb").toFile(), "rwd").getChannel();
        fileChannelIndex = new RandomAccessFile(p.resolve("data.idx").toFile(), "rwd").getChannel();

        init();
    }

    final class DataQueue {

        public final static int ADD = 0, MODIFY = 1, DELETE = 2;
        private int type;
        private long id;
        private Object object;
        private byte[] data;

        public DataQueue(int type, long id, byte[] data, Object object) {
            this.type = type;
            this.id = id;
            this.data = data;
            this.object = object;
        }

        public byte[] getData() {
            return data;
        }

        public int getType() {
            return type;
        }

        public long getId() {
            return id;
        }

        public Object getObject() {
            return object;
        }
    }

    private void init() {
        if (isInit) {
            return;
        }
        mapCollection = getCollection();
        timer = new Timer();
        timer.scheduleAtFixedRate(new StoreSync(), 0, 1);
        isInit = true;

        autoStart = DBProperties.getInstance().getAutoStart();
        autoIncrement = DBProperties.getInstance().getAutoIncrement();
        autoOffset = DBProperties.getInstance().getAutoOffset();
    }

    final class StoreSync extends TimerTask {

        @Override
        public void run() {
            try {
                if (queue.isEmpty()) {
                    return;
                }

                if (isSyncLock.get()) {
                    return;
                }

                isSyncLock.set(true);

                DataQueue dataQueue = queue.poll();
                if (dataQueue == null) {
                    isSyncLock.set(false);
                    return;
                }

                switch (dataQueue.getType()) {
                    case DataQueue.ADD:
                        addSync(dataQueue.getId(), dataQueue.getData());
                        Logger.getGlobal().log(Level.INFO, "addSync {0}, {1}", new Object[]{className, dataQueue.getId()});
                        isSyncLock.set(false);
                        break;

                    case DataQueue.MODIFY:
                        updateSync(dataQueue.getId(), dataQueue.getData());
                        Logger.getGlobal().log(Level.INFO, "updateSync {0}, {1}", new Object[]{className, dataQueue.getId()});
                        isSyncLock.set(false);
                        break;

                    case DataQueue.DELETE:
                        deleteSync(dataQueue.getId());
                        Logger.getGlobal().log(Level.INFO, "deleteSync {0}, {1}", new Object[]{className, dataQueue.getId()});
                        isSyncLock.set(false);
                        break;
                }

            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, className, e);
            }
        }
    }

    public boolean isEmpty() throws IOException {
        return fileChannelData.size() == 0 ? true : false;
    }

    private String zeroLead(long value) {
        StringBuilder sb = new StringBuilder("" + value);
        int count = 20 - sb.length();
        for (int i = 0; i < count; i++) {
            sb.insert(0, "0");
        }

        return sb.toString();
    }

    private byte[] convertToByteArray(String s) {
        byte[] b = new byte[10];
        int index = 0;
        for (int i = 0; i < 20; i = i + 2) {
            b[index] = Integer.valueOf(s.substring(i, i + 2), 16).byteValue();
            index++;
        }

        return b;
    }

    private void setLastPosition(long id, long startPosition, long endPosition, long timestamp) throws IOException {
        byte[] data = new byte[50];

        System.arraycopy(convertToByteArray(zeroLead(id)), 0, data, 0, 10);
        System.arraycopy(convertToByteArray(zeroLead(startPosition)), 0, data, 10, 10);
        System.arraycopy(convertToByteArray(zeroLead(endPosition)), 0, data, 20, 10);
        System.arraycopy(convertToByteArray(zeroLead(timestamp)), 0, data, 30, 10);
        System.arraycopy(convertToByteArray(zeroLead(0)), 0, data, 40, 10);

        ByteBuffer bb = ByteBuffer.wrap(data);
        fileChannelIndex.write(bb, fileChannelIndex.size());

        byte dataDst[] = new byte[byteBufferMarker.limit() + data.length];
        byteBufferMarker.clear();
        byteBufferMarker.get(dataDst, 0, byteBufferMarker.limit());
        System.arraycopy(data, 0, dataDst, byteBufferMarker.limit(), data.length);
        byteBufferMarker = ByteBuffer.wrap(dataDst);
    }

    private void modifyPosition(long startIndexPosition, long id,
            long startPosition, long endPosition,
            long timestamp, long service) throws IOException {

        byte[] data = new byte[50];

        System.arraycopy(convertToByteArray(zeroLead(id)), 0, data, 0, 10);
        System.arraycopy(convertToByteArray(zeroLead(startPosition)), 0, data, 10, 10);
        System.arraycopy(convertToByteArray(zeroLead(endPosition)), 0, data, 20, 10);
        System.arraycopy(convertToByteArray(zeroLead(timestamp)), 0, data, 30, 10);
        System.arraycopy(convertToByteArray(zeroLead(service)), 0, data, 40, 10);

        ByteBuffer bb = ByteBuffer.wrap(data);
        fileChannelIndex.write(bb, startIndexPosition);

        byte dataDst[] = new byte[byteBufferMarker.limit()];
        byteBufferMarker.clear();
        byteBufferMarker.get(dataDst, 0, byteBufferMarker.limit());
        System.arraycopy(data, 0, dataDst, (int) startIndexPosition, data.length);
        byteBufferMarker = ByteBuffer.wrap(dataDst);
    }

    private Marker getMarker(long id) throws IOException {
        long ds = new Date().getTime();

        Marker marker = null;
        byte[] bId = convertToByteArray(zeroLead(id));
        //long requestId = ByteBuffer.wrap(bId).getLong();

        byte[] byteArray = new byte[50];
        if (byteBufferMarker == null) {
            byteBufferMarker = ByteBuffer.allocate((int) fileChannelIndex.size());
            fileChannelIndex.read(byteBufferMarker, 0);
        }

        byteBufferMarker.clear();
        for (int pos = 0; pos < byteBufferMarker.limit(); pos = pos + 50) {
            byteBufferMarker.position(pos);
            byteBufferMarker.get(byteArray);

            if (Arrays.equals(bId, Arrays.copyOf(byteArray, 10))) {
                //if (requestId == ByteBuffer.wrap(Arrays.copyOf(byteArray, 10)).getLong()) {
                StringBuilder sb = new StringBuilder("");
                for (byte b : byteArray) {
                    String s = Integer.toHexString(0xFF & b);
                    s = s.length() == 1 ? "0" + s : s;
                    sb.append(s);
                }
                long start = Long.valueOf(sb.toString().substring(20, 40)).longValue();
                long end = Long.valueOf(sb.toString().substring(40, 60)).longValue();
                long timestamp = Long.valueOf(sb.toString().substring(60, 80)).longValue();
                long service = Long.valueOf(sb.toString().substring(80, 100)).longValue();
                marker = new Marker(pos, id, start, end, timestamp, service);
                break;
            }
        }

        System.out.println("getMarker: (" + className + ", id = " + id + ") " + (new Date().getTime() - ds) + "ms.");

        return marker;
    }

    private ArrayList<Marker> getAllMarkers() throws IOException {
        long ds = new Date().getTime();

        ArrayList<Marker> markers = new ArrayList<>();
        
        if(fileChannelIndex.size() == 0){
            byteBufferMarker = ByteBuffer.allocate((int) fileChannelIndex.size());
            return markers;
        }

        long position = 0;
        ByteBuffer byteBuffer = ByteBuffer.allocate(50);
        StringBuilder sb = new StringBuilder("");
        while (position < fileChannelIndex.size()) {
            fileChannelIndex.read(byteBuffer, position);
            byte[] byteArray = new byte[50];

            byteBuffer.clear();
            byteBuffer.get(byteArray);
            byteBuffer.flip();

            sb.setLength(0);
            for (byte b : byteArray) {
                String s = Integer.toHexString(0xFF & b);
                s = s.length() == 1 ? "0" + s : s;
                sb.append(s);
            }

            long id = Long.valueOf(sb.toString().substring(0, 20)).longValue();
            long start = Long.valueOf(sb.toString().substring(20, 40)).longValue();
            long end = Long.valueOf(sb.toString().substring(40, 60)).longValue();
            long timestamp = Long.valueOf(sb.toString().substring(60, 80)).longValue();
            long service = Long.valueOf(sb.toString().substring(80, 100)).longValue();
            markers.add(new Marker(position, id, start, end, timestamp, service));

            position = position + 50;
        }

        byteBufferMarker = ByteBuffer.allocate((int) fileChannelIndex.size());
        fileChannelIndex.read(byteBufferMarker, 0);

        System.out.println("getAllMarkers: (" + className + ") " + (new Date().getTime() - ds) + "ms.");

        return markers;
    }

    private void addSync(long id, byte[] data) {
        try {
            long lastPosition = fileChannelData.size();
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            fileChannelData.write(byteBuffer, fileChannelData.size());
            long newlastPosition = lastPosition + data.length;
            setLastPosition(id, lastPosition, newlastPosition, new Date().getTime());

        } catch (IOException ex) {
            Logger.getLogger(DataFileChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean add(long id, byte[] data, Object object) {
        long ds = new Date().getTime();

        while (isLock.get()) {
            LockSupport.parkNanos(1000);
        }
        isLock.set(true);

        Object o = mapCollection.put(Long.valueOf(id), object);
        System.out.println("DataFileChannel add: id=" + id + ", new Object " + object + ", old Object " + o);

        queue.add(new DataQueue(DataQueue.ADD, id, data, object));

        isLock.set(false);

        System.out.println("DataFileChannel add: (" + className + ") " + (new Date().getTime() - ds) + "ms.");

        return true;
    }

    private void updateSync(long id, byte[] data) {
        try {
            Marker marker = getMarker(id);
            if (marker == null) {
                return;
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            long startPosition = fileChannelData.size();
            long endPosition = fileChannelData.size() + data.length;
            fileChannelData.write(byteBuffer, fileChannelData.size());
            modifyPosition(marker.startIndexPosition, marker.id,
                    startPosition, endPosition,
                    new Date().getTime(), 0);

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
        }
    }

    public boolean update(long id, byte[] data, Object object) {
        long ds = new Date().getTime();

        while (isLock.get()) {
            LockSupport.parkNanos(1000);
        }
        isLock.set(true);

        Object o = mapCollection.put(id, object);
        System.out.println("DataFileChannel update: new Object " + object + ", old Object " + o);

        queue.add(new DataQueue(DataQueue.MODIFY, id, data, object));

        isLock.set(false);

        System.out.println("DataFileChannel update: (" + className + ") " + (new Date().getTime() - ds) + "ms.");

        return true;
    }

    private void deleteSync(final long id) {
        try {
            Marker marker = getMarker(id);
            if (marker == null) {
                isLock.set(false);
            }

            modifyPosition(marker.startIndexPosition, marker.id, marker.start,
                    marker.end, marker.timestamp, 1);
            isLock.set(false);

        } catch (IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
    }

    public boolean delete(final long id) {
        while (isLock.get()) {
            LockSupport.parkNanos(1000);
        }
        isLock.set(true);

        mapCollection.remove(id);

        queue.add(new DataQueue(DataQueue.DELETE, id, null, null));

        isLock.set(false);

        return true;
    }

    public LinkedHashMap<Long, Object> getCollection(Date dateStart, Date dateEnd) {
        while (isLock.get()) {
            LockSupport.parkNanos(1000);
        }
        isLock.set(true);

        try {
            ArrayList<Marker> markers = new ArrayList<>();
            for (Marker marker : getAllMarkers()) {
                if (marker.timestamp >= dateStart.getTime() && marker.timestamp <= dateEnd.getTime()) {
                    markers.add(marker);
                }
            }

            long ds = new Date().getTime();
            LinkedHashMap<Long, Object> map = new LinkedHashMap<>();

            for (Marker marker : markers) {
                if (marker.service == 1) {
                    continue;
                }
                map.put(marker.id, getObject(marker));
            }
            System.out.println("getCollection by Dates: (" + className + ") " + (new Date().getTime() - ds) + "ms.");

            isLock.set(false);
            return map;

        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, className, ex);
            isLock.set(false);
            return new LinkedHashMap<>();
        }
    }

    public LinkedHashMap<Long, Object> getCollection() {
        while (isLock.get()) {
            LockSupport.parkNanos(1000);
        }
        isLock.set(true);

        try {
            long ds = new Date().getTime();

            LinkedHashMap<Long, Object> map;

            if (!mapCollection.isEmpty()) {
                isLock.set(false);

                // check
                ArrayList<Long> correctionList = new ArrayList<>();
                Iterator<Long> it = mapCollection.keySet().iterator();
                while (it.hasNext()) {
                    long key = it.next();
                    Object obj = mapCollection.get(key);
                    if (obj instanceof KnowsId) {
                        if (((KnowsId) obj).getId() == key) {
                            continue;
                        } else {
                            //isNeedCorrection = true;
                            correctionList.add(key);
                        }
                    } else {
                        break;
                    }
                }

                if (!correctionList.isEmpty()) {

                    for (long id : correctionList) {
                        mapCollection.remove(id);
                        mapCollection.put(id, handler.get(id, className));
                    }

                    System.out.println("getCollection: (" + className + ") " + (new Date().getTime() - ds) + "ms.");
                    return mapCollection;

                } else {
                    System.out.println("getCollection: (" + className + ") " + (new Date().getTime() - ds) + "ms.");
                    return mapCollection;
                }

            } else {
                ArrayList<Marker> markers = getAllMarkers();
                map = new LinkedHashMap<>();

                for (Marker marker : markers) {
                    if (marker.service == 1) {
                        continue;
                    }
                    map.put(Long.valueOf(marker.id), getObject(marker));
                }
                System.out.println("getCollection: (" + className + ") " + (new Date().getTime() - ds) + "ms.");
            }

            isLock.set(false);

            return map;

            //} catch (IOException | ClassNotFoundException e) {
        } catch (IOException e) {
            Logger.getGlobal().log(Level.SEVERE, className, e);
            isLock.set(false);
            return new LinkedHashMap<>();
        }

    }

    private Object getObject(Marker marker) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) (marker.end - marker.start));
            fileChannelData.read(byteBuffer, marker.start);
            byteBuffer.clear();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, className + ", id = " + marker.id, e);
            return null;
        }
    }

    public Object getObject(long id) {
        while (isLock.get()) {
            LockSupport.parkNanos(1000);
        }
        isLock.set(true);

        try {
            Object object = mapCollection.get(id);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object _object = ois.readObject();

            isLock.set(false);

            return _object;

        } catch (IOException | ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, className, e);
            isLock.set(false);

            return null;
        }
    }

    public long getDataSize() throws IOException {
        long totalSize = 0;
        ArrayList<Marker> markers = getAllMarkers();
        for (Marker marker : markers) {
            if (marker.service == 1) {
                continue;
            }

            totalSize += marker.end - marker.start;
        }
        return totalSize;
    }

    public long getNextId() {
        long number = 0;

        if (nextId == -1) {
            try {
                ArrayList<Marker> list = getAllMarkers();
                Collections.sort(list, new Comparator<Marker>() {
                    @Override
                    public int compare(Marker m1, Marker m2) {
                        return Long.valueOf(m1.id).compareTo(Long.valueOf(m2.id));
                    }
                });

                if(!list.isEmpty()){
                    number = list.get(list.size() - 1).id + 1;
                }

                nextId = computeNextId(number);

                return nextId;

            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, className, e);
                return -1;
            }

        } else {
            number = nextId + 1;
            nextId = computeNextId(number);
            return nextId;
        }
    }

    private long computeNextId(long number) {
        long newId = 0;
        if (number < autoStart) {
            newId = autoStart;

        } else if (number == autoStart) {
            newId = autoStart + autoIncrement + autoOffset;

        } else if (number > autoStart) {
            if ((number % autoOffset) == 0) {
                newId = (number - (number % autoOffset)) + autoIncrement;
            } else {
                newId = (number - (number % autoOffset)) + autoIncrement + autoOffset;
            }
        }

        return newId;
    }
}
