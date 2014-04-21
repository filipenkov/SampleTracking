package com.atlassian.applinks.api;

import java.util.List;
import java.util.Properties;

/**
 * Provides access to settings.
 * <p/>
 * The following types are supported:
 * <ul>
 * <li>java.lang.String</li>
 * <li>java.util.List</li>
 * <li>java.util.Properties</li>
 * </ul>
 * <p/>
 * Instances are assumed to be not threadsafe and mutable.
 *
 * @since 3.0
 */
public interface PropertySet
{
    /**
     * Gets a setting value.
     *
     * @param key The setting key.  Cannot be null
     * @return The setting value. May be null
     */
    Object getProperty(String key);

    /**
     * Puts a setting value. Note that the namespace for this key is shared between all applinks consumers. If you don't
     * want a different plugin to override a property you have set, ensure that you use a unique key. A good way to do
     * this is to prefix the key with your plugin key, which is guaranteed to be globally unique.
     *
     * @param key   Setting key.  Cannot be null
     * @param value Setting value.  Must be one of {@link String}, {@link List}, {@link Properties} or null. a null value is equivalent to {@link #removeProperty(String)}
     * @throws IllegalArgumentException if value is not {@link String}, {@link List}, {@link Properties} or null.
     * @return The setting value that was over ridden. Null if none existed.
     */
    Object putProperty(String key, Object value);

    /**
     * Removes a setting value
     *
     * @param key The setting key
     * @return The setting value that was removed. Null if nothing was removed.
     */
    Object removeProperty(String key);

}
