package com.atlassian.jira.pageobjects.util;

import com.atlassian.webdriver.AtlassianWebDriver;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Cookie;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class that can be used to manipulate the current user's session on the server.
 *
 * @since v5.0.1
 */
public class UserSessionHelper
{
    private static final String RESET_WEBSUDO = readResource("js/websudo.js");
    private static final String RESET_XSRF = readResource("js/xsrf.js");

    @Inject
    private AtlassianWebDriver driver;

    public void clearWebSudo()
    {
        driver.executeScript(RESET_WEBSUDO);
    }

    /**
     * Sets the JSESSIONID value to garbage. Doing this will essentially invalidate the cookie, forcing JIRA to create
     * treat the request as a request that comes in with an expired JSESSIONID.
     */
    public void invalidateSession()
    {
        driver.manage().deleteCookieNamed("JSESSIONID");
        driver.manage().addCookie(new Cookie("JSESSIONID", "nonsense"));
    }

    public void destoryAllXsrfTokens()
    {
        driver.executeScript(RESET_XSRF);
    }

    private static String readResource(String name)
    {
        final ClassLoader loader = UserSessionHelper.class.getClassLoader();
        final InputStream resourceAsStream = loader.getResourceAsStream(name);
        try
        {
            return IOUtils.toString(resourceAsStream, "utf-8");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            IOUtils.closeQuietly(resourceAsStream);
        }
    }
}
