/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Set;


public abstract class AbstractIssueFieldSecurityType extends AbstractSecurityType
{
    private static final Logger log = Logger.getLogger(AbstractIssueFieldSecurityType.class);

    protected abstract String getFieldName();

    public Query getQuery(com.opensymphony.user.User searcher, GenericValue entity, String parameter)
    {
        if (entity == null)
        {
            return null;
        }

        //Check to see if it is a project
        boolean hasQuery = false;
        BooleanQuery query = new BooleanQuery();
        if ("Project".equals(entity.getEntityName()))
        {
            PermissionSchemeManager permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
            try
            {
                List schemes = permissionSchemeManager.getSchemes(entity);
                for (int i = 0; i < schemes.size(); i++)
                {
                    GenericValue scheme = (GenericValue) schemes.get(i);
                    if (permissionSchemeManager.getEntities(scheme, getType(), new Long(Permissions.BROWSE)).size() > 0)
                    {
                        BooleanQuery projectAndUserQuery = getQueryForProject(entity, searcher);
                        if (projectAndUserQuery != null)
                        {
                            query.add(projectAndUserQuery, BooleanClause.Occur.SHOULD);
                            hasQuery = true;
                        }
                    }
                }
            }
            catch (Exception e)
            {
                log.error("Could not retrieve scheme for this project.");
            }
        }
        else if ("SchemeIssueSecurityLevels".equals(entity.getEntityName()))
        {
            BooleanQuery queryForSecurityLevel = getQueryForSecurityLevel(entity, searcher);
            if (queryForSecurityLevel != null)
            {
                query.add(queryForSecurityLevel, BooleanClause.Occur.MUST);
                hasQuery = true;
            }
        }
        if (hasQuery)
        {
            return query;
        }
        else
        {
            return null;
        }
    }

    /*
     * Ignore project for most types.
     */
    public Query getQuery(com.opensymphony.user.User searcher, Project project, GenericValue securityLevel, String parameter)
    {
        return getQuery(searcher, securityLevel, parameter);
    }

    /**
     * Gets called to produce the Lucene query for a project
     * @param project The project for which to construct a query
     * @param searcher The user who is searching to add to the query
     * @return A BooleanQuery with the project and searcher terms
     */
    protected BooleanQuery getQueryForProject(GenericValue project, com.opensymphony.user.User searcher)
    {
        BooleanQuery projectAndUserQuery = new BooleanQuery();
        Query projectQuery = new TermQuery(new Term(DocumentConstants.PROJECT_ID, project.getString("id")));
        Query userQuery = new TermQuery(new Term(getFieldName(), searcher.getName()));
        projectAndUserQuery.add(projectQuery, BooleanClause.Occur.MUST);
        projectAndUserQuery.add(userQuery, BooleanClause.Occur.MUST);
        return projectAndUserQuery;
    }

    /**
     * Produces a Lucene query for a given issue security type such that documents
     * match the query only when the given user is defined for the issue by this
     * custom field in the given security.
     *
     * @param issueSecurity the security defined by this IssueFieldSecurityType instance.
     * @param searcher      the user.
     * @return a query to constrain to the given issue security for the given user or null if user is null.
     */
    protected BooleanQuery getQueryForSecurityLevel(GenericValue issueSecurity, com.opensymphony.user.User searcher)
    {
        BooleanQuery issueLevelAndUserQuery = null;
        if (searcher != null)
        {
            issueLevelAndUserQuery = new BooleanQuery();
            //We wish to ensure that the search has the value of the field
            Term securityLevelIsSet = new Term(DocumentConstants.ISSUE_SECURITY_LEVEL, issueSecurity.getString("id"));
            issueLevelAndUserQuery.add(new TermQuery(securityLevelIsSet), BooleanClause.Occur.MUST);
            Term customFieldSpecifiesUser = new Term(getFieldName(), searcher.getName());
            issueLevelAndUserQuery.add(new TermQuery(customFieldSpecifiesUser), BooleanClause.Occur.MUST);
        }

        return issueLevelAndUserQuery;
    }

    public boolean hasPermission(GenericValue entity, String argument)
    {
        return false;
    }

    /**
     * Decides if the given User has permission to see the given issue or project.
     * If the user is null they can never have the permission so false is returned.
     * If the entity is a Project the permission is always true as report and
     * assignee have no context in a project only on the issues with the project
     * It the entity is an Issue check if the user is in the relevent field in the issue
     *
     * @param entity        The Generic Value. Shoule be an Issue
     * @param argument      Not needed for this implementation
     * @param user          User to check the permission on. If it is null then the check is made on the current user
     * @param issueCreation NFI
     * @return true if the user is the current assignee otherwise false
     * @see CurrentReporter#hasPermission
     * @see ProjectLead#hasPermission
     * @see SingleUser#hasPermission
     * @see GroupDropdown#hasPermission
     */
    public boolean hasPermission(GenericValue entity, String argument, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
    {

        if (user == null)
        {
            return false;
        }
        else
        {
            if (entity != null)
            {
                if ("Issue".equals(entity.getEntityName()))
                {
                    return hasIssuePermission(user, issueCreation, entity, argument);
                }
                else
                {
                    return "Project".equals(entity.getEntityName()) && hasProjectPermission(user, issueCreation, entity);
                }
            }
        }

        return false;
    }

    protected abstract boolean hasIssuePermission(User user, boolean issueCreation, GenericValue issueGv, String argument);

    protected abstract boolean hasProjectPermission(User user, boolean issueCreation, GenericValue project);

    public Set<com.opensymphony.user.User> getUsers(PermissionContext ctx, String argument)
    {
        throw new UnsupportedOperationException();
    }


}
