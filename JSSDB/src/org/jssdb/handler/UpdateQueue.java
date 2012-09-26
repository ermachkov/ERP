/*
 * Copyright (C) 2011 developer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jssdb.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class UpdateQueue {

    private CopyOnWriteArrayList<QueueItem> queueList;
    private AtomicBoolean isBusy = new AtomicBoolean(false);

    public UpdateQueue() {
        queueList = new CopyOnWriteArrayList<>();
        ScheduledExecutorService scheduller = Executors.newSingleThreadScheduledExecutor();
        scheduller.scheduleAtFixedRate(sorter(), 100, 200, TimeUnit.MILLISECONDS);
    }

    public void addToQueue(long id, Object object) {
        queueList.add(new QueueItem(id, object));
    }

    private Runnable sorter() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                if (!isBusy.get()) {
                    sendToHandler();
                }
            }
        };

        return r;
    }

    private void sendToHandler() {
        isBusy.set(true);
        ArrayList<QueueItem> updateList = getUpdateList();
        for (QueueItem queueItem : updateList) {
            Handler.getInstance().updateFromQueue(queueItem.getId(), queueItem.getObject());
        }
        
        long currentTime = new Date().getTime();
        ArrayList<QueueItem> removeList = new ArrayList<>();
        for(QueueItem q : queueList){
            if((currentTime - q.getTimeMark()) > 1000){
                removeList.add(q);
            }
        }
        queueList.removeAll(removeList);
        isBusy.set(false);
    }

    private ArrayList<QueueItem> getUpdateList() {
        long controlTime = new Date().getTime();
        ArrayList<QueueItem> list = new ArrayList<>();
        ArrayList<String> classes = new ArrayList<>();

        for (QueueItem item : queueList) {
            if (classes.contains(item.getObjectClass())) {
                continue;
            }
            classes.add(item.getObjectClass());
        }
        //System.out.println(classes);

        Map<String, ArrayList<QueueItem>> mapMulti = new HashMap<>();
        for (String className : classes) {
            ArrayList<QueueItem> multiList = new ArrayList<>();
            for (QueueItem item : queueList) {
                if (item.getObjectClass().equals(className)) {
                    multiList.add(item);
                }
            }
            mapMulti.put(className, multiList);
        }
        //System.out.println("mapMulti = " + mapMulti);

        // get single
        Iterator<String> it = mapMulti.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            ArrayList<QueueItem> l = mapMulti.get(key);
            if (l.size() != 1) {
                continue;
            }

            if ((controlTime - l.get(0).getTimeMark()) < 30) {
                continue;
            }

            list.add(l.get(0));
        }

        // get multi
        it = mapMulti.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            ArrayList<QueueItem> l = mapMulti.get(key);
            if (l.size() < 2) {
                continue;
            }

            Collections.sort(l);
            if ((controlTime - l.get(0).getTimeMark()) < 30) {
                continue;
            }

            list.add(l.get(0));
        }

        return list;
    }
}
