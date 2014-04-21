package com.atlassian.jira.webtest.selenium.browseprojects;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

/**
 * @since v4.0
 */
@SkipInBrowser(browsers={Browser.IE}) //JS Errors - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestChangeLog extends AbstractTestBrowseProjects
{

    private static final String
            ACCORDION_EXPANDED_XPATH = "//ul[@class='versionBannerList']/li[2][contains(@class,'active expanded')]",
            ACCORDION_HEADER_XPATH = "//ul[@class='versionBannerList']/li[2]/div[@class='versionBanner-header']",
            ACCORDION_CONTENT_XPATH = "//ul[@class='versionBannerList']/li[2]/div[@class='versionBanner-content']";

    public static Test suite()
    {
        return suiteFor(TestChangeLog.class);
    }

    public void testChangeLogContents()
    {
        loadAjaxTab(CHANGE_LOG);
        waitFor(500);

        client.click(ACCORDION_HEADER_XPATH, false);

        assertThat.elementPresentByTimeout(ACCORDION_CONTENT_XPATH, LOAD_WAIT);
        assertThat.elementPresentByTimeout(ACCORDION_EXPANDED_XPATH, LOAD_WAIT);

        assertThat.textPresent("HSP-1");
        assertThat.textPresent("Unresolved");
        assertThat.textPresent("A editable issue");

        client.click(ACCORDION_HEADER_XPATH, false);
        waitFor(500);

        assertThat.textNotPresent("HSP-1");
        assertThat.textNotPresent("Unresolved");
        assertThat.textNotPresent("A editable issue");
    }
}
