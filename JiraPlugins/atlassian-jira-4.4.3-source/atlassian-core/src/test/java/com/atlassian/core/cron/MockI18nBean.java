package com.atlassian.core.cron;

import com.atlassian.core.i18n.I18nTextProvider;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class MockI18nBean implements I18nTextProvider
{
    final ResourceBundle resourceBundle;

    public MockI18nBean()
    {
        resourceBundle = ResourceBundle.getBundle("com.atlassian.core.AtlassianCore");
    }

    public String getText(final String key)
    {
        return resourceBundle.getString(key);
    }

    public String getText(final String key, final Object[] args)
    {
        String message = getText(key);
        final MessageFormat mf = new MessageFormat(message);
        return mf.format(args);
    }
}
