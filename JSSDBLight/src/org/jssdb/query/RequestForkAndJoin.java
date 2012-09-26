/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class RequestForkAndJoin extends RecursiveAction {

    private ArrayList<CacheData> mSource;
    private int mStart, mLength;
    private String className;
    public Map<Long, Object> dest;
    private static int sThreshold = 100;

    public RequestForkAndJoin(ArrayList<CacheData> source, int start,
            int length, Map<Long, Object> dest, String className) {
        this.mSource = source;
        this.mStart = start;
        this.mLength = length;
        this.className = className;
        this.dest = dest;

        if (dest == null) {
            this.dest = new HashMap<>();
        }
    }

    protected void fillDirectly() {
        for (int index = mStart; index < mStart + mLength; index++) {
            CacheData cd = mSource.get(index);
            if (cd.getClassName().equals(className)) {
                dest.put(cd.getId(), cd.getObject());
            }
        }
    }

    @Override
    protected void compute() {
        if (mLength < sThreshold) {
            fillDirectly();
            return;
        }

        int split = mLength / 2;

        invokeAll(new RequestForkAndJoin(mSource, mStart, split, dest, className),
                new RequestForkAndJoin(mSource, mStart + split, mLength - split,
                dest, className));
    }
}
