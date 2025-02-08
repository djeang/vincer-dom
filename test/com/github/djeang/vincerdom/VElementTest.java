package com.github.djeang.vincerdom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class VElementTest {

    @Test
    void testRemove_firstLevel_removed() {
        VDocument sample1 = DocSamples.pomSample();
        VElement version = sample1.root().get("version");
        version.remove();
    }

    @Test
    void testRemove_nestedLevel_removed() {
        VDocument sample1 = DocSamples.pomSample();
        VElement plugins = sample1.root().get("build").get("plugins");
        VElement firstPlugin =  plugins.get("plugin");
        int childCount = plugins.children().size();
        firstPlugin.remove();
        Assertions.assertEquals(childCount -1, plugins.children().size());

    }

    @Test
    void testChildren_withComment() {
        List<VElement<Void>> pluginEls = DocSamples.pomSample().root()
                .get("build").get("plugins").children();
        Assertions.assertEquals(3, pluginEls.size());
    }

    @Test
    void testGet_takeOnlyChildren() {
        String groupId = DocSamples.pomSample().root().get("groupId").text();
        Assertions.assertEquals("org.github.djeang", groupId);
    }

    @Test
    void testGGet_multipleSegment() {
        String parentGroupId = DocSamples.pomSample().root().get("parent").get("groupId").text();
        String parentGroupId2 = DocSamples.pomSample().root().get("parent/groupId").text();
        Assertions.assertEquals(parentGroupId, parentGroupId2);
    }

    @Test
    void testChild() {
        String groupId = DocSamples.pomSample().root()
                .child("groupId").text();
        Assertions.assertEquals("org.github.djeang", groupId);
    }

    @Test
    void testAdd_externalElement() {
        VElement firstPlugin = DocSamples.pomSample().root().get("build/plugins/plugin");

        VDocument newPom = VDocument.of("project");
        newPom.root().get("build/pluginManagement").add(firstPlugin);
        Assertions.assertEquals("org.apache.maven.plugins",
                newPom.root().get("build/pluginManagement/plugin/groupId").text());

    }




}
