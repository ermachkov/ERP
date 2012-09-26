/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.print;

import org.ubo.document.Document;
import ru.sibek.business.core.Carssier;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class HTMLDocHandlerFactory {
    
    public static HTMLDocHandler getHandler(final String documentName, Document doc){
        HTMLDocHandler docHandler = null;
        switch(documentName){
            case Carssier.ACT:
                docHandler = new ACTHandler();
                break;
                
            case Carssier.BILL:
                docHandler = new BillHandler();
                break;
                
            case Carssier.INVOICE:
                docHandler = new InvoiceHandler();
                break;
                
            case Carssier.BILL_POS:
                docHandler = new POSBillHandler();
                break;
        }
        
        return docHandler;
    }
    
}
