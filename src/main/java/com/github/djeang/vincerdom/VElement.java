package com.github.djeang.vincerdom;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class VElement<P> extends VNode<P> {

    private Element w3cElement;

    private ElementProxy proxyElement;  // only used for non-existing element, so we can create it afterward.

    protected VElement(P __, Element element) {
        super(__);
        this.w3cElement = element;
    }

    private VElement(P __, VElement parent, String name) {
        super(__);
        this.proxyElement = ElementProxy.of(parent, name);
    }

    public Element getW3cElement() {
        return w3cElement;
    }

    public VElement<P> attr(String name, String value) {
        assertExist();
        w3cElement.setAttribute(name, value);
        return this;
    }

    public VElement<P> removeAttr(String name) {
        assertExist();
        w3cElement.removeAttribute(name);
        return this;
    }

    public VElement<P> text(String text) {
        assertExist();
        w3cElement.setTextContent(text);;
        return this;
    }

    public String getText() {
        if (!exist()) {
            return null;
        }
        return w3cElement.getTextContent();
    }

    public VElement<VElement<P>> add(String name) {
        assertExist();
        Element newElement = w3cElement.getOwnerDocument().createElement(name);
        w3cElement.appendChild(newElement);
        return new VElement<>(this, newElement);
    }

    public VElement<VElement<P>> get(String name) {
        if (!exist()) {
            return ElementProxy.of(this, name).create();
        }
        NodeList nodeList = w3cElement.getElementsByTagName(name);
        if (nodeList.getLength() > 0) {
            return new VElement(this, (Element) nodeList.item(0));
        }
        return new VElement<>(this, this, name);
    }

    public List<VElement<VElement<P>>> getAll(String name) {
        if (!exist()) {
            return Collections.emptyList();
        }
        List<VElement<VElement<P>>> result = new LinkedList<>();
        NodeList nodeList = w3cElement.getElementsByTagName(name);
        for (int i = 0; i < nodeList.getLength(); i++) {
            VElement el = new VElement(this, (Element) nodeList.item(i));
            result.add(el);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Adds a sibling element of the specified name just before this one.
     *
     * @return The newly created sibling.
     */
    public VElement<P> addSibling(String name) {
        assertExist();
        Element newElement = w3cElement.getOwnerDocument().createElement(name);
        w3cElement.getParentNode().insertBefore(newElement, w3cElement);
        return new VElement<>(this.__, newElement);
    }

    public VElement<P> remove() {
        assertExist();
        this.w3cElement.getParentNode().removeChild(w3cElement);
        return null;
    }

    public VElement<P> apply(Consumer<VElement<?>> consumer) {
        assertExist();
        consumer.accept(this);
        return this;
    }

    public boolean exist() {
        return w3cElement != null;
    }

    public VElement<P> createIfNotExist() {
        if (!exist()) {
            this.w3cElement = proxyElement.create().w3cElement;
            this.proxyElement = null;
        }
        return this;
    }

    private void assertExist() {
        if (w3cElement == null) {
            throw new IllegalStateException("Element does not exist. " +
                    "Please, invoke #createIfNotExist() prior trying to modify it.");
        }
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
            parent.createIfNotExist();
            return parent.add(name);
        }
    }


}
