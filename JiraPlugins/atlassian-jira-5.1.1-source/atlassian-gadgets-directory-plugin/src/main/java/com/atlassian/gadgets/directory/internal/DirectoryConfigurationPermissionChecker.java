package com.atlassian.gadgets.directory.internal;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.directory.spi.DirectoryPermissionService;
import com.atlassian.sal.api.user.UserManager;

/**
 * Checks that the user making a request to add a gadget to the directory has permission
 * to do so.
 */
public class DirectoryConfigurationPermissionChecker
{
    private final DirectoryPermissionService permissionService;
    private final UserManager userManager;

    /**
     * Constructor.
     * @param permissionService the {@code PermissionService} implementation to use
     * @param userManager the {@code UserManager} implementation to use
     */
    public DirectoryConfigurationPermissionChecker(DirectoryPermissionService permissionService, UserManager userManager)
    {
        this.permissionService = permissionService;
        this.userManager = userManager;
    }

    /**
     * Check that the user making the {@code request} has permission to add entries to or remove entries from the
     * directory.
     *
     * @param request request to pull user information from
     * @throws PermissionException thrown if the user making the request does not have permission to add to the
     * directory
     */
    public void checkForPermissionToConfigureDirectory(HttpServletRequest request) throws PermissionException
    {
        if (!permissionService.canConfigureDirectory(userManager.getRemoteUsername(request)))
        {
            throw new PermissionException();
        }
    }
}
