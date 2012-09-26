/*
 * Не дописан, есть баги...и нету проверок на валидность
 */
package ru.sibek.parsers;

import ru.sibek.techcard.db.Workers;
import ru.sibek.techcard.db.Operations;
import ru.sibek.techcard.db.Machines;
import ru.sibek.techcard.db.Devices;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.uui.db.DataBase;
import org.w3c.dom.*;
import org.xml.sax.*;
/**
 *
 * @author toor
 */
/*public class CardXmlParser {
private DataBase db;

    public CardXmlParser() {
        Path p = Paths.get(System.getProperty("user.dir"), "db.properties");
        db = DataBase.getInstance(p.toString());  
    } 
private Document doc = null;
private String txt = ""; 
private Operations card = null;
private Workers worker = null;
private Devices device = null;
private Machines machine = null;
boolean workerflag = false;
boolean deviceflag = false;
boolean operationflag=false;
boolean timeflag=false;
ArrayList<String> workers = new ArrayList();
ArrayList<String> devices = new ArrayList();;
ArrayList<String> machines = null;
// fName - имя хмл файла
public void DomHostsParser(String fName) 
{
  try
  {
   doc = parserXML(new File(fName));
   visit(doc, 0);
   
  }
  catch(Exception error)
  {
   error.printStackTrace();
  }
}

public void visit(Node node, int level) 
{
  NodeList nl = node.getChildNodes();  
  int g =node.getChildNodes().getLength();
  String parent="";
  for(int i=0, cnt=nl.getLength(); i<cnt; i++)
  {   
   if (nl.item(i).getNodeType()==Node.TEXT_NODE){ // if1
    parent=nl.item(i).getParentNode().getNodeName();
    txt=nl.item(i).getNodeValue();
    switch (parent)
    {
        case "name":
        {
            card.setName(txt);
            break;
        }
            
        case "description":
        {
            card.setDescription(txt);
            break;
        }
        
        case "link":
        {
            card.setLink(txt);
            break;
        }
        
        case "time":
        {
           //card.setTime(txt);
           break;
        }
        
        case "post":
        {
            worker.setStatus(txt);
            break;
        }
        
        case "category":
        {
            worker.setCategory(txt);
            break;
        }
        
        case "price":
        {
          worker.setPrice(Float.valueOf(txt));
          workerflag = true;
          break;
        }
        
        case "devicename":
        {
           device.setName(txt);
           break;
        }
            
        case "code":
        {
          device.setExternalPartNumber(txt);
          device.setInternalPartNumber(txt);
          deviceflag = true;
          break;
        }
            
    }

   } else {
        
    if (nl.item(i).getNodeName().equals("operation")){
      //  if (operationflag) {db.addObject(card);operationflag=false;}
        card=new Operations();
        operationflag=true;
    }
     if (nl.item(i).getNodeName().equals("worker")){ 
        worker=new Workers();
        
    }
     if (nl.item(i).getNodeName().equals("device")){
     //    
        device = new Devices();
     } 
     
    }
   //worker gotov
   if (workerflag) {
       long id = db.addObject(worker);
       workers.add(String.valueOf(id));
       card.setWorkers(workers);
       workerflag=false;
       devices.clear();
   }
      if (deviceflag) {
       long id = db.addObject(device);
       devices.add(String.valueOf(id));
       card.setDevices(devices); 
      deviceflag=false;
      workers.clear();
      } 
   //System.out.println(nl.item(i).getNodeName() + " = " + nl.item(i).getNodeValue());
   visit(nl.item(i), level+1);
  }
   // if ((card!=null)&&(card.getName()!="")&&(operationflag)) {
     //   db.addObject(card);}
    
}
public static Document parserXML(File file) throws SAXException, IOException, ParserConfigurationException 
{
  return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
}
/*public List getStds() {
  return stds;
}
public void setStds(List stds) {
  this.stds = stds;
}*/
////////}
