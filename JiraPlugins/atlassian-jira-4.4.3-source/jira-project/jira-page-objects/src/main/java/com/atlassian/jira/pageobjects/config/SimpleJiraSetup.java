package com.atlassian.jira.pageobjects.config;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.setup.AdminSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.ApplicationSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.DatabaseSetupPage;
import com.atlassian.jira.pageobjects.pages.setup.MailSetupPage;
import com.atlassian.jira.webtests.LicenseKeys;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.apache.log4j.Logger;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Conditions.and;
import static com.atlassian.pageobjects.elements.query.Conditions.not;
import static com.atlassian.pageobjects.elements.query.Conditions.or;

/**
 * Implementation of JIRA setup that performs the simplest set up possible via UI.
 *
 * @since v4.4
 */
public class SimpleJiraSetup implements JiraSetup
{
    private static final Logger log = Logger.getLogger(SimpleJiraSetup.class);

    @Inject private PageBinder pageBinder;
    @Inject private JiraTestedProduct product;


    @Override
    public TimedCondition isSetUp()
    {
        return hasNotRedirectedToSetup();
    }

    private TimedCondition hasNotRedirectedToSetup()
    {
        TimedCondition isOnLogin = product.visitDelayed(JiraLoginPage.class).inject().get().isAt();
        TimedQuery<Boolean> isNotOnDbSetup = not(pageBinder.delayedBind(DatabaseSetupPage.class).inject().get().isAt());
        TimedQuery<Boolean> isNotOnAppSetup = not(pageBinder.delayedBind(ApplicationSetupPage.class).inject().get().isAt());
        TimedQuery<Boolean> isNotOnAdminSetup = not(pageBinder.delayedBind(AdminSetupPage.class).inject().get().isAt());
        TimedQuery<Boolean> isNotOnMailSetup = not(pageBinder.delayedBind(MailSetupPage.class).inject().get().isAt());
        // either went to login, or DID NOT go to any of the setup pages
        return or(isOnLogin, and(isNotOnDbSetup, isNotOnAppSetup, isNotOnAdminSetup, isNotOnMailSetup));
    }

    @Override
    public void performSetUp()
    {
        if (not(isSetUp()).byDefaultTimeout())
        {
            DelayedBinder<DatabaseSetupPage> delayedSetup = pageBinder.delayedBind(DatabaseSetupPage.class);
            if (delayedSetup.canBind())
            {
                doDbSetup(delayedSetup.bind());
            }
            // TODO do this for other pages (we might be on some other setup stage)
            pageBinder.bind(ApplicationSetupPage.class)
                    .setTitle("Testing JIRA")
                    .setLicense(LicenseKeys.V2_COMMERCIAL.getLicenseString())
                    .submit()
                    .setUsername("admin")
                    .setPasswordAndConfirmation("admin")
                    .setFullName("Administrator")
                    .setEmail("admin@stuff.com.com")
                    .submit()
                    .submitDisabledEmail();
        }
        else
        {
            log.warn("Already set up, skipping");
        }
    }

    @Override
    public void performSetUp(String jiraName, String username, String password)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetSetup()
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    private void doDbSetup(DatabaseSetupPage setupPage)
    {
        setupPage.submitInternalDb();
    }
}
