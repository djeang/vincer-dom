# Vincer-Dom &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<img src="syringe.svg"/> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;Modern DOM manipulation  

Vincer-Dom aims at fixing DOM manipulation in Java using  [Parent-Chaining Pattern](https://github.com/djeang/parent-chaining/blob/master/readme.md) pattern.

Vincer-Dom simply wraps `org.w3c.dom.Element` and `org.w3c.dom.Element` API from JDK to offer 
to offer a fresh style API.

To give a concrete idea, the following code reads a pom.xml file, adds a dependecy, removes all dependencies having `test` scope and modifies distribution repository url.

```Java
public void editMavenPom() {
        InputStream is = EditRunner.class.getResourceAsStream("sample-pom.xml");
        VDocument doc = VDocument.parse(is);
        doc.root()
            .get("dependencies")
                .add("dependency")
                    .add("groupId").text("com.github.djeang").__
                    .add("artifactId").text("vincer-dom").__
                    .add("version").text("0.1-SNAPSHOT").__.__
                .apply(this::removeTests).__
            .get("distributionManagement")
                .get("repository")
                    .get("url").text("http://myserver::8081");
        doc.print(System.out);
    }

    private void removeTests(VElement<?> dependencies) {
        dependencies.getAll("dependency").stream()
            .filter(dependency -> "test".equals(dependency.get("scope").getText()))
            .forEach(dep -> dep.remove());
    }
```

