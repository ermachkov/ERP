/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jssdb.tests;

import java.nio.file.Paths;
import java.util.concurrent.locks.LockSupport;
import org.jssdb.core.DBProperties;

/**
 *
 * @author Dmitry Zubanov zubanov@gmail.com
 */
public class UDPSuperNodeTest {
    
    private DBProperties dataBase;
    
    public UDPSuperNodeTest(){
        dataBase = DBProperties.getInstance();
        dataBase.setProperties(Paths.get("/home/developer/.saas/app/config/db.properties").toString());
        dataBase.initMe();
    }
    
    public static void main(String args[]){
        UDPSuperNodeTest t = new UDPSuperNodeTest();
        t.testModify();
    }
    
    private void testModify(){
        //
    }
    
}
