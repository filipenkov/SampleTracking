package com.atlassian.renderer.v2.macro.code.formatter;

import com.atlassian.renderer.v2.macro.code.formatter.AbstractFormatter;

public class JavaFormatter extends AbstractFormatter
{
    private static final String KEYWORDS =
            "\\b(abstract|assert|break|byvalue|case|cast|catch|" +
            "const|continue|default|do|else|enum|extends|" +
            "false|final|finally|for|future|generic|goto|if|" +
            "implements|import|inner|instanceof|interface|" +
            "native|new|null|operator|outer|package|private|" +
            "protected|public|rest|return|static|super|switch|" +
            "synchronized|this|throw|throws|transient|true|try|" +
            "var|volatile|while)\\b";

    private static final String OBJECTS =
            "\\b(Boolean|Byte|Character|Class|ClassLoader|Cloneable|Compiler|" +
            "Double|Float|Integer|Long|Math|Number|Object|Process|" +
            "Runnable|Runtime|SecurityManager|Short|String|StringBuffer|" +
            "System|Thread|ThreadGroup|Void|boolean|char|byte|short|int|long|float|double)\\b";
    private static final String[] SUPPORTED_LANGUAGES = new String[] { "java" };

    public JavaFormatter()
    {
        addReplacement(QUOTES, QUOTES_REPLACEMENT);
        addReplacement(LINE_COMMENTS, COMMENTS_REPLACEMENT);
        addReplacement(KEYWORDS, KEYWORD_REPLACEMENT);
        addReplacement(OBJECTS, OBJECT_REPLACEMENT);
    }

    public String[] getSupportedLanguages()
    {
        return SUPPORTED_LANGUAGES;
    }
}
