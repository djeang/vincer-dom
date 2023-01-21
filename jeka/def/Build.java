import dev.jeka.core.api.crypto.gpg.JkGpg;
import dev.jeka.core.api.depmanagement.JkRepoSet;
import dev.jeka.core.api.project.JkProject;
import dev.jeka.core.api.tooling.JkGitProcess;
import dev.jeka.core.tool.JkBean;
import dev.jeka.core.tool.JkInjectProperty;
import dev.jeka.core.tool.builtins.project.ProjectJkBean;

//@formatter:off
class Build extends JkBean {

    ProjectJkBean project = getBean(ProjectJkBean.class).configure(this::configure);

    @JkInjectProperty("OSSRH_USER")
    public String ossrhUser;  // OSSRH user and password will be injected from environment variables

    @JkInjectProperty("OSSRH_PWD")
    public String ossrhPwd;

    private void configure(JkProject project) {
        project.flatFacade()
            .useSimpleLayout()
            .configureTestDependencies(deps -> deps
                .and("org.jdom:jdom2:2.0.6.1")
                .and("org.junit.jupiter:junit-jupiter:5.8.1")
            );
        project.publication
            .setModuleId("com.github.djeang:vincer-dom")
            .setVersion(JkGitProcess.of(getBaseDir())::getVersionFromTag)
            .setRepos(JkRepoSet.ofOssrhSnapshotAndRelease(ossrhUser, ossrhPwd, JkGpg.ofDefaultGnuPg().getSigner("")))
            .maven.pomMetadata
                .setProjectName("Vincer-Dom")
                .setProjectDescription("Modern Dom manipulation library for Java")
                .setProjectUrl("https://github.com/djeang/vincer-dom")
                .setScmUrl("https://github.com/djeang/vincer-dom.git")
                .addApache2License()
                .addGithubDeveloper("djeang", "djeangdev@yahoo.fr");
    }

    public void cleanPack() {
        project.clean(); project.pack();
    }

}