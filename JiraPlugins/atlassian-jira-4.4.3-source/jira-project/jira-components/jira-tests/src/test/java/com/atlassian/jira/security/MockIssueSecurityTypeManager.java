package com.atlassian.jira.security;

import com.atlassian.jira.security.type.*;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType;
import com.atlassian.jira.user.util.MockUserManager;

import java.util.Map;
import java.util.HashMap;

/**
 * @since v3.13
 */
public class MockIssueSecurityTypeManager implements SecurityTypeManager
{
    private final Map<String, SecurityType> securityTypes = new HashMap<String, SecurityType>();

    public MockIssueSecurityTypeManager(final JiraAuthenticationContext jiraAuthenticationContext)
    {
//        <type id="reporter" enterprise="true">
//            <class>com.atlassian.jira.security.type.CurrentReporter</class>
//        </type>
        securityTypes.put("reporter", new CurrentReporter(jiraAuthenticationContext));
//        <type id="group" enterprise="false">
//            <class>com.atlassian.jira.security.type.GroupDropdown</class>
//        </type>
        securityTypes.put("group", new GroupDropdown(jiraAuthenticationContext));
//        <type id="user" enterprise="true">
//            <class>com.atlassian.jira.security.type.SingleUser</class>
//        </type>
        securityTypes.put("user", new SingleUser(jiraAuthenticationContext, new MockUserManager()));
//        <type id="lead" enterprise="true">
//            <class>com.atlassian.jira.security.type.ProjectLead</class>
//        </type>
        securityTypes.put("lead", new ProjectLead(jiraAuthenticationContext));
//        <type id="assignee" enterprise="true">
//            <class>com.atlassian.jira.security.type.CurrentAssignee</class>
//        </type>
        securityTypes.put("assignee", new CurrentAssignee(jiraAuthenticationContext));
//        <type id="userCF" enterprise="true">
//             <class>com.atlassian.jira.security.type.UserCF</class>
//        </type>
        securityTypes.put("userCF", new UserCF(jiraAuthenticationContext, null));
//        <type id="projectrole" enterprise="false">
//             <class>com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType</class>
//        </type>
        securityTypes.put("projectrole", new ProjectRoleSecurityAndNotificationType(jiraAuthenticationContext, null, null));
//        <type id="groupCF" enterprise="true">
//            <class>com.atlassian.jira.security.type.GroupCF</class>
//        </type>
        securityTypes.put("groupCF", new GroupCF(jiraAuthenticationContext, null, null));
    }

    /* Implementation of SecurityTypeManager */
    public SecurityType getSecurityType(final String id)
    {
        return securityTypes.get(id);
    }

    public Map getSecurityTypes()
    {
        return securityTypes;
    }

    public void setSecurityTypes(final Map/*<String, SecurityType>*/ securityTypes)
    {
        this.securityTypes.clear();
        this.securityTypes.putAll(securityTypes);
    }

    public boolean hasSecurityType(final String securityTypeStr)
    {
        return false;
    }

    /* Implementation of SchemeTypeManager */

    public String getResourceName()
    {
        return null;
    }

    public Class getTypeClass()
    {
        return MockIssueSecurityTypeManager.class;
    }

    public SchemeType getSchemeType(final String id)
    {
        return getSecurityType(id);
    }

    public Map getSchemeTypes()
    {
        return getSecurityTypes();
    }

    public void setSchemeTypes(final Map schemeType)
    {
    }

    public Map getTypes()
    {
        return getSecurityTypes();
    }
}
