/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jssdb.core.test;

import java.io.Serializable;

/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia,
 * created 22.05.2010
 */
public class MyPerson implements Serializable{
    
    String name;

    public MyPerson(String name){
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }

}
