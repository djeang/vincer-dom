# Vincer-Dom : Modern DOM manipulation  <img src="syringe.svg"/>

Vincer-Dom aims at fixing DOM manipulation in Java which suffers from old style cumbersome API.

Vincer-Dom simply wraps `org.w3c.dom.Element` and `org.w3c.dom.Element` API from JDK to offer 
to offer a new style API leveraging `lambda`and [Parent-Chaining Pattern](https://github.com/djeang/parent-chaining/blob/master/readme.md)

Using Vincer-Dom, Dom manipulation code should look like :

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

