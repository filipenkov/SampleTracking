package com.atlassian.voorhees;

import java.io.Serializable;
import java.util.Arrays;

/**
 */
public class MockI18nAdapter implements I18nAdapter
{
    @Override
    public String getText(String key, Serializable... arguments)
    {
        return getText(key) + " " + Arrays.toString(arguments);
    }

    @Override
    public String getText(String key)
    {
        return "(i18n) " + key;
    }
}
