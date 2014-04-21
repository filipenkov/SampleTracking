package com.atlassian.jira.issue.security;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
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

    @Override
    public List<GenericValue> getSchemeIssueSecurityLevels(final Long schemeId)
    {
        return null;
    }

    @Override
    public List<IssueSecurityLevel> getIssueSecurityLevels(long schemeId)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean schemeIssueSecurityExists(final Long id)
    {
        return false;
    }

    @Override
    public String getIssueSecurityName(final Long id)
    {
        return null;
    }

    @Override
    public String getIssueSecurityDescription(final Long id)
    {
        return null;
    }

    @Override
    public GenericValue getIssueSecurity(final Long id)
    {
        return null;
    }

    @Override
    public IssueSecurityLevel getSecurityLevel(long id)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<GenericValue> getUsersSecurityLevels(final GenericValue entity, final User user) throws GenericEntityException
    {
        return null;
    }

    @Override
    public List<IssueSecurityLevel> getUsersSecurityLevels(Issue issue, User user)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public List<IssueSecurityLevel> getUsersSecurityLevels(Project project, User user)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<GenericValue> getAllUsersSecurityLevels(final User user) throws GenericEntityException
    {
        return null;
    }

    @Override
    public Collection<IssueSecurityLevel> getAllSecurityLevelsForUser(User user)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<GenericValue> getAllSecurityLevels() throws GenericEntityException
    {
        return null;
    }

    @Override
    public Collection<IssueSecurityLevel> getAllIssueSecurityLevels()
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<GenericValue> getUsersSecurityLevelsByName(final User user, final String securityLevelName)
            throws GenericEntityException
    {
        return null;
    }

    @Override
    public Collection<IssueSecurityLevel> getSecurityLevelsForUserByName(User user, String securityLevelName)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<GenericValue> getSecurityLevelsByName(final String securityLevelName)
            throws GenericEntityException
    {
        return null;
    }

    @Override
    public Collection<IssueSecurityLevel> getIssueSecurityLevelsByName(String securityLevelName)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Long getSchemeDefaultSecurityLevel(final GenericValue project) throws GenericEntityException
    {
        if (project == null)
        {
            return null;
        }
        return (Long) defaultSecurityLevelMap.get(getID(project));
    }

    @Override
    public Long getDefaultSecurityLevel(Project project)
    {
        // TODO: Implement Me!
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void setDefaultSecurityLevelForProject(final Long projectId, final Long defaultSecurityLevelId) throws GenericEntityException
    {
        defaultSecurityLevelMap.put(projectId, defaultSecurityLevelId);
    }

    private Object getID(final GenericValue genericValue)
    {
        return genericValue.get("id");
    }

    @Override
    public GenericValue getIssueSecurityLevel(final Long id) throws GenericEntityException
    {
        return null;
    }

    @Override
    public void deleteSecurityLevel(final Long levelId)
    {
    }

    @Override
    public void clearUsersLevels()
    {
    }

    @Override
    public void clearProjectLevels(final GenericValue project)
    {
    }
}
