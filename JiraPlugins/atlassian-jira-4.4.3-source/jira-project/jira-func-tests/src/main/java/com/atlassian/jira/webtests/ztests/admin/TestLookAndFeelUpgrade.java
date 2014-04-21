package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.NodeLocator;
import com.atlassian.jira.functest.framework.locator.TableCellLocator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.w3c.dom.Node;

/**
 * A func test for the LookAndFeel upgrades
 *
 * @since v3.13
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestLookAndFeelUpgrade extends FuncTestCase
{


    public void test312NonModified() throws Exception
    {
        administration.restoreData("TestUpgradeBase312.xml");

        navigation.gotoAdminSection("lookandfeel");

        assertHasDefaultLookAndFeel();
    }
    public void test312Modified() throws Exception
    {
        administration.restoreData("TestUpgradeModifiedLookAndFeel312.xml");
        navigation.gotoAdminSection("lookandfeel");

        assertHas312Modifications();
    }

    public void test313NonModified() throws Exception
    {
        administration.restoreData("TestUpgradeBase313.xml");
        navigation.gotoAdminSection("lookandfeel");

        assertHasDefaultLookAndFeel();
    }

    public void test313Modified() throws Exception
    {
        administration.restoreData("TestUpgradeModifiedLookAndFeel313.xml");
        navigation.gotoAdminSection("lookandfeel");

        assertHas313Modifications();
    }

    private void assertHas312Modifications()
    {
        assertHasUrl("lookAndFeelLogo", 0, 1, "/images/debug/debug.png");

        // Assert the table 'lookAndFeelColors'
        TableLocator tableLocatorColors = new TableLocator(tester, "lookAndFeelColors");
        String textStr = tableLocatorColors.getText();
        text.assertTextSequence(textStr, new String[]{
                "Header Background Colour", "#99ccff",
                "Header Highlight Background Colour", "<Default>",
                "Header Text Colour", "#cc00ff",
                "Header Text Highlight Colour", "<Default>",
                "Header Separator Color", "#99ccff",
                "Navigation Bar Background Colour", "#3366ff",
                "Navigation Bar Text Colour", "#000066",
                "Navigation Bar Separator Colour", "<Default>"
        });
    }

    private void assertHas313Modifications()
    {
        assertHasUrl("lookAndFeelLogo", 0, 1, "/images/debug/debug.png");

        // Assert the table 'lookAndFeelColors'
        TableLocator tableLocatorColors = new TableLocator(tester, "lookAndFeelColors");
        String textStr = tableLocatorColors.getText();
        text.assertTextSequence(textStr, new String[]{
                "Header Background Colour", "#663399",
                "Header Highlight Background Colour", "#ff33cc",
                "Header Text Colour", "#cccccc",
                "Header Text Highlight Colour", "#6666cc",
                "Header Separator Color", "<Default>",
                "Navigation Bar Background Colour", "#9966ff",
                "Navigation Bar Text Colour", "#777777",
                "Navigation Bar Separator Colour", "<Default>"
        });
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
        });
    }

    private void assertHasDefaultLogos()
    {
        TableLocator tableLocatorLogo = new TableLocator(tester, "lookAndFeelLogo");
        text.assertTextSequence(tableLocatorLogo, new String[]{
                "Preview", "Favicon Preview",
        });
        assertHasUrl("lookAndFeelLogo", 0, 1, "/images/jira111x30.png");
        assertHasUrl("lookAndFeelLogo", 1, 1, "/images/icons/favicon32.png");
    }

    private void assertHasUrl(String tableId, int row, int col, String expectedUrl)
    {
        final TableCellLocator cellLocator = new TableCellLocator(tester, tableId, row, col);
        Node[] nodes = cellLocator.getNodes();
        text.assertTextPresent(new NodeLocator(nodes[0].getFirstChild().getNextSibling().getAttributes().getNamedItem("src")),
                expectedUrl);
    }




}
