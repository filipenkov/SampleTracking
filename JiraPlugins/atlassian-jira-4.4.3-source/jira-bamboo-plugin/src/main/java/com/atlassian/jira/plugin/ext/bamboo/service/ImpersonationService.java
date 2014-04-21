package com.atlassian.jira.plugin.ext.bamboo.service;

import org.jetbrains.annotations.NotNull;

/**
 * TODO: replace this with the service that is coming to a future SAL release
 */
public interface ImpersonationService
{
    /**
     * Delegates the given runnable so that it can run in the context of the given user
     * @param username
     * @param delegate
     * @return runnable
     */
    @NotNull
    Runnable runAsUser(@NotNull final String username, @NotNull Runnable delegate);
}
