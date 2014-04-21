package com.atlassian.jira.webtest.selenium.framework;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.selenium.SeleniumClient;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods to handle windows in Selenium tests.
 *
 * @since v4.2
 */
public class Window
{
    private static String WAIT_FOR_POPUP_TIMEOUT = "5000";

    private Window()
    {
        // Non-instantiable.
    }

    /**
     * Open a new window, perform the given tasks, then close the window and
     * refocus the original window.
     *
     * <p>
     * You shouldn't focus other windows within the task. Instead, use
     * {@link #openAndSelect(com.atlassian.selenium.SeleniumClient, String, String)}
     * if you want more control over the windows.
     * </p>
     *
     * @param client selenium client
     * @param url URL to open the window to; pass in a blank URL to open a window at the default location
     * @param windowName an identifier of your choosing for the new window
     * @param task task to perform inside the new window
     * @throws Exception possible exception from within the task
     * @throws IllegalArgumentException if there is already an open window with the specified windowName
     */
    public static void withNewWindow(SeleniumClient client, String url, String windowName, SeleniumClosure task)
            throws Exception
    {
        openAndSelect(client, url, windowName);
        task.execute();
        close(client, windowName);
    }

    /**
     * Open a new window and give it Selenium's focus.
     *
     * @param client selenium client
     * @param url URL to open the window to; pass in a blank URL to open a window at the default location
     * @param windowName an identifier of your choosing for the new window
     * @throws IllegalArgumentException if there is already an open window with the specified windowName
     */
    public static void openAndSelect(SeleniumClient client, String url, String windowName)
    {
        Assertions.notBlank("windowName", windowName);
        if (openWindows(client).contains(windowName))
        {
            throw new IllegalArgumentException(String.format("Window with name '%s' is already open.", windowName));
        }

        client.openWindow(url, windowName);
        // A blank URL seems to open the about:blank page, so nothing to wait
        // for (and waiting will cause Selenium to timeout).
        if (!StringUtils.isBlank(url))
        {
            client.waitForPopUp(windowName, WAIT_FOR_POPUP_TIMEOUT);
        }
        client.selectWindow(windowName);
    }

    /**
     * Closes the specified window if open and gives Selenium's attention to
     * the original browser window. Is a no-op if the window is not open.
     *
     * @param client selenium client
     * @param windowName identifier used in {@link #openAndSelect(com.atlassian.selenium.SeleniumClient, String, String)}
     */
    public static void close(SeleniumClient client, String windowName)
    {
        Assertions.notBlank("windowName", windowName);
        if (openWindows(client).contains(windowName))
        {
            client.close();
            client.selectWindow(null);
            client.windowFocus();
        }
    }

    private static Set<String> openWindows(SeleniumClient client)
    {
        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(client.getAllWindowNames())));
    }
}
