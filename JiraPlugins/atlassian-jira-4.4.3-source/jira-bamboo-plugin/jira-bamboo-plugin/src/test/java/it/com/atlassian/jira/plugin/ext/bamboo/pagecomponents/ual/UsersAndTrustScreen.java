package it.com.atlassian.jira.plugin.ext.bamboo.pagecomponents.ual;

import com.atlassian.webdriver.AtlassianWebDriver;
import it.com.atlassian.jira.plugin.ext.bamboo.WebDriverUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

public class UsersAndTrustScreen
{
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(UsersAndTrustScreen.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    @FindBy(css = "#add-application-link-dialog  #sameUser")
    private WebElement sameUsersRadio;
    @FindBy(css = "#add-application-link-dialog  #differentUser")
    private WebElement differentUsersRadio;
    @FindBy(css = "#add-application-link-dialog  #trusted")
    private WebElement trustedRadio;
    @FindBy(css = "#add-application-link-dialog  #notTrusted")
    private WebElement notTrustedRadio;
    // ---------------------------------------------------------------------------------------------------- Dependencies
    @Inject
    private AtlassianWebDriver driver;
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    public void submitTrustDetails(boolean sameUsers, boolean trusted)
    {
        if (sameUsers)
        {
            sameUsersRadio.click();
        }
        else
        {
            differentUsersRadio.click();
        }

        if (trusted)
        {
            trustedRadio.click();
        }
        else
        {
            notTrustedRadio.click();
        }

        WebDriverUtils.clickFirstVisibleElement(driver, By.className("wizard-submit"));
        driver.waitUntilElementIsNotVisible(By.className("add-application-link-dialog"));
    }
    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
