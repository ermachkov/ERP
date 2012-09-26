/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.replicator;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class IncrementalFileList implements Serializable {

    private String className;
    private Set<IncrementalFile> files = new HashSet<>();

    public IncrementalFileList(String className) {
        this.className = className;
    }

    public boolean addFileInfo(String name, long fileSize, long lastModified, long revision) {
        return files.add(new IncrementalFile(name, fileSize, lastModified, revision));
    }

    public Set<IncrementalFile> getFiles() {
        return files;
    }

    public String getClassName() {
        return className;
    }

    public boolean isPresentFile(String name) {
        for (IncrementalFile incrementalFile : files) {
            if (incrementalFile.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public boolean isEqual(IncrementalFile incrementalFile) {
        boolean result = false;

        for (IncrementalFile _incrementalFile : files) {
            if (!_incrementalFile.getName().equals(incrementalFile.getName())) {
                continue;
            }

            result = true;

            if (_incrementalFile.getFileSize() != incrementalFile.getFileSize()) {
                return false;
            }

            if (_incrementalFile.getLastModify() != incrementalFile.getLastModify()) {
                return false;
            }

            if (_incrementalFile.getRevision() != incrementalFile.getRevision()) {
                return false;
            }
            
            return result;
        }

        return result;
    }

    @Override
    public String toString() {
        return "IncrementalFileList{" + "className=" + className + ", files=" + files + '}';
    }
}
