package com.github.djeang.vincerdom;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Wrapper for {@link org.w3c.dom.Element} offering a Parent-Chaining fluent interface. <p>
 * The underlying element may exist or not. If the underlying element does not exist,
 * a proxy element is used in place but write methods are disabled. The concrete w3c element
 * can be created afterward using the {@link #make()} method.
 *
 * @author Jerome Angibaud
 */
public final class VElement<P> {

    public final P __;

    private Element w3cElement;

    private ElementProxy proxyElement;  // only used for non-existing element, so we can create it afterward.

    VElement(P parent, Element element) {
        this.__ = parent;
        this.w3cElement = element;
    }

    private VElement(P __, VElement parent, String name) {
        this.__ = __;
        this.proxyElement = ElementProxy.of(parent, name);
    }

    /**
     * Creates a VElement wrapping the specified element.
     */
    public static VElement<Void> of(Element element) {
        return new VElement(null, element);
    }

    /**
     * Creates a VElement wrapping the specified parent and element.
     */
    public static <P> VElement of(P parent, Element element) {
        return new VElement(parent, element);
    }

    /**
     * Returns the underlying w3cElement. This element can be null if this VElement does not exist.
     */
    public Element getW3cElement() {
        return w3cElement;
    }

    /**
     * Adds the specified attribute name/value on the underlying element.
     * @throws IllegalStateException if the underlying element does not exist.
     */
    public VElement<P> attr(String name, String value) {
        assertExist();
        w3cElement.setAttribute(name, value);
        return this;
    }

    /**
     * Returns the value of the specified attribute on this element. Returns <code>null</code> if no such
     * attribute exists.
     */
    public String attr(String name) {
        return w3cElement.getAttribute(name);
    }

    /**
     * Removes the specified attribute of the specified name from the underlying element.
     * @throws IllegalStateException if the underlying element does not exist.
     */
    public VElement<P> removeAttr(String name) {
        assertExist();
        w3cElement.removeAttribute(name);
        return this;
    }

    /**
     * Sets the specified text on the underlying element.
     * @throws IllegalStateException if the underlying element does not exist.
     */
    public VElement<P> text(String text) {
        assertExist();
        w3cElement.setTextContent(text);
        return this;
    }

    /**
     * Returns the tag name of the underlying element.
     * If the element does not exist, this method returns {@code null}.
     *
     */
    public String tagName() {
        if (!exist()) {
            return null;
        }
        return w3cElement.getTagName();
    }

    /**
     * Returns the text pof the underlying element. <code>null</code> if the underlying element does not exist.
     */
    public String text() {
        if (!exist()) {
            return null;
        }
        return w3cElement.getTextContent();
    }

    /**
     * Adds a child element of the specified name on the underlying element. This methods returns the
     * newly created element.
     * @throws IllegalStateException if the underlying element does not exist.
     */
    public VElement<VElement<P>> add(String name) {
        assertExist();
        Element newElement = w3cElement.getOwnerDocument().createElement(name);
        w3cElement.appendChild(newElement);
        return new VElement<>(this, newElement);
    }

    /**
     * Returns the first child element of the underlying element having the specified name. <p>
     * If no such element exist, this method returns a proxy element that let creation possible afterward.
     */
    public VElement<VElement<P>> get(String name) {
        if (!exist()) {  // If this element does not exist, it creates the proxy on the child element
            return ElementProxy.of(this, name).create();
        }
        VElement child = this.child(name);
        if (child != null) {
            return child;
        }
        return new VElement<>(this, this, name);
    }

    /**
     * Returns an unmodifiable list of the child elements having the specified name and verifying the specified predicate.
     * Returns an empty list if the underlying element does not exist.
     */
    public List<VElement<Void>> children(String name, Predicate<VElement<Void>> predicate) {
        return children().stream()
                .filter(element -> name.equals(element.tagName()))
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Returns an unmodifiable list of the child elements having the specified name.
     * Returns an empty list if the underlying element does not exist.
     */
    public List<VElement<Void>> children(String name) {
        return children(name, el -> true);
    }

    /**
     * Returns an unmodifiable list of the child elements of this element.
     * If the element does not exist, this method returns an empty list.
     */
    public List<VElement<Void>> children() {
        if (!exist()) {
            return Collections.emptyList();
        }
        List<VElement<Void>> result = new LinkedList<>();
        NodeList nodeList = w3cElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                VElement<Void> el = new VElement(this, (Element) nodeList.item(i));
                result.add(el);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the child first child of this element having the specified name and verifying the specified predicate.
     * Returns <code>null</null> if the underlying element does not exist or no such named child exists.
     */
    public VElement<Void> child(String name, Predicate<VElement<Void>> predicate) {
        return children(name, predicate).stream().findFirst().orElse(null);
    }

    /**
     * Returns the child first child of this element having the specified name.
     * Returns <code>null</null> if the underlying element does not exist or no such named child exists.
     */
    public VElement<Void> child(String name) {
        return child(name, vElement -> true);
    }



    /**
     * Returns an unmodifiable list of elements matching the specified xPath expression.
     *
     * @deprecated Use {@link VDocument#xPath(XPathExpression)} instead
     */
    @Deprecated
    public List<VElement> xPath(XPathExpression xPathExpression) {
        List<VElement<VElement<P>>> result = new LinkedList<>();
        final NodeList nodeList;
        try {
            nodeList = (NodeList) xPathExpression.evaluate(w3cElement, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Error when evaluating xPath expression " + xPathExpression, e);
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            VElement el = new VElement(this, (Element) nodeList.item(i));
            result.add(el);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable list of elements matching the specified xPath expression.
     *
     * @deprecated Use {@link VDocument#xPath(String)} instead
     */
    @Deprecated
    public List<VElement> xPath(String xPathExpression) {
        XPathExpression compiledExpression = VXPath.compile(xPathExpression);
        return xPath(compiledExpression);
    }



    /**
     * Adds a sibling element of the specified name just before this one. This method returns the newly
     * created element.
     */
    public VElement<P> addSibling(String name) {
        assertExist();
        Element newElement = w3cElement.getOwnerDocument().createElement(name);
        w3cElement.getParentNode().insertBefore(newElement, w3cElement);
        return VElement.of(this.__, newElement);
    }

    /**
     * Removes the underlying element from its parent children.
     */
    public VElement<P> remove() {
        assertExist();
        Element parent = (Element) w3cElement.getParentNode();
        parent.removeChild(w3cElement);
        return this;
    }

    /**
     * Runs the specified consumer with this element as argument.
     */
    public VElement<P> apply(Consumer<VElement<?>> consumer) {
        assertExist();
        consumer.accept(this);
        return this;
    }

    /**
     * Returns <code>true</code> if the underlying element exist.
     */
    public boolean exist() {
        return w3cElement != null;
    }

    /**
     * Creates the underlying element and its non-existing parents.
     * Does nothing if the underlying element already exists.
     */
    public VElement<P> make() {
        if (!exist()) {
            this.w3cElement = proxyElement.create().w3cElement;
            this.proxyElement = null;
        }
        return this;
    }

    private void assertExist() {
        if (w3cElement == null) {
            throw new IllegalStateException("Element " + this + " does not exist. " +
                    "Please, invoke #make() prior trying to modify it.");
        }
    }

    private String getName() {
        if (exist()) {
            return w3cElement.getNodeName();
        }
        return proxyElement.name;
    }

    @Override
    public String toString() {
        if (this.__ == null || !(this.__ instanceof VElement))  {
            return getName();
        }
        return this.__ + "/" + getName();
    }

    private static class ElementProxy {

        private final VElement parent;

        private final String name;

        private ElementProxy(VElement parent, String name) {
            this.name = name;
            this.parent = parent;
        }

        static ElementProxy of(VElement parent, String name) {
            return new ElementProxy(parent, name);
        }

        VElement create() {
            parent.make();
            return parent.add(name);
        }

    }


}
