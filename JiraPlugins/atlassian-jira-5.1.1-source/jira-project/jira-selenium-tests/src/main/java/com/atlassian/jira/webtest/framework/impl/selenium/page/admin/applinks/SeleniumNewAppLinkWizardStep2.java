package com.atlassian.jira.webtest.framework.impl.selenium.page.admin.applinks;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.component.Checkbox;
import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.SeleniumCheckbox;
import com.atlassian.jira.webtest.framework.impl.selenium.core.component.SeleniumInput;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.AbstractSeleniumDialogContent;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizard;
import com.atlassian.jira.webtest.framework.page.admin.applinks.NewAppLinkWizardStep2;
import com.atlassian.webtest.ui.keys.CharacterKeySequence;
import com.atlassian.webtest.ui.keys.KeySequence;

/**
 * Selenium-backed implementation of NewAppLinkWizardStep2.
 *
 * @since v4.3
 */
public class SeleniumNewAppLinkWizardStep2
        extends AbstractSeleniumDialogContent<NewAppLinkWizardStep2, NewAppLinkWizard>
        implements NewAppLinkWizardStep2

{
    private final SeleniumLocator step2Detector;
    private final Input reciprocalUrlInput;
    private final Input usernameInput;
    private final Input passwordInput;

    private final Checkbox reciprocalLinkCheckbox;

    public SeleniumNewAppLinkWizardStep2(SeleniumContext ctx, NewAppLinkWizard dialog)
    {
        super(ctx, dialog);
        step2Detector = css(".dialog-title.step-2-ual-header");
        reciprocalUrlInput = new SeleniumInput(locator().combine(css(".reciprocal-rpc-url")), ctx);
        reciprocalLinkCheckbox = new SeleniumCheckbox(locator().combine(id("reciprocalLink")), ctx, Timeouts.AJAX_ACTION);
        usernameInput = new SeleniumInput(locator().combine(jQuery("#reciprocal-link-username")), ctx);
        passwordInput = new SeleniumInput(locator().combine(jQuery("#reciprocal-link-password")), ctx);
    }

    @Override
    public NewAppLinkWizardStep2 insertApplicationName(KeySequence applicationName)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public NewAppLinkWizardStep2 selectApplicationType(ApplicationType applicationType)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Input getReciprocalURL()
    {
        return reciprocalUrlInput;
    }

    @Override
    public NewAppLinkWizardStep2 enterUsername(String username)
    {
        usernameInput.type(new CharacterKeySequence(username));
        return this;
    }

    @Override
    public NewAppLinkWizardStep2 enterPassword(String password)
    {
        passwordInput.type(new CharacterKeySequence(password));
        return this;
    }

    @Override
    public NewAppLinkWizardStep2 enterRpcUrl(String rpcUrl)
    {
        reciprocalUrlInput.type(new CharacterKeySequence(rpcUrl));
        return this;
    }

    /**
     * Returns the create reciprocal link checkbox.
     *
     * @return a Checkbox
     */
    @Override
    public Checkbox createReciprocalLink()
    {
        return reciprocalLinkCheckbox;
    }

    @Override
    protected SeleniumLocator detector()
    {
        return step2Detector;
    }

    @Override
    public SeleniumLocator locator()
    {
        return (SeleniumLocator) dialog().locator();
    }
}
