/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.tcr.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.uui.component.Component;
import org.uui.webkit.WebKitUtil;
import org.uui.db.DataBase;
import org.uui.db.Condition;
import ru.sibek.techcard.db.*;

/**
 *
 * @author toor
 */
public class ReportsCore extends Component{
    private DataBase db;
    public ReportsCore (String sessionId)
    {   
        super(sessionId);
        Path p = Paths.get(System.getProperty("user.dir"), "db.properties");
        db = DataBase.getInstance(p.toString());
        
    }
    public String getSpecificationModel(String element)
    {
        String fullspec = "",header="",nullstring="",string="",footer="",body="";
                       String specheader = "app/ui/specifications/header.html";
                       String specnullstring = "app/ui/specifications/null.html";
                       String specstring = "app/ui/specifications/string.html";
                       String specfooter="app/ui/specifications/footer.html";
                try {
                    List<String> lines = Files.readAllLines(Paths.get(specheader),
                            Charset.defaultCharset());
                    for (String line : lines) {
                        header += line;//System.out.println(line);
                    }
                   lines = Files.readAllLines(Paths.get(specnullstring),
                            Charset.defaultCharset());
                    for (String line : lines) {
                        nullstring += line;//System.out.println(line);
                    }
                    lines = Files.readAllLines(Paths.get(specstring),
                            Charset.defaultCharset());
                    for (String line : lines) {
                        string += line;//System.out.println(line);
                    }
                    lines = Files.readAllLines(Paths.get(specfooter),
                            Charset.defaultCharset());
                    for (String line : lines) {
                        footer += line;//System.out.println(line);
                    }
                    fullspec=header+nullstring+nullstring;
                    String zagolovok=string;
                    zagolovok=zagolovok.replace("{name}", "<U>Детали</U>");
                    zagolovok=zagolovok.replace("{format}", "").replace("{zona}", "").replace("{number}", "").replace("{kol}", "").replace("{description}", "").replace("{position}", "");
                    fullspec+=zagolovok;
                    
                  //Condition c1 = Condition.newCondition(Condition.EQUAL, element);
                 //ArrayList<Long> arraydocsid = ((TechnologyCard)db.getObject(TechnologyCard.class.getName(),Long.valueOf(element))).getDocuments();
                 ArrayList<DocumentState> state = db.getAllObjectsList(DocumentState.class.getName());
//               
                
                    ArrayList<MarshrutCard> mcard = null;

                     for (DocumentState st : state) {
                      if (st.getDeviceId()==Long.valueOf(element) && st.isState()==true)
                      {
                          //get spisok vsex MK u kotoryh device_id==st.getDeviceId()
                          mcard=db.getAllObjectsList(MarshrutCard.class.getName());
                          break;
                      } else System.out.println("Komplekta docum net. Sozdat specific vse ravno?");
                    }
                           
                    
                    fullspec+=nullstring;
                    for (MarshrutCard mc : mcard) {
                       
                           body+=string.replace("{number}",mc.getNumber1()).replace("{name}", mc.getPartname()).replace("{kol}", mc.getKd());
                    }
                    fullspec+=body+footer;
                   
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return fullspec;
    }
    public void ShowSpecification(String element)
    {
                   
                  JSMediator.exec(getSession(),
                        //WebKitFrame.getInstance().browserExecutor(
                        "getUICore().setTabHeader('"
                        + WebKitUtil.prepareToJS("<li>" + "<a class='tab-link' data-toggle='tab' href='#spec"+element+"'>Specification</a>" + "</li>")
                        + "');");
                          
                           JSMediator.exec(getSession(),
                        //WebKitFrame.getInstance().browserExecutor(
                        "getUICore().setTabBody('"
                        + WebKitUtil.prepareToJS("<div class='tab-pane' id='spec" + element + "'>" + getSpecificationModel(element) + "</div>")
                        + "');");  
    }
    
}
