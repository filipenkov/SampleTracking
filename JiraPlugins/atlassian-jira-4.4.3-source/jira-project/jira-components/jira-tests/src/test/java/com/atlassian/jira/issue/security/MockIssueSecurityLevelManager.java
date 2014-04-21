package com.atlassian.jira.issue.security;

import com.atlassian.crowd.embedded.api.User;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock IssueSecurityLevelManager
 *
 * @since v3.13
 */
public class MockIssueSecurityLevelManager implements IssueSecurityLevelManager
{
    private Map defaultSecurityLevelMap = new HashMap();

    public List<GenericValue> getSchemeIssueSecurityLevels(final Long schemeId)
    {
        return null;
    }

    public boolean schemeIssueSecurityExists(final Long id)
    {
        return false;
    }

    public String getIssueSecurityName(final Long id)
    {
        return null;
    }

    public String getIssueSecurityDescription(final Long id)
    {
        return null;
    }

    public GenericValue getIssueSecurity(final Long id)
    {
        return null;
    }

    public List<GenericValue> getUsersSecurityLevels(final GenericValue entity, final User user) throws GenericEntityException
    {
        return null;
    }

    public List<GenericValue> getUsersSecurityLevels(final GenericValue entity, final com.opensymphony.user.User user)
    {
        return null;
    }

    public Collection<GenericValue> getAllUsersSecurityLevels(final User user) throws GenericEntityException
    {
        return null;
    }

    public Collection<GenericValue> getAllUsersSecurityLevels(final com.opensymphony.user.User user)
            throws GenericEntityException
    {
        return null;
    }

    public Collection<GenericValue> getAllSecurityLevels() throws GenericEntityException
    {
        return null;
    }

    public Collection<GenericValue> getUsersSecurityLevelsByName(final User user, final String securityLevelName)
            throws GenericEntityException
    {
        return null;
    }

    public Collection<GenericValue> getUsersSecurityLevelsByName(final com.opensymphony.user.User user, final String securityLevelName)
            throws GenericEntityException
    {
        return null;
    }

    public Collection<GenericValue> getSecurityLevelsByName(final String securityLevelName)
            throws GenericEntityException
    {
        return null;
    }

    public Long getSchemeDefaultSecurityLevel(final GenericValue project) throws GenericEntityException
    {
        if (project == null)
        {
            return null;
        }
        return (Long) defaultSecurityLevelMap.get(getID(project));
    }

    public void setDefaultSecurityLevelForProject(final Long projectId, final Long defaultSecurityLevelId) throws GenericEntityException
    {
        defaultSecurityLevelMap.put(projectId, defaultSecurityLevelId);
    }

    private Object getID(final GenericValue genericValue)
    {
        return genericValue.get("id");
    }

    public GenericValue getIssueSecurityLevel(final Long id) throws GenericEntityException
    {
        return null;
    }

    public void deleteSecurityLevel(final Long levelId) throws GenericEntityException
    {
    }

    public void clearUsersLevels()
    {
    }

    public void clearProjectLevels(final GenericValue project)
    {
    }
}
