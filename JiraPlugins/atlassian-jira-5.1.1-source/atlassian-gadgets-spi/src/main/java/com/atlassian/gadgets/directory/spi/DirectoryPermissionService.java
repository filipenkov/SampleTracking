package com.atlassian.gadgets.directory.spi;

import javax.annotation.Nullable;

/**
 * Provide a way to determine if a user has permission to perform the given operations on the directory.
 * 
 * @since 2.0
 */
public interface DirectoryPermissionService
{
    /**
     * Returns {@code true} if the user identified by {@code username} has permission to add new external gadgets to the
     * directory, remove external gadgets that were previously added, and otherwise configure the directory.  Returns
     * {@code false} if the specified user does not have this permission.  Generally, this permission should be
     * restricted to administrators, as gadgets have security considerations that can impact the whole system.
     *
     * @param username the name of the user logged in, {@code null} if no user is currently logged in
     * @return {@code true} if the user identified by {@code username} has permission to configure the directory,
     *         {@code false} otherwise
     */
    boolean canConfigureDirectory(@Nullable String username);
}
