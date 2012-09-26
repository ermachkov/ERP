/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Zubanov Dmitry aka javadimon, zubanov@gmail.com Omsk, Russia, created
 * 09.02.2011 (C) Copyright by Zubanov Dmitry
 */
package org.ubo.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SystemXML {

    private static SystemXML self = null;
    Path p;

    private SystemXML(Path path) {
        if (!path.toFile().exists()) {
            try {
                try (BufferedWriter bw = Files.newBufferedWriter(path, Charset.forName("utf-8"),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.WRITE)) {
                    bw.write("<?xml version=\"1.0\" encoding=\"utf-8\" "
                            + "standalone=\"no\"?>\n<root>\n</root>");
                    bw.flush();
                }
            } catch (IOException ex) {
                Logger.getGlobal().log(Level.WARNING, Objects.toString(p), ex);
            }
        }
        p = path;
    }

    public static synchronized SystemXML newSystemXML(Path path) {
        if (self == null) {
            self = new SystemXML(path);
        }

        return self;
    }

    public boolean isPresent(String xPathString) {
        boolean result = false;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            Document xmlDocument = dbf.newDocumentBuilder().parse(p.toString());
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(xPathString);
            Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
            if (node != null) {
                result = true;
            }

        } finally {
            return result;
        }
    }

    public boolean setValue(String xPathString, Map<String, String> attr) {
        boolean result = false;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            Document xmlDocument = dbf.newDocumentBuilder().parse(p.toString());
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(xPathString);
            Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
            if (node == null) {
                return result;
            }

            boolean hasFail = false;
            Iterator<String> it = attr.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                Node n = node.getAttributes().getNamedItem(key);
                if (n == null) {
                    Element element = (Element) node;
                    element.setAttribute(key, attr.get(key));
                    continue;
                }

                n.setNodeValue(attr.get(key));
            }

            storeXML(xmlDocument);
            result = hasFail ? false : true;

        } finally {
            return result;
        }
    }

    public boolean setValue(String xPathString, String value) {
        boolean result = false;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            Document xmlDocument = dbf.newDocumentBuilder().parse(p.toString());
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(xPathString);
            Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
            if (node == null) {
                return result;
            }

            node.setTextContent(value);
            storeXML(xmlDocument);

            result = true;

        } finally {
            return result;
        }
    }

    public boolean addNode(String xPathString, String nodeName, Map<String, String> attr) {
        boolean result = false;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            Document xmlDocument = dbf.newDocumentBuilder().parse(p.toString());
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(xPathString);
            Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
            if (node == null) {
                return result;
            }

            Element newNode = xmlDocument.createElement(nodeName);
            Iterator<String> it = attr.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                newNode.setAttribute(key, attr.get(key));
            }

            node.appendChild(newNode);
            storeXML(xmlDocument);
            result = true;

        } finally {
            return result;
        }
    }

    /**
     *
     * @param xPathString
     * @param isTextContent
     * @return "undefined" or value
     */
    public String getValue(String xPathString, boolean isTextContent) {
        String value = "undefined";

        try {
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(p.toString());
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(xPathString);
            Node node = (Node) xPathExpression.evaluate(xmlDocument, XPathConstants.NODE);
            if (node == null) {
                return value;
            }

            if (isTextContent) {
                value = node.getTextContent();
            } else {
                value = node.getNodeValue();
            }

        } finally {
            return value;
        }
    }

    public List<String> getValues(String xPathString, boolean isTextContent) {
        List<String> values = new ArrayList<>();

        try {
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(p.toString());
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(xPathString);
            NodeList nodeList = (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
            if (nodeList == null) {
                return values;
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                if (isTextContent) {
                    values.add(nodeList.item(i).getTextContent());
                } else {
                    values.add(nodeList.item(i).getNodeValue());
                }
            }

        } finally {
            return values;
        }
    }

    public Map<String, String> getValues(String xPathString) {
        Map<String, String> values = new HashMap<>();

        try {
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(p.toString());
            XPath xPath = XPathFactory.newInstance().newXPath();

            XPathExpression xPathExpression = xPath.compile(xPathString);
            NodeList nodeList = (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
            if (nodeList == null) {
                return values;
            }

            for (int i = 0; i < nodeList.getLength(); i++) {
                values.put(nodeList.item(i).getNodeName(), nodeList.item(i).getNodeValue());
            }

        } finally {
            return values;
        }
    }

    private synchronized void storeXML(Document doc) throws TransformerConfigurationException, IOException, TransformerException {
        doc.normalize();
        TransformerFactory tranFactory = TransformerFactory.newInstance();
        Transformer aTransformer = tranFactory.newTransformer();
        aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult dest = new StreamResult(baos);
        aTransformer.transform(new DOMSource(doc), dest);
        String xml = new String(baos.toByteArray());
        try (FileWriter fw = new FileWriter(p.toString())) {
            fw.write(xml);
            fw.flush();
        }
    }
}
