package com.atlassian.plugins.rest.common.sal.websudo;

public interface WebSudoResourceContext
{
    /**
     * Checks if WebSudo protection is required.
     *
     * @return true if WebSudo protection should be enforced, false otherwise.
     */
    boolean shouldEnforceWebSudoProtection();
}
