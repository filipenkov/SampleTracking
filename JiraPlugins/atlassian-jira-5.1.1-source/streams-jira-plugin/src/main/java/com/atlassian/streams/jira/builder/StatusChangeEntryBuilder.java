package com.atlassian.streams.jira.builder;

import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.JiraHelper;
import com.atlassian.streams.jira.renderer.IssueTransitionRendererFactory;
import com.atlassian.streams.spi.StreamsI18nResolver;

import com.google.common.collect.ImmutableList;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.jira.JiraActivityVerbs.close;
import static com.atlassian.streams.jira.JiraActivityVerbs.open;
import static com.atlassian.streams.jira.JiraActivityVerbs.reopen;
import static com.atlassian.streams.jira.JiraActivityVerbs.resolve;
import static com.atlassian.streams.jira.JiraActivityVerbs.start;
import static com.atlassian.streams.jira.JiraActivityVerbs.stop;
import static com.atlassian.streams.jira.JiraActivityVerbs.transition;
import static com.google.common.base.Preconditions.checkNotNull;

class StatusChangeEntryBuilder
{
    private final JiraHelper helper;
    private final IssueTransitionRendererFactory rendererFactory;
    private final StreamsI18nResolver i18nResolver;

    StatusChangeEntryBuilder(JiraHelper helper,
            IssueTransitionRendererFactory rendererFactory,
            StreamsI18nResolver i18nResolver)
    {
        this.helper = checkNotNull(helper, "helper");
        this.rendererFactory = checkNotNull(rendererFactory, "rendererFactory");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
    }

    Option<StreamsEntry> build(JiraActivityItem item, GenericValue change)
    {
        ActivityVerb verb = item.getActivity().second();

        if (resolve().equals(verb))
        {
            return newEntryForStatus(item, "resolved", verb);
        }
        else if (close().equals(verb))
        {
            return newEntryForStatus(item, "closed", verb);
        }
        else if (reopen().equals(verb))
        {
            return newEntryForStatus(item, "reopened", verb);
        }
        else if (open().equals(verb))
        {
            return newEntryForStatus(item, "opened", verb);
        }
        else if (stop().equals(verb))
        {
            return newEntryForStatus(item, "stopped", verb);
        }
        else if (start().equals(verb))
        {
            return newEntryForStatus(item, "started", verb);
        }
        else
        {
            // We don't know about this status transition, use a generic "updated status to" wording
            for (String statusName : helper.getNewChangeItemNameTranslation(change))
            {
                return some(new StreamsEntry(helper.newBuilder(item).
                    authors(helper.getUserProfiles(item.getChangeHistoryAuthors())).
                    categories(ImmutableList.of(statusName)).
                    verb(transition()).
                    addActivityObject(helper.buildActivityObject(item.getIssue(), item.getDisplaySummary())).
                    renderer(rendererFactory.newCustomTransitionRenderer(item, statusName)), i18nResolver));
            }

            return none();
        }
    }

    private Option<StreamsEntry> newEntryForStatus(JiraActivityItem item, String status, ActivityVerb verb)
    {
        Renderer renderer = resolve().equals(verb) ?
            rendererFactory.newResolvedRenderer(item) :
                rendererFactory.newSystemTransitionRenderer(item, verb);

        return some(new StreamsEntry(helper.newBuilder(item).
            authors(helper.getUserProfiles(item.getChangeHistoryAuthors())).
            categories(ImmutableList.of(status)).
            verb(verb).
            addActivityObject(helper.buildActivityObject(item.getIssue(), item.getDisplaySummary())).
            renderer(renderer), i18nResolver));
    }
}
