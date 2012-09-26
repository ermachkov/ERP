/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.print;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ubo.document.Document;
import org.ubo.document.Order;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class InvoiceHandler implements HTMLDocHandler{

    @Override
    public String getHTML(Document doc, String pathToTemplate) {
        String html = "";
        Order order = (Order)doc;
        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(pathToTemplate), Charset.forName("utf-8"));
            String str = null;
            while((str = br.readLine()) != null){
                html += str + "\n";
            }
            
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, pathToTemplate, ex);
        }
        
        return html;
    }

    @Override
    public String getBarcode(String height, String moduleWidth, String orientation) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
