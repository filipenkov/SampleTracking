package com.atlassian.jira.issue.vote;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.config.properties.ApplicationPropertiesStore;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.config.util.MockIndexPathManager;
import com.atlassian.jira.config.util.MockIndexingConfiguration;
import com.atlassian.jira.config.util.TestAbstractJiraHome;
import com.atlassian.jira.issue.index.BulkOnlyIndexManager;
import com.atlassian.jira.issue.index.MemoryIssueIndexer;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.user.MockUser;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.Locale;

public class TestDefaultVoteManager extends AbstractUsersTestCase
{
    DefaultVoteManager voteManager;
    private User bob;
    private GenericValue issue;
    private ReindexMessageManager reindexIssueManager;

    public TestDefaultVoteManager(final String s)
    {
        super(s);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        CoreTransactionUtil.setUseTransactions(false);
        CrowdService crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);
        VoteHistoryStore voteHistoryStore = ComponentManager.getComponentInstanceOfType(VoteHistoryStore.class);
        bob = new MockUser("bob");
        crowdService.addUser(bob, "password");
        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "key", "JRA-52", "project", new Long(1), "votes", new Long(1)));

        JiraTestUtil.loginUser(bob);

        final ApplicationPropertiesImpl appProperties = new ApplicationPropertiesImpl(
                new ApplicationPropertiesStore(PropertiesManager.getInstance(),
                new TestAbstractJiraHome.FixedHome()));
        appProperties.setOption(APKeys.JIRA_OPTION_VOTING, true);

        reindexIssueManager = EasyMock.createMock(ReindexMessageManager.class);
        EasyMock.replay(reindexIssueManager);

        voteManager = new DefaultVoteManager(appProperties, getUserAssociationStore(), new BulkOnlyIndexManager(
            new MockIndexingConfiguration(), new MemoryIssueIndexer(), new MockIndexPathManager(), reindexIssueManager), voteHistoryStore);
    }

    @Override
    protected void tearDown() throws Exception
    {
        EasyMock.verify(reindexIssueManager);
        CoreTransactionUtil.setUseTransactions(true);
        issue = null;
        bob = null;
        voteManager = null;
        UtilsForTests.cleanUsers();
        super.tearDown();
    }

    public void testAddRemoveVote()
    {
        assertTrue(voteManager.getVoters(Locale.ENGLISH, issue).isEmpty());
        assertFalse(voteManager.getVoters(Locale.ENGLISH, issue).contains(bob));
        assertFalse(voteManager.hasVoted(bob, issue));

        voteManager.addVote(bob, issue);

        assertFalse(voteManager.getVoters(Locale.ENGLISH, issue).isEmpty());
        assertTrue(voteManager.getVoters(Locale.ENGLISH, issue).contains(bob));
        assertTrue(voteManager.hasVoted(bob, issue));
        assertEquals(1, voteManager.getVoteHistory(new MockIssue(1L)).size());

        voteManager.removeVote(bob, issue);

        assertTrue(voteManager.getVoters(Locale.ENGLISH, issue).isEmpty());
        assertFalse(voteManager.getVoters(Locale.ENGLISH, issue).contains(bob));
        assertFalse(voteManager.hasVoted(bob, issue));
        assertEquals(2, voteManager.getVoteHistory(new MockIssue(1L)).size());
    }

    public void testRemoveVotesForUser()
    {
        final GenericValue issueGV1 = UtilsForTests.getTestEntity("Issue",
            EasyMap.build("id", new Long(1111), "key", "JRA-1", "project", new Long(1), "votes", new Long(1)));
        final MockIssue issue1 = new MockIssue((long) 1111);
        issue1.setGenericValue(issueGV1);
        final GenericValue issueGV2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(222), "key", "JRA-2", "project", new Long(1), "votes", new Long(1)));
        final MockIssue issue2 = new MockIssue((long) 222);
        issue2.setGenericValue(issueGV2);
        final GenericValue issueGV3 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(333), "key", "JRA-3", "project", new Long(1), "votes", new Long(1)));
        final MockIssue issue3 = new MockIssue((long) 333);
        issue3.setGenericValue(issueGV3);

        voteManager.addVote(bob, issueGV1);
        voteManager.addVote(bob, issueGV2);
        assertEquals(1, voteManager.getVoters(Locale.ENGLISH, issueGV1).size());
        assertEquals(1, voteManager.getVoters(Locale.ENGLISH, issueGV2).size());
        assertEquals(0, voteManager.getVoters(Locale.ENGLISH, issueGV3).size());
        assertEquals(1, voteManager.getVoteHistory(issue1).size());
        assertEquals(1, voteManager.getVoteHistory(issue2).size());
        assertEquals(0, voteManager.getVoteHistory(issue3).size());

        voteManager.removeVotesForUser(bob);
        assertEquals(0, voteManager.getVoters(Locale.ENGLISH, issueGV1).size());
        assertEquals(0, voteManager.getVoters(Locale.ENGLISH, issueGV2).size());
        assertEquals(0, voteManager.getVoters(Locale.ENGLISH, issueGV3).size());
        assertEquals(2, voteManager.getVoteHistory(issue1).size());
        assertEquals(2, voteManager.getVoteHistory(issue2).size());
        assertEquals(0, voteManager.getVoteHistory(issue3).size());
    }

    public void testRemoveVotesForUserNullParam()
    {
        try
        {
            voteManager.removeVotesForUser(null);
            fail("IAE expected");
        }
        catch (final IllegalArgumentException yay)
        {
            // good
        }
    }

    private UserAssociationStore getUserAssociationStore()
    {
        return (UserAssociationStore) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(UserAssociationStore.class);
    }
}
