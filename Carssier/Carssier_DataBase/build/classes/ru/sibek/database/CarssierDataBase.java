/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.sibek.database;

import java.io.File;
import org.uui.db.DataBase;

/**
 *
 * @author anton
 */
public class CarssierDataBase {

    private static DataBase db = DataBase.getInstance(System.getProperty("user.home") + File.separator
                + ".saas" + File.separator + "app" + File.separator
                + "config" + File.separator + "db.properties");

    public CarssierDataBase(){
    }

    public static synchronized DataBase getDataBase(){
        return db;
    }

}
