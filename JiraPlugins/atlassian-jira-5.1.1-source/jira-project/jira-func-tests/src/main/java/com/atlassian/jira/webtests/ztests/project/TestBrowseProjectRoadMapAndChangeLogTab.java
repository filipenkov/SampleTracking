package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.apache.commons.lang.StringUtils;

import java.net.URLEncoder;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestBrowseProjectRoadMapAndChangeLogTab extends FuncTestCase
{
    private static final String SECOND_LINE = "//ul[@class='versionBannerList']/li[2]/div[1]/a[@class='versionBanner-name']";
    private static final String FIRST_LINE = "//ul[@class='versionBannerList']/li[1]/div[1]/a[@class='versionBanner-name']";

    protected void setUpTest()
    {
        administration.restoreDataSlowOldWay("TestBrowseProjectRoadmapAndChangeLogTab.xml");
    }

    public void testEmptyRoadMapAndChangelLog()
    {
        navigation.browseProjectTabPanel("NORELEASED", "roadmap");
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText(SECOND_LINE, "Version 2");
        navigation.browseProjectTabPanel("NORELEASED", "changelog");
        assertions.assertNodeDoesNotExist(FIRST_LINE);
        assertions.assertNodeHasText("//ul[@class='versionBannerList']", "No Versions");

        navigation.browseProjectTabPanel("RELEASED", "changelog");
        assertions.assertNodeHasText(FIRST_LINE, "Vesion 2");
        assertions.assertNodeHasText(SECOND_LINE, "Version 1");
        navigation.browseProjectTabPanel("RELEASED", "roadmap");
        assertions.assertNodeDoesNotExist(FIRST_LINE);
        assertions.assertNodeHasText("//ul[@class='versionBannerList']", "No Versions");

        // component tabs

        navigation.browseComponentTabPanel("NORELEASED", "Component 1", "roadmap");
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText(SECOND_LINE, "Version 2");
        navigation.browseComponentTabPanel("NORELEASED", "Component 1", "changelog");
        assertions.assertNodeDoesNotExist(FIRST_LINE);
        assertions.assertNodeHasText("//ul[@class='versionBannerList']", "No Versions");

        navigation.browseComponentTabPanel("RELEASED", "Component 1", "changelog");
        assertions.assertNodeHasText(FIRST_LINE, "Vesion 2");
        assertions.assertNodeHasText(SECOND_LINE, "Version 1");
        navigation.browseComponentTabPanel("RELEASED", "Component 1", "roadmap");
        assertions.assertNodeDoesNotExist(FIRST_LINE);
        assertions.assertNodeHasText("//ul[@class='versionBannerList']", "No Versions");


        // personal road map
        tester.gotoPage("secure/ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-roadmap-panel&pid=10050");
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText(SECOND_LINE, "Version 2");
        tester.gotoPage("secure/ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-roadmap-panel&pid=10051");
        assertions.assertNodeDoesNotExist(FIRST_LINE);
        assertions.assertNodeHasText("//ul[@class='versionBannerList']", "No Versions");


    }

    public void testPaging()
    {
        navigation.browseProjectTabPanel("LOTS", "roadmap");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        Locator locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Bored");

        tester.clickLinkWithText("all versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(11, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[11]/div[1]/a[@class='versionBanner-name']", "Version still Bored");

        navigation.browseProject("LOTS");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(11, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[11]/div[1]/a[@class='versionBanner-name']", "Version still Bored");
        
        tester.clickLinkWithText("upcoming 10 versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Bored");
        tester.clickLinkWithText("all versions");

        navigation.browseProjectTabPanel("LOTS", "changelog");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "previous 10 versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "<b>CSS</b>");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Dylan");

        tester.clickLinkWithText("all versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "previous 10 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(11, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "<b>CSS</b>");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[11]/div[1]/a[@class='versionBanner-name']", "Version Andreas");

        navigation.browseProject("LOTS");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "previous 10 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(11, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "<b>CSS</b>");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[11]/div[1]/a[@class='versionBanner-name']", "Version Andreas");

        tester.clickLinkWithText("previous 10 versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "previous 10 versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "<b>CSS</b>");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Dylan");


        // components
        navigation.browseComponentTabPanel("LOTS", "Component 1", "roadmap");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Bored");

        tester.clickLinkWithText("all versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(11, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[11]/div[1]/a[@class='versionBanner-name']", "Version still Bored");

        navigation.browseComponentTabPanel("LOTS", "Component 1");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(11, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[11]/div[1]/a[@class='versionBanner-name']", "Version still Bored");

        tester.clickLinkWithText("upcoming 10 versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Bored");
        tester.clickLinkWithText("all versions");

        navigation.browseComponentTabPanel("LOTS", "Component 1", "changelog");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "previous 10 versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "<b>CSS</b>");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Dylan");

        tester.clickLinkWithText("all versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "previous 10 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(11, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "<b>CSS</b>");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[11]/div[1]/a[@class='versionBanner-name']", "Version Andreas");

        navigation.browseComponentTabPanel("LOTS", "Component 1");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "previous 10 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(11, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "<b>CSS</b>");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[11]/div[1]/a[@class='versionBanner-name']", "Version Andreas");

        tester.clickLinkWithText("previous 10 versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "previous 10 versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "<b>CSS</b>");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Dylan");


        // test the version tab here as well

        navigation.browseProjectTabPanel("LOTS", "versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "next 20 versions");
        locator = new XPathLocator(tester, "//table[@id='versions_panel']/tbody/tr");
        assertEquals(20, locator.getNodes().length);
        assertions.assertNodeHasText("//table[@id='versions_panel']/tbody/tr[1]/td[2]", "<b>CSS</b>");
        assertions.assertNodeHasText("//table[@id='versions_panel']/tbody/tr[20]/td[2]", "Version A");

        tester.clickLinkWithText("all versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "next 20 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//table[@id='versions_panel']/tbody/tr");
        assertEquals(22, locator.getNodes().length);
        assertions.assertNodeHasText("//table[@id='versions_panel']/tbody/tr[1]/td[2]", "<b>CSS</b>");
        assertions.assertNodeHasText("//table[@id='versions_panel']/tbody/tr[22]/td[2]", "Version 1");

        navigation.browseProject("LOTS");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "next 20 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//table[@id='versions_panel']/tbody/tr");
        assertEquals(22, locator.getNodes().length);
        assertions.assertNodeHasText("//table[@id='versions_panel']/tbody/tr[1]/td[2]", "<b>CSS</b>");
        assertions.assertNodeHasText("//table[@id='versions_panel']/tbody/tr[22]/td[2]", "Version 1");

        tester.clickLinkWithText("next 20 versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "next 20 versions");
        locator = new XPathLocator(tester, "//table[@id='versions_panel']/tbody/tr");
        assertEquals(20, locator.getNodes().length);
        assertions.assertNodeHasText("//table[@id='versions_panel']/tbody/tr[1]/td[2]", "<b>CSS</b>");
        assertions.assertNodeHasText("//table[@id='versions_panel']/tbody/tr[20]/td[2]", "Version A");


        // test personal road map
        tester.gotoPage("secure/ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-roadmap-panel&pid=10040");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Bored");

        tester.clickLinkWithText("all versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "all versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(11, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[11]/div[1]/a[@class='versionBanner-name']", "Version still Bored");

        tester.clickLinkWithText("upcoming 10 versions");
        assertions.getLinkAssertions().assertLinkPresentWithExactText("//ul[@class='paging']", "all versions");
        assertions.getLinkAssertions().assertLinkNotPresentWithExactText("//ul[@class='paging']", "upcoming 10 versions");
        locator = new XPathLocator(tester, "//ul[@class='versionBannerList']/li");
        assertEquals(10, locator.getNodes().length);
        assertions.assertNodeHasText(FIRST_LINE, "Version 1");
        assertions.assertNodeHasText("//ul[@class='versionBannerList']/li[10]/div[1]/a[@class='versionBanner-name']", "Version Bored");
        tester.clickLinkWithText("all versions");


    }

    public void testVersionsHeaderContent()
    {
        navigation.browseProjectTabPanel("LOTS", "roadmap");
        tester.clickLinkWithText("all versions");
        assertUnreleasedVersion("Version 1", 10040, "", "", 2, 4, -1, null);
        assertUnreleasedVersion("Version 2", 10041, "", "", 2, 3, -1, null);
        assertUnreleasedVersion("Version A", 10042, "", "This is version A", 1, 3, -1, null);
        assertUnreleasedVersion("Version <b>B</b>", 10043, "", "This is version B - Version <b>B</b>", 1, 2, -1, null);
        assertUnreleasedVersion("Version 3", 10044, "19/Feb/09", "", 0, 1, -1, null);
        assertUnreleasedVersion("Version Version", 10045, "", "VVVVVVVVVVVVVVVVV", 0, 1, -1, null);
        assertUnreleasedVersion("Version 6", 10046, "", "", 0, 1, -1, null);
        assertUnreleasedVersion("Version 8", 10047, "", "", 0, 1, -1, null);
        assertUnreleasedVersion("Version Nick", 10048, "", "", 0, 1, -1, null);
        assertUnreleasedVersion("Version Bored", 10049, "", "", 0, 1, -1, null);
        assertUnreleasedVersion("Version still Bored", 10050, "", "", 0, 0, -1, null);

        navigation.browseProjectTabPanel("LOTS", "changelog");
        tester.clickLinkWithText("all versions");
        assertReleasedVersion("<b>CSS</b>", 10061, "21/Feb/09", "<b>CSS</b><b>CSS</b><b>CSS</b><b>CSS</b>", 0, -1, null);
        assertReleasedVersion("This is getting silly", 10060, "16/Feb/09", "", 1, -1, null);
        assertReleasedVersion("V2", 10059, "16/Feb/09", "V2V2V2V2", 2, -1, null);
        assertReleasedVersion("Lets throw in a Date", 10058, "27/Feb/09", "", 2, -1, null);
        assertReleasedVersion("Still going", 10057, "16/Feb/09", "", 1, -1, null);
        assertReleasedVersion("Going Again Version", 10056, "16/Feb/09", "", 1, -1, null);
        assertReleasedVersion("Version Anton", 10055, "16/Feb/09", "", 0, -1, null);
        assertReleasedVersion("Version Justus", 10054, "16/Feb/09", "", 1, -1, null);
        assertReleasedVersion("Version Brenden", 10053, "", "", 2, -1, null);
        assertReleasedVersion("Version Dylan", 10052, "01/Feb/09", "", 1, -1, null);
        assertReleasedVersion("Version Andreas", 10051, "16/Feb/09", "", 1, -1, null);


        // test components
        navigation.browseComponentTabPanel("LOTS", "Component 1", "roadmap");
        tester.clickLinkWithText("all versions");
        assertUnreleasedVersion("Version 1", 10040, "", "", 0, 1, 10030, "Component 1");
        assertUnreleasedVersion("Version 2", 10041, "", "", 0, 1, 10030, "Component 1");
        assertUnreleasedVersion("Version A", 10042, "18/Feb/09", "This is version A", 0, 1, 10030, "Component 1");
        assertUnreleasedVersion("Version <b>B</b>", 10043, "", "This is version B - Version <b>B</b>", 0, 1, 10030, "Component 1");
        assertUnreleasedVersion("Version 3", 10044, "19/Feb/09", "", 0, 1, 10030, "Component 1");
        assertUnreleasedVersion("Version Version", 10045, "", "VVVVVVVVVVVVVVVVV", 0, 1, 10030, "Component 1");
        assertUnreleasedVersion("Version 6", 10046, "", "", 0, 1, 10030, "Component 1");
        assertUnreleasedVersion("Version 8", 10047, "", "", 0, 1, 10030, "Component 1");
        assertUnreleasedVersion("Version Nick", 10048, "", "", 0, 1, 10030, "Component 1");
        assertUnreleasedVersion("Version Bored", 10049, "", "", 0, 0, 10030, "Component 1");
        assertUnreleasedVersion("Version still Bored", 10050, "", "", 0, 0, 10030, "Component 1");

        navigation.browseComponentTabPanel("LOTS", "Component 1", "changelog");
        tester.clickLinkWithText("all versions");
        assertReleasedVersion("<b>CSS</b>", 10061, "21/Feb/09", "<b>CSS</b><b>CSS</b><b>CSS</b><b>CSS</b>", 0, 10030, "Component 1");
        assertReleasedVersion("This is getting silly", 10060, "16/Feb/09", "", 1, 10030, "Component 1");
        assertReleasedVersion("V2", 10059, "16/Feb/09", "V2V2V2V2", 1, 10030, "Component 1");
        assertReleasedVersion("Lets throw in a Date", 10058, "27/Feb/09", "", 1, 10030, "Component 1");
        assertReleasedVersion("Still going", 10057, "16/Feb/09", "", 1, 10030, "Component 1");
        assertReleasedVersion("Going Again Version", 10056, "16/Feb/09", "", 0, 10030, "Component 1");
        assertReleasedVersion("Version Anton", 10055, "16/Feb/09", "", 0, 10030, "Component 1");
        assertReleasedVersion("Version Justus", 10054, "16/Feb/09", "", 1, 10030, "Component 1");
        assertReleasedVersion("Version Brenden", 10053, "", "", 1, 10030, "Component 1");
        assertReleasedVersion("Version Dylan", 10052, "01/Feb/09", "", 0, 10030, "Component 1");
        assertReleasedVersion("Version Andreas", 10051, "16/Feb/09", "", 0, 10030, "Component 1");


        // personal road map
        tester.gotoPage("secure/ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-roadmap-panel&pid=10040");
        tester.clickLinkWithText("all versions");
        assertPersonalUnreleasedVersion("Version 1", 10040, "", "", 2, 4);
        assertPersonalUnreleasedVersion("Version 2", 10041, "", "", 2, 3);
        assertPersonalUnreleasedVersion("Version A", 10042, "18/Feb/09", "This is version A", 1, 3);
        assertPersonalUnreleasedVersion("Version <b>B</b>", 10043, "", "This is version B - Version <b>B</b>", 1, 2);
        assertPersonalUnreleasedVersion("Version 3", 10044, "19/Feb/09", "", 0, 1);
        assertPersonalUnreleasedVersion("Version Version", 10045, "", "VVVVVVVVVVVVVVVVV", 0, 1);
        assertPersonalUnreleasedVersion("Version 6", 10046, "", "", 0, 1);
        assertPersonalUnreleasedVersion("Version 8", 10047, "", "", 0, 1);
        assertPersonalUnreleasedVersion("Version Nick", 10048, "", "", 0, 1);
        assertPersonalUnreleasedVersion("Version Bored", 10049, "", "", 0, 1);
        assertPersonalUnreleasedVersion("Version still Bored", 10050, "", "", 0, 0);

    }

    public void testVersionIssuesContent()
    {
        navigation.browseProjectTabPanel("LOTS", "roadmap");
        tester.clickLinkWithText("all versions");
        
        tester.clickLink("version-expando-" + 10040);
        assertRowCount(10040, 4);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10040, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10040, "LOTS-4", "Lots 4");
        assertIssueRow(4, 10040, "LOTS-2", "Lots 2");

        tester.clickLink("version-expando-" + 10041);
        assertRowCount(10040, 4);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10040, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10040, "LOTS-4", "Lots 4");
        assertIssueRow(4, 10040, "LOTS-2", "Lots 2");

        assertRowCount(10041, 3);
        assertIssueRow(1, 10041, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10041, "LOTS-4", "Lots 4");
        assertIssueRow(3, 10041, "LOTS-2", "Lots 2");


        tester.clickLink("version-expando-" + 10042);
        assertRowCount(10040, 4);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10040, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10040, "LOTS-4", "Lots 4");
        assertIssueRow(4, 10040, "LOTS-2", "Lots 2");

        assertRowCount(10041, 3);
        assertIssueRow(1, 10041, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10041, "LOTS-4", "Lots 4");
        assertIssueRow(3, 10041, "LOTS-2", "Lots 2");

        assertRowCount(10042, 3);
        assertIssueRow(1, 10042, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10042, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10042, "LOTS-2", "Lots 2");

        navigation.browseProject("LOTS");
        assertRowCount(10040, 4);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10040, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10040, "LOTS-4", "Lots 4");
        assertIssueRow(4, 10040, "LOTS-2", "Lots 2");

        assertRowCount(10041, 3);
        assertIssueRow(1, 10041, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10041, "LOTS-4", "Lots 4");
        assertIssueRow(3, 10041, "LOTS-2", "Lots 2");

        assertRowCount(10042, 3);
        assertIssueRow(1, 10042, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10042, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10042, "LOTS-2", "Lots 2");

        tester.clickLink("version-expando-" + 10050);
        assertions.assertNodeHasText("//li[@id='version-" + 10050 + "']/div[2]", "No issues");

        navigation.browseProjectTabPanel("LOTS", "changelog");
        tester.clickLinkWithText("all versions");

        tester.clickLink("version-expando-" + 10061);
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");

        tester.clickLink("version-expando-" + 10060);
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");
        assertRowCount(10060, 1);
        assertIssueRow(1, 10060, "LOTS-1", "LOTS 1");

        tester.clickLink("version-expando-" + 10059);
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");
        assertRowCount(10060, 1);
        assertIssueRow(1, 10060, "LOTS-1", "LOTS 1");
        assertRowCount(10059, 2);
        assertIssueRow(1, 10059, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10059, "LOTS-3", "Lots 3");

        tester.clickLink("version-expando-" + 10053);
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");
        assertRowCount(10060, 1);
        assertIssueRow(1, 10060, "LOTS-1", "LOTS 1");
        assertRowCount(10059, 2);
        assertIssueRow(1, 10059, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10059, "LOTS-3", "Lots 3");
        assertRowCount(10053, 2);
        assertIssueRow(1, 10053, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10053, "LOTS-4", "Lots 4");

        navigation.browseProject("LOTS");
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");
        assertRowCount(10060, 1);
        assertIssueRow(1, 10060, "LOTS-1", "LOTS 1");
        assertRowCount(10059, 2);
        assertIssueRow(1, 10059, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10059, "LOTS-3", "Lots 3");
        assertRowCount(10053, 2);
        assertIssueRow(1, 10053, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10053, "LOTS-4", "Lots 4");

        // test components

        navigation.browseComponentTabPanel("LOTS", "Component 1", "roadmap");
        tester.clickLinkWithText("all versions");

        tester.clickLink("version-expando-" + 10040);
        assertRowCount(10040, 1);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");

        tester.clickLink("version-expando-" + 10041);
        assertRowCount(10040, 1);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertRowCount(10041, 1);
        assertIssueRow(1, 10041, "LOTS-1", "LOTS 1");

        tester.clickLink("version-expando-" + 10042);
        assertRowCount(10040, 1);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertRowCount(10041, 1);
        assertIssueRow(1, 10041, "LOTS-1", "LOTS 1");
        assertRowCount(10042, 1);
        assertIssueRow(1, 10042, "LOTS-1", "LOTS 1");

        navigation.browseComponentTabPanel("LOTS", "Component 1");
        assertRowCount(10040, 1);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertRowCount(10041, 1);
        assertIssueRow(1, 10041, "LOTS-1", "LOTS 1");
        assertRowCount(10042, 1);
        assertIssueRow(1, 10042, "LOTS-1", "LOTS 1");

        tester.clickLink("version-expando-" + 10050);
        assertions.assertNodeHasText("//li[@id='version-" + 10050 + "']/div[2]", "No issues");

        navigation.browseComponentTabPanel("LOTS", "Component 1", "changelog");
        tester.clickLinkWithText("all versions");

        tester.clickLink("version-expando-" + 10061);
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");

        tester.clickLink("version-expando-" + 10060);
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");
        assertRowCount(10060, 1);
        assertIssueRow(1, 10060, "LOTS-1", "LOTS 1");

        tester.clickLink("version-expando-" + 10059);
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");
        assertRowCount(10060, 1);
        assertIssueRow(1, 10060, "LOTS-1", "LOTS 1");
        assertRowCount(10059, 1);
        assertIssueRow(1, 10059, "LOTS-1", "LOTS 1");

        tester.clickLink("version-expando-" + 10053);
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");
        assertRowCount(10060, 1);
        assertIssueRow(1, 10060, "LOTS-1", "LOTS 1");
        assertRowCount(10059, 1);
        assertIssueRow(1, 10059, "LOTS-1", "LOTS 1");
        assertRowCount(10053, 1);
        assertIssueRow(1, 10053, "LOTS-1", "LOTS 1");

        navigation.browseComponentTabPanel("LOTS", "Component 1");
        assertions.assertNodeHasText("//li[@id='version-" + 10061 + "']/div[2]", "No issues");
        assertRowCount(10060, 1);
        assertIssueRow(1, 10060, "LOTS-1", "LOTS 1");
        assertRowCount(10059, 1);
        assertIssueRow(1, 10059, "LOTS-1", "LOTS 1");
        assertRowCount(10053, 1);
        assertIssueRow(1, 10053, "LOTS-1", "LOTS 1");


        // test personal roadmap
        tester.gotoPage("secure/ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-roadmap-panel&pid=10040");
        tester.clickLinkWithText("all versions");

        tester.clickLink("version-expando-" + 10040);
        assertRowCount(10040, 4);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10040, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10040, "LOTS-4", "Lots 4");
        assertIssueRow(4, 10040, "LOTS-2", "Lots 2");

        tester.clickLink("version-expando-" + 10041);
        assertRowCount(10040, 4);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10040, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10040, "LOTS-4", "Lots 4");
        assertIssueRow(4, 10040, "LOTS-2", "Lots 2");

        assertRowCount(10041, 3);
        assertIssueRow(1, 10041, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10041, "LOTS-4", "Lots 4");
        assertIssueRow(3, 10041, "LOTS-2", "Lots 2");


        tester.clickLink("version-expando-" + 10042);
        assertRowCount(10040, 4);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10040, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10040, "LOTS-4", "Lots 4");
        assertIssueRow(4, 10040, "LOTS-2", "Lots 2");

        assertRowCount(10041, 3);
        assertIssueRow(1, 10041, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10041, "LOTS-4", "Lots 4");
        assertIssueRow(3, 10041, "LOTS-2", "Lots 2");

        assertRowCount(10042, 3);
        assertIssueRow(1, 10042, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10042, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10042, "LOTS-2", "Lots 2");

        tester.gotoPage("secure/ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-roadmap-panel&pid=10040");
        assertRowCount(10040, 4);
        assertIssueRow(1, 10040, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10040, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10040, "LOTS-4", "Lots 4");
        assertIssueRow(4, 10040, "LOTS-2", "Lots 2");

        assertRowCount(10041, 3);
        assertIssueRow(1, 10041, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10041, "LOTS-4", "Lots 4");
        assertIssueRow(3, 10041, "LOTS-2", "Lots 2");

        assertRowCount(10042, 3);
        assertIssueRow(1, 10042, "LOTS-1", "LOTS 1");
        assertIssueRow(2, 10042, "LOTS-3", "Lots 3");
        assertIssueRow(3, 10042, "LOTS-2", "Lots 2");

        tester.clickLink("version-expando-" + 10050);
        assertions.assertNodeHasText("//li[@id='version-" + 10050 + "']/div[2]", "No issues");


    }

    private void assertRowCount(int versionId, int count)
    {
        Locator locator = new XPathLocator(tester, "//li[@id='version-" + versionId + "']/div[2]/table//tr");
        assertEquals(count, locator.getNodes().length);
    }

    private void assertIssueRow(int row, int versionId, String issueKey, String issueSummary)
    {
        assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[2]/table//tr[" + row + "]/td[2]", issueKey);
        assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[2]/table//tr[" + row + "]/td[2]/a", "browse/" + issueKey);
        assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[2]/table//tr[" + row + "]/td[4]", issueSummary);
        assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[2]/table//tr[" + row + "]/td[4]/a", "browse/" + issueKey);

    }

    private void assertUnreleasedVersion(final String versionName, final int versionId, String releaseDate, String description,
                                         int unresolved, int total, int componentId, final String componentName)
    {
        assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/a[@class='versionBanner-name']", versionName);
        assertions.getLinkAssertions().assertLinkLocationEndsWith(versionName, "browse/LOTS/fixforversion/" + versionId);
        if (componentId == -1)
        {
            assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/a[1]", "browse/LOTS?selectedTab=com.atlassian.jira.plugin.system.project:roadmap-panel&expandVersion=" + versionId);
            assertions.assertNodeHasText("//a[@id='release-notes-" + versionId +"']", "Release Notes");
            assertions.getLinkAssertions().assertLinkIdLocationEndsWith("release-notes-" + versionId, "secure/ReleaseNote.jspa?projectId=10040&version=" + versionId);
        }
        else
        {
            assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/a[1]", "browse/LOTS/component/" + componentId + "?selectedTab=com.atlassian.jira.plugin.system.project:component-roadmap-panel&expandVersion=" + versionId);            
            assertions.assertNodeDoesNotExist("//a[@id='release-notes-" + versionId +"']");
        }
        if (StringUtils.isNotBlank(releaseDate))
        {
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/span[2]", releaseDate);
        }
        else
        {
            if (componentId != -1)
            {
                assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/span[2]", "Release Date: N/A");
            }
        }
        assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/span[1]", description);
        if (total != 0)
        {
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/div[1]/table//tr[2]", unresolved + " of " + total + " issues have been resolved");
            if (componentId == -1)
            {
                assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/div[1]/table//tr[2]/td[1]/span/a[1]", "secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+%22" + URLEncoder.encode(versionName) + "%22+AND+resolution+in+%28Fixed%2C+%22Won%27t+Fix%22%2C+Duplicate%2C+Incomplete%2C+%22Cannot+Reproduce%22%29");
                assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/div[1]/table//tr[2]/td[1]/span/a[2]", "secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+%22" + URLEncoder.encode(versionName) + "%22");
            }
            else
            {
                //'secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+%22Version+1%22+AND+component+%3D+%22Component+1%22%29+AND+resolution+in+%28Fixed%2C+%22Won%27t+Fix%22%2C+Duplicate%2C+Incomplete%2C+%22Cannot+Reproduce%22%29' but points to
                //'secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+%22Version+1%22+AND+component+%3D+%22Component+1%22+AND+resolution+in+%28Fixed%2C+%22Won%27t+Fix%22%2C+Duplicate%2C+Incomplete%2C+%22Cannot+Reproduce%22%29'.
                assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/div[1]/table//tr[2]/td[1]/span/a[1]", "secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+%22" + URLEncoder.encode(versionName) + "%22+AND+component+%3D+%22" + URLEncoder.encode(componentName) + "%22+AND+resolution+in+%28Fixed%2C+%22Won%27t+Fix%22%2C+Duplicate%2C+Incomplete%2C+%22Cannot+Reproduce%22%29");
                assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/div[1]/table//tr[2]/td[1]/span/a[2]", "secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+%22" + URLEncoder.encode(versionName) + "%22+AND+component+%3D+%22" + URLEncoder.encode(componentName) + "%22");
            }
        }
        else
        {
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/div[1]", "No issues");
        }
    }

    private void assertPersonalUnreleasedVersion(final String versionName, final int versionId, String releaseDate, String description,
                                         int unresolved, int total)
    {
        assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/a[@class='versionBanner-name']", versionName);
        assertions.getLinkAssertions().assertLinkLocationEndsWith(versionName, "browse/LOTS/fixforversion/" + versionId);
        assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/a[1]", "secure/ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-roadmap-panel&pid=10040&expandVersion=" + versionId);
        assertions.assertNodeDoesNotExist("//a[@id='release-notes-" + versionId +"']");
        if (StringUtils.isNotBlank(releaseDate))
        {
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/span[2]", releaseDate);
        }
        else
        {
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/span[2]", "Release Date: N/A");            
        }
        assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/span[1]", description);
        if (total != 0)
        {
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/div[1]/table//tr[2]", unresolved + " of " + total + " issues have been resolved");
            assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/div[1]/table//tr[2]/td[1]/span/a[1]", "secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+%22" + URLEncoder.encode(versionName) + "%22+AND+assignee+%3D+currentUser%28%29+AND+resolution+in+%28Fixed%2C+%22Won%27t+Fix%22%2C+Duplicate%2C+Incomplete%2C+%22Cannot+Reproduce%22%29");
            assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/div[1]/table//tr[2]/td[1]/span/a[2]", "secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+%22" + URLEncoder.encode(versionName) + "%22+AND+assignee+%3D+currentUser%28%29");
        }
        else
        {
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/div[1]", "No issues");
        }
    }

    private void assertReleasedVersion(final String versionName, final int versionId, String releaseDate, String description, int total, int componentId, final String componentName)
    {
        assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/a[@class='versionBanner-name']", versionName);
        assertions.getLinkAssertions().assertLinkLocationEndsWith(versionName, "browse/LOTS/fixforversion/" + versionId);
        if (componentId == -1)
        {
            assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/a[1]", "browse/LOTS?selectedTab=com.atlassian.jira.plugin.system.project:changelog-panel&expandVersion=" + versionId);
            assertions.assertNodeHasText("//a[@id='release-notes-" + versionId +"']", "Release Notes");
            assertions.getLinkAssertions().assertLinkIdLocationEndsWith("release-notes-" + versionId, "secure/ReleaseNote.jspa?projectId=10040&version=" + versionId);
        }
        else
        {
            assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/a[1]", "browse/LOTS/component/" + componentId + "?selectedTab=com.atlassian.jira.plugin.system.project:component-changelog-panel&expandVersion=" + versionId);
            assertions.assertNodeDoesNotExist("//a[@id='release-notes-" + versionId +"']");
        }
        if (StringUtils.isNotBlank(releaseDate))
        {
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/span[2]", releaseDate);
        }
        else
        {
            if (componentId != -1)
            {
                assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/span[2]", "Release Date: N/A");
            }
        }
        assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/span[1]", description);
        if (total != 0)
        {
            boolean versionRequiresQuotes = versionName.contains(" ");
            final String jqlVersionString = (versionRequiresQuotes) ? URLEncoder.encode("\"" + versionName + "\"") : URLEncoder.encode(versionName);
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/div[1]/a", total + " issues");
            if (componentId == -1)
            {
                assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/div[1]/a", "secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+" + jqlVersionString + "");
            }
            else
            {
                assertions.getLinkAssertions().assertLinkAtNodeEndsWith("//li[@id='version-" + versionId + "']/div[1]/div[1]/a", "secure/IssueNavigator.jspa?reset=true&mode=hide&jqlQuery=project+%3D+LOTS+AND+fixVersion+%3D+" + jqlVersionString + "+AND+component+%3D+%22" + URLEncoder.encode(componentName) + "%22");
            }
        }
        else
        {
            assertions.assertNodeHasText("//li[@id='version-" + versionId + "']/div[1]/div[1]", "No issues");
        }
    }

}