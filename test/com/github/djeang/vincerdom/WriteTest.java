package com.github.djeang.vincerdom;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WriteTest {

    @Test
    public void createFromScratch() {
        VDocument doc = VDocument.of("accounts")
            .root()
                .add("account").attr("id", "000001").attr("num", "APBHUYF").__
                .add("account").attr("id", "0000002").attr("num", "UYUUYU")
                    .add("properties")
                        .add("property").attr("name", "manager").attr("id", "key2").text("Jhon Doe").__.__.__
                .add("account").attr("id", "000001").attr("num", "APBHUYF").__.__;
        doc.print(System.out);
        Document w3cDocument = doc.getW3cDocument();
        Element rootEl = w3cDocument.getDocumentElement();
        NodeList accountEls = rootEl.getElementsByTagName("account");
        Assertions.assertEquals(3, accountEls.getLength());
        Element acccount1 = (Element) accountEls.item(0);
        Assertions.assertEquals("APBHUYF", acccount1.getAttribute("num"));
    }
}
