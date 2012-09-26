/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sibek.business.print;

import org.ubo.document.Document;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public interface HTMLDocHandler {
    
    public String getHTML(Document doc, String pathToTemplate);
    
    public String getBarcode(String height, String moduleWidth, String orientation);
    
}
