package com.github.djeang.vincerdom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.function.Consumer;

public class VDocument {

    private final Document w3cDocument;

    private VDocument(Document w3cDocument) {
        this.w3cDocument = w3cDocument;
    }

    public Document getW3cDocument() {
        return w3cDocument;
    }

    public static VDocument of(String rootName) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder;
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document doc = builder.newDocument();
        Element element = doc.createElement(rootName);
        doc.appendChild(element);
        return new VDocument(doc);
    }

    public static VDocument parse(InputStream inputStream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            doc = builder.parse(inputStream);
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new VDocument(doc);
    }

    public VElement<VDocument> root() {
        Element root = w3cDocument.getDocumentElement();
        return new VElement<>(this, root);
    }

    public void print(OutputStream out) {
        print(out, configurer -> {});
    }

    public void print(OutputStream out, Consumer<Transformer> transformerConfigurer) {
        TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformerConfigurer.accept(transformer);
        try {
            transformer.transform(new DOMSource(w3cDocument),
                    new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

}
