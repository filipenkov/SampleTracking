package com.atlassian.jira.security.type;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.project.Project;
import com.opensymphony.user.User;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractProjectsSecurityType extends AbstractSecurityType
{
    public Query getQuery(User searcher, GenericValue entity, String parameter)
    {
        if (entity == null)
        {
            return null;
        }

        if ("Project".equals(entity.getEntityName()))
        {
            //Check to see if this permission exists in the current project scheme
            return new TermQuery(new Term(DocumentConstants.PROJECT_ID, entity.getString("id")));
        }
        else if ("SchemeIssueSecurityLevels".equals(entity.getEntityName()))
        {
            //We wish to ensure that the search has the value of the field
            return new TermQuery(new Term(DocumentConstants.ISSUE_SECURITY_LEVEL, entity.getString("id")));
        }
        else
        {
            return null;
        }
    }

    /*
     * Ignore project for most types.
     */
    public Query getQuery(User searcher, Project project, GenericValue securityLevel, String parameter)
    {
        return getQuery(searcher, securityLevel, parameter);
    }
}
