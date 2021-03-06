package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.RestFuncTest;
import com.google.common.collect.ImmutableMap;
import com.meterware.httpunit.WebResponse;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Ensure the application conform to Platform's specification.
 *
 * @since 4.3.
 */
@WebTest ({ Category.DEV_MODE, Category.PLATFORM_COMPATIBILITY })
public class TestPlatformCompatibility extends RestFuncTest
{
    private static final String PLATFORM_CTK_PLUGIN_KEY = "com.atlassian.refapp.ctk";
    private boolean shouldRun;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();

        shouldRun = isPlatformCtkPluginInstalled();

        if (shouldRun)
        {
            administration.project().addProject("tautomerism", "TTM", ADMIN_USERNAME);
            navigation.issue().createIssue("tautomerism", "Bug", "jira blah blah");
            administration.reIndex();
            navigation.logout();
        }
    }

    public void testCtk() throws IOException, SAXException, JSONException
    {
        if (shouldRun)
        {
            log.log("found platform-ctk plugin. run it now!!");
            WebResponse response = GET("/rest/functest/1.0/junit/runTests?outdir=target/runtest", ImmutableMap.of("Accept", "application/json"));

            // check that the output is in good format.
            assertEquals("application/json", response.getContentType());
            assertEquals("UTF-8", response.getCharacterSet());

            JSONObject contents = new JSONObject(response.getText());

            // zero here means no test is failing.
            assertEquals("Test result:" + contents.getString("output"), 0, Integer.parseInt(contents.getString("result")));
        }
        else
        {
            log.log("platform-ctk plugin not found. skipped the test");
        }
    }

    private boolean isPlatformCtkPluginInstalled()
    {
        navigation.gotoAdmin();
        return administration.plugins().isPluginInstalled(PLATFORM_CTK_PLUGIN_KEY);
    }
}
