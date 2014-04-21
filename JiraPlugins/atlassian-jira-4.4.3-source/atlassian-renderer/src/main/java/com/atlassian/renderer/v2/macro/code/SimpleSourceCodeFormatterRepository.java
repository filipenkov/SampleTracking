package com.atlassian.renderer.v2.macro.code;

import java.util.*;

public class SimpleSourceCodeFormatterRepository implements SourceCodeFormatterRepository
{
    private Map formatters = new HashMap();

    public SimpleSourceCodeFormatterRepository()
    {
    }

    public SimpleSourceCodeFormatterRepository(List formatters)
    {
        setCodeFormatters(formatters);
    }

    public SourceCodeFormatter getSourceCodeFormatter(String language)
    {
        return (SourceCodeFormatter) formatters.get(language);
    }

    public Collection getAvailableLanguages()
    {
        return Collections.unmodifiableSet(formatters.keySet());
    }

    private void setCodeFormatters(List codeFormatters)
    {
        formatters.clear();

        for (Iterator it = codeFormatters.iterator(); it.hasNext();)
        {
            SourceCodeFormatter formatter = (SourceCodeFormatter) it.next();
            for (int i = 0; i < formatter.getSupportedLanguages().length; i++)
            {
                String language = formatter.getSupportedLanguages()[i];
                formatters.put(language, formatter);
            }
        }

    }
}
