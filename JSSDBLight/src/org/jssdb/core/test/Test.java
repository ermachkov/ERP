/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.core.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jssdb.handler.Handler;
import org.jssdb.utils.SizeException;

/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia,
 * created 09.05.2010
 */
public class Test {

    ExecutorService es;
    Date dateStart;
    Logger logger = Logger.getLogger(Test.class.getName());

    public Test() {
        es = Executors.newFixedThreadPool(2);
        //find();
        //get();
        //simpleFind();
    }

    /*
    private void get() {
        dateStart = new Date();
        try {
            ArrayList<Map<Long, Object>> a = Handler.getInstance().getObjectList(
                    Handler.getInstance().getMinId(Person.class),
                    Handler.getInstance().getMaxId(Person.class), Person.class);
            es.execute(findThem(a, 0, a.size()));
        } catch (Exception e) {
            logger.log(Level.OFF, null, e);
        }

    }
     * 
     */

    public void simpleFind() {
        dateStart = new Date();
        for (int i = 0; i < Handler.getInstance().getMaxId(Person.class); i++) {
            Person p = (Person) Handler.getInstance().get(i, Person.class);
            if (p != null) {
                if (p.toString().equals("Dima Zubanov")) {
                    System.out.println("FIND!!!");
                }
            }
        }

        Date dateEnd = new Date();
        long l = dateEnd.getTime() - dateStart.getTime();
        logger.info("" + l);
    }

    /*
    public void find() {
        dateStart = new Date();

        //Person p = new Person("Fucker", "Fuck");
        //for (int i = 0; i < 10000; i++) {
        //    Handler.getInstance().add(p);
        //}

        try {
            ArrayList<Map<Long, Object>> a = Handler.getInstance().getObjectList(
                    Handler.getInstance().getMinId(Person.class),
                    Handler.getInstance().getMaxId(Person.class), Person.class);
            int halfSize = a.size() / 2;

            logger.info("End read list, time = " + (new Date().getTime() - dateStart.getTime()));

            dateStart = new Date();

            //es.execute(findThem(a, 0, halfSize));
            Thread t1 = new Thread(findThem(a, 0, halfSize));
            t1.start();
            logger.info("run first");

            Thread t2 = new Thread(findThem(a, halfSize, a.size()));
            t2.start();
            //es.execute(findThem(a, halfSize, a.size()));
            logger.info("run second");

        } catch (SizeException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     *
     */

    private Runnable findThem(final ArrayList<Map<Integer, Object>> a, final int start, final int end) {
        Runnable exec = new Runnable() {

            public void run() {
                int lastId = 0;
                for (int i = start; i < end; i++) {
                    Map m = a.get(i);
                    Person pf = (Person) m.get(m.keySet().toArray()[0]);
                    //logger.info("" + i + " " + pf.toString());
                    if (pf.toString().equals("Dima Zubanov")) {
                        logger.info("find " + pf + " " + m.keySet().toArray()[0]);

                    }
                    lastId = (Integer) m.keySet().toArray()[0];
                }
                Date dateEnd = new Date();
                long l = dateEnd.getTime() - dateStart.getTime();
                logger.info("time = " + l + ", lastId = " + lastId);
                logger.info("end Thread");
            }
        };

        return exec;
    }

    public static void main(String args[]) {
        new Test();
    }
}
