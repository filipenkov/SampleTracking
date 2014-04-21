package com.atlassian.jira.issue.security;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.scheme.SchemeManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

@PublicApi
public interface IssueSecuritySchemeManager extends SchemeManager
{

    public List getEntitiesBySecurityLevel(Long securityLevelId) throws GenericEntityException;

    /**
     * This is a method that is meant to quickly get you all the schemes that contain an entity of the
     * specified type and parameter.
     * @param type is the entity type
     * @param parameter is the scheme entries parameter value
     * @return Collection of GenericValues that represents a scheme
     */
    public Collection<GenericValue> getSchemesContainingEntity(String type, String parameter);

}
