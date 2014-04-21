package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Iterator;
import java.util.List;

public class TestUpgradeTask_Build231 extends LegacyJiraMockTestCase
{

    private PermissionSchemeManager permissionSchemeManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        // Get a hold of the notification scheme manager
        permissionSchemeManager = (PermissionSchemeManager) ComponentManager.getComponentInstanceOfType(PermissionSchemeManager.class);
    }

    // The upgrade task should not update the default perm scheme since it is created with the values already in place
    public void testDoUpgradeWithStandardPermissionScheme() throws Exception
    {
        // Create the real default permission scheme
        UpgradeTask1_2 upgradeTask1_2 = new UpgradeTask1_2();
        upgradeTask1_2.doUpgrade(false);

        // create two more schemes - copies of default notification scheme
        GenericValue defaultPermissionScheme = permissionSchemeManager.getDefaultScheme();

        // Make sure the scheme is correct to begin with
        assertDefaultSchemeCorrect(defaultPermissionScheme);

        // run the upgrade task
        UpgradeTask_Build231 upgradeTask_build231 = new UpgradeTask_Build231(permissionSchemeManager);
        upgradeTask_build231.doUpgrade(false);

        // verify that all schemes were updated
        final List schemes = permissionSchemeManager.getSchemes();

        assertEquals(1, schemes.size());

        GenericValue schemeGV = (GenericValue) schemes.iterator().next();
        // Make sure the scheme is still unchanged
        assertDefaultSchemeCorrect(schemeGV);
    }

    public void testDoUpgradeWithCustomPermissionScheme() throws Exception
    {
        GenericValue value = permissionSchemeManager.createDefaultScheme();

        SchemeEntity testSchemeEntity = new SchemeEntity("projectRole", "10000", new Long(Permissions.DELETE_ISSUE));
        permissionSchemeManager.createSchemeEntity(value, testSchemeEntity);

        // create two more schemes - copies of default notification scheme
        GenericValue defaultPermissionScheme = permissionSchemeManager.getDefaultScheme();
        permissionSchemeManager.copyScheme(defaultPermissionScheme);
        permissionSchemeManager.copyScheme(defaultPermissionScheme);

        // run the upgrade task
        UpgradeTask_Build231 upgradeTask_build231 = new UpgradeTask_Build231(permissionSchemeManager);
        upgradeTask_build231.doUpgrade(false);

        // verify that all schemes were updated
        final List schemes = permissionSchemeManager.getSchemes();

        assertEquals(3, schemes.size());

        for (Iterator i = schemes.iterator(); i.hasNext();)
        {
            GenericValue schemeGV = (GenericValue) i.next();
            final List entitiesAttachAll = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.ATTACHMENT_DELETE_ALL));
            assertNotNull(entitiesAttachAll);
            assertEquals(1, entitiesAttachAll.size());
            assertSchemeEntityListContainsTypeAndParam("projectRole", "10000", entitiesAttachAll);
            final List entitiesCommentAll = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.COMMENT_DELETE_ALL));
            assertNotNull(entitiesCommentAll);
            assertEquals(1, entitiesCommentAll.size());
            assertSchemeEntityListContainsTypeAndParam("projectRole", "10000", entitiesCommentAll);
            final List entitiesCommentOwn = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.COMMENT_DELETE_OWN));
            assertNotNull(entitiesCommentOwn);
            assertEquals(0, entitiesCommentOwn.size());
            final List entitiesAttachOwn = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.ATTACHMENT_DELETE_OWN));
            assertNotNull(entitiesAttachOwn);
            assertEquals(0, entitiesAttachOwn.size());
        }
    }

    public void testDoUpgradeWithMultipleSchemeEntitiesInCustomPermissionScheme() throws Exception
    {
        GenericValue value = permissionSchemeManager.createDefaultScheme();

        SchemeEntity testSchemeEntityOne = new SchemeEntity("projectRole", "10000", new Long(Permissions.DELETE_ISSUE));
        SchemeEntity testSchemeEntityTwo = new SchemeEntity("projectRole", "10010", new Long(Permissions.DELETE_ISSUE));
        permissionSchemeManager.createSchemeEntity(value, testSchemeEntityOne);
        permissionSchemeManager.createSchemeEntity(value, testSchemeEntityTwo);

        // create two more schemes - copies of default notification scheme
        GenericValue defaultPermissionScheme = permissionSchemeManager.getDefaultScheme();
        permissionSchemeManager.copyScheme(defaultPermissionScheme);
        permissionSchemeManager.copyScheme(defaultPermissionScheme);

        // run the upgrade task
        UpgradeTask_Build231 upgradeTask_build231 = new UpgradeTask_Build231(permissionSchemeManager);
        upgradeTask_build231.doUpgrade(false);

        // verify that all schemes were updated
        final List schemes = permissionSchemeManager.getSchemes();

        assertEquals(3, schemes.size());

        for (Iterator i = schemes.iterator(); i.hasNext();)
        {
            GenericValue schemeGV = (GenericValue) i.next();
            final List entitiesAttachAll = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.ATTACHMENT_DELETE_ALL));
            assertNotNull(entitiesAttachAll);
            assertSchemeEntityListContainsTypeAndParam("projectRole", "10000", entitiesAttachAll);
            assertSchemeEntityListContainsTypeAndParam("projectRole", "10010", entitiesAttachAll);
            assertEquals(2, entitiesAttachAll.size());
            final List entitiesCommentAll = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.COMMENT_DELETE_ALL));
            assertNotNull(entitiesCommentAll);
            assertEquals(2, entitiesCommentAll.size());
            assertSchemeEntityListContainsTypeAndParam("projectRole", "10000", entitiesCommentAll);
            assertSchemeEntityListContainsTypeAndParam("projectRole", "10010", entitiesCommentAll);
            final List entitiesCommentOwn = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.COMMENT_DELETE_OWN));
            assertNotNull(entitiesCommentOwn);
            assertEquals(0, entitiesCommentOwn.size());
            final List entitiesAttachOwn = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.ATTACHMENT_DELETE_OWN));
            assertNotNull(entitiesAttachOwn);
            assertEquals(0, entitiesAttachOwn.size());
        }
    }

    public void testDoUpgradeWithEmptyPermissionScheme() throws Exception
    {
        GenericValue emptyDefaultPermissionScheme = permissionSchemeManager.createDefaultScheme();

        // run the upgrade task
        UpgradeTask_Build231 upgradeTask_build231 = new UpgradeTask_Build231(permissionSchemeManager);
        upgradeTask_build231.doUpgrade(false);

        // verify that the notification schemes was updated by adding no new scheme entities

        List entities = permissionSchemeManager.getEntities(emptyDefaultPermissionScheme, new Long(Permissions.COMMENT_DELETE_ALL));
        assertEquals(0, entities.size());
        entities = permissionSchemeManager.getEntities(emptyDefaultPermissionScheme, new Long(Permissions.ATTACHMENT_DELETE_ALL));
        assertEquals(0, entities.size());
    }

    private void assertDefaultSchemeCorrect(GenericValue schemeGV) throws GenericEntityException
    {
        final List entitiesAttachAll = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.ATTACHMENT_DELETE_ALL));
        assertNotNull(entitiesAttachAll);
        assertEquals(1, entitiesAttachAll.size());
        assertSchemeEntityListContainsTypeAndParam(GroupDropdown.DESC, "jira-administrators", entitiesAttachAll);

        final List entitiesCommentAll = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.COMMENT_DELETE_ALL));
        assertNotNull(entitiesCommentAll);
        assertEquals(1, entitiesCommentAll.size());
        assertSchemeEntityListContainsTypeAndParam(GroupDropdown.DESC, "jira-administrators", entitiesCommentAll);
        final List entitiesCommentOwn = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.COMMENT_DELETE_OWN));
        assertNotNull(entitiesCommentOwn);
        assertEquals(1, entitiesCommentOwn.size());
        assertSchemeEntityListContainsTypeAndParam(GroupDropdown.DESC, "jira-users", entitiesCommentOwn);
        final List entitiesAttachOwn = permissionSchemeManager.getEntities(schemeGV, new Long(Permissions.ATTACHMENT_DELETE_OWN));
        assertNotNull(entitiesAttachOwn);
        assertEquals(1, entitiesAttachOwn.size());
        assertSchemeEntityListContainsTypeAndParam(GroupDropdown.DESC, "jira-users", entitiesAttachOwn);
    }

    private void assertSchemeEntityListContainsTypeAndParam(String type, String parameter, List entities)
    {
        for (Iterator i = entities.iterator(); i.hasNext();)
        {
            GenericValue entity = (GenericValue) i.next();
            final String entityType = entity.getString("type");
            final String entityParam = entity.getString("parameter");
            if (entityType.equals(type) && entityParam.equals(parameter))
            {
                //list does contain target GV type/param combination, return without failure
                return;
            }
        }
        //GV type/param combination not found! 
        fail("List of scheme entities does not contain an entity with type '"+ type + "' and parameter '" + parameter + "'");
    }
}
