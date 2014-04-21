package com.atlassian.streams.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.spi.StreamsFilterOption;

import com.google.common.collect.ImmutableList;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraFilterOptionProviderTest
{
    private static final String KEY = "Bug";
    private static final String ENGLISH_TRANSLATION = "The Bug";
    private static final String FRENCH_TRANSLATION = "Le Bug";
    
    @Mock PermissionManager permissionManager;
    @Mock JiraAuthenticationContext authenticationContext;
    @Mock IssueTypeSchemeManager issueTypeSchemeManager;
    @Mock I18nResolver i18nResolver;

    @Mock IssueType issueType;
    @Mock Project project;

    private JiraFilterOptionProvider provider;

    @Before
    public void setup()
    {
        when(permissionManager.getProjectObjects(anyInt(), any(User.class))).thenReturn(ImmutableList.of(project));
        when(issueTypeSchemeManager.getIssueTypesForProject(project)).thenReturn(ImmutableList.of(issueType));
        when(issueType.getId()).thenReturn(KEY);
        when(issueType.getName()).thenReturn(ENGLISH_TRANSLATION);
        when(issueType.getNameTranslation()).thenReturn(FRENCH_TRANSLATION);

        provider = new JiraFilterOptionProvider(permissionManager, authenticationContext, issueTypeSchemeManager, i18nResolver);
    }

    @Test
    public void assertThatFilterResourceHasTranslatedIssueTypeValues()
    {
        assertThat(provider.getFilterOptions(), hasOption(withValues(contains(FRENCH_TRANSLATION))));
    }

    @Test
    public void assertThatFilterResourceDoesNotHaveNonTranslatedIssueTypeValues()
    {
        assertThat(provider.getFilterOptions(), not(hasOption(withValues(contains(ENGLISH_TRANSLATION)))));
    }

    static Matcher<Iterable<? super StreamsFilterOption>> hasOption(Matcher<StreamsFilterOption> matcher)
    {
        return hasItem(matcher);
    }

    static Matcher<StreamsFilterOption> withValues(Matcher<Iterable<String>> matcher)
    {
        return new WithValues(matcher);
    }

    private static final class WithValues extends TypeSafeDiagnosingMatcher<StreamsFilterOption>
    {
        private final Matcher<Iterable<String>> matcher;

        public WithValues(Matcher<Iterable<String>> matcher)
        {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(StreamsFilterOption element, Description mismatchDescription)
        {
            if (!matcher.matches(element.getValues().values()))
            {
                mismatchDescription.appendText("values ");
                return false;
            }

            return true;
        }

        public void describeTo(Description description)
        {
            description.appendText("values ").appendDescriptionOf(matcher);
        }
    }
}
