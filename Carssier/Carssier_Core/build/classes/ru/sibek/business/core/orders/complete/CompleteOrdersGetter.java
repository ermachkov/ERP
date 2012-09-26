/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.core.orders.complete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.ubo.document.Order;
import org.ubo.tree.treefilterfolder.TreeFolderObjectsGetter;
import org.uui.db.DataBase;
import ru.sibek.database.CarssierDataBase;

/**
 *
 * @author anton
 */
public class CompleteOrdersGetter implements TreeFolderObjectsGetter {

    private int fromHours;
    private int toHours;

    public CompleteOrdersGetter(int fromHours, int toHours) {
        this.fromHours = fromHours;
        this.toHours = toHours;
    }
    
    @Override
    public List<Object> getObjects() {
        DataBase db = CarssierDataBase.getDataBase();
        ArrayList<Object> list = new ArrayList<>();
        ArrayList<Object> objs = db.getObjects(Order.class.getName(), "getWorkStatus", Order.WORK_COMPLETE);
                
        if( objs.size() > 0 ){
        
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            cal.add(Calendar.HOUR, -fromHours);
            Date from = cal.getTime();
            cal.setTime(now);
            cal.add(Calendar.HOUR, -toHours);
            Date to = cal.getTime();
            
            Iterator<Object> i = objs.iterator();
            while(i.hasNext()){
                Order ord = (Order) i.next();
                Date sortDate = ord.getClosedDate();
                if( sortDate.compareTo(from) >= 0 && sortDate.compareTo(to) <= 0 ) list.add(ord);
            }
        
        }
        
        return list;
    }
    
}
