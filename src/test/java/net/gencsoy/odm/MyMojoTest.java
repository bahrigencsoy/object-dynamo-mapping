package net.gencsoy.odm;


import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.testing.WithoutMojo;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@MojoTest
public class MyMojoTest {


    /**
     * @throws Exception if any
     */
    @Test
    @InjectMojo(goal = "touch", pom = "src/test/resources/project-to-test/pom.xml")
    @MojoParameter(name = "inputModel", value = "src/test/resources/project-to-test/model.json")
    public void testSomething(MyMojo myMojo)
            throws Exception {
        File pom = new File("target/test-classes/project-to-test/");
        assertNotNull(pom);
        assertTrue(pom.exists());

        assertNotNull(myMojo);
        myMojo.execute();

        File outputDirectory = myMojo.getOutputDirectory();
        assertNotNull(outputDirectory);
        assertTrue(outputDirectory.exists());

        File touch = new File(outputDirectory, "touch.txt");
        assertTrue(touch.exists());

        File expectedOutputDirectory = new File("target/test-harness/project-to-test").getAbsoluteFile();
        assertEquals(expectedOutputDirectory, outputDirectory);
    }

    /**
     * Do not need the MojoRule.
     */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn() {
        assertTrue(true);
    }

}

