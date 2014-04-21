package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.AbstractSeleniumAuiPageDialog;
import com.atlassian.jira.webtest.framework.page.admin.applinks.AppLinksAdminPage;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizard;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizardStep1;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizardStep2;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizardStep3;

/**
 * Selenium-backed implementation of NewAppLinkWizard.
 *
 * @since v4.3
 */
public class SeleniumNewAppLinksWizard extends AbstractSeleniumAuiPageDialog<NewAppLinkWizard, AppLinksAdminPage>
        implements NewAppLinkWizard
{
    private final Locator stepTitleLocator;
    private final Locator errorsLocator;
    private final Locator warningsLocator;
    private final NewAppLinkWizardStep1 step1;
    private final Locator nextButtonLocator;
    private final Locator cancelLinkLocator;
    private final Locator submitLinkLocator;
    private final NewAppLinkWizardStep2 step2;
    private final NewAppLinkWizardStep3 step3;

    public SeleniumNewAppLinksWizard(SeleniumContext ctx, AppLinksAdminPage page)
    {
        super(page, ctx, "add-application-link-dialog");
        stepTitleLocator = locator().combine(css(".step-title.link-to-app-type"));
        errorsLocator = locator().combine(css(".manifest-validation-errors"));
        warningsLocator = locator().combine(css(".applinks-warning-box"));
        nextButtonLocator = locator().combine(jQuery(".applinks-next-button:visible"));
        cancelLinkLocator = locator().combine(jQuery(".applinks-cancel-link:visible"));
        submitLinkLocator = locator().combine(jQuery(".wizard-submit:visible"));
        step1 = new SeleniumNewAppLinkWizardStep1(ctx, this);
        step2 = new SeleniumNewAppLinkWizardStep2(ctx, this);
        step3 = new SeleniumNewAppLinkWizardStep3(ctx, this);
    }

    @Override
    public String title()
    {
        return stepTitleLocator.element().text().byDefaultTimeout();
    }

    @Override
    public NewAppLinkWizardStep1 step1()
    {
        return step1;
    }

    @Override
    public NewAppLinkWizardStep2 step2()
    {
        return step2;
    }

    @Override
    public NewAppLinkWizardStep3 step3()
    {
        return step3;
    }

    @Override
    public NewAppLinkWizard open()
    {
        throw new UnsupportedOperationException("Dariusz says this will be removed");
    }

    @Override
    protected String getOpenDialogClass()
    {
        return "aui-dialog-open";
    }

    @Override
    public NewAppLinkWizard clickNext()
    {
        clickIfPresent(nextButtonLocator);
        return this;
    }

    @Override
    public NewAppLinkWizard clickSubmit()
    {
        clickIfPresent(submitLinkLocator);
        return this;
    }

    @Override
    public NewAppLinkWizard clickCancel()
    {
        clickIfPresent(cancelLinkLocator);
        return this;
    }

    @Override
    public TimedCondition hasErrorMessage(String errorMessage)
    {
        return errorsLocator.element().containsText(errorMessage);
    }

    @Override
    public TimedCondition hasWarning(String warnMessage)
    {
        return warningsLocator.element().containsText(warnMessage);
    }

    /**
     * Attempts to click the element specified by the given locator, if it is present.
     *
     * @param locator a Locator
     * @throws IllegalStateException if the element not present by the default timeout
     */
    private void clickIfPresent(Locator locator)
    {
        if (!locator.element().isPresent().byDefaultTimeout())
        {
            throw new IllegalStateException("Not present: " + locator);
        }

        locator.element().click();
    }
}
