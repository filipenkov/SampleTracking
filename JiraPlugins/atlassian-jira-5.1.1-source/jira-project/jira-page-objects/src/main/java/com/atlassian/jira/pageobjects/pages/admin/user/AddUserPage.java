package com.atlassian.jira.pageobjects.pages.admin.user;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import static com.atlassian.pageobjects.elements.query.Conditions.and;

/**
 * Page for adding a new user
 *
 * @since v4.4
 */
public class AddUserPage extends AbstractJiraPage
{

    private static final String URI = "/secure/admin/user/AddUser!default.jspa";

    @ElementBy(name = "username")
    private PageElement username;

    @ElementBy(name = "password")
    private PageElement password;

    @ElementBy(name = "confirm")
    private PageElement passwordConfirmation;

    @ElementBy(name = "fullname")
    private PageElement fullName;

    @ElementBy(name = "email")
    private PageElement email;

    @ElementBy(name = "sendemail")
    private PageElement sendEmail;

    @ElementBy(id = "user-create-submit")
    private PageElement submit;

    @ElementBy (id = "user-create-cancel")
    private PageElement cancelButton;

    @Override
    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return and(username.timed().isPresent(), password.timed().isPresent(), fullName.timed().isPresent());
    }

    public AddUserPage addUser(final String username, final String password, final String fullName, final String email,
            final boolean receiveEmail)
    {
        this.username.type(username);
        this.password.type(password);
        this.passwordConfirmation.type(password);
        this.fullName.type(fullName);
        this.email.type(email);
        if(receiveEmail)
        {
            this.sendEmail.select();
        }
        return this;
    }

    public UserBrowserPage createUser()
    {
        return createUser(UserBrowserPage.class);
    }

    public AddUserPage createUserExpectingError()
    {
        return createUser(AddUserPage.class);
    }

    public <T extends Page> T createUser(Class<T> nextPage, Object...args)
    {
        submit.click();
        // TODO https://studio.atlassian.com/browse/JPO-12
        // this is now a dialog so clicking submit results in some JS brain-farting that eventually leads to page re-load
        // in the mean time the page would be bound to the old cached content and.... boooom!
        // as a work-around until this is re-implemented as dialog we re-navigate to the UserBrowser page to make sure
        // the page object gets properly re-bound (-binded?:)
        return pageBinder.navigateToAndBind(nextPage, args);
    }

    public UserBrowserPage cancelCreateUser()
    {
        cancelButton.click();
        return pageBinder.bind(UserBrowserPage.class);
    }
}
