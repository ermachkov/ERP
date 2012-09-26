/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.parsers;

import ru.sibek.techcard.db.Workers;
import ru.sibek.techcard.db.Operations;
import ru.sibek.techcard.db.Machines;
import ru.sibek.techcard.db.Devices;
import ru.sibek.techcard.db.TechnologyCard;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import org.uui.db.DataBase;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

/**
 *
 * @author Borodulin Igor argen666@gmail.com
 */
/*public class HtmlParser {

    private DataBase db;

    public HtmlParser() {
        Path p = Paths.get(System.getProperty("user.dir"), "db.properties");
        db = DataBase.getInstance(p.toString());
    }
    private Document doc = null;
    private Operations operation_obj = null;
    private Workers worker_obj = null;
    private Devices device_obj = null;
    private Machines machine_obj = null;
    private TechnologyCard card_obj = null;
    ArrayList<String> workers_list = null;
    ArrayList<String> devices_list = null;
    ArrayList<String> machines_list = null;
    ArrayList<String> operations_list = null;
    long operation_id = 0;
// fName - имя хмл файла

    public void visit(File file, String charset) throws IOException {
        //File input = new File("/home/toor/test/index.html");
        doc = Jsoup.parse(file, charset);//.get();
        Elements cards = doc.getElementsByClass("tech_card");
        //need make object TechCard etc

        for (Element card : cards) {
            card_obj = new TechnologyCard();
            card_obj.setName(card.getElementsByClass("name_card").text());
            // System.out.println(card.getElementsByClass("name_card").text());
            Elements operations = card.getElementsByClass("operation");
            operations_list = new ArrayList();
            for (Element operation : operations) {
                operation_obj = new Operations();
                operation_obj.setName(operation.getElementsByClass("operation_name").text());
                //System.out.println(operation.getElementsByClass("operation_name").text());
                operation_obj.setDescription(operation.getElementsByClass("description").text());
                //System.out.println(operation.getElementsByClass("description").text());
                operation_obj.setTime(operation.getElementsByClass("time").text());
                operation_obj.setLink(operation.getElementsByTag("a").attr("href"));
                //System.out.println(operation.getElementsByTag("a").attr("href"));
                //operation_obj.setWeight(operation.getElementsByTag("weight").text());
                Elements workers = operation.getElementsByClass("workers");
                workers_list = new ArrayList();
                for (Element worker : workers) {
                    worker_obj = new Workers();
                    worker_obj.setStatus(worker.getElementsByClass("worker_name").text());
                    //System.out.println(worker.getElementsByClass("worker_name").text());
                    //worker_obj.setTime(worker.getElementsByClass("time").text());
                    //System.out.println(worker.getElementsByClass("time").text());
                    worker_obj.setCategory(worker.getElementsByClass("category").text());
                    //System.out.println(worker.getElementsByClass("category").text());
                    worker_obj.setPrice(Float.valueOf(worker.getElementsByClass("price").text()));
                    // System.out.println(worker.getElementsByClass("price").text());
                    long id = db.addObject(worker_obj);
                    workers_list.add(String.valueOf(id));
                }
                Elements devices = operation.getElementsByClass("devices");
                devices_list = new ArrayList();
                for (Element device : devices) {
                    device_obj = new Devices();
                    device_obj.setName(device.getElementsByClass("device_name").text());
                    //System.out.println(device.getElementsByClass("device_name").text());
                    device_obj.setExternalPartNumber(device.getElementsByClass("device_code").text());
                    device_obj.setInternalPartNumber(device.getElementsByClass("device_code").text());
                    //System.out.println(device.getElementsByClass("device_code").text());
                    long id = db.addObject(device_obj);
                    devices_list.add(String.valueOf(id));
                }
                Elements machines = operation.getElementsByClass("machines");
                //if (machines.size()!=0){
                machines_list = new ArrayList();
                for (Element machine : machines) {
                    machine_obj = new Machines();
                    machine_obj.setName(machine.getElementsByClass("machine_name").text());
                    machine_obj.setExternalPartNumber(machine.getElementsByClass("machine_code").text());
                    machine_obj.setInternalPartNumber(machine.getElementsByClass("machine_code").text());
                    //System.out.println(machine.getElementsByClass("machine_name").text());
                    //System.out.println(machine.getElementsByClass("machine_code").text());
                    long id = db.addObject(machine_obj);
                    machines_list.add(String.valueOf(id));
                }
                //} else  System.out.println("nope");

                operation_obj.setWorkers(workers_list);
                operation_obj.setDevices(devices_list);
                operation_obj.setMachines(machines_list);
                operation_id = db.addObject(operation_obj);
                operations_list.add(String.valueOf(operation_id));
            }
            //operations_list.add(String.valueOf(operation_id));
            //card_obj.setOperations(operations_list);
            long id = db.addObject(card_obj);
            //operation_id=0;
        }


    }
}
*/