package com.atlassian.jira.plugin.componentpanel;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.AndClause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;

import java.util.Map;

/**
 * A narrowing of context from {@link BrowseProjectContext} that also includes a {@link ProjectComponent}.
 *
 * @since v4.0
 */
public class BrowseComponentContextImpl extends BrowseProjectContext implements BrowseComponentContext
{
    private final SimpleFieldSearchConstantsWithEmpty searchConstants = SystemSearchConstants.forComponent();
    private final SearchService searchService;
    private final ProjectComponent component;
    private TerminalClause componentClause;

    public BrowseComponentContextImpl(final SearchService searchService, ProjectComponent component, User user)
    {
        // NOTE: instantiate Project with null and lazyload it by overriding #getProject()
        super(user, null);
        this.searchService = searchService;
        this.component = component;
    }

    public ProjectComponent getComponent()
    {
        return component;
    }

    public Project getProject()
    {
        if (project == null)
        {
            project = getProjectManager().getProjectObj(component.getProjectId());
        }
        return project;
    }

    public Query createQuery()
    {
        // Let the super class build its search
        final Query superQuery = super.createQuery();

        // Lets AND the other search with out portion
        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(superQuery);
        builder.where().and().addClause(getComponentSearchClause());
        return builder.buildQuery();
    }

    protected TerminalClause getComponentSearchClause()
    {
        if (componentClause == null)
        {
            componentClause = new TerminalClauseImpl(searchConstants.getJqlClauseNames().getPrimaryName(), Operator.EQUALS, component.getName());
        }
        return componentClause;
    }

    ///CLOVER:OFF
    public String getQueryString()
    {
        final QueryImpl query = new QueryImpl(new AndClause(getComponentSearchClause(), getProjectClause()));
        return searchService.getQueryString(getUser(), query);
    }
    ///CLOVER:ON

    public Map<String, Object> createParameterMap()
    {
        final Map<String, Object> map = super.createParameterMap();
        map.put("component", getComponent());
        return map;
    }

    public String getContextKey()
    {
        return super.getContextKey() + "_component" + component.getId();
    }
}
