package com.atlassian.streams.jira.builder;

import java.net.URI;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.StreamsEntry.HasAlternateLinkUri;
import com.atlassian.streams.api.StreamsEntry.HasApplicationType;
import com.atlassian.streams.api.StreamsEntry.HasId;
import com.atlassian.streams.api.StreamsEntry.HasPostedDate;
import com.atlassian.streams.api.StreamsEntry.NeedsAuthors;
import com.atlassian.streams.api.StreamsEntry.NeedsRenderer;
import com.atlassian.streams.api.StreamsEntry.NeedsVerb;
import com.atlassian.streams.api.StreamsEntry.Parameters;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.ImmutableNonEmptyList;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.jira.AggregatedJiraActivityItem;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.JiraHelper;
import com.atlassian.streams.jira.renderer.AttachmentRendererFactory;
import com.atlassian.streams.jira.renderer.IssueUpdateRendererFactory;
import com.atlassian.streams.spi.StreamsI18nResolver;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.ISSUE_TYPE;
import static com.atlassian.streams.api.ActivityVerbs.update;
import static com.atlassian.streams.api.StreamsEntry.params;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.google.common.base.Predicates.alwaysTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GeneralUpdateEntryBuilderTest
{
    @Mock AttachmentRendererFactory attachmentRendererFactory;
    @Mock IssueUpdateRendererFactory issueUpdateRendererFactory;
    @Mock JiraHelper helper;
    @Mock StreamsI18nResolver i18nResolver;
    Predicate<GenericValue> validAttachment = alwaysTrue();

    GeneralUpdateEntryBuilder builder;

    @Before
    public void prepareBuilder()
    {
        builder = new GeneralUpdateEntryBuilder(helper, attachmentRendererFactory, issueUpdateRendererFactory, i18nResolver);
    }

    @Before
    public void prepareHelper()
    {
        Parameters<HasId, HasPostedDate, HasAlternateLinkUri, HasApplicationType, NeedsRenderer, NeedsVerb, NeedsAuthors> params = newParams();
        when(helper.newBuilder(any(JiraActivityItem.class))).thenReturn(params);
        when(helper.buildActivityObject(any(Issue.class), any(String.class))).thenReturn(newActivityObject());
        when(helper.validAttachment()).thenReturn(validAttachment);

        ImmutableNonEmptyList<UserProfile> userProfiles = ImmutableNonEmptyList.of(newUserProfile("fred"));
        when(helper.getUserProfiles(argThat(contains("fred")))).thenReturn(userProfiles);
    }

    @Before
    public void prepareIssueUpdateRendererFactory()
    {
        Renderer r = newRenderer();
        when(issueUpdateRendererFactory.newRenderer(any(JiraActivityItem.class), anyIterable(GenericValue.class))).thenReturn(r);
    }

    @SuppressWarnings("unchecked")
    private <T> Iterable<T> anyIterable(Class<T> t)
    {
        return any(Iterable.class);
    }

    @Test
    public void assertThatEmptyChangeItemsReturnNone()
    {
        AggregatedJiraActivityItem item = newActivityItem();

        assertThat(builder.build(item), is(none(StreamsEntry.class)));
    }

    @Test
    public void assertThatEntryBuilderReturnsStreamEntryForValidChangeItem()
    {
        AggregatedJiraActivityItem item = newActivityItem(newChangeItem(ISSUE_TYPE, "Bug"));

        assertThat(builder.build(item), is(not(none(StreamsEntry.class))));
    }

    private Parameters<HasId, HasPostedDate, HasAlternateLinkUri, HasApplicationType, NeedsRenderer, NeedsVerb, NeedsAuthors> newParams()
    {
        return params().
            id(URI.create("urn:1")).
            postedDate(new DateTime()).
            alternateLinkUri(URI.create("http://example.com/1")).
            applicationType("test");
    }

    private Renderer newRenderer()
    {
        Renderer renderer = mock(Renderer.class);
        return renderer;
    }

    @Test
    public void assertThatEntryBuilderSkipsProjectImportChanges()
    {
        AggregatedJiraActivityItem item = newActivityItem(newChangeItem("projectimport", "newvalue"));

        assertThat(builder.build(item), is(none(StreamsEntry.class)));
    }

    @Test
    public void assertThatEntryBuilderSkipsWorkflowChanges()
    {
        AggregatedJiraActivityItem item = newActivityItem(newChangeItem("workflow", "newvalue"));

        assertThat(builder.build(item), is(none(StreamsEntry.class)));
    }

    private AggregatedJiraActivityItem newActivityItem(GenericValue... changeItems)
    {
        ChangeHistory history = newChangeHistory(changeItems);
        return newActivityItem(history);
    }

    private ChangeHistory newChangeHistory(GenericValue... changeItems)
    {
        ChangeHistory history = mock(ChangeHistory.class);
        when(history.getChangeItems()).thenReturn(ImmutableList.of(changeItems));
        when(history.getUsername()).thenReturn("fred");
        return history;
    }

    private AggregatedJiraActivityItem newActivityItem(ChangeHistory changeHistory)
    {
        JiraActivityItem item = mock(JiraActivityItem.class);
        when(item.getChangeHistory()).thenReturn(some(changeHistory));
        when(item.getActivity()).thenReturn(pair(issue(), update()));
        when(item.getChangeHistoryAuthors()).thenReturn(ImmutableList.of("fred"));

        AggregatedJiraActivityItem aggregatedItem = mock(AggregatedJiraActivityItem.class);
        Option<Iterable<JiraActivityItem>> relatedItems = none();
        when(aggregatedItem.getRelatedActivityItems()).thenReturn(relatedItems);
        when(aggregatedItem.getActivityItem()).thenReturn(item);
        return aggregatedItem;
    }

    private GenericValue newChangeItem(String field, String newValue)
    {
        GenericValue changeItem = mock(GenericValue.class);
        when(changeItem.getString("field")).thenReturn(field);
        when(changeItem.getString("newvalue")).thenReturn(newValue);
        when(changeItem.getString("newstring")).thenReturn(newValue);
        return changeItem;
    }

    private ActivityObject newActivityObject()
    {
        return new ActivityObject(ActivityObject.params().
                id("urn:1").
                alternateLinkUri(URI.create("http://example.com/1")).
                activityObjectType(issue()));
    }

    private UserProfile newUserProfile(String name)
    {
        UserProfile p = mock(UserProfile.class);
        when(p.getUsername()).thenReturn(name);
        when(p.getFullName()).thenReturn(name);
        when(p.getEmail()).thenReturn(some(name + "@a.com"));
        when(p.getProfilePageUri()).thenReturn(none(URI.class));
        when(p.getProfilePictureUri()).thenReturn(none(URI.class));
        return p;
    }
}
