package com.atlassian.renderer.v2.macro.code;

import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import org.apache.log4j.Category;

public class SourceCodeFormatterModuleDescriptor extends AbstractModuleDescriptor
{
    public static final Category log = Category.getInstance(SourceCodeFormatterModuleDescriptor.class);

    private SourceCodeFormatter formatter;

    public Object getModule()
    {
        return getFormatter();
    }

    /**
     * Override this method if you need to autowire the formatter, or something.
     */
    protected SourceCodeFormatter makeFormatterFromClass()
    {
        try
        {
            return (SourceCodeFormatter) getModuleClass().newInstance();
        }
        catch (Throwable t)
        {
            log.error("Unable to instantiate code formatter: " + getCompleteKey() + " " + t.getMessage());
        }

        return null;
    }

    public SourceCodeFormatter getFormatter()
    {
        if (formatter == null)
            formatter = makeFormatterFromClass();

        return formatter;
    }
}
