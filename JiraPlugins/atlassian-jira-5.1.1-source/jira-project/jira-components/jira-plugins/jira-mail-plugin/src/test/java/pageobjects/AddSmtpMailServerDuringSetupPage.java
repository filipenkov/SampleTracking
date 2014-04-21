/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package pageobjects;

import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class AddSmtpMailServerDuringSetupPage extends AbstractEditMailServerPage
{
    @ElementBy(id = "jira-setupwizard-email-notifications-disabled")
    private PageElement disabledEmailOption;

    @ElementBy(id = "jira-setupwizard-email-notifications-enabled")
    private PageElement enabledEmailOption;

    @ElementBy(id = "jira-setupwizard-email-notifications-smtp")
    private PageElement serverTypeSmtp;

    @ElementBy(id = "jira-setupwizard-email-notifications-jndi")
    private PageElement serverTypeJndi;

    @ElementBy (id = "jira-setupwizard-submit")
    private PageElement submitButton;

    @ElementBy (id = "jira-setupwizard-test-mailserver-connection")
    private PageElement testButton;

    @Override
    public TimedCondition isAt()
    {
        return submitButton.timed().isPresent();
    }

    public AddSmtpMailServerDuringSetupPage setEmailNotifications(boolean enabled)
     {
         if (enabled)
         {
             enabledEmailOption.click();
         }
         else
         {
             disabledEmailOption.click();
         }

         return this;
     }

    public boolean isSmtpServerType() {
        return serverTypeSmtp.isSelected();
    }

    public AddSmtpMailServerDuringSetupPage setSmtpServerType() {
        serverTypeSmtp.click();
        return this;
    }

    public boolean isJndiServerType() {
        return serverTypeJndi.isSelected();
    }

    public AddSmtpMailServerDuringSetupPage setJndiServerType() {
        serverTypeJndi.click();
        return this;
    }

    public DashboardPage finish()
    {
        submitButton.click();
        return pageBinder.bind(DashboardPage.class);
    }

    public AddSmtpMailServerDuringSetupPage test() {
        testButton.click();
        return pageBinder.bind(AddSmtpMailServerDuringSetupPage.class);
    }

    public AddSmtpMailServerDuringSetupPage finishWithError()
    {
        submitButton.click();
        return pageBinder.bind(AddSmtpMailServerDuringSetupPage.class);
    }

    public boolean isSmtpPartHidden()
    {
        return !serviceProvider.isVisible()
                && !hostname.isVisible()
                && !protocol.isVisible()
                && !port.isVisible()
                && !timeout.isVisible()
                && !tlsRequired.isVisible()
                && !username.isVisible()
                && !password.isVisible();
    }

    @Override
    public boolean testSucceeded() {
        final List<WebElement> messages = driver.findElements(By.cssSelector("#test-connection-messages div.aui-message"));

        return messages.size() == 1 && messages.get(0).getAttribute("class").contains("success");
    }
}
