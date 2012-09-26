/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jssdb.core.test;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia,
 * created 08.05.2010
 */
public class Person implements Serializable{
    
    private String fname, lname;
    private Date born;
    private int age;
    private static final long serialVersionUID = 234098243823481000L;

    public Person (String fName, String lName) {
        fname = fName;
        lname = lName;
    }

    public void setAge(int age){
        this.age = age;
    }

    public int getAge(){
        return this.age;
    }

    public void setBorn(Date date){
        this.born = date;
    }

    public Date getBorn(){
        return this.born;
    }

    public String getFirstName(){
        return fname;
    }

    public String getLastName(){
        return lname;
    }

    @Override
    public String toString () {
        return fname + " " + lname + ", age = " + age + ", born = " + born;
    }

}
