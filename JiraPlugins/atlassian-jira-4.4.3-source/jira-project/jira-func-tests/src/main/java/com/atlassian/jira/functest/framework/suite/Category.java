package com.atlassian.jira.functest.framework.suite;

import java.util.EnumSet;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static java.util.Arrays.asList;

/**
 * Enumeration of possible categories of JIRA web tests.
 *
 * @since v4.4
 */
public enum Category
{
    /**
     * Generic category applied to all func tests.
     *
     */
    FUNC_TEST,

    /**
     * Generic category applied to all Selenium tests. NOTE: those tests should be steadily migrated to WebDriver suite.
     */
    SELENIUM_TEST,

    /**
     * Generic category applied to all WebDriver tests.
     *
     */
    WEBDRIVER_TEST,

    /**
     * Marks tests that can only be run in the TPM builds.
     *
     */
    TPM,

    /**
     * Platform compatibility tests.
     *
     */
    PLATFORM_COMPATIBILITY,

    /**
     * Tests for visual regression.
     *
     */
    VISUAL_REGRESSION,

    /**
     * QUnit runner.
     *
     */
    QUNIT,

    /**
     * Marks all tests that require 'dev-mode' plugins installed in JIRA.
     *
     */
    DEV_MODE,

    /**
     * Marks all tests that require the JIRA reference plugin installed in JIRA.
     *
     */
    REFERENCE_PLUGIN,

    /**
     * Requires ignite plugin to run.
     *
     */
    IGNITE,

    /**
     * Performance test
     *
     */
    PERFORMANCE,

    /**
     * Tests for plugins reloadability (involve slow data restore)
     *
     */
    RELOADABLE_PLUGINS,

//    /**
//     * Test that must be run first
//     *
//     */
//    RUN_FIRST,

    // 'functional' test categories
    ACTIVITY_STREAMS,
    ADMINISTRATION,
    API,
    APP_LINKS,
    ATTACHMENTS,
    BROWSE_PROJECT,
    BROWSING,
    BULK_OPERATIONS,
    CHARTING,
    CLONE_ISSUE,
    COMMENTS,
    COMPONENTS_AND_VERSIONS,
    CUSTOM_FIELDS,
    DASHBOARDS,
    EMAIL,
    FIELDS,
    FILTERS,
    GADGETS,
    HTTP,
    I18N,
    IMPORT_EXPORT,
    INDEXING,
    ISSUE_NAVIGATOR,
    ISSUES,
    JELLY,
    JQL,
    LDAP,
    LICENSING,
    MOVE_ISSUE,
    PERMISSIONS,
    PLUGINS,
    PORTLETS,
    PROJECT_IMPORT,
    PROJECTS,
    QUARTZ,
    REPORTS,
    REST,
    ROLES,
    SCHEMES,
    SECURITY,
    SETUP,
    SUB_TASKS,
    TIME_TRACKING,
    TIME_ZONES,
    UPGRADE_TASKS,
    USERS_AND_GROUPS,
    WORKFLOW,
    WORKLOGS,
    CHANGE_HISTORY;
    // Add more here if you need to

    public static Category forString(String constName)
    {
        notBlank("constName", constName);
        for (Category category : values())
        {
            if (category.name().equalsIgnoreCase(constName))
            {
                return category;
            }
        }
        throw new IllegalArgumentException("No corresponding Category constant for value \"" + constName + "\"");
    }

    public static Set<Category> fromAnnotation(WebTest webTest)
    {
        if (webTest == null || webTest.value().length == 0)
        {
            // EnumSet can't be created from empty collection - we get IllegalArgumentException - awesome!
            return EnumSet.noneOf(Category.class);
        }
        else
        {
            // in order to get enum set we create a list - WIN!
            return EnumSet.copyOf(asList(webTest.value()));
        }
    }
    
}
