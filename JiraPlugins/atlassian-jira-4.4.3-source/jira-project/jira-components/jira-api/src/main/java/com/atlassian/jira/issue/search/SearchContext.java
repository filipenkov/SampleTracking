package com.atlassian.jira.issue.search;

import com.atlassian.jira.issue.context.IssueContext;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public interface SearchContext
{

    /**
     * Returns whether the context is <em>global</em> or not. A context is global when there are no project
     * restrictions and no project category restrictions.
     * @return boolean
     */
    boolean isForAnyProjects();

    /**
     * Returns true if no specific issue types have been selected
     * @return boolean
     */
    boolean isForAnyIssueTypes();

    boolean isSingleProjectContext();

    /**
     * Returns selected categories
     * @return Empty list if no categories were selected
     */
    List getProjectCategoryIds();

    /**
     * Project ids as Longs.
     * @return List of Long objects, possibly empty.
     */
    List<Long> getProjectIds();

    GenericValue getOnlyProject();

    /**
     * Issue ids as Strings
     * @return List of issue type ids possibly empty.
     */
    List<String> getIssueTypeIds();

    /**
     * Gets the search context as a list of {@link IssueContext} objects
     * @return List of {@link IssueContext}. If no issue types or projects selected. A blank issue context is returned. Never null.
     */
    List<IssueContext> getAsIssueContexts();

    /**
     * Verifies that all issue types and projects in the context actually still exists. This might not be the case.
     * Also removes any projects or issue types from this SearchContext that do not (any longer) exist in the backing
     * store. 
     */
    void verify();

}
