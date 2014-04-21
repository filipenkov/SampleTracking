package com.atlassian.jira.webtest.selenium.framework.model;

import com.atlassian.selenium.SeleniumClient;

/**
 * Utility methods for dealing with the mouse in Selenium.
 *
 * @since v4.2
 */
public class Mouse
{
    private Mouse()
    {
        // Uninstantiable.
    }

    /**
     * Trigger a mouseover event.
     *
     * <p>
     * The mouseover handler may have a prerequisite of actual mouse motion,
     * e.g., via {@code jira.mouse.MotionDetector}, so we'll also trigger
     * some mouse movement to set off the motion detector.
     * </p>
     */
    public static void mouseover(SeleniumClient seleniumClient, String locator)
    {
        simulateMouseOver(seleniumClient, locator);
    }

    private static void simulateMouseOver(final SeleniumClient seleniumClient, final String locator)
    {
        seleniumClient.mouseMoveAt(locator, "0,0");
        //need a wait before doing another mousemove otherwise it sometimes doesn't
        //get picked up by the client!
        try
        {
            Thread.sleep(200);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        seleniumClient.mouseMoveAt(locator, "0,1");
        seleniumClient.mouseOver(locator);
    }
}
