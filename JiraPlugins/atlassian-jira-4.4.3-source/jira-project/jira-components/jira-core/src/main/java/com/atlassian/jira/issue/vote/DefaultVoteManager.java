package com.atlassian.jira.issue.vote;

import com.atlassian.core.util.Clock;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.dbc.Null;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DefaultVoteManager implements VoteManager
{
    private static final Logger log = Logger.getLogger(DefaultVoteManager.class);
    private static final String ASSOCIATION_TYPE = "VoteIssue";

    private final ApplicationProperties applicationProperties;
    private final UserAssociationStore userAssociationStore;
    private final VoteHistoryStore voteHistoryStore;
    private final IssueIndexManager indexManager;


    public DefaultVoteManager(final ApplicationProperties applicationProperties, final UserAssociationStore userAssociationStore, final IssueIndexManager indexManager, VoteHistoryStore voteHistoryStore)
    {
        this.applicationProperties = applicationProperties;
        this.userAssociationStore = userAssociationStore;
        this.indexManager = indexManager;
        this.voteHistoryStore = voteHistoryStore;
    }

    @Override
    public boolean addVote(com.opensymphony.user.User user, Issue issue)
    {
        return updateVote(true, user, issue.getGenericValue());
    }

    public boolean addVote(final User user, final Issue issue)
    {
        return updateVote(true, user, issue.getGenericValue());
    }

    @Override
    public boolean addVote(com.opensymphony.user.User user, GenericValue issue)
    {
        return updateVote(true, user, issue);
    }

    public boolean addVote(final User user, final GenericValue issue)
    {
        return updateVote(true, user, issue);
    }

    @Override
    public boolean removeVote(com.opensymphony.user.User user, Issue issue)
    {
        return updateVote(false, user, issue.getGenericValue());
    }

    public boolean removeVote(final User user, final Issue issue)
    {
        return updateVote(false, user, issue.getGenericValue());
    }

    @Override
    public boolean removeVote(com.opensymphony.user.User user, GenericValue issue)
    {
        return updateVote(false, user, issue);
    }

    public boolean removeVote(final User user, final GenericValue issue)
    {
        return updateVote(false, user, issue);
    }

    public Collection<String> getVoterUsernames(final Issue issue)
    {
        return userAssociationStore.getUsernamesFromSink(ASSOCIATION_TYPE, issue.getGenericValue());
    }

    public Collection<String> getVoterUsernames(final GenericValue issue)
    {
        return userAssociationStore.getUsernamesFromSink(ASSOCIATION_TYPE, issue);
    }

    public List<VoteHistoryEntry> getVoteHistory(Issue issue)
    {
        return voteHistoryStore.getHistory(issue.getId());
    }

    public List<User> getVoters(final Issue issue, final Locale usersLocale)
    {
        return getVoters(issue.getGenericValue(), usersLocale);
    }

    public Collection<com.opensymphony.user.User> getVoters(final Locale usersLocale, final GenericValue issue)
    {
        return OSUserConverter.convertToOSUserList(getVoters(issue, usersLocale));
    }

    private List<User> getVoters(final GenericValue issueGV, final Locale usersLocale)
    {
        // Find the associated voters for this issue
        final List<User> voters = userAssociationStore.getUsersFromSink(ASSOCIATION_TYPE, issueGV);
        // Sort by User DisplayName in the preferred Locale
        Collections.sort(voters, new UserBestNameComparator(usersLocale));
        return voters;
    }

    private boolean updateVote(final boolean isVoting, final User user, final GenericValue issue)
    {
        if (validateUpdate(user, issue))
        {
            try
            {
                if (isVoting)
                {
                    if (!hasVoted(user, issue))
                    {
                        userAssociationStore.createAssociation(ASSOCIATION_TYPE, user, issue);
                        adjustVoteCount(issue, 1);
                        return true;
                    }
                }
                else
                {
                    if (hasVoted(user, issue))
                    {
                        userAssociationStore.removeAssociation(ASSOCIATION_TYPE, user, issue);
                        adjustVoteCount(issue, -1);
                        return true;
                    }
                }
            }
            catch (final GenericEntityException e)
            {
                log.error("Error changing vote association", e);
                return false;
            }
        }
        return false;
    }

    /**
     * Adjusts the vote count for an issue.
     *
     * @param issue       the issue to change count for
     * @param adjustValue the value to change it by
     * @throws GenericEntityException If there wasa persitence problem
     */

    private void adjustVoteCount(final GenericValue issue, final int adjustValue) throws GenericEntityException
    {
        Long votes = issue.getLong("votes");

        if (votes == null)
        {
            votes = 0L;
        }
        votes = votes + adjustValue;

        if (votes < 0)
        {
            votes = 0L;
        }

        issue.set("votes", votes);
        issue.store();

        final Timestamp now = new Timestamp(new Date().getTime());
        voteHistoryStore.add(new VoteHistoryEntryImpl(issue.getLong("id"), now ,votes));

        try
        {
            indexManager.reIndex(issue);
        }
        catch (final IndexException e)
        {
            log.error("Exception re-indexing issue " + e, e);
        }
    }

    /**
     * Validates that the params andd the system are in a correct state to change a vote
     *
     * @param user  The user who is voting
     * @param issue the issue the user is voting for
     * @return whether or not to go ahead with the vote.
     */
    private boolean validateUpdate(final User user, final GenericValue issue)
    {
        if (issue == null)
        {
            log.error("You must specify an issue.");
            return false;
        }

        if (!isVotingEnabled())
        {
            log.error("Voting is not enabled - the change vote on issue " + issue.getString("key") + " by user " + user.getName() + " was unsuccessful.");

            return false;
        }

        if (issue.getString("resolution") != null)
        {
            log.error("Cannot change vote on issue that has been resolved.");
            return false;
        }

        if (user == null)
        {
            log.error("You must specify a user.");
            return false;
        }
        return true;
    }

    /**
     * Check if voting has been enabled
     */
    public boolean isVotingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING);
    }

    @Override
    public boolean hasVoted(com.opensymphony.user.User user, Issue issue)
    {
        return hasVoted((User) user, issue);
    }

    public boolean hasVoted(User user, Issue issue)
    {
        if (user == null)
        {
            return false;
        }
        // For performance: if there are no votes for the issue then this dude didn't vote for it.
        if (issue.getVotes() == 0)
        {
            return false;
        }
        return userAssociationStore.associationExists(ASSOCIATION_TYPE, user, "Issue", issue.getId());
    }

    @Override
    public boolean hasVoted(com.opensymphony.user.User user, GenericValue issue)
    {
        return hasVoted((User) user, issue);
    }

    public boolean hasVoted(final User user, final GenericValue issue)
    {
        if (user == null)
        {
            return false;
        }
        // For performance: if there are no votes for the issue then this dude didn't vote for it.
        if (issue.getLong("votes") == 0)
        {
            return false;
        }
        return userAssociationStore.associationExists(ASSOCIATION_TYPE, user, issue);
    }

    @Override
    public void removeVotesForUser(com.opensymphony.user.User user)
    {
        removeVotesForUser((User) user);
    }

    public void removeVotesForUser(final User user)
    {
        Null.not("user", user);
        // Get all the issues
        List<GenericValue> issueGvs = userAssociationStore.getSinksFromUser(ASSOCIATION_TYPE, user, "Issue");
        for (GenericValue issueGv : issueGvs)
        {
            updateVote(false, user, issueGv);
        }
    }
}
