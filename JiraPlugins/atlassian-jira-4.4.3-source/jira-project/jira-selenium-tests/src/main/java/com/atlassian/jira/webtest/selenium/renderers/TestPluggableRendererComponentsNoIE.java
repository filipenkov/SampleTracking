package com.atlassian.jira.webtest.selenium.renderers;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

import java.io.File;

/**
 * This test fails in IE, because selenium can't set the value of an input field of type file due to security restrictions.
 *
 * @since v4.3
 */
@SkipInBrowser (browsers={ Browser.IE})
@WebTest({Category.SELENIUM_TEST })
public class TestPluggableRendererComponentsNoIE extends AbstractPluggableRendererComponents
{

    public static Test suite()
    {
        return suiteFor(TestPluggableRendererComponentsNoIE.class);
    }

    public void testWikiRenderedCommentsImages()
    {
        boolean[] useThumbnailOptions = new boolean[] { true, false };

        File screenshot = new File(getEnvironmentData().getXMLDataLocation().getAbsolutePath() + "/" + "screenshot2.png");
        getNavigator().issue().viewIssue("HSP-1").attachFileWithComment("tempFilename", screenshot.getAbsolutePath(), 0, "A screenshot", null);

        getNavigator().gotoIssue("HSP-1");
        int commentCount = 10001;

        for (int i = 0; i < useThumbnailOptions.length; i++)
        {
            String imageLocator = useThumbnailOptions[i] ? "//a[@id='10020_thumb']" : "//img";
            String wikiText = useThumbnailOptions[i] ? "!screenshot2.png|thumbnail!" : "!screenshot.png!";

            String commentDivLocator = "xpath=//div[@id='comment-" + (commentCount + i) + "']//div[@class='action-body flooded']";

            client.click("comment-issue");
            client.type("comment", wikiText);
            client.click("comment-preview_link");
            assertThat.notVisibleByTimeout("xpath=//div[@id='comment-wiki-edit']/textarea", PREVIEW_WAIT);
            assertThat.elementPresent(PREVIEW_DIV_LOCATOR + imageLocator);
            // Alternate mode, checks it submits in either preview or standard mode.
            if (i%2==0) {
               client.click("comment-preview_link");
            }

            client.click("jquery=#issue-comment-add-submit", true);
            assertThat.elementPresent(commentDivLocator + imageLocator);

            client.click("delete_comment_" + (commentCount + i), true);
            assertThat.elementPresent(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + imageLocator);
            client.click("Delete", true);

            assertThat.elementNotPresent(commentDivLocator);
        }
    }

}
