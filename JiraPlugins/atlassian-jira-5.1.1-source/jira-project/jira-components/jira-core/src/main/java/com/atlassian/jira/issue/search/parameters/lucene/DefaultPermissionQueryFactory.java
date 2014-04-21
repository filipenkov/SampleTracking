package com.atlassian.jira.issue.search.parameters.lucene;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.RequestCacheKeys;
import com.atlassian.jira.security.SecurityTypeManager;
import com.atlassian.jira.security.type.SecurityType;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DefaultPermissionQueryFactory implements PermissionQueryFactory
{
    private static final Logger log = Logger.getLogger(DefaultPermissionQueryFactory.class);

    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final PermissionManager permissionManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final PermissionTypeManager permissionTypeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;
    private final SecurityTypeManager issueSecurityTypeManager;
    private final ProjectFactory projectFactory;

    public DefaultPermissionQueryFactory(final IssueSecurityLevelManager issueSecurityLevelManager, final PermissionManager permissionManager, final PermissionSchemeManager permissionSchemeManager, final PermissionTypeManager permissionTypeManager, final IssueSecuritySchemeManager issueSecuritySchemeManager, final SecurityTypeManager issueSecurityTypeManager, final ProjectFactory projectFactory)
    {
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.permissionManager = permissionManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.permissionTypeManager = permissionTypeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
        this.issueSecurityTypeManager = issueSecurityTypeManager;
        this.projectFactory = projectFactory;
    }

    public Query getQuery(final User searcher, final int permissionId)
    {
        try
        {
            final BooleanQuery query = new BooleanQuery();

            // This function loop around all the security types in the current scheme or schemes
            final Collection<GenericValue> projects = permissionManager.getProjects(permissionId, searcher);

            // collect unique project queries
            final Set<Query> projectQueries = new LinkedHashSet<Query>();
            for (final GenericValue projectGV : projects)
            {
                collectProjectTerms(projectGV, searcher, projectQueries, permissionId);
            }

            // add them to the permission query
            final BooleanQuery permissionQuery = new BooleanQuery();
            for (final Query projectQuery : projectQueries)
            {
                permissionQuery.add(projectQuery, BooleanClause.Occur.SHOULD);
            }

            // If you have a project query then add it and look for issue level queries
            if (!permissionQuery.clauses().isEmpty())
            {
                query.add(permissionQuery, BooleanClause.Occur.MUST);

                // collect unique issue level security queries
                final Set<Query> issueLevelSecurityQueries = new LinkedHashSet<Query>();
                issueLevelSecurityQueries.add(new TermQuery(new Term(SystemSearchConstants.forSecurityLevel().getIndexField(), "-1")));

                try
                {
                    //Also loop through the project and return the security levels this user has access
                    for (final GenericValue projectGV : projects)
                    {
                        collectSecurityLevelTerms(projectGV, searcher, issueLevelSecurityQueries);
                    }
                }
                catch (final GenericEntityException e)
                {
                    log.error("Error occurred retrieving security levels for this user");
                }

                final BooleanQuery issueLevelQuery = new BooleanQuery();
                for (final Query issueLevelSecurityQuery : issueLevelSecurityQueries)
                {
                    issueLevelQuery.add(issueLevelSecurityQuery, BooleanClause.Occur.SHOULD);
                }

                query.add(issueLevelQuery, BooleanClause.Occur.MUST);
            }

            return query;
        }
        catch (final GenericEntityException e)
        {
            log.error("Error constructing query: " + e, e);
            return null;
        }
    }

    ///CLOVER:OFF
    PermissionsFilterCache getCache()
    {
        PermissionsFilterCache cache = (PermissionsFilterCache) JiraAuthenticationContextImpl.getRequestCache().get(
            RequestCacheKeys.PERMISSIONS_FILTER_CACHE);

        if (cache == null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Creating new PermissionsFilterCache");
            }
            cache = new PermissionsFilterCache();
            JiraAuthenticationContextImpl.getRequestCache().put(RequestCacheKeys.PERMISSIONS_FILTER_CACHE, cache);
        }

        return cache;
    }

    ///CLOVER:ON

    /**
     * Loops around the permission schemes for the current project and adds a query for the SecurityType if there is one
     * in scheme.
     *
     * @param projectGV The project for which we need to construct the query
     * @param searcher The user conducting the search
     * @param queries The collection of queries already generated for projects
     * @throws org.ofbiz.core.entity.GenericEntityException If there's a problem retrieving permissions.
     */
    void collectProjectTerms(final GenericValue projectGV, final User searcher, final Set<Query> queries, final int permissionId) throws GenericEntityException
    {
        final List<GenericValue> schemes = permissionSchemeManager.getSchemes(projectGV);
        for (final GenericValue scheme : schemes)
        {
            final List<GenericValue> entities = permissionSchemeManager.getEntities(scheme, (long) permissionId);
            for (final GenericValue entity : entities)
            {
                final SecurityType securityType = permissionTypeManager.getSecurityType(entity.getString("type"));
                try
                {
                    if (userHasPermissionForProjectAndSecurityType(searcher, projectGV, entity, securityType))
                    {
                        final Query tempQuery = securityType.getQuery(searcher, projectGV, entity.getString("parameter"));
                        if (tempQuery != null)
                        {
                            queries.add(tempQuery);
                        }
                    }
                }
                catch (final Exception e)
                {
                    log.error("Could not add query for security type:" + securityType.getDisplayName(), e);
                }
            }
        }
    }

    /**
     * Loop through the user security levels for project adding them to the query if they exists
     *
     * @param project The project for which we are constructing a query for the security levels
     * @param queries The collection of queries already generated for security levels
     * @param searcher The user conducting the search
     * @throws org.ofbiz.core.entity.GenericEntityException If there's a problem retrieving security levels.
     */
    void collectSecurityLevelTerms(final GenericValue project, final User searcher, final Set<Query> queries) throws GenericEntityException
    {
        final List<GenericValue> usersSecurityLevels = issueSecurityLevelManager.getUsersSecurityLevels(project, searcher);
        for (final GenericValue securityLevel : usersSecurityLevels)
        {
            @SuppressWarnings("unchecked")
            final List<GenericValue> securities = issueSecuritySchemeManager.getEntitiesBySecurityLevel(securityLevel.getLong("id"));
            for (final GenericValue entity : securities)
            {
                final SecurityType securityType = issueSecurityTypeManager.getSecurityType(entity.getString("type"));
                if (userHasPermissionForProjectAndSecurityType(searcher, project, entity, securityType))
                {
                    final Project projectObject = projectFactory.getProject(project);
                    final Query tempQuery = securityType.getQuery(searcher, projectObject, securityLevel, entity.getString("parameter"));
                    if (tempQuery != null)
                    {
                        queries.add(tempQuery);
                    }
                }
            }
        }
    }

    /**
     * Tests if the specified user has permission for the specified security type in the specified project given the
     * context of the permission scheme entity.
     *
     * @param searcher the user; may be null if user is anonymous
     * @param project the project
     * @param entity the permission scheme entity
     * @param securityType the security type
     * @return true if the user has permission; false otherwise
     */
    boolean userHasPermissionForProjectAndSecurityType(final User searcher, final GenericValue project, final GenericValue entity, final SecurityType securityType)
    {
        boolean hasPermission;
        if (searcher == null)
        {
            hasPermission = securityType.hasPermission(project, entity.getString("parameter"));
        }
        else
        {
            hasPermission = securityType.hasPermission(project, entity.getString("parameter"), searcher, false);
        }
        return hasPermission;
    }
}
