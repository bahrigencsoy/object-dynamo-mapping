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
import java.nio.file.Files;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "touch", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MyMojo
        extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/odm", property = "outputDir", required = true)
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


        var splitted = projectDef.getPackageName().split("\\.");
        File packageDirectory = outputDirectory;
        for (String dir : splitted) {
            File dirChild = new File(packageDirectory, dir);
            dirChild.mkdir();
            packageDirectory = dirChild;
        }

        TemplateProcessor templateProcessor = new TemplateProcessor();
        String factoryClassContents = templateProcessor.processFactoryClass(projectDef);

        try {
            Files.writeString(packageDirectory.toPath().resolve(projectDef.getFactoryClass() + ".java"), factoryClassContents);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file ", e);
        }
    }
}
