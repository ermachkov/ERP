/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.core.DBProperties;
import org.jssdb.filesystem.DataFileChannel;
import org.jssdb.query.Query;
import org.jssdb.query.Request;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class DataFileChannelTest {
    
    private DBProperties dataBase;

    public DataFileChannelTest() {
        dataBase = DBProperties.getInstance();
        dataBase.setProperties("/home/developer/NetBeansProjects/JSSDBLight/test/db.properties");
        dataBase.initMe();
    }

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        DataFileChannelTest dfct = new DataFileChannelTest();
        dfct.add();
        //dfct.modify();
        //dfct.read();
        
        //dfct.test();
        //dfct.testReadSingle();
        //dfct.testModifySingle();
        //dfct.testDeleteSingle();
        //dfct.testReadAll();
    }
    
    private void modify() throws ClassNotFoundException{
        Query q = Query.getInstance();
        q.init();
        
        Request request = new Request(DFC.class.getName());
        Map<Long, Object> map = q.getMapResultSet(request);
        Iterator<Long> it = map.keySet().iterator();
        while(it.hasNext()){
            long id = it.next();
            DFC dfc = (DFC)map.get(id);
            String newName = dfc.getName() + " 1000";
            dfc.setName(newName);
            q.updateObject(id, dfc);
            System.out.println("Updated " + dfc);
        }
        
        map = q.getMapResultSet(request);
        it = map.keySet().iterator();
        while(it.hasNext()){
            long id = it.next();
            Object obj = map.get(id);
            System.out.println("id " + id + ", Object " + obj);
        }
    }
    
    private void add() throws ClassNotFoundException{
        Query q = Query.getInstance();
        q.init();
        
        Request request = new Request(DFC.class.getName());
        Map<Long, Object> map = q.getMapResultSet(request);
        Iterator<Long> it = map.keySet().iterator();
        while(it.hasNext()){
            long id = it.next();
            Object obj = map.get(id);
            System.out.println("id " + id + ", Object " + obj);
        }
        
        for(int i = 201; i < 110000; i++){
            DFC dfc = new DFC();
            dfc.setName("My name is " + i);
            long id = q.addObject(dfc);
            q.updateJournalObject(id, dfc);
        }
        
        request = new Request(DFC.class.getName());
        map = q.getMapResultSet(request);
        it = map.keySet().iterator();
        while(it.hasNext()){
            long id = it.next();
            Object obj = map.get(id);
            System.out.println("id " + id + ", Object " + obj);
        }
    }
    
    private void read() throws ClassNotFoundException{
        Query q = Query.getInstance();
        q.init();
        
        Request request = new Request(DFC.class.getName());
        Map<Long, Object> map = q.getMapResultSet(request);
        Iterator<Long> it = map.keySet().iterator();
        while(it.hasNext()){
            long id = it.next();
            Object obj = map.get(id);
            System.out.println("id " + id + ", Object " + obj);
        }
    }

    private void testReadAll() throws FileNotFoundException, IOException, ClassNotFoundException {
        DBProperties.getInstance().setPathToDB("/home/developer/NetBeansProjects/JSSDBLight/test");
        DataFileChannel dataFileChannel = new DataFileChannel(DFC.class.getName());
        Map<Long, Object> map = dataFileChannel.getCollection();
        Iterator<Long> it = map.keySet().iterator();
        while (it.hasNext()) {
            long id = it.next();
            Object obj = map.get(id);
            System.out.println("id: " + id + ", DFC: " + obj);
        }
    }

    private void testDeleteSingle() throws FileNotFoundException, IOException, ClassNotFoundException {
        DBProperties.getInstance().setPathToDB("/home/developer/NetBeansProjects/JSSDBLight/test");
        DataFileChannel dataFileChannel = new DataFileChannel(DFC.class.getName());
        DFC dfc = (DFC) dataFileChannel.getObject(95);
        System.out.println("Before delete: " + dfc);

        dataFileChannel.delete(95);
        dfc = (DFC) dataFileChannel.getObject(95);
        System.out.println("After delete: " + dfc);
    }

    private void testModifySingle() throws FileNotFoundException, IOException, ClassNotFoundException {
        DBProperties.getInstance().setPathToDB("/home/developer/NetBeansProjects/JSSDBLight/test");
        DataFileChannel dataFileChannel = new DataFileChannel(DFC.class.getName());
        long ds = new Date().getTime();
        DFC dfc = (DFC) dataFileChannel.getObject(95);
        dfc.setName("This is modify!!!!!! This is modify!!!!!! This is modify!!!!!! This is modify!!!!!!");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dfc);
        dataFileChannel.update(95, baos.toByteArray(), dfc);

        dfc = (DFC) dataFileChannel.getObject(95);
        System.out.println("Modify DFC: " + dfc);
    }

    private void testReadSingle() throws FileNotFoundException, IOException, ClassNotFoundException {
        DBProperties.getInstance().setPathToDB("/home/developer/NetBeansProjects/JSSDBLight/test");
        DataFileChannel dataFileChannel = new DataFileChannel(DFC.class.getName());
        long ds = new Date().getTime();
        Object obj = dataFileChannel.getObject(95);
        System.out.println("getObject(95): " + obj);
        System.out.println((new Date().getTime() - ds) + "ms.");

        ds = new Date().getTime();
        Map<Long, Object> map = dataFileChannel.getCollection();
        System.out.println(map.size());
        System.out.println((new Date().getTime() - ds) + "ms.");
    }

    private void test() throws IOException, ClassNotFoundException {
        System.out.println("--------------------- START TEST ---------------------");

        File f = new File("/home/developer/NetBeansProjects/JSSDBLight/test/org.jssdb.tests.DFC/data/data.idx");
        f.delete();

        f = new File("/home/developer/NetBeansProjects/JSSDBLight/test/org.jssdb.tests.DFC/data/data.jsdb");
        f.delete();

        DBProperties.getInstance().setPathToDB("/home/developer/NetBeansProjects/JSSDBLight/test");
        try {
            DataFileChannel dataFileChannel = new DataFileChannel(DFC.class.getName());

            for (int i = 1; i < 100; i++) {
                DFC dfc = new DFC();
                dfc.setName("My name is Number" + i);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(dfc);

                boolean success = dataFileChannel.add(i, baos.toByteArray(), dfc);
                System.out.println(MessageFormat.format(
                        "Result for add DFC {0} is {1}",
                        new Object[]{dfc, success}));
            }

            Object obj = dataFileChannel.getObject(95);
            System.out.println("getObject(95): " + obj);

            Map<Long, Object> map = dataFileChannel.getCollection();
            Iterator<Long> it = map.keySet().iterator();
            while (it.hasNext()) {
                long id = it.next();
                obj = map.get(id);
                System.out.println("id: " + id + ", DFC: " + obj);
            }

            System.out.println("--------------------- END TEST ---------------------");

        } catch (FileNotFoundException ex) {
            System.err.println(ex);
            Logger.getLogger(DataFileChannelTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
