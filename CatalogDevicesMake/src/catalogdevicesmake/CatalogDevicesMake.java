/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package catalogdevicesmake;

/**
 *
 * @author toor
 */
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import org.uui.db.DataBase;

public class CatalogDevicesMake {

    private static DataBase db;

    public static void main(String[] args) {

        Path p = Paths.get(System.getProperty("user.dir"), "db.properties");
        db = DataBase.getInstance(p.toString());


        MachinesCatalog mac = new MachinesCatalog();
        mac.setName("Вулканизатор микрон");
        mac.setPartNumber("ВЭР 1.000.000 СБ");
        db.addObject(mac);
        MachinesCatalog mac2 = new MachinesCatalog();
        mac2.setName("Станок Фаворит");
        mac2.setPartNumber("ВЭР 2.000.000 СБ");
        db.addObject(mac2);
        CatalogDevicesMake r= new CatalogDevicesMake();
                r.getAllObjects();

    }

    private void getAllObjects() {//
        ArrayList<MachinesCatalog> list5 = db.getAllObjectsList(MachinesCatalog.class.getName());
        System.out.println("DataBase consist of:\n ");
        for (MachinesCatalog mach : list5) {
            System.out.println("Machine:" + mach.getName() + " id=" + mach.getId() + "\n Internal Part number: " + mach.getPartNumber());
        }
    }
}
