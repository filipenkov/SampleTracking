package com.atlassian.jira.security;

import com.atlassian.jira.scheme.SchemeTypeManager;
import com.atlassian.jira.security.type.SecurityType;

import java.util.Map;

public interface SecurityTypeManager extends SchemeTypeManager<SecurityType>
{
    SecurityType getSecurityType(String id);

    Map getSecurityTypes();

    void setSecurityTypes(Map<String, SecurityType> securityType);

    boolean hasSecurityType(String securityTypeStr);
}
