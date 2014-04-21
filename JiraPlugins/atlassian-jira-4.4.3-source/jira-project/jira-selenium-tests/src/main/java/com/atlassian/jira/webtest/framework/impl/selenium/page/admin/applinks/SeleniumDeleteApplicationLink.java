package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.locator.Element;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.AbstractSeleniumAuiPageDialog;
import com.atlassian.jira.webtest.framework.page.admin.applinks.AppLinksAdminPage;
import com.atlassian.jira.webtest.framework.page.admin.applinks.DeleteApplicationLink;

/**
 * Selenium-backed delete application link dialog.
 *
 * @since v4.3
 */
public class SeleniumDeleteApplicationLink
        extends AbstractSeleniumAuiPageDialog<DeleteApplicationLink, AppLinksAdminPage>
        implements DeleteApplicationLink
{
    private final Locator confirmLocator;
    private final Locator cancelLocator;

    public SeleniumDeleteApplicationLink(SeleniumContext ctx, AppLinksAdminPage page)
    {
        super(page, ctx, "delete-application-link-dialog");
        confirmLocator = locator().combine(jQuery(".wizard-submit:visible"));
        cancelLocator = locator().combine(jQuery(".applinks-cancel-link:visible"));
    }

    @Override
    public DeleteApplicationLink clickConfirm()
    {
        clickIfPresent(confirmLocator);
        return this;
    }

    @Override
    public DeleteApplicationLink clickCancel()
    {
        clickIfPresent(cancelLocator);
        return this;
    }

    @Override
    public DeleteApplicationLink open()
    {
        throw new UnsupportedOperationException("Dariusz says this will be removed");
    }

    @Override
    protected String getOpenDialogClass()
    {
        return "aui-dialog-open";
    }
    
    private void clickIfPresent(Locator locator)
    {
        Element element = locator.element();
        if (!element.isPresent().byDefaultTimeout())
        {
            throw new IllegalStateException("Not present: " + locator);
        }

        element.click();
    }
}
