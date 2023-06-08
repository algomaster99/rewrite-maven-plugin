package org.openrewrite.maven;

import com.soebes.itf.jupiter.extension.*;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

@MavenJupiterExtension
@MavenOption(MavenCLIOptions.NO_TRANSFER_PROGRESS)
@MavenOption(MavenCLIExtra.MUTE_PLUGIN_VALIDATION_WARNING)
@MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:cyclonedx")
@SuppressWarnings("NewClassNamingConvention")
class RewriteCycloneDxIT {

    @MavenTest
    @Disabled("module b consistently fails to resolve the locally-built artifact a due to aether resolution errors")
    void multi_module_with_cross_module_dependencies(MavenExecutionResult result) {
        assertThat(result)
                .isSuccessful()
                .project()
                .hasTarget()
                .withFile("multi_module_with_cross_module_dependencies-1.0-cyclonedx.xml")
                .exists();

        assertThat(result)
                .project()
                .withModule("a")
                .hasTarget()
                .withFile("a-1.0-cyclonedx.xml")
                .exists();

        assertThat(result)
                .project()
                .withModule("b")
                .hasTarget()
                .withFile("b-1.0-cyclonedx.xml")
                .exists();

        assertThat(result).out().warn().isEmpty();
    }

    @MavenTest
    void multi_module_with_independent_modules(MavenExecutionResult result) {
        assertThat(result)
                .isSuccessful()
                .project()
                .hasTarget()
                .withFile("multi_module_with_independent_modules-1.0-cyclonedx.xml")
                .exists();

        assertThat(result)
                .project()
                .withModule("a")
                .hasTarget()
                .withFile("a-1.0-cyclonedx.xml")
                .exists();

        assertThat(result)
                .project()
                .withModule("b")
                .hasTarget()
                .withFile("b-1.0-cyclonedx.xml")
                .exists();

        assertThat(result).out().warn().isEmpty();
    }

    @MavenTest
    void single_project(MavenExecutionResult result) {
        assertThat(result)
                .isSuccessful()
                .project()
                .hasTarget()
                .withFile("single_project-1.0-cyclonedx.xml")
                .exists();

        assertThat(result).out().warn().isEmpty();
    }

    @MavenTest
    void pom_packaging(MavenExecutionResult result) throws IOException {
        assertThat(result)
                .isSuccessful()
                .project()
                .hasTarget()
                .withFile("pom_packaging-1.0-cyclonedx.xml")
                .exists();

        Path sbomPath = Paths.get("target/maven-it/org/openrewrite/maven/RewriteCycloneDxIT/pom_packaging/project/target/pom_packaging-1.0-cyclonedx.xml");
        byte[] xmlBytes = Files.readAllBytes(sbomPath);
        String sbomContents = new String(xmlBytes, Charset.defaultCharset());
        String expected = "        <component bom-ref=\"pkg:maven/org.example/pom_packaging@1.0?type=pom\" type=\"library\">\n" +
                "            <group>org.example</group>\n" +
                "            <name>pom_packaging</name>\n" +
                "            <version>1.0</version>\n" +
                "            <scope>required</scope>\n" +
                "            <purl>pkg:maven/org.example/pom_packaging@1.0?type=pom</purl>\n" +
                "        </component>";

        assertThat(sbomContents).contains(expected);
    }
}
