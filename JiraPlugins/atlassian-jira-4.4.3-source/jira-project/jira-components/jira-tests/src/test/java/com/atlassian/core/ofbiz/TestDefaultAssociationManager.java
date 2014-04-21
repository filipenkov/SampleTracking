package com.atlassian.core.ofbiz;

import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.ofbiz.association.DefaultAssociationManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;

import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.OSUserConverter;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import com.opensymphony.user.User;

import java.util.Collection;
import java.util.List;

public class TestDefaultAssociationManager extends AbstractOFBizTestCase
{
    private DefaultAssociationManager dam;
    private GenericValue entity1;
    private GenericValue entity2;
    private GenericValue entity3;
    private GenericValue entity4;
    private GenericValue entity5;

    private User user;

    @Before
    public void setUp() throws Exception
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();
        user = OSUserConverter.convertToOSUser(new MockUser("admin"));

        dam = new DefaultAssociationManager(CoreFactory.getGenericDelegator());
        entity1 = UtilsForTests.getTestEntity("Issue", UtilMisc.toMap("id", new Long(1)));
        entity2 = com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity("Issue", UtilMisc.toMap("id", new Long(2)));
        entity3 = com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity("Issue", UtilMisc.toMap("id", new Long(3)));
        entity4 = com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity("Issue", UtilMisc.toMap("id", new Long(4)));
        entity5 = com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity("Issue", UtilMisc.toMap("id", new Long(5)));
    }

    @After
    public void tearDown() throws Exception
    {
        dam = null;
        entity1 = null;
        entity2 = null;
        entity3 = null;
        entity4 = null;
        entity5 = null;
        UtilsForTestSetup.deleteAllEntities();
//        UtilsForTests.cleanUsers();
    }

    // test create node association
    @Test
    public void testCreateAssociation() throws GenericEntityException
    {
        Collection associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertTrue(associations.isEmpty());
        dam.createAssociation(entity3, entity1, "IssueComponent");
        associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(1, associations.size());
        doCheckSingleAssociation((GenericValue) associations.iterator().next(), new Long(3), new Long(1));
    }

    @Test
    public void testCreateAssociationExisting() throws GenericEntityException
    {
        dam.createAssociation(entity3, entity1, "IssueComponent");
        Collection associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(1, associations.size());
        doCheckSingleAssociation((GenericValue) associations.iterator().next(), new Long(3), new Long(1));
        dam.createAssociation(entity3, entity1, "IssueComponent");
        associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(1, associations.size());
        doCheckSingleAssociation((GenericValue) associations.iterator().next(), new Long(3), new Long(1));
    }

    // test get assocation
    @Test
    public void testGetAssociation() throws GenericEntityException
    {
        dam.createAssociation(entity3, entity1, "IssueComponent");
        final GenericValue association = dam.getAssociation(entity3, entity1, "IssueComponent");
        final Collection associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        UtilsForTests.checkSingleElementCollection(associations, association);
    }

    // test remove node association
    @Test
    public void testRemoveAssociation() throws GenericEntityException
    {
        dam.createAssociation(entity3, entity1, "IssueComponent");
        Collection associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(1, associations.size());
        doCheckSingleAssociation((GenericValue) associations.iterator().next(), new Long(3), new Long(1));
        dam.removeAssociation(entity3, entity1, "IssueComponent");
        associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(0, associations.size());
    }

    @Test
    public void testRemoveAssociationsFromSource() throws GenericEntityException
    {
        dam.createAssociation(entity3, entity1, "IssueComponent");
        dam.createAssociation(entity3, entity2, "IssueComponent");
        dam.createAssociation(entity4, entity1, "IssueComponent");
        Collection associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(3, associations.size());
        dam.removeAssociationsFromSource(entity3);
        associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(1, associations.size());
        doCheckSingleAssociation((GenericValue) associations.iterator().next(), new Long(4), new Long(1));
    }

    @Test
    public void testRemoveAssociationsFromSink() throws GenericEntityException
    {
        dam.createAssociation(entity3, entity1, "IssueComponent");
        dam.createAssociation(entity4, entity1, "IssueComponent");
        dam.createAssociation(entity3, entity2, "IssueComponent");
        Collection associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(3, associations.size());
        dam.removeAssociationsFromSink(entity1);
        associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(1, associations.size());
        doCheckSingleAssociation((GenericValue) associations.iterator().next(), new Long(3), new Long(2));
    }

    // test swap association
    @Test
    public void testSwapAssociation() throws GenericEntityException
    {
        dam.createAssociation(entity3, entity1, "IssueComponent");
        dam.createAssociation(entity4, entity1, "IssueComponent");
        Collection associations = CoreFactory.getGenericDelegator().findAll("NodeAssociation");
        assertEquals(2, associations.size());
        dam.swapAssociation("Issue", "IssueComponent", entity1, entity2);
        associations = CoreFactory.getGenericDelegator().findByAnd("NodeAssociation", UtilMisc.toMap("sinkNodeId", new Long(2)));
        assertEquals(2, associations.size());
        associations = CoreFactory.getGenericDelegator().findByAnd("NodeAssociation", UtilMisc.toMap("sinkNodeId", new Long(1)));
        assertEquals(0, associations.size());
    }

    @Test
    public void testSwapAssociationDifferentTypes() throws GenericEntityException
    {
        dam.createAssociation(entity3, entity1, "a");
        dam.createAssociation(entity4, entity1, "b");

        dam.swapAssociation("Issue", "a", entity1, entity2);
        assertNotNull(CoreFactory.getAssociationManager().getAssociation(entity4, entity1, "b"));
    }

    @Test
    public void testSwapAssociationGivenList() throws Exception
    {
        dam.createAssociation(entity2, entity1, "a");
        dam.createAssociation(entity3, entity1, "a");
        dam.createAssociation(entity4, entity1, "a");

        Collection associations = CoreFactory.getGenericDelegator().findByAnd("NodeAssociation",
            UtilMisc.toMap("sinkNodeId", new Long(5), "associationType", "a"));
        assertEquals(0, associations.size());

        dam.swapAssociation(UtilMisc.toList(entity2, entity3), "a", entity1, entity5);
        associations = CoreFactory.getGenericDelegator().findByAnd("NodeAssociation",
            UtilMisc.toMap("sinkNodeId", new Long(5), "associationType", "a"));
        assertEquals(2, associations.size());
    }

    protected void doCheckSingleAssociation(final GenericValue association, final Long sourceId, final Long sinkId)
    {
        assertEquals(sourceId, association.get("sourceNodeId"));
        assertEquals(sinkId, association.get("sinkNodeId"));
        assertEquals("IssueComponent", association.get("associationType"));
        assertEquals("Issue", association.get("sinkNodeEntity"));
        assertEquals("Issue", association.get("sourceNodeEntity"));
    }

    @Test
    public void testGetSinkIdsFromUser() throws Exception
    {

        final String entityType1 = "TestEntity";
        final String entityType2 = "TestEntity2";
        final GenericValue ent1 = new MockGenericValue(entityType1, UtilMisc.toMap("id", new Long(1)));
        final GenericValue ent2 = new MockGenericValue(entityType1, UtilMisc.toMap("id", new Long(2)));
        final GenericValue ent3 = new MockGenericValue(entityType2, UtilMisc.toMap("id", new Long(3)));

        dam.createAssociation(user, ent1, "favourite");
        dam.createAssociation(user, ent2, "favourite");
        dam.createAssociation(user, ent3, "favourite");

        List result = dam.getSinkIdsFromUser(user, entityType1, "favourite", false);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(new Long(1)));
        assertTrue(result.contains(new Long(2)));

        result = dam.getSinkIdsFromUser(user, entityType2, "favourite", false);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(new Long(3)));

        try
        {
            dam.getSinkIdsFromUser(null, entityType1, "favourite", false);
            fail("should have thrown IllegalArgument");
        }
        catch (final IllegalArgumentException e)
        {
            // good
        }

    }

    @Test
    public void testRemoveAssociationFromUser() throws Exception
    {

        final String entityType1 = "TestEntity";
        final String entityType2 = "TestEntity2";
        final GenericValue ent1 = new MockGenericValue(entityType1, UtilMisc.toMap("id", new Long(1)));
        final GenericValue ent2 = new MockGenericValue(entityType1, UtilMisc.toMap("id", new Long(2)));
        final GenericValue ent3 = new MockGenericValue(entityType2, UtilMisc.toMap("id", new Long(3)));

        dam.createAssociation(user, ent1, "favourite");
        dam.createAssociation(user, ent2, "favourite");
        dam.createAssociation(user, ent3, "favourite");

        Collection associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(3, associations.size());

        dam.removeUserAssociationsFromUser(user);

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(0, associations.size());

        dam.createAssociation(user, ent1, "favourite");
        dam.createAssociation(user, ent2, "favourite");
        dam.createAssociation(user, ent3, "nonfavourite");

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(3, associations.size());

        dam.removeUserAssociationsFromUser(user, "favourite");

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(1, associations.size());

        dam.removeUserAssociationsFromUser(user, "nonfavourite");

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(0, associations.size());

        dam.createAssociation(user, ent1, "favourite");
        dam.createAssociation(user, ent2, "favourite");
        dam.createAssociation(user, ent3, "favourite");

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(3, associations.size());

        dam.removeUserAssociationsFromUser(user, "favourite", entityType1);

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(1, associations.size());

        dam.removeUserAssociationsFromUser(user, "favourite", entityType2);

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(0, associations.size());
    }

    @Test
    public void testRemoveAssociationFromEntity() throws Exception
    {

        final String entityType1 = "TestEntity";
        final String entityType2 = "TestEntity2";
        final GenericValue ent1 = new MockGenericValue(entityType1, UtilMisc.toMap("id", new Long(1)));
        final GenericValue ent2 = new MockGenericValue(entityType1, UtilMisc.toMap("id", new Long(2)));
        final GenericValue ent3 = new MockGenericValue(entityType2, UtilMisc.toMap("id", new Long(3)));

        final User user2 = OSUserConverter.convertToOSUser(new MockUser("joe"));

        dam.createAssociation(user, ent1, "favourite");
        dam.createAssociation(user2, ent1, "favourite");
        dam.createAssociation(user, ent1, "favourite2");
        dam.createAssociation(user2, ent1, "favourite2");
        dam.createAssociation(user, ent2, "favourite");
        dam.createAssociation(user, ent3, "favourite");

        Collection associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(6, associations.size());

        dam.removeUserAssociationsFromSink(ent1, "favourite");

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(4, associations.size());

        dam.removeUserAssociationsFromSink(ent1, "favourite2");

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(2, associations.size());

        dam.removeUserAssociationsFromSink(ent2, "favourite");

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(1, associations.size());

        dam.removeUserAssociationsFromSink(ent3, "favourite");

        associations = CoreFactory.getGenericDelegator().findAll("UserAssociation");
        assertEquals(0, associations.size());
    }
}
