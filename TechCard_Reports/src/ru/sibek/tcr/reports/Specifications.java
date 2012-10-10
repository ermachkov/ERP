/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.tcr.reports;
import java.io.*;
import java.util.ArrayList;
import ru.sibek.tcr.core.ReportsCore;
/**
 *
 * @author toor
 */
public class Specifications {

ReportsCore rc=null;
String model="";
public Specifications(String sessionId) {

    rc=new ReportsCore(sessionId);
    }
public void ShowSpecification(String element)
    {
        rc.ShowSpecification(element);
    }

    public String getModel(String element) {
       model= rc.getSpecificationModel(element);
        
      return model;  
    }
}
