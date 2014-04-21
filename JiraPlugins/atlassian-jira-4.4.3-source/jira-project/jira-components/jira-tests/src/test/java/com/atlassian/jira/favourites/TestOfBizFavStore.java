package com.atlassian.jira.favourites;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.sharing.SharedEntity;

import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import com.opensymphony.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TestOfBizFavStore extends LegacyJiraMockTestCase
{
    private OfBizDelegator ofBizDelegator;
    private User user;
    private SharedEntity entity;
    private FavouritesStore store;
    private static final String NOTADMIN_USER = "notadmin";
    private static final String ADMIN_USER = "admin";

    private static class Column
    {
        private static final String USERNAME = "username";
        private static final String ENTITY_TYPE = "entityType";
        private static final String ENTITY_ID = "entityId";
        private static final String SEQUENCE = "sequence";
    }

    private static class ID
    {
        private static final Long _666 = new Long(666);
        private static final Long _999 = new Long(999);
        private static final Long _123 = new Long(123);
        private static final Long _456 = new Long(456);
        private static final Long _789 = new Long(789);
    }

    private static class SharedEntityType
    {
        static final SharedEntity.TypeDescriptor DASHBOARD = SharedEntity.TypeDescriptor.Factory.get().create("Dashboard");
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        ofBizDelegator = (OfBizDelegator) ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        user = UtilsForTests.getTestUser(ADMIN_USER);
        UtilsForTests.getTestUser(NOTADMIN_USER);
        entity = new SharedEntity.Identifier(ID._999, SearchRequest.ENTITY_TYPE, user);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        UtilsForTests.cleanUsers();
        UtilsForTests.cleanOFBiz();
        ofBizDelegator = null;
        user = null;
        store = null;
        entity = null;
    }

    public void testAddFavouriteSuccess()
    {
        store = new OfBizFavouritesStore(ofBizDelegator);

        assertTrue(store.addFavourite(user, entity));

        final List results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertNotNull(results);
        assertEquals(1, results.size());
        final GenericValue result = EntityUtil.getOnly(results);
        assertEquals(entity.getEntityType().getName(), result.getString(Column.ENTITY_TYPE));
        assertEquals(entity.getId(), result.getLong(Column.ENTITY_ID));
        assertEquals(user.getName(), result.getString(Column.USERNAME));
        assertEquals(new Long(0), result.getLong(Column.SEQUENCE));
    }

    public void testMultipleCReatesIncreasesSequence() throws Exception
    {
        store = new OfBizFavouritesStore(ofBizDelegator);

        assertTrue(store.addFavourite(user, entity));

        List gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(Column.ENTITY_ID,
            entity.getId()).toMap());
        assertGVHaveThisSequence(gvs, new long[] { 0 });

        entity = new SharedEntity.Identifier(ID._123, SearchRequest.ENTITY_TYPE, user);
        assertTrue(store.addFavourite(user, entity));
        gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(Column.ENTITY_ID,
            entity.getId()).toMap());
        assertGVHaveThisSequence(gvs, new long[] { 1 });

        entity = new SharedEntity.Identifier(ID._456, SearchRequest.ENTITY_TYPE, user);
        assertTrue(store.addFavourite(user, entity));
        gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(Column.ENTITY_ID,
            entity.getId()).toMap());
        assertGVHaveThisSequence(gvs, new long[] { 2 });

        entity = new SharedEntity.Identifier(ID._789, SearchRequest.ENTITY_TYPE, user);
        assertTrue(store.addFavourite(user, entity));
        gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(Column.ENTITY_ID,
            entity.getId()).toMap());
        assertGVHaveThisSequence(gvs, new long[] { 3 });

        // now test the double addition does nothing to the sequence
        assertFalse(store.addFavourite(user, entity));
        gvs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(Column.ENTITY_ID,
            entity.getId()).toMap());
        assertGVHaveThisSequence(gvs, new long[] { 3 });
    }

    private void assertGVHaveThisSequence(final List gvs, final long[] expectedSequence)
    {
        assertNotNull(gvs);
        assertEquals(expectedSequence.length, gvs.size());
        for (int i = 0; i < expectedSequence.length; i++)
        {
            final long l = expectedSequence[i];
            final GenericValue gv = (GenericValue) gvs.get(0);
            assertEquals(new Long(l), gv.getLong(Column.SEQUENCE));
        }
    }

    public void testAddFavouriteAssociationAlreadyExists()
    {
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, entity.getId()).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        assertFalse(store.addFavourite(user, entity));

        final List results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertNotNull(results);
        assertEquals(1, results.size());
        final GenericValue result = EntityUtil.getOnly(results);
        assertEquals(entity.getEntityType().getName(), result.getString(Column.ENTITY_TYPE));
        assertEquals(entity.getId(), result.getLong(Column.ENTITY_ID));
        assertEquals(user.getName(), result.getString(Column.USERNAME));
    }

    /**
     * Remove a favourite that is the last. In that case no reorder should occur.
     */
    public void testRemoveSuccessNoReorder()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.SEQUENCE, new Long(0)).add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, "Dashboard").add(Column.ENTITY_ID,
            entity.getId()).toMap());
        final GenericValue gv2 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.SEQUENCE, new Long(0)).add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
            Column.ENTITY_ID, ID._456).toMap());
        final GenericValue gv3 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.SEQUENCE, new Long(0)).add(Column.USERNAME, NOTADMIN_USER).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
            Column.ENTITY_ID, entity.getId()).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.SEQUENCE, new Long(1)).add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE,
                entity.getEntityType().getName()).add(Column.ENTITY_ID, entity.getId()).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        assertTrue(store.removeFavourite(user, entity));

        final List results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.contains(gv1));
        assertTrue(results.contains(gv2));
        assertTrue(results.contains(gv3));

    }

    /**
     * Remove a favourite that is not the last, this should trigger a reorder.
     */
    public void testRemoveSuccessWithReorder()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.SEQUENCE, new Long(0)).add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, "Dashboard").add(Column.ENTITY_ID,
            entity.getId()).toMap());
        final GenericValue gv2 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.SEQUENCE, new Long(0)).add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
            Column.ENTITY_ID, ID._123).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.SEQUENCE, new Long(1)).add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE,
                entity.getEntityType().getName()).add(Column.ENTITY_ID, entity.getId()).toMap());
        final GenericValue gv3 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.SEQUENCE, new Long(0)).add(Column.USERNAME, NOTADMIN_USER).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
            Column.ENTITY_ID, ID._456).toMap());
        final GenericValue gv4 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.SEQUENCE, new Long(2)).add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
            Column.ENTITY_ID, ID._666).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        assertTrue(store.removeFavourite(user, entity));

        gv4.set(Column.SEQUENCE, new Long(1));

        final List results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, EasyList.build(Column.ENTITY_TYPE,
            Column.USERNAME, Column.SEQUENCE));
        assertNotNull(results);
        assertEquals(4, results.size());
        assertTrue(results.contains(gv1));
        assertTrue(results.contains(gv2));
        assertTrue(results.contains(gv3));
        assertTrue(results.contains(gv4));
    }

    public void testRemoveNoAssociation()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, "Dashboard").add(Column.ENTITY_ID, entity.getId()).toMap());
        final GenericValue gv2 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(Column.ENTITY_ID, ID._456).toMap());
        final GenericValue gv3 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.USERNAME, NOTADMIN_USER).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(Column.ENTITY_ID, entity.getId()).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        assertFalse(store.removeFavourite(user, entity));

        final List results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.contains(gv1));
        assertTrue(results.contains(gv2));
        assertTrue(results.contains(gv3));
    }

    public void testIsFavourite()
    {
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, entity.getId()).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        assertTrue(store.isFavourite(user, entity));
    }

    public void testIsNotFavourite()
    {
        UtilsForTests.getTestEntity(
            OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, "Dashboard").add(Column.ENTITY_ID, entity.getId()).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, ID._456).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, NOTADMIN_USER).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, entity.getId()).toMap());
        store = new OfBizFavouritesStore(ofBizDelegator);

        assertFalse(store.isFavourite(user, entity));
    }

    public void testGetFavIdsNoneStored()
    {
        UtilsForTests.getTestEntity(
            OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, "Dashboard").add(Column.ENTITY_ID, entity.getId()).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, NOTADMIN_USER).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, entity.getId()).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        final Collection ids = store.getFavouriteIds(user, entity.getEntityType());

        assertNotNull(ids);
        assertTrue(ids.isEmpty());
    }

    public void testGetFavIdsOneStored()
    {
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, entity.getId()).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        final Collection ids = store.getFavouriteIds(user, entity.getEntityType());

        assertNotNull(ids);
        assertFalse(ids.isEmpty());
        assertEquals(1, ids.size());
        assertTrue(ids.contains(ID._999));
    }

    public void testGetFavIdsManyStored()
    {

        UtilsForTests.getTestEntity(
            OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, "Dashboard").add(Column.ENTITY_ID, new Long(1)).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, new Long(1)).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, ID._999).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, ID._123).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        final Collection ids = store.getFavouriteIds(user, entity.getEntityType());

        assertNotNull(ids);
        assertFalse(ids.isEmpty());

        final List returnList = EasyList.build(new Long(1), ID._123, ID._999);
        for (final Iterator iterator = ids.iterator(); iterator.hasNext();)
        {
            final Long aLong = (Long) iterator.next();
            assertTrue(returnList.contains(aLong));
        }
    }

    public void testRemoveFavouritesForUser()
    {

        final GenericValue gv1 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, "Dashboard").add(Column.ENTITY_ID, new Long(1)).toMap());
        final GenericValue gv2 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.USERNAME, NOTADMIN_USER).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(Column.ENTITY_ID, new Long(1)).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, ID._999).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, ID._123).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        store.removeFavouritesForUser(user, SearchRequest.ENTITY_TYPE);

        final List results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(gv1));
        assertTrue(results.contains(gv2));
    }

    public void testRemoveFavouritesForUserNoEntries()
    {
        store = new OfBizFavouritesStore(ofBizDelegator);

        store.removeFavouritesForUser(user, SearchRequest.ENTITY_TYPE);

        final List results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    public void testRemoveFavouritesForEntity()
    {
        final GenericValue gv1 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, "Dashboard").add(Column.ENTITY_ID, new Long(1)).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, NOTADMIN_USER).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, ID._999).toMap());
        UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(
                Column.ENTITY_ID, ID._999).toMap());
        final GenericValue gv4 = UtilsForTests.getTestEntity(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(
            Column.USERNAME, user.getName()).add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(Column.ENTITY_ID, ID._123).toMap());

        store = new OfBizFavouritesStore(ofBizDelegator);

        store.removeFavouritesForEntity(entity);

        final List results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains(gv1));
        assertTrue(results.contains(gv4));
    }

    public void testRemoveFavouritesNoEntries()
    {
        store = new OfBizFavouritesStore(ofBizDelegator);

        store.removeFavouritesForEntity(entity);

        final List results = ofBizDelegator.findAll(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION);
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    /**
     * Happy path test of re-ordering where everthing is as expected
     */
    public void testUpdateSequenceReorders()
    {
        final SharedEntity.TypeDescriptor entityType = entity.getEntityType();

        final List inputGvs = new ArrayList(10);
        inputGvs.add(storeTestEntity(new Long(1), user.getName(), SharedEntityType.DASHBOARD, 999));
        inputGvs.add(storeTestEntity(ID._123, user.getName(), entityType, 0, 2));
        inputGvs.add(storeTestEntity(ID._456, user.getName(), entityType, 1, 0));
        inputGvs.add(storeTestEntity(ID._789, user.getName(), entityType, 2, 1));
        inputGvs.add(storeTestEntity(ID._999, user.getName(), entityType, 3, 3));
        inputGvs.add(storeTestEntity(ID._999, NOTADMIN_USER, entityType, 3));

        final List inputList = EasyList.build(new SharedEntity.Identifier(ID._456, entityType, user), new SharedEntity.Identifier(ID._789,
            entityType, user), new SharedEntity.Identifier(ID._123, entityType, user), new SharedEntity.Identifier(ID._999, entityType, user));

        store = new OfBizFavouritesStore(ofBizDelegator);
        store.updateSequence(user, inputList);

        assertFavouriteSequence(inputGvs);
    }

    /**
     * Test update sequence where the list contains a non favourite.  This is unlikely but hey lets test it
     */
    public void testUpdateSequenceReordersWhereANonFavouriteIsInTheList()
    {
        final SharedEntity.TypeDescriptor entityType = entity.getEntityType();

        final List inputGvs = new ArrayList(10);
        inputGvs.add(storeTestEntity(new Long(1), user.getName(), SharedEntityType.DASHBOARD, 999));
        inputGvs.add(storeTestEntity(ID._123, user.getName(), entityType, 0, 2));
        inputGvs.add(storeTestEntity(ID._456, user.getName(), entityType, 1, 0));
        inputGvs.add(storeTestEntity(ID._789, user.getName(), entityType, 2, 1));
        inputGvs.add(storeTestEntity(ID._999, user.getName(), entityType, 3, 3));
        inputGvs.add(storeTestEntity(ID._999, NOTADMIN_USER, entityType, 3));

        final List inputList = EasyList.build(new SharedEntity.Identifier(ID._456, entityType, user), new SharedEntity.Identifier(ID._789,
            entityType, user), new SharedEntity.Identifier(ID._123, entityType, user), new SharedEntity.Identifier(ID._999, entityType, user),
            new SharedEntity.Identifier(ID._666, entityType, user));

        store = new OfBizFavouritesStore(ofBizDelegator);
        store.updateSequence(user, inputList);

        assertFavouriteSequence(inputGvs);
    }

    /**
     * Test update sequence where the list is empty.  Nothing should change
     */
    public void testUpdateSequenceNoopForAnEmptyList()
    {
        final SharedEntity.TypeDescriptor entityType = entity.getEntityType();

        final List inputGvs = new ArrayList(10);
        inputGvs.add(storeTestEntity(new Long(1), user.getName(), SharedEntityType.DASHBOARD, 999));
        inputGvs.add(storeTestEntity(ID._123, user.getName(), entityType, 0));
        inputGvs.add(storeTestEntity(ID._456, user.getName(), entityType, 1));
        inputGvs.add(storeTestEntity(ID._789, user.getName(), entityType, 2));
        inputGvs.add(storeTestEntity(ID._999, user.getName(), entityType, 3));
        inputGvs.add(storeTestEntity(ID._999, NOTADMIN_USER, entityType, 3));

        store = new OfBizFavouritesStore(ofBizDelegator);
        store.updateSequence(user, Collections.EMPTY_LIST);

        assertFavouriteSequence(inputGvs);
    }

    private void assertFavouriteSequence(final List expectedGVS)
    {
        for (final Iterator iterator = expectedGVS.iterator(); iterator.hasNext();)
        {
            final GenericValue expectedGV = (GenericValue) iterator.next();
            final List /*<GenericValue>*/actualGVs = ofBizDelegator.findByAnd(OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
                new PrimitiveMap.Builder().add(Column.ENTITY_TYPE, expectedGV.getString(Column.ENTITY_TYPE)).add(Column.ENTITY_ID,
                    expectedGV.getString(Column.ENTITY_ID)).add(Column.USERNAME, expectedGV.getString(Column.USERNAME)).toMap());

            assertEquals(1, actualGVs.size());
            final GenericValue actualGV = (GenericValue) actualGVs.get(0);
            assertEquals("Favourite is invalid: " + actualGV, expectedGV.getLong(Column.SEQUENCE), actualGV.getLong(Column.SEQUENCE));
        }
    }

    private GenericValue storeTestEntity(final Long entityId, final String userName, final SharedEntity.TypeDescriptor entityType, final long sequence, final long sequenceNew)
    {
        final GenericValue gv = storeTestEntity(entityId, userName, entityType, sequence);
        gv.set("sequence", new Long(sequenceNew));
        return gv;
    }

    private GenericValue storeTestEntity(final Long entityId, final String userName, final SharedEntity.TypeDescriptor entityType, final long sequence)
    {
        return UtilsForTests.getTestEntity(
            OfBizFavouritesStore.Table.FAVOURITE_ASSOCIATION,
            new PrimitiveMap.Builder().add(Column.USERNAME, userName).add(Column.ENTITY_TYPE, entityType.getName()).add(Column.ENTITY_ID, entityId).add(
                Column.SEQUENCE, new Long(sequence)).toMap());
    }
}
