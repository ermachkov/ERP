/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com
 * Omsk, Russia, created 31.03.2011
 * (C) Copyright by Zubanov Dmitry
 */
package org.ubo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class XPathUtil {

    public static String getNodeValue(String XPath, String pathToXML, String itemName) {
        String result = "undefined";

        try {
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pathToXML);
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(XPath);
            Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
            if (node == null) {
                return result;
            }

            NamedNodeMap nnm = node.getAttributes();
            if (nnm == null) {
                return result;
            }

            Node nResult = nnm.getNamedItem(itemName);
            if (nResult == null) {
                return result;
            }

            result = nResult.getNodeValue();

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | DOMException e) {
            Logger.getGlobal().log(Level.WARNING, XPath, e);
            System.err.println(e);
        }
        return result;
    }
    
    public static String getNodeValueFromXMLString(String XPath, String xml, String itemName) {
        String result = "undefined";

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bais);
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(XPath);
            Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
            if (node == null) {
                return result;
            }

            NamedNodeMap nnm = node.getAttributes();
            if (nnm == null) {
                return result;
            }

            Node nResult = nnm.getNamedItem(itemName);
            if (nResult == null) {
                return result;
            }

            result = nResult.getNodeValue();

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | DOMException e) {
            Logger.getGlobal().log(Level.WARNING, XPath, e);
            System.err.println(e);
        }
        return result;
    }
    
    public static String getTextValueFromXMLString(String XPath, String xml) {
        String result = "undefined";

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bais);
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(XPath);
            Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
            if (node == null) {
                return result;
            }

            result = node.getTextContent();

        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | DOMException e) {
            Logger.getGlobal().log(Level.WARNING, XPath, e);
            System.err.println(e);
        }
        
        if(result.equals("undefined")){
            Logger.getGlobal().log(Level.WARNING, "return undefined\n for {0},\n{1}", new Object[]{XPath, xml});
        }
        return result;
    }
    
    public static ArrayList<Map<String, String>> getNodesFromXMLString(String XPath, String xml) {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bais);
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(XPath);
            NodeList nodeList = (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NamedNodeMap nnm = node.getAttributes();
                Map<String, String> map = new HashMap<>();
                for (int j = 0; j < nnm.getLength(); j++) {
                    map.put(nnm.item(j).getNodeName(), nnm.getNamedItem(nnm.item(j).getNodeName()).getNodeValue());
                }
                list.add(map);
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | DOMException e) {
            Logger.getGlobal().log(Level.WARNING, XPath, e);

        }
        
        return list;
    }
    
    public static ArrayList<Map<String, String>> getNodesTextFromXMLString(String XPath, String xml) {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(bais);
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(XPath);
            NodeList nodeList = (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                Map<String, String> map = new HashMap<>();
                map.put(node.getNodeName(), node.getTextContent());
                list.add(map);
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | DOMException e) {
            Logger.getGlobal().log(Level.WARNING, XPath, e);

        }
        
        return list;
    }

    public static ArrayList<Map<String, String>> getNodes(String XPath, String pathToXML) {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        
        try {
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pathToXML);
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(XPath);
            NodeList nodeList = (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NamedNodeMap nnm = node.getAttributes();
                Map<String, String> map = new HashMap<>();
                for (int j = 0; j < nnm.getLength(); j++) {
                    map.put(nnm.item(j).getNodeName(), nnm.getNamedItem(nnm.item(j).getNodeName()).getNodeValue());
                }
                list.add(map);
            }
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | DOMException e) {
            Logger.getGlobal().log(Level.WARNING, XPath, e);

        }
        
        return list;
    }
    
    public static void setValue(String pathToXML, String xPathString, Map<String, String> attr) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            dbf.setNamespaceAware(true);
            Document xmlDocument = dbf.newDocumentBuilder().parse(pathToXML);
            XPath xPath = XPathFactory.newInstance().newXPath();

            xPath.setNamespaceContext(new UniversalNamespaceResolver(xmlDocument));
            
            XPathExpression xPathExpression = xPath.compile(xPathString);
            Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
            if (node == null) {
                return;
            }

            Iterator<String> it = attr.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                Node n = node.getAttributes().getNamedItem(key);
                if (n == null) {
                    Element element = (Element)node;
                    element.setAttribute(key, attr.get(key));
                    continue;
                }

                n.setNodeValue(attr.get(key));
            }

            storeXML(xmlDocument, pathToXML);

        } catch(ParserConfigurationException | SAXException | IOException | XPathExpressionException | DOMException | TransformerException ex) {
            Logger.getGlobal().log(Level.SEVERE, pathToXML, ex);
        }
    }
    
    private static synchronized void storeXML(Document doc, String pathToXML) throws TransformerConfigurationException, IOException, TransformerException {
        doc.normalize();
        TransformerFactory tranFactory = TransformerFactory.newInstance();
        Transformer aTransformer = tranFactory.newTransformer();
        aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult dest = new StreamResult(baos);
        aTransformer.transform(new DOMSource(doc), dest);
        String xml = new String(baos.toByteArray());
        try (FileWriter fw = new FileWriter(pathToXML)) {
            fw.write(xml);
            fw.flush();
        }
    }
}
