package com.atlassian.jira.sharing;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.user.MockUser;
import com.atlassian.query.QueryImpl;
import com.atlassian.jira.local.ListeningTestCase;
import org.easymock.MockControl;

import java.util.Collections;

public class TestCachingSharePermissionStore extends ListeningTestCase
{
    private User user;
    private MockControl shareStoreCtrl;

    private SharePermissionStore shareStore;
    private PortalPage entity1;
    private static final Long ENTITY1_ID = new Long(10);
    private static final Long ENTITY2_ID = new Long(11);
    private PortalPage entity2;
    private SearchRequest entity3;

    @Before
    public void setUp() throws Exception
    {
        user = new MockUser("testUser");
        shareStoreCtrl = MockControl.createStrictControl(SharePermissionStore.class);
        shareStore = (SharePermissionStore) shareStoreCtrl.getMock();

        entity1 = PortalPage.id(ENTITY1_ID).name("page1").owner(user.getName()).build();
        entity2 = PortalPage.id(ENTITY2_ID).name("page2").owner(user.getName()).build();      

        entity3 = new SearchRequest(new QueryImpl(), user.getName(), null, null, ENTITY1_ID, 0L);
    }

    /**
     * Make sure the call to get is cached after he first call.
     */
    @Test
    public void testGetSharePermissions()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        shareStore.getSharePermissions(entity2);
        shareStoreCtrl.setReturnValue(expectedEntity2Permissions);

        shareStore.getSharePermissions(entity3);
        final SharedEntity.SharePermissions expectedEntity3Permissions = new SharedEntity.SharePermissions(
            Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));
        shareStoreCtrl.setReturnValue(expectedEntity3Permissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //this should call the database.
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));

        //this should not call the database.
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));

        //this should call the databases.
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        //this should not call the database
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity1Permissions, sharePermissionStore.getSharePermissions(entity1));

        //this should call the databases.
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));

        //this should not call the database
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));
        assertEquals(expectedEntity3Permissions, sharePermissionStore.getSharePermissions(entity3));

        verifyMocks();
    }

    /**
     * Null share should be replaced with private share.
     */
    @Test
    public void testGetSharePermissionsNull()
    {
        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(null);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //this should call the database.
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));

        verifyMocks();
    }

    /**
     * Make sure delete clears the related cache enity.
     */
    @Test
    public void testDeleteSharePermissions()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        shareStore.getSharePermissions(entity2);
        shareStoreCtrl.setReturnValue(expectedEntity2Permissions);

        shareStore.deleteSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(-1);

        final SharedEntity.SharePermissions expectedEntity1NewPermissions = new SharedEntity.SharePermissions(
            Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));
        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1NewPermissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);

        assertEquals(-1, sharePermissionStore.deleteSharePermissions(entity1));

        //This should go back to the database
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        // This should now be cached
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        verifyMocks();

    }

    /**
     * Make sure that delete by like cleans up the whole cache. 
     */
    @Test
    public void testDeleteSharePermissionsLike()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        shareStore.getSharePermissions(entity2);
        shareStoreCtrl.setReturnValue(expectedEntity2Permissions);

        final SharePermissionImpl expectedShareType = new SharePermissionImpl(GlobalShareType.TYPE, "p1", "p2");
        shareStore.deleteSharePermissionsLike(expectedShareType);
        shareStoreCtrl.setReturnValue(-1);

        final SharedEntity.SharePermissions expectedEntity1NewPermissions = new SharedEntity.SharePermissions(
            Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));
        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1NewPermissions);

        shareStore.getSharePermissions(entity2);
        shareStoreCtrl.setReturnValue(expectedEntity2Permissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);

        assertEquals(-1, sharePermissionStore.deleteSharePermissionsLike(expectedShareType));

        //This should go back to the database
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        // This should now be cached
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        //This should go back to the database
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        //This should now be cached
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));
        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        verifyMocks();

    }

    /**
     * Store share permissions should replace the permissions of the old cache entry.
     */
    @Test
    public void testStoreSharePermissions()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        shareStore.getSharePermissions(entity2);
        shareStoreCtrl.setReturnValue(expectedEntity2Permissions);

        final SharedEntity.SharePermissions expectedEntity1NewPermissions = new SharedEntity.SharePermissions(
            Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));
        shareStore.storeSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1NewPermissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);

        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.storeSharePermissions(entity1));

        // This should now be cached
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        verifyMocks();

    }

    /**
     * Make sure that store replaces null with private permission.
     */
    @Test
    public void testStoreSharePermissionsNullCase()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        shareStore.getSharePermissions(entity2);
        shareStoreCtrl.setReturnValue(expectedEntity2Permissions);

        shareStore.storeSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(null);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);

        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.storeSharePermissions(entity1));

        // This should now be cached
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(SharedEntity.SharePermissions.PRIVATE, sharePermissionStore.getSharePermissions(entity1));

        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        verifyMocks();

    }

    /**
     * Make sure that store removes entry from cache on error.
     */
    @Test
    public void testStoreSharePermissionsErrorCondition()
    {
        final SharedEntity.SharePermissions expectedEntity1Permissions = SharedEntity.SharePermissions.GLOBAL;

        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1Permissions);

        final SharedEntity.SharePermissions expectedEntity2Permissions = SharedEntity.SharePermissions.PRIVATE;

        shareStore.getSharePermissions(entity2);
        shareStoreCtrl.setReturnValue(expectedEntity2Permissions);

        final SharedEntity.SharePermissions expectedEntity1NewPermissions = new SharedEntity.SharePermissions(
            Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE, "random group", null)));
        shareStore.storeSharePermissions(entity1);
        shareStoreCtrl.setThrowable(new RuntimeException("some error"));

        shareStore.getSharePermissions(entity1);
        shareStoreCtrl.setReturnValue(expectedEntity1NewPermissions);

        final SharePermissionStore sharePermissionStore = createShareStore();

        //Priming the cache
        sharePermissionStore.getSharePermissions(entity1);
        sharePermissionStore.getSharePermissions(entity2);
        try
        {
            sharePermissionStore.storeSharePermissions(entity1);
            fail("Should have failed");
        }
        catch (final RuntimeException ignore)
        {}

        //This should go back to the database
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        // This should now be cached
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));
        assertEquals(expectedEntity1NewPermissions, sharePermissionStore.getSharePermissions(entity1));

        assertEquals(expectedEntity2Permissions, sharePermissionStore.getSharePermissions(entity2));

        verifyMocks();

    }

    private void verifyMocks()
    {
        shareStoreCtrl.verify();
    }

    private SharePermissionStore createShareStore()
    {
        shareStoreCtrl.replay();

        return new CachingSharePermissionStore(shareStore, null);
    }
}
