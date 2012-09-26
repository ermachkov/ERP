/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;
import org.jssdb.event.ServiceEvent;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ConvertToSingle {

    private FileChannel fileChannelData, fileChannelIndex;
    private String className, pathToDB;
    private NumberFormat decimalFormat;

    public ConvertToSingle(String pathToDB, String className, boolean isClear) throws FileNotFoundException {
        this.pathToDB = pathToDB;
        this.className = className;
        Path p = Paths.get(pathToDB, className, "data");
        if (!p.toFile().exists()) {
            p.toFile().mkdirs();
        }

        if (isClear) {
            p.resolve("data.jsdb").toFile().delete();
            p.resolve("data.idx").toFile().delete();
        }

        fileChannelData = new RandomAccessFile(p.resolve("data.jsdb").toFile(), "rwd").getChannel();
        fileChannelIndex = new RandomAccessFile(p.resolve("data.idx").toFile(), "rwd").getChannel();

        decimalFormat = DecimalFormat.getInstance();
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setMaximumFractionDigits(2);
    }

    private ArrayList<Marker> getAllMarkers(boolean withoutDeleted) throws IOException {
        long ds = new Date().getTime();

        ArrayList<Marker> markers = new ArrayList<>();

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
            if (withoutDeleted) {
                if (service != 1) {
                    markers.add(new Marker(position, id, start, end, timestamp, service));
                }

            } else {
                markers.add(new Marker(position, id, start, end, timestamp, service));
            }

            position = position + 50;
        }
        System.out.println("getAllMarkers: (" + className + ") " + (new Date().getTime() - ds) + "ms.");

        return markers;
    }

    public void fastConvert() throws IOException {
        Path p = Paths.get(pathToDB, className);
        ArrayList<Long> ids = new ArrayList<>();
        for (File f : p.toFile().listFiles()) {
            if (f.isDirectory()) {
                continue;
            }

            ids.add(Long.valueOf(f.getName()));
        }

        ArrayList<Marker> markers = getAllMarkers(true);
        Collections.sort(markers, new Comparator<Marker>() {
            @Override
            public int compare(Marker m1, Marker m2) {
                return Long.valueOf(m1.id).compareTo(Long.valueOf(m2.id));
            }
        });

        Collections.sort(ids);

        ArrayList<Long> markersId = new ArrayList<>();
        for (Marker marker : markers) {
            markersId.add(marker.id);
        }
        Collections.sort(markersId);

        long diff = markersId.size() - ids.size();
        if (diff == 0) {
            return;
        }

        if (diff > 0) {
            ArrayList<Long> findId = new ArrayList<>();
            for (long id : markersId) {
                int pos = Collections.binarySearch(ids, Long.valueOf(id));
                if (pos < 0) {
                    findId.add(id);
                }
            }

            // extract
            for (long id : findId) {
                for (Marker marker : markers) {
                    if (marker.id == id) {
                        extractObject(marker);
                        DBProperties.getInstance().fireServiceEvent(new ServiceEvent(
                                "{message:\"restore cache for " + className + ", "
                                + id + "\"}"));
                        break;
                    }
                }
            }

        } else {
            ArrayList<Long> findId = new ArrayList<>();
            for (long id : ids) {
                int pos = Collections.binarySearch(markersId, Long.valueOf(id));
                if (pos < 0) {
                    findId.add(id);
                }
            }

            // add
            for (long id : findId) {
                byte data[] = Files.readAllBytes(Paths.get(pathToDB, className, "" + id));
                add(id, data);
                DBProperties.getInstance().fireServiceEvent(new ServiceEvent(
                        "{message:\"restore cache for " + className + ", "
                        + id + "\"}"));
            }
        }
    }

    private void add(long id, byte[] data) {
        try {
            long lastPosition = fileChannelData.size();
            ByteBuffer byteBuffer = ByteBuffer.wrap(data);
            fileChannelData.write(byteBuffer, fileChannelData.size());
            long newlastPosition = lastPosition + data.length;
            byte[] dataIndex = setLastPosition(id, lastPosition, newlastPosition, new Date().getTime());

            ByteBuffer bb = ByteBuffer.wrap(dataIndex);
            fileChannelIndex.write(bb, fileChannelIndex.size());

        } catch (IOException ex) {
            Logger.getLogger(DataFileChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void extractObject(Marker marker) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) (marker.end - marker.start));
            fileChannelData.read(byteBuffer, marker.start);
            byteBuffer.clear();
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);

            Path p = Paths.get(pathToDB, className, "" + marker.id);
            Files.write(p, data);

        } catch (IOException e) {
            Logger.getGlobal().log(Level.SEVERE, className + ", id = " + marker.id, e);
        }
    }

    public void convert() throws IOException {
        Path p = Paths.get(pathToDB, className);
        ArrayList<File> files = new ArrayList<>();
        files.addAll(Arrays.asList(p.toFile().listFiles()));

        Collections.sort(files);

        long startPosition = 0, endPosition;
        ByteArrayOutputStream baosIndex = new ByteArrayOutputStream();
        ByteArrayOutputStream baosData = new ByteArrayOutputStream();
        double step = 100d / files.size();
        double progress = 0;
        System.out.println(className);
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }

            byte[] b = Files.readAllBytes(f.toPath());
            baosData.write(b);

            long id = Long.valueOf(f.getName());
            endPosition = startPosition + b.length;
            long timestamp = f.lastModified();
            baosIndex.write(setLastPosition(id, startPosition, endPosition, timestamp));
            startPosition = endPosition;

            progress += step;

            DBProperties.getInstance().fireServiceEvent(new ServiceEvent(
                    "{message:\"restore cache for " + className + ", "
                    + decimalFormat.format(progress) + "%\"}"));

            System.out.print("\r" + decimalFormat.format(progress) + "%");
        }
        System.out.print("\n\n");

        ByteBuffer byteBuffer = ByteBuffer.wrap(baosData.toByteArray());
        fileChannelData.write(byteBuffer);

        byteBuffer = ByteBuffer.wrap(baosIndex.toByteArray());
        fileChannelIndex.write(byteBuffer);
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

    private byte[] setLastPosition(long id, long startPosition, long endPosition, long timestamp) throws IOException {
        byte[] data = new byte[50];

        System.arraycopy(convertToByteArray(zeroLead(id)), 0, data, 0, 10);
        System.arraycopy(convertToByteArray(zeroLead(startPosition)), 0, data, 10, 10);
        System.arraycopy(convertToByteArray(zeroLead(endPosition)), 0, data, 20, 10);
        System.arraycopy(convertToByteArray(zeroLead(timestamp)), 0, data, 30, 10);
        System.arraycopy(convertToByteArray(zeroLead(0)), 0, data, 40, 10);

        return data;
    }
}
