package net.gencsoy.odm;


import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import net.gencsoy.odm.expandedmodel.ExtendedDynamoAttribute;
import net.gencsoy.odm.expandedmodel.ExtendedDynamoItem;
import net.gencsoy.odm.expandedmodel.ExtendedDynamoTable;
import net.gencsoy.odm.expandedmodel.ExtendedOdmProject;
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
import java.util.function.Consumer;

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

    private <T, V extends T> void mapListItems(List<T> list, Class<V> targetClass, Consumer<V> customizer) {
        List<T> clone = new ArrayList<>(list);
        list.clear();
        for (T original : clone) {
            V copy = modelMapper.map(original, targetClass);
            if (customizer != null) {
                customizer.accept(copy);
            }
            list.add(copy);
        }
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

        if (!"0.2".equals(projectDefOriginal.getVersion())) {
            throw new MojoExecutionException("Please provide version 0.2 of the model json file");
        }

        ExtendedOdmProject extendProject = modelMapper.map(projectDefOriginal, ExtendedOdmProject.class);

        mapListItems(extendProject.getTables(), ExtendedDynamoTable.class, new Consumer<ExtendedDynamoTable>() {
            @Override
            public void accept(ExtendedDynamoTable dynamoTable) {
                dynamoTable.setPartitionKey(modelMapper.map(dynamoTable.getPartitionKey(), ExtendedDynamoAttribute.class));
                dynamoTable.setSortKey(modelMapper.map(dynamoTable.getSortKey(), ExtendedDynamoAttribute.class));
                mapListItems(dynamoTable.getItems(), ExtendedDynamoItem.class, new Consumer<ExtendedDynamoItem>() {
                    @Override
                    public void accept(ExtendedDynamoItem extendedDynamoItem) {
                        extendedDynamoItem.setTable(dynamoTable);
                        mapListItems(extendedDynamoItem.getAttributes(), ExtendedDynamoAttribute.class, null);
                    }
                });
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
                for (DynamoItem item : table.getItems()) {
                    String itemContents = templateProcessor.processItem(extendProject, (ExtendedDynamoItem) item);
                    itemContents = codeFormatter.formatNoException(itemContents);
                    Files.writeString(packageDirectory.toPath().resolve(item.getName() + ".java"), itemContents);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating item ", e);
        }
    }
}
