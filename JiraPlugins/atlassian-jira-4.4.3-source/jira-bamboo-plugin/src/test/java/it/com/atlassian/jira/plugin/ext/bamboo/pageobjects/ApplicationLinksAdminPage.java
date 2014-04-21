package it.com.atlassian.jira.plugin.ext.bamboo.pageobjects;

import com.atlassian.jira.pageobjects.pages.AbstractJiraAdminPage;
import it.com.atlassian.jira.plugin.ext.bamboo.pagecomponents.ual.AddApplicationLinkDialog;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class ApplicationLinksAdminPage extends AbstractJiraAdminPage
{

    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(ApplicationLinksAdminPage.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    @FindBy(id = "add-application-link")
    private WebElement addApplicationLink;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public String linkId()
    {
        return null;
    }

    public String getUrl()
    {
        return "/plugins/servlet/applinks/listApplicationLinks";
    }

    public AddApplicationLinkDialog addApplicationLink()
    {
        addApplicationLink.click();
        driver.waitUntilElementIsVisible(By.id("add-application-link-dialog"));
        return pageBinder.bind(AddApplicationLinkDialog.class);
    }

    public void clearAllApplicationLinks()
    {

        if (driver.elementIsVisible(By.id("add-first-application-link")))
        {
            return;
        }

        // todo there can be a second step in the deletion process.
        driver.waitUntilElementIsVisible(By.id("application-links-table"));
        List<WebElement> elements = driver.findElements(By.className("app-delete-link"));
        for (WebElement element : elements)
        {
            element.click();
            driver.waitUntilElementIsNotVisible(By.className("delete-applink-loading"));
            driver.waitUntilElementIsVisible(By.cssSelector("#delete-application-link-dialog .wizard-submit"));
            driver.findElement(By.cssSelector("#delete-application-link-dialog .wizard-submit")).click();
            driver.waitUntilElementIsNotVisible(By.id("delete-application-link-dialog"));
        }
    }
    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
