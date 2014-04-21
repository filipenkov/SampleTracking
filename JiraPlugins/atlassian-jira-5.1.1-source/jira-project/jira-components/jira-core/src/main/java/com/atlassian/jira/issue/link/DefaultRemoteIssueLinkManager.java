package com.atlassian.jira.issue.link;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.link.RemoteIssueLinkCreateEvent;
import com.atlassian.jira.event.issue.link.RemoteIssueLinkDeleteEvent;
import com.atlassian.jira.event.issue.link.RemoteIssueLinkUpdateEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Default implementation of the RemoteIssueLinkManager.
 *
 * @since v5.0
 */
public class DefaultRemoteIssueLinkManager implements RemoteIssueLinkManager
{
    public static final int MAX_LONG_VARCHAR_LENGTH = 255;

    private final RemoteIssueLinkStore remoteIssueLinkStore;
    private final IssueManager issueManager;
    private final IssueUpdater issueUpdater;
    private final I18nBean.BeanFactory i18nBeanFactory;
    private final EventPublisher eventPublisher;

    public DefaultRemoteIssueLinkManager(final RemoteIssueLinkStore remoteIssueLinkStore, final IssueManager issueManager, final IssueUpdater issueUpdater, final I18nBean.BeanFactory i18nBeanFactory, EventPublisher eventPublisher)
    {
        this.remoteIssueLinkStore = remoteIssueLinkStore;
        this.issueManager = issueManager;
        this.issueUpdater = issueUpdater;
        this.i18nBeanFactory = i18nBeanFactory;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RemoteIssueLink getRemoteIssueLink(final Long remoteIssueLinkId)
    {
        return remoteIssueLinkStore.getRemoteIssueLink(remoteIssueLinkId);
    }

    @Override
    public List<RemoteIssueLink> getRemoteIssueLinksForIssue(final Issue issue)
    {
        return remoteIssueLinkStore.getRemoteIssueLinksForIssue(issue);
    }

    @Override
    public RemoteIssueLink getRemoteIssueLinkByGlobalId(final Issue issue, final String globalId)
    {
        return Iterables.get(remoteIssueLinkStore.getRemoteIssueLinksByGlobalId(issue, globalId), 0, null);
    }

    @Override
    public RemoteIssueLink createRemoteIssueLink(final RemoteIssueLink remoteIssueLink, final User user) throws CreateException
    {
        validateMandatoryFieldsForCreate(remoteIssueLink);
        validateFieldLengthsForCreate(remoteIssueLink);
        validateUrlsForCreate(remoteIssueLink);

        final Issue issue = issueManager.getIssueObject(remoteIssueLink.getIssueId());
        if (issue == null)
        {
            throw new CreateException("Issue with id '" + remoteIssueLink.getIssueId() + "' does not exist.");
        }

        validateGlobalIdForCreate(issue, remoteIssueLink.getGlobalId());
        
        final RemoteIssueLink created = remoteIssueLinkStore.createRemoteIssueLink(remoteIssueLink);

        if (hasDuplicateGlobalId(issue, created))
        {
            remoteIssueLinkStore.removeRemoteIssueLink(created.getId());
            throw new CreateException("A remote issue link already exists on this issue with the globalId '" + created.getGlobalId() + "'");
        }

        createChangeItemForCreate(created, issue, user);

        eventPublisher.publish(new RemoteIssueLinkCreateEvent(created.getId(), created.getApplicationType()));

        return created;
    }

    @Override
    public void updateRemoteIssueLink(final RemoteIssueLink remoteIssueLink, final User user) throws UpdateException
    {
        validateMandatoryFieldsForUpdate(remoteIssueLink);
        validateFieldLengthsForUpdate(remoteIssueLink);
        validateUrlsForUpdate(remoteIssueLink);

        final Issue issue = issueManager.getIssueObject(remoteIssueLink.getIssueId());
        if (issue == null)
        {
            throw new UpdateException("Issue with id '" + remoteIssueLink.getIssueId() + "' does not exist.");
        }

        // Make sure remote issue link exists
        final RemoteIssueLink found = getRemoteIssueLink(remoteIssueLink.getId());
        if (found == null)
        {
            throw new UpdateException("Remote link with id '" + remoteIssueLink.getId() + "' does not exist.");
        }
        else
        {
            validateGlobalIdForUpdate(issue, found.getGlobalId(), remoteIssueLink.getGlobalId());
        }

        remoteIssueLinkStore.updateRemoteIssueLink(remoteIssueLink);

        if (found.getGlobalId() != null && !found.getGlobalId().equals(remoteIssueLink.getGlobalId()) && hasDuplicateGlobalId(issue, remoteIssueLink))
        {
            remoteIssueLinkStore.updateRemoteIssueLink(found);
            throw new UpdateException("A remote issue link already exists on this issue with the globalId '" + remoteIssueLink.getGlobalId() + "'");
        }

        createChangeItemForUpdate(found, remoteIssueLink, issue, user);

        eventPublisher.publish(new RemoteIssueLinkUpdateEvent(remoteIssueLink.getId(), remoteIssueLink.getApplicationType()));
    }

    @Override
    public void removeRemoteIssueLink(final Long remoteIssueLinkId, final User user)
    {
        // Make sure remote issue link exists
        final RemoteIssueLink remoteIssueLink = getRemoteIssueLink(remoteIssueLinkId);
        if (remoteIssueLink == null)
        {
            // Nothing to do
            return;
        }

        removeRemoteIssueLink(remoteIssueLink, user);
    }

    @Override
    public void removeRemoteIssueLinkByGlobalId(Issue issue, String globalId, final User user)
    {
        final List<RemoteIssueLink> remoteIssueLinks = remoteIssueLinkStore.getRemoteIssueLinksByGlobalId(issue, globalId);
        if (remoteIssueLinks.isEmpty())
        {
            // Nothing to do
            return;
        }

        // Delete in reverse order, so that #getRemoteIssueLinkByGlobalId will behave predictably while removal is being
        // performed.
        for (RemoteIssueLink remoteIssueLink : Iterables.reverse(remoteIssueLinks))
        {
            removeRemoteIssueLink(remoteIssueLink, user);
        }
    }

    private void removeRemoteIssueLink(RemoteIssueLink remoteIssueLink, User user)
    {
        remoteIssueLinkStore.removeRemoteIssueLink(remoteIssueLink.getId());

        final Issue issue = issueManager.getIssueObject(remoteIssueLink.getIssueId());
        createChangeItemForRemove(remoteIssueLink, issue, user);

        eventPublisher.publish(new RemoteIssueLinkDeleteEvent(remoteIssueLink.getId()));
    }

    private void validateGlobalIdForCreate(final Issue issue, final String globalId) throws CreateException
    {
            final Result result = validateGlobalId(issue, globalId);
            if (!result.isValid())
            {
                throw new CreateException(result.getMessage());
            }
    }

    private void validateGlobalIdForUpdate(final Issue issue, final String oldGlobalId, final String newGlobalId) throws UpdateException
    {
        // Validate only if it has changed
        if (!isEqual(oldGlobalId, newGlobalId))
        {
            final Result result = validateGlobalId(issue, newGlobalId);
            if (!result.isValid())
            {
                throw new UpdateException(result.getMessage());
            }
        }
    }

    private Result validateGlobalId(final Issue issue, final String globalId)
    {
        if (globalId == null)
        {
            return TRUE;
        }

        // Check for duplicate globalId on this issue
        if (getRemoteIssueLinkByGlobalId(issue, globalId) != null)
        {
            return new Result(false, "A remote issue link already exists on this issue with the globalId '" + globalId + "'");
        }

        return TRUE;
    }

    private boolean hasDuplicateGlobalId(Issue issue, RemoteIssueLink link)
    {
        if (link.getGlobalId() == null)
        {
            return false;
        }

        final List<RemoteIssueLink> links = remoteIssueLinkStore.getRemoteIssueLinksByGlobalId(issue, link.getGlobalId());
        return links.size() > 1;
    }

    /**
     * Mandatory fields are:
     *    - issueId
     *    - title
     *    - url
     *
     * @param remoteIssueLink
     * @throws CreateException
     */
    private void validateMandatoryFieldsForCreate(final RemoteIssueLink remoteIssueLink) throws CreateException
    {
        final Result result = validateMandatoryFields(remoteIssueLink);
        if (!result.isValid())
        {
            throw new CreateException(result.getMessage());
        }
    }

    /**
     * Mandatory fields are:
     *    - id (the remote issue link must exist)
     *    - issueId
     *    - title
     *    - url
     *
     * @param remoteIssueLink
     * @throws UpdateException
     */
    private void validateMandatoryFieldsForUpdate(final RemoteIssueLink remoteIssueLink) throws UpdateException
    {
        if (remoteIssueLink.getId() == null)
        {
            throw new UpdateException(missingMandatoryFieldMessage("id"));
        }

        final Result result = validateMandatoryFields(remoteIssueLink);
        if (!result.isValid())
        {
            throw new UpdateException(result.getMessage());
        }
    }

    private Result validateMandatoryFields(final RemoteIssueLink remoteIssueLink)
    {
        if (remoteIssueLink.getIssueId() == null)
        {
            return new Result(false, missingMandatoryFieldMessage("issueId"));
        }

        if (StringUtils.isBlank(remoteIssueLink.getTitle()))
        {
            return new Result(false, missingMandatoryFieldMessage("title"));
        }

        if (StringUtils.isBlank(remoteIssueLink.getUrl()))
        {
            return new Result(false, missingMandatoryFieldMessage("url"));
        }

        return TRUE;
    }

    private void validateFieldLengthsForCreate(final RemoteIssueLink remoteIssueLink) throws CreateException
    {
        final Result result = validateFieldLengths(remoteIssueLink);
        if (!result.isValid())
        {
            throw new CreateException(result.getMessage());
        }
    }

    private void validateFieldLengthsForUpdate(final RemoteIssueLink remoteIssueLink) throws UpdateException
    {
        final Result result = validateFieldLengths(remoteIssueLink);
        if (!result.isValid())
        {
            throw new UpdateException(result.getMessage());
        }
    }

    private Result validateFieldLengths(final RemoteIssueLink remoteIssueLink)
    {
        if (isLongerThan(remoteIssueLink.getGlobalId(), MAX_LONG_VARCHAR_LENGTH))
        {
            return new Result(false, tooLongFieldMessage("globalId", MAX_LONG_VARCHAR_LENGTH));
        }

        if (isLongerThan(remoteIssueLink.getTitle(), MAX_LONG_VARCHAR_LENGTH))
        {
            return new Result(false, tooLongFieldMessage("title", MAX_LONG_VARCHAR_LENGTH));
        }

        if (isLongerThan(remoteIssueLink.getRelationship(), MAX_LONG_VARCHAR_LENGTH))
        {
            return new Result(false, tooLongFieldMessage("relationship", MAX_LONG_VARCHAR_LENGTH));
        }

        if (isLongerThan(remoteIssueLink.getApplicationType(), MAX_LONG_VARCHAR_LENGTH))
        {
            return new Result(false, tooLongFieldMessage("applicationType", MAX_LONG_VARCHAR_LENGTH));
        }

        if (isLongerThan(remoteIssueLink.getApplicationName(), MAX_LONG_VARCHAR_LENGTH))
        {
            return new Result(false, tooLongFieldMessage("applicationName", MAX_LONG_VARCHAR_LENGTH));
        }

        return TRUE;
    }

    private boolean isLongerThan(String value, int length)
    {
        return value != null && value.length() > length;
    }

    private void validateUrlsForCreate(final RemoteIssueLink remoteIssueLink) throws CreateException
    {
        final Result result = validateUrls(remoteIssueLink);
        if (!result.isValid())
        {
            throw new CreateException(result.getMessage());
        }
    }

    private void validateUrlsForUpdate(final RemoteIssueLink remoteIssueLink) throws UpdateException
    {
        final Result result = validateUrls(remoteIssueLink);
        if (!result.isValid())
        {
            throw new UpdateException(result.getMessage());
        }
    }

    private Result validateUrls(final RemoteIssueLink remoteIssueLink)
    {
        if (!isValidUrl(remoteIssueLink.getUrl()))
        {
            return new Result(false, invalidUrlMessage("url"));
        }

        if (!isValidUrl(remoteIssueLink.getIconUrl()))
        {
            return new Result(false, invalidUrlMessage("iconUrl"));
        }

        if (!isValidUrl(remoteIssueLink.getStatusIconUrl()))
        {
            return new Result(false, invalidUrlMessage("statusIconUrl"));
        }

        if (!isValidUrl(remoteIssueLink.getStatusIconLink()))
        {
            return new Result(false, invalidUrlMessage("statusIconLink"));
        }

        return TRUE;
    }

    private boolean isValidUrl(final String url)
    {
        if (url == null) return true;

        try
        {
            new URI(url);
        }
        catch (final URISyntaxException e)
        {
            return false;
        }

        return true;
    }

    private void createChangeItemForCreate(final RemoteIssueLink remoteIssueLink, final Issue issue, final User remoteUser)
    {
        final ChangeItemBean changeItemBean = new ChangeItemBean(
                ChangeItemBean.STATIC_FIELD,
                "RemoteIssueLink",
                null,
                null,
                remoteIssueLink.getId().toString(),
                getChangeItemDescription(remoteIssueLink, remoteUser));

        createChangeItem(changeItemBean, issue, remoteUser);
    }

    private void createChangeItemForRemove(final RemoteIssueLink remoteIssueLink, final Issue issue, final User remoteUser)
    {
        final ChangeItemBean changeItemBean = new ChangeItemBean(
                ChangeItemBean.STATIC_FIELD,
                "RemoteIssueLink",
                remoteIssueLink.getId().toString(),
                getChangeItemDescription(remoteIssueLink, remoteUser),
                null,
                null);
        
        createChangeItem(changeItemBean, issue, remoteUser);
    }

    private void createChangeItemForUpdate(final RemoteIssueLink oldLink, final RemoteIssueLink newLink, final Issue issue, final User remoteUser)
    {
        final ChangeItemBean changeItemBean = new ChangeItemBean(
                ChangeItemBean.STATIC_FIELD,
                "RemoteIssueLink",
                oldLink.getId().toString(),
                getChangeItemDescription(oldLink, remoteUser),
                newLink.getId().toString(),
                getChangeItemDescription(newLink, remoteUser));

        createChangeItem(changeItemBean, issue, remoteUser);
    }

    private String getChangeItemDescription(final RemoteIssueLink remoteIssueLink, final User remoteUser)
    {
        final I18nHelper i18nHelper = i18nBeanFactory.getInstance(remoteUser);

        final String applicationName = StringUtils.defaultIfEmpty(remoteIssueLink.getApplicationName(),
                                                                  i18nHelper.getText("remotelink.manager.changeitem.applicationname.default"));

        return i18nHelper.getText("remotelink.manager.changeitem", remoteIssueLink.getTitle(), applicationName);
    }

    private void createChangeItem(final ChangeItemBean changeItemBean, final Issue issue, final User remoteUser)
    {
        final GenericValue issueGv = issue.getGenericValue();

        // Note the event will not be dispatched. The issue is updated however so we pass ISSUE_UPDATED constant
        final IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issueGv, issueGv, EventType.ISSUE_UPDATED_ID, remoteUser);
        issueUpdateBean.setDispatchEvent(false);
        issueUpdateBean.setChangeItems(EasyList.build(changeItemBean));
        issueUpdater.doUpdate(issueUpdateBean, true);
    }

    private boolean isEqual(final String a, final String b)
    {
        return (a == null) ? (b == null) : a.equals(b);
    }

    private String missingMandatoryFieldMessage(final String fieldName)
    {
        return "Missing mandatory field: " + fieldName;
    }

    private String invalidUrlMessage(final String fieldName)
    {
        return "Invalid " + fieldName + ", it must be a valid URI";
    }

    private String tooLongFieldMessage(final String fieldName, int maxLength)
    {
        return "The length of the " + fieldName + " cannot exceed " + maxLength + " characters.";
    }

    private static final Result TRUE = new Result(true, null);

    private static class Result
    {
        private final boolean valid;
        private final String message;

        private Result(final boolean valid, final String message)
        {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid()
        {
            return valid;
        }

        public String getMessage()
        {
            return message;
        }
    }
}
