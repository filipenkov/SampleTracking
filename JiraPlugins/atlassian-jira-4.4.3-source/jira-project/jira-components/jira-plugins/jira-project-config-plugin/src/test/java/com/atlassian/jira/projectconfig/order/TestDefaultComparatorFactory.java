package com.atlassian.jira.projectconfig.order;

import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.projectconfig.beans.NamedDefault;
import com.atlassian.jira.projectconfig.beans.SimpleIssueType;
import com.atlassian.jira.projectconfig.beans.SimpleIssueTypeImpl;
import com.atlassian.jira.projectconfig.util.MockProjectConfigRequestCache;
import com.atlassian.jira.user.MockUser;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @since v4.4
 */
public class TestDefaultComparatorFactory
{
    private MockSimpleAuthenticationContext context;
    private MockProjectConfigRequestCache cache;
    private DefaultComparatorFactory factory;

    @Before
    public void setUp() throws Exception
    {
        context = new MockSimpleAuthenticationContext(new MockUser("bbain"), Locale.ENGLISH);
        cache = new MockProjectConfigRequestCache();
        factory = new DefaultComparatorFactory(cache, context);
    }

    @Test
    public void testStringComparator()
    {
        Comparator<String> comparator = factory.createStringComparator();

        assertSame(comparator, factory.createStringComparator());

        List<String> order = newArrayList("zzz", "ZZZ", "def", "AAA", "aaa");
        Collections.sort(order, comparator);

        List<String> order2 = newArrayList("ZZZ", "zzz", "aaa", "def", "AAA");
        Collections.sort(order2, comparator);

        final List<String> expected;
        //The order of either of these lists is correct dependeing upon the Locale settings.
        if (comparator.compare("Z", "z") < 0)
        {
            expected = newArrayList("AAA", "aaa", "def", "ZZZ", "zzz");
        }
        else
        {
            expected = newArrayList("aaa", "AAA", "def", "zzz", "ZZZ");
        }

        assertEquals(expected, order);
        assertEquals(expected, order2);
    }

    @Test
    public void testNamedDefaultOrder()
    {
        SimpleNamedDefault def1 = new SimpleNamedDefault("zzz", true);
        SimpleNamedDefault def2 = new SimpleNamedDefault("abc", true);
        SimpleNamedDefault def3 = new SimpleNamedDefault("ABC", true);

        SimpleNamedDefault name1 = new SimpleNamedDefault("aaa", false);
        SimpleNamedDefault name2 = new SimpleNamedDefault("AAA", false);
        SimpleNamedDefault name3 = new SimpleNamedDefault("zzz", false);

        Comparator<NamedDefault> comparator = factory.createNamedDefaultComparator();
        assertSame(comparator, factory.createNamedDefaultComparator());

        boolean upperLess = comparator.compare(def3, def2) < 0;
        final List<SimpleNamedDefault> expectedList;
        if (upperLess)
        {
            expectedList = newArrayList(def3, def2, def1, name2, name1, name3);
        }
        else
        {
            expectedList = newArrayList(def2, def3, def1, name1, name2, name3);
        }

        List<SimpleNamedDefault> actual = newArrayList(def3, name3, name2, name1, def2, def1);
        Collections.sort(actual, comparator);
        assertEquals(expectedList, actual);

        actual = newArrayList(def2, def3, name3, name1, def1, name2);
        Collections.sort(actual, comparator);
        assertEquals(expectedList, actual);
    }

    @Test
    public void testSimpleIssueTypeOrder()
    {
        SimpleIssueType def1 = createIssueType("zzz", true, true);
        SimpleIssueType def2 = createIssueType("abc", true, false);
        SimpleIssueType def3 = createIssueType("ABC", true, false);

        SimpleIssueType name1 = createIssueType("aaa", false, false);
        SimpleIssueType name2 = createIssueType("AAA", false, false);
        SimpleIssueType name3 = createIssueType("zzz", false, false);

        SimpleIssueType subName1 = createIssueType("aaa", false, true);
        SimpleIssueType subName2 = createIssueType("AAA", false, true);
        SimpleIssueType subName3 = createIssueType("zzz", false, true);

        Comparator<SimpleIssueType> comparator = factory.createIssueTypeComparator();
        assertSame(comparator, factory.createIssueTypeComparator());

        boolean upperLess = comparator.compare(def3, def2) < 0;
        final List<SimpleIssueType> expectedList;
        if (upperLess)
        {
            expectedList = newArrayList(def3, def2, def1, name2, name1, name3, subName2, subName1, subName3);
        }
        else
        {
            expectedList = newArrayList(def2, def3, def1, name1, name2, name3, subName1, subName2, subName3);
        }

        ArrayList<SimpleIssueType> actual = newArrayList(subName3, subName2, subName1, name1, def3, def2, def1, name2, name3);
        Collections.sort(actual, comparator);
        assertEquals(expectedList, actual);

        actual = newArrayList(name2, subName3, subName1, subName2, name1, def3, def1, name3, def2);
        Collections.sort(actual, comparator);
        assertEquals(expectedList, actual);
    }

    private static SimpleIssueType createIssueType(final String name, final boolean defaultIssueType, final boolean subTask)
    {
        return new SimpleIssueTypeImpl(name, name, name, null, subTask, defaultIssueType);
    }
}
