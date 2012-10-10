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
                    zagolovok=zagolovok.replace("{U}", "<U>").replace("{!U}", "</U>").replace("{name}", "Детали");
                    zagolovok=zagolovok.replace("{format}", "").replace("{zona}", "").replace("{number}", "").replace("{kol}", "").replace("{description}", "").replace("{position}", "");
                    fullspec+=zagolovok;
                    
                  Condition c1 = Condition.newCondition(Condition.EQUAL, element);
                 
                  //ArrayList  personList = db.getFilteredResultList(MarshrutCard.class.getName(), "getAge", c1);
                    
                   MachinesCatalog mc = (MachinesCatalog)db.getObject(MachinesCatalog.class.getName(), Long.valueOf(element));
                    //ArrayList<TechnologyCard> tcard = db.getAllObjectsList(TechnologyCard.class.getName(),mc.getTechCard());
                   ArrayList<TechnologyCard> tcard = new ArrayList();
                    for (Long p : mc.getTechCard()) {
                        tcard.add((TechnologyCard) db.getObject(TechnologyCard.class.getName(), p));
                    }
                    
                    fullspec+=nullstring;
                    for (TechnologyCard tc : tcard) {
                       
                           body+=string.replace("{number}",tc.getNumber()).replace("{name}", tc.getName());
                    }
                    fullspec+=body+footer;
                    // int tt=mcard.getOperations().size();
                    /*for (int c = 0; c < mcard.getOperations().size(); c++) {
                        lines = Files.readAllLines(Paths.get(formbody),
                                Charset.defaultCharset());
                        for (String line : lines) {
                            body += line;//System.out.println(line);
                        }
                        //Operations oper  = operations.get(c);//(Operations)db.getObject(Operations.class.getName(), operations.get(c));
                        body = body.replace("{ceh}", operations.get(c).getCeh()).replace("{uch}", operations.get(c).getUch());
                        body = body.replace("{rm}", operations.get(c).getRm()).replace("{oper}", String.valueOf(operations.get(c).getNumber()));
                        body = body.replace("{opername}", operations.get(c).getOpername()).replace("{sm}", operations.get(c).getSm());
                        body = body.replace("{prof}", operations.get(c).getProf()).replace("{r}", operations.get(c).getR());
                        body = body.replace("{ut}", operations.get(c).getUt()).replace("{kr}", operations.get(c).getKr());
                        body = body.replace("{koid}", operations.get(c).getKoid()).replace("{en}", operations.get(c).getEn());
                        body = body.replace("{Ksh}", operations.get(c).getKst()).replace("{Tpz}", operations.get(c).getTpz()).replace("{Tsh}", operations.get(c).getTsh());
                        body = body.replace("{devicename}", operations.get(c).getDevicename()).replace("{docname}", operations.get(c).getDocname());

                    }
                    lines = Files.readAllLines(Paths.get(formfooter),
                            Charset.defaultCharset());
                    for (String line : lines) {
                        footer += line;//System.out.println(line);
                    }*/
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
