package com.atlassian.jira.pageobjects.components.fields;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.List;

/**
 * Matchers for suggestions.
 *
 * @since v4.4
 */
public class SuggestionMatchers
{
    public static Matcher<Suggestion> hasMainLabel(final String mainLabel)
    {
        return new BaseMatcher<Suggestion>()
        {
            @Override
            public boolean matches(Object obj)
            {
                Suggestion suggestion = (Suggestion) obj;
                return suggestion.getMainLabel().equals(mainLabel);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion with main label ").appendValue(mainLabel).toString();
            }
        };
    }

    public static Matcher<Suggestion> hasLabels(final String mainLabel, final String alias)
    {
        return new BaseMatcher<Suggestion>()
        {
            @Override
            public boolean matches(Object obj)
            {
                Suggestion suggestion = (Suggestion) obj;
                return suggestion.getMainLabel().equals(mainLabel) && suggestion.getAliasLabel().equals(alias);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion with main label ").appendValue(mainLabel)
                        .appendText(" and alias ").appendValue(alias).toString();
            }
        };
    }

    public static Matcher<List<Suggestion>> containsSuggestion(final String mainLabel)
    {
        return new BaseMatcher<List<Suggestion>>()
        {
            @Override
            public boolean matches(Object obj)
            {
                @SuppressWarnings ( { "unchecked" }) List<Suggestion> suggestions = (List) obj;
                for (Suggestion suggestion : suggestions)
                {
                    if (hasMainLabel(mainLabel).matches(suggestion))
                    {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion list contains suggestion with main label ").appendValue(mainLabel)
                        .toString();
            }
        };
    }

    public static Matcher<List<Suggestion>> containsSuggestion(final String mainLabel, final String alias)
    {
        return new BaseMatcher<List<Suggestion>>()
        {
            @Override
            public boolean matches(Object obj)
            {
                @SuppressWarnings ( { "unchecked" }) List<Suggestion> suggestions = (List) obj;
                for (Suggestion suggestion : suggestions)
                {
                    if (hasLabels(mainLabel, alias).matches(suggestion))
                    {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("Suggestion list contains suggestion with main label ").appendValue(mainLabel)
                        .appendText(" and alias ").appendValue(alias).toString();
            }
        };
    }
}
