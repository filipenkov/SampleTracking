package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Represents the CVS Module view page.
 *
 * @since v4.4
 */
public class ViewCvsModules extends AbstractJiraAdminPage
{
    @ElementBy(id = "add_cvs_module")
    private PageElement addCvsLink;

    @Override
    public String linkId()
    {
        return "cvs_modules";
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ViewRepositories.jspa";
    }

    @Override
    public TimedCondition isAt()
    {
        return addCvsLink.timed().isPresent();
    }
}
