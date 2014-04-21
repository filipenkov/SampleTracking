package com.atlassian.jira.pageobjects;

import com.atlassian.webdriver.AtlassianWebDriver;

/**
* Utility for opening new window sessions.
*
* @since v4.4
*/
public class WindowSession
{
    private final String defaultWindow;
    private AtlassianWebDriver driver;

    WindowSession(final AtlassianWebDriver driver)
    {
        this.driver = driver;
        this.defaultWindow = driver.getWindowHandle();
    }

    /**
     * Opens a new window with the given name
     *
     * @param newWindow the name of the new window
     * @return a window that you can switch to when you want focus
     */
    public BrowserWindow openNewWindow(final String newWindow)
    {
        return new BrowserWindow(driver, newWindow);
    }

    public void switchToDefault()
    {
        driver.switchTo().window(defaultWindow);
    }

    public static class BrowserWindow
    {
        private final AtlassianWebDriver driver;
        private final String windowName;

        private BrowserWindow(AtlassianWebDriver driver, final String windowName)
        {
            this.driver = driver;
            this.windowName = windowName;
            driver.executeScript("window.open('', '" + windowName + "')");
        }

        /**
         * Switch to this window. Future webdriver commands
         * will be done in this window
         *
         * @return the new active window
         */
        public BrowserWindow switchTo()
        {
            driver.switchTo().window(windowName);
            return this;
        }

        /**
         * Closes this window. Remember to {@link com.atlassian.jira.pageobjects.WindowSession#switchToDefault()}
         * after this.
         */
        public void close()
        {
            driver.switchTo().window(windowName);
            driver.executeScript("self.close()");
        }
    }
}
