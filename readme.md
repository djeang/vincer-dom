# Vincer-Dom : Fix Dom Manipulation &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<img src="syringe.svg"/> 

Vincer-Dom aims at fixing DOM manipulation in Java using  [Parent-Chaining Pattern](https://github.com/djeang/parent-chaining/blob/master/readme.md).

Vincer-Dom simply wraps `org.w3c.dom.Document` and `org.w3c.dom.Element` from JDK API to offer 
a fresh new style API. Thanks to Parent-Chaining pattern the API is very this as it consists in only 
2 classes : `VDocument` and `VElement`, each one wrapping its `org.w3c.dom` counterpart. 

To give a concrete idea, the following code does :
* read a pom.xml file (A Maven file that most of Java developer knows)
* add 2 dependencies
* remove all dependencies having `test` scope
* modify distribution repository. Potential missing elements are created through method `make()`. 

```Java
public class EditTest {
    
    @Test
    public void editMavenPom() {
        InputStream is = EditTest.class.getResourceAsStream("sample-pom.xml");
        VDocument doc = VDocument.parse(is)
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
                        .get("url").make().text("http://myserver::8081").__.__.__.__;
        doc.print(System.out);
    }

    private void removeTests(VElement<?> dependencies) {
        dependencies.getAll("dependency").stream()
            .filter(dependency -> "test".equals(dependency.get("scope").getText()))
            .forEach(dep -> dep.remove());
    }
}
```

If we want to achieve the same using [JDom2](http://www.jdom.org/), the best we can do is :

```Java
public class JdomEditTest {

    @Test
    public void editMavenPomWithJdom() {
        InputStream is = JdomEditTest.class.getResourceAsStream("sample-pom.xml");
        final Document document;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document w3cDocument = builder.parse(is);
            document = new DOMBuilder().build(w3cDocument);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        Element root = document.getRootElement();
        Element dependencies = root.getChild("dependencies");
        dependencies.addContent
                (new Element("dependency")
                    .addContent(new Element("group").setText("com.github.djeang"))
                    .addContent(new Element("artifactId").setText("vincer-dom"))
                    .addContent(new Element("version").setText("0.1-SNAPSHOT"))
                );
        dependencies.addContent
                (new Element("dependency")
                    .addContent(new Element("group").setText("org.junit.jupiter"))
                    .addContent(new Element("artifactId").setText("unit-jupiter-engine"))
                    .addContent(new Element("version").setText("5.4.0"))
                    .addContent(new Element("scope").setText("test"))
                );
        removeTests(dependencies);
        Element distributionManagement = getOrCreate(root, "distributionManagement");
        Element repository = getOrCreate(distributionManagement, "repository");
        getOrCreate(repository, "id").setText("My repo id");
        getOrCreate(repository, "name").setText("My repo name");
        getOrCreate(repository, "url").setText("http://myserver::8081");
        final XMLOutputter xmlOutputter = new XMLOutputter();
        try {
            xmlOutputter.output(document, System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Element getOrCreate(Element parent, String name) {
        Element element = parent.getChild(name);
        if (element == null) {
            element = new Element(name);
            parent.addContent(element);
        }
        return element;
    }

    private void removeTests(Element dependencies) {
        for (ListIterator<Element> it = dependencies.getChildren().listIterator();it.hasNext();) {
            Element dependency = it.next();
            if ("test".equals(dependency.getChildText("scope"))) {
                it.remove();
            }
        }
    }
}
```
As you can see, Vincer-Dom saves a lot of coding effort while getting code much more readable.

Enjoy !


> Icon made by https://www.stockio.com/free-icon/medical-icons-syringe
