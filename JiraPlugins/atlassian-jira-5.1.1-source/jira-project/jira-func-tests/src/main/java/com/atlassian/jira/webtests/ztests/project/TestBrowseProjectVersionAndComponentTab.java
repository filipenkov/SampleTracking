package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.BROWSE_PROJECT })
public class TestBrowseProjectVersionAndComponentTab extends FuncTestCase
{

    protected void setUpTest()
    {
        administration.restoreData("TestBrowseProjectVersionTab.xml");
    }


    public void testNoVersionsAndComponents()
    {
        navigation.browseProject("NOVERSIONS");
        assertions.assertNodeDoesNotExist("//a[@id='versions-panel-panel']");
        assertions.assertNodeDoesNotExist("//a[@id='roadmap-panel-panel']");
        assertions.assertNodeDoesNotExist("//a[@id='changelog-panel-panel']");
        assertions.assertNodeExists("//a[@id='components-panel-panel']");

        navigation.browseComponentTabPanel("NOVERSIONS", "Component 1");
        assertions.assertNodeDoesNotExist("//a[@id='component-roadmap-panel-panel']");
        assertions.assertNodeDoesNotExist("//a[@id='component-changelog-panel-panel']");

        navigation.browseProject("NOCOMPS");
        assertions.assertNodeExists("//a[@id='versions-panel-panel']");
        assertions.assertNodeExists("//a[@id='roadmap-panel-panel']");
        assertions.assertNodeExists("//a[@id='changelog-panel-panel']");
        assertions.assertNodeDoesNotExist("//a[@id='components-panel-panel']");

        navigation.browseComponentTabPanel("HSP", "New Component 1");
        assertions.assertNodeExists("//a[@id='component-roadmap-panel-panel']");
        assertions.assertNodeExists("//a[@id='component-changelog-panel-panel']");
        
    }

    public void testFieldsAreHidden()
    {
        navigation.browseProject("FFHIDDEN");
        assertions.assertNodeDoesNotExist("//a[@id='versions-panel-panel']");
        assertions.assertNodeDoesNotExist("//a[@id='roadmap-panel-panel']");
        assertions.assertNodeDoesNotExist("//a[@id='changelog-panel-panel']");
        assertions.assertNodeExists("//a[@id='components-panel-panel']");

        navigation.browseComponentTabPanel("FFHIDDEN", "Component 1");
        assertions.assertNodeDoesNotExist("//a[@id='component-roadmap-panel-panel']");
        assertions.assertNodeDoesNotExist("//a[@id='component-changelog-panel-panel']");

        navigation.browseProject("COMPHIDDEN");
        assertions.assertNodeExists("//a[@id='versions-panel-panel']");
        assertions.assertNodeExists("//a[@id='roadmap-panel-panel']");
        assertions.assertNodeExists("//a[@id='changelog-panel-panel']");
        assertions.assertNodeDoesNotExist("//a[@id='components-panel-panel']");

        navigation.browseComponentTabPanel("HSP", "New Component 1");
        assertions.assertNodeExists("//a[@id='component-roadmap-panel-panel']");
        assertions.assertNodeExists("//a[@id='component-changelog-panel-panel']");
    }

    public void testVersionOrder() throws SAXException
    {
        navigation.browseProjectTabPanel("NOCOMPS", "versions");
        TableLocator tableLocator = new TableLocator(tester, "versions_panel");
        assertEquals(4, tableLocator.getTable().getRowCount());
        assertVersionRow(1, 10013, "<b>Version 4</b>", "23/Feb/09", "<b>Version 4 description</b>");
        assertVersionRow(2, 10012, "V3", "25/Feb/09", "");
        assertVersionRow(3, 10011, "Version 2", "", "V2 description");
        assertVersionRow(4, 10010, "Version 1", "", "");

        assertEquals("No Components - jWebTest JIRA installation", tester.getDialog().getResponse().getTitle().trim());

        tester.clickLinkWithText("V3");
        assertions.assertNodeHasText(new CssLocator(tester, "#content header .breadcrumbs"), "No Components");
        assertions.assertNodeHasText(new CssLocator(tester, "#content header h1"), "V3");

        assertEquals("No Components: V3 - jWebTest JIRA installation", tester.getDialog().getResponse().getTitle().trim());

    }

    public void testManageVersionsAsAdmin()
    {
        navigation.login(ADMIN_USERNAME);
        navigation.browseProjectTabPanel("NOCOMPS", "versions");
        final String manageVersionsLinkId = "manage_versions";
        final Locator locator = new IdLocator(tester, manageVersionsLinkId);
        assertions.assertNodeExists(locator);

        tester.clickLink(manageVersionsLinkId);
        assertions.getURLAssertions().assertCurrentURLEndsWith(getEnvironmentData().getContext() +
                "/plugins/servlet/project-config/NOCOMPS/versions");

        assertions.assertNodeByIdHasText("project-config-header-name", "No Components");
    }

    public void testManageVersionsAsUser()
    {
        navigation.login(FRED_USERNAME);
        navigation.browseProjectTabPanel("NOCOMPS", "versions");
        final String manageVersionsLinkId = "manage_versions";
        final Locator locator = new IdLocator(tester, manageVersionsLinkId);

        assertions.assertNodeDoesNotExist(locator);
    }

    public void testManageVersionsAsProjectAdmin()
    {
        navigation.login(ADMIN_USERNAME);

        administration.permissionSchemes().defaultScheme().grantPermissionToGroup(23, "jira-users");

        navigation.logout();

        navigation.login(FRED_USERNAME);
        navigation.browseProjectTabPanel("NOCOMPS", "versions");
        final String manageVersionsLinkId = "manage_versions";
        final Locator locator = new IdLocator(tester, manageVersionsLinkId);

        assertions.assertNodeExists(locator);
        tester.clickLink(manageVersionsLinkId);
        assertions.getURLAssertions().assertCurrentURLEndsWith(getEnvironmentData().getContext() +
                "/plugins/servlet/project-config/NOCOMPS/versions");

        assertions.assertNodeByIdHasText("project-config-header-name", "No Components");
    }

    private void assertVersionRow(int row, int versionId, final String name, final String date, final String description)
    {
        assertions.assertNodeHasText("//table[@id='versions_panel']//tr[" + row + "]/td[2]", name);
        assertions.getLinkAssertions().assertLinkLocationEndsWith(name, "/browse/NOCOMPS/fixforversion/" + versionId);
        assertions.assertNodeHasText("//table[@id='versions_panel']//tr[" + row + "]/td[3]", date);
        assertions.assertNodeHasText("//table[@id='versions_panel']//tr[" + row + "]/td[4]/span", description);
    }

    private void assertComponentRow(int row, int versionId, final String name, final String lead, final String description)
    {
        assertions.assertNodeHasText("//table[@id='components_panel']//tr[" + row + "]/td[2]/a", name);
        assertions.getLinkAssertions().assertLinkLocationEndsWith(name, "/browse/NOVERSIONS/component/" + versionId);
        assertions.assertNodeHasText("//table[@id='components_panel']//tr[" + row + "]/td[3]", lead);
        assertions.assertNodeHasText("//table[@id='components_panel']//tr[" + row + "]/td[4]", description);
    }

    public void testComponentsTab() throws SAXException
    {
        navigation.browseProjectTabPanel("NOVERSIONS", "components");
        TableLocator tableLocator = new TableLocator(tester, "components_panel");
        assertEquals(4, tableLocator.getTable().getRowCount());
        assertComponentRow(1, 10011, "<b>Component 2</b>", "", "");
        assertComponentRow(2, 10010, "Component 1", ADMIN_FULLNAME, "Component Description");
        assertComponentRow(3, 10012, "Component 3", ADMIN_FULLNAME, "");
        assertComponentRow(4, 10013, "Component 4", "", "");

        assertEquals("No Versions - jWebTest JIRA installation", tester.getDialog().getResponse().getTitle().trim());

        tester.clickLinkWithText("Component 1");
        assertions.assertNodeHasText(new CssLocator(tester, "#content header .breadcrumbs"), "No Versions");
        assertions.assertNodeHasText(new CssLocator(tester, "#content header h1"), "Component 1");

        assertEquals("No Versions: Component 1 - jWebTest JIRA installation", tester.getDialog().getResponse().getTitle().trim());

    }

    public void testEmptyAndExpandedColumnsInSummary()
    {
        navigation.browseVersionTabPanel("NOCOMPS", "Version 1", "summary");
        assertions.assertNodeHasText(new CssLocator(tester, "#content .content-body"), "There is no information to display for the summary of this version. This could be a version with no issues or activity.");

        navigation.browseVersionTabPanel("NOCOMPS", "Version 2", "summary");
        assertions.assertNodeHasText(new CssLocator(tester, "#fragversiondescription .mod-header"), "Description");
        assertions.assertNodeHasText(new CssLocator(tester, "#fragversiondescription .mod-content"), "V2 description");

        navigation.browseComponentTabPanel("NOVERSIONS", "<b>Component 2</b>", "summary");
        assertions.assertNodeHasText(new CssLocator(tester, "#content .content-body"), "There is no information to display for the summary of this component. This could be a component with no issues, versions or activity.");

        navigation.browseComponentTabPanel("NOVERSIONS", "Component 1", "summary");
        assertions.assertNodeHasText(new CssLocator(tester, "#fragcomponentdescription .mod-header"), "Description");
        assertions.assertNodeHasText(new CssLocator(tester, "#fragcomponentdescription .mod-content"), "Component Description");

        navigation.browseComponentTabPanel("HSP", "New Component 1", "summary");
        assertions.assertNodeHasText(new CssLocator(tester, "#fragdueversions .mod-header"), "Versions: Unreleased");
        assertions.assertNodeHasText(new CssLocator(tester, "#fragdueversions .mod-content .issues li"), "New Version 4");
    }

}