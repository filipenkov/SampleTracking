package it.com.atlassian.jira.plugin.ext.bamboo.pagecomponents.ual;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

public class ServerUrlScreen
{
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(ServerUrlScreen.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    @FindBy(id = "application-url")
    private WebElement serverUrlField;
    @FindBy(css = "#add-application-link-dialog .applinks-next-button")
    private WebElement button;
    // ---------------------------------------------------------------------------------------------------- Dependencies
    @Inject
    private AtlassianWebDriver driver;
    @Inject
    private PageBinder pageBinder;
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    public ReciprocalUrlScreen submitServerUrl(String url)
    {
        serverUrlField.sendKeys(url);
        button.click();

        // wait for new screen
        driver.waitUntilElementIsVisible(By.cssSelector("#add-application-link-dialog #reciprocalLink"));

        return pageBinder.bind(ReciprocalUrlScreen.class);
    }
    // -------------------------------------------------------------------------------------------------- Public Methods
    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
