/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.service;

import org.ubo.tree.TreeFolderBasic;

/**
 *
 * @author developer
 */
public class ServiceExchange {
    
    private static ServiceExchange self = null;
    private TreeFolderBasic currentTreeFolder;
    
    public static synchronized ServiceExchange getInstance(){
        if(self == null){
            self = new ServiceExchange();
        }
        
        return self;
    }
    
    public void setCurrentTreeFolder(TreeFolderBasic treeFolder){
        currentTreeFolder = treeFolder;
    }
    
    public TreeFolderBasic getCurrentTreeFolder(){
        return currentTreeFolder;
    }
}
