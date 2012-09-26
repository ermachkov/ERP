/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.core.reportshift.paid;

import java.util.Date;
import org.ubo.document.Order;
import org.ubo.report.ReportShift;
import org.ubo.tree.treedatesorted.TreeDateGetter;

/**
 *
 * @author anton
 */
public class ReportShiftDateGetter implements TreeDateGetter {

    @Override
    public Date getDate(Object obj) {
        ReportShift o = (ReportShift) obj;
        return o.getDatePaid();
    }
    
}
