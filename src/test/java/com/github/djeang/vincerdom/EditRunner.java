package com.github.djeang.vincerdom;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class EditRunner {

    @Test
    public void editMavenPom() {
        InputStream is = EditRunner.class.getResourceAsStream("sample-pom.xml");
        VDocument doc = VDocument.parse(is);
        doc.root()
            .get("dependencies")
                .add("dependency")
                    .add("groupId").text("com.github.djeang").__
                    .add("artifactId").text("vincer-dom").__
                    .add("version").text("0.1-SNAPSHOT").__.__
                .apply(this::removeTests).__
            .get("distributionManagement")
                .get("repository")
                    .get("url").text("http://myserver::8081");
        doc.print(System.out);
    }

    private void removeTests(VElement<?> dependencies) {
        dependencies.getAll("dependency").stream()
            .filter(dependency -> "test".equals(dependency.get("scope").getText()))
            .forEach(dep -> dep.remove());
    }
}
