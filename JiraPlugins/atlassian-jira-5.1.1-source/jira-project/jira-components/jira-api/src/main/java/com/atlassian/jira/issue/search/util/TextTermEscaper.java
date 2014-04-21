package com.atlassian.jira.issue.search.util;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Set;

public class TextTermEscaper implements Function<CharSequence, String>
{
    // the full list of reserved chars in Lucene:
    // '\\', '+', '-', '!', '(', ')', ':', '^', '[', ']', '\"', '{', '}', '~', '*', '?', '|', '&'
    private static final Set<Character> chars = CollectionBuilder.newBuilder('\\',':').asSet();

    public static String escape(final CharSequence input)
    {
        return new TextTermEscaper().get(input);
    }

    public String get(final CharSequence input)
    {
        final StringBuilder escaped = new StringBuilder(input.length() * 2);
        for (int i = 0; i < input.length(); i++)
        {
            final Character c = input.charAt(i);
            if (chars.contains(c))
            {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }
}
