package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page object implementation for the LoginPage in JIRA.
 *
 * @since 4.4
 */
public class JiraLoginPage extends AbstractJiraPage implements LoginPage
{
    private static final String URI = "/login.jsp";

    private static final String USER_ADMIN = "admin";
    private static final String PASSWORD_ADMIN = "admin";

    @FindBy (name = "os_username")
    private WebElement usernameField;

    @FindBy (name = "os_password")
    private WebElement passwordField;

    @FindBy (name = "os_cookie")
    private WebElement rememberMeTickBox;

    @ElementBy (id = "login-form-submit")
    private PageElement loginButton;

    @ElementBy (name = "os_destination")
    private PageElement redirect;

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return loginButton.timed().isPresent();
    }

    public <M extends Page> M login(String username, String password, Class<M> nextPage, Object...args)
    {
        return login(username, password, false, false, nextPage, args);
    }

    public <M extends Page> M loginAsSysAdmin(Class<M> nextPage, Object...args)
    {
        return login(USER_ADMIN, PASSWORD_ADMIN, nextPage, args);
    }

    public <M extends Page> M loginAndFollowRedirect(String username, String password, Class<M> redirectPage, Object...args)
    {
        return login(username, password, false, true, redirectPage, args);
    }

    public <M extends Page> M loginAsSystemAdminAndFollowRedirect(Class<M> redirectPage, Object...args)
    {
        return loginAndFollowRedirect(USER_ADMIN, PASSWORD_ADMIN, redirectPage, args);
    }

    public <M extends Page> M login(String username, String password, boolean rememberMe, boolean followRedirect, Class<M> nextPage, Object... args)
    {
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);

        if (rememberMe)
        {
            rememberMeTickBox.click();
        }

        loginButton.click();

        if (HomePage.class.isAssignableFrom(nextPage) || followRedirect && redirect.isPresent() && redirect.getValue().length() > 0)
        {
            return pageBinder.bind(nextPage, args);
        }
        else
        {
            return pageBinder.navigateToAndBind(nextPage, args);
        }
    }

    @Override
    public <M extends Page> M login(String username, String password, Class<M> mClass)
    {
        return login(username, password, mClass, new Object[]{});
    }

    @Override
    public <M extends Page> M loginAsSysAdmin(Class<M> mClass)
    {
        return loginAsSysAdmin(mClass, new Object[]{});
    }
}