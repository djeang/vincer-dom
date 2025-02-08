package com.github.djeang.vincerdom;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Utility class to create {@link javax.xml.xpath.XPathExpression} avoiding checked exceptions.
 */
public final class VXPath {

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    private VXPath() {
    }

    /**
     * Compiles a string to a {@link XPathExpression}.
     */
    public static XPathExpression compile(String expression, Object ...items) {
        try {
            return XPATH.compile(String.format(expression, items));
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Error when compiling xPath expression " + expression, e);
        }
    }

}
