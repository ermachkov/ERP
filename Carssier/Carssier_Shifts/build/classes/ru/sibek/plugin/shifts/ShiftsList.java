/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.plugin.shifts;

import java.util.ArrayList;
import org.ubo.json.JSONObject;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ShiftsList {
    
    public static ArrayList<ShiftListElement> getShifts(ArrayList<JSONObject> data){
        ArrayList<ShiftListElement> list = new ArrayList<>();
        for(JSONObject json : data){
            list.add(new ShiftListElement(json));
        }
        
        return list;
    }
    
}
