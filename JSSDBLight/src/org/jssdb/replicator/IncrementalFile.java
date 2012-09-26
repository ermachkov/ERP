/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.replicator;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class IncrementalFile implements Serializable, Comparable{

    private String name;
    private long fileSize, lastModify, revision;
    public static final int ADD = Replica.ADD, MODIFY = Replica.MODIFY,
            DELETE = Replica.DELETE;
    public int type = -1;

    public IncrementalFile(String name, long fileSize, long lastModify, long revision) {
        this.name = name;
        this.fileSize = fileSize;
        this.lastModify = lastModify;
        this.revision = revision;
    }

    public IncrementalFile setAction(int type) {
        this.type = type;
        return this;
    }

    public int getAction() {
        return type;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getLastModify() {
        return lastModify;
    }

    public String getName() {
        return name;
    }

    public long getRevision() {
        return revision;
    }

    @Override
    public String toString() {
        return "IncrementalFile{" + "name=" + name + ", fileSize=" + fileSize + ", lastModify=" + lastModify + ", revision=" + revision + ", type=" + type + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IncrementalFile other = (IncrementalFile) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.fileSize != other.fileSize) {
            return false;
        }
        if (this.revision != other.revision) {
            return false;
        }
        if (this.lastModify != other.lastModify) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + (int) this.fileSize;
        hash = 97 * hash + (int) this.revision;
        hash = 97 * hash + (int) (this.lastModify ^ (this.lastModify >>> 32));
        return hash;
    }

    public String getComparableString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append(fileSize);
        sb.append(revision);
        sb.append(lastModify);
        return sb.toString();
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof IncrementalFile){
            return name.compareTo(((IncrementalFile)o).getName());
        } else {
            return -1;
        }
    }
}
