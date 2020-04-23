import dev.jeka.core.api.depmanagement.JkDependencySet;
import dev.jeka.core.api.depmanagement.JkRepoSet;
import dev.jeka.core.api.tooling.JkGitWrapper;
import dev.jeka.core.tool.JkCommandSet;
import dev.jeka.core.tool.JkEnv;
import dev.jeka.core.tool.JkInit;
import dev.jeka.core.tool.builtins.java.JkPluginJava;
import dev.jeka.core.tool.builtins.repos.JkPluginPgp;

import static dev.jeka.core.api.depmanagement.JkScope.TEST;

//@formatter:off
class Build extends JkCommandSet {

    JkPluginJava java = getPlugin(JkPluginJava.class);

    @JkEnv("OSSRH_USER")
    public String ossrhUser;  // OSSRH user and password will be injected from environment variables

    @JkEnv("OSSRH_PWD")
    public String ossrhPwd;

    @Override
    protected void setup() {
        getPlugin(JkPluginPgp.class);  // supply automatically a signer with the secret key located in jeka/gpg
        java.getProject()
            .getDependencyManagement()
                .addDependencies(JkDependencySet.of()
                    .and("org.jdom:jdom2:jar:2.0.6", TEST)
                    .and("org.junit.jupiter:junit-jupiter:5.6.2", TEST)).__
            .getPublication()
                .setModuleId("com.github.djeang:vincer-dom")
                .setVersionSupplier(JkGitWrapper.of(getBaseDir())::getJkVersionFromTags)
                .setRepos(JkRepoSet.ofOssrhSnapshotAndRelease(ossrhUser, ossrhPwd))
                .getPublishedPomMetadata()
                    .getProjectInfo()
                        .setName("Vincer-Dom")
                        .setDescription("Modern Dom manipulation library for Java")
                        .setUrl("https://github.com/djeang/vincer-dom").__
                    .getScm()
                        .setConnection("https://github.com/djeang/vincer-dom.git").__
                    .addApache2License()
                    .addGithubDeveloper("djeang", "djeangdev@yahoo.fr");
    }

    public void cleanPack() {
        clean(); java.pack();
    }

    public void publish() {
        java.publish();
    }

    public static void main(String[] args) {
        JkInit.instanceOf(Build.class, args).cleanPack();
    }

}