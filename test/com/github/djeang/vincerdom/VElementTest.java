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
        List<VElement> pluginEls = DocSamples.pomSample().root()
                .get("build").get("plugins").children();
        Assertions.assertEquals(3, pluginEls.size());
    }


}
