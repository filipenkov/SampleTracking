package com.atlassian.renderer.v2.macro.code.formatter;

import com.atlassian.renderer.v2.Replacer;
import com.atlassian.renderer.v2.macro.code.SourceCodeFormatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public abstract class AbstractFormatter implements SourceCodeFormatter
{
    public static final String QUOTES = "(\"|\\&quot;)(.+?)(?<!(\"|\\&quot;))(\"|\\&quot;)";
    public static final String LINE_COMMENTS = "//(.*?)($|\r?\n)";

    public static final String QUOTES_REPLACEMENT = "<span class=\"code-quote\">$1$2$4</span>";
    public static final String COMMENTS_REPLACEMENT = "<span class=\"code-comment\">$0</span>";
    public static final String KEYWORD_REPLACEMENT = "<span class=\"code-keyword\">$1</span>";
    public static final String OBJECT_REPLACEMENT = "<span class=\"code-object\">$1</span>";

    protected List replacers = new ArrayList();

    protected void addReplacement(String pattern, String replacement)
    {
        replacers.add(new Replacer(Pattern.compile(pattern), replacement, new String[]{}));
    }

    public String format(String code, String language)
    {
        for (Iterator it = replacers.iterator(); it.hasNext();)
        {
            Replacer replacer = (Replacer) it.next();
            code = replacer.replaceAll(code);
        }

        return code;
    }
}
