package it.com.atlassian.jira.plugin.ext.bamboo.pagecomponents.ual;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import it.com.atlassian.jira.plugin.ext.bamboo.WebDriverUtils;
import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

public class ReciprocalUrlScreen
{
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(ReciprocalUrlScreen.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties

    @FindBy(css = "#add-application-link-dialog #reciprocal-link-back-to-server")
    private WebElement reciprocalCheckbox;
    @FindBy(css = "#add-application-link-dialog #reciprocal-link-username")
    private WebElement reciprocalUsernameEl;
    @FindBy(css = "#add-application-link-dialog #reciprocal-link-password")
    private WebElement reciprocalPasswordEl;
    @FindBy(css = "#add-application-link-dialog #reciprocal-rpc-url")
    private WebElement reciprocalUrlEl;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    @Inject
    private AtlassianWebDriver driver;
    @Inject
    private PageBinder pageBinder;

    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods

    public UsersAndTrustScreen submitReciprocalDetails(boolean reciprocalLink, String reciprocalUrl, String reciprocalUser, String reciprocalPassword)
    {
        if (reciprocalLink)
        {
            reciprocalCheckbox.click();
            reciprocalUsernameEl.sendKeys(reciprocalUser);
            reciprocalPasswordEl.sendKeys(reciprocalPassword);
            if (!StringUtils.isEmpty(reciprocalUrl))
            {
                reciprocalUrlEl.sendKeys(reciprocalUrl);
            }
        }

        WebDriverUtils.clickFirstVisibleElement(driver, By.className("applinks-next-button"));

        // wait for next screen
        driver.waitUntilElementIsVisible(By.cssSelector("#add-application-link-dialog .step-3"));
        return pageBinder.bind(UsersAndTrustScreen.class);
    }

    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
