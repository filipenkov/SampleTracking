package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.util.I18nHelper;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 * @since v3.12
 */
public class MockI18nHelper implements I18nHelper
{
    private final Locale locale;

    public MockI18nHelper()
    {
        this(Locale.getDefault());
    }

    public MockI18nHelper(final Locale locale)
    {
        this.locale = locale;
    }

    public String getText(final String key)
    {
        return key;
    }

    public String getUnescapedText(final String key)
    {
        return getText(key);
    }

    public Locale getLocale()
    {
        return locale;
    }

    public String getText(final String key, final String value1)
    {
        return format(key, new String[] { value1 });
    }

    public String getText(final String key, final String value1, final String value2)
    {
        return format(key, new String[] { value1, value2 });
    }

    public String getText(final String key, final String value1, final String value2, final String value3)
    {
        return format(key, new String[] { value1, value2, value3 });
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
    {
        return format(key, new String[] { value1, value2, value3, value4 });
    }

    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
    {
        return format(key, new Object[] { value1, value2, value3, value4, value5, value6 });
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
    {
        return format(key, new String[] { value1, value2, value3, value4, value5, value6, value7 });
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
    {
        return format(key, new String[] { value1, value2, value3, value4, value5, value6, value7, value8, value9 });
    }

    public String getText(String key, Object value1, Object value2, Object value3)
    {
        return getText(key, EasyList.build(value1, value2, value3));
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4));
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4, value5));
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7));
    }

    public String getText(String key, Object value1, Object value2, Object value3, Object value4, Object value5, Object value6, Object value7, Object value8)
    {
        return getText(key, EasyList.build(value1, value2, value3, value4, value5, value6, value7, value8));
    }
    

    public ResourceBundle getDefaultResourceBundle()
    {
        return null;
    }

    public String getText(final String key, final Object parameter)
    {
        if (parameter == null)
        {
            return key;
        }
        if (parameter.getClass().isArray())
        {
            return format(key, (Object[]) parameter);
        }
        return format(key, new Object[] { parameter });
    }

    public Set<String> getKeysForPrefix(final String prefix)
    {
        return null;
    }

    private String format(String input, final Object[] params)
    {
        for (int i = 0; i < params.length; i++)
        {
            input += " {" + i + "}";
        }
        return MessageFormat.format(input, params);
    }
}
