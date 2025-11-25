package net.gencsoy.odm;

import net.gencsoy.odm.expandedmodel.ExtendedDynamoItem;
import net.gencsoy.odm.inputmodel.OdmProject;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Arrays;

public class TemplateProcessor {

    private final TemplateEngine templateEngine;

    public TemplateProcessor() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver(getClass().getClassLoader());
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".tmpl");
        templateResolver.setTemplateMode(TemplateMode.JAVASCRIPT);
        templateResolver.setCacheable(false);
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    public static void main(String[] args) {
        TemplateProcessor processor = new TemplateProcessor();
        Context ctxt = new Context();
        ctxt.setVariable("name", "Gemini");
        ctxt.setVariable("items", Arrays.asList("aaa", "bbb", "ccc"));
        String hello = processor.templateEngine.process("Factory.java", ctxt);
        System.out.println(hello);
    }

    public String processFactoryClass(OdmProject project) {
        var context = new Context();
        context.setVariable("project", project);
        return templateEngine.process("Factory.java", context);
    }

    public String processItem(OdmProject project, ExtendedDynamoItem item) {
        var context = new Context();
        context.setVariable("project", project);
        context.setVariable("item", item);
        return templateEngine.process("Item.java", context);
    }
}
