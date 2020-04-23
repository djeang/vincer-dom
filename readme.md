[![Build Status](https://travis-ci.org/djeang/vincer-dom.svg?branch=master)](https://travis-ci.org/djeang/vincer-dom)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.djeang/vincer-dom.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.djeang%22%20AND%20a:%22vincer-dom%22) 

# Vincer-Dom : A Cure for Dom Manipulation &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<img src="media/syringe.svg"/> 

Vincer-Dom aims at easing DOM manipulation in Java using  [Parent-Chaining Pattern](https://github.com/djeang/parent-chaining/blob/master/readme.md).

Vincer-Dom simply wraps `org.w3c.dom.Document` and `org.w3c.dom.Element` from JDK to offer 
a fresh new style API. With the use of *Parent-Chaining* pattern, the API is very thin as it consists of only 
2 classes : `VDocument` and `VElement`, each one wrapping its `org.w3c.dom` counterpart. 

To give a concrete idea, the following code:

* Reads a pom.xml file (an XML file from Maven that most of Java developers know)
* Adds 2 dependencies
* Removes all dependencies having `test` scope
* Modifies distribution repository, creating potential missing elements. 

```Java
public class EditTest {
    
    @Test
    public void editMavenPom() {
        InputStream stream = EditTest.class.getResourceAsStream("sample-pom.xml");
        VDocument doc = VDocument.parse(stream)
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
                .get("distributionManagement")     // The distributionManagement tag may be ptresent or not
                    .get("repository")      
                        .get("id").make().text("My repo id").__    // make() creates absent element and its ancestors
                        .get("name").make().text("My repo name").__
                        .get("url").make().text("http://myserver::8081").__.__.__.__;
        doc.print(System.out);
    }

    private void removeTests(VElement<?> dependencies) {
        dependencies.getAll("dependency").stream()
            .filter(dependency -> "test".equals(dependency.get("scope").getText()))
            .forEach(VElement::remove);
    }
}
```

If we want to achieve exaxtly the same using *JDom* (not even talking about w3c API or *Dom4J*), the best we can do is:

```Java
public class JdomEditTest {

    @Test
    public void editMavenPomWithJdom() {
        InputStream stream = JdomEditTest.class.getResourceAsStream("sample-pom.xml");
        final Document document;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document w3cDocument = builder.parse(stream);
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

W3C Dom level API is still available through methods `VDocument#getW3cDocument` and `VElement#getW3cElement`.

## Roadmap
* Add namespace support
* Add XPath support

## Adding Vincer-Dom to your build

Maven: 
```
<dependency>
    <groupId>com.github.djeang</groupId>
    <artifactId>vincer-dom</artifactId>
    <version>1.0.0</version>
</dependency>
```

Jeka:
```Java
.add("com.github.djeang:vincer-dom:1.0.0")
```


## How to build

Vincer-dom is build with [Jeka](https://jeka.dev).

* Execute `./jekaw cleanPack`. This will compile, test and package the library in jar file. You don't need to install Jeka 
  on your machine.
  
* To release, just execute `./jekaw git#tagRemote` and choose a version name for the release.
  Release mechanism will be automatically handled by *Travis*.

Enjoy !


> Icon made by https://www.stockio.com/free-icon/medical-icons-syringe
