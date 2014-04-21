package com.atlassian.jira.pageobjects.xsrf;

import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.apache.commons.lang.NotImplementedException;

import javax.inject.Inject;

/**
 * Represents the XSRF page. This is a page that can appear anywhere because of our XSRF magic. Because of this it
 * is not really a page.
 *
 * @since v5.0.1
 */
public class XsrfPage implements Xsrf, Page
{
    @Inject
    private PageBinder binder;

    private XsrfMessage message;

    @ElementBy (id = "xsrf-login-link")
    private PageElement loginLink;

    @Init
    public void init()
    {
        message = binder.bind(XsrfMessage.class);
    }

    public boolean isSessionExpired()
    {
        return message.isSessionExpired();
    }

    public boolean isXsrfCheckFailed()
    {
        return message.isXsrfCheckFailed();
    }

    @Override
    public boolean hasParamaters()
    {
        return message.hasParamaters();
    }

    @Override
    public boolean canRetry()
    {
        return message.canRetry();
    }

    @Override
    public <P> P retry(Class<P> page, Object... args)
    {
        message.retry();
        return binder.bind(page, args);
    }

    public JiraLoginPage login()
    {
        loginLink.click();
        return binder.bind(JiraLoginPage.class);
    }

    @Override
    public String getUrl()
    {
        throw new NotImplementedException("I'm a page that can appear anywhere so I really don't have a URL.");
    }
}
