package com.atlassian.jira.issue.search.quicksearch;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import org.apache.commons.collections.map.MultiValueMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Quick search handler for components. Note that this handler needs to run after the Project Handler has run.
 *
 * @since v3.13
 */
public class ComponentQuickSearchHandler extends PrefixedSingleWordQuickSearchHandler
{
    private static final String PREFIX = "c:";

    private final ProjectComponentManager projectComponentManager;
    private final ProjectAwareQuickSearchHandler projectAwareQuickSearchHandler;

    public ComponentQuickSearchHandler(final ProjectComponentManager projectComponentManager, final ProjectManager projectManager, final PermissionManager permissionManager, final JiraAuthenticationContext authenticationContext)
    {
        this.projectComponentManager = projectComponentManager;
        projectAwareQuickSearchHandler = new ProjectAwareQuickSearchHandlerImpl(projectManager, permissionManager, authenticationContext);
    }

    protected Map/*<String, String>*/handleWordSuffix(final String wordSuffix, final QuickSearchResult searchResult)
    {
        final List possibleProjects = projectAwareQuickSearchHandler.getProjects(searchResult);

        final Map/*<String, String>*/components = new MultiValueMap();
        for (final Iterator iterator = possibleProjects.iterator(); iterator.hasNext();)
        {
            final GenericValue project = (GenericValue) iterator.next();
            getComponentsByName(project, wordSuffix, components);
        }
        return components;
    }

    protected String getPrefix()
    {
        return PREFIX;
    }

    private void getComponentsByName(final GenericValue project, final String word, final Map/*<String, String>*/components)
    {
        getAllNamesMatchingSubstring(projectComponentManager.findAllForProject(project.getLong("id")), word, components);
    }

    protected void getAllNamesMatchingSubstring(final Collection/*<ProjectComponent>*/projectComponents, final String name, final Map/*<String, String>*/existingComponents)
    {
        for (final Iterator iterator = projectComponents.iterator(); iterator.hasNext();)
        {
            final ProjectComponent projectComponent = (ProjectComponent) iterator.next();
            final String componentName = projectComponent.getName();
            if (componentName != null)
            {
                final StringTokenizer st = new StringTokenizer(componentName, " ");
                while (st.hasMoreTokens())
                {
                    final String word = st.nextToken();
                    if (name.equalsIgnoreCase(word))
                    {
                        existingComponents.put("component", projectComponent.getId().toString());
                    }
                }
            }
        }
    }
}
