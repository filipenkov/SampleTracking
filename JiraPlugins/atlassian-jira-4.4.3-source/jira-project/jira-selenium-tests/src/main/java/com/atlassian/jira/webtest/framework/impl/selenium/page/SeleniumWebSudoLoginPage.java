package com.atlassian.jira.webtest.framework.impl.selenium.page;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.page.WebSudoLoginPage;
import com.atlassian.webtest.ui.keys.KeySequence;
import com.atlassian.webtest.ui.keys.Sequences;

/**
 * @since 4.3
 */
public class SeleniumWebSudoLoginPage extends AbstractSeleniumPage implements WebSudoLoginPage
{
    private final Locator passwordLocator;
    private final Locator submitLocator;

    public SeleniumWebSudoLoginPage(SeleniumContext seleniumContext)
    {
        super(seleniumContext);
        passwordLocator = id("login-form-authenticatePassword");
        submitLocator = id("authenticateButton");
    }

    protected Locator detector()
    {
        return passwordLocator;
    }

    public void setPassword(String password)
    {
        KeySequence keySequence = Sequences.chars(password);
        passwordLocator.element().type(keySequence);
    }

    public void submit()
    {
        submitLocator.element().click();
        waitFor().pageLoad();
    }
}
