package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.sharing.ShareManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.type.GlobalShareType;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.upgrade.UpgradeTask;
import com.atlassian.query.QueryImpl;
import com.opensymphony.user.User;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class TestUpgradeTask_Build321 extends MockControllerTestCase
{
    private ShareManager shareManager;
    private OfBizDelegator delegator;
    private static final User USER = new User("User1", new MockProviderAccessor(), new MockCrowdService());

    static final class Column
    {
        private static final String ID = "id";
        private static final String AUTHOR = "author";
        private static final String USER = "user";
        private static final String GROUP = "group";
    }

    static final class Table
    {
        private static final String ENTITY_TYPE_NAME = SearchRequest.ENTITY_TYPE.getName();
    }

    private static final String JIRA_USERS = "jira-users";

    @Before
    public void setUp() throws Exception
    {
        delegator = mockController.getMock(OfBizDelegator.class);
        shareManager = mockController.getMock(ShareManager.class);
    }

    @After
    public void tearDown() throws Exception
    {
        delegator = null;
        shareManager = null;

    }

    @Test
    public void testUpgradeNullSearchRequests() throws Exception
    {

        delegator.findByCondition(Table.ENTITY_TYPE_NAME, null, EasyList.build(Column.ID, Column.AUTHOR, Column.USER, Column.GROUP), null);
        mockController.setReturnValue(null);

        final UpgradeTask upgradeTask = createUpgradeTask();
        upgradeTask.doUpgrade(false);

        mockController.verify();
    }

    @Test
    public void testUpgradeNoSearchRequests() throws Exception
    {

        delegator.findByCondition(Table.ENTITY_TYPE_NAME, null, EasyList.build(Column.ID, Column.AUTHOR, Column.USER, Column.GROUP), null);
        mockController.setReturnValue(Collections.EMPTY_LIST);

        final UpgradeTask upgradeTask = createUpgradeTask();
        upgradeTask.doUpgrade(false);

        mockController.verify();
    }

    @Test
    public void testUpgradeSearchRequestsDupes() throws Exception
    {
        final Long entityId = new Long(1);

        final MockGenericValue gv1 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId, Column.AUTHOR, "nick",
            Column.GROUP, TestUpgradeTask_Build321.JIRA_USERS, "name", "Filter1", "description", "Desc1"));
        final SearchRequest entity1WithPermissions = new SearchRequest(new QueryImpl(), USER.getName(), "Filter1", "Desc1", entityId, 0L);
        final SharedEntity.SharePermissions sharePermissions = new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE,
                JIRA_USERS, null)));
        entity1WithPermissions.setPermissions(sharePermissions);

        final List returnList = EasyList.build(gv1);
        delegator.findByCondition(Table.ENTITY_TYPE_NAME, null, EasyList.build(Column.ID, Column.AUTHOR, Column.USER, Column.GROUP), null);
        mockController.setReturnValue(returnList);

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(sharePermissions);

        final UpgradeTask upgradeTask = createUpgradeTask();
        upgradeTask.doUpgrade(false);

        mockController.verify();
    }

    @Test
    public void testUpgradeSearchRequestsPrivate() throws Exception
    {
        final Long entityId = new Long(1);
        final MockGenericValue gv1 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId, Column.AUTHOR, "nick",
            Column.USER, "nick"));

        final List returnList = EasyList.build(gv1);
        delegator.findByCondition(Table.ENTITY_TYPE_NAME, null, EasyList.build(Column.ID, Column.AUTHOR, Column.USER, Column.GROUP), null);
        mockController.setReturnValue(returnList);

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(new SharedEntity.SharePermissions(Collections.<SharePermission>emptySet()));

        final UpgradeTask upgradeTask = createUpgradeTask();
        upgradeTask.doUpgrade(false);

        mockController.verify();
    }

    @Test
    public void testUpgradeSearchRequestsGlobal() throws Exception
    {
        final Long entityId = new Long(1);
        final MockGenericValue gv1 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId, Column.AUTHOR, "nick", "name", "Filter1", "description", "Desc1", "favCount", 0L));

        final SearchRequest entity1WithPermissions = new SearchRequest(new QueryImpl(), "nick", "Filter1", "Desc1", entityId, 0L);
        entity1WithPermissions.setPermissions(new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(
            GlobalShareType.TYPE, null, null))));

        final List returnList = EasyList.build(gv1);
        delegator.findByCondition(Table.ENTITY_TYPE_NAME, null, EasyList.build(Column.ID, Column.AUTHOR, Column.USER, Column.GROUP), null);
        mockController.setReturnValue(returnList);

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(new SharedEntity.SharePermissions(Collections.<SharePermission>emptySet()));

        shareManager.updateSharePermissions(entity1WithPermissions);
        mockController.setReturnValue(null);

        final UpgradeTask upgradeTask = createUpgradeTask();
        upgradeTask.doUpgrade(false);

        mockController.verify();
    }

    @Test
    public void testUpgradeSearchRequestsGroup() throws Exception
    {
        final Long entityId = new Long(1);
        final MockGenericValue gv1 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId, Column.AUTHOR, "nick",
            Column.GROUP, JIRA_USERS, "name", "Filter1", "description", "Desc1", "favCount", 0L));

        final SearchRequest entity1WithPermissions = new SearchRequest(new QueryImpl(), "nick", "Filter1", "Desc1", entityId, 0L);
        entity1WithPermissions.setPermissions(new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE,
            JIRA_USERS, null))));

        final List returnList = EasyList.build(gv1);
        delegator.findByCondition(Table.ENTITY_TYPE_NAME, null, EasyList.build(Column.ID, Column.AUTHOR, Column.USER, Column.GROUP), null);
        mockController.setReturnValue(returnList);

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(new SharedEntity.SharePermissions(Collections.<SharePermission>emptySet()));

        shareManager.updateSharePermissions(entity1WithPermissions);
        mockController.setReturnValue(null);

        final UpgradeTask upgradeTask = createUpgradeTask();
        upgradeTask.doUpgrade(false);

        mockController.verify();
    }

    @Test
    public void testUpgradeSearchRequestsMultiple() throws Exception
    {
        final Long entityId = new Long(1);
        final Long entityId2 = new Long(9);
        final Long entityId3 = new Long(6);
        final MockGenericValue gv1 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId, Column.AUTHOR, "nick",
            Column.GROUP, TestUpgradeTask_Build321.JIRA_USERS, "name", "Filter1", "description", "Desc1", "favCount", 0L));
        final MockGenericValue gv2 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId2, Column.AUTHOR, "nick", "name", "Filter2", "description", "Desc2", "favCount", 0L));
        final MockGenericValue gv3 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId3, Column.AUTHOR, "nick",
            Column.USER, "nick", "name", "Filter3", "description", "Desc3", "favCount", 0L));

        final SearchRequest entity1WithPermissions = new SearchRequest(new QueryImpl(), "nick", "Filter1", "Desc1", entityId, 0L);
        entity1WithPermissions.setPermissions(new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE,
            JIRA_USERS, null))));

        final SearchRequest entity2WithPermissions = new SearchRequest(new QueryImpl(), "nick", "Filter2", "Desc2", entityId2, 0L);
        entity2WithPermissions.setPermissions(new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(
            GlobalShareType.TYPE, null, null))));

        final List returnList = EasyList.build(gv1, gv2, gv3);
        delegator.findByCondition(Table.ENTITY_TYPE_NAME, null, EasyList.build(Column.ID, Column.AUTHOR, Column.USER, Column.GROUP), null);
        mockController.setReturnValue(returnList);

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(new SharedEntity.SharePermissions(Collections.<SharePermission>emptySet()));

        shareManager.updateSharePermissions(entity1WithPermissions);
        mockController.setReturnValue(null);

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId2, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(new SharedEntity.SharePermissions(Collections.<SharePermission>emptySet()));

        shareManager.updateSharePermissions(entity2WithPermissions);
        mockController.setReturnValue(null);

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId3, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(
            GlobalShareType.TYPE, null, null))));


        final UpgradeTask upgradeTask = createUpgradeTask();
        upgradeTask.doUpgrade(false);

        mockController.verify();
    }

    /**
     * Make sure a read error from the manager does not stop the upgrade.
     *
     * @throws Exception this indicates an error.
     */
    @Test
    public void testUpgradeSearchRequestReadError() throws Exception
    {
        final Long entityId1 = new Long(1);
        final Long entityId2 = new Long(9);

        final MockGenericValue gv1 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId1, Column.AUTHOR, "nick",
            Column.GROUP, TestUpgradeTask_Build321.JIRA_USERS));
        final MockGenericValue gv2 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId2, Column.AUTHOR, "nick", "name", "Filter2", "description", "Desc2", "favCount", 0L));

        final SearchRequest entity2WithPermissions = new SearchRequest(new QueryImpl(), "nick", "Filter2", "Desc2", entityId2, 0L);
        entity2WithPermissions.setPermissions(new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(
            GlobalShareType.TYPE, null, null))));

        delegator.findByCondition(Table.ENTITY_TYPE_NAME, null, EasyList.build(Column.ID, Column.AUTHOR, Column.USER, Column.GROUP), null);
        mockController.setReturnValue(EasyList.build(gv1, gv2));

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId1, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setThrowable(new RuntimeException("test the exception"));

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId2, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(new SharedEntity.SharePermissions(Collections.<SharePermission>emptySet()));

        shareManager.updateSharePermissions(entity2WithPermissions);
        mockController.setReturnValue(null);

        final UpgradeTask upgradeTask = createUpgradeTask();
        upgradeTask.doUpgrade(false);

        mockController.verify();
    }

    /**
     * Make sure a write error from the manager does not stop the upgrade.
     *
     * @throws Exception this indicates an error.
     */
    @Test
    public void testUpgradeSearchRequestWriteError() throws Exception
    {
        final Long entityId1 = new Long(1);
        final Long entityId2 = new Long(9);

        final MockGenericValue gv1 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId1, Column.AUTHOR, "nick",
            Column.GROUP, TestUpgradeTask_Build321.JIRA_USERS, "name", "Filter1", "description", "Desc1", "favCount", 0L));
        final MockGenericValue gv2 = new MockGenericValue(Table.ENTITY_TYPE_NAME, EasyMap.build(Column.ID, entityId2, Column.AUTHOR, "nick", "name", "Filter2", "description", "Desc2", "favCount", 0L));

        final SearchRequest entity1WithPermissions = new SearchRequest(new QueryImpl(), "nick", "Filter1", "Desc1", entityId1, 0L);
        entity1WithPermissions.setPermissions(new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(GroupShareType.TYPE,
            JIRA_USERS, null))));

        final SearchRequest entity2WithPermissions = new SearchRequest(new QueryImpl(), "nick", "Filter2", "Desc2", entityId2, 0L);
        entity2WithPermissions.setPermissions(new SharedEntity.SharePermissions(Collections.singleton(new SharePermissionImpl(
            GlobalShareType.TYPE, null, null))));

        delegator.findByCondition(Table.ENTITY_TYPE_NAME, null, EasyList.build(Column.ID, Column.AUTHOR, Column.USER, Column.GROUP), null);
        mockController.setReturnValue(EasyList.build(gv1, gv2));

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId1, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(new SharedEntity.SharePermissions(Collections.<SharePermission>emptySet()));

        shareManager.updateSharePermissions(entity1WithPermissions);
        mockController.setThrowable(new RuntimeException("test the exception"));

        shareManager.getSharePermissions(new SharedEntity.Identifier(entityId2, SearchRequest.ENTITY_TYPE, "nick"));
        mockController.setReturnValue(new SharedEntity.SharePermissions(Collections.<SharePermission>emptySet()));

        shareManager.updateSharePermissions(entity2WithPermissions);
        mockController.setReturnValue(null);

        final UpgradeTask upgradeTask = createUpgradeTask();
        upgradeTask.doUpgrade(false);

        mockController.verify();
    }

    @Test
    public void testGetBuildNumber()
    {
        final UpgradeTask task = createUpgradeTask();
        assertEquals("321", task.getBuildNumber());
    }

    private UpgradeTask createUpgradeTask()
    {
        mockController.replay();
        final UpgradeTask upgradeTask = new UpgradeTask_Build321(delegator, shareManager);
        return upgradeTask;
    }

}
