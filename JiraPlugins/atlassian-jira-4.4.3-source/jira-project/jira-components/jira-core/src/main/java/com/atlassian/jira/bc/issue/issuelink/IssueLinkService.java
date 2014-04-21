package com.atlassian.jira.bc.issue.issuelink;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceResultImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuelink.Direction;
import com.atlassian.jira.issue.issuelink.IssueLink;
import com.atlassian.jira.issue.issuelink.IssueLinkType;
import com.atlassian.jira.issue.issuelink.IssueLinks;
import com.atlassian.jira.util.ErrorCollection;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

/**
 * A service that provides issue linking capabilities
 *
 * @since v4.4
 */
public interface IssueLinkService
{
    /**
     * @return the all issue link types defined in JIRA
     */
    public Collection<IssueLinkType> getIssueLinkTypes();

    /**
     * Returns the issue links that the specified user can see. Will only return links that are non-system (user-defined).
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
     * Validates an attempt to remove the link of a specified type and direction between two issues.
     *
     * @param user the user
     * @param thisIssue the issue of focus in the link
     * @param issueLinkTypeId the type of issue link
     * @param direction the direction of the link
     * @param thatIssue the other issue in the link
     * @param excludeSystemLinks whether or not system links should be allowed to be deleted
     * @return the validation result
     */
    DeleteIssueLinkValidationResult validateDeleteIssueLink(final User user, final Issue thisIssue, final Long issueLinkTypeId, final Direction direction, final Issue thatIssue, boolean excludeSystemLinks);

    /**
     * Actually delete the issue link.
     *
     * @param user the user doing the deletion.
     * @param deleteIssueLinkValidationResult the validated result.
     */
    void deleteIssueLink(final User user, final DeleteIssueLinkValidationResult deleteIssueLinkValidationResult);

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


    public static class DeleteIssueLinkValidationResult extends ServiceResultImpl
    {
        private final IssueLink theLinkToDelete;

        public DeleteIssueLinkValidationResult(ErrorCollection errorCollection)
        {
            this(errorCollection, null);
        }

        public DeleteIssueLinkValidationResult(ErrorCollection errorCollection, @Nullable IssueLink theLinkToDelete)
        {
            super(errorCollection);
            this.theLinkToDelete = theLinkToDelete;
        }

        public IssueLink getTheLinkToDelete()
        {
            return theLinkToDelete;
        }
    }
}
