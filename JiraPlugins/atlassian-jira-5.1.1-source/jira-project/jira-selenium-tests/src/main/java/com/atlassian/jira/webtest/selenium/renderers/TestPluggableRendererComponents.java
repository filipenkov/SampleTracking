package com.atlassian.jira.webtest.selenium.renderers;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import junit.framework.Test;

/**
 * @since v4.2
 */
@WebTest({Category.SELENIUM_TEST })
public class TestPluggableRendererComponents extends AbstractPluggableRendererComponents
{
    private static class TextEffectsRecord
    {
        final String input;
        final String locator;
        final String output;

        TextEffectsRecord(String input, String locator, String output)
        {
            this.input = input;
            this.locator = locator;
            this.output = output;
        }
    }

    private static final TextEffectsRecord[] TEXT_EFFECTS = {
            new TextEffectsRecord("text with no formatting", "", "text with no formatting"),
            new TextEffectsRecord("*text with no formatting*", "p/b", "text with no formatting"),
            new TextEffectsRecord("_emphasis_", "p/em", "emphasis"),
            new TextEffectsRecord("??citation??", "p/cite", "citation"),
            new TextEffectsRecord("-deleted-", "p/del", "deleted"),
            new TextEffectsRecord("+inserted+", "p/ins", "inserted"),
            new TextEffectsRecord("^superscript^", "p/sup", "superscript"),
            new TextEffectsRecord("~subscript~", "p/sub", "subscript"),
            new TextEffectsRecord("{{monospaced}}", "p/tt", "monospaced"),
            new TextEffectsRecord("bq. some block quoted text", "blockquote/p", "some block quoted text"),
            new TextEffectsRecord("{quote}\nhere is quoteable\ncontent to be quoted\n{quote}", "blockquote/p", "here is quoteable\ncontent to be quoted"),
            new TextEffectsRecord("h1. heading 1", "h1", "heading 1"),
            new TextEffectsRecord("h2. heading 2", "h2", "heading 2"),
            new TextEffectsRecord("h3. heading 3", "h3", "heading 3"),
            new TextEffectsRecord("h4. heading 4", "h4", "heading 4"),
            new TextEffectsRecord("h5. heading 5", "h5", "heading 5"),
            new TextEffectsRecord("h6. heading 6", "h6", "heading 6"),
            new TextEffectsRecord("[^brokenattachment.ext]", "", "[^brokenattachment.ext]"),
            new TextEffectsRecord("{noformat}_This_ text -has- no *f*ormating{noformat}", "/pre", "_This_ text -has- no *f*ormating") };


    private static final String[] LINKS_INPUT = new String[] { "[#anchor]",
            "[^attachment.ext]",
            "[~admin]",
            "[link text|http://www.atlassian.com]" };

    private static final String[] LINKS_TITLE = new String[] { "anchor",
            "attachment.ext",
            "Administrator",
            "link text" };

    private static final String[] LINKS_HREF = new String[] { "#anchor",
            "attachment.ext",
            "?name=admin",
            "http://www.atlassian.com" };

    public static Test suite()
    {
        return suiteFor(TestPluggableRendererComponents.class);
    }

    // test text effects, headings and other misc HTML rendering
    public void testWikiRenderedCommentsTextEffects()
    {

        getNavigator().gotoIssue("HSP-1");
        int commentCount = 10000;

        for (int i = 0; i < TEXT_EFFECTS.length; i++)
        {
            String input = TEXT_EFFECTS[i].input;
            String output = TEXT_EFFECTS[i].output;
            String previewLocator = "xpath=//div[@id='comment-wiki-edit']/div[@class='content-inner']/" + TEXT_EFFECTS[i].locator;
            String renderedLocator = "xpath=//div[@class='action-body flooded']/" + TEXT_EFFECTS[i].locator;
            String deleteLocator = DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "/" + TEXT_EFFECTS[i].locator;

            client.click("comment-issue");
            client.type("comment", input);
            client.typeWithFullKeyEvents("comment", " ", false); // enable submit button
            client.click("comment-preview_link");
            assertThat.notVisibleByTimeout("xpath=//div[@id='comment-wiki-edit']/textarea", PREVIEW_WAIT);
            assertTrue(output.equals(cannonicalForm(client.getText(previewLocator))));
            // Alternate mode, checks it submits in either preview or standard mode.
            if (i%2==0) {
                client.click("comment-preview_link");
            }

            client.click("jquery=#issue-comment-add-submit", true);
            assertTrue(output.equals(cannonicalForm(client.getText(renderedLocator))));

            client.click("delete_comment_" + (commentCount + i), true);
            assertTrue(output.equals(cannonicalForm(client.getText(deleteLocator))));
            client.click("Delete", true);

            assertThat.textNotPresent(output);
        }

        // JRA-17417: testing field does not revert to state, previous to preview.
        client.click("comment-issue");
        client.typeWithFullKeyEvents("comment", "a");
        client.click("comment-preview_link");
        assertThat.notVisibleByTimeout("xpath=//div[@id='comment-wiki-edit']/textarea", PREVIEW_WAIT);
        client.click("comment-preview_link");
        client.typeWithFullKeyEvents("comment", "b", false);
        client.click("id=issue-comment-add-submit", true);
        assertTrue("ab".equals(client.getText("xpath=//div[@class='action-body flooded']")));


    }

    /**
     * Strips additional spaces around newline characters to make
     * strings returned from different browsers equivalent.
     */
    String cannonicalForm(String str)
    {
        if (str == null)
        {
            return str;
        }

        StringBuilder strBuff = new StringBuilder(str.trim());
        for (int i = 0; i < strBuff.length() - 1; i++)
        {
            if ((strBuff.charAt(i) == '\n') && (strBuff.charAt(i + 1) == ' '))
            {
                strBuff.deleteCharAt(i + 1);
            }
            if ((strBuff.charAt(i) == ' ') && (strBuff.charAt(i + 1) == '\n'))
            {
                strBuff.deleteCharAt(i);
            }
        }
        return strBuff.toString();
    }

    public void testWikiRenderedCommentsLinks()
    {
        getNavigator().gotoIssue("HSP-1");
        int commentCount = 10000;

        for (int i = 0; i < LINKS_INPUT.length; i++)
        {
            String input = LINKS_INPUT[i];
            String title = LINKS_TITLE[i];
            String href = LINKS_HREF[i];
            String commentDivLocator = "xpath=//div[@id='comment-" + (commentCount + i) + "']//div[@class='action-body flooded']";

            client.click("comment-issue");
            client.type("comment", input);
            client.typeWithFullKeyEvents("comment", " ", false); // enable submit button
            client.click("comment-preview_link");
            assertThat.notVisibleByTimeout("xpath=//div[@id='comment-wiki-edit']/textarea", PREVIEW_WAIT);
            assertThat.elementPresent(PREVIEW_DIV_LOCATOR + "//a");
            assertThat.elementHasText(PREVIEW_DIV_LOCATOR + "//a", title);
            assertThat.attributeContainsValue(PREVIEW_DIV_LOCATOR + "//a", "href", href);
            // Alternate mode, checks it submits in either preview or standard mode.
            if (i%2==0) {
                client.click("comment-preview_link");
            }
            client.click("jquery=#issue-comment-add-submit", true);
            assertThat.elementPresent(commentDivLocator + "//a");

            client.click("delete_comment_" + (commentCount + i), true);
            assertThat.elementPresent(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "//a");
            assertThat.elementHasText(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "//a", title);
            assertThat.attributeContainsValue(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "//a", "href", href);
            client.click("Delete", true);

            assertThat.elementNotPresent(commentDivLocator);

        }

    }

    public void testWikiRenderedCommentsLists()
    {
        String input = "* a\n"
                + "* bulletted\n"
                + "*# with\n"
                + "*# nested\n"
                + "*# numbered\n"
                + "* list";

        getNavigator().gotoIssue("HSP-1");

        String commentDivLocator = "xpath=//div[@id='comment-10000']//div[@class='action-body flooded']";

        client.click("comment-issue");
        client.type("comment", input);
        client.typeWithFullKeyEvents("comment", " ", false); // activate submit
        client.click("comment-preview_link");
        assertThat.notVisibleByTimeout("xpath=//div[@id='comment-wiki-edit']/textarea", PREVIEW_WAIT);
        assertThat.elementPresent(PREVIEW_DIV_LOCATOR + "//ul");
        assertThat.elementPresent(PREVIEW_DIV_LOCATOR + "//ul//ol");
        assertThat.elementPresent(PREVIEW_DIV_LOCATOR + "//ul//ol/li");
        assertThat.elementHasText(PREVIEW_DIV_LOCATOR + "//ul/li[1]", "a");
        assertThat.elementHasText(PREVIEW_DIV_LOCATOR + "//ul/li[3]", "list");
        client.click("comment-preview_link");

        client.click("jquery=#issue-comment-add-submit", true);
        assertThat.elementPresent(commentDivLocator + "//ul");
        assertThat.elementPresent(commentDivLocator + "//ul//ol");
        assertThat.elementPresent(commentDivLocator + "//ul//ol/li");
        assertThat.elementHasText(commentDivLocator + "//ul/li[1]", "a");
        assertThat.elementHasText(commentDivLocator + "//ul/li[3]", "list");

        client.click("delete_comment_10000", true);
        assertThat.elementPresent(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "//ul");
        assertThat.elementPresent(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "//ul//ol");
        assertThat.elementPresent(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "//ul//ol/li");
        assertThat.elementHasText(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "//ul/li[1]", "a");
        assertThat.elementHasText(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "//ul/li[3]", "list");
        client.click("Delete", true);

        assertThat.elementNotPresent(commentDivLocator);
    }

    public void testWikiRenderedCommentsTables()
    {
        String commentDivLocator = "xpath=//div[@id='comment-10000']//div[@class='action-body flooded']";

        getNavigator().gotoIssue("HSP-1");
        client.click("comment-issue");

        client.type("comment", "||table header 1||table header 2||\n|table cell 1|table cell 2|");
        client.typeWithFullKeyEvents("comment", " ", false);
        client.click("comment-preview_link");
        assertThat.notVisibleByTimeout("xpath=//div[@id='comment-wiki-edit']/textarea", PREVIEW_WAIT);
        assertEquals("table header 1", client.getTable(PREVIEW_DIV_LOCATOR + "/table.0.0"));
        client.click("comment-preview_link");

        client.click("jquery=#issue-comment-add-submit", true);
        assertEquals("table header 1", client.getTable(commentDivLocator + "/table.0.0"));

        client.click("delete_comment_10000", true);
        assertEquals("table header 1", client.getTable(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "/table.0.0"));
        client.click("Delete", true);

        assertThat.elementNotPresent(commentDivLocator);
    }

    // miscellaneous test cases that could not be bundled with others
    public void testWikiRenderedCommentsMisc()
    {
        String commentDivLocator = "xpath=//div[@id='comment-10000']//div[@class='action-body flooded']";

        getNavigator().gotoIssue("HSP-1");

        client.click("comment-issue");
        client.typeWithFullKeyEvents("comment", "{color:red}look ma, red text!{color}");
        client.click("comment-preview_link");
        assertThat.notVisibleByTimeout("xpath=//div[@id='comment-wiki-edit']/textarea", PREVIEW_WAIT);
        assertThat.elementPresent(PREVIEW_DIV_LOCATOR + "/p/font");
        assertThat.elementHasText(PREVIEW_DIV_LOCATOR + "/p/font", "look ma, red text!");
        client.click("comment-preview_link");

        client.click("jquery=#issue-comment-add-submit", true);
        assertThat.elementPresent(commentDivLocator + "/p/font");
        assertThat.elementHasText(commentDivLocator + "/p/font", "look ma, red text!");

        client.click("delete_comment_10000", true);
        assertThat.elementPresent(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "/p/font");
        assertThat.elementHasText(DELETE_COMMENT_RENDERED_CONTENT_LOCATOR + "/p/font", "look ma, red text!");
        client.click("Delete", true);

        assertThat.textNotPresent("look ma, red text!");
    }
}
