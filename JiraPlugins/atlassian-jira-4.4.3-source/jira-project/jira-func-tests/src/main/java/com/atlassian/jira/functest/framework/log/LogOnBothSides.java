package com.atlassian.jira.functest.framework.log;

import com.atlassian.jira.webtests.WebTesterFactory;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Common code for logging messages on both side of the equator.  It writes to the func test log and to the
 * JIRA log if it can!
 * 
 * @since v4.0
 */
public class LogOnBothSides
{
    /**
     * This will log messages on the client side and the JIRA log.  It catches all runtime exceptions so logging
     * doesn't stop things from working otherwise
     *
     * @param environmentData the environment in play
     * @param msg the message to log
     */
    public static void log(JIRAEnvironmentData environmentData, String msg)
    {
        try
        {
            FuncTestOut.log(msg);

            // we use a separate tester so that the main one is not polluted
            WebTester tester = WebTesterFactory.createNewWebTester(environmentData);
            // we must be logged in to log a message
            tester.beginAt("/secure/admin/debug/logMessage.jsp?decorator=none&os_username=admin&os_password=admin&message=" + URLEncoder.encode(msg, "UTF-8"));
        }
        catch (RuntimeException ignored)
        {
            // don't let the logging stop the eventual running of the test
        }
        catch (UnsupportedEncodingException ingored)
        {
            // don't let the logging stop the eventual running of the test
        }
    }
}
