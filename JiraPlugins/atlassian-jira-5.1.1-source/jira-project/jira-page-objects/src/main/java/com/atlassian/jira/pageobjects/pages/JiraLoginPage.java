package com.atlassian.jira.pageobjects.pages;

import com.atlassian.jira.pageobjects.elements.AuiMessage;
import com.atlassian.jira.pageobjects.elements.AuiMessages;
import com.atlassian.jira.pageobjects.global.User;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.collect.Iterables;
import org.openqa.selenium.By;

import static org.apache.commons.lang.StringUtils.stripToNull;

/**
 * Page object implementation for the LoginPage in JIRA.
 *
 * @since 4.4
 */
public class JiraLoginPage extends AbstractJiraPage implements LoginPage
{
    private static final String URI = "/login.jsp";

    public static final String USER_ADMIN = "admin";
    public static final String PASSWORD_ADMIN = "admin";

    @ElementBy(id = "login-form")
    protected PageElement loginForm;

    @ElementBy(className = "aui-message", within = "loginForm")
    protected Iterable<PageElement> messages;

    @ElementBy (name = "os_username")
    protected PageElement usernameField;

    @ElementBy (name = "os_password")
    protected PageElement passwordField;

    @ElementBy (name = "os_cookie")
    protected PageElement rememberMeTickBox;

    @ElementBy (id = "login-form-submit")
    protected PageElement loginButton;

    @ElementBy (name = "os_destination")
    protected PageElement redirect;

    @ElementBy (id = "sign-up-hint")
    protected PageElement signUpHint;

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

    public <M extends Page> M login(User user, Class<M> nextPage, Object...args)
    {
        return login(user.getUserName(), user.getPassword(), nextPage, args);
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
        usernameField.type(username);
        passwordField.type(password);

        if (rememberMe)
        {
            rememberMeTickBox.click();
        }
        followRedirect = followRedirect && redirect.isPresent() && stripToNull(redirect.getValue()) != null;
        loginButton.click();

        if (HomePage.class.isAssignableFrom(nextPage) || followRedirect)
        {
            return pageBinder.bind(nextPage, args);
        }
        else
        {
            final DelayedBinder<M> delayedPage = pageBinder.delayedBind(nextPage, args);
            if (delayedPage.canBind())
            {
                return delayedPage.bind();
            }
            else
            {
                return pageBinder.navigateToAndBind(nextPage, args);
            }
        }
    }

    public DashboardPage loginAndGoToHome(String username, String password)
    {
        return login(username, password, DashboardPage.class);
    }

    public DashboardPage loginAsSysadminAndGoToHome()
    {
        return loginAsSysAdmin(DashboardPage.class);
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

    public PageElement getSingUpHint()
    {
        return signUpHint;
    }

    public PageElement getLoginForm()
    {
        return loginForm;
    }

    /**
     * Get first error in the form. If there is no errors, this element's {@link com.atlassian.pageobjects.elements.PageElement#isPresent()}
     * method will return <code>false</code>.
     *
     * @return element representing the first error on the page
     */
    public PageElement getError()
    {
        return loginForm.find(By.cssSelector(AuiMessages.AUI_MESSAGE_ERROR_SELECTOR));
    }

    public Iterable<PageElement> getErrors()
    {
        return Iterables.filter(messages, AuiMessages.isAuiMessageOfType(AuiMessage.Type.ERROR));
    }

    public boolean hasErrors()
    {
        return !Iterables.isEmpty(getErrors());
    }
}