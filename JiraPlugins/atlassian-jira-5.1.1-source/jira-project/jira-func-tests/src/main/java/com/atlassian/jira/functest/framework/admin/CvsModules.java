package com.atlassian.jira.functest.framework.admin;

/**
 * Functionality around the administration of CVS Modules
 *
 * @since v4.1
 */
public interface CvsModules
{
    /**
     * Adds a dummy CVS module.
     *
     * @param moduleName the name of the module
     * @param logFile the filename of the dummy CVS log. Must exist inside the func test XML directory.
     */
    void addCvsModule(final String moduleName, final String logFile);
}
