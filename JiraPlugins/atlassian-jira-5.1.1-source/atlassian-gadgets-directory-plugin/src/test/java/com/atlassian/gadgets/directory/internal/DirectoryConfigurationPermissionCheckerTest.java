package com.atlassian.gadgets.directory.internal;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.directory.spi.DirectoryPermissionService;
import com.atlassian.sal.api.user.UserManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class DirectoryConfigurationPermissionCheckerTest
{
    @Mock DirectoryPermissionService permissionService;
    @Mock UserManager userManager;

    DirectoryConfigurationPermissionChecker checker;

    @Before
    public void setUp()
    {
        checker = new DirectoryConfigurationPermissionChecker(permissionService, userManager);

        when(permissionService.canConfigureDirectory("user")).thenReturn(false);
        when(permissionService.canConfigureDirectory("admin")).thenReturn(true);
    }

    @Test
    public void assertThatAdminCanConfigureDirectoryWhenUserHasPermission()
    {
        try
        {
            checker.checkForPermissionToConfigureDirectory(createRequest("admin"));
        }
        catch (Exception e)
        {
            fail("Should not get exception when checking for permission for admin: " + e.getMessage());
        }
    }

    @Test(expected=PermissionException.class)
    public void assertThatUserCannotConfigureDirectoryWithoutPermission()
    {
        checker.checkForPermissionToConfigureDirectory(createRequest("user"));
    }

    private HttpServletRequest createRequest(String username)
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(userManager.getRemoteUsername(request)).thenReturn(username);
        return request;
    }
}
