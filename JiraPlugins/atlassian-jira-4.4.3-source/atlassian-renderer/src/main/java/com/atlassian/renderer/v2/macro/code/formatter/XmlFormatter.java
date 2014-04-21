package com.atlassian.renderer.v2.macro.code.formatter;

import com.atlassian.renderer.v2.macro.code.formatter.AbstractFormatter;

public class XmlFormatter extends AbstractFormatter
{
    private static final String KEYWORDS1 = "\\b(xsl:[^&\\s]*)\\b";
    private static final String KEYWORDS2 = "\\b(xmlns:[^&=\\s]*)\\b";
    private static final String TAGS = "(&lt;/?(.*?)&gt;)";
    private static final String COMMENTS = "((<|&lt;)(\\!|&#33;)--)(.+?)(--(>|&gt;))";

    private static final String XML_COMMENTS_REPLACEMENT = "<span class=\"code-comment\">$1$4$5</span>";
    private static final String TAGS_REPLACEMENT = "<span class=\"code-tag\">$1</span>";
    private static final String[] SUPPORTED_LANGUAGES = new String[] { "xml", "html", "xhtml" };

    public XmlFormatter()
    {
        addReplacement(QUOTES, QUOTES_REPLACEMENT);
        addReplacement(KEYWORDS1, KEYWORD_REPLACEMENT);
        addReplacement(KEYWORDS2, KEYWORD_REPLACEMENT);
        addReplacement(TAGS, TAGS_REPLACEMENT);
        addReplacement(COMMENTS, XML_COMMENTS_REPLACEMENT);
    }

    public String[] getSupportedLanguages()
    {
        return SUPPORTED_LANGUAGES;
    }
}