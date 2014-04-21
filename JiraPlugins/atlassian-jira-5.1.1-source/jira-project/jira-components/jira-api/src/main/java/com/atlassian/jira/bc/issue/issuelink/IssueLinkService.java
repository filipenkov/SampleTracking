package com.atlassian.jira.bc.issue.issuelink;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuelink.Direction;
import com.atlassian.jira.issue.issuelink.IssueLink;
import com.atlassian.jira.issue.issuelink.IssueLinkType;
import com.atlassian.jira.issue.issuelink.IssueLinks;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;
import java.util.Set;

/**
 * A service that provides issue linking capabilities
 *
 * @since v4.4
 * @deprecated Use {@link com.atlassian.jira.bc.issue.link.IssueLinkService} instead. Since v5.0.
 */
@PublicApi
public interface IssueLinkService
{
    /**
     * @return the all issue link types defined in JIRA
     */
    public Collection<IssueLinkType> getIssueLinkTypes();

    /**
     * Returns the issue link or null if not found.
     *
     * @param sourceId source issue id of the link
     * @param destinationId destination issue id of the link
     * @param issueLinkTypeId link type id of the link
     * @return issue link or null if not found
     */
    public IssueLink getIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId);

    /**
     * Returns the issue links that the specified user can see. Will only return non-system (user-defined) links.
     *
     * @param user The user performing the operation
     * @param issue The issue that links will retrieved on
     * @return a result that contains the issue links
     */
    IssueLinkResult getIssueLinks(final User user, final Issue issue);

    /**
     * @see #getIssueLinks(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue)
     * @param user The user performing the operation
     * @param issue The issue that links will retrieved on
     * @param excludeSystemLinks whether or not to exclude system links
     * @return a result that contains the issue links
     */
    IssueLinkResult getIssueLinks(final User user, final Issue issue, boolean excludeSystemLinks);

    /**
     * Validates that the user provided can add the link provided for a particular issue.  Validation will ensure that
     * the user has the EDIT_ISSUE permission for the issue in question.  The label will also be validated to ensure
     * that it doesn't contain spaces and that it doesn't exceed the max length of 255 characters.
     *
     * Only user-created (i.e. non-system) links are allowed.
     *
     * @param user The user performing the operation
     * @param issue The issue that links will be set on
     * @param linkName The actual link name as strings to set on the issue
     * @param linkKeys The collection of issue keys to link against
     * @return a validation result, that can be used to set the labels or to display errors.
     */
    AddIssueLinkValidationResult validateAddIssueLinks(final User user, final Issue issue, final String linkName, final Collection<String> linkKeys);

    /**
     * @param user The user performing the operation
     * @param issue The issue that links will be set on
     * @param issueLinkTypeId The actual link id to set on the issue
     * @param direction which direction we are linking in
     * @param linkKeys The collection of issue keys to link against
     * @param excludeSystemLinks whether or not system links are okay
     * @return a validation result, that can be used to set the labels or to display errors.
     */
    AddIssueLinkValidationResult validateAddIssueLinks(final User user, final Issue issue, final Long issueLinkTypeId, final Direction direction, final Collection<String> linkKeys, boolean excludeSystemLinks);

    /**
     * Adds the issue link to the issue specified by the validation result.
     *
     * @param user The user performing the operation
     * @param result The validation result obtained via {@link #validateAddIssueLinks(com.atlassian.crowd.embedded.api.User,com.atlassian.jira.issue.Issue, String, java.util.Collection }
     * @return result containing the new links
     */
    IssueLinkResult addIssueLinks(final User user, final AddIssueLinkValidationResult result);

    /**
     * Validates parameters and checks permissions, and if all checks pass it will create a
     * {@link DeleteIssueLinkValidationResult} that can be passed to the
     * {@link #delete(DeleteIssueLinkValidationResult)} method.
     *
     * @param user who the permission checks will be run against (can be null, indicating an anonymous user)
     * @param issue issue the link is being deleted from
     * @param issueLink issue link to be deleted
     * @return a validation result, if all validation and permission passes it will contain a validated issue link id,
     * otherwise the issue link id will be null
     */
    DeleteIssueLinkValidationResult validateDelete(final User user, final Issue issue, final IssueLink issueLink);

    /**
     * Deletes the issue link with the given id from the JIRA datastore, if the current user has permission to do so.
     *
     * @param deleteValidationResult contains the remote issue link id to delete. This should have been created by the
     * {@link #validateDelete(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.Issue, com.atlassian.jira.issue.issuelink.IssueLink)}
     * method. The result must have {@link com.atlassian.jira.bc.ServiceResult#isValid()} return true. If false this
     * method will throw an IllegalStateException.
     */
    void delete(final DeleteIssueLinkValidationResult deleteValidationResult);

    @PublicApi
    public static class IssueLinkResult extends ServiceResultImpl
    {
        private final Set<IssueLinkType> issueLinkTypes;
        private final IssueLinks issueLinks;

        public IssueLinkResult(final ErrorCollection errorCollection, final Set<IssueLinkType> issueLinkTypes, IssueLinks issueLinks)
        {
            super(errorCollection);
            this.issueLinkTypes = issueLinkTypes;
            this.issueLinks = issueLinks;
        }

        public Set<IssueLinkType> getIssueLinkTypes()
        {
            return issueLinkTypes;
        }

        public IssueLinks getIssueLinks()
        {
            return issueLinks;
        }
    }


    @PublicApi
    public static abstract class IssueLinkValidationResult extends ServiceResultImpl
    {
        private final Issue issue;

        public IssueLinkValidationResult(final ErrorCollection errorCollection, final Issue issueId)
        {
            super(errorCollection);
            this.issue = issueId;
        }

        public Issue getIssue()
        {
            return issue;
        }
    }


    @PublicApi
    public static class AddIssueLinkValidationResult extends IssueLinkValidationResult
    {
        private final User user;
        private final Collection<String> linkKeys;
        private IssueLinkType linkType;
        private Direction direction;

        public AddIssueLinkValidationResult(User user, ErrorCollection errorCollection, Issue issueId, IssueLinkType linkType, Direction direction, Collection<String> linkKeys)
        {
            super(errorCollection, issueId);
            this.user = user;
            this.linkType = linkType;
            this.direction = direction;
            this.linkKeys = linkKeys;
        }

        public Collection<String> getLinkKeys()
        {
            return linkKeys;
        }

        /**
         * @return the directional name.
         */
        public String getLinkName()
        {
            return direction == Direction.OUT ? linkType.getOutward() : linkType.getInward();
        }

        public IssueLinkType getLinkType()
        {
            return linkType;
        }

        public Direction getDirection()
        {
            return direction;
        }

        public User getUser()
        {
            return user;
        }
    }

    @PublicApi
    public static class DeleteIssueLinkValidationResult extends ServiceResultImpl
    {
        private final IssueLink issueLink;
        private final User user;

        DeleteIssueLinkValidationResult(final ErrorCollection errorCollection, final IssueLink issueLink, final User user)
        {
            super(errorCollection);
            this.issueLink = issueLink;
            this.user = user;
        }

        public IssueLink getIssueLink()
        {
            return issueLink;
        }

        public User getUser()
        {
            return user;
        }
    }
}
