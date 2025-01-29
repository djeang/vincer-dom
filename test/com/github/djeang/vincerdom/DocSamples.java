package com.github.djeang.vincerdom;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

@Disabled
public class DocSamples {

    static VDocument pomSample() {
        return VDocument.parse(DocSamples.class.getResourceAsStream("sample-pom.xml"));
    }

    @Test
    void printAllDeps() {
        VElement root = VDocument.parse(EditTest.class.getResourceAsStream("sample-pom.xml")).root();
        List<VElement> depEls = root.child("dependencies").children("dependency");
        List<VElement> pluginEls = root.xPath("build/plugins/plugin");
        depEls.forEach(this::printDependency);
        System.out.println("---");
        pluginEls.forEach(this::printDependency);
    }

    private void printDependency(VElement el) {
        System.out.println(String.format("%s:%s:%s", el.get("groupId").text(), el.get("artifactId").text(),
                el.get("version").text()));
    }
}
