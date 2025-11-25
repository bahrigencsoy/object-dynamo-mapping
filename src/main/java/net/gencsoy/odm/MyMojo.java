package net.gencsoy.odm;


import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import net.gencsoy.odm.inputmodel.OdmProject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "touch", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MyMojo
        extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(property = "inputModel", required = true)
    private File inputModel;

    @VisibleForTesting
    File getOutputDirectory() {
        return outputDirectory;
    }

    public void execute()
            throws MojoExecutionException {
        File f = outputDirectory;

        if (!f.exists()) {
            f.mkdirs();
        }

        if (inputModel == null) {
            throw new MojoExecutionException("Input Model not supplied");
        }

        if (!inputModel.exists()) {
            throw new MojoExecutionException("Input Model does not exist");
        }

        OdmProject projectDef;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputModel)))) {
            projectDef = new Gson().fromJson(reader, OdmProject.class);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex);
        }

        File touch = new File(f, "touch.txt");

        FileWriter w = null;
        try {
            w = new FileWriter(touch);

            w.write("touch.txt");
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + touch, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
