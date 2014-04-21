package com.atlassian.jira.util;

import com.atlassian.core.util.collection.EasyList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * An {@link com.atlassian.jira.util.I18nHelper} that returns the i18n key concatenated with its arguments.
 * This can be used in tests to ensure correct i18n messages are returned. The {@link com.atlassian.jira.util.NoopI18nHelper#makeTranslation(String, java.util.List)}
 * method can be used to create an expected value of a test.
 *
 * @since v4.0
 */
public class NoopI18nHelper implements I18nHelper
{
    public String getText(final String key)
    {
        return getText(key, Collections.emptyList());
    }

    public Locale getLocale()
    {
        return Locale.ENGLISH;
    }

    public String getUnescapedText(final String key)
    {
        throw new UnsupportedOperationException();
    }

    public String getText(final String key, final String value1)
    {
        return getText(key, Collections.singletonList(value1));
    }

    public String getText(String key, Object value1, Object value2, Object value3)
    {
        return getText(key, com.atlassian.core.util.collection.EasyList.build(value1, value2, value3));
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

    public String getText(final String key, final String value1, final String value2)
    {
        return getText(key, new String[] { value1, value2 });
    }

    public String getText(final String key, final String value1, final String value2, final String value3)
    {
        return getText(key, new String[] { value1, value2, value3 });
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4)
    {
        return getText(key, new String[] { value1, value2, value3, value4 });
    }

    public String getText(final String key, final Object value1, final Object value2, final Object value3, final Object value4, final Object value5, final Object value6)
    {
        return getText(key, new Object[] { value1, value2, value3, value4, value5, value6 });
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7)
    {
        return getText(key, new Object[] { value1, value2, value3, value4, value5, value6, value7 });
    }

    public String getText(final String key, final String value1, final String value2, final String value3, final String value4, final String value5, final String value6, final String value7, final String value8, final String value9)
    {
        return getText(key, new Object[] { value1, value2, value3, value4, value5, value6, value7, value8, value9 });
    }

    public ResourceBundle getDefaultResourceBundle()
    {
        throw new UnsupportedOperationException();
    }

    public String getText(final String key, final Object params)
    {
        if (params instanceof List<?>)
        {
            return makeTranslation(key, (List<?>) params);
        }
        else if (params instanceof Object[])
        {
            return makeTranslation(key, Arrays.asList((Object[]) params));
        }
        else
        {
            return makeTranslation(key, Collections.singletonList(params));
        }
    }

    public Set<String> getKeysForPrefix(final String prefix)
    {
        return null;
    }

    public static String makeTranslation(final String key, final Object... arguments)
    {
        return makeTranslation(key, Arrays.asList(arguments));
    }

    public static String makeTranslation(final String key, final List<?> arguments)
    {
        return key + "{" + arguments + "}";
    }
}
