package com.atlassian.streams.jira.renderer;

import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.StreamsEntry.Renderer;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.ImmutableNonEmptyList;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.renderer.StreamsEntryRendererFactory;
import com.atlassian.streams.jira.AggregatedJiraActivityItem;
import com.atlassian.streams.jira.JiraActivityItem;
import com.atlassian.streams.jira.JiraHelper;
import com.atlassian.streams.jira.UriProvider;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.AGGREGATE_TIME_ESTIMATE;
import static com.atlassian.jira.issue.IssueFieldConstants.AGGREGATE_TIME_ORIGINAL_ESTIMATE;
import static com.atlassian.jira.issue.IssueFieldConstants.AGGREGATE_TIME_SPENT;
import static com.atlassian.jira.issue.IssueFieldConstants.ASSIGNEE;
import static com.atlassian.jira.issue.IssueFieldConstants.DESCRIPTION;
import static com.atlassian.jira.issue.IssueFieldConstants.DUE_DATE;
import static com.atlassian.jira.issue.IssueFieldConstants.ENVIRONMENT;
import static com.atlassian.jira.issue.IssueFieldConstants.REPORTER;
import static com.atlassian.jira.issue.IssueFieldConstants.SUMMARY;
import static com.atlassian.jira.issue.IssueFieldConstants.TIME_ESTIMATE;
import static com.atlassian.jira.issue.IssueFieldConstants.TIME_ORIGINAL_ESTIMATE;
import static com.atlassian.jira.issue.IssueFieldConstants.TIME_SPENT;
import static com.atlassian.streams.api.Html.trimHtmlToNone;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.option;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Options.catOptions;
import static com.atlassian.streams.jira.ChangeItems.getFirstChangeItem;
import static com.atlassian.streams.jira.ChangeItems.getWorklogId;
import static com.atlassian.streams.jira.ChangeItems.isAttachment;
import static com.atlassian.streams.jira.util.RenderingUtilities.link;
import static com.atlassian.streams.spi.renderer.Renderers.render;
import static com.atlassian.streams.spi.renderer.Renderers.truncate;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.transform;
import static java.lang.Math.min;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringUtils.isBlank;

public class IssueUpdateRendererFactory
{
    private static final String AFFECTS_VERSION = "version";
    private static final String COMPONENT = "component";
    private static final String FIX_VERSION = "fixversion";
    private static final Iterable<String> FIELDS_THAT_EXCLUDE_VALUE = ImmutableSet.of(DESCRIPTION, ENVIRONMENT);
    private static final Iterable<String> DURATION_FIELDS = ImmutableSet.of(
            TIME_SPENT,
            TIME_ESTIMATE,
            TIME_ORIGINAL_ESTIMATE,
            AGGREGATE_TIME_SPENT,
            AGGREGATE_TIME_ESTIMATE,
            AGGREGATE_TIME_ORIGINAL_ESTIMATE);
    private static final Iterable<String> WORKLOG_FIELDS = ImmutableSet.of(TIME_SPENT, AGGREGATE_TIME_SPENT);
    private static final Iterable<String> MULTI_VALUE_FIELDS = ImmutableSet.of(AFFECTS_VERSION, COMPONENT, FIX_VERSION);

    private final JiraHelper helper;
    private final AttachmentRendererFactory attachmentRendererFactory;
    private final I18nResolver i18nResolver;
    private final JiraAuthenticationContext authenticationContext;
    private final OutlookDateManager outlookDateManager;
    private final JiraDurationUtils jiraDurationUtils;
    private final ProjectComponentManager projectComponentManager;
    private final VersionService versionService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WorklogManager worklogManager;
    private final UriProvider uriProvider;
    private final StreamsEntryRendererFactory rendererFactory;
    private final IssueActivityObjectRendererFactory issueActivityObjectRendererFactory;
    private final TemplateRenderer templateRenderer;
    private final Function<Comment, Html> commentRenderer;

    IssueUpdateRendererFactory(JiraHelper helper,
            AttachmentRendererFactory attachmentRendererFactory,
            StreamsEntryRendererFactory rendererFactory,
            IssueActivityObjectRendererFactory issueActivityObjectRendererFactory,
            I18nResolver i18nResolver,
            JiraAuthenticationContext authenticationContext,
            OutlookDateManager outlookDateManager,
            ProjectComponentManager projectComponentManager,
            VersionService versionService,
            JiraAuthenticationContext jiraAuthenticationContext,
            WorklogManager worklogManager,
            UriProvider uriProvider,
            TemplateRenderer templateRenderer)
    {
        this.helper = checkNotNull(helper, "helper");
        this.attachmentRendererFactory = checkNotNull(attachmentRendererFactory, "attachmentRendererFactory");
        this.rendererFactory = checkNotNull(rendererFactory, "rendererFactory");
        this.issueActivityObjectRendererFactory = checkNotNull(issueActivityObjectRendererFactory, "issueActivityObjectRendererFactory");
        this.i18nResolver = checkNotNull(i18nResolver, "i18nResolver");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.outlookDateManager = checkNotNull(outlookDateManager, "outlookDateManager");
        this.jiraDurationUtils = ComponentManager.getComponentInstanceOfType(JiraDurationUtils.class);
        this.projectComponentManager = checkNotNull(projectComponentManager, "projectComponentManager");
        this.versionService = checkNotNull(versionService, "versionService");
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext, "jiraAuthenticationContext");
        this.worklogManager = checkNotNull(worklogManager, "worklogManager");
        this.uriProvider = checkNotNull(uriProvider, "uriProvider");
        this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
        this.commentRenderer = helper.renderComment();
    }

    public Renderer newRenderer(JiraActivityItem item, Iterable<GenericValue> changeItems)
    {
        if (size(changeItems) == 1)
        {
            GenericValue changeItem = getOnlyElement(changeItems);
            if (DESCRIPTION.equals(field(changeItem)))
            {
                return newDescriptionChangeEntryRenderer(item, changeItem);
            }
            return newSingleFieldChangedEntryRenderer(item, changeItem);
        }
        else if (uniqueFieldSize(changeItems) == 1)
        {
            // STRM-1196 - Better display for JIRA Multi-value fields
            return newSingleMultiValueFieldChangedEntryRenderer(item, changeItems);
        }
        else
        {
            return newMultipleFieldsChangedEntryRenderer(item, changeItems);
        }
    }

    public Renderer newIssueLinkEntryRenderer(AggregatedJiraActivityItem aggregatedItem)
    {
        return new IssueLinkEntryRenderer(aggregatedItem);
    }

    public Renderer newRemoteIssueLinkEntryRenderer(JiraActivityItem item, RemoteIssueLink remoteIssueLink)
    {
        return new RemoteIssueLinkEntryRenderer(item, remoteIssueLink);
    }

    private Renderer newMultipleFieldsChangedEntryRenderer(JiraActivityItem item, Iterable<GenericValue> changeItems)
    {
        return new MultipleFieldsChangedEntryRenderer(item, changeItems);
    }

    private final class MultipleFieldsChangedEntryRenderer extends MultipleValueChangedEntryRenderer
    {
        public MultipleFieldsChangedEntryRenderer(JiraActivityItem item, Iterable<GenericValue> changeItems)
        {
            super(item, changeItems);
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {
            return new Html(i18nResolver.getText("streams.item.jira.title.updated.multiple.fields",
                    authorsRenderer.apply(entry.getAuthors()),
                    min(attachments.size(), 1) + uniqueFieldSize(fieldChanges),
                    activityObjectsRenderer.apply(entry.getActivityObjects()).get()));
        }
    }

    private Renderer newSingleMultiValueFieldChangedEntryRenderer(JiraActivityItem item, Iterable<GenericValue> changeItems)
    {
        return new SingleMultiValueFieldChangedEntryRenderer(item, changeItems);
    }

    private final class SingleMultiValueFieldChangedEntryRenderer extends MultipleValueChangedEntryRenderer
    {
        public SingleMultiValueFieldChangedEntryRenderer(JiraActivityItem item, Iterable<GenericValue> changeItems)
        {
            super(item, changeItems);
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {
            return new Html(i18nResolver.getText("streams.item.jira.title.updated.field.exclude.value",
                    authorsRenderer.apply(entry.getAuthors()),
                    getFieldName(get(fieldChanges, 0)),
                    min(attachments.size(), 1) + uniqueFieldSize(fieldChanges),
                    activityObjectsRenderer.apply(entry.getActivityObjects()).get()));
        }
    }

    private abstract class MultipleValueChangedEntryRenderer implements Renderer
    {
        final Function<Iterable<UserProfile>, Html> authorsRenderer = rendererFactory.newAuthorsRenderer();
        final Function<Iterable<ActivityObject>, Option<Html>> activityObjectsRenderer;
        final Function<Iterable<Attachment>, Html> attachmentsRenderer;

        final JiraActivityItem item;
        final List<Attachment> attachments;
        final List<GenericValue> fieldChanges;

        public MultipleValueChangedEntryRenderer(JiraActivityItem item, Iterable<GenericValue> changeItems)
        {
            this.item = item;

            this.attachmentsRenderer = attachmentRendererFactory.newAttachmentsRendererWithoutComment(item);
            this.activityObjectsRenderer = issueActivityObjectRendererFactory.newIssueActivityObjectsRenderer(item.getIssue());
            this.attachments = ImmutableList.copyOf(helper.extractAttachments(changeItems));
            this.fieldChanges = ImmutableList.copyOf(filter(changeItems, not(isAttachment())));
        }

        public Option<Html> renderContentAsHtml(StreamsEntry entry)
        {
            return some(new Html(render(templateRenderer, "updated-field-list.vm", ImmutableMap.of(
                    "comment", item.getComment().map(commentRenderer).flatMap(trimHtmlToNone()),
                    "fieldChanges", transform(fieldChanges, renderFieldChanges()),
                    "attachments", attachments,
                    "attachmentsRenderer", attachmentsRenderer))));
        }

        private final Function<GenericValue, Html> fieldChangeRenderer = new Function<GenericValue, Html>()
        {
            public Html apply(GenericValue changeItem)
            {
                for (Html newValue : newValue(changeItem))
                {
                    return renderFieldChange(changeItem, getI18nKey(changeItem, true), newValue, changeItem.getString("newvalue"));
                }
                return renderFieldChange(changeItem, getRemovedI18nKey(changeItem, true), oldValue(changeItem), changeItem.getString("oldvalue"));
            }
        };

        private Html renderFieldChange(GenericValue changeItem, String i18nKey, Html displayValue, String id)
        {
            return new Html(render(templateRenderer, "jira-field-change-item.vm", ImmutableMap.of(
                "i18nKey", i18nKey,
                "fieldName", getFieldName(changeItem),
                "fieldValue", displayValue,
                "fieldUri", fieldUri(item, field(changeItem), id)
            )));
        }

        private Function<GenericValue, Html> renderFieldChanges()
        {
            return fieldChangeRenderer;
        }

        public Option<Html> renderSummaryAsHtml(StreamsEntry entry)
        {
            return none();
        }
    }

    private Renderer newSingleFieldChangedEntryRenderer(JiraActivityItem item, GenericValue changeItem)
    {
        Function<ActivityObject, Option<Html>> activityObjectRenderer = updatedSummary(changeItem) ?
                issueActivityObjectRendererFactory.newIssueActivityObjectRendererWithoutSummary(item.getIssue()) :
                issueActivityObjectRendererFactory.newIssueActivityObjectRendererWithSummary(item.getIssue());
        Function<StreamsEntry, Html> titleRenderer = new SingleFieldChangeTitleRenderer(item, changeItem, activityObjectRenderer);

        if (worklogField(changeItem))
        {
            return rendererFactory.newCommentRenderer(titleRenderer,
                    worklogComment(item).map(helper.renderCommentString(item.getIssue())).flatMap(trimHtmlToNone()).getOrElse(new Html("")));
        }
        return rendererFactory.newCommentRenderer(titleRenderer,
                item.getComment().map(commentRenderer).flatMap(trimHtmlToNone()).getOrElse(new Html("")));
    }

    private final class SingleFieldChangeTitleRenderer  implements Function<StreamsEntry, Html>
    {
        private final Function<Iterable<UserProfile>, Html> authorsRenderer = rendererFactory.newAuthorsRenderer();
        private final Function<Iterable<ActivityObject>, Option<Html>> activityObjectsRenderer;
        private final GenericValue changeItem;
        private final JiraActivityItem item;

        public SingleFieldChangeTitleRenderer(JiraActivityItem item, GenericValue changeItem, Function<ActivityObject,
                Option<Html>> activityObjectRenderer)
        {
            this.item = item;
            this.changeItem = changeItem;
            this.activityObjectsRenderer = rendererFactory.newActivityObjectsRenderer(activityObjectRenderer);
        }

        public Html apply(StreamsEntry entry)
        {
            for (Html newValue : newValue(changeItem))
            {
                return new Html(i18nResolver.getText(getI18nKey(changeItem, false),
                        authorsRenderer.apply(entry.getAuthors()),
                        getFieldName(changeItem),
                        link(fieldUri(item, field(changeItem), changeItem.getString("newvalue")), newValue),
                        activityObjectsRenderer.apply(entry.getActivityObjects()).get()));
            }
            return new Html(i18nResolver.getText(getRemovedI18nKey(changeItem, false),
                    authorsRenderer.apply(entry.getAuthors()),
                    getFieldName(changeItem),
                    activityObjectsRenderer.apply(entry.getActivityObjects()).get(),
                    link(fieldUri(item, field(changeItem), changeItem.getString("oldvalue")), oldValue(changeItem))));
        }
    }

    private Renderer newDescriptionChangeEntryRenderer(JiraActivityItem item, GenericValue changeItem)
    {
        return new DescriptionChangeEntryRenderer(item, changeItem);
    }

    private final class DescriptionChangeEntryRenderer implements Renderer
    {
        private final JiraActivityItem item;
        private final GenericValue changeItem;
        private final Function<StreamsEntry, Html> titleRenderer;
        private final Function<Boolean, Option<Html>> renderDescription;

        public DescriptionChangeEntryRenderer(JiraActivityItem item, GenericValue changeItem)
        {
            this.item = item;
            this.changeItem = changeItem;
            titleRenderer = new SingleFieldChangeTitleRenderer(item, changeItem,
                    issueActivityObjectRendererFactory.newIssueActivityObjectRendererWithSummary(item.getIssue()));
            this.renderDescription = renderDescription(item.getIssue());
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {
            return titleRenderer.apply(entry);
        }

        public Option<Html> renderSummaryAsHtml(StreamsEntry entry)
        {
            return renderDescription.apply(true);
        }

        public Option<Html> renderContentAsHtml(StreamsEntry entry)
        {
            return renderDescription.apply(false);
        }

        private Function<Boolean, Option<Html>> renderDescription(final Issue issue)
        {
            final Option<Html> description = getDescription(issue);

            return new Function<Boolean, Option<Html>>()
            {
                public Option<Html> apply(Boolean truncate)
                {
                    return description.flatMap(renderContent(truncate));
                }

                private Function<Html, Option<Html>> renderContent(final boolean truncate)
                {
                    return new Function<Html, Option<Html>>()
                    {
                        public Option<Html> apply(Html d)
                        {
                            Html description = truncate ? truncate(SUMMARY_LIMIT, d) : d;
                            if (truncate && d.equals(description))
                            {
                                return none(); // we don't want a summary if it will be the same as the content
                            }
                            Map<String, Object> context = ImmutableMap.<String, Object>builder().
                                put("contentHtml", description).
                                put("truncated", truncate).
                                put("contentUri", uriProvider.getIssueUri(issue.getKey())).
                                put("comment", item.getComment().map(commentRenderer).flatMap(trimHtmlToNone())).
                                build();

                            return some(new Html(render(templateRenderer, "jira-description-field-update.vm", context)));
                        }
                    };
                }
            };
        }

        private Option<Html> getDescription(Issue issue)
        {
            String newValueString = changeItem.getString("newstring");
            if (!isBlank(newValueString))
            {
                return option(helper.renderIssueFieldValue(issue, DESCRIPTION, newValueString)).flatMap(trimHtmlToNone());
            }
            return none();
        }
    }

    private final class IssueLinkEntryRenderer implements Renderer
    {
        private final Function<Iterable<UserProfile>, Html> authorsRenderer = rendererFactory.newAuthorsRenderer();
        private final Function<JiraActivityItem, Option<Html>> issueRenderer = new IssueRenderer(templateRenderer, true);
        private final Function<Iterable<JiraActivityItem>, Html> issuesRenderer = new IssuesRenderer(issueRenderer);
        private final AggregatedJiraActivityItem aggregatedItem;
        private final boolean removed;

        public IssueLinkEntryRenderer(AggregatedJiraActivityItem aggregatedItem)
        {
            this.aggregatedItem = aggregatedItem;
            removed = removed(aggregatedItem.getActivityItem());
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {
            if (removed)
            {
                return new Html(i18nResolver.getText("streams.item.jira.title.remove.linked",
                        authorsRenderer.apply(entry.getAuthors()),
                        some(aggregatedItem.getActivityItem()).flatMap(issueRenderer).get(),
                        issuesRenderer.apply(aggregatedItem.getRelatedActivityItems().getOrElse(
                            ImmutableList.<JiraActivityItem>of()))));
            }
            return new Html(i18nResolver.getText("streams.item.jira.title.linked",
                    authorsRenderer.apply(entry.getAuthors()),
                    size(aggregatedItem.getRelatedActivityItems().getOrElse(ImmutableList.<JiraActivityItem>of())) + 1));
        }

        public Option<Html> renderSummaryAsHtml(StreamsEntry entry)
        {
            return none();
        }

        public Option<Html> renderContentAsHtml(StreamsEntry entry)
        {
            if (!removed)
            {
                return some(new Html(render(templateRenderer, "jira-link-field-update.vm", ImmutableMap.of(
                    "comment", getAggregatedComments(aggregatedItem),
                    "issueKeyHtml", some(aggregatedItem.getActivityItem()).flatMap(issueRenderer).get(),
                    "linkText", linkingText(aggregatedItem.getActivityItem()),
                    "linkedIssueKeysHtml", issuesRenderer.apply(aggregatedItem.getRelatedActivityItems()
                            .getOrElse(ImmutableList.<JiraActivityItem>of()))))));
            }
            return none();
        }

        private Iterable<Html> getAggregatedComments(AggregatedJiraActivityItem aggregatedItem)
        {
            Iterable<JiraActivityItem> items = ImmutableList.<JiraActivityItem>builder()
                    .add(aggregatedItem.getActivityItem())
                    .addAll(aggregatedItem.getRelatedActivityItems().getOrElse(ImmutableList.<JiraActivityItem>of()))
                    .build();

            return catOptions(transform(items, toCommentHtml));
        }

        private boolean removed(JiraActivityItem item)
        {
            return !newValue(getFirstChangeItem(item)).isDefined();
        }

        private final Function<JiraActivityItem, Option<Html>> toCommentHtml = new Function<JiraActivityItem, Option<Html>>()
        {
            public Option<Html> apply(JiraActivityItem activityItem)
            {
                return activityItem.getComment().map(commentRenderer).flatMap(trimHtmlToNone());
            }
        };
    }

    private final class RemoteIssueLinkEntryRenderer implements Renderer
    {
        private final Function<Iterable<UserProfile>, Html> authorsRenderer = rendererFactory.newAuthorsRenderer();
        private final Function<JiraActivityItem, Option<Html>> issueRenderer = new IssueRenderer(templateRenderer, true);
        private final JiraActivityItem item;
        private final RemoteIssueLink remoteIssueLink;
        private final boolean updated;

        public RemoteIssueLinkEntryRenderer(JiraActivityItem item, RemoteIssueLink remoteIssueLink)
        {
            this.item = item;
            this.remoteIssueLink = remoteIssueLink;
            updated = updated(item);
        }

        public Html renderTitleAsHtml(StreamsEntry entry)
        {

            Html remoteIssueLinkHtml = new Html(render(templateRenderer, "jira-remote-issue-link.vm",
                    ImmutableMap.of("remoteIssueLink", remoteIssueLink,
                            "withSummary", false)));

            if (updated)
            {
                return new Html(i18nResolver.getText("streams.item.jira.title.update.linked.remote",
                        authorsRenderer.apply(entry.getAuthors()),
                        issueRenderer.apply(item).get(),
                        remoteIssueLinkHtml,
                        getApplicationName()));
            }

            return new Html(i18nResolver.getText("streams.item.jira.title.linked.remote",
                    authorsRenderer.apply(entry.getAuthors()),
                    issueRenderer.apply(item).get(),
                    remoteIssueLinkHtml,
                    getApplicationName()));
        }

        public Option<Html> renderSummaryAsHtml(StreamsEntry entry)
        {
            return none();
        }

        public Option<Html> renderContentAsHtml(StreamsEntry entry)
        {
            return some(new Html(render(templateRenderer, "jira-remote-issue-comment-block.vm", ImmutableMap.of(
                    "comment", item.getComment().map(helper.renderComment()).flatMap(trimHtmlToNone())))));
        }

        private boolean updated(JiraActivityItem item)
        {
            return helper.getOldChangeItemNameTranslation(getFirstChangeItem(item)).isDefined();
        }

        private String getApplicationName()
        {
            return option(remoteIssueLink.getApplicationName()).getOrElse(
                    i18nResolver.getText("remotelink.manager.changeitem.applicationname.default"));
        }
    }

    private final class IssuesRenderer implements Function<Iterable<JiraActivityItem>, Html>
    {
        private final Function<Iterable<JiraActivityItem>, Option<Html>> compoundRenderer;

        public IssuesRenderer(final Function<JiraActivityItem, Option<Html>> issueRenderer)
        {
            compoundRenderer = rendererFactory.newCompoundStatementRenderer(issueRenderer);
        }

        @HtmlSafe
        public Html apply(final Iterable<JiraActivityItem> activityItems)
        {
            return compoundRenderer.apply(activityItems).get();
        }
    }

    private final class IssueRenderer implements Function<JiraActivityItem, Option<Html>>
    {
        private final TemplateRenderer templateRenderer;
        private final boolean withSummary;

        private IssueRenderer(final TemplateRenderer templateRenderer, boolean withSummary)
        {
            this.templateRenderer = checkNotNull(templateRenderer, "templateRenderer");
            this.withSummary = withSummary;
        }

        public Option<Html> apply(final JiraActivityItem activityItem)
        {
            return some(new Html(render(templateRenderer, "jira-issue-link.vm",
                    ImmutableMap.of("issue", activityItem.getIssue(),
                                    "uriProvider", uriProvider,
                                    "issueSummary", activityItem.getDisplaySummary(),
                                    "withSummary", withSummary))));
        }
    }

    private String getI18nKey(GenericValue changeItem, boolean fromList)
    {
        if (multiValuedField(changeItem))
        {
            return fromList ? "streams.item.jira.added.list.single" : "streams.item.jira.title.added.field";
        }
        else if (worklogField(changeItem))
        {
            // JRA-26731 - proper logging of move/split/delete of worklog value.
            // This chooses a different translation message under the same
            // conditions that cause newValue to return a translation of the
            // negative of the time delta.
            if (contains(DURATION_FIELDS, field(changeItem))) {
                final String oldValue = changeItem.getString("oldstring");
                if (!isBlank(oldValue))
                {
                    final String newValue = changeItem.getString("newstring");
                    final long delta = Long.parseLong(newValue) - Long.parseLong(oldValue);
                    if (delta < 0L)
                    {
                        return fromList ? "streams.item.jira.updated.list.single.worklog.reduced" :
                               "streams.item.jira.title.updated.field.worklog.reduced";
                    }
                }
            }
            return fromList ? "streams.item.jira.updated.list.single.worklog" :
                    "streams.item.jira.title.updated.field.worklog";
        }
        else if (excludeValue(changeItem))
        {
            return fromList ? "streams.item.jira.updated.list.single.exclude.value" :
                    "streams.item.jira.title.updated.field.exclude.value";
        }
        else if (updatedSummary(changeItem) && !fromList)
        {
            return "streams.item.jira.title.updated.field.summary";
        }

        return fromList ? "streams.item.jira.updated.list.single" : "streams.item.jira.title.updated.field";
    }

    private String getRemovedI18nKey(GenericValue changeItem, boolean fromList)
    {
        if (multiValuedField(changeItem))
        {
            return fromList ? "streams.item.jira.removed.list" : "streams.item.jira.title.removed.field";
        }

        return fromList ? "streams.item.jira.removed.list.exclude.value" : "streams.item.jira.title.removed.field.exclude.value";
    }

    private String linkingText(JiraActivityItem item)
    {
        GenericValue changeItem = getFirstChangeItem(item);

        String linkString = changeItem.getString("newstring");
        if (!isBlank(linkString) && linkString.startsWith("This issue "))
        {
            return linkString.substring("This issue ".length(), linkString.lastIndexOf(" "));
        }
        return linkString;
    }

    private Option<String> worklogComment(JiraActivityItem item)
    {
        for (Long worklogId : getWorklogId(item))
        {
            Worklog worklog = worklogManager.getById(worklogId);
            if (worklog != null)
            {
                String comment = worklog.getComment();
                if (!isBlank(comment))
                {
                    return some(comment);
                }
            }
        }

        return none();
    }

    private boolean worklogField(GenericValue changeItem)
    {
        return contains(WORKLOG_FIELDS, field(changeItem));
    }

    private boolean multiValuedField(GenericValue changeItem)
    {
        return contains(MULTI_VALUE_FIELDS, field(changeItem));
    }

    private boolean updatedSummary(GenericValue changeItem)
    {
        return SUMMARY.equalsIgnoreCase(field(changeItem));
    }

    private Html getFieldName(GenericValue changeItem)
    {
        final String field = field(changeItem);

        // Logic adapted from JIRA's changehistory.vm
        if (isCustomType(changeItem))
        {
            return new Html(escapeHtml(changeItem.getString("field")));
        }
        else if (multiValuedField(changeItem))
        {
            return new Html(escapeHtml(i18nResolver.getText("streams.issue.field." + field)));
        }
        else
        {
            return new Html(escapeHtml(i18nResolver.getText("issue.field." + field)));
        }
    }

    private Option<URI> fieldUri(JiraActivityItem item, String field, String id)
    {
        if (isBlank(id) || isBlank(field))
        {
            return none();
        }

        if (COMPONENT.equalsIgnoreCase(field))
        {
            try
            {
                return some(
                    uriProvider.getComponentUri(item.getIssue(), projectComponentManager.find(Long.valueOf(id))));
            }
            catch (Exception e)
            {
                return none();
            }
        }
        else if (AFFECTS_VERSION.equalsIgnoreCase(field) || FIX_VERSION.equalsIgnoreCase(field))
        {
            Version version = versionService.getVersionById(jiraAuthenticationContext.getLoggedInUser(), Long.valueOf(id)).getVersion();
            if (version == null)
            {
                return none();
            }
            return some(uriProvider.getFixForVersionUri(version));
        }

        return none();
    }

    private String field(GenericValue changeItem)
    {
        return changeItem.getString("field").replaceAll(" ", "").toLowerCase();
    }

    private boolean excludeValue(GenericValue changeItem)
    {
        return contains(FIELDS_THAT_EXCLUDE_VALUE, field(changeItem)) || isCustomType(changeItem);
    }

    private boolean isCustomType(GenericValue changeItem)
    {
        final String fieldType = changeItem.getString("fieldtype");
        return fieldType != null && fieldType.toLowerCase().equals("custom");
    }

    private int uniqueFieldSize(Iterable<GenericValue> changeItems)
    {
        return size(ImmutableSet.copyOf(transform(changeItems, toFieldName)));
    }

    private Function<GenericValue, String> toFieldName = new Function<GenericValue, String>()
    {
        public String apply(GenericValue changeItem)
        {
            return field(changeItem);
        }
    };

    private Option<Html> newValue(GenericValue changeItem)
    {
        final String field = field(changeItem);

        for (String newValue : helper.getNewChangeItemNameTranslation(changeItem))
        {
            // STRM-285 - add link to new assignee's profile url
            if (ASSIGNEE.equalsIgnoreCase(field) || REPORTER.equalsIgnoreCase(field))
            {
                return some(rendererFactory.newUserProfileRenderer().apply(ImmutableNonEmptyList.of(helper.getUserProfile().apply(changeItem.getString("newvalue")))));
            }
            // STRM-1002 - Format display of 'Due Date' in streams entry
            else if (DUE_DATE.equalsIgnoreCase(field))
            {
                OutlookDate outlookDate = outlookDateManager.getOutlookDate(authenticationContext.getLocale());
                try
                {
                    return some(new Html(outlookDate.formatDatePicker(Timestamp.valueOf(newValue))));
                }
                catch (IllegalArgumentException e)
                {
                    return none();
                }
            }
            else if (contains(DURATION_FIELDS, field))
            {
                final String oldValue = changeItem.getString("oldstring");
                if (worklogField(changeItem) && !isBlank(oldValue))
                {
                    // JRA-26731 - proper logging of move/split/delete of worklog value.
                    // Format time delta correctly regardless of whether it increased or
                    // decreased.  If it decreased, then getI18nKey also has to notice
                    // this and return a different translation key.
                    long delta = Long.parseLong(newValue) - Long.parseLong(oldValue);
                    if (delta < 0L)
                    {
                        delta = -delta;
                    }
                    return some(new Html(jiraDurationUtils.getFormattedDuration(delta)));
                }
                return some(new Html(jiraDurationUtils.getFormattedDuration(Long.valueOf(newValue))));
            }

            return some(new Html(escapeHtml(newValue)));
        }

        return none();
    }

    private Html oldValue(GenericValue changeItem)
    {
        for (String oldValue : helper.getOldChangeItemNameTranslation(changeItem))
        {
            return new Html(escapeHtml(oldValue));
        }

        return new Html("");
    }
}
