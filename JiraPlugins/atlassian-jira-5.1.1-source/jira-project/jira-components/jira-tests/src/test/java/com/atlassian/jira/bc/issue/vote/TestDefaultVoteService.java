package com.atlassian.jira.bc.issue.vote;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;


public class TestDefaultVoteService extends ListeningTestCase
{
    private User user = new MockUser("admin");
    private User voter = new MockUser("voter");

    @Test
    public void testValidateAddVote()
    {
        final MockIssue issue = new MockIssue(10000L);
        issue.setReporterId("voter");
        issue.setResolution(new MockGenericValue("blah"));

        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final I18nHelper.BeanFactory mockBeanFactory = createMock(I18nHelper.BeanFactory.class);
        final VoteManager mockVoteManager = createMock(VoteManager.class);
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        final MockI18nHelper mockI18nHelper = new MockI18nHelper();
        expect(mockBeanFactory.getInstance(user)).andReturn(mockI18nHelper).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, voter)).andReturn(false);
        expect(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_VOTING)).andReturn(false);
        expect(mockVoteManager.hasVoted(voter, issue)).andReturn(true);

        replay(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
        final DefaultVoteService defaultVoteService = new DefaultVoteService(mockVoteManager, mockBeanFactory, mockApplicationProperties, mockPermissionManager, null);

        final VoteService.VoteValidationResult validationResult = defaultVoteService.validateAddVote(user, voter, issue);

        assertFalse(validationResult.isValid());
        final Collection<String> errors = validationResult.getErrorCollection().getErrorMessages();
        assertTrue(errors.contains("issue.operations.error.vote.issue.permission"));
        assertTrue(errors.contains("issue.operations.voting.resolved"));
        assertTrue(errors.contains("issue.operations.voting.disabled"));
        assertTrue(errors.contains("issue.operations.error.add.vote.already.voted"));
        assertTrue(errors.contains("issue.operations.novote"));
        assertEquals(5, errors.size());

        verify(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
    }

    @Test
    public void testValidateRemoveVote()
    {
        final MockIssue issue = new MockIssue(10000L);
        issue.setReporterId("voter");
        issue.setResolution(new MockGenericValue("blah"));

        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final I18nHelper.BeanFactory mockBeanFactory = createMock(I18nHelper.BeanFactory.class);
        final VoteManager mockVoteManager = createMock(VoteManager.class);
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        final MockI18nHelper mockI18nHelper = new MockI18nHelper();
        expect(mockBeanFactory.getInstance(user)).andReturn(mockI18nHelper).anyTimes();
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, voter)).andReturn(false);
        expect(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_VOTING)).andReturn(false);
        expect(mockVoteManager.hasVoted(voter, issue)).andReturn(false);

        replay(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
        final DefaultVoteService defaultVoteService = new DefaultVoteService(mockVoteManager, mockBeanFactory, mockApplicationProperties, mockPermissionManager, null);

        final VoteService.VoteValidationResult validationResult = defaultVoteService.validateRemoveVote(user, voter, issue);

        assertFalse(validationResult.isValid());
        final Collection<String> errors = validationResult.getErrorCollection().getErrorMessages();
        assertTrue(errors.contains("issue.operations.error.vote.issue.permission"));
        assertTrue(errors.contains("issue.operations.voting.resolved"));
        assertTrue(errors.contains("issue.operations.voting.disabled"));
        assertTrue(errors.contains("issue.operations.error.remove.vote.not.voted"));
        assertTrue(errors.contains("issue.operations.novote"));
        assertEquals(5, errors.size());

        verify(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
    }

    @Test
    public void testAddVote()
    {
        final MockIssue issue = new MockIssue(10000L);
        issue.setReporterId("somedude");

        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final I18nHelper.BeanFactory mockBeanFactory = createMock(I18nHelper.BeanFactory.class);
        final VoteManager mockVoteManager = createMock(VoteManager.class);
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        final MockI18nHelper mockI18nHelper = new MockI18nHelper();
        expect(mockBeanFactory.getInstance(user)).andReturn(mockI18nHelper);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, voter)).andReturn(true);
        expect(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_VOTING)).andReturn(true);
        expect(mockVoteManager.hasVoted(voter, issue)).andReturn(false);
        expect(mockVoteManager.addVote(voter, issue)).andReturn(true);
        expect(mockVoteManager.getVoterUsernames(issue)).andReturn(CollectionBuilder.newBuilder("voter", "someelse").asList());

        replay(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
        final DefaultVoteService defaultVoteService = new DefaultVoteService(mockVoteManager, mockBeanFactory, mockApplicationProperties, mockPermissionManager, null);

        final VoteService.VoteValidationResult validationResult = defaultVoteService.validateAddVote(user, voter, issue);

        assertTrue(validationResult.isValid());

        final int result = defaultVoteService.addVote(voter, validationResult);
        assertEquals(2, result);

        verify(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
    }

    @Test
    public void testRemoveVote()
    {
        final MockIssue issue = new MockIssue(10000L);
        issue.setReporterId("somedude");

        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final I18nHelper.BeanFactory mockBeanFactory = createMock(I18nHelper.BeanFactory.class);
        final VoteManager mockVoteManager = createMock(VoteManager.class);
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        final MockI18nHelper mockI18nHelper = new MockI18nHelper();
        expect(mockBeanFactory.getInstance(user)).andReturn(mockI18nHelper);
        expect(mockPermissionManager.hasPermission(Permissions.BROWSE, issue, voter)).andReturn(true);
        expect(mockApplicationProperties.getOption(APKeys.JIRA_OPTION_VOTING)).andReturn(true);
        expect(mockVoteManager.hasVoted(voter, issue)).andReturn(true);
        expect(mockVoteManager.removeVote(voter, issue)).andReturn(true);
        expect(mockVoteManager.getVoterUsernames(issue)).andReturn(Collections.<String>emptyList());

        replay(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
        final DefaultVoteService defaultVoteService = new DefaultVoteService(mockVoteManager, mockBeanFactory, mockApplicationProperties, mockPermissionManager, null);

        final VoteService.VoteValidationResult validationResult = defaultVoteService.validateRemoveVote(user, voter, issue);

        assertTrue(validationResult.isValid());

        final int result = defaultVoteService.removeVote(voter, validationResult);
        assertEquals(0, result);

        verify(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
    }

    @Test
    public void testViewVoters_noPermission() throws Exception
    {
        final MockIssue issue = new MockIssue(10000L);

        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final I18nHelper.BeanFactory mockBeanFactory = createMock(I18nHelper.BeanFactory.class);
        final VoteManager mockVoteManager = createMock(VoteManager.class);
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        final MockI18nHelper mockI18nHelper = new MockI18nHelper();
        expect(mockBeanFactory.getInstance(user)).andReturn(mockI18nHelper);
        expect(mockPermissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue.getProjectObject(), user)).andReturn(false);

        replay(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);

        final DefaultVoteService defaultVoteService = new DefaultVoteService(mockVoteManager, mockBeanFactory, mockApplicationProperties, mockPermissionManager, mockBeanFactory);

        final ServiceOutcome<Collection<User>> outcome = defaultVoteService.viewVoters(issue, user);
        assertFalse(outcome.isValid());
        assertEquals(null, outcome.getReturnedValue());

        verify(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
    }

    @Test
    public void testViewVoters_votingDisabled() throws Exception
    {
        final MockIssue issue = new MockIssue(10000L);

        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final I18nHelper.BeanFactory mockBeanFactory = createMock(I18nHelper.BeanFactory.class);
        final VoteManager mockVoteManager = createMock(VoteManager.class);
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        final MockI18nHelper mockI18nHelper = new MockI18nHelper();
        expect(mockBeanFactory.getInstance(user)).andReturn(mockI18nHelper);
        expect(mockPermissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue.getProjectObject(), user)).andReturn(true);
        expect(mockVoteManager.isVotingEnabled()).andReturn(false);

        replay(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);

        final DefaultVoteService defaultVoteService = new DefaultVoteService(mockVoteManager, mockBeanFactory, mockApplicationProperties, mockPermissionManager, mockBeanFactory);

        final ServiceOutcome<Collection<User>> outcome = defaultVoteService.viewVoters(issue, user);
        assertTrue(!outcome.isValid());
        assertEquals(null, outcome.getReturnedValue());

        verify(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
    }

    @Test
    public void testViewVoters() throws Exception
    {
        final MockIssue issue = new MockIssue(10000L);

        final User user = new MockUser("bob");

        final PermissionManager mockPermissionManager = createMock(PermissionManager.class);
        final I18nHelper.BeanFactory mockBeanFactory = createMock(I18nHelper.BeanFactory.class);
        final VoteManager mockVoteManager = createMock(VoteManager.class);
        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);

        final MockI18nHelper mockI18nHelper = new MockI18nHelper();
        expect(mockBeanFactory.getInstance(user)).andReturn(mockI18nHelper);
        expect(mockPermissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue.getProjectObject(), user)).andReturn(true);
        expect(mockVoteManager.isVotingEnabled()).andReturn(true);

        expect(mockVoteManager.getVoters(issue, mockI18nHelper.getLocale())).andReturn(Arrays.asList(user));

        replay(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);

        final DefaultVoteService defaultVoteService = new DefaultVoteService(mockVoteManager, mockBeanFactory, mockApplicationProperties, mockPermissionManager, mockBeanFactory);

        final ServiceOutcome<Collection<User>> outcome = defaultVoteService.viewVoters(issue, user);
        assertTrue(outcome.isValid());
        assertEquals(1, outcome.getReturnedValue().size());
        assertEquals(user, outcome.getReturnedValue().iterator().next());

        verify(mockPermissionManager, mockBeanFactory, mockVoteManager, mockApplicationProperties);
    }
}
