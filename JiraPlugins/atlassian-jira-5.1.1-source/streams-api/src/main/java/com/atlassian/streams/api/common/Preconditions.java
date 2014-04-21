package com.atlassian.streams.api.common;

import java.net.URI;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Provides static utility methods for checking arguments and state in constructors and methods, throwing appropriate
 * exceptions if the checks fail.  This is modeled after a similar class in the Google Collections API, though it does
 * not share any of its source code.
 * <p/>
 * It's recommended to use static imports to reduce verbosity.
 *
 * @see <code><a href="http://google-collections.googlecode.com/svn/trunk/javadoc/com/google/common/base/Preconditions.html">com.google.common.base.Preconditions</a></code>
 * @since 1.1
 */
public final class Preconditions
{
    /**
     * The constructor is private because this class should not be instantiated.
     */
    private Preconditions() {}

    public static <T, C extends Iterable<T>> C checkNotEmpty(C iterable, String name)
    {
        if (isEmpty(checkNotNull(iterable, name)))
        {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return iterable;
    }

    public static <T> T[] checkNotEmpty(T[] array, String name)
    {
        if (isEmpty(checkNotNull(array, name)))
        {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return array;
    }

    public static <K, V> Map<K, V> checkNotEmpty(Map<K, V> map, String name)
    {
        if (checkNotNull(map, name).isEmpty())
        {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return map;
    }

    public static String checkNotBlank(String text, String name)
    {
        if (isBlank(checkNotNull(text, name)))
        {
            throw new IllegalArgumentException(name + " must not be empty or blank");
        }
        return text;
    }
    
    public static URI checkAbsolute(URI uri, String name)
    {
        if (uri != null && !uri.isAbsolute())
        {
            throw new IllegalArgumentException(name + " must be an absolute URI");
        }
        return uri;
    }
    
}
