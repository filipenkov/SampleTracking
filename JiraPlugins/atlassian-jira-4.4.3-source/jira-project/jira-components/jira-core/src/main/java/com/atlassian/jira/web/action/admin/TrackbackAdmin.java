/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

@WebSudoRequired
public class TrackbackAdmin extends ProjectActionSupport
{
    private static final String ALL_ISSUES = "allIssues";
    private static final String PUBLIC_ISSUES_ONLY = "public";
    private static final String NO_OUTGOING_PINGS = "false";

    public TrackbackAdmin(ProjectManager projectManager, PermissionManager permissionManager)
    {
        super(projectManager, permissionManager);
    }

    public String doInitial() throws Exception
    {
        return SUCCESS;
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        return super.doExecute();
    }

    public boolean isAcceptPings()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_TRACKBACK_RECEIVE);
    }

    public void setAcceptPings(boolean acceptPings)
    {
        getApplicationProperties().setOption(APKeys.JIRA_OPTION_TRACKBACK_RECEIVE, acceptPings);
    }

    public String getUrlExcludePattern()
    {
        return getApplicationProperties().getString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN);
    }

    public void setUrlExcludePattern(String urlExcludePattern)
    {
        if (TextUtils.stringSet(urlExcludePattern))
        {
            getApplicationProperties().setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, urlExcludePattern);
        }
        else
        {
            //do not store empty string "" because oracle is incapable of storing it - JRA-11956
            getApplicationProperties().setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, null);
        }
    }

    public String getSendPings()
    {
        boolean trackBackEnabled = getApplicationProperties().getOption(APKeys.JIRA_OPTION_TRACKBACK_SEND);

        if (trackBackEnabled)
        {
            boolean sendToPublicIssuesOnly = getApplicationProperties().getOption(APKeys.JIRA_OPTION_TRACKBACK_SEND_PUBLIC);
            if (!sendToPublicIssuesOnly)
            {
                return ALL_ISSUES;
            }
            else
            {
                return PUBLIC_ISSUES_ONLY;
            }
        }
        else
        {
            return NO_OUTGOING_PINGS;
        }
    }

    public void setSendPings(String sendPingsStr)
    {
        if (NO_OUTGOING_PINGS.equals(sendPingsStr))
        {
            getApplicationProperties().setOption(APKeys.JIRA_OPTION_TRACKBACK_SEND, false);
            getApplicationProperties().setOption(APKeys.JIRA_OPTION_TRACKBACK_SEND_PUBLIC, false);
        }
        else if (ALL_ISSUES.equals(sendPingsStr))
        {
            getApplicationProperties().setOption(APKeys.JIRA_OPTION_TRACKBACK_SEND, true);
            getApplicationProperties().setOption(APKeys.JIRA_OPTION_TRACKBACK_SEND_PUBLIC, false);
        }
        else if (PUBLIC_ISSUES_ONLY.equals(sendPingsStr))
        {
            getApplicationProperties().setOption(APKeys.JIRA_OPTION_TRACKBACK_SEND, true);
            getApplicationProperties().setOption(APKeys.JIRA_OPTION_TRACKBACK_SEND_PUBLIC, true);
        }
    }
//
//    public String getUrlAllowedPattern()
//    {
//        return getApplicationProperties().getString(APKeys.JIRA_TRACKBACK_SEND_ALLOWED_PATTERN);
//    }
//
//    public void setUrlAllowedPattern(String urlExcludePattern)
//    {
//        getApplicationProperties().setString(APKeys.JIRA_TRACKBACK_SEND_ALLOWED_PATTERN, urlExcludePattern);
//    }

}
