package com.atlassian.jira.pageobjects.config;

import com.atlassian.pageobjects.ProductInstance;
import com.google.inject.Inject;
import org.openqa.selenium.WebDriver;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Enables/disables web sudo in JIRA.
 *
 * @since v4.4
 */
public class WebSudoControl
{
    private static final String WEBSUDO_URL_TEMPLATE = "rest/func-test/1.0/websudo?enabled=%s";
    private static final String WEBSUDO_URL_ENABLE = format(WEBSUDO_URL_TEMPLATE, "true");
    private static final String WEBSUDO_URL_DISABLE = format(WEBSUDO_URL_TEMPLATE, "false");

    @Inject
    private WebDriver driver;
    @Inject
    private ProductInstance productInstance;

    public WebSudoControl()
    {
    }

    @Inject public WebSudoControl(WebDriver driver, ProductInstance productInstance)
    {
        this.driver = checkNotNull(driver);
        this.productInstance = checkNotNull(productInstance);
    }


    public void enable()
    {
        callRest(WEBSUDO_URL_ENABLE);
    }

    public void disable()
    {
        callRest(WEBSUDO_URL_DISABLE);
    }

    public void toogle(boolean targetWebSudoState)
    {
        if (targetWebSudoState)
        {
            enable();
        }
        else
        {
            disable();
        }
    }

    private void callRest(String path)
    {
        driver.get(productInstance.getBaseUrl() + "/" + path);
    }
}
