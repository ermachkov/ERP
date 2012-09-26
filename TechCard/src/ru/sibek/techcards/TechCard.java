/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.techcards;

import ru.sibek.techcard.db.Workers;
import ru.sibek.techcard.db.Operations;
import ru.sibek.techcard.db.Machines;
import ru.sibek.techcard.db.Devices;
import ru.sibek.techcard.db.TechnologyCard;
//import ru.sibek.parsers.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.uui.db.*;
import org.jssdb.core.*;
import org.jssdb.query.*;
/*
 * *
 * @author toor
 */
public class TechCard {

    private DataBase db;

    public TechCard() {
        Path p = Paths.get(System.getProperty("user.dir"), "db.properties");
        db = DataBase.getInstance(p.toString());
    }

    public static void main(String[] args) throws IOException {
      // Расскоментить чтобы прочитать базу
      TechCard tc = new TechCard();
      tc.getAllObjects();
        
        
        
     // Расскоментить чтобы распарсить хтмл файл в базу
      /*File input = new File("/home/toor/test/index.html");
      HtmlParser parser = new HtmlParser();
      parser.visit(input, "UTF-8");*/
        System.exit(0);
    }


    private void getAllObjects() {//
        ArrayList<TechnologyCard> list1 = db.getAllObjectsList(TechnologyCard.class.getName());
        ArrayList<Operations> list2 = db.getAllObjectsList(Operations.class.getName());
        ArrayList<Workers> list3 = db.getAllObjectsList(Workers.class.getName());
        ArrayList<Devices> list4 = db.getAllObjectsList(Devices.class.getName());
        ArrayList<Machines> list5 = db.getAllObjectsList(Machines.class.getName());
        System.out.println("DataBase consist of:\n ");
        for (TechnologyCard card : list1) {
        //System.out.println("TechnologyCard:" + card.getName()+"\n List of operations id's:"+card.getOperations());
        for (Operations oper : list2) {
       // System.out.println("Operation:" + oper.getName()+" id="+oper.getId()+"\n Description:"+oper.getDescription() + "\n time:" + oper.getTime()+ "\n Workers id's list:"+oper.getWorkers()
       //         +"\n Devices id's list:" + oper.getDevices()+ "\n Machines id's list:" + oper.getMachines());
        }
        }
         for (Workers wrk : list3) {
        System.out.println("Worker:" + wrk.getStatus()+" id="+wrk.getId()+"\n Category:"+wrk.getCategory()+ "\n Price:"+wrk.getPrice());
         }
         for (Devices dev : list4) {
         System.out.println("Device:" + dev.getName()+" id="+dev.getId()+"\n Internal Part number: "+dev.getInternalPartNumber()+ "\n External Part number: " + dev.getExternalPartNumber());
         }
         for (Machines mach : list5) {
         System.out.println("Machine:" + mach.getName()+" id="+mach.getId()+"\n Internal Part number: "+mach.getInternalPartNumber()+ "\n External Part number: " + mach.getExternalPartNumber());
         }
    }
}
