package com.atlassian.jira.pageobjects.pages.admin.user;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Author: Geoffrey Wong
 * JIRA administration page to edit the user details (i.e. Full name and email) of a JIRA user
 */
public class EditUserDetailsPage extends AbstractJiraPage
{
    private static final String URI_TEMPLATE = "/secure/admin/user/EditUser!default.jspa?editName=";

    @ElementBy (id = "user-edit-fullName")
    private PageElement currentUserFullName;

    @ElementBy (id = "user-edit-email")
    private PageElement currentUserEmail;

    @ElementBy (id = "user-edit-submit")
    private PageElement update;

    @ElementBy (id = "user-edit-cancel")
    private PageElement cancel;
    
    @ElementBy (id = "user-edit-active")
    private PageElement activeUser;

    private final String username;
    
    public EditUserDetailsPage(String username)
    {
        this.username = username;
    }
    
    public String getUrl()
    {
        return URI_TEMPLATE + username;
    }

    @Override
    public TimedCondition isAt()
    {
        return update.timed().isPresent();
    }
    
    public String getCurrentUserFullName()
    {
        return currentUserFullName.getValue();
    }
    
    public String getCurrentUserEmail()
    {
        return currentUserEmail.getValue();
    }

    public boolean getIsActiveUser()
    {
        return activeUser.isSelected();
    }

    public EditUserDetailsPage setUserAsActive()
    {
        if (!getIsActiveUser())
        {
            activeUser.select();
        }

        return this;
    }

    public EditUserDetailsPage setUserAsInactive()
    {
        if (getIsActiveUser())
        {
            activeUser.click();
        }

        return this;
    }

    public EditUserDetailsPage fillUserFullName(String newUserFullName)
    {
        currentUserFullName.clear().type(newUserFullName);
        return this;
    }
    
    public EditUserDetailsPage fillUserEmail(String newUserEmail)
    {
        currentUserEmail.clear().type(newUserEmail);
        return this;
    }

    public ViewUserPage cancelEdit()
    {
        cancel.click();
        return pageBinder.bind(ViewUserPage.class, username);
    }

    public ViewUserPage submit()
    {
        update.click();
        return pageBinder.bind(ViewUserPage.class, username);
    }
}
