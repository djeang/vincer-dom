package com.github.djeang.vincerdom;

import org.junit.jupiter.api.Test;

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
    }
}
