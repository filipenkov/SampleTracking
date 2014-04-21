package com.atlassian.jira.security;

import com.atlassian.jira.issue.security.IssueSecurityTypeManager;
import com.atlassian.jira.scheme.AbstractSchemeTypeManager;
import com.atlassian.jira.security.type.SecurityType;

import java.util.Map;

public abstract class AbstractSecurityTypeManager extends AbstractSchemeTypeManager<SecurityType> implements SecurityTypeManager
{
    private Map<String, SecurityType> schemeTypes;

    public SecurityType getSecurityType(String id)
    {
        return (SecurityType) getSchemeType(id);
    }

    public Map<String, SecurityType> getSecurityTypes()
    {
        return getSchemeTypes();
    }

    public void setSecurityTypes(Map<String, SecurityType> securityTypes)
    {
        setSchemeTypes(securityTypes);
    }

    public String getResourceName()
    {
        return "permission-types.xml";
    }

    public Class getTypeClass()
    {
        return IssueSecurityTypeManager.class;
    }

    public Map<String, SecurityType> getSchemeTypes()
    {
        return schemeTypes;
    }

    public void setSchemeTypes(Map<String, SecurityType> schemeType)
    {
        schemeTypes = schemeType;
    }

    public boolean hasSecurityType(String securityTypeStr)
    {
        getTypes();
        return schemeTypes.containsKey(securityTypeStr);
    }
}
