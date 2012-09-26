/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.goods;

import org.ubo.tree.TreeFolderBasic;

/**
 *
 * @author developer
 */
public class GoodsExchange {
    
    private static GoodsExchange self = null;
    private TreeFolderBasic currentTreeFolder;
    
    public static synchronized GoodsExchange getInstance(){
        if(self == null){
            self = new GoodsExchange();
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
