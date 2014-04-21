package com.atlassian.jira.config.properties;

import com.atlassian.annotations.PublicApi;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * This can be used to lookup JIRA application properties. This uses a two stage strategy for finding property values.
 * First the database is checked to see if a value exists. If it doesnt exist, it falls back to the
 * file for a value.
 *
 * Once a key is placed in the database (via an upgrade task or UI interaction) then it will always be loaded from
 * the database.
 *
 * NOTE : Be very careful with boolean property values. Because of the way OSPropertySets work, its impossible to
 * distinguish between properties that have a false value and properties that have NO value. Therefore it is usually
 * better to have a "String" property set to the value "true" or "false" and then use Boolean.valueOf()
 * in it. This way it's possible to distinguish the absence of a property value from it being set to false.
 */
@PublicApi
public interface ApplicationProperties
{
    String getText(String name);

    String getDefaultBackedText(String name);

    void setText(String name, String value);

    String getString(String name);

    Collection<String> getDefaultKeys();

    String getDefaultBackedString(String name);

    String getDefaultString(String name);

    void setString(String name, String value);

    boolean getOption(String key);

    Collection<String> getKeys();

    void setOption(String key, boolean value);

    String getEncoding();

    public String getMailEncoding();

    String getContentType();

    void refresh();

    Locale getDefaultLocale();

    Collection<String> getStringsWithPrefix(String prefix);

    /**
     * This will return all application and typed values.  For example if the property is a boolean
     * then a Boolean object will be returned.
     *
     * If an application property has a null value, then the key will still be in the {@link java.util.Map#keySet()} 
     *
     * @return a map of key to actual value object
     */
    Map<String, Object> asMap();
}