package com.github.djeang.vincerdom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Wrapper for {@link org.w3c.dom.Document} offering a Parent-Chaining fluent interface.
 *
 * @author Jerome Angibaud
 */
public final class VDocument {

    private final Document w3cDocument;

    private final Path fileHolder;

    private VDocument(Document w3cDocument, Path fileHolder) {
        this.w3cDocument = w3cDocument;
        this.fileHolder = fileHolder;
    }

    /**
     * Creates a {@link VDocument} wrapping the specified w3c document.
     */
    public static VDocument of(Document w3cDocument) {
        return new VDocument(w3cDocument, null);
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
        return new VDocument(doc, null);
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
        return new VDocument(doc, null);
    }

    /**
     * Creates a {@link VDocument} by parsing the content of specified string.
     */
    public static VDocument parse(String xml) {
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        return parse(stream);
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
     * Creates a {@link VDocument} by parsing the content of the specified path.
     */
    public static VDocument parse(Path xmlFile) {
        final DocumentBuilder builder;
        Document doc;
        try (InputStream inputStream = Files.newInputStream(xmlFile)) {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
        return new VDocument(doc, xmlFile);
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
     * Converts the current document into a string representation of its XML content.
     * This method generates the XML content as UTF-8 encoded text.
     */
    public String printAsString() {
        return printAsString(transformerConfigurer -> {});
    }

    public String printAsString(Consumer<Transformer> transformerConfigurer) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        print(baos, transformerConfigurer);
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
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

    /**
     * Saves the current document to the specified output file.
     *
     * @param outputFile the path to the file where the document will be saved.
     * @param openOptions options specifying how the file is opened or created. These are the same options
     *                    supported by {@link Files#newOutputStream}.
     * @throws UncheckedIOException if an I/O error occurs while writing to the file.
     */
    public void save(Path outputFile, OpenOption... openOptions) {
        OutputStream out = null;
        try {
            out = Files.newOutputStream(outputFile, openOptions);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        print(out);
    }

    /**
     * Saves the current document to the file it was initially loaded from.
     *
     * @param openOptions options specifying how the file is opened or created.
     *                    These are the same options supported by {@link Files#newOutputStream}.
     * @throws IllegalStateException if the document has not been created from an existing file.
     */
    public void save(OpenOption... openOptions) {
        if (fileHolder == null) {
            throw new IllegalStateException("This document has not been created from an existing file. " +
                    "Use `#save(Path)` method instead.");
        }
        save(fileHolder, openOptions);
    }

    /**
     * Returns an unmodifiable list of elements matching the specified xPath expression.
     */
    public List<VElement<Void>> xPath(XPathExpression xPathExpression) {
        List<VElement<Void>> result = new LinkedList<>();
        final NodeList nodeList;
        try {
            nodeList = (NodeList) xPathExpression.evaluate(this.w3cDocument, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Error when evaluating xPath expression " + xPathExpression, e);
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            VElement<Void> el = new VElement(this, (Element) nodeList.item(i));
            result.add(el);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable list of elements matching the specified xPath expression.
     */
    public List<VElement<Void>> xPath(String xPathExpression, Object... items) {
        XPathExpression compiledExpression = VXPath.compile(xPathExpression, items);
        return xPath(compiledExpression);
    }

}
