/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package okpdtrparser;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
/* *
 * @author toor
 */
public class OKPDTRParser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
        HtmlParser parser = new HtmlParser();
        Document doc=parser.go("http://ru.wikisource.org/wiki/%D0%9E%D0%B1%D1%89%D0%B5%D1%80%D0%BE%D1%81%D1%81%D0%B8%D0%B9%D1%81%D0%BA%D0%B8%D0%B9_%D0%BA%D0%BB%D0%B0%D1%81%D1%81%D0%B8%D1%84%D0%B8%D0%BA%D0%B0%D1%82%D0%BE%D1%80_%D0%BF%D1%80%D0%BE%D1%84%D0%B5%D1%81%D1%81%D0%B8%D0%B9_%D1%80%D0%B0%D0%B1%D0%BE%D1%87%D0%B8%D1%85,_%D0%B4%D0%BE%D0%BB%D0%B6%D0%BD%D0%BE%D1%81%D1%82%D0%B5%D0%B9_%D1%81%D0%BB%D1%83%D0%B6%D0%B0%D1%89%D0%B8%D1%85_%D0%B8_%D1%82%D0%B0%D1%80%D0%B8%D1%84%D0%BD%D1%8B%D1%85_%D1%80%D0%B0%D0%B7%D1%80%D1%8F%D0%B4%D0%BE%D0%B2/%D0%9F%D1%80%D0%BE%D1%84%D0%B5%D1%81%D1%81%D0%B8%D0%B8");
        parser.visit(doc);
    }
}
