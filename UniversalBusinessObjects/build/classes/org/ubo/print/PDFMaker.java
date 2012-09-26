/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.print;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.PageSequenceResults;
import org.apache.xmlgraphics.util.MimeConstants;
import org.xml.sax.SAXException;

/**
 *
 * @author Zubanov Dmitry, zubanov@gmail.com
 */
public class PDFMaker {
    
    public static boolean html2PDF(String pathToXSL, String pathToFO, 
            String pathToHTML, String pathToPDF, String pathToFOPConf){
        boolean result = false;
        if(makeFO(pathToXSL, pathToFO, pathToHTML)){
            result = convertFO2PDF(pathToFO, pathToPDF, pathToFOPConf);
        }
        return result;
    }
    
    private static boolean convertFO2PDF(String pathToFO, String pathToPDF, String pathToFOPConf) {
        boolean result = true;
        
        File fo = new File(pathToFO);
        File pdf = new File(pathToPDF);
        
        FopFactory fopFactory = FopFactory.newInstance();
        OutputStream out = null;

        try {
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
            foUserAgent.setProducer("UniversalUI print manager");
            foUserAgent.setCreator("UniversalUI print manager");
            foUserAgent.setCreationDate(new Date());

            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            Configuration cfg = cfgBuilder.buildFromFile(new File(pathToFOPConf));
            fopFactory.setUserConfig(cfg);
            
            // configure foUserAgent as desired
            // Setup output stream.  Note: Using BufferedOutputStream
            // for performance reasons (helpful with FileOutputStreams).
            out = new FileOutputStream(pdf);
            out = new BufferedOutputStream(out);

            // Construct fop with desired output format
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Setup JAXP using identity transformer
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(); // identity transformer

            // Setup input stream
            Source src = new StreamSource(fo);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

            // Result processing
            FormattingResults foResults = fop.getResults();
            java.util.List pageSequences = foResults.getPageSequences();
            for (java.util.Iterator it = pageSequences.iterator(); it.hasNext();) {
                PageSequenceResults pageSequenceResults = (PageSequenceResults) it.next();
                System.out.println("PageSequence "
                        + (String.valueOf(pageSequenceResults.getID()).length() > 0
                        ? pageSequenceResults.getID() : "<no id>")
                        + " generated " + pageSequenceResults.getPageCount() + " pages.");
            }
            System.out.println("Generated " + foResults.getPageCount() + " pages in total.");
            out.flush();
            out.close();

        } catch (SAXException | IOException | org.apache.avalon.framework.configuration.ConfigurationException | TransformerFactoryConfigurationError | TransformerException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            result = false;
        }
        
        return result;
    }
    
    private static boolean makeFO(String pathToXSL, String pathToFO, String pathToHTML) {
        boolean result = true;
        TransformerFactory tFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer =
                    tFactory.newTransformer(
                    new javax.xml.transform.stream.StreamSource(pathToXSL));

            transformer.transform(new javax.xml.transform.stream.StreamSource(pathToHTML),
                    new javax.xml.transform.stream.StreamResult(new FileOutputStream(pathToFO)));

        } catch (FileNotFoundException | TransformerException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
            result = false;
        }
        
        return result;
    }
    
}
