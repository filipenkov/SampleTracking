package com.atlassian.jira.plugin.profile;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.plugin.userformat.FullProfileUserFormat;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import com.opensymphony.user.User;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
public class TestFullProfileUserFormat extends ListeningTestCase
{
    @Test
    public void testGetHtmlNullUser()
    {
        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUser(null);
        mockUserUtilControl.setReturnValue(null);
        mockUserUtil.getUser(null);
        mockUserUtilControl.setReturnValue(null);
        mockUserUtilControl.replay();

        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        MockControl mockUserPropertyManagerControl = MockClassControl.createControl(UserPropertyManager.class);
        UserPropertyManager mockUserPropertyManager = (UserPropertyManager) mockUserPropertyManagerControl.getMock();
        FullProfileUserFormat userFormat = new FullProfileUserFormat(null, null, null, null, null, mockUserUtil, mockUserFormatModuleDescriptor, mockUserPropertyManager);

        mockUserPropertyManager.getPropertySet(mockUserUtil.getUser(null));
        final MapPropertySet mapPs = new MapPropertySet();
        mapPs.setMap(new HashMap());
        mockUserPropertyManagerControl.setReturnValue(mapPs);
        mockUserPropertyManagerControl.replay();

        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", null, "user", null, "action", userFormat, "navWebFragment", null, "id", "testid"));
        mockUserFormatModuleDescriptorControl.setReturnValue("Anonymous");
        mockUserFormatModuleDescriptorControl.replay();


        final String html = userFormat.format(null, "testid");
        assertEquals("Anonymous", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
    }

    @Test
    public void testGetHtmlUnknownUser()
    {
        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUser("unknown");
        mockUserUtilControl.setReturnValue(null);
        mockUserUtil.getUser("unknown");
        mockUserUtilControl.setReturnValue(null);
        mockUserUtilControl.replay();

        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        MockControl mockUserPropertyManagerControl = MockClassControl.createControl(UserPropertyManager.class);
        UserPropertyManager mockUserPropertyManager = (UserPropertyManager) mockUserPropertyManagerControl.getMock();
        FullProfileUserFormat userFormat = new FullProfileUserFormat(null, null, null, null, null, mockUserUtil, mockUserFormatModuleDescriptor, mockUserPropertyManager);
        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", "unknown", "user", null, "action", userFormat, "navWebFragment", null, "id", "testid"));
        mockUserFormatModuleDescriptorControl.setReturnValue("unknown");
        mockUserFormatModuleDescriptorControl.replay();

        mockUserPropertyManager.getPropertySet(mockUserUtil.getUser("unknown"));
        final MapPropertySet mapPs = new MapPropertySet();
        mapPs.setMap(new HashMap());
        mockUserPropertyManagerControl.setReturnValue(mapPs);
        mockUserPropertyManagerControl.replay();

        final String html = userFormat.format("unknown", "testid");
        assertEquals("unknown", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
    }

    @Test
    public void testGetHtmlKnownUser()
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User adminUser = new User("admin", mpa, new MockCrowdService());

        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUser("admin");
        mockUserUtilControl.setReturnValue(adminUser);
        mockUserUtil.getUser("admin");
        mockUserUtilControl.setReturnValue(adminUser);
        mockUserUtilControl.replay();

        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        MockControl mockUserPropertyManagerControl = MockClassControl.createControl(UserPropertyManager.class);
        UserPropertyManager mockUserPropertyManager = (UserPropertyManager) mockUserPropertyManagerControl.getMock();
        FullProfileUserFormat userFormat = new FullProfileUserFormat(null, null, null, null, null, mockUserUtil, mockUserFormatModuleDescriptor, mockUserPropertyManager);
        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", "admin", "user", adminUser, "action", userFormat, "navWebFragment", null, "id", "testid"));
        mockUserFormatModuleDescriptorControl.setReturnValue("<a>admin</a>");
        mockUserFormatModuleDescriptorControl.replay();

        mockUserPropertyManager.getPropertySet(mockUserUtil.getUser("admin"));
        final MapPropertySet mapPs = new MapPropertySet();
        mapPs.setMap(new HashMap());
        mockUserPropertyManagerControl.setReturnValue(mapPs);
        mockUserPropertyManagerControl.replay();

        final String html = userFormat.format("admin", "testid");
        assertEquals("<a>admin</a>", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
    }

    @Test
    public void testGetHtmlWithParams()
    {
        MockProviderAccessor mpa = new MockProviderAccessor();
        final User adminUser = new User("admin", mpa, new MockCrowdService());

        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.getUser("admin");
        mockUserUtilControl.setDefaultReturnValue(adminUser);
        mockUserUtilControl.replay();

        MockControl mockUserFormatModuleDescriptorControl = MockClassControl.createControl(UserFormatModuleDescriptor.class);
        UserFormatModuleDescriptor mockUserFormatModuleDescriptor = (UserFormatModuleDescriptor) mockUserFormatModuleDescriptorControl.getMock();
        MockControl mockUserPropertyManagerControl = MockClassControl.createControl(UserPropertyManager.class);
        UserPropertyManager mockUserPropertyManager = (UserPropertyManager) mockUserPropertyManagerControl.getMock();
        FullProfileUserFormat userFormat = new FullProfileUserFormat(null, null, null, null, null, mockUserUtil, mockUserFormatModuleDescriptor, mockUserPropertyManager);
        mockUserFormatModuleDescriptor.getHtml("view", EasyMap.build("username", "admin", "user", adminUser, "action", userFormat, "navWebFragment", null, "id", "testid"));
        mockUserFormatModuleDescriptorControl.setReturnValue("<a>admin</a>");
        mockUserFormatModuleDescriptorControl.replay();

        mockUserPropertyManager.getPropertySet(mockUserUtil.getUser(null));
        final MapPropertySet mapPs = new MapPropertySet();
        mapPs.setMap(new HashMap());
        mockUserPropertyManagerControl.setReturnValue(mapPs);
        mockUserPropertyManagerControl.replay();

        final String html = userFormat.format("admin", "testid", EasyMap.build());
        assertEquals("<a>admin</a>", html);

        mockUserFormatModuleDescriptorControl.verify();
        mockUserUtilControl.verify();
    }
}
