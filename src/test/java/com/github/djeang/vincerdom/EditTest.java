package com.github.djeang.vincerdom;

import org.junit.jupiter.api.Test;

import javax.xml.xpath.XPathExpression;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class EditTest {

    @Test
    void editMavenPom() {
        InputStream is = EditTest.class.getResourceAsStream("sample-pom.xml");
        VDocument.parse(is)
            .root()
                .get("dependencies")
                    .add("dependency")
                        .add("groupId").text("com.github.djeang").__
                        .add("artifactId").text("vincer-dom").__
                        .add("version").text("0.1-SNAPSHOT").__.__
                    .add("dependency")
                        .add("groupId").text("org.junit.jupiter").__
                        .add("artifactId").text("junit-jupiter-engine").__
                        .add("version").text("5.4.0").__
                        .add("scope").text("test").__.__
                    .apply(this::removeTests).__
                .get("distributionManagement")
                    .get("repository")
                        .get("id").make().text("My repo id").__
                        .get("name").make().text("My repo name").__
                        .get("url").make().text("http://myserver::8081").__.__.__.__
            .print(System.out);
    }

    @Test
    void children() {
        InputStream is = EditTest.class.getResourceAsStream("sample-pom.xml");
        VDocument doc = VDocument.parse(is);
        List<VElement> dependencyEls = doc.root().get("dependencies").children("dependency");
        assertTrue(dependencyEls.size() > 3);
        VElement secondArtifactEl = dependencyEls.get(1).child("artifactId");
        assertEquals("hibernate-core", secondArtifactEl.getText());
    }

    @Test
    void xPath() {
        InputStream is = EditTest.class.getResourceAsStream("sample-pom.xml");
        VDocument doc = VDocument.parse(is);
        XPathExpression xPathExpression = VXPath.compile("dependency/artifactId");
        List<VElement> dependencyEls = doc.root().get("dependencies").xPath(xPathExpression);
        assertTrue(dependencyEls.size() > 3);
        assertEquals("hibernate-core", dependencyEls.get(1).getText());
    }



    private void removeTests(VElement<?> dependencies) {
        dependencies.children("dependency").stream()
            .filter(dependency -> "test".equals(dependency.get("scope").getText()))
            .forEach(VElement::remove);
    }
}
