package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractNavigationUtil;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.navigation.IssueNavigatorNavigation;
import com.atlassian.jira.functest.framework.navigator.ContainsIssueKeysCondition;
import com.atlassian.jira.functest.framework.navigator.DoesNotContainIssueKeyCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorCondition;
import com.atlassian.jira.functest.framework.navigator.NavigatorSearch;
import com.atlassian.jira.functest.framework.navigator.NumberOfIssuesCondition;
import com.atlassian.jira.functest.framework.navigator.SearchResultsCondition;
import com.atlassian.jira.functest.framework.sharing.SharedEntityInfo;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Arrays.asList;

public class IssueNavigatorAssertionsImpl extends AbstractNavigationUtil implements IssueNavigatorAssertions
{
    public IssueNavigatorAssertionsImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData);
    }

    public void assertSimpleSearch(NavigatorSearch search, WebTester tester)
    {
        tester.setWorkingForm("issue-filter");
        for (NavigatorCondition condition : search.getConditions())
        {
            condition.assertSettings(tester);
        }
    }

    public void assertAdvancedSearch(WebTester tester, String...values)
    {
        final String jqlValue = tester.getDialog().getFormParameterValue("jqlQuery");
        for (String value : values)
        {
            Assert.assertTrue("The string '" + value + "' could not be found in the JQL '" + jqlValue + "'.", jqlValue.contains(value));
        }
    }

    public void assertSearchInfo(SharedEntityInfo info)
    {
        final XPathLocator locator = new XPathLocator(tester, "//*[@id='filter-description']");
        getTextAssertions().assertTextPresent(locator, info.getName());
        if (StringUtils.isNotBlank(info.getDescription()))
        {
            getTextAssertions().assertTextPresent(locator, info.getDescription());
        }

        boolean isFavourite = new XPathLocator(tester, "//a[starts-with(@id, 'fav_a_nav_SearchRequest_') and @title='Remove this filter from your favourites']").exists();
        boolean isNotFavourite = new XPathLocator(tester, "//a[starts-with(@id, 'fav_a_nav_SearchRequest_') and @title='Add this filter to your favourites']").exists();
        if (info.isFavourite())
        {
            Assert.assertTrue("Should have been favourited but wasn't", isFavourite);
            Assert.assertFalse("Should not have been un-favourited but was", isNotFavourite);
        }
        else
        {
            Assert.assertFalse("Should not have been favourited but wasn", isFavourite);
            Assert.assertTrue("Should have been un-favourited but wasn't", isNotFavourite);
        }
    }

    public void assertSearchResults(final Iterable<? extends SearchResultsCondition> conditions)
    {
        for (final SearchResultsCondition condition : conditions)
        {
            condition.assertCondition(tester);
        }
    }

    @Override
    public void assertSearchResultsAreEmpty()
    {
        assertSearchResults(ImmutableList.of(new NumberOfIssuesCondition(getTextAssertions(),0)));
    }

    public void assertExactIssuesInResults(final String... keys)
    {
        final List<SearchResultsCondition> conditions = new ArrayList<SearchResultsCondition>();
        if (keys.length > 0)
        {
            conditions.add(new ContainsIssueKeysCondition(getTextAssertions(), keys));
        }
        conditions.add(new NumberOfIssuesCondition(getTextAssertions(), keys.length));

        assertSearchResults(conditions);
    }

    @Override
    public void assertSearchResultsContain(final String... keys)
    {
        checkNotNull(keys);
        checkArgument(!isEmpty(asList(keys)));

        assertSearchResults(ImmutableList.<SearchResultsCondition>of(new ContainsIssueKeysCondition(getTextAssertions(), keys)));
    }

    @Override
    public void assertSearchResultsDoNotContain(String... keys)
    {
        checkNotNull(keys);
        checkArgument(!isEmpty(asList(keys)));

        assertSearchResults(Iterables.transform(asList(keys), new Function<String, SearchResultsCondition>()
        {
            @Override
            public SearchResultsCondition apply(@Nullable String input)
            {
                return new DoesNotContainIssueKeyCondition(getTextAssertions(), input);
            }
        }));
    }

    public void assertJqlErrors(final String ... errorMessages)
    {
        getTextAssertions().assertTextSequence(new IdLocator(tester, "jqlerror"), errorMessages);
    }

    public void assertJqlWarnings(final String... warningMessages)
    {
        getTextAssertions().assertTextSequence(new IdLocator(tester, "jqlwarning"), warningMessages);
    }

    public void assertJqlTooComplex()
    {
        Assert.assertEquals("Should be on the advanced view.", IssueNavigatorNavigation.NavigatorEditMode.ADVANCED, getNavigation().issueNavigator().getCurrentEditMode());        
        final Locator locator = new IdLocator(tester, "filter-switch");
        getTextAssertions().assertTextSequence(locator, "This query is too complex to display in Simple mode.");
    }

    public void assertNoJqlErrors()
    {
        Assert.assertEquals(0, new IdLocator(tester, "jqlerror").getNodes().length);
        Assert.assertEquals(0, new IdLocator(tester, "jqlwarning").getNodes().length);
    }

    public void assertJqlFitsInFilterForm(final String jqlQuery, final FilterFormParam... formParams)
    {
        log("Asserting fits filter: '" + jqlQuery + "' with params: " + asList(formParams));
        getNavigation().issueNavigator().createSearch(jqlQuery);
        getNavigation().issueNavigator().gotoEditMode(IssueNavigatorNavigation.NavigatorEditMode.SIMPLE);
        for (FilterFormParam formParam : formParams)
        {
            tester.setWorkingForm("issue-filter");
            assertSameElements(formParam.getValues(), tester.getDialog().getForm().getParameterValues(formParam.getName()));
        }
    }

    private static void assertSameElements(String[] a, String[] b)
    {
        Set<String> as = (a == null || a.length == 0) ? null : new HashSet<String>(asList(a));
        Set<String> bs = (b == null || b.length == 0) ? null : new HashSet<String>(asList(b));
        Assert.assertEquals(as, bs);
    }

    public void assertIssueNavigatorDisplaying(Locator locator, String from, String to, String of)
    {
        getTextAssertions().assertTextSequence(locator,new String[] { "Displaying issues", from, "to", to, "of", of, "matching issues" });
    }

    protected TextAssertions getTextAssertions()
    {
        return getFuncTestHelperFactory().getTextAssertions();
    }
}
