package com.atlassian.crowd.util;

import org.apache.commons.lang.StringUtils;

public class Assert
{

    /**
     * Assert that an object is not <code>null</code> .
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     *
     * @param object  the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is <code>null</code>
     */
    public static void notNull(Object object, String message)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not <code>null</code> .
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     *
     * @param object the object to check
     * @throws IllegalArgumentException if the object is <code>null</code>
     */
    public static void notNull(Object object) throws IllegalArgumentException
    {
        if (null == object)
        {
            throw new IllegalArgumentException("[Assertion Failed] - argument passed to method must not be null");
        }
    }

    /**
     * Assert that a string is not blank.
     *
     * @param string the string to check
     * @throws IllegalArgumentException if the object is blank
     */
    public static void notBlank(String string) throws IllegalArgumentException
    {
        if (StringUtils.isBlank(string))
        {
            throw new IllegalArgumentException("[Assertion Failed] - argument passed to method must be non-empty");
        }
    }
}