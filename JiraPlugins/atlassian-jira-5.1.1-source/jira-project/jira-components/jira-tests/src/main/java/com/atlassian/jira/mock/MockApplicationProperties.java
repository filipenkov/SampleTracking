package com.atlassian.jira.mock;

import com.atlassian.jira.config.properties.ApplicationProperties;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Simple ApplicationProperties implementation backed by a Map.
 */
public class MockApplicationProperties implements ApplicationProperties
{
    Map properties = new HashMap();
    private String encoding;

    public MockApplicationProperties() {

    }

    public MockApplicationProperties(Map initialProperties) {

        properties = initialProperties;
    }

    public String getText(String name)
    {
        return (String) properties.get(name);
    }

    public String getDefaultBackedText(String name)
    {
        return getText(name);
    }

    //remove the name/value entry if value is NULL
    public void setText(String name, String value)
    {
        if (value == null)
        {
            if (StringUtils.isNotEmpty(getDefaultBackedString(name)))
            {
                properties.remove(name);
            }
        }
        else
        {
            properties.put(name, value);
        }
    }

    public String getString(String name)
    {
        return getText(name);
    }

    public Collection<String> getDefaultKeys()
    {
        return Collections.EMPTY_SET;
    }

    public String getDefaultBackedString(String name)
    {
        return getText(name);
    }

    public String getDefaultString(String name)
    {
        return getText(name);
    }

    public void setString(String name, String value)
    {
        setText(name, value);
    }

    public boolean getOption(String key)
    {
        final Boolean aBoolean = (Boolean) properties.get(key);
        return aBoolean != null && aBoolean.booleanValue();
    }

    public Collection getKeys()
    {
        return properties.keySet();
    }

    public void setOption(String key, boolean value)
    {
        properties.put(key, Boolean.valueOf(value));
    }

    public String getEncoding()
    {
        return encoding;
    }

    public String getMailEncoding()
    {
        return "UTF-8";
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getContentType()
    {
        return null;
    }

    public void refresh()
    {

    }

    public Locale getDefaultLocale()
    {
        return null;
    }

    public Collection getStringsWithPrefix(String prefix)
    {
        return null;
    }

    public Map<String, Object> asMap()
    {
        return properties;
    }
}
