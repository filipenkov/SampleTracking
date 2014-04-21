/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.jira.pageobjects.pages.JiraLoginPage;

public class EmptyOutgoingMailPage extends JiraLoginPage
{
    @Override
    public String getUrl()
    {
        return "/secure/admin/OutgoingMailServers.jspa";
    }
}
