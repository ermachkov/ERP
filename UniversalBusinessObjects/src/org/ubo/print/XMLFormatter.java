/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ubo.print;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

/**
 *
 * @author Pechenko Anton aka parilo, forpost78 aaaaaat gmail doooooot com
 *         (C) Copyright by Pechenko Anton, created 15.04.2011
 */
public class XMLFormatter {

    private static XMLFormatter self = null;

    private XMLFormatter() {
    }

    public static XMLFormatter getInstance() {
        if (self == null) {
            self = new XMLFormatter();
        }

        return self;
    }

    public String xmlXsltFromFile(String xml, String xsltFileName) {
        try {

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xsltFileName));

            StringWriter result = new StringWriter();

            transformer.transform(
                    new javax.xml.transform.stream.StreamSource(new StringReader(xml)),
                    new javax.xml.transform.stream.StreamResult(result));

            return result.toString();

        } catch (TransformerException ex) {
            System.out.println("XMLFormatter: xmlXsltFromFile: " + ex.getMessage());
            ex.printStackTrace();
            return "";
        }
    }
}
