package net.gencsoy.odm;


import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import net.gencsoy.odm.expandedmodel.ExtendedDynamoAttribute;
import net.gencsoy.odm.expandedmodel.ExtendedDynamoTable;
import net.gencsoy.odm.expandedmodel.ExtendedOdmProject;
import net.gencsoy.odm.inputmodel.DynamoAttribute;
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
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

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

    private ModelMapper modelMapper = new ModelMapper();

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

        OdmProject projectDefOriginal;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputModel)))) {
            projectDefOriginal = new Gson().fromJson(reader, OdmProject.class);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex);
        }

        if (!"0.4".equals(projectDefOriginal.getVersion())) {
            throw new MojoExecutionException("Please provide version 0.4 of the model json file");
        }

        ExtendedOdmProject extendProject = modelMapper.map(projectDefOriginal, ExtendedOdmProject.class);

        extendProject.getTables().replaceAll(new UnaryOperator<DynamoTable>() {
            @Override
            public DynamoTable apply(DynamoTable input) {
                ExtendedDynamoTable dynamoTable = modelMapper.map(input, ExtendedDynamoTable.class);
                dynamoTable.getAttributes().replaceAll(new UnaryOperator<DynamoAttribute>() {
                    @Override
                    public DynamoAttribute apply(DynamoAttribute dynamoAttribute) {
                        return modelMapper.map(dynamoAttribute, ExtendedDynamoAttribute.class);
                    }
                });
                return dynamoTable;
            }
        });

        var splitted = extendProject.getPackageName().split("\\.");
        File packageDirectory = outputDirectory;
        for (String dir : splitted) {
            File dirChild = new File(packageDirectory, dir);
            dirChild.mkdir();
            packageDirectory = dirChild;
        }

        TemplateProcessor templateProcessor = new TemplateProcessor();
        JavaCodeFormatter codeFormatter = new JavaCodeFormatter();

        try {
            String factoryClassContents = templateProcessor.processFactoryClass(extendProject);
            factoryClassContents = codeFormatter.formatNoException(factoryClassContents);
            Files.writeString(packageDirectory.toPath().resolve(extendProject.getFactoryClass() + ".java"), factoryClassContents);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating factory ", e);
        }

        try {
            String libClassContents = templateProcessor.processLibClass(extendProject);
            libClassContents = codeFormatter.formatNoException(libClassContents);
            Files.writeString(packageDirectory.toPath().resolve("lib.java"), libClassContents);
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating factory ", e);
        }

        try {
            for (DynamoTable table : extendProject.getTables()) {
                String itemContents = templateProcessor.processTable(extendProject, (ExtendedDynamoTable) table);
                itemContents = codeFormatter.formatNoException(itemContents);
                Files.writeString(packageDirectory.toPath().resolve(table.getJavaClass() + ".java"), itemContents);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating item ", e);
        }
    }
}
