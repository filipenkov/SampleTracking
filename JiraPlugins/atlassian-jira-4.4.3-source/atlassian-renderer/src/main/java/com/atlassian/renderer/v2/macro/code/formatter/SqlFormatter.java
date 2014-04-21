package com.atlassian.renderer.v2.macro.code.formatter;

import com.atlassian.renderer.v2.macro.code.formatter.AbstractFormatter;

public class SqlFormatter extends AbstractFormatter
{
    private static final String KEYWORDS = "(?i)\\b(SELECT|DELETE|UPDATE|WHERE|FROM|GROUP|BY|HAVING)\\b";
    private static final String OBJECTS = "\\b(VARCHAR)\\b";
    private static final String COMMENTS = "^\\s*--.*";
    private static final String[] SUPPORTED_LANGUAGES = new String[] { "sql" };

    public SqlFormatter()
    {
        addReplacement(QUOTES, QUOTES_REPLACEMENT);
        addReplacement(COMMENTS, COMMENTS_REPLACEMENT);
        addReplacement(OBJECTS, OBJECT_REPLACEMENT);
        addReplacement(KEYWORDS, KEYWORD_REPLACEMENT);
    }

    public String[] getSupportedLanguages()
    {
        return SUPPORTED_LANGUAGES;
    }
}
