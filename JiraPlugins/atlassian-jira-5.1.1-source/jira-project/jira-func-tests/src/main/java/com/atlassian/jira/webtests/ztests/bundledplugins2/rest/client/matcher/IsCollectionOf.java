package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.matcher;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Project;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @since v4.3
 */
public class IsCollectionOf<T> extends TypeSafeMatcher<Collection<T>> {

    private final Collection<T> expected;

    public IsCollectionOf(Collection<T> expected)
    {
        this.expected = expected;
    }

    public boolean matchesSafely(Collection<T> given)
    {
        List<T> tmp = new ArrayList<T>(expected);
        for (T t : given)
        {
            //noinspection SimplifiableIfStatement
            if (!tmp.remove(t))
            {
                return false;
            }
        }
        return tmp.isEmpty();
    }

    public static Matcher<Collection<String>> ofProjectKeys(List<Project> projects)
    {
        final Collection<String> projectKeys = Transformed.collection(projects, new Function<Project, String>()
        {
            @Override
            public String get(Project project)
            {
                return project.key;
            }
        });

        return new IsCollectionOf<String>(projectKeys);
    }
    public static <T> Matcher<Collection<T>> ofItems(T... items)
    {
        return new IsCollectionOf<T>(Arrays.asList(items));
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(expected.toString());
    }
}
