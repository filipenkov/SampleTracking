package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.search.SearchContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker} for component fields
 *
 * @since v4.0
 */
public class ComponentSearchContextVisibilityChecker implements SearchContextVisibilityChecker
{
    private final ProjectComponentManager componentManager;

    public ComponentSearchContextVisibilityChecker(ProjectComponentManager componentManager)
    {
        this.componentManager = componentManager;
    }

    public Set<String> FilterOutNonVisibleInContext(final SearchContext searchContext, final Collection<String> ids)
    {
        final List<Long> projects = searchContext.getProjectIds();
        if (projects.size() != 1)
        {
            return Collections.emptySet();
        }
        Set<String> visibleIds = new HashSet<String>();
        for (String sid : ids)
        {
            Long lid = parseLong(sid);
            if (lid != null)
            {
                try
                {
                    Long pid = componentManager.findProjectIdForComponent(lid);
                    if (projects.contains(pid))
                    {
                        visibleIds.add(sid);
                    }
                }
                catch (EntityNotFoundException ignored) {}
            }
        }
        return visibleIds;
    }

    private Long parseLong(String str)
    {
        try
        {
            return Long.valueOf(str);
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }
}
