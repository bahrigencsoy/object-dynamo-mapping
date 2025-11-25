package net.gencsoy.odm;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class TemplateProcessor {

    private final TemplateEngine templateEngine;

    public TemplateProcessor() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver(getClass().getClassLoader());
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".tmpl");
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    public static void main(String[] args) {
        TemplateProcessor processor = new TemplateProcessor();
        Context ctxt = new Context();
        String hello = processor.templateEngine.process("Factory.java", ctxt);
        System.out.println(hello);


    }
}
