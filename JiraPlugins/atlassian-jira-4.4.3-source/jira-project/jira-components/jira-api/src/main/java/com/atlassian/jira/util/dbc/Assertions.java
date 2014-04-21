package com.atlassian.jira.util.dbc;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;

/**
 * Utility class with design by contract checks. Should be preferred to {@code com.atlassian.jira.util.dbcNull}.
 *
 * @since v3.13
 */
public final class Assertions
{
    /**
     * Throw an IllegalArgumentException if the string is null
     *
     * @param name added to the exception
     * @param notNull should not be null
     * @return argument being checked.
     * @throws IllegalArgumentException if null
     */
    public static <T> T notNull(final String name, final T notNull) throws IllegalArgumentException
    {
        if (notNull == null)
        {
            throw new NullArgumentException(name);
        }
        return notNull;
    }

    /**
     * Throw an IllegalArgumentException if the argument is null or empty
     *
     * @param name added to the exception
     * @param notEmpty should not be null or empty
     * @return argument being checked.
     * @throws IllegalArgumentException if null or empty
     * @since v4.1
     */
    public static <T extends Collection> T notEmpty(final String name, final T notEmpty) throws IllegalArgumentException
    {
        if (notEmpty == null)
        {
            throw new NullArgumentException(name);
        }
        else if (notEmpty.isEmpty())
        {
            throw new EmptyArgumentException(name);
        }
        return notEmpty;
    }

    /**
     * Throw an IllegalArgumentException  if the passed collection is null or contains any null values.
     *
     * @param name name added to the exception.
     * @param containsNoNulls the collection to check.
     * @return the passed in Iterable.
     * @throws IllegalArgumentException if the passed collection is null or contains any null.
     */
    public static <C extends Iterable<?>> C containsNoNulls(final String name, final C containsNoNulls)
            throws IllegalArgumentException
    {
        notNull(name, containsNoNulls);
        int i = 0;
        for (final Object item : containsNoNulls)
        {
            if (item == null)
            {
                throw new NullArgumentException(name + "[" + i + "]");
            }
            i++;
        }
        return containsNoNulls;
    }
    /**
     * Throw an IllegalArgumentException  if the passed array is null or contains any null values.
     *
     * @param name name added to the exception.
     * @param containsNoNulls the collection to check.
     * @return the passed in Iterable.
     * @throws IllegalArgumentException if the passed collection is null or contains any null.
     */
    public static <C> C[] containsNoNulls(final String name, final C[] containsNoNulls)
            throws IllegalArgumentException
    {
        notNull(name, containsNoNulls);
        int i = 0;
        for (final Object item : containsNoNulls)
        {
            if (item == null)
            {
                throw new NullArgumentException(name + "[" + i + "]");
            }
            i++;
        }
        return containsNoNulls;
    }

    /**
     * Throw an IllegalArgumentException if the string is null or blank (only contains whitespace)
     *
     * @param name added to the exception
     * @param string should not be null or blank
     * @return argument being checked.
     * @throws IllegalArgumentException if null or blank
     */
    public static String notBlank(final String name, final String string) throws IllegalArgumentException
    {
        notNull(name, string);
        if (string.trim().length() == 0)
        {
            throw new BlankStringArgumentException(name);
        }
        return string;
    }

    /**
     * Throw and IllegalArgumentException if the passed collection is null or contains any blank
     * Strings.
     *
     * @param name name added to the exception
     * @param stringsNotBlank the collection of strings that should not be blank.
     * @param <C> the type of the collection of strings.
     * @return the passed argument.
     * @throws IllegalArgumentException if the passed collection is null or contains any blank strings.
     */
    public static <C extends Iterable<String>> C containsNoBlanks(final String name, C stringsNotBlank)
    {
        notNull(name, stringsNotBlank);
        int i = 0;
        for (final String item : stringsNotBlank)
        {
            if (StringUtils.isBlank(item))
            {
                throw new BlankStringArgumentException(name + "[" + i + "]");
            }
            i++;
        }
        return stringsNotBlank;
    }

    /**
     * Throw an IllegalArgumentException if the condition is true
     *
     * @param name added to the exception
     * @param condition should be false
     * @throws IllegalArgumentException if true
     */
    public static void not(final String name, final boolean condition) throws IllegalArgumentException
    {
        if (condition)
        {
            throw new IllegalArgumentException(name);
        }
    }

    /**
     * State check. Throw an IllegalStateException if the condition is false
     *
     * @param name added to the exception
     * @param condition should be true
     * @throws IllegalStateException if false
     */
    public static void stateTrue(final String name, final boolean condition) throws IllegalStateException
    {
        if (!condition)
        {
            throw new IllegalStateException(name);
        }
    }

    /**
     * State check. Throw an IllegalStateException if the supplied argument is null.
     *
     * @param name added to the exception
     * @param notNull should not be null
     * @return argument being checked.
     * @throws IllegalStateException if false
     */
    public static <T> T stateNotNull(final String name, final T notNull)
    {
        if (notNull == null)
        {
            throw new NullStateException(name);
        }
        return notNull;
    }

    public static <T> T equals(final String name, final T expected, final T got) throws IllegalArgumentException
    {
        if (!expected.equals(got))
        {
            throw new IllegalArgumentException(name + ". Expected:" + expected + " but got: " + got);
        }
        return got;
    }

    private Assertions()
    {}

    static class BlankStringArgumentException extends IllegalArgumentException
    {
        BlankStringArgumentException(final String name)
        {
            super(name + " should not be empty!");
        }
    }

    static class NullArgumentException extends IllegalArgumentException
    {
        NullArgumentException(final String name)
        {
            super(name + " should not be null!");
        }
    }

    static class EmptyArgumentException extends IllegalArgumentException
    {
        EmptyArgumentException(final String name)
        {
            super(name + " should not be empty!");
        }
    }

    static class NullStateException extends IllegalStateException
    {
        NullStateException(final String name)
        {
            super(name + " should not be null!");
        }
    }
}
