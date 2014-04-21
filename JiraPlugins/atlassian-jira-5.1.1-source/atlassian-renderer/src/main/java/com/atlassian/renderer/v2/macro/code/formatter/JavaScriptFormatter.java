package com.atlassian.renderer.v2.macro.code.formatter;

import com.atlassian.renderer.v2.macro.code.formatter.AbstractFormatter;

public class JavaScriptFormatter extends AbstractFormatter
{
    private static final String KEYWORDS =
            "\\b(abstract|boolean|break|byte|case|catch|char|const|continue|debugger|" +
            "default|delete|do|double|else|enum|export|extends|false|final|finally|float|" +
            "for|function|goto|if|implements|import|in|instanceof|int|interface|long|native|" +
            "new|null|package|private|protected|public|return|short|static|super|switch|" +
            "synchronized|this|throw|throws|transient|true|try|typeof|var|void|while|with)\\b";

    private static final String OBJECTS =
            "\\b(Boolean|Byte|Character|Class|ClassLoader|Cloneable|Compiler|" +
            "Double|Float|Integer|Long|Math|Number|Object|Process|" +
            "Runnable|Runtime|SecurityManager|Short|String|StringBuffer|" +
            "System|Thread|ThreadGroup|Void|boolean|char|byte|short|int|long|float|double)\\b";
    private static final String[] SUPPORTED_LANGUAGES = new String[] { "javascript" };

    public JavaScriptFormatter()
    {
        addReplacement(KEYWORDS, KEYWORD_REPLACEMENT);
        addReplacement(OBJECTS, OBJECT_REPLACEMENT);
    }

    public String[] getSupportedLanguages()
    {
        return SUPPORTED_LANGUAGES;
    }
}