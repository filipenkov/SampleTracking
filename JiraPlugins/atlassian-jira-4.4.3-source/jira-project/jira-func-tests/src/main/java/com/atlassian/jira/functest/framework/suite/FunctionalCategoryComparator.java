package com.atlassian.jira.functest.framework.suite;

import com.google.common.collect.ImmutableSet;

import java.util.Comparator;
import java.util.Set;

/**
 * Compares tests using their functional categories. Useful for keeping consistent ordering across the whole suite.
 *
 * @since v4.3
 */
public class FunctionalCategoryComparator implements Comparator<Class<?>>
{

    public static final FunctionalCategoryComparator INSTANCE = new FunctionalCategoryComparator();

    /**
     * Collection of all categories marking functional 'areas' of web tests.
     *
     */
    private static final Set<Category> FUNC_TEST_CATEGORIES = ImmutableSet.of(
            Category.ADMINISTRATION,
            Category.APP_LINKS,
            Category.ATTACHMENTS,
            Category.BROWSE_PROJECT,
            Category.BROWSING,
            Category.BULK_OPERATIONS,
            Category.CHARTING,
            Category.CLONE_ISSUE,
            Category.COMMENTS,
            Category.COMPONENTS_AND_VERSIONS,
            Category.CUSTOM_FIELDS,
            Category.DASHBOARDS,
            Category.EMAIL,
            Category.FIELDS,
            Category.FILTERS,
            Category.GADGETS,
            Category.HTTP,
            Category.I18N,
            Category.IMPORT_EXPORT,
            Category.INDEXING,
            Category.ISSUE_NAVIGATOR,
            Category.ISSUES,
            Category.JELLY,
            Category.JQL,
            Category.LDAP,
            Category.LICENSING,
            Category.MOVE_ISSUE,
            Category.PERMISSIONS,
            Category.RELOADABLE_PLUGINS,
            Category.PORTLETS,
            Category.PROJECT_IMPORT,
            Category.PROJECTS,
            Category.QUARTZ,
            Category.REPORTS,
            Category.REST,
            Category.ROLES,
            Category.SCHEMES,
            Category.SECURITY,
            Category.SETUP,
            Category.SUB_TASKS,
            Category.TIME_TRACKING,
            Category.UPGRADE_TASKS,
            Category.USERS_AND_GROUPS,
            Category.WORKFLOW,
            Category.WORKLOGS
    );

    public static boolean isFunctionalCategory(Category category)
    {
        return FUNC_TEST_CATEGORIES.contains(category);
    }

    @Override
    public int compare(Class<?> test1, Class<?> test2)
    {
        final Category category1 = getCategory(test1);
        final Category category2 = getCategory(test2);
        if (category1 == null && category2 == null)
        {
            return compareByName(test1,test2);
        }
        if (category1 == null)
        {
            // non-functional test classes come last
            return 1;
        }
        if (category2 == null)
        {
            // non-functional test classes come last
            return -1;
        }
        // let enum ordinal decide
        int result = category1.compareTo(category2);
        return result != 0 ? result : compareByName(test1, test2);
    }

    private Category getCategory(Class<?> test1)
    {
        WebTest webTest = test1.getAnnotation(WebTest.class);
        if (webTest == null)
        {
            return null;
        }
        for (Category category : webTest.value())
        {
            if (isFunctionalCategory(category))
            {
                return category;
            }
        }
        return null;
    }

    private int compareByName(Class<?> test1, Class<?> test2)
    {
        return test1.getSimpleName().compareTo(test2.getSimpleName());
    }
}
