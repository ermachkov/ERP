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

/**
 *
 * @author toor
 */
public class HtmlParser {

    public HtmlParser() {
    }

    public Document go(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return doc;
    }

    ;
    public void visit(Document doc) throws IOException {
        int i = 1, n = 0, h = 0;
        Elements content = doc.getElementsByClass("mw-content-ltr");
        //need make object TechCard etc
        Elements uls = content.first().getElementsByTag("ul");



        for (Element ul : uls) {
            //System.out.println(ul.text());
            Elements links = ul.getElementsByTag("a");
            for (Element link : links) {
                n++;
                //System.out.println("http://ru.wikisource.org"+link.attr("href"));
                if (n < 11) {
                    Document doc2 = this.go("http://ru.wikisource.org" + link.attr("href"));
                    //получили контент по ссылке

                    Elements table = doc2.getElementsByClass("wikitable");
                    Elements trs = table.first().getElementsByTag("tr");
                    for (Element tr : trs) {
                        Elements tds = tr.getElementsByTag("td");
                        for (Element td : tds) {
                            switch (i)
                            {
                                case 1:{System.out.println(td.text());i++;break;}
                                case 2:{System.out.println(td.text());i++;break;}
                                case 3:{System.out.println(td.text());i++;break;}
                                case 4:{System.out.println(td.text());i++;break;}
                                case 5:{System.out.println(td.text());i++;break;}
                                case 6:{System.out.println(td.text());i=1;break;}
                            }
                            //System.out.println(td.text());

                        }

                    }

                }

            }

        }



    }
}
