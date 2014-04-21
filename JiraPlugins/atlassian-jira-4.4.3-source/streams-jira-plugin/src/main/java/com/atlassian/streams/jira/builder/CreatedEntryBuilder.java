package com.atlassian.streams.jira.builder;

import com.atlassian.jira.issue.Issue;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.common.ImmutableNonEmptyList;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.JiraHelper;
import com.atlassian.streams.jira.renderer.CreatedIssueRendererFactory;
import com.atlassian.streams.spi.StreamsI18nResolver;

import com.google.common.collect.ImmutableList;

import static com.atlassian.streams.api.ActivityVerbs.post;
import static com.atlassian.streams.api.common.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public class CreatedEntryBuilder
{
    private static final String CREATED_CATEGORY = "created";
    private final JiraHelper helper;
    private final CreatedIssueRendererFactory rendererFactory;
    private final StreamsI18nResolver i18nResolver;

    CreatedEntryBuilder(JiraHelper helper, CreatedIssueRendererFactory rendererFactory, StreamsI18nResolver i18nResolver)
    {
        this.helper = checkNotNull(helper, "helper");
        this.rendererFactory = checkNotNull(rendererFactory, "rendererFactory");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
    }

    public Option<StreamsEntry> build(JiraActivityItem item)
    {
        Issue issue = item.getIssue();
        return some(new StreamsEntry(helper.newBuilder(item).
            authors(ImmutableNonEmptyList.of(helper.getUserProfile().apply(issue.getReporterId()))).
            categories(ImmutableList.of(CREATED_CATEGORY)).
            addActivityObject(helper.buildActivityObject(issue, item.getDisplaySummary())).
            verb(post()).
            renderer(rendererFactory.newInstance(item.getIssue(), item.getInitialDescription())), i18nResolver));
    }
}
