package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraPermissionImpl;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import org.easymock.MockControl;
import org.junit.Test;

import java.util.Collections;

/**
 *
 * @since v3.12
 */
public class TestUpgradeTask_Build296 extends ListeningTestCase
{
    @Test
    public void testDoUpgrade() throws Exception
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.getPermissions(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setReturnValue(Collections.EMPTY_LIST, 1);
        mockGlobalPermissionManager.getPermissions(Permissions.ADMINISTER);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList
                .build(new JiraPermissionImpl(Permissions.ADMINISTER, "mygroup1", GroupDropdown.DESC),
                new JiraPermissionImpl(Permissions.ADMINISTER, "mygroup2", GroupDropdown.DESC)), 1);
        mockGlobalPermissionManager.addPermission(Permissions.SYSTEM_ADMIN, "mygroup1");
        mockGlobalPermissionManagerControl.setReturnValue(true);
        mockGlobalPermissionManager.addPermission(Permissions.SYSTEM_ADMIN, "mygroup2");
        mockGlobalPermissionManagerControl.setReturnValue(true);
        mockGlobalPermissionManagerControl.replay();

        UpgradeTask_Build296 upgradeTask_build296 = new UpgradeTask_Build296(mockGlobalPermissionManager);
        upgradeTask_build296.doUpgrade(false);
        mockGlobalPermissionManagerControl.verify();
    }
    
    @Test
    public void testDoUpgradeAlreadySysAdmins() throws Exception
    {
        final MockControl mockGlobalPermissionManagerControl = MockControl.createStrictControl(GlobalPermissionManager.class);
        final GlobalPermissionManager mockGlobalPermissionManager = (GlobalPermissionManager) mockGlobalPermissionManagerControl.getMock();
        mockGlobalPermissionManager.getPermissions(Permissions.SYSTEM_ADMIN);
        mockGlobalPermissionManagerControl.setReturnValue(EasyList.build(new JiraPermissionImpl(Permissions.SYSTEM_ADMIN, "mygroup1", GroupDropdown.DESC)), 1);
        mockGlobalPermissionManagerControl.replay();

        UpgradeTask_Build296 upgradeTask_build296 = new UpgradeTask_Build296(mockGlobalPermissionManager);
        upgradeTask_build296.doUpgrade(false);
        mockGlobalPermissionManagerControl.verify();
    }
}
