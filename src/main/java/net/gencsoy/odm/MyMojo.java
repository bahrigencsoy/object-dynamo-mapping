package net.gencsoy.odm;


import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import net.gencsoy.odm.expandedmodel.ExtendedDynamoAttribute;
import net.gencsoy.odm.expandedmodel.ExtendedDynamoItem;
import net.gencsoy.odm.inputmodel.DynamoAttribute;
import net.gencsoy.odm.inputmodel.DynamoItem;
import net.gencsoy.odm.inputmodel.DynamoTable;
import net.gencsoy.odm.inputmodel.OdmProject;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.modelmapper.ModelMapper;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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

        try {
            String factoryClassContents = templateProcessor.processFactoryClass(projectDef);
            Files.writeString(packageDirectory.toPath().resolve(projectDef.getFactoryClass() + ".java"), factoryClassContents);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating factory ", e);
        }

        try {
            ModelMapper modelMapper = new ModelMapper();
            for (DynamoTable table : projectDef.getTables()) {
                for (DynamoItem item : table.getItems()) {
                    ExtendedDynamoItem extendItem = modelMapper.map(item, ExtendedDynamoItem.class);
                    List<? extends DynamoAttribute> originalAttributes = extendItem.getAttributes();
                    List<ExtendedDynamoAttribute> extendedAtributes = new ArrayList<>();
                    extendedAtributes.add(modelMapper.map(table.getPartitionKey(), ExtendedDynamoAttribute.class));
                    extendedAtributes.add(modelMapper.map(table.getSortKey(), ExtendedDynamoAttribute.class));
                    for (DynamoAttribute attribute:originalAttributes){
                        extendedAtributes.add(modelMapper.map(attribute, ExtendedDynamoAttribute.class));
                    }
                    extendItem.setAttributes(extendedAtributes);

                    String itemContents = templateProcessor.processItem(projectDef, extendItem);
                    Files.writeString(packageDirectory.toPath().resolve(extendItem.getName() + ".java"), itemContents);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating item ", e);
        }
    }
}
