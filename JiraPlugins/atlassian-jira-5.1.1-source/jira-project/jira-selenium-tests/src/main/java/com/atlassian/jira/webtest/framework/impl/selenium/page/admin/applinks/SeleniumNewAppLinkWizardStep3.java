package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.SeleniumInput;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.AbstractSeleniumDialogContent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizard;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizardStep3;

/**
 * Selenium-backed implementation of NewAppLinkWizardStep3.
 *
 * @since v4.3
 */
public class SeleniumNewAppLinkWizardStep3 extends AbstractSeleniumDialogContent<NewAppLinkWizardStep3, NewAppLinkWizard>
        implements NewAppLinkWizardStep3
{
    private final SeleniumLocator step3Detector;
    private final Input haveTheSameUserbaseRadioBtn;
    private final Input trustEachOtherRadioBtn;


    public SeleniumNewAppLinkWizardStep3(SeleniumContext ctx, NewAppLinkWizard dialog)
    {
        super(ctx, dialog);
        step3Detector = css(".dialog-title.step-3-header");
        haveTheSameUserbaseRadioBtn = new SeleniumInput(locator().combine(jQuery("'input[name='authentication']:visible'")), ctx);
        trustEachOtherRadioBtn = new SeleniumInput(locator().combine(jQuery("'input[name='trust']:visible'")), ctx);
    }

    @Override
    public Input haveTheSameUserbase()
    {
        return haveTheSameUserbaseRadioBtn;
    }

    @Override
    public Input trustEachOther()
    {
        return trustEachOtherRadioBtn;
    }

    @Override
    public SeleniumLocator locator()
    {
        return (SeleniumLocator) dialog().locator();
    }

    @Override
    protected SeleniumLocator detector()
    {
        return step3Detector;
    }
}
