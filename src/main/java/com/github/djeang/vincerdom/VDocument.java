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
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Wrapper for {@link org.w3c.dom.Document} offering a Parent-Chaining fluent interface.
 *
 * @author Jerome Angibaud
 */
public final class VDocument {

    private final Document w3cDocument;

    private VDocument(Document w3cDocument) {
        this.w3cDocument = w3cDocument;
    }

    /**
     * Creates a {@link VDocument} wrapping the specified w3c document.
     */
    public static VDocument of(Document w3cDocument) {
        return new VDocument(w3cDocument);
    }

    /**
     * Creates a document with a root element of the specified name.
     */
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

    /**
     * Creates a {@link VDocument} by parsing the content of specified input stream.
     * The stream content is parsed with the specified documentBuilder.
     */
    public static VDocument parse(InputStream inputStream, DocumentBuilder documentBuilder) {
        Document doc;
        try {
            doc = documentBuilder.parse(inputStream);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new VDocument(doc);
    }

    /**
     * Same as {@link #parse(InputStream, DocumentBuilder)} but using a default {@link DocumentBuilder}.
     */
    public static VDocument parse(InputStream inputStream) {
        final DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return parse(inputStream, builder);
    }

    /**
     * Returns thd underlying w3c {@link Document}.
     */
    public Document getW3cDocument() {
        return w3cDocument;
    }

    /**
     * Returns the root element of this document.
     */
    public VElement<VDocument> root() {
        Element root = w3cDocument.getDocumentElement();
        return VElement.of(this, root);
    }



    /**
     * Outputs xml in the specified stream.
     */
    public void print(OutputStream out) {
        print(out, configurer -> {});
    }

    /**
     * Same as {@link #print(OutputStream)} but caller can modify the default XML transformer using the
     * specified {@link Consumer<Transformer>}.
     */
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
                    new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
