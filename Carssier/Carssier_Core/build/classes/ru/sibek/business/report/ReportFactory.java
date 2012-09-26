/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.report;

import org.ubo.report.ReportException;
import org.ubo.report.ReportHandler;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class ReportFactory {
    
    public static final int REPORT_SALARY = 0;
    
    public static ReportHandler newReportFactory(int documentType, long reportBundleId) throws ReportException{
        
        ReportHandler reportHandler = null;
        
        switch(documentType){
            case 0:
                reportHandler = new ReportSales(reportBundleId);
                break;
        }
        
        return reportHandler;
    }
    
}
