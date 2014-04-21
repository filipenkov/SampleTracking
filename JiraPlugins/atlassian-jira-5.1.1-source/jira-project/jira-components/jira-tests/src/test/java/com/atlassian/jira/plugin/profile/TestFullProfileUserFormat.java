package com.atlassian.jira.plugin.profile;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.plugin.userformat.FullProfileUserFormat;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
@RunWith (ListeningMockitoRunner.class)
public class TestFullProfileUserFormat
{
    @Mock private UserUtil userUtil;
    @Mock private UserFormatModuleDescriptor userFormatModuleDescriptor;
    @Mock private UserPropertyManager userPropertyManager;

    @Test
    public void testGetHtmlNullUser()
    {
        testGetHtml(null, null, "Anonymous");
    }

    @Test
    public void testGetHtmlUnknownUser()
    {
        testGetHtml("unknown", null, "unknown");
    }

    @Test
    public void testGetHtmlKnownUser()
    {
        testGetHtml("admin", new MockUser("admin"), "<a>admin</a>");
    }

    @Test
    public void testGetHtmlKnownUserWithParams()
    {
        testGetHtml("admin", new MockUser("admin"), "<a>admin</a>", EasyMap.build());
    }

    private void testGetHtml(final String username, final User user, final String expectedHtml)
    {
        testGetHtml(username, user, expectedHtml, null);
    }

    private void testGetHtml(final String username, final User user, final String expectedHtml, final Map params)
    {
        final FullProfileUserFormat userFormat = new FullProfileUserFormat(null, null, null, null, null, userUtil, userFormatModuleDescriptor, userPropertyManager, null);

        when(userUtil.getUserObject(username)).thenReturn(user);

        final MapPropertySet mapPs = new MapPropertySet();
        mapPs.setMap(new HashMap());
        when(userPropertyManager.getPropertySet(user)).thenReturn(mapPs);

        final String resourceName = "view";
        final Map<String, ?> startingParams = EasyMap.build("username", username, "user", user, "action", userFormat, "navWebFragment", null, "id", "testid");
        when(userFormatModuleDescriptor.getHtml(resourceName, startingParams)).thenReturn(expectedHtml);

        final String html;
        if (params == null)
        {
            html = userFormat.format(username, "testid");
        }
        else
        {
            html = userFormat.format(username, "testid", params);
        }

        assertEquals(expectedHtml, html);

        verify(userFormatModuleDescriptor).getHtml(resourceName, startingParams);
        verify(userUtil).getUserObject(username);
    }
}
