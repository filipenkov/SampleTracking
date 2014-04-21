package com.atlassian.jira.bc.issue.vote;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.vote.VoteHistoryEntry;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultVoteService implements VoteService
{
    private final VoteManager voteManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;
    private final I18nHelper.BeanFactory i18nFactory;

    public DefaultVoteService(final VoteManager voteManager, final I18nHelper.BeanFactory beanFactory,
            final ApplicationProperties applicationProperties, final PermissionManager permissionManager,
            final I18nHelper.BeanFactory i18nFactory)
    {
        this.voteManager = voteManager;
        this.beanFactory = beanFactory;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
        this.i18nFactory = i18nFactory;
    }

    @Override
    public VoteValidationResult validateAddVote(com.opensymphony.user.User remoteUser, com.opensymphony.user.User user, Issue issue)
    {
        return validateAddVote((User) remoteUser, (User) user, issue);
    }

    public VoteValidationResult validateAddVote(final User remoteUser, final User voter, final Issue issue)
    {
        notNull("voter", voter);
        notNull("issue", issue);

        final VoteValidationResult result = validateVoting(remoteUser, voter, issue);

        if (voteManager.hasVoted(voter, issue))
        {
            final I18nHelper i18n = beanFactory.getInstance(remoteUser);
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.error.add.vote.already.voted"));
        }
        return result;
    }

    @Override
    public int addVote(com.opensymphony.user.User remoteUser, VoteValidationResult validationResult)
    {
        return addVote((User) remoteUser, validationResult);
    }

    public int addVote(final User remoteUser, final VoteValidationResult validationResult)
    {
        notNull("remoteUser", remoteUser);
        notNull("validationResult", validationResult);

        voteManager.addVote(validationResult.getVoter(), validationResult.getIssue());
        final Collection usernames = voteManager.getVoterUsernames(validationResult.getIssue());
        return usernames.size();
    }

    @Override
    public VoteValidationResult validateRemoveVote(com.opensymphony.user.User remoteUser, com.opensymphony.user.User user, Issue issue)
    {
        return validateRemoveVote((User) remoteUser, (User) user, issue);
    }

    public VoteValidationResult validateRemoveVote(final User remoteUser, final User voter, final Issue issue)
    {
        notNull("voter", voter);
        notNull("issue", issue);

        final VoteValidationResult result = validateVoting(remoteUser, voter, issue);

        if (!voteManager.hasVoted(voter, issue))
        {
            final I18nHelper i18n = beanFactory.getInstance(remoteUser);
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.error.remove.vote.not.voted"));
        }
        return result;
    }

    @Override
    public int removeVote(com.opensymphony.user.User remoteUser, VoteValidationResult validationResult)
    {
        return removeVote((User) remoteUser, validationResult);
    }

    public int removeVote(final User remoteUser, final VoteValidationResult validationResult)
    {
        notNull("remoteUser", remoteUser);
        notNull("validationResult", validationResult);

        voteManager.removeVote(validationResult.getVoter(), validationResult.getIssue());

        final Collection usernames = voteManager.getVoterUsernames(validationResult.getIssue());
        return usernames.size();
    }

    @Override
    public ServiceOutcome<Collection<com.opensymphony.user.User>> viewVoters(Issue issue, com.opensymphony.user.User remoteUser)
    {
        final I18nHelper i18n = i18nFactory.getInstance(remoteUser);

        if (!permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue.getProjectObject(), remoteUser))
        {
            return ServiceOutcomeImpl.error(i18n.getText("voters.no.permission"));
        }
        else
        {
            if (voteManager.isVotingEnabled())
            {
                final Collection<com.opensymphony.user.User> voters = voteManager.getVoters(i18n.getLocale(), issue.getGenericValue());
                return ServiceOutcomeImpl.ok(voters);
            }
            else
            {
                return ServiceOutcomeImpl.error(i18n.getText("issue.operations.voting.disabled"));
            }
        }
    }

    public ServiceOutcome<Collection<User>> viewVoters(final Issue issue, final User remoteUser)
    {
        final I18nHelper i18n = i18nFactory.getInstance(remoteUser);

        if (!permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue.getProjectObject(), remoteUser))
        {
            return ServiceOutcomeImpl.error(i18n.getText("voters.no.permission"));
        }
        else
        {
            if (voteManager.isVotingEnabled())
            {
                final Collection<User> voters = voteManager.getVoters(issue, i18n.getLocale());
                return ServiceOutcomeImpl.ok(voters);
            }
            else
            {
                return ServiceOutcomeImpl.error(i18n.getText("issue.operations.voting.disabled"));
            }
        }
    }

    @Override
    public ServiceOutcome<List<VoteHistoryEntry>> getVoterHistory(Issue issue, User remoteUser)
    {
        final I18nHelper i18n = i18nFactory.getInstance(remoteUser);

        if (!permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue.getProjectObject(), remoteUser))
        {
            return ServiceOutcomeImpl.error(i18n.getText("voters.no.permission"));
        }
        else
        {
            if (voteManager.isVotingEnabled())
            {
                return ServiceOutcomeImpl.ok(voteManager.getVoteHistory(issue));
            }
            else
            {
                return ServiceOutcomeImpl.error(i18n.getText("issue.operations.voting.disabled"));
            }
        }
    }

    public boolean isVotingEnabled()
    {
        return voteManager.isVotingEnabled();
    }

    @Override
    public boolean hasVoted(Issue issue, com.opensymphony.user.User user)
    {
        return hasVoted(issue, (User) user);
    }

    public boolean hasVoted(final Issue issue, final User user)
    {
        return voteManager.hasVoted(user, issue.getGenericValue());
    }

    private VoteValidationResult validateVoting(final User remoteUser, final User voter, final Issue issue)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = beanFactory.getInstance(remoteUser);
        final VoteValidationResult result = new VoteValidationResult(errors, voter, issue);
        if (remoteUser == null || voter == null)
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.voting.not.loggedin"));
        }
        if(!permissionManager.hasPermission(Permissions.BROWSE, issue, voter))
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.error.vote.issue.permission"));
        }
        final String reporterId = issue.getReporterId();
        if (StringUtils.isNotBlank(reporterId) && voter != null && reporterId.equals(voter.getName()))
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.novote"));
        }
        if (issue.getResolution() != null)
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.voting.resolved"));
        }
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING))
        {
            result.getErrorCollection().addErrorMessage(i18n.getText("issue.operations.voting.disabled"));
        }
        return result;
    }
}
