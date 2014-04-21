package com.atlassian.jira.pageobjects.project.summary.notifications;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;

/**
 * @since v4.4
 */
public class ProjectEmailDialog
{

    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder binder;

    @WaitUntil
    public void waitForDialogContent()
    {
        driver.waitUntilElementIsLocated(By.cssSelector("#project-email-dialog.aui-dialog-content-ready"));
    }

    @ElementBy(id="project-email-dialog", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement dialog;

    @ElementBy(id="fromAddress")
    private PageElement fromField;

    @ElementBy(id="submit")
    private PageElement submit;

    public ProjectEmailDialog setFromAddress(final String fromAddress)
    {
        getFromAddressField().clear()
                .type(fromAddress);
        
        return this;
    }

    private PageElement getFromAddressField()
    {
        return dialog.find(By.id("fromAddress"));
    }


    private PageElement getSubmit()
    {
        return dialog.find(By.id("submit"));
    }

    public String getFromAddressValue()
    {
        return getFromAddressField().getValue();
    }

    public String getError()
    {
        final PageElement errorElement = dialog.find(By.className("error"));

        if (errorElement.timed().isPresent().byDefaultTimeout())
        {
            return errorElement.getText();
        }

        return null;
    }

    public <T extends Page> T submit(final Class<T> nextPage, String... arguments)
    {
        getSubmit().click();
        waitUntilFalse("Expected dialog to be dismissed, but was not", dialog.timed().isPresent());
        return binder.bind(nextPage, arguments);
    }

    public ProjectEmailDialog submit()
    {
        getSubmit().click();
        return binder.bind(ProjectEmailDialog.class);
    }

}
