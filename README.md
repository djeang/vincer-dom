[![Build Status](https://travis-ci.org/djeang/vincer-dom.svg?branch=master)](https://travis-ci.org/djeang/vincer-dom)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.djeang/vincer-dom.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.djeang%22%20AND%20a:%22vincer-dom%22) 

# Last features
*  Predicates on `VElement#child` and `VElement#children` methods
* `VElement#child` and `VElement#children` methods to retrieve direct children.
* `VElement#xPath` to retrieve elements based on xPath expressions.

# Vincer-Dom : A Cure for DOM/XML Manipulation &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<img src="media/syringe.svg"/> 

Writing or editing Dom/XML in Java has never been natural or straightforward. 
The Java code structure diverges inexorably from the underlying tree structure it manipulates, obscuring the original intentions.

Vincer-Dom fixes this issue by using  [Parent-Chaining Pattern](https://github.com/djeang/parent-chaining/blob/master/readme.md).

Vincer-Dom simply wraps `org.w3c.dom.Document` and `org.w3c.dom.Element` from JDK to offer a powerful fresh new style API. 

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
                .get("distributionManagement")     // The distributionManagement tag may be present or not
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
The complete Dom manipulation has been expressed in a single chained statement, reflecting the tree structure 
of the manipulated data.

Vincer-dom makes also parsing easier. By proxying non-existing element, it removes the need of null checking parent.

For example, this prints all dependencies and plugins found in a pom. If `<version>` tag does not exist, the `text()`
method will return `null` instead of throwing a `NullPointerException`.

```Java
void printAllDeps() {
    VElement root = VDocument.parse(EditTest.class.getResourceAsStream("sample-pom.xml")).root();
    root
        .child("dependencies")
            .children("dependency").forEach(this::printDependency);
            
    System.out.println("---");
    root.xPath("build/plugins/plugin").forEach(this::printDependency);
}

private void printDependency(VElement el) {
    System.out.println(String.format("%s:%s:%s", 
        el.get("groupId").text(), 
        el.get("artifactId").text(), 
        el.get("version").text()));
}
```
Output :
```
mysql:mysql-connector-java:5.1.14
org.slf4j:slf4j-simple:1.6.1
---
org.apache.maven.plugins:maven-compiler-plugin:2.3.2
org.apache.maven.plugins:maven-jar-plugin:null
org.apache.maven.plugins:maven-shade-plugin:1.4
```

As you can see, Vincer-Dom saves a lot of coding effort while getting code much more readable.

With the use of *Parent-Chaining* pattern, the API is very thin as it consists of only 
2 classes : `VDocument` and `VElement`, each one wrapping its `org.w3c.dom` counterpart. The example does not show, but you can of course edit attributes in a fluent way.

W3C Dom level API is still available through methods `VDocument#getW3cDocument` and `VElement#getW3cElement`.

Tips: With IntelliJ you can adjust indentation right and left using `tab` and `shift+tab`.

## Adding Vincer-Dom to your build

Maven: 
```
<dependency>
    <groupId>com.github.djeang</groupId>
    <artifactId>vincer-dom</artifactId>
    <version>1.4.0</version>
</dependency>
```

Jeka:
```Java
.add("com.github.djeang:vincer-dom:1.4.0")
```

## How to build

Vincer-dom is build with [Jeka](https://jeka.dev).

If you don't use [Intellij Plugin for Jeka](https://plugins.jetbrains.com/plugin/13489-jeka), right after fetching this
project, execute:
* For Eclipse: `./jekaw eclipse#files`
* For IntelliJ: `./jekaw intellij#iml` 

*Note that you don't need to install Jeka on your machine.*

### How to build

```shell
./jeka project: pack
```

## How to release

Just create a new release on project github page.
  
## Roadmap
* Add namespace support

Enjoy !


> Icon made by https://www.stockio.com/free-icon/medical-icons-syringe
