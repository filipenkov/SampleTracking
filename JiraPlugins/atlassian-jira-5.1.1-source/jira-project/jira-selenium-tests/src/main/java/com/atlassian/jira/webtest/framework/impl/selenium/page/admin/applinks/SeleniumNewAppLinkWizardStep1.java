package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.locator.Element;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.AbstractSeleniumDialogContent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizard;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizardStep1;

/**
 * Selenium-backed implementation of NewAppLinkWizardStep1.
 *
 * @since v4.3
 */
public class SeleniumNewAppLinkWizardStep1
        extends AbstractSeleniumDialogContent<NewAppLinkWizardStep1, NewAppLinkWizard>
        implements NewAppLinkWizardStep1
{
    private final Locator inputLocator;
    private final SeleniumLocator step1Detector;

    public SeleniumNewAppLinkWizardStep1(SeleniumContext ctx, NewAppLinkWizard page)
    {
        super(ctx, page);
        inputLocator = locator().combine(jQuery("#application-url:visible"));
        step1Detector = css(".dialog-title.step-1-header");
    }

    @Override
    protected SeleniumLocator detector()
    {
        return step1Detector;
    }

    @Override
    public Locator locator()
    {
        return dialog().locator();
    }

    /**
     * Returns the "Server URL" input box.
     *
     * @return an Element for the "Server URL"
     */
    @Override
    public Element serverURL()
    {
        return inputLocator.element();
    }
}
