/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.tests;

import java.nio.file.Paths;
import org.jssdb.core.DBProperties;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class UDPNodeTest {
    
    private DBProperties dataBase;
    
    public UDPNodeTest(){
        dataBase = DBProperties.getInstance();
        dataBase.setProperties(Paths.get("/home/granat/temp/db2.properties").toString());
        dataBase.initMe();
    }
    
    public static void main(String args[]){
        UDPNodeTest t = new UDPNodeTest();
    }
    
}
