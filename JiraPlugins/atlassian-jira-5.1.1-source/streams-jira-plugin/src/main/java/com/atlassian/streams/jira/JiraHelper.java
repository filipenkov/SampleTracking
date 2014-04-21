package com.atlassian.streams.jira;

import java.net.URI;
import java.util.NoSuchElementException;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityVerb;
import com.atlassian.streams.api.Html;
import com.atlassian.streams.api.StreamsEntry;
import com.atlassian.streams.api.StreamsEntry.ActivityObject;
import com.atlassian.streams.api.StreamsEntry.HasAlternateLinkUri;
import com.atlassian.streams.api.StreamsEntry.HasApplicationType;
import com.atlassian.streams.api.StreamsEntry.HasId;
import com.atlassian.streams.api.StreamsEntry.HasPostedDate;
import com.atlassian.streams.api.StreamsEntry.NeedsAuthors;
import com.atlassian.streams.api.StreamsEntry.NeedsRenderer;
import com.atlassian.streams.api.StreamsEntry.NeedsVerb;
import com.atlassian.streams.api.UserProfile;
import com.atlassian.streams.api.common.ImmutableNonEmptyList;
import com.atlassian.streams.api.common.NonEmptyIterable;
import com.atlassian.streams.api.common.NonEmptyIterables;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.api.common.Options;
import com.atlassian.streams.api.common.Pair;
import com.atlassian.streams.jira.builder.ActivityObjectBuilder;
import com.atlassian.streams.jira.builder.JiraEntryBuilderFactory;
import com.atlassian.streams.spi.UserProfileAccessor;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.ofbiz.core.entity.GenericValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.jira.issue.IssueFieldConstants.ATTACHMENT;
import static com.atlassian.jira.issue.IssueFieldConstants.CLOSED_STATUS_ID;
import static com.atlassian.jira.issue.IssueFieldConstants.INPROGRESS_STATUS_ID;
import static com.atlassian.jira.issue.IssueFieldConstants.OPEN_STATUS_ID;
import static com.atlassian.jira.issue.IssueFieldConstants.REOPENED_STATUS_ID;
import static com.atlassian.jira.issue.IssueFieldConstants.RESOLVED_STATUS_ID;
import static com.atlassian.streams.api.ActivityObjectTypes.file;
import static com.atlassian.streams.api.ActivityVerbs.post;
import static com.atlassian.streams.api.ActivityVerbs.update;
import static com.atlassian.streams.api.common.Functions.parseLong;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.option;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Options.catOptions;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.ChangeItems.getChangeItems;
import static com.atlassian.streams.jira.ChangeItems.isAttachment;
import static com.atlassian.streams.jira.ChangeItems.isDeletedComment;
import static com.atlassian.streams.jira.ChangeItems.isRemoteLinkUpdate;
import static com.atlassian.streams.jira.ChangeItems.isStatusUpdate;
import static com.atlassian.streams.jira.ChangeItems.updatedFieldCount;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static com.atlassian.streams.jira.JiraActivityVerbs.close;
import static com.atlassian.streams.jira.JiraActivityVerbs.open;
import static com.atlassian.streams.jira.JiraActivityVerbs.remoteLink;
import static com.atlassian.streams.jira.JiraActivityVerbs.reopen;
import static com.atlassian.streams.jira.JiraActivityVerbs.resolve;
import static com.atlassian.streams.jira.JiraActivityVerbs.start;
import static com.atlassian.streams.jira.JiraActivityVerbs.stop;
import static com.atlassian.streams.jira.JiraActivityVerbs.transition;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.math.NumberUtils.createLong;
import static org.apache.commons.lang.math.NumberUtils.toInt;

public class JiraHelper
{
    private static final Logger log = LoggerFactory.getLogger(JiraHelper.class);

    private final UriProvider uriProvider;
    private final JiraEntryBuilderFactory entryBuilderFactory;
    private final ActivityObjectBuilder activityObjectBuilder;
    private final UserProfileAccessor userProfileAccessor;
    private final AttachmentManager attachmentManager;
    private final RemoteIssueLinkManager remoteIssueLinkManager;
    private final RendererManager rendererManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final ConstantsManager constantsManager;

    JiraHelper(JiraEntryBuilderFactory entryBuilderFactory,
            UriProvider uriProvider,
            ActivityObjectBuilder activityObjectBuilder,
            UserProfileAccessor userProfileAccessor,
            AttachmentManager attachmentManager,
            RemoteIssueLinkManager remoteIssueLinkManager,
            RendererManager rendererManager,
            FieldLayoutManager fieldLayoutManager,
            ConstantsManager constantsManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.entryBuilderFactory = checkNotNull(entryBuilderFactory, "entryBuilderFactory");
        this.uriProvider = checkNotNull(uriProvider, "uriProvider");
        this.activityObjectBuilder = checkNotNull(activityObjectBuilder, "activityObjectBuilder");
        this.userProfileAccessor = checkNotNull(userProfileAccessor, "userProfileAccessor");
        this.attachmentManager = checkNotNull(attachmentManager, "attachmentManager");
        this.remoteIssueLinkManager = checkNotNull(remoteIssueLinkManager, "remoteIssueLinkManager");
        this.rendererManager = checkNotNull(rendererManager, "rendererManager");
        this.constantsManager = checkNotNull(constantsManager, "constantsManager");
    }

    public StreamsEntry.Parameters<HasId, HasPostedDate, HasAlternateLinkUri, HasApplicationType, NeedsRenderer, NeedsVerb, NeedsAuthors> newBuilder(JiraActivityItem item)
    {
        return entryBuilderFactory.newParams(item, getIssueUri(item));
    }

    public StreamsEntry.Parameters<HasId, HasPostedDate, HasAlternateLinkUri, HasApplicationType, NeedsRenderer, NeedsVerb, NeedsAuthors> newCommentBuilder(JiraActivityItem item)
    {
        return entryBuilderFactory.newParams(item, uriProvider.getIssueCommentUri(item.getComment().get()));
    }

    public StreamsEntry.Parameters<HasId, HasPostedDate, HasAlternateLinkUri, HasApplicationType, NeedsRenderer, NeedsVerb, NeedsAuthors> newLinkedIssueBuilder(JiraActivityItem item)
    {
        return entryBuilderFactory.newLinkedIssueParams(item, getIssueUri(item));
    }

    public URI getIssueUri(JiraActivityItem activityItem)
    {
        return getIssueUri(activityItem.getIssue());
    }

    URI getIssueUri(Issue issue)
    {
        return getIssueUri(issue.getKey());
    }

    URI getIssueUri(String issueKey)
    {
        return uriProvider.getIssueUri(issueKey);
    }

    public ActivityObject buildActivityObject(Comment comment)
    {
        return activityObjectBuilder.build(comment);
    }

    public ActivityObject buildActivityObject(Issue issue, String issueSummary)
    {
        return activityObjectBuilder.build(issue, issueSummary);
    }

    public Iterable<ActivityObject> buildActivityObjects(Iterable<Attachment> attachments)
    {
        return activityObjectBuilder.build(attachments);
    }

    public Function<String, UserProfile> getUserProfile()
    {
        return getUserProfile;
    }

    public NonEmptyIterable<UserProfile> getUserProfiles(Iterable<String> usernames)
    {
        return NonEmptyIterables.<UserProfile>from(transform(usernames,getUserProfile())).getOrElse(ImmutableNonEmptyList.of(userProfileAccessor.getAnonymousUserProfile()));
    }

    private final Function<String, UserProfile> getUserProfile = new Function<String, UserProfile>()
    {
        public UserProfile apply(String username)
        {
            return userProfileAccessor.getUserProfile(username);
        }
    };

    public Iterable<Attachment> extractAttachments(final Iterable<GenericValue> changeItems)
    {
        return catOptions(transform(filter(changeItems, isAttachment()), getAttachment()));
    }

    private Function<GenericValue, Option<Attachment>> getAttachment()
    {
        return getAttachment;
    }

    private final Function<GenericValue, Option<Attachment>> getAttachment = new Function<GenericValue, Option<Attachment>>()
    {
        public Option<Attachment> apply(GenericValue v)
        {
            return option(v.getString("newvalue")).map(parseLong()).flatMap(lookupAttachment);
        }
    };

    private final Function<Long, Option<Attachment>> lookupAttachment = new Function<Long, Option<Attachment>>()
    {
        public Option<Attachment> apply(Long id)
        {
            try
            {
                return option(attachmentManager.getAttachment(id));
            }
            catch (DataAccessException e)
            {
                log.error("Error retrieving attachments", e);
                return none();
            }
            catch (AttachmentNotFoundException e)
            {
                return none();
            }
        }
    };

    public Option<RemoteIssueLink> extractRemoteIssueLink(final Iterable<GenericValue> changeItems)
    {
        return Options.find(transform(filter(changeItems, isRemoteLinkUpdate()), getRemoteIssueLink()));
    }

    private Function<GenericValue, Option<RemoteIssueLink>> getRemoteIssueLink()
    {
        return getRemoteIssueLink;
    }

    private final Function<GenericValue, Option<RemoteIssueLink>> getRemoteIssueLink = new Function<GenericValue, Option<RemoteIssueLink>>()
    {
        public Option<RemoteIssueLink> apply(GenericValue v)
        {
            return option(v.getString("newvalue")).map(parseLong()).flatMap(lookupRemoteIssueLink);
        }
    };

    private final Function<Long, Option<RemoteIssueLink>> lookupRemoteIssueLink = new Function<Long, Option<RemoteIssueLink>>()
    {
        public Option<RemoteIssueLink> apply(Long id)
        {
            try
            {
                return option(remoteIssueLinkManager.getRemoteIssueLink(id));
            }
            catch (DataAccessException e)
            {
                log.error("Error retrieving remote issue link", e);
                return none();
            }
        }
    };

    public Option<Pair<ActivityObjectType, ActivityVerb>> jiraActivity(final ChangeHistory history)
    {
        try
        {
            GenericValue changeItem = find(getChangeItems(history), or(isStatusUpdate(), isDeletedComment()));
            if (isStatusUpdate(changeItem))
            {
                return some(pair(issue(), getTransitionVerb(changeItem)));
            }
            else
            {
                return none();
            }
        }
        catch (NoSuchElementException e)
        {
            Iterable<GenericValue> changeItems = filter(getChangeItems(history), validAttachment);
            if (isEmpty(changeItems))
            {
                return none();
            }

            if (updatedFieldCount(changeItems) == 1 && isAttachment(get(changeItems, 0)))
            {
                    return some(pair(file(), post()));
            }
            if (updatedFieldCount(changeItems) == 1 && isRemoteLinkUpdate(get(changeItems, 0)))
            {
                return some(pair(issue(), remoteLink()));
            }
            else
            {
                return some(pair(issue(), update()));
            }
        }
    }

    private ActivityVerb getTransitionVerb(GenericValue changeItem)
    {
        final int oldStatusId = toInt(changeItem.getString("oldvalue"), -1);
        final String newStatusIdString = changeItem.getString("newvalue");
        final int newStatusId = toInt(newStatusIdString, -1);

        switch (newStatusId)
        {
            case RESOLVED_STATUS_ID:
                return resolve();
            case CLOSED_STATUS_ID:
                return close();
            case REOPENED_STATUS_ID:
                return reopen();
            case OPEN_STATUS_ID:
                if (oldStatusId == INPROGRESS_STATUS_ID)
                {
                    return stop();
                }
                return open();
            case INPROGRESS_STATUS_ID:
                return start();
            default:
                return transition();
        }
    }

    public Predicate<GenericValue> validAttachment()
    {
        return validAttachment;
    }

    private final Predicate<GenericValue> validAttachment = new Predicate<GenericValue>()
    {
        public boolean apply(GenericValue item)
        {
            if (ATTACHMENT.equalsIgnoreCase(item.getString("field")))
            {
                try
                {
                    return attachmentManager.getAttachment(createLong(item.getString("newvalue"))) != null;
                }
                catch (final Exception e)
                {
                    return false;
                }
            }

            return true;
        }
    };

    public Function<Comment, Html> renderComment()
    {
        return renderComment;
    }
    private final Function<Comment, Html> renderComment = new Function<Comment, Html>()
    {
        public Html apply(Comment comment)
        {
            return renderIssueFieldValue(comment.getIssue(), "comment", comment.getBody());
        }
    };

    public Function<String, Html> renderCommentString(final Issue issue)
    {
        return new Function<String, Html>()
        {
            public Html apply(String comment)
            {
                return renderIssueFieldValue(issue, "comment", comment);
            }
        };
    }

    public Function<Issue, Html> renderDescription()
    {
        return renderDescription;
    }
    private final Function<Issue, Html> renderDescription = new Function<Issue, Html>()
    {
        public Html apply(Issue issue)
        {
            return renderIssueFieldValue(issue, "description", issue.getDescription());
        }
    };

    public Html renderIssueFieldValue(Issue issue, String fieldId, String value)
    {
        FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(fieldId);
        if (fieldLayoutItem == null)
        {
            return new Html(value);
        }
        return new Html(rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), value, issue.getIssueRenderContext()));
    }

    /**
     * Returns the translated version of the "newstring" value.
     *
     * @param changeItem the change item
     * @return the translated version of the "newstring" value.
     */
    public Option<String> getNewChangeItemNameTranslation(GenericValue changeItem)
    {
        if (changeItem == null)
        {
            return none(String.class);
        }

        String field = changeItem.getString("field");
        String newValue = changeItem.getString("newvalue");
        String newString = changeItem.getString("newstring");

        return getChangeItemNameTranslation(field, newValue, newString);
    }

    /**
     * Returns the translated version of the "oldstring" value.
     *
     * @param changeItem the change item
     * @return the translated version of the "oldstring" value.
     */
    public Option<String> getOldChangeItemNameTranslation(GenericValue changeItem)
    {
        if (changeItem == null)
        {
            return none(String.class);
        }

        String field = changeItem.getString("field");
        String oldValue = changeItem.getString("oldvalue");
        String oldString = changeItem.getString("oldstring");

        return getChangeItemNameTranslation(field, oldValue, oldString);
    }

    private Option<String> getChangeItemNameTranslation(String field, String value, String str)
    {
        //not all change items have set the "newvalue"/"oldvalue" values.
        if (isNotBlank(value) && isNotBlank(field))
        {
            IssueConstant issueConstant = constantsManager.getConstantObject(field, value);
            //might be null if status/resolution/etc was deleted after this changeItem was persisted
            if (issueConstant != null)
            {
                return some(issueConstant.getNameTranslation());
            }
            else if (str != null)
            {
                return some(str);
            }
            else
            {
                return none(String.class);
            }
        }
        else if (isNotBlank(str))
        {
            return some(str);
        }
        else
        {
            return none(String.class);
        }
    }
}
