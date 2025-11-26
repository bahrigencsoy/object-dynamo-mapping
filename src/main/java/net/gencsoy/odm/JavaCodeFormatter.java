package net.gencsoy.odm;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple Java code formatter that uses Eclipse JDT to format Java source code.
 */
public class JavaCodeFormatter {

    private final CodeFormatter codeFormatter;

    public JavaCodeFormatter() {
        Map<String, String> options = new HashMap<>();
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, "space");
        options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
        options.put(DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "4");
        this.codeFormatter = ToolFactory.createCodeFormatter(options);
    }

    public JavaCodeFormatter(Map<String, String> options) {
        this.codeFormatter = ToolFactory.createCodeFormatter(options);
    }

    public static void main(String[] args) {
        JavaCodeFormatter formatter = new JavaCodeFormatter();

        String unformattedCode = "public class HelloWorld{public static void main(String[]args){System.out.println(\"Hello, World!\");}}";

        try {
            String formattedCode = formatter.format(unformattedCode);
            System.out.println("Formatted code:");
            System.out.println(formattedCode);
        } catch (FormatterException e) {
            System.err.println("Error formatting code: " + e.getMessage());
        }
    }

    public String formatNoException(String sourceCode) {
        try {
            return format(sourceCode);
        } catch (FormatterException ex) {
            // TODO log
            return sourceCode;
        }
    }

    /**
     * Formats the given Java source code string.
     *
     * @param sourceCode the unformatted Java source code
     * @return the formatted Java source code
     * @throws FormatterException if the source code cannot be formatted
     */
    public String format(String sourceCode) throws FormatterException {
        TextEdit edit = codeFormatter.format(
                CodeFormatter.K_COMPILATION_UNIT,
                sourceCode,
                0,
                sourceCode.length(),
                0,
                System.lineSeparator()
        );

        if (edit == null) {
            throw new FormatterException("Failed to format source code - possibly invalid syntax");
        }

        IDocument document = new Document(sourceCode);
        try {
            edit.apply(document);
        } catch (Exception e) {
            throw new FormatterException("Failed to apply formatting edits: " + e.getMessage(), e);
        }

        return document.get();
    }

    /**
     * Exception thrown when formatting fails.
     */
    public static class FormatterException extends Exception {
        public FormatterException(String message) {
            super(message);
        }

        public FormatterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
