package com.atlassian.jira.issue.link;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TestDefaultIssueLinkTypeManager extends LegacyJiraMockTestCase
{
    private DefaultIssueLinkTypeManager diltm = new DefaultIssueLinkTypeManager(new MockOfBizDelegator(null, null), null);
    MockOfBizDelegator delegator;


    public TestDefaultIssueLinkTypeManager(String s)
    {
        super(s);
    }

    public void testCreateIssueLinkTypeNullName() throws GenericEntityException
    {
        _testCreateIssueLinkValidation(null, "test outward", "test inward", "test style", "name should not be null!");
    }

    public void testCreateIssueLinkTypeNullOutward() throws GenericEntityException
    {
        _testCreateIssueLinkValidation("test name", null, "test inward", "test style", "outward should not be null!");
    }

    public void testCreateIssueLinkTypeNullInward() throws GenericEntityException
    {
        _testCreateIssueLinkValidation("test name", "test outward", null, "test style", "inward should not be null!");
    }

    private void _testCreateIssueLinkValidation(String name, String outward, String inward, String style, String errorMessage)
    {
        try
        {
            diltm.createIssueLinkType(name, outward, inward, style);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    public void testCreateIssueLinkType()
    {
        String name = "test name";
        String outward = "test outward";
        String inward = "test inward";
        String style = "test style";
        final Map expectedFields = EasyMap.build("linkname", name, "outward", outward, "inward", inward, "style", style);
        final List startingFields = null;
        setupDatabase(startingFields, EasyList.build(new MockGenericValue("IssueLinkType", expectedFields)));
        diltm.createIssueLinkType(name, outward, inward, style);
    }

    private void setupDatabase(final List startingFields, final List expectedFields)
    {
        delegator = new MockOfBizDelegator(startingFields, expectedFields);
        setupManager(delegator);
    }

    private void setupDatabase(GenericValue start, GenericValue end)
    {
        setupDatabase(EasyList.build(start), end != null ? EasyList.build(end) : null);
    }


    public void testGetIssueLinkType()
    {
        setupDatabase(new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("linkname", "test-link", "id", new Long(11))), null);
        IssueLinkType issueLinkType = diltm.getIssueLinkType(new Long(11));
        assertNotNull(issueLinkType);
        assertEquals("test-link", issueLinkType.getName());
    }

    public void testGetIssueLinkTypes()
    {
        setupDatabase(new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("linkname", "test-link", "id", new Long(11))), null);
        Collection issueLinkTypes = diltm.getIssueLinkTypes();

        assertEquals(1, issueLinkTypes.size());
        IssueLinkType issueLinkType = (IssueLinkType) issueLinkTypes.iterator().next();
        assertEquals("test-link", issueLinkType.getName());
    }

    public void testRemoveIssueLinkType() throws RemoveException, GenericEntityException
    {
        setupDatabase(new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("linkname", "test-link", "id", new Long(11))), null);
        diltm.removeIssueLinkType(new Long(11));
        delegator.verify();

    }

    public void testGetIssueLinkTypeByName()
    {
        setupDatabase(new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("linkname", "test-link", "id", new Long(11))), null);
        final Collection issueLinkTypes = diltm.getIssueLinkTypesByName("test-link");
        assertFalse(issueLinkTypes.isEmpty());
        assertEquals(1, issueLinkTypes.size());
        IssueLinkType issueLinkType = (IssueLinkType) issueLinkTypes.iterator().next();
        assertEquals("test-link", issueLinkType.getName());
    }

    public void testGetIssueLinkTypeByInwardDescription()
    {
        setupDatabase(new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("linkname", "test-link", "id", new Long(11), "inward", "indesc")), null);

        Collection issueLinkTypes = diltm.getIssueLinkTypesByInwardDescription("indesc");
        assertFalse(issueLinkTypes.isEmpty());
        assertEquals(1, issueLinkTypes.size());
        IssueLinkType issueLinkType = (IssueLinkType) issueLinkTypes.iterator().next();
        assertEquals("test-link", issueLinkType.getName());
        assertEquals("indesc", issueLinkType.getInward());

        // case is different - same result
        issueLinkTypes = diltm.getIssueLinkTypesByInwardDescription("INDESC");
        assertFalse(issueLinkTypes.isEmpty());
        assertEquals(1, issueLinkTypes.size());
        issueLinkType = (IssueLinkType) issueLinkTypes.iterator().next();
        assertEquals("test-link", issueLinkType.getName());
        assertEquals("indesc", issueLinkType.getInward());

        issueLinkTypes = diltm.getIssueLinkTypesByInwardDescription("doesntexist");
        assertTrue(issueLinkTypes.isEmpty());
    }

    public void testGetIssueLinkTypeByOutwardDescription()
    {
        setupDatabase(new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("linkname", "test-link", "id", new Long(11), "outward", "outdesc")), null);

        Collection issueLinkTypes = diltm.getIssueLinkTypesByOutwardDescription("outdesc");
        assertFalse(issueLinkTypes.isEmpty());
        assertEquals(1, issueLinkTypes.size());
        IssueLinkType issueLinkType = (IssueLinkType) issueLinkTypes.iterator().next();
        assertEquals("test-link", issueLinkType.getName());
        assertEquals("outdesc", issueLinkType.getOutward());

        // case is different - same result
        issueLinkTypes = diltm.getIssueLinkTypesByOutwardDescription("OUTDESC");
        assertFalse(issueLinkTypes.isEmpty());
        assertEquals(1, issueLinkTypes.size());
        issueLinkType = (IssueLinkType) issueLinkTypes.iterator().next();
        assertEquals("test-link", issueLinkType.getName());
        assertEquals("outdesc", issueLinkType.getOutward());

        issueLinkTypes = diltm.getIssueLinkTypesByOutwardDescription("doesntexist");
        assertTrue(issueLinkTypes.isEmpty());
    }

    public void testGetIssueLinkTypeByStyle()
    {
        setupDatabase(new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("linkname", "test-link", "id", new Long(11))), null);
        final Collection issueLinkTypes = diltm.getIssueLinkTypesByStyle(null);
        assertFalse(issueLinkTypes.isEmpty());
        assertEquals(1, issueLinkTypes.size());
        IssueLinkType issueLinkType = (IssueLinkType) issueLinkTypes.iterator().next();
        assertEquals("test-link", issueLinkType.getName());
    }

    public void testUpdateIssueLinkType() throws GenericEntityException
    {
        final MockGenericValue start = new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("linkname", "test-link", "id", new Long(11)));
        final MockGenericValue end = new MockGenericValue(OfBizDelegator.ISSUE_LINK_TYPE, EasyMap.build("linkname", "test name", "id", new Long(11), "outward", "test outward", "inward", "test inward"));
        setupDatabase(start, end);


        final String name = "test name";
        final String outward = "test outward";
        final String inward = "test inward";
        final IssueLinkType issueLinkType = diltm.getIssueLinkType(new Long(11));
        diltm.updateIssueLinkType(issueLinkType, name, outward, inward);

        assertEquals(name, issueLinkType.getName());
        assertEquals(outward, issueLinkType.getOutward());
        assertEquals(inward, issueLinkType.getInward());
        delegator.verify();

    }

    /*
    public void testInvalidData() throws Exception
    {
        LinkTypeCreate ltc = new LinkTypeCreate();

        String result = ltc.execute();
        assertEquals(Action.ERROR, result);
        assertEquals(3, ltc.getErrorMessages().size());
        assertTrue(ltc.getErrorMessages().contains("Please specify a name for this link type."));
        assertTrue(ltc.getErrorMessages().contains("Please specify a description for the outward link."));
        assertTrue(ltc.getErrorMessages().contains("Please specify a description for the inward link."));
    }

    public void testNameAlreadyExists() throws Exception
    {
        UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "foo"));

        LinkTypeCreate ltc = new LinkTypeCreate();

        ltc.setName("foo");

        String result = ltc.execute();
        assertEquals(Action.ERROR, result);
        assertTrue(ltc.getErrorMessages().contains("Another link type with that name already exists"));
    }

    public void testValidData() throws Exception
    {
        LinkTypeCreate ltc = new LinkTypeCreate();
        ltc.setName("foo");
        ltc.setInward("bar");
        ltc.setOutward("baz");

        String result = ltc.execute();
        checkSuccess(result, ltc);

        assertEquals("foo", ltc.getLinkType().getString("linkname"));
        assertEquals("bar", ltc.getLinkType().getString("inward"));
        assertEquals("baz", ltc.getLinkType().getString("outward"));
    }

    public void testInvalidData() throws Exception
    {
        LinkTypeUpdate ltu = new LinkTypeUpdate();

        String result = ltu.execute();
        assertEquals(Action.ERROR, result);
        assertTrue(ltu.getErrorMessages().contains("No link type provided."));
    }

    public void testValidData() throws Exception
    {
        GenericValue linkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "foo", "inward", "bar", "outward", "baz"));

        LinkTypeUpdate ltu = new LinkTypeUpdate();
        linkType.set("inward", "in");
        linkType.set("outward", "out");

        ltu.setLinktype(linkType);

        String result = ltu.execute();
        checkSuccess(result, ltu);

        GenericValue lookedUp = CoreFactory.getGenericDelegator().findByPrimaryKey("IssueLinkType", EasyMap.build("id", new Long(1)));
        assertEquals(lookedUp, ltu.getLinktype());
    }


    // Link Type Delete
    protected void setUp() throws Exception
    {
        super.setUp();
        (new ApplicationProperties()).setOption(APKeys.JIRA_OPTION_CACHE_ISSUES, true);
        ManagerFactory.addService(CacheManager.class, mdcm);
    }

    protected void tearDown() throws Exception
    {
        ManagerFactory.addService(CacheManager.class, null);
        (new ApplicationProperties()).setOption(APKeys.JIRA_OPTION_CACHE_ISSUES, false);
        super.tearDown();
    }

    public void testInvalidData() throws Exception
    {
        LinkTypeDelete ltd = new LinkTypeDelete();

        String result = ltd.execute();
        assertEquals(Action.ERROR, result);
        assertTrue(ltd.getErrorMessages().contains("No link type provided."));
    }

    public void testRemoveLinkType() throws Exception
    {
        setupValidData();

        LinkTypeDelete ltd = new LinkTypeDelete();

        ltd.setLinktype(linkType1);
        ltd.setSwapLinkType(null);

        String result = ltd.execute();
        checkSuccess(result, ltd);

        assertNull(CoreFactory.getGenericDelegator().findByPrimaryKey("IssueLinkType", EasyMap.build("id", new Long(1))));
        assertNull(CoreFactory.getGenericDelegator().findByPrimaryKey("IssueLink", EasyMap.build("id", new Long(1))));

        Collection flushes = ((MockDefaultCacheManager) ManagerFactory.getCacheManager()).getFlushes();
        assertTrue(UtilsForIssueActionTests.contains(flushes, new Object[] { "flush", CacheManager.ISSUE_CACHE, ManagerFactory.getIssueManager().getIssue(issue1.getLong("id")), null, null }));
        assertTrue(UtilsForIssueActionTests.contains(flushes, new Object[] { "flush", CacheManager.ISSUE_CACHE, ManagerFactory.getIssueManager().getIssue(issue2.getLong("id")), null, null }));
    }

    public void testSwapLinkType() throws Exception
    {
        setupValidData();
        linkType2 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(2), "linkname", "test 2", "inward", "inward 2", "outward", "outward 2"));

        // Make sure there are two link types and that the issue link is associated with link type 1
        assertEquals(2, CoreFactory.getGenericDelegator().findAll("IssueLinkType").size());
        assertEquals(new Long(1), CoreFactory.getGenericDelegator().findByPrimaryKey("IssueLink", EasyMap.build("id", new Long(1))).getLong("linktype"));

        LinkTypeDelete ltd = new LinkTypeDelete();

        ltd.setLinktype(linkType1);
        ltd.setSwapLinkType(linkType2);

        String result = ltd.execute();
        checkSuccess(result, ltd);

        // make sure that the link type 1 has been deleted and the link has been swapped to link type 2
        assertEquals(1, CoreFactory.getGenericDelegator().findAll("IssueLinkType").size());
        assertNull(CoreFactory.getGenericDelegator().findByPrimaryKey("IssueLinkType", EasyMap.build("id", new Long(1))));
        assertEquals(new Long(2), CoreFactory.getGenericDelegator().findByPrimaryKey("IssueLink", EasyMap.build("id", new Long(1))).getLong("linktype"));

        Collection flushes = ((MockDefaultCacheManager) ManagerFactory.getCacheManager()).getFlushes();
        assertTrue(UtilsForIssueActionTests.contains(flushes, new Object[] { "flush", CacheManager.ISSUE_CACHE, ManagerFactory.getIssueManager().getIssue(issue1.getLong("id")), null, null }));
        assertTrue(UtilsForIssueActionTests.contains(flushes, new Object[] { "flush", CacheManager.ISSUE_CACHE, ManagerFactory.getIssueManager().getIssue(issue2.getLong("id")), null, null }));
    }

    private void setupValidData()
    {
        linkType1 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "test 1", "inward", "inward 1", "outward", "outward 1"));
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "project 1"));
        issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(4), "project", project.getLong("id")));
        issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(5), "project", project.getLong("id")));
        link = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linkType1.getLong("id"), "source", issue1.getLong("id"), "destination", issue2.getLong("id")));
    }

    // Link Create
    protected void setUp() throws Exception
    {
        super.setUp();
        (new ApplicationProperties()).setOption(APKeys.JIRA_OPTION_CACHE_ISSUES, true);
        ManagerFactory.addService(CacheManager.class, mdcm);
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "project 1"));
        issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(4), "project", project.getLong("id")));
        issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(5), "project", project.getLong("id")));
        linktype = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "Test Link", "inward", "Inward Link", "outward", "Outward Link"));
        scheme = JiraTestUtil.setupAndAssociateDefaultPermissionScheme(project);
        ManagerFactory.getPermissionManager().addPermission(Permissions.LINK_ISSUE, scheme, null, GroupDropdown.DESC);
    }

    protected void tearDown() throws Exception
    {
        ManagerFactory.addService(CacheManager.class, null);
        (new ApplicationProperties()).setOption(APKeys.JIRA_OPTION_CACHE_ISSUES, false);
        super.tearDown();
    }

    public void testValidation() throws Exception
    {
        LinkCreate linkCreate = new LinkCreate(null);
        String result = linkCreate.execute();
        assertEquals(Action.ERROR, result);
        assertTrue(linkCreate.getErrorMessages().contains("You did not specify an issue."));
        assertTrue(linkCreate.getErrorMessages().contains("You did not specify a destination issue."));
        assertTrue(linkCreate.getErrorMessages().contains("You did not specify an issue type."));
    }

    public void testLinkNoPermissions() throws Exception
    {
        User testUser = UserManager.getInstance().createUser("bob");

        // Removing the global permission should stop bob from creating issue links, because the project
        // has no permission itself.
        ManagerFactory.getPermissionSchemeManager().removeEntities(scheme, new Long(Permissions.LINK_ISSUE));

        LinkCreate linkCreate = new LinkCreate(null);
        linkCreate.setIssue(issue1);
        linkCreate.setDestination(issue2);
        linkCreate.setLinkType(linktype);
        linkCreate.setRemoteUser(testUser);
        try
        {
            linkCreate.execute();
            fail("PermissionException should have been thrown.");
        }
        catch (PermissionException e)
        {
            // Expected result
        }
    }

    public void testLink() throws Exception
    {
        LinkCreate linkCreate = new LinkCreate(null);
        linkCreate.setIssue(issue1);
        linkCreate.setDestination(issue2);
        linkCreate.setLinkType(linktype);

        String result = linkCreate.execute();
        assertEquals(Action.SUCCESS, result);

        Collection flushes = ((MockDefaultCacheManager) ManagerFactory.getCacheManager()).getFlushes();
        assertEquals(5, flushes.size());
        assertTrue(UtilsForIssueActionTests.contains(flushes, new Object[] { "flush", CacheManager.ISSUE_CACHE, ManagerFactory.getIssueManager().getIssue(issue1.getLong("id")), null, null }));
        assertTrue(UtilsForIssueActionTests.contains(flushes, new Object[] { "flush", CacheManager.ISSUE_CACHE, ManagerFactory.getIssueManager().getIssue(issue2.getLong("id")), null, null }));
        assertEquals(1, ManagerFactory.getIssueManager().getEntitiesByIssue(IssueRelationConstants.LINKS_OUTWARD, issue1).size());
        assertEquals(1, ManagerFactory.getIssueManager().getEntitiesByIssue(IssueRelationConstants.LINKS_INWARD, issue2).size());
    }

    // Test Link Delete
    protected void setUp() throws Exception
    {
        super.setUp();
        (new ApplicationProperties()).setOption(APKeys.JIRA_OPTION_CACHE_ISSUES, true);
        ManagerFactory.addService(CacheManager.class, mdcm);
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "project 1"));
        issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(4), "project", project.getLong("id")));
        issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(5), "project", project.getLong("id")));
        linktype = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "Test Link", "inward", "Inward Link", "outward", "Outward Link"));
        link = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linktype.getLong("id"), "source", issue1.getLong("id"), "destination", issue2.getLong("id")));
        scheme = JiraTestUtil.setupAndAssociateDefaultPermissionScheme(project);
        ManagerFactory.getPermissionManager().addPermission(Permissions.LINK_ISSUE, scheme, null, GroupDropdown.DESC);
    }

    protected void tearDown() throws Exception
    {
        ManagerFactory.addService(CacheManager.class, null);
        (new ApplicationProperties()).setOption(APKeys.JIRA_OPTION_CACHE_ISSUES, false);
        super.tearDown();
    }

    public void testValidation() throws Exception
    {
        LinkDelete linkDelete = new LinkDelete(null);
        String result = linkDelete.execute();
        assertEquals(Action.ERROR, result);
        assertTrue(linkDelete.getErrorMessages().contains("You must specify a link to delete."));
    }

    public void testLinkNoPermissions() throws Exception
    {
        User testUser = UserManager.getInstance().createUser("bob");

        // Removing the global permission should stop bob from creating issue links, because the project
        // has no permission itself.
        ManagerFactory.getPermissionSchemeManager().removeEntities(scheme, new Long(Permissions.LINK_ISSUE));

        LinkDelete linkDelete = new LinkDelete(null);
        linkDelete.setLink(link);
        linkDelete.setRemoteUser(testUser);
        try
        {
            linkDelete.execute();
            fail("PermissionException should have been thrown.");
        }
        catch (PermissionException e)
        {
            // Expected result.
        }
    }

    public void testLink() throws Exception
    {
        LinkDelete linkDelete = new LinkDelete(null);
        linkDelete.setLink(link);

        String result = linkDelete.execute();
        assertEquals(Action.SUCCESS, result);

        Collection flushes = ((MockDefaultCacheManager) ManagerFactory.getCacheManager()).getFlushes();

        // UtilsForIssueActionTests.printObjectArray(flushes);
        assertEquals(5, flushes.size());
        assertTrue(UtilsForIssueActionTests.contains(flushes, new Object[] { "flush", CacheManager.ISSUE_CACHE, ManagerFactory.getIssueManager().getIssue(issue1.getLong("id")), null, null }));
        assertTrue(UtilsForIssueActionTests.contains(flushes, new Object[] { "flush", CacheManager.ISSUE_CACHE, ManagerFactory.getIssueManager().getIssue(issue2.getLong("id")), null, null }));
    }
    */

    private void setupManager(OfBizDelegator delegator)
    {
        diltm = new DefaultIssueLinkTypeManager(delegator, null);
    }

}
