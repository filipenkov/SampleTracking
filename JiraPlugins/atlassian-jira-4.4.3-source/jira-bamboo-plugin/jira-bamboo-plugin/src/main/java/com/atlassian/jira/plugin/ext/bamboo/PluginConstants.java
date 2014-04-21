package com.atlassian.jira.plugin.ext.bamboo;

import org.apache.log4j.Logger;

public final class PluginConstants
{
    private static final Logger log = Logger.getLogger(PluginConstants.class);
    // ------------------------------------------------------------------------------------------------------- Constants

    /**
     * Release Tab Panel module key
     */
    public static final String BAMBOO_RELEASE_TABPANEL_MODULE_KEY = "bamboo-release-tabpanel";

    /**
     * Keys for storing release information in Plugin Settings (PS)
     */
    public static final String PS_CONFIG_DATA_KEY = "bamboo.release.config.";
    public static final String PS_CONFIG_DEFAUTS_KEY = "bamboo.release.config.defaults";
    public static final String PS_CONFIG_PLAN = "plan";
    public static final String PS_CONFIG_STAGE = "stage";
    public static final String PS_CONFIG_USER_NAME = "user";
    public static final String PS_CONFIG_OPEN_ISSUES = "openIssuesAction";
    public static final String PS_CONFIG_OPEN_ISSUES_VERSION = "openIssuesMoveVersion";
    public static final String PS_CONFIG_BUILD_TYPE = "buildType";
    public static final String ISSUE_ACTION_IGNORE = "ignore";
    public static final String ISSUE_ACTION_MOVE = "move";

    public static final String PS_BUILD_DATA_KEY = "bamboo.release.build.";
    public static final String PS_BUILD_COMPLETED_STATE = "state";
    public static final String PS_BUILD_RESULT = "result";
    public static final String PS_RELEASE_COMPLETE = "completed";

    public static final String PS_RELEASE_ERRORS = "bamboo.release.errors.";

    /** variables for UI are prefixed with this **/
    public static final String VARIABLE_PARAM_PREFIX = "variable_";
    /** variables for REST requests are prefixed with this **/
    public static final String VARIABLE_REST_PREFIX = "bamboo.variable.";

    public static final String CREDENTIALS_REQUIRED = "Credentials required";
    public static final String LOGIN_AND_APPROVE_BEFORE_CONTINUING = "You will need to log in and approve before continuing with the release.";
    public static final String LOGIN_AND_APPROVE_BEFORE_STATUS_VISIBLE = "You will need to log in and approve to see the status of the release build.";
    public static final String BAMBOO_UNREACHABLE_MSG = "Bamboo is unreachable at this time.";
    public static final String BAMBOO_UNREACHABLE_TITLE = "Bamboo Unreachable";

    public static final String BUILD_TYPE_NO_BUILD = "no-build";
    public static final String BUILD_TYPE_NEW_BUILD = "new-build";
    public static final String BUILD_TYPE_EXISTING_BUILD = "existing-build";

    private PluginConstants()
    {
    }
}
