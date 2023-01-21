package com.github.djeang.vincerdom;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ListIterator;

public class JdomEditTest {

    @Test
    public void editMavenPomWithJdom() {
        InputStream is = JdomEditTest.class.getResourceAsStream("sample-pom.xml");
        final Document document;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document w3cDocument = builder.parse(is);
            document = new DOMBuilder().build(w3cDocument);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        Element root = document.getRootElement();
        Element dependencies = root.getChild("dependencies");
        dependencies.addContent
                (new Element("dependency")
                    .addContent(new Element("group").setText("com.github.djeang"))
                    .addContent(new Element("artifactId").setText("vincer-dom"))
                    .addContent(new Element("version").setText("0.1-SNAPSHOT"))
                );
        dependencies.addContent
                (new Element("dependency")
                    .addContent(new Element("group").setText("org.junit.jupiter"))
                    .addContent(new Element("artifactId").setText("unit-jupiter-engine"))
                    .addContent(new Element("version").setText("5.4.0"))
                    .addContent(new Element("scope").setText("test"))
                );
        removeTests(dependencies);
        Element distributionManagement = getOrCreate(root, "distributionManagement");
        Element repository = getOrCreate(distributionManagement, "repository");
        getOrCreate(repository, "id").setText("My repo id");
        getOrCreate(repository, "name").setText("My repo name");
        getOrCreate(repository, "url").setText("http://myserver::8081");
        final XMLOutputter xmlOutputter = new XMLOutputter();
        try {
            xmlOutputter.output(document, System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Element getOrCreate(Element parent, String name) {
        Element element = parent.getChild(name);
        if (element == null) {
            element = new Element(name);
            parent.addContent(element);
        }
        return element;
    }

    private void removeTests(Element dependencies) {
        for (ListIterator<Element> it = dependencies.getChildren().listIterator();it.hasNext();) {
            Element dependency = it.next();
            if ("test".equals(dependency.getChildText("scope"))) {
                it.remove();
            }
        }
    }
}
