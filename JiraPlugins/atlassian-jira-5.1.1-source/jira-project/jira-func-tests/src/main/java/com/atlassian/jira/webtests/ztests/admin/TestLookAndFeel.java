package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.NodeLocator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import org.w3c.dom.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A func test for the LookAndFeel pages
 *
 * @since v3.13
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestLookAndFeel extends FuncTestCase
{
    private static final String VERSION_ELEMENT_STR = "<dl style=\"display:none;\" id=\"jira.webresource.flushcounter\">";
    private static final String DEFAULT_LOGO_URL ="/images/jira111x30.png";
    public static final String DEFAULT_FAVICON_URL = "/images/64jira.png";

    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        navigation.gotoAdminSection("lookandfeel");
    }

    public void testHasDefaults() throws Exception
    {
        assertHasDefaultLookAndFeel();
    }

    public void testEditLookAndFeelIncrementsWebResourceUrl()
    {
        tester.clickLinkWithText("Edit Configuration");

        // Get the old look and feel version number from the screen. NOTE: this is a rolling number that persists across imports, this is why we have to get it this way
        final String responseText = tester.getDialog().getResponseText();
        final String oldVersionNumber = responseText.substring(responseText.indexOf(VERSION_ELEMENT_STR) + VERSION_ELEMENT_STR.length(), responseText.indexOf("</dl>"));

        String oldSuperbatchVer = findVersionNumberInSuperbatchCss(responseText);
        assertEquals(oldVersionNumber, oldSuperbatchVer);
        
        // now change a value
        tester.setFormElement("topBgColour", "#010101");
        tester.submit("Update");

        long newVersionNum = Long.parseLong(oldVersionNumber) + 1;
        // now make sure that the URL for the global-static css has changed
        String newResponseText = tester.getDialog().getResponseText();
        String newSuperbatchVer = findVersionNumberInSuperbatchCss(newResponseText);
        assertEquals(newVersionNum + "", newSuperbatchVer);
    }

    private String findVersionNumberInSuperbatchCss(String responseText)
    {
        // e.g. /atlassian-jira/s/en_AU-8a2a3a/756/6216/1/_/download/superbatch/en_AU-8a2a3a/1/css/batch.css
        Pattern p = Pattern.compile("/s/[^/]+/([0-9]+)/([0-9]+)/([0-9]+)/_/download/superbatch/[^\"]*?/batch.css\"");
        final Matcher matcher = p.matcher(responseText);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }

    private void assertHasDefaultLookAndFeel()
    {
        assertHasDefaultLogos();

        // Assert the table 'lookAndFeelColors'
        TableLocator tableLocatorColors = new TableLocator(tester, "lookAndFeelColors");
        String textStr = tableLocatorColors.getText();
        text.assertTextSequence(textStr, new String[]{
                "Header Background Colour", "<Default>",
                "Header Highlight Background Colour", "<Default>",
                "Header Text Colour", "<Default>",
                "Header Text Highlight Colour", "<Default>",
                "Header Separator Color", "<Default>",
                "Navigation Bar Background Colour", "<Default>",
                "Navigation Bar Text Colour", "<Default>",
                "Navigation Bar Separator Colour", "<Default>",
                "Link Colour", "<Default>",
                "Link Active Colour", "<Default>",
                "Heading Colour", "<Default>"
        });

        // Assert the table 'lookAndFeelGadgetChromeColours'
        TableLocator tableLocatorGadgetColors = new TableLocator(tester, "lookAndFeelGadgetChromeColours");
        textStr = tableLocatorGadgetColors.getText();
        text.assertTextSequence(textStr, new String[]{
                "Colour 1", "<Default>",
                "Colour 2", "<Default>",
                "Colour 3", "<Default>",
                "Colour 4", "<Default>",
                "Colour 5", "<Default>",
                "Colour 6", "<Default>",
                "Colour 7", "<Default>"
        });

        TableLocator tableLocatorFormats = new TableLocator(tester, "lookAndFeelFormats");
        text.assertTextSequence(tableLocatorFormats, new String[]{
                "Time Format", "h:mm a",
                "Day Format", "EEEE h:mm a",
                "Complete Date/Time Format", "dd/MMM/yy h:mm a",
                "Day/Month/Year Format", "dd/MMM/yy",
                "Use ISO8601 standard in Date Picker", "OFF"
        });
    }

    private void assertHasDefaultLogos()
    {
        TableLocator tableLocatorLogo = new TableLocator(tester, "lookAndFeelLogo");
        text.assertTextSequence(tableLocatorLogo, new String[]{
                "Preview", "Favicon Preview",
        });
        assertHasUrl("lookAndFeelLogo", 0, 1, DEFAULT_LOGO_URL);
        assertHasUrl("lookAndFeelLogo", 1, 1, DEFAULT_FAVICON_URL);
    }

    private void assertHasUrl(String tableId, int row, int col, String expectedUrl)
    {
        final TableCellLocator cellLocator = new TableCellLocator(tester, tableId, row, col);
        Node[] nodes = cellLocator.getNodes();
        text.assertTextPresent(new NodeLocator(nodes[0].getFirstChild().getNextSibling().getAttributes().getNamedItem("src")),
                expectedUrl);
    }

    public void testReflectsEdits() throws Exception
    {
        tester.clickLinkWithText("Edit Configuration");

        // assert that the defaults actual colors are show 5.0 specific
        WebPageLocator pageLocator = new WebPageLocator(tester);

        text.assertTextSequence(pageLocator.getHTML(), new String[]{
                "Header Background Colour", "#003366",
                "Header Highlight Background Colour", "#326ca6",
                "Header Text Colour", "#ffffff",
                "Header Text Highlight Colour", "#f0f0f0",
                "Header Separator Color", "#003366",
                "Navigation Bar Background Colour", "#326ca6",
                "Navigation Bar Text Colour", "#ffffff",
                "Navigation Bar Separator Colour", "#f0f0f0",
                "Link Colour", "#326ca6",
                "Link Active Colour", "#326ca6",
                "Heading Colour", "#292929"
        });

        // now change the values
        tester.setFormElement("logoUrl", "logoURL");
        //plugin does not allow you to set width andheight directly - they are derived form the image
        //tester.setFormElement("logoWidth", "666");
        //tester.setFormElement("logoHeight", "9");
        tester.setFormElement("topBgColour", "#010101");
        tester.setFormElement("topTextColour", "#020202");
        tester.setFormElement("topHighlightBgColour", "#030303");
        tester.setFormElement("topTextHighlightColour", "#040404");
        tester.setFormElement("topSeparatorColor", "#050505");
        tester.setFormElement("menuBgColour", "#060606");
        tester.setFormElement("menuTextColour", "#070707");
        tester.setFormElement("menuSeparatorColour", "#080808");
        tester.setFormElement("linkColour", "#101010");
        tester.setFormElement("linkAColour", "#111111");
        tester.setFormElement("headingColour", "#121212");
        tester.setFormElement("gadgetChromeColorcolor1", "#313131");
        tester.setFormElement("gadgetChromeColorcolor2", "#323131");
        tester.setFormElement("gadgetChromeColorcolor3", "#333131");
        tester.setFormElement("gadgetChromeColorcolor4", "#343131");
        tester.setFormElement("gadgetChromeColorcolor5", "#353131");
        tester.setFormElement("gadgetChromeColorcolor6", "#363131");
        tester.setFormElement("gadgetChromeColorcolor7", "#373131");
        tester.setFormElement("formatTime", "h:m:s");
        tester.setFormElement("formatDay", "d:M:y");
        tester.setFormElement("formatComplete", "d:M:y h:m:s");
        tester.setFormElement("formatDMY", "d/M/y");
        tester.checkCheckbox("useISO8601", "true");
        tester.submit("Update");


        TableLocator tableLocatorLogo = new TableLocator(tester, "lookAndFeelLogo");
        assertHasUrl("lookAndFeelLogo", 0, 1, DEFAULT_LOGO_URL);

        // Assert the table 'lookAndFeelColors'
        TableLocator tableLocatorColors = new TableLocator(tester, "lookAndFeelColors");
        String textStr = tableLocatorColors.getText();
        text.assertTextSequence(textStr, new String[]{
                "Header Background Colour", "#010101",
                "Header Highlight Background Colour", "#030303",
                "Header Text Colour", "#020202",
                "Header Text Highlight Colour", "#040404",
                "Header Separator Color", "#050505",
                "Navigation Bar Background Colour", "#060606",
                "Navigation Bar Text Colour", "#070707",
                "Navigation Bar Separator Colour", "#080808",
                "Link Colour", "#101010",
                "Link Active Colour", "#111111",
                "Heading Colour", "#121212"
        });

        // Assert the table 'lookAndFeelGadgetChromeColours'
        TableLocator tableLocatorGadgetColors = new TableLocator(tester, "lookAndFeelGadgetChromeColours");
        textStr = tableLocatorGadgetColors.getText();
        text.assertTextSequence(textStr, new String[]{
                "Colour 1", "#313131",
                "Colour 2", "#323131",
                "Colour 3", "#333131",
                "Colour 4", "#343131",
                "Colour 5", "#353131",
                "Colour 6", "#363131",
                "Colour 7", "#373131",
        });

        TableLocator tableLocatorFormats = new TableLocator(tester, "lookAndFeelFormats");
        text.assertTextSequence(tableLocatorFormats, new String[] {
                "Time Format", "h:m:s",
                "Day Format", "d:M:y",
                "Complete Date/Time Format", "d:M:y h:m:s",
                "Day/Month/Year Format", "d/M/y",
                "Use ISO8601 standard in Date Picker", "ON"
        });

        // now reset to defaults
        tester.gotoPage(page.addXsrfToken("EditLookAndFeel!reset.jspa"));
        assertHasDefaultLookAndFeel();
    }


    public void testInvalidUrlLogoUpload()
    {
        tester.clickLinkWithText("Edit Configuration");
        FormParameterUtil formHelper = new FormParameterUtil(tester, "jiraform", "Update");
        formHelper.setFormElement("logoOption", "url");
        formHelper.setFormElement("logoUrl", "logourl");
        Node document = formHelper.submitForm();
        Locator locator = new XPathLocator(document, "//*[@id='error-container']");

        assertTrue(locator.getText().contains("Failed to upload image from"));

    }

    public void testInvalidFaviconLogoUpload()
    {
        tester.clickLinkWithText("Edit Configuration");
        FormParameterUtil formHelper = new FormParameterUtil(tester, "jiraform", "Update");
        formHelper.setFormElement("faviconOption", "url");
        formHelper.setFormElement("faviconUrl", "logourl");
        Node document = formHelper.submitForm();
        Locator locator = new XPathLocator(document, "//*[@id='error-container']");

        assertTrue(locator.getText().contains("Failed to upload image from"));

    }


}


