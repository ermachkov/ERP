/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.query;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public abstract class RequestCondition {
    
    private Path rootPath;

    public RequestCondition() {
        //
    }
    
    public void setRootPath(Path rootPath){
        this.rootPath = rootPath;
    }
    
    public Path getRootPath(){
        return rootPath;
    }

    public abstract ArrayList<Long> getResultSet();

}
