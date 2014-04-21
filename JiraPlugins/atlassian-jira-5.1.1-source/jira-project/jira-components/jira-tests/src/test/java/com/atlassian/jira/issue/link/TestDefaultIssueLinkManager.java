package com.atlassian.jira.issue.link;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.index.MemoryIndexManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.mock.user.MockUserHistoryManager;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestDefaultIssueLinkManager extends AbstractUsersTestCase
{
    private DefaultIssueLinkManager dilm;
    private OfBizDelegator genericDelegator;
    private final CollectionReorderer collectionReorderer = new CollectionReorderer();
    private Mock mockIssueManager;
    private Mock mockIssueLinkTypeManager;
    private Mock mockIssueUpdater;
    private MockApplicationProperties applicationProperties;
    private MyIssueLinkTypeManager issueLinkTypeManager;
    private MockIssueManager issueManager;
    private GenericValue sourceIssue;
    private MockIssue sourceIssueObject;
    private GenericValue destinationIssue;
    private MockIssue destinationIssueObject;
    private IssueIndexManager indexManager;
    private FieldVisibilityBean origFieldVisibilityBean;
    private UserHistoryManager userHistoryManager;

    public TestDefaultIssueLinkManager(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        origFieldVisibilityBean = ComponentManager.getComponentInstanceOfType(FieldVisibilityBean.class);
        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String) EasyMock.anyObject(), (Issue) EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);

        genericDelegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());

        // Setup Mocks
        mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
        mockIssueLinkTypeManager = new Mock(IssueLinkTypeManager.class);
        mockIssueLinkTypeManager.setStrict(true);
        mockIssueUpdater = new Mock(IssueUpdater.class);
        mockIssueUpdater.setStrict(true);
        applicationProperties = new MockApplicationProperties();

        sourceIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test source summary", "key", "TST-1", "id", new Long(1)));
        sourceIssueObject = new MockIssue();
        sourceIssueObject.setGenericValue(sourceIssue);

        destinationIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test destination summary", "key", "TST-2", "id", new Long(2)));
        destinationIssueObject = new MockIssue();
        destinationIssueObject.setGenericValue(destinationIssue);

        issueManager = new MockIssueManager();
        issueManager.addIssue(sourceIssue);
        issueManager.addIssue(destinationIssue);

        indexManager = new MemoryIndexManager(issueManager);

        userHistoryManager = new MockUserHistoryManager();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        ManagerFactory.addService(FieldVisibilityBean.class, origFieldVisibilityBean);
    }

    private void setupManager(final IssueLinkTypeManager issueLinkTypeManager, final IssueUpdater issueUpdater)
    {
        dilm = new DefaultIssueLinkManager(genericDelegator, new DefaultIssueLinkCreator(issueLinkTypeManager, issueManager), issueLinkTypeManager,
                collectionReorderer, issueUpdater, indexManager, applicationProperties)
        {
            @Override
            protected void reindexLinkedIssues(final IssueLink issueLink)
            {
                //do nothing - ugly hack, but so is this entire test :)
            }
        };
    }

    public void testCreateIssueLinkSystemLinkTypeCreatesGenericValue() throws CreateException, GenericEntityException
    {
        final Long testSourceId = new Long(0);
        final Long testDestinationId = new Long(1);
        final Long testSequence = new Long(0);
        final Long testLinkType = new Long(11);

        final Map expectedFields = EasyMap.build("linktype", testLinkType, "source", testSourceId, "destination", testDestinationId, "sequence",
                testSequence);
        final GenericValue issueLinkGV = new MockGenericValue("IssueLink", new HashMap(expectedFields));
        final MockOfBizDelegator delegator = new MockOfBizDelegator(null, EasyList.build(issueLinkGV));

        final GenericValue issueLinkTypeGV = new MockGenericValue("IssueLinkType", EasyMap.build("linkname", "test name", "outward", "test outward",
                "inward", "test inward", "style", "jira_test style"));
        final MockIssueManager issueManager = new MockIssueManager();
        issueManager.addIssue(sourceIssue);
        issueManager.addIssue(destinationIssue);

        final IssueLinkTypeManager issueLinkTypeManager = new MyIssueLinkTypeManager()
        {
            @Override
            public IssueLinkType getIssueLinkType(final Long id)
            {
                return new IssueLinkTypeImpl(issueLinkTypeGV);
            }
        };

        final IssueLinkCreator issueLinkCreator = new IssueLinkCreator()
        {
            public IssueLink createIssueLink(final GenericValue issueLinkGV)
            {
                return new IssueLinkImpl(issueLinkGV, issueLinkTypeManager, issueManager);
            }
        };

        dilm = new DefaultIssueLinkManager(delegator, issueLinkCreator, issueLinkTypeManager, collectionReorderer, null, indexManager,
                applicationProperties)
        {
            @Override
            protected void reindexLinkedIssues(final IssueLink issueLink)
            {
                //do nothing - ugly hack, but so is this entire test :)
            }
        };
        dilm.createIssueLink(testSourceId, testDestinationId, testLinkType, testSequence, null);
        delegator.verify();
    }

    public void testCreateIssueLinkNonSystemLinkTypeCreateGVsAndChangeItems()
            throws CreateException, GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final Long testSourceId = new Long(1);
        final Long testDestinationId = new Long(2);
        final Long testSequence = new Long(0);
        final Long testLinkType = new Long(11);

        final Map expectedFields = EasyMap.build("linktype", testLinkType, "source", testSourceId, "destination", testDestinationId, "sequence",
                testSequence);
        final GenericValue issueLinkGV = new MockGenericValue("IssueLink", new HashMap(expectedFields));
        final MockOfBizDelegator delegator = new MockOfBizDelegator(null, EasyList.build(issueLinkGV));

        final GenericValue issueLinkTypeGV = new MockGenericValue("IssueLinkType", EasyMap.build("linkname", "test name", "outward", "test outward",
                "inward", "test inward", "style", null));

        final IssueLinkTypeManager issueLinkTypeManager = new MyIssueLinkTypeManager()
        {
            @Override
            public IssueLinkType getIssueLinkType(final Long id)
            {
                return new IssueLinkTypeImpl(issueLinkTypeGV);
            }
        };

        final GenericValue sourceIssue = new MockGenericValue("Issue", EasyMap.build("summary", "test source summary", "key", "TST-1", "id",
                testSourceId));
        final GenericValue destinationIssue = new MockGenericValue("Issue", EasyMap.build("summary", "test destination summary", "key", "TST-2",
                "id", testDestinationId));

        final MockIssueManager mockIssueManager = new MockIssueManager();
        mockIssueManager.addIssue(sourceIssue);
        mockIssueManager.addIssue(destinationIssue);

        final IssueLinkCreator issueLinkCreator = new IssueLinkCreator()
        {
            public IssueLink createIssueLink(final GenericValue issueLinkGV)
            {
                return new IssueLinkImpl(issueLinkGV, issueLinkTypeManager, mockIssueManager);
            }
        };

        final User testUser = createMockUser("test user");

        final IssueUpdateBean issueUpdateBean1 = new IssueUpdateBean(sourceIssue, sourceIssue, EventType.ISSUE_UPDATED_ID, testUser);
        issueUpdateBean1.setDispatchEvent(false);
        final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null, destinationIssue.getString("key"),
                "This issue " + "test outward" + " " + destinationIssue.getString("key"));
        issueUpdateBean1.setChangeItems(EasyList.build(expectedCib));

        final IssueUpdateBean issueUpdateBean2 = new IssueUpdateBean(destinationIssue, destinationIssue, EventType.ISSUE_UPDATED_ID, testUser);
        final ChangeItemBean expectedCib2 = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null, sourceIssue.getString("key"),
                "This issue " + "test inward" + " " + sourceIssue.getString("key"));
        issueUpdateBean2.setDispatchEvent(false);
        issueUpdateBean2.setChangeItems(EasyList.build(expectedCib2));

        final List issueUpdateBeans = new ArrayList();
        final IssueUpdater updater = new IssueUpdater()
        {
            public void doUpdate(final IssueUpdateBean issueUpdateBean, final boolean generateChangeItems)
            {
                issueUpdateBeans.add(issueUpdateBean);
            }
        };

        dilm = new DefaultIssueLinkManager(delegator, issueLinkCreator, issueLinkTypeManager, collectionReorderer, updater, indexManager,
                applicationProperties);
        dilm.createIssueLink(testSourceId, testDestinationId, testLinkType, testSequence, testUser);
        delegator.verify();
        assertEquals(2, issueUpdateBeans.size());
        assertTrue(issueUpdateBeans.contains(issueUpdateBean1));
        assertTrue(issueUpdateBeans.contains(issueUpdateBean2));
    }

    public void testRemoveIssueLinkSystemLinkType() throws RemoveException, GenericEntityException
    {
        final GenericValue issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("outward", "test out", "inward",
                "test inward", "linkname", "test name", "style", "jira_some system style"));
        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);
        final Long linkTypeId = issueLinkType.getId();
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(linkTypeId)), issueLinkType);
        setupManager((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), null);
        final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", new Long(0), "destination", new Long(1),
                "linktype", linkTypeId));
        final IssueLink issueLink = new IssueLinkImpl(issueLinkGV, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), null);
        dilm.removeIssueLink(issueLink, null);

        final List issueLinkGVs = genericDelegator.findAll("IssueLink");
        assertTrue(issueLinkGVs.isEmpty());
        verifyMocks();
    }

    public void testRemoveIssueLinkNonSystemLinkType()
            throws RemoveException, GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = createMockUser("test user");
        final GenericValue issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "test name", "outward",
                "test outward", "inward", "test inward"));
        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(issueLinkType.getId())), issueLinkType);

        final MyIssueUpdater issueUpdater = new MyIssueUpdater()
        {
            int called = 0;
            int expectedCalled = 2;

            public void doUpdate(final IssueUpdateBean issueUpdateBean, final boolean generateChangeItems)
            {
                if (called == 0)
                {
                    assertFalse(issueUpdateBean.isDispatchEvent());
                    assertEquals(sourceIssue, issueUpdateBean.getOriginalIssue());
                    assertEquals(sourceIssue, issueUpdateBean.getChangedIssue());
                    assertEquals(testUser, issueUpdateBean.getUser());
                    assertEquals(EventType.ISSUE_UPDATED_ID, issueUpdateBean.getEventTypeId());
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", destinationIssue.getString("key"),
                            "This issue " + issueLinkType.getOutward() + " " + destinationIssue.getString("key"), null, null);
                    assertEquals(EasyList.build(expectedCib), issueUpdateBean.getChangeItems());
                    called++;
                }
                else if (called == 1)
                {
                    assertFalse(issueUpdateBean.isDispatchEvent());
                    assertEquals(destinationIssue, issueUpdateBean.getOriginalIssue());
                    assertEquals(destinationIssue, issueUpdateBean.getChangedIssue());
                    assertEquals(testUser, issueUpdateBean.getUser());
                    assertEquals(EventType.ISSUE_UPDATED_ID, issueUpdateBean.getEventTypeId());
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", sourceIssue.getString("key"),
                            "This issue " + issueLinkType.getInward() + " " + sourceIssue.getString("key"), null, null);
                    assertEquals(EasyList.build(expectedCib), issueUpdateBean.getChangeItems());
                    called++;
                }
                else
                {
                    fail("doUpdate called " + ++called + " times.");
                }
            }

            public void verify()
            {
                if (called != expectedCalled)
                {
                    fail("doUpdate was called '" + called + " times instead of " + expectedCalled + ".");
                }
            }
        };

        final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId()));
        final IssueLink issueLink = new IssueLinkImpl(issueLinkGV, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), issueManager);

        setupManager((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), issueUpdater);

        dilm.removeIssueLink(issueLink, testUser);
        issueUpdater.verify();
        verifyMocks();
    }

    public void testRemoveIssueLinks() throws RemoveException, GenericEntityException
    {
        // Setup system link type
        final GenericValue issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("outward", "test out", "inward",
                "test inward", "linkname", "test name", "style", "jira_some system style"));
        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(issueLinkType.getId())), issueLinkType);
        setupManager((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), null);

        // Create issue links - one with the issue as source the other as destination
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", sourceIssue.getLong("id"), "destination", new Long(999), "linktype",
                issueLinkType.getId()));
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", new Long(7654), "destination", sourceIssue.getLong("id"), "linktype",
                issueLinkType.getId()));

        dilm.removeIssueLinks(sourceIssue, null);

        final List issueLinkGVs = genericDelegator.findAll("IssueLink");
        assertTrue(issueLinkGVs.isEmpty());
        verifyMocks();
    }

    public void testGetOutwardLinks() throws GenericEntityException
    {
        final List issueLinkTypes = setupIssueLinkTypes(false);

        final List expectedLinks = setupLinks(sourceIssue, destinationIssue, issueLinkTypes);

        setupManager(null, null);
        final List outwardLinks = dilm.getOutwardLinks(sourceIssue.getLong("id"));

        // Copare the contents of the list (order does not matter)
        assertEquals(2, outwardLinks.size());
        assertTrue(outwardLinks.contains(expectedLinks.get(0)));
        assertTrue(outwardLinks.contains(expectedLinks.get(1)));
        verifyMocks();
    }

    public void testGetInwardLinks() throws GenericEntityException
    {
        final List issueLinkTypes = setupIssueLinkTypes(false);
        final List expectedLinks = setupLinks(sourceIssue, destinationIssue, issueLinkTypes);

        setupManager(null, null);
        final List inwardLinks = dilm.getInwardLinks(destinationIssue.getLong("id"));

        // Copare the contents of the list (order does not matter)
        assertEquals(2, inwardLinks.size());
        assertTrue(inwardLinks.contains(expectedLinks.get(0)));
        assertTrue(inwardLinks.contains(expectedLinks.get(1)));
        verifyMocks();
    }

    public void testGetLinkCollection() throws GenericEntityException
    {
        // Mock out permission manager
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(destinationIssueObject),
                new IsNull()), Boolean.TRUE);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final List expectedIssueLinkTypes = setupIssueLinkTypes(false);
        // Setup links one way
        setupLinks(sourceIssue, destinationIssue, expectedIssueLinkTypes);
        // Setup links the other way
        setupLinks(destinationIssue, sourceIssue, expectedIssueLinkTypes);

        setupManager(issueLinkTypeManager, null);
        mockIssueManager.expectAndReturn("getIssue", P.ANY_ARGS, null);
        final LinkCollection linkCollection = dilm.getLinkCollection(sourceIssueObject, null, true);

        final Set resultLinkTypes = linkCollection.getLinkTypes();
        assertEquals(2, resultLinkTypes.size());

        // Iterate over the expected link types
        for (final Iterator iterator = expectedIssueLinkTypes.iterator(); iterator.hasNext();)
        {
            final IssueLinkType issueLinkType = (IssueLinkType) iterator.next();
            assertTrue(resultLinkTypes.contains(issueLinkType));

            // Test Outward issues
            final List resultOutwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
            assertEquals(1, resultOutwardIssues.size());
            final Issue outwardIssue = (Issue) resultOutwardIssues.get(0);
            assertEquals(destinationIssue.getLong("id"), outwardIssue.getGenericValue().getLong("id"));

            // Test Inward Links
            final List resultInwardIssues = linkCollection.getInwardIssues(issueLinkType.getName());
            assertEquals(1, resultInwardIssues.size());
            final Issue inwardIssue = (Issue) resultInwardIssues.get(0);
            assertEquals(destinationIssue.getLong("id"), inwardIssue.getGenericValue().getLong("id"));
        }

        mockPermissionManager.verify();
        verifyMocks();
    }

    public void testGetLinkCollectionIssueObject() throws GenericEntityException
    {
        // Mock out permission manager
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsEqual(destinationIssueObject),
                new IsNull()), Boolean.TRUE);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final List expectedIssueLinkTypes = setupIssueLinkTypes(false);
        // Setup links one way
        setupLinks(sourceIssue, destinationIssue, expectedIssueLinkTypes);
        // Setup links the other way
        setupLinks(destinationIssue, sourceIssue, expectedIssueLinkTypes);

        setupManager(issueLinkTypeManager, null);
        mockIssueManager.expectAndReturn("getIssue", P.ANY_ARGS, null);
        final LinkCollection linkCollection = dilm.getLinkCollection(sourceIssueObject, null);

        final Set resultLinkTypes = linkCollection.getLinkTypes();
        assertEquals(2, resultLinkTypes.size());

        // Iterate over the expected link types
        for (final Iterator iterator = expectedIssueLinkTypes.iterator(); iterator.hasNext();)
        {
            final IssueLinkType issueLinkType = (IssueLinkType) iterator.next();
            assertTrue(resultLinkTypes.contains(issueLinkType));

            // Test Outward issues
            final List resultOutwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
            assertEquals(1, resultOutwardIssues.size());
            final Issue outwardIssue = (Issue) resultOutwardIssues.get(0);
            assertEquals(destinationIssue.getLong("id"), outwardIssue.getGenericValue().getLong("id"));

            // Test Inward Links
            final List resultInwardIssues = linkCollection.getInwardIssues(issueLinkType.getName());
            assertEquals(1, resultInwardIssues.size());
            final Issue inwardIssue = (Issue) resultInwardIssues.get(0);
            assertEquals(destinationIssue.getLong("id"), inwardIssue.getGenericValue().getLong("id"));
        }

        mockPermissionManager.verify();
        verifyMocks();
    }

    public void testGetLinkCollectionIssueObjectOverrideSecurity() throws GenericEntityException
    {
        // Mock out permission manager
        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final List expectedIssueLinkTypes = setupIssueLinkTypes(false);
        // Setup links one way
        setupLinks(sourceIssue, destinationIssue, expectedIssueLinkTypes);
        // Setup links the other way
        setupLinks(destinationIssue, sourceIssue, expectedIssueLinkTypes);

        setupManager(issueLinkTypeManager, null);
        mockIssueManager.expectAndReturn("getIssue", P.ANY_ARGS, null);
        final LinkCollection linkCollection = dilm.getLinkCollectionOverrideSecurity(sourceIssueObject);

        final Set resultLinkTypes = linkCollection.getLinkTypes();
        assertEquals(2, resultLinkTypes.size());

        // Iterate over the expected link types
        for (final Iterator iterator = expectedIssueLinkTypes.iterator(); iterator.hasNext();)
        {
            final IssueLinkType issueLinkType = (IssueLinkType) iterator.next();
            assertTrue(resultLinkTypes.contains(issueLinkType));

            // Test Outward issues
            final List resultOutwardIssues = linkCollection.getOutwardIssues(issueLinkType.getName());
            assertEquals(1, resultOutwardIssues.size());
            final Issue outwardIssue = (Issue) resultOutwardIssues.get(0);
            assertEquals(destinationIssue.getLong("id"), outwardIssue.getGenericValue().getLong("id"));

            // Test Inward Links
            final List resultInwardIssues = linkCollection.getInwardIssues(issueLinkType.getName());
            assertEquals(1, resultInwardIssues.size());
            final Issue inwardIssue = (Issue) resultInwardIssues.get(0);
            assertEquals(destinationIssue.getLong("id"), inwardIssue.getGenericValue().getLong("id"));
        }

        mockPermissionManager.verify();
        verifyMocks();
    }

    public void testMoveIssueLink() throws GenericEntityException
    {
        final List issueLinks = setupIssueLinkSequence();

        final List expecteIssueLinks = new ArrayList(issueLinks);

        setupManager(null, null);
        dilm.moveIssueLink(issueLinks, new Long(0), new Long(2));

        // Ensure the sequences are reset
        List results = genericDelegator.findByAnd("IssueLink", EasyMap.build("id", ((IssueLink) expecteIssueLinks.get(0)).getId()));
        assertEquals(1, results.size());
        GenericValue result = (GenericValue) results.get(0);
        assertEquals(new Long(2), result.getLong("sequence"));

        results = genericDelegator.findByAnd("IssueLink", EasyMap.build("id", ((IssueLink) expecteIssueLinks.get(1)).getId()));
        assertEquals(1, results.size());
        result = (GenericValue) results.get(0);
        assertEquals(new Long(0), result.getLong("sequence"));

        results = genericDelegator.findByAnd("IssueLink", EasyMap.build("id", ((IssueLink) expecteIssueLinks.get(2)).getId()));
        assertEquals(1, results.size());
        result = (GenericValue) results.get(0);
        assertEquals(new Long(1), result.getLong("sequence"));
        verifyMocks();
    }

    private List setupIssueLinkSequence()
    {
        final GenericValue issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("outward", "test outward", "inward",
                "test inward", "linkname", "test name"));

        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);

        final GenericValue issueLinkGV1 = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId(), "sequence", new Long(0)));
        final GenericValue issueLinkGV2 = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId(), "sequence", new Long(1)));
        final GenericValue issueLinkGV3 = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId(), "sequence", new Long(2)));

        final List issueLinks = new ArrayList();

        issueLinks.add(new IssueLinkImpl(issueLinkGV1, null, null));
        issueLinks.add(new IssueLinkImpl(issueLinkGV2, null, null));
        issueLinks.add(new IssueLinkImpl(issueLinkGV3, null, null));
        return issueLinks;
    }

    public void testResetSequences() throws GenericEntityException
    {
        final List issueLinks = setupIssueLinkSequence();
        final List expecteIssueLinks = new ArrayList(issueLinks);
        issueLinks.remove(1);

        setupManager(null, null);
        dilm.resetSequences(issueLinks);

        // Ensure the sequences are reset
        List results = genericDelegator.findByAnd("IssueLink", EasyMap.build("id", ((IssueLink) expecteIssueLinks.get(0)).getId()));
        assertEquals(1, results.size());
        GenericValue result = (GenericValue) results.get(0);
        assertEquals(new Long(0), result.getLong("sequence"));

        results = genericDelegator.findByAnd("IssueLink", EasyMap.build("id", ((IssueLink) expecteIssueLinks.get(2)).getId()));
        assertEquals(1, results.size());
        result = (GenericValue) results.get(0);
        assertEquals(new Long(1), result.getLong("sequence"));

        verifyMocks();
    }

    public void testGetIssueLink()
    {
        final List expectedIssueLinks = setupIssueLinkSequence();
        setupManager(null, null);

        final IssueLink expectedIssueLink = (IssueLink) expectedIssueLinks.get(0);
        final IssueLink result = dilm.getIssueLink(expectedIssueLink.getSourceId(), expectedIssueLink.getDestinationId(),
                expectedIssueLink.getLinkTypeId());

        // As we do not care about which actual link we get as long as the source, destination and link type id are the same just test them
        assertEquals(expectedIssueLink.getSourceId(), result.getSourceId());
        assertEquals(expectedIssueLink.getDestinationId(), result.getDestinationId());
        assertEquals(expectedIssueLink.getLinkTypeId(), result.getLinkTypeId());
        verifyMocks();
    }

    public void testChangeIssueLinkTypeNonSystemLink()
            throws RemoveException, GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = createMockUser("test use");

        final List issueLinkTypes = setupIssueLinkTypes(false);
        final IssueLinkType issueLinkType = (IssueLinkType) issueLinkTypes.get(0);

        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(issueLinkType.getId())), issueLinkType);
        final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId()));
        final IssueLink issueLink = new IssueLinkImpl(issueLinkGV, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), issueManager);
        final IssueLinkType swapIssueLinkType = (IssueLinkType) issueLinkTypes.get(1);

        final MyIssueUpdater issueUpdater = new MyIssueUpdater()
        {
            int called = 0;
            int expectedCalled = 4;

            public void doUpdate(final IssueUpdateBean issueUpdateBean, final boolean generateChangeItems)
            {
                if (called == 0)
                {
                    assertFalse(issueUpdateBean.isDispatchEvent());
                    assertEquals(sourceIssue, issueUpdateBean.getOriginalIssue());
                    assertEquals(sourceIssue, issueUpdateBean.getChangedIssue());
                    assertEquals(testUser, issueUpdateBean.getUser());
                    assertEquals(EventType.ISSUE_UPDATED_ID, issueUpdateBean.getEventTypeId());
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", destinationIssue.getString("key"),
                            "This issue " + issueLinkType.getOutward() + " " + destinationIssue.getString("key"), null, null);
                    assertEquals(EasyList.build(expectedCib), issueUpdateBean.getChangeItems());
                    called++;
                }
                else if (called == 1)
                {
                    assertFalse(issueUpdateBean.isDispatchEvent());
                    assertEquals(destinationIssue, issueUpdateBean.getOriginalIssue());
                    assertEquals(destinationIssue, issueUpdateBean.getChangedIssue());
                    assertEquals(testUser, issueUpdateBean.getUser());
                    assertEquals(EventType.ISSUE_UPDATED_ID, issueUpdateBean.getEventTypeId());
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", sourceIssue.getString("key"),
                            "This issue " + issueLinkType.getInward() + " " + sourceIssue.getString("key"), null, null);
                    assertEquals(EasyList.build(expectedCib), issueUpdateBean.getChangeItems());
                    called++;
                }
                else if (called == 2)
                {
                    assertFalse(issueUpdateBean.isDispatchEvent());
                    assertEquals(sourceIssue, issueUpdateBean.getOriginalIssue());
                    assertEquals(sourceIssue, issueUpdateBean.getChangedIssue());
                    assertEquals(testUser, issueUpdateBean.getUser());
                    assertEquals(EventType.ISSUE_UPDATED_ID, issueUpdateBean.getEventTypeId());
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null,
                            destinationIssue.getString("key"), "This issue " + swapIssueLinkType.getOutward() + " " + destinationIssue.getString("key"));
                    assertEquals(EasyList.build(expectedCib), issueUpdateBean.getChangeItems());
                    called++;
                }
                else if (called == 3)
                {
                    assertFalse(issueUpdateBean.isDispatchEvent());
                    assertEquals(destinationIssue, issueUpdateBean.getOriginalIssue());
                    assertEquals(destinationIssue, issueUpdateBean.getChangedIssue());
                    assertEquals(testUser, issueUpdateBean.getUser());
                    assertEquals(EventType.ISSUE_UPDATED_ID, issueUpdateBean.getEventTypeId());
                    final ChangeItemBean expectedCib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Link", null, null,
                            sourceIssue.getString("key"), "This issue " + swapIssueLinkType.getInward() + " " + sourceIssue.getString("key"));
                    assertEquals(EasyList.build(expectedCib), issueUpdateBean.getChangeItems());
                    called++;
                }
                else
                {
                    fail("doUpdate called " + ++called + " times.");
                }
            }

            public void verify()
            {
                if (called != expectedCalled)
                {
                    fail("doUpdate was called '" + called + " times instead of " + expectedCalled + ".");
                }
            }
        };

        setupManager(null, issueUpdater);
        dilm.changeIssueLinkType(issueLink, swapIssueLinkType, testUser);

        final List results = genericDelegator.findByAnd("IssueLink", EasyMap.build("id", issueLink.getId()));
        assertEquals(1, results.size());
        final GenericValue result = (GenericValue) results.get(0);
        assertEquals(swapIssueLinkType.getId(), result.getLong("linktype"));

        issueUpdater.verify();
        verifyMocks();
    }

    public void testChangeIssueLinkTypeSystemLink()
            throws RemoveException, GenericEntityException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final User testUser = createMockUser("test use");

        final List issueLinkTypes = setupIssueLinkTypes(true);
        final IssueLinkType issueLinkType = (IssueLinkType) issueLinkTypes.get(0);

        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(issueLinkType.getId())), issueLinkType);
        final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", sourceIssue.getLong("id"), "destination",
                destinationIssue.getLong("id"), "linktype", issueLinkType.getId()));
        final IssueLink issueLink = new IssueLinkImpl(issueLinkGV, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), issueManager);
        final IssueLinkType swapIssueLinkType = (IssueLinkType) issueLinkTypes.get(1);

        setupManager(null, null);
        dilm.changeIssueLinkType(issueLink, swapIssueLinkType, testUser);

        final List results = genericDelegator.findByAnd("IssueLink", EasyMap.build("id", issueLink.getId()));
        assertEquals(1, results.size());
        final GenericValue result = (GenericValue) results.get(0);
        assertEquals(swapIssueLinkType.getId(), result.getLong("linktype"));

        verifyMocks();
    }

    private List setupIssueLinkTypes(final boolean system) throws GenericEntityException
    {
        // Setup system link type
        final GenericValue issueLinkTypeGV1 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("outward", "test outward", "inward",
                "test inward", "linkname", "test name"));
        final GenericValue issueLinkTypeGV2 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("outward", "out test", "inward", "in test",
                "linkname", "another test name"));

        if (system)
        {
            issueLinkTypeGV1.set("style", "jira_some system style");
            issueLinkTypeGV1.store();
            issueLinkTypeGV2.set("style", "jira_some system style");
            issueLinkTypeGV2.store();
        }

        final List issueLinkTypes = new ArrayList();
        issueLinkTypes.add(new IssueLinkTypeImpl(issueLinkTypeGV1));
        issueLinkTypes.add(new IssueLinkTypeImpl(issueLinkTypeGV2));
        return issueLinkTypes;
    }

    private List setupLinks(final GenericValue sourceIssue, final GenericValue destinationIssue, final List linkTypes)
    {
        issueLinkTypeManager = new MyIssueLinkTypeManager()
        {
            public IssueLinkType getIssueLinkType(final Long id, boolean includeSystem)
            {
                for (final Iterator iterator = linkTypes.iterator(); iterator.hasNext();)
                {
                    final IssueLinkType issueLinkType = (IssueLinkType) iterator.next();
                    if (issueLinkType.getId().equals(id))
                    {
                        return issueLinkType;
                    }
                }

                fail("Invalid sourceIssue link type id '" + id + "'.");
                return null;
            }
        };

        MyIssueManager issueManager = new MyIssueManager()
        {
            public GenericValue getIssue(final Long id) throws DataAccessException
            {
                if (id.equals(sourceIssue.getLong("id")))
                {
                    return sourceIssue;
                }
                else if (id.equals(destinationIssue.getLong("id")))
                {
                    return destinationIssue;
                }
                else
                {
                    fail("Invalid issue id '" + id + "'.");
                    return null;
                }
            }

            public MutableIssue getIssueObject(final Long id) throws DataAccessException
            {
                if (id.equals(sourceIssue.getLong("id")))
                {
                    final MockIssue mockIssue = new MockIssue();
                    mockIssue.setGenericValue(sourceIssue);
                    return mockIssue;
                }
                else if (id.equals(destinationIssue.getLong("id")))
                {
                    final MockIssue mockIssue = new MockIssue();
                    mockIssue.setGenericValue(destinationIssue);
                    return mockIssue;
                }
                else
                {
                    fail("Invalid issue id '" + id + "'.");
                    return null;
                }
            }

            @Override
            public List<Issue> getIssueObjects(Collection<Long> ids)
            {
                 throw new UnsupportedOperationException("Not implemented.");
            }

            @Override
            public List<Issue> getIssueObjectsByEntity(String relationName, GenericValue entity)
                    throws GenericEntityException
            {
                 throw new UnsupportedOperationException("Not implemented.");
            }

            @Override
            public Issue createIssueObject(String remoteUserName, Map<String, Object> fields) throws CreateException
            {
                 throw new UnsupportedOperationException("Not implemented.");
            }

            @Override
            public Issue createIssueObject(User remoteUser, Map<String, Object> fields) throws CreateException
            {
                throw new UnsupportedOperationException("Not implemented.");
            }

            @Override
            public Issue createIssueObject(User remoteUser, Issue issue) throws CreateException
            {
                 throw new UnsupportedOperationException("Not implemented.");
            }

            public boolean isEditable(final Issue issue)
            {
                throw new UnsupportedOperationException("Not implemented.");
            }
        };

        // Create a link from sourceIssue to destinationIssue for every passed issue link type
        final List issueLinks = new ArrayList(linkTypes.size());
        for (final Iterator iterator = linkTypes.iterator(); iterator.hasNext();)
        {
            final IssueLinkType issueLinkType = (IssueLinkType) iterator.next();
            final GenericValue issueLinkGV = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", sourceIssue.getLong("id"),
                    "destination", destinationIssue.getLong("id"), "linktype", issueLinkType.getId()));
            issueLinks.add(new IssueLinkImpl(issueLinkGV, issueLinkTypeManager, issueManager));
        }

        return issueLinks;
    }

    private void verifyMocks()
    {
        mockIssueLinkTypeManager.verify();
        mockIssueUpdater.verify();
    }

}

class MyIssueManager implements IssueManager
{
    public GenericValue getIssue(final Long id) throws DataAccessException
    {
        return null;
    }

    public GenericValue getIssueByWorkflow(final Long wfid) throws GenericEntityException
    {
        return null;
    }

    public MutableIssue getIssueObjectByWorkflow(Long workflowId) throws GenericEntityException
    {
        return null;
    }

    public MutableIssue getIssueObject(final String key) throws DataAccessException
    {
        return null;
    }

    public List<GenericValue> getIssues(final Collection<Long> ids)
    {
        return null;
    }

    @Override
    public List<Issue> getIssueObjects(Collection<Long> ids)
    {
        return null;
    }

    public MutableIssue getIssueObject(final Long id) throws DataAccessException
    {
        return null;
    }

    public GenericValue getIssue(final String key) throws GenericEntityException
    {
        return null;
    }

    @Override
    public List getVotedIssues(final User user) throws GenericEntityException
    {
        return null;
    }

    @Override
    public List<Issue> getVotedIssuesOverrideSecurity(final User user) throws GenericEntityException
    {
        return null;
    }

    @Override
    public List<User> getWatchers(Issue issue)
    {
        return null;
    }

    @Override
    public List<Issue> getWatchedIssues(final User user)
    {
        return null;
    }

    @Override
    public List<Issue> getWatchedIssuesOverrideSecurity(final User user)
    {
        return null;
    }

    public List execute(final SearchRequest searchRequest, final User searcher) throws SearchException
    {
        return null;
    }

    public List getEntitiesByIssue(final String relationName, final GenericValue issue) throws GenericEntityException
    {
        return null;
    }

    public List getEntitiesByIssueObject(final String relationName, final Issue issue) throws GenericEntityException
    {
        return null;
    }

    public List getIssuesByEntity(final String relationName, final GenericValue entity) throws GenericEntityException
    {
        return null;
    }

    @Override
    public List<Issue> getIssueObjectsByEntity(String relationName, GenericValue entity)
            throws GenericEntityException
    {
        return null;
    }

    @Override
    public GenericValue createIssue(final String remoteUserName, final Map<String, Object> fields)
            throws CreateException
    {
        return null;
    }

    @Override
    public Issue createIssueObject(String remoteUserName, Map<String, Object> fields) throws CreateException
    {
        return null;
    }

    @Override
    public GenericValue createIssue(final User remoteUser, final Map<String, Object> fields) throws CreateException
    {
        return null;
    }

    @Override
    public Issue createIssueObject(User remoteUser, Map<String, Object> fields) throws CreateException
    {
        return null;
    }

    @Override
    public GenericValue createIssue(final User remoteUser, final Issue issue) throws CreateException
    {
        return null;
    }

    @Override
    public Issue createIssueObject(User remoteUser, Issue issue) throws CreateException
    {
        return null;
    }

    @Override
    public Issue updateIssue(final User user, final MutableIssue issue, final EventDispatchOption eventDispatchOption, final boolean sendMail)
    {
        return null;
    }

    @Override
    public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
    }

    @Override
    public void deleteIssue(final User user, final MutableIssue issue, final EventDispatchOption eventDispatchOption, final boolean sendMail)
            throws RemoveException
    {
    }

    @Override
    public void deleteIssueNoEvent(Issue issue) throws RemoveException
    {
    }

    @Override
    public void deleteIssueNoEvent(MutableIssue issue) throws RemoveException
    {
    }

    public List getProjectIssues(final GenericValue project) throws GenericEntityException
    {
        return null;
    }

    public boolean isEditable(final Issue issue)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public boolean isEditable(final Issue issue, final User user)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public Collection getIssueIdsForProject(final Long projectId) throws GenericEntityException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public long getIssueCountForProject(final Long projectId)
    {
        return 0;
    }

    @Override
    public boolean hasUnassignedIssues()
    {
        return false;
    }

    @Override
    public long getUnassignedIssueCount()
    {
        return 0;
    }
}

abstract class MyIssueUpdater implements IssueUpdater
{
    abstract public void verify();
}

class MyIssueLinkTypeManager implements IssueLinkTypeManager
{
    // Issue Link Types
    public void createIssueLinkType(final String name, final String outward, final String inward, final String style)
    {
    }

    public void updateIssueLinkType(final IssueLinkType issueLinkType, final String name, final String outward, final String inward)
    {
    }

    public void removeIssueLinkType(final Long issueLinkTypeId)
    {
    }

    public Collection getIssueLinkTypes()
    {
        return null;
    }

    public IssueLinkType getIssueLinkType(final Long id)
    {
        return null;
    }

    public Collection getIssueLinkTypesByName(final String name)
    {
        return null;
    }

    public Collection<IssueLinkType> getIssueLinkTypesByInwardDescription(final String desc)
    {
        return null;
    }

    public Collection<IssueLinkType> getIssueLinkTypesByOutwardDescription(final String desc)
    {
        return null;
    }

    public Collection getIssueLinkTypesByStyle(final String style)
    {
        return null;
    }

    @Override
    public IssueLinkType getIssueLinkType(Long id, boolean excludeSystemLinks)
    {
        return null;
    }

    @Override
    public Collection<IssueLinkType> getIssueLinkTypes(boolean excludeSystemLinks)
    {
        return null;
    }
}
