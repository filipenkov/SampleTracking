package com.atlassian.jira.portal;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Generator for project and category values.
 * <p/>
 * This generator produces a map of value -&gt;label for HTML select box options. The option may look like
 * <pre>
 * All Projects
 * homosapian
 * monkey
 * </pre>
 * for projects only, or:
 * <pre>
 * Projects
 * - All Projects
 * - homosapian
 * - monkey
 * Project Categories
 * - Category 1
 * </pre>
 * for projects and categories.
 *
 * @since v3.11
 */
public class ProjectAndProjectCategoryValuesGenerator implements ValuesGenerator
{
    private static final Logger log = Logger.getLogger(ProjectAndProjectCategoryValuesGenerator.class);

    public static final class Values
    {
        /**
         * value for 'All Projects' option
         */
        public static final String ALL_PROJECTS = "allprojects";

        private static final String PROJECTS = "-1";
        private static final String CATEGORIES = "-2";
    }

    private static final class Prefix
    {
        private static final String SUB_OPTION = "- ";
        private static final String CATEGORY = "cat";
    }

    public Map<String, String> getValues(final Map/*<String, Object>*/params)
    {
        @SuppressWarnings("unchecked")
        final Map<String, String> values = new ListOrderedMap();
        try
        {
            final Map<String, String> projects = getProjects(params);

            if (projects != null)
            {
                final Map<Long, String> categories = getCategoriesForProjects(projects.keySet());

                //Loop through projects and get their categories
                if (categories.isEmpty())
                {
                    values.put(Values.ALL_PROJECTS, getText("gadget.projects.display.name.all"));
                    values.putAll(projects);
                }
                else
                {
                    values.put(Values.PROJECTS, getText("common.concepts.projects"));
                    values.put(Values.ALL_PROJECTS, Prefix.SUB_OPTION + getText("gadget.projects.display.name.all"));

                    for (final Map.Entry<String, String> entry : invertAndSort(projects).entrySet())
                    {
                        final String projectId = entry.getValue();
                        if (projectId != null)
                        {
                            values.put(projectId, Prefix.SUB_OPTION + entry.getKey());
                        }
                    }

                    values.put(Values.CATEGORIES, getText("admin.menu.projects.project.categories"));
                    for (final Map.Entry<String, Long> entry : invertAndSort(categories).entrySet())
                    {
                        values.put(Prefix.CATEGORY + entry.getValue(), Prefix.SUB_OPTION + entry.getKey());
                    }
                }
            }
            else
            {
                values.put(Values.ALL_PROJECTS, getText("gadget.projects.display.name.all"));
            }
        }
        catch (final RuntimeException e)
        {
            log.error("Could not retrieve project and project category values", e);
            return null;
        }

        return values;
    }

    /**
     * Inverts the given map and sorts it. This is useful for sorting by values.
     *
     * @param map map to invert and sort
     * @return inverted and sorted map
     */
    static <K, V> Map<V, K> invertAndSort(final Map<K, V> map)
    {
        // TODO use a GoggleCollections BiMap
        return map == null ? null : new TreeMap<V, K>(MapUtils.invertMap(map));
    }

    /**
     * Filter project ids from the given collection. Returns a set of project ids as Long objects.
     * Value is a valid project id if it is a long number.
     *
     * @param projectOrCategoryIds collection of project or category ids
     * @return set of project ids, never null
     */
    public static Set<Long> filterProjectIds(final Collection<String> projectOrCategoryIds)
    {
        final Set<Long> projectIds = new HashSet<Long>();
        if ((projectOrCategoryIds != null) && !projectOrCategoryIds.isEmpty())
        {
            for (final String id : projectOrCategoryIds)
            {
                if (!id.startsWith(Prefix.CATEGORY))
                {
                    try
                    {
                        final Long projectId = new Long(id);
                        projectIds.add(projectId);
                    }
                    catch (final NumberFormatException e)
                    {
                        // ignore if ID is not a number
                        log.warn("Project ID '" + id + "' could not be parsed!", e);
                    }
                }
            }
        }
        return projectIds;
    }

    /**
     * Filter project category ids from the given collection. Returns a set of project category ids as Long objects.
     * Value is a valid category if it takes form "catXYZ" where "cat" is the prefix ({@link Prefix#CATEGORY})
     * and XYZ is a long number.
     *
     * @param projectOrCategoryIds collection of project or category ids
     * @return set of project category ids, never null
     */
    public static Set<Long> filterProjectCategoryIds(final Collection<String> projectOrCategoryIds)
    {
        final Set<Long> categoryIds = new HashSet<Long>();
        if ((projectOrCategoryIds != null) && !projectOrCategoryIds.isEmpty())
        {
            for (final String id : projectOrCategoryIds)
            {
                if (id.startsWith(Prefix.CATEGORY))
                {
                    try
                    {
                        final Long categoryId = new Long(id.substring(Prefix.CATEGORY.length()));
                        categoryIds.add(categoryId);
                    }
                    catch (final NumberFormatException e)
                    {
                        // ignore if ID is not a number
                        log.warn("Project Category ID '" + id + "' could not be parsed!", e);
                    }
                }
            }
        }
        return categoryIds;
    }

    /**
     * Creates and returns a map of category IDs (Long) to category names (String).
     * Returns an empty map if non-enterprise edition of JIRA (project categories is an ent. feature).
     *
     * @param projectIds set of project IDs
     * @return map of category id to category name, never null
     */
    Map<Long, String> getCategoriesForProjects(final Set<String> projectIds)
    {
        final Map<Long, String> categories = new HashMap<Long, String>();
        if ((projectIds != null) && !projectIds.isEmpty())
        {
            for (final String projectId : projectIds)
            {
                if (projectId != null)
                {
                    addCategory(categories, projectId);
                }
            }
        }
        return categories;
    }

    /**
     * Adds project category to the map if project with given ID has a category.
     *
     * @param categories map of category id to category name to add to
     * @param projectId  project id to find the category for
     */
    void addCategory(final Map<Long, String> categories, final String projectId)
    {
        final Project project = getProjectManager().getProjectObj(new Long(projectId));
        if (project != null)
        {
            final GenericValue categoryGV = project.getProjectCategory();
            if (categoryGV != null)
            {
                categories.put(categoryGV.getLong("id"), categoryGV.getString("name"));
            }
        }
    }

    /**
     * Return a map of project id to project name
     *
     * @param params user params
     * @return map of project id to project name, never null
     */
    Map<String, String> getProjects(final Map<String, Object> params)
    {
        final Map<String, String> projectMap = new ProjectValuesGenerator().getValues(params);
        if (projectMap == null)
        {
            return Collections.emptyMap();
        }
        return projectMap;
    }

    ProjectManager getProjectManager()
    {
        return ManagerFactory.getProjectManager();
    }

    String getText(final String key)
    {
        return new I18nBean().getText(key);
    }
}
