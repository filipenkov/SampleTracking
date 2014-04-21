package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * A matcher to match iterables that contain a string having a given prefix.
 *
 * @since v4.3
 */
public class ContainsStringThatStartsWith<T extends Iterable<String>> extends TypeSafeMatcher<T>
{
    public static <T extends Iterable<String>> Matcher<T> containsStringThatStartsWith(String startsWith)
    {
        return new ContainsStringThatStartsWith<T>(startsWith);
    }

    /**
     * The expected prefix.
     */
    private final String startsWith;

    /**
     * Creates a new ContainsStringStartingWith matcher.
     *
     * @param startsWith the prefix to expect
     */
    public ContainsStringThatStartsWith(String startsWith)
    {
        if (startsWith == null) { throw new NullPointerException("startsWith"); }
        this.startsWith = startsWith;
    }

    @Override
    public boolean matchesSafely(T item)
    {
        for (String string : item)
        {
            if (string.startsWith(startsWith))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(String.format("An Iterable containing a string that starts with \"%s\"", startsWith));
    }
}
