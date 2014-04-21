package it.com.atlassian.jira.plugin.ext.bamboo;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.RenderedWebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class WebDriverUtils
{
    @SuppressWarnings("UnusedDeclaration")
    private static final Logger log = Logger.getLogger(WebDriverUtils.class);
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    // ----------------------------------------------------------------------------------------------- Interface Methods
    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Public Methods
    public static void clickFirstVisibleElement(WebDriver driver, By selector)
    {
        List<WebElement> elements = driver.findElements(selector);
        for (WebElement element : elements)
        {
            RenderedWebElement renderedWebElement = (RenderedWebElement) element;
            if (renderedWebElement.isDisplayed())
            {
                renderedWebElement.click();
                return;
            }
        }
    }

    public static String getNewProjectKey()
    {
        return RandomStringUtils.randomAlphabetic(5).toUpperCase();
    }
    // ------------------------------------------------------------------------------------------------- Helper Methods
    // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
}
