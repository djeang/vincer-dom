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
        dependencies.children("dependency").stream()
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
    
    // Get the groupId of the first declared <plugin> element
    String groupId = root.get("build/plugins/plugin/groupId").text();
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

Vincer-Dom has no dependency and runs on Java 8 or higher.

Maven:
```xml
<dependency>
    <groupId>com.github.djeang</groupId>
    <artifactId>vincer-dom</artifactId>
    <version>1.7.1</version>
</dependency>
```

JeKa, in `jeka.project.deps`:
```ini
[compile]
com.github.djeang:vincer-dom:1.7.1
```

## How to build

Vincer-Dom is built with [JeKa](https://jeka-dev.github.io/jeka/). This repository is also a JeKa example: a library,
targeting Java 8, released on Maven Central from GitHub releases. You don't need to install JeKa: the `./jeka`
script downloads it, with the JDKs it needs.

Run the tests and create the jars in `jeka-output/`:
```shell
./jeka project: test pack
```

Publish to a local directory, to check the published artifacts and POM:
```shell
./jeka maven: publishLocal
```

To edit the project in your IDE, if you don't use the [IntelliJ plugin for JeKa](https://plugins.jetbrains.com/plugin/13489-jeka):
* For IntelliJ: `./jeka intellij: iml`
* For Eclipse: `./jeka eclipse: files`

### How it works

[jeka.properties](jeka.properties) holds the whole configuration:
- `jeka.java.version` is the JDK running JeKa, while `@project.javaVersion=8` is the target of the library.
  JeKa compiles with a JDK 8. Tests run in the JeKa JVM (`@project.test.fork=false`), as JeKa can't fork them on Java 8.
- `@project.layout.style=SIMPLE`: sources in `src`, tests in `test`, resources mixed with sources.
- `@project.gitVersioning.enable=true`: the version comes from the Git tag, or is a SNAPSHOT.
- `jeka.classpath=dev.jeka:centralportal-plugin` and `@maven.pub.metadata.*`: the Maven Central publication.

The test dependencies are in [jeka.project.deps](jeka.project.deps).

## How to release

Create a new release on the GitHub page of the project.

## Deploying on Maven Central

The publication is done by a [GitHub action](.github/workflows/main.yml), running `./jeka build centralportal: publish`
on each push. It publishes only when the commit has a tag: otherwise the version is a SNAPSHOT, and the plugin skips it.

The action defines the required environment variables:

```yaml
env:
    JEKA_CENTRAL_PORTAL_USERNAME: ${{ secrets.CENTRAL_PORTAL_USERNAME }}
    JEKA_CENTRAL_PORTAL_PASSWORD: ${{ secrets.CENTRAL_PORTAL_PASSWORD }}
    JEKA_CENTRAL_PORTAL_SIGN_KEY: ${{ secrets.GPG_SECRET_KEY}}
    JEKA_CENTRAL_PORTAL_SIGN_KEY_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
```
The content of `secrets.GPG_SECRET_KEY` has been obtained by executing: `gpg --export-secret-key --armor my-key-name`.

Learn more: [maven KBean](https://jeka-dev.github.io/jeka/reference/kbeans-maven/),
[git KBean](https://jeka-dev.github.io/jeka/reference/kbeans-git/),
[project KBean](https://jeka-dev.github.io/jeka/reference/kbeans-project/).

## Roadmap
* Add namespace support

Enjoy !


> Icon made by https://www.stockio.com/free-icon/medical-icons-syringe
