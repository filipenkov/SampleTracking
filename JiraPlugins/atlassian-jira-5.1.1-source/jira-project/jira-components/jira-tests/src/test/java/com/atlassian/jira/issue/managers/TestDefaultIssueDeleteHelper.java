package com.atlassian.jira.issue.managers;

import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkImpl;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.transaction.MockTransactionSupport;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.anyMap;
import static com.atlassian.jira.easymock.EasyMockMatcherUtils.nullArg;
import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link com.atlassian.jira.issue.managers.DefaultIssueDeleteHelper}.
 *
 * @since v4.0
 */
public class TestDefaultIssueDeleteHelper extends ListeningTestCase
{

    private static final long PROJECT_ID = 1L;
    private static final long ISSUE_ID = 1L;
    private static final long PARENT_ISSUE_ID = 20000L;
    private static final RemoteIssueLink REMOTE_ISSUE_LINK = new RemoteIssueLinkBuilder().id(10000L).build();

    @Mock
    private AssociationManager associationManager;

    @Mock
    private CustomFieldManager customFieldManager;

    @Mock
    private AttachmentManager attachmentManager;

    @Mock
    private ActionDispatcher actionDispatcher;

    @Mock
    private MailThreadManager mailThreadManager;

    @Mock
    private SubTaskManager mockSubTaskManager;

    @Mock
    private IssueManager mockIssueManager;

    @Mock
    private IssueLinkManager mockIssueLinkManager;

    @Mock
    private RemoteIssueLinkManager mockRemoteIssueLinkManager;

    @Mock
    private WorkflowManager workflowManager;

    @Mock
    private IssueIndexManager indexManager;

    @Mock
    private ChangeHistoryManager changeHistoryManager;

    @Mock
    private IssueEventManager issueEventManager;

    @Mock
    private MutableIssue issue;

    private DbIndependentMockGenericValue issueGenericValue;
    private Attachment attachment;

    private Map<String,Object> capturedEventParams;
    private boolean capturedSendMailFlag;

    @Before
    public void initMocks() throws Exception
    {
        EasyMockAnnotations.initMocks(this);

        issueGenericValue = createIssueGV(ISSUE_ID, PROJECT_ID, "Test issue", "Test-1", "Test Assignee", "Test Resolution");
        MockGenericValue attachmentGV = new MockGenericValue("Attachment");
        attachment = new Attachment(null, attachmentGV, null);

        final MockComponentWorker mockComponentWorker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(mockComponentWorker);
        mockComponentWorker.registerMock(TransactionSupport.class, new MockTransactionSupport());
    }

    private IssueDeleteHelper getIssueDeleteHelper() throws Exception
    {
        setUpIssue();
        baseMockSetup();
        defaultSubtaskSetup();
        return new DefaultIssueDeleteHelper(indexManager, mockSubTaskManager,  mockIssueLinkManager, mockRemoteIssueLinkManager, mailThreadManager,
                customFieldManager, attachmentManager, mockIssueManager, associationManager, workflowManager, changeHistoryManager,
                issueEventManager);
    }

    private void setUpIssue()
    {
        expect(issue.getId()).andReturn(ISSUE_ID).anyTimes();
        expect(issue.getGenericValue()).andReturn(issueGenericValue).anyTimes();
    }

    private void setUpEmptyCustomFields()
    {
        expect(customFieldManager.getCustomFieldObjects(issue)).andReturn(Collections.<CustomField>emptyList());
    }

    private void setUpEmptyWatchers()
    {
        expect(mockIssueManager.getWatchers(issue)).andReturn(Collections.<User>emptyList());
    }

    private void baseMockSetup() throws GenericEntityException, RemoveException, IndexException
    {
        expect(mailThreadManager.removeAssociatedEntries(ISSUE_ID)).andReturn(1);
        expect(mockIssueLinkManager.removeIssueLinksNoChangeItems(issue)).andReturn(1);

        expect(mockRemoteIssueLinkManager.getRemoteIssueLinksForIssue(issue)).andReturn(ImmutableList.of(REMOTE_ISSUE_LINK));
        mockRemoteIssueLinkManager.removeRemoteIssueLink(eq(REMOTE_ISSUE_LINK.getId()), nullArg(User.class));
        expectLastCall();

        expect(attachmentManager.getAttachments(issue)).andReturn(ImmutableList.of(attachment));

        expect(mockIssueManager.getIssueObject(ISSUE_ID)).andReturn(issue);
        associationManager.removeAssociationsFromSource(issueGenericValue);
        expectLastCall();
        associationManager.removeUserAssociationsFromSink(issueGenericValue);
        expectLastCall();
        customFieldManager.removeCustomFieldValues(issueGenericValue);
        expectLastCall();
        attachmentManager.deleteAttachment(attachment);
        expectLastCall();
        attachmentManager.deleteAttachmentDirectory(issue);
        expectLastCall();
        workflowManager.removeWorkflowEntries(issueGenericValue);
        expectLastCall();
        indexManager.deIndex(issue);
        expectLastCall();
        changeHistoryManager.removeAllChangeItems(issue);
        expectLastCall();
        stubEventManager();
    }

    private void defaultSubtaskSetup()
    {
        expect(mockSubTaskManager.getSubTaskIssueLinks(ISSUE_ID)).andReturn(Collections.<IssueLink>emptyList());
    }

    private void stubEventManager()
    {
        issueEventManager.dispatchEvent(eq(EventDispatchOption.ISSUE_DELETED.getEventTypeId()), eq(issue),
                anyMap(String.class, Object.class), nullArg(User.class), anyBoolean());
        expectLastCall().andAnswer(new IAnswer<Void>()
        {
            @Override
            public Void answer() throws Throwable
            {
                capturedEventParams = (Map) getCurrentArguments()[2];
                capturedSendMailFlag = (Boolean) getCurrentArguments()[4];
                return null;
            }
        });
    }

    private void replayMocks()
    {
        replay(associationManager, customFieldManager, attachmentManager, actionDispatcher, mailThreadManager,
                mockSubTaskManager, mockIssueManager, mockIssueLinkManager, mockRemoteIssueLinkManager, workflowManager, indexManager,
                issueEventManager, issue);
    }

    @Test
    public void shouldDeleteSubTaskWithLinks() throws Exception
    {
        setUpEmptyCustomFields();
        IssueDeleteHelper tested = getIssueDeleteHelper();
        setUpEmptyWatchers();
        MutableIssue parentIssue = createNiceMock(MutableIssue.class);
        expect(parentIssue.getId()).andReturn(PARENT_ISSUE_ID);
        replay(parentIssue);
        expect(issue.isSubTask()).andReturn(true);
        expect(issue.getParentObject()).andReturn(parentIssue);
        mockSubTaskManager.resetSequences(parentIssue);
        expectLastCall();
//        expect(mockIssueManager.getIssueObject(PARENT_ISSUE_ID)).andReturn(parentIssue);

        replayMocks();
        tested.deleteIssue((User) null, issue, EventDispatchOption.ISSUE_DELETED, true);
        makeAssertions();
        assertTrue(capturedSendMailFlag);
    }

    @Test
    public void shouldDeleteNotSubtask() throws Exception
    {
        setUpEmptyCustomFields();
        IssueDeleteHelper tested = getIssueDeleteHelper();
        setUpEmptyWatchers();
        expect(issue.isSubTask()).andReturn(false);
        replayMocks();

        tested.deleteIssue((User) null, issue, EventDispatchOption.ISSUE_DELETED, false);
        makeAssertions();
        assertFalse(capturedSendMailFlag);
    }

    @Test
    public void testRemoveSubTasks() throws Exception
    {
        setUpIssue();
        final AtomicInteger deleteIssueCalled = new AtomicInteger(0);
        DefaultIssueDeleteHelper tested = newMockDeleteIssueHelper(deleteIssueCalled);

        final MockGenericValue subTask1 = createIssueGV(2L, PROJECT_ID, "sub task 1", "TST-10", null, null);
        final MutableIssue subTaskIssue1 = createNiceMock(MutableIssue.class);
        expect(subTaskIssue1.getId()).andReturn(2L).anyTimes();
        expect(subTaskIssue1.getGenericValue()).andReturn(subTask1).anyTimes();
        final MockGenericValue subTask2 = createIssueGV(3L, PROJECT_ID, "sub task 2", "TST-11", null, null);
        final MutableIssue subTaskIssue2 = createNiceMock(MutableIssue.class);
        expect(subTaskIssue2.getId()).andReturn(3L).anyTimes();
        expect(subTaskIssue2.getGenericValue()).andReturn(subTask2).anyTimes();

        replay(subTaskIssue1, subTaskIssue2);
        expect(mockIssueManager.getIssueObject(2L)).andReturn(subTaskIssue1);
        expect(mockIssueManager.getIssueObject(3L)).andReturn(subTaskIssue2);

        final MockGenericValue mockLinkGV1 = new DbIndependentMockGenericValue("IssueLink",
                ImmutableMap.<String,Object>of("destination", 2L));
        final IssueLink issueLink1 = new IssueLinkImpl(mockLinkGV1, null, mockIssueManager);
        final MockGenericValue mockLinkGV2 = new DbIndependentMockGenericValue("IssueLink",
                ImmutableMap.<String,Object>of("destination", 3L));
        final IssueLink issueLink2 = new IssueLinkImpl(mockLinkGV2, null, mockIssueManager);
        expect(mockSubTaskManager.getSubTaskIssueLinks(ISSUE_ID)).andReturn(ImmutableList.of(issueLink1, issueLink2));

        replayMocks();

        tested.removeSubTasks(null, issue, EventDispatchOption.ISSUE_DELETED, true);

        assertEquals(2, deleteIssueCalled.get());
        verifyMocks();
    }

    @Test
    public void shouldAddCustomFieldParamsOnDelete() throws Exception
    {
        final CustomField customField1 = createMock(CustomField.class);
        expect(customField1.getId()).andReturn("customfield_10000");
        expect(customField1.getValue(issue)).andReturn("Value1");
        final CustomField customField2 = createMock(CustomField.class);
        expect(customField2.getId()).andReturn("customfield_10001");
        expect(customField2.getValue(issue)).andReturn("Value2");
        replay(customField1, customField2);

        expect(customFieldManager.getCustomFieldObjects(issue)).andReturn(ImmutableList.of(customField1, customField2));
        IssueDeleteHelper tested = getIssueDeleteHelper();
        setUpEmptyWatchers();
        expect(issue.isSubTask()).andReturn(false);
        replayMocks();

        tested.deleteIssue((User) null, issue, EventDispatchOption.ISSUE_DELETED, true);
        makeAssertions();
        verify(customField1, customField2);
        final Map<String,Object> expected = ImmutableMap.<String, Object>of("customfield_10000", "Value1", "customfield_10001", "Value2");
        assertEquals(expected, capturedEventParams.get(IssueEvent.CUSTOM_FIELDS_PARAM_NAME));
        assertTrue(capturedSendMailFlag);
    }

    @Test
    public void shouldAddWatchersParamOnDelete() throws Exception
    {
        setUpEmptyCustomFields();
        IssueDeleteHelper tested = getIssueDeleteHelper();
        expect(mockIssueManager.getWatchers(issue)).andReturn(ImmutableList.<User>of(
                new MockUser("one"),
                new MockUser("two")
        ));
        expect(issue.isSubTask()).andReturn(false);
        replayMocks();

        tested.deleteIssue((User) null, issue, EventDispatchOption.ISSUE_DELETED, true);
        makeAssertions();
        final List<User> expected = ImmutableList.<User>of(new MockUser("one"), new MockUser("two"));
        assertEquals(expected, capturedEventParams.get(IssueEvent.WATCHERS_PARAM_NAME));
        assertTrue(capturedSendMailFlag);
    }

    private DefaultIssueDeleteHelper newMockDeleteIssueHelper(final AtomicInteger deleteIssueCalled)
    {
        return new DefaultIssueDeleteHelper(indexManager, mockSubTaskManager,
                mockIssueLinkManager, mockRemoteIssueLinkManager, mailThreadManager, customFieldManager, attachmentManager, mockIssueManager,
                associationManager, workflowManager, changeHistoryManager, issueEventManager)
        {
            @Override
            public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
                    throws RemoveException
            {
                deleteIssueCalled.incrementAndGet();
            }
        };
    }

    private void makeAssertions() throws GenericEntityException
    {
        verifyMocks();
        assertTrue(issueGenericValue.isRemoved());
    }

    private void verifyMocks()
    {
        verify(associationManager, customFieldManager, attachmentManager, actionDispatcher, mailThreadManager,
                mockSubTaskManager, mockIssueManager, mockIssueLinkManager, mockRemoteIssueLinkManager, workflowManager, indexManager,
                issueEventManager);
    }

    static DbIndependentMockGenericValue createIssueGV(Long id, Long projectId, String summary, String key, String assignee, String resolution)
    {
        return new DbIndependentMockGenericValue("Issue", MapBuilder.<String,Object>newBuilder()
                .add("id", id)
                .add("project", projectId)
                .add("assignee", assignee)
                .add("summary", summary)
                .add("key", key)
                .add("resolution", resolution)
                .toMap()
        );
    }

    private static class DbIndependentMockGenericValue extends MockGenericValue
    {

        public DbIndependentMockGenericValue(String entityName)
        {
            super(entityName);
        }

        public DbIndependentMockGenericValue(String entityName, Map<String,Object> fields)
        {
            super(entityName, fields);
        }

        public DbIndependentMockGenericValue(String entityName, Long id)
        {
            super(entityName, id);
        }

        @Override
        public void store() throws GenericEntityException
        {
            stored = true;
        }

        @Override
        public void remove() throws GenericEntityException
        {
            removed = true;
        }

        @Override
        public void refresh() throws GenericEntityException
        {
            refreshed = true;
        }
    }

}
