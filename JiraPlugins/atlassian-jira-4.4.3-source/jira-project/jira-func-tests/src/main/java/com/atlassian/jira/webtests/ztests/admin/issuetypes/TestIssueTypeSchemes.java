package com.atlassian.jira.webtests.ztests.admin.issuetypes;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.table.AndCell;
import com.atlassian.jira.webtests.table.LinkCell;
import com.atlassian.jira.webtests.table.NotCell;
import com.atlassian.jira.webtests.table.TextCell;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 * Functional test issue type schemes
 *
 * @since v3.12
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.ISSUES, Category.SCHEMES })
public class TestIssueTypeSchemes extends JIRAWebTest
{
    private static final String DEFAULT_ISSUE_TYPE_SCHEME_NAME = "Default Issue Type Scheme";
    private static final String DEFAULT_ISSUE_TYPE_SCHEME_DESC = "Default issue type scheme is the list of global issue types. All newly created issue types will automatically be added to this scheme.";

    public void testIssueTypeSchemesSelectOrder() throws SAXException
    {
        restoreData("TestIssueTypeSchemes_Order.xml");
        // Click Link 'administration' (id='admin_link').
        navigation.gotoAdmin();
        // Click Link 'administration' (id='admin_link').

        gotoPage("/plugins/servlet/project-config/HSP/issuetypes");

        tester.clickLink("project-config-issuetype-scheme-change");
        tester.checkCheckbox("createType", "chooseScheme");

        final WebForm select = getDialog().getResponse().getFormWithName("jiraform");
        final String[] values = select.getOptionValues("schemeId");
        assertEquals("10000", values[0]);
        assertEquals("10010", values[1]);
        assertEquals("10013", values[2]);
        assertEquals("10012", values[3]);
        assertEquals("10011", values[4]);
    }

    public TestIssueTypeSchemes(final String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestIssueTypeSchemes.xml");
    }

    //------------------------------------------------------------------------------------------------------------ Tests
    public void testIssueTypeSchemesView()
    {
        gotoIssueTypeSchemes();
        assertCommonIssueTypeSchemesTable();
    }

    public void testIssueTypeSchemeCreate()
    {
        _testIssueTypeSchemeCreateValidation();
        _testIssueTypeSchemeCreate();
    }

    public void testIssueTypeSchemeCopy()
    {
        _testIssueTypeSchemeCopyValidation();
        _testIssueTypeSchemeCopy();
    }

    public void testIssueTypeSchemeEdit()
    {
        _testIssueTypeSchemeEditValidation();
    }

    public void testIssueTypeSchemeDelete()
    {
        //delete scheme with project association
        gotoIssueTypeSchemes();
        clickLink("delete_10011");
        assertTextPresent("Delete Issue Type Scheme: Associated Issue Type Scheme");
        assertTextSequence(new String[] { "You are about to delete the Issue Type Scheme named", "Associated Issue Type Scheme" });
        assertTextPresent("There is one project (" + PROJECT_MONKEY + ") currently using this scheme. This project will revert to using the default global issue type scheme.");
        submit("Delete");

        try
        {
            final WebTable issueTypeSchemesTable = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(3, issueTypeSchemesTable.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable, 0, new Object[] { "Name", "Description", "Options", "Projects", "Operations" });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable, 1, new Object[] { DEFAULT_ISSUE_TYPE_SCHEME_NAME, DEFAULT_ISSUE_TYPE_SCHEME_DESC, new TextCell(new String[] { "Bug", "(Default)", "New Feature", "Task", "Improvement" }), "Global (all unconfigured projects)", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10000", "Edit"), new LinkCell("AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=issuetype&schemeId=10000", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10000", "Copy"), new NotCell(new TextCell("Delete"))) });
            assertTableHasNoMatchingRow(issueTypeSchemesTable, 1, new Object[] { "Associated Issue Type Scheme", "Description for associated issue type scheme", new AndCell(new TextCell(new String[] { "Bug", "Improvement" }), new NotCell(new TextCell("(Default)"))), new LinkCell("/plugins/servlet/project-config/MKY/summary", PROJECT_MONKEY), new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10011", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10011", "Delete")) });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable, 1, new Object[] { "Test Issue Type Scheme", "Description for test issue type scheme", new TextCell(new String[] { "Improvement", "New Feature", "Bug", "(Default)", "Task" }), "No projects", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10010", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10010", "Delete")) });
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }

        //delete scheme with no project association
        gotoIssueTypeSchemes();
        clickLink("delete_10010");
        assertTextPresent("Delete Issue Type Scheme: Test Issue Type Scheme");
        assertTextSequence(new String[] { "You are about to delete the Issue Type Scheme named", "Test Issue Type Scheme" });
        assertTextPresent("There are no projects currently using this scheme.");
        submit("Delete");

        try
        {
            final WebTable issueTypeSchemesTable1 = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(2, issueTypeSchemesTable1.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable1, 0, new Object[] { "Name", "Description", "Options", "Projects", "Operations" });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { DEFAULT_ISSUE_TYPE_SCHEME_NAME, DEFAULT_ISSUE_TYPE_SCHEME_DESC, new TextCell(new String[] { "Bug", "(Default)", "New Feature", "Task", "Improvement" }), "Global (all unconfigured projects)", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10000", "Edit"), new LinkCell("AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=issuetype&schemeId=10000", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10000", "Copy"), new NotCell(new TextCell("Delete"))) });
            assertTableHasNoMatchingRow(issueTypeSchemesTable1, 1, new Object[] { "Associated Issue Type Scheme", "Description for associated issue type scheme", new AndCell(new TextCell(new String[] { "Bug", "Improvement" }), new NotCell(new TextCell("(Default)"))), new LinkCell("/plugins/servlet/project-config/MKY/summary", PROJECT_MONKEY), new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10011", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10011", "Delete")) });
            assertTableHasNoMatchingRow(issueTypeSchemesTable1, 1, new Object[] { "Test Issue Type Scheme", "Description for test issue type scheme", new TextCell(new String[] { "Improvement", "New Feature", "Bug", "(Default)", "Task" }), "No projects", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10010", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10010", "Delete")) });
        }
        catch (SAXException e1)
        {
            throw new RuntimeException(e1);
        }
    }

    public void testIssueTypeSchemeAssociateDefaultScheme()
    {
        gotoIssueTypeSchemes();
        clickLink("associate_10000");
        assertTextPresent("Associate Issue Type Scheme");
        assertTextPresent("Only projects not currently associated with the default scheme are displayed.");
        assertOptionsEqual("projects", new String[] { PROJECT_MONKEY });
        selectOption("projects", PROJECT_MONKEY);
        submit("Associate");

        try
        {
            final WebTable issueTypeSchemesTable1 = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(4, issueTypeSchemesTable1.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable1, 0, new Object[] { "Name", "Description", "Options", "Projects", "Operations" });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { DEFAULT_ISSUE_TYPE_SCHEME_NAME, DEFAULT_ISSUE_TYPE_SCHEME_DESC, new TextCell(new String[] { "Bug", "(Default)", "New Feature", "Task", "Improvement" }), "Global (all unconfigured projects)", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10000", "Edit"), new LinkCell("AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=issuetype&schemeId=10000", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10000", "Copy"), new NotCell(new TextCell("Delete"))) });
            //assert that Associated Issue type Scheme no longer has the monkey projects as an association
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { "Associated Issue Type Scheme", "Description for associated issue type scheme", new AndCell(new TextCell(new String[] { "Bug", "Improvement" }), new NotCell(new TextCell("(Default)"))), "No projects", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10011", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10011", "Delete")) });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { "Test Issue Type Scheme", "Description for test issue type scheme", new TextCell(new String[] { "Improvement", "New Feature", "Bug", "(Default)", "Task" }), "No projects", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10010", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10010", "Delete")) });
        }
        catch (SAXException e1)
        {
            throw new RuntimeException(e1);
        }

        //goto associate default issue type scheme to check that all projects are associated with it
        clickLink("associate_10000");
        assertTextPresent("Associate Issue Type Scheme");
        assertTextSequence(new String[] { "No projects available to be associated with scheme", "Default Issue Type Scheme" });
        assertFormElementNotPresent("projects");
        assertSubmitButtonNotPresent("Associate");
    }

    public void testIssueTypeSchemeAssociateSchemeWithProjectAssociation()
    {
        //associate another project (ie. 2 selected now).
        gotoIssueTypeSchemes();
        clickLink("associate_10011");
        assertTextPresent("Associate Issue Type Scheme");
        assertOptionsEqual("projects", new String[] { PROJECT_HOMOSAP, PROJECT_MONKEY });
        assertOptionSelected("projects", PROJECT_MONKEY);//assert that currently associated project is selected
        selectMultiOption("projects", PROJECT_MONKEY);
        selectMultiOption("projects", PROJECT_HOMOSAP);
        submit("Associate");

        try
        {
            final WebTable issueTypeSchemesTable = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(4, issueTypeSchemesTable.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable, 0, new Object[] { "Name", "Description", "Options", "Projects", "Operations" });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable, 1, new Object[] { DEFAULT_ISSUE_TYPE_SCHEME_NAME, DEFAULT_ISSUE_TYPE_SCHEME_DESC, new TextCell(new String[] { "Bug", "(Default)", "New Feature", "Task", "Improvement" }), "Global (all unconfigured projects)", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10000", "Edit"), new LinkCell("AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=issuetype&schemeId=10000", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10000", "Copy"), new NotCell(new TextCell("Delete"))) });
            //check scheme is now associated to two projects
            assertTableHasMatchingRowFrom(issueTypeSchemesTable, 1, new Object[] { "Associated Issue Type Scheme", "Description for associated issue type scheme", new AndCell(new TextCell(new String[] { "Bug", "Improvement" }), new NotCell(new TextCell("(Default)"))), new AndCell(new LinkCell("/plugins/servlet/project-config/MKY/summary", PROJECT_MONKEY), new LinkCell("/plugins/servlet/project-config/HSP/summary", PROJECT_HOMOSAP)), new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10011", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10011", "Delete")) });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable, 1, new Object[] { "Test Issue Type Scheme", "Description for test issue type scheme", new TextCell(new String[] { "Improvement", "New Feature", "Bug", "(Default)", "Task" }), "No projects", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10010", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10010", "Delete")) });
        }
        catch (SAXException e1)
        {
            throw new RuntimeException(e1);
        }

        //remove one project association from the 2
        clickLink("associate_10011");
        assertTextPresent("Associate Issue Type Scheme");
        assertOptionsEqual("projects", new String[] { PROJECT_HOMOSAP, PROJECT_MONKEY });
        assertOptionSelected("projects", PROJECT_MONKEY);//assert that currently associated project is selected
        assertOptionSelected("projects", PROJECT_HOMOSAP);//assert that currently associated project is selected
        selectOption("projects", PROJECT_HOMOSAP);
        submit("Associate");

        try
        {
            final WebTable issueTypeSchemesTable1 = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(4, issueTypeSchemesTable1.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable1, 0, new Object[] { "Name", "Description", "Options", "Projects", "Operations" });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { DEFAULT_ISSUE_TYPE_SCHEME_NAME, DEFAULT_ISSUE_TYPE_SCHEME_DESC, new TextCell(new String[] { "Bug", "(Default)", "New Feature", "Task", "Improvement" }), "Global (all unconfigured projects)", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10000", "Edit"), new LinkCell("AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=issuetype&schemeId=10000", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10000", "Copy"), new NotCell(new TextCell("Delete"))) });
            //assert that Associated Issue type Scheme is associated to homosapien only
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { "Associated Issue Type Scheme", "Description for associated issue type scheme", new AndCell(new TextCell(new String[] { "Bug", "Improvement" }), new NotCell(new TextCell("(Default)"))), new LinkCell("/plugins/servlet/project-config/HSP/summary", PROJECT_HOMOSAP), new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10011", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10011", "Delete")) });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { "Test Issue Type Scheme", "Description for test issue type scheme", new TextCell(new String[] { "Improvement", "New Feature", "Bug", "(Default)", "Task" }), "No projects", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10010", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10010", "Delete")) });
        }
        catch (final SAXException e1)
        {
            throw new RuntimeException(e1);
        }
    }

    public void testIssueTypeSchemeAssociateSchemeWithNoProjectAssociation()
    {
        gotoIssueTypeSchemes();
        clickLink("associate_10010");
        assertTextPresent("Associate Issue Type Scheme");
        assertOptionsEqual("projects", new String[] { PROJECT_HOMOSAP, PROJECT_MONKEY });
        selectMultiOption("projects", PROJECT_MONKEY);//take the monkey project association from the other scheme
        submit("Associate");

        try
        {
            final WebTable issueTypeSchemesTable1 = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(4, issueTypeSchemesTable1.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable1, 0, new Object[] { "Name", "Description", "Options", "Projects", "Operations" });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { DEFAULT_ISSUE_TYPE_SCHEME_NAME, DEFAULT_ISSUE_TYPE_SCHEME_DESC, new TextCell(new String[] { "Bug", "(Default)", "New Feature", "Task", "Improvement" }), "Global (all unconfigured projects)", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10000", "Edit"), new LinkCell("AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=issuetype&schemeId=10000", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10000", "Copy"), new NotCell(new TextCell("Delete"))) });
            //assert that Associated Issue type Scheme no longer has the monkey projects as an association
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { "Associated Issue Type Scheme", "Description for associated issue type scheme", new AndCell(new TextCell(new String[] { "Bug", "Improvement" }), new NotCell(new TextCell("(Default)"))), "No projects", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10011", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10011", "Delete")) });
            //assert that test issue type scheme is associated to monkey
            assertTableHasMatchingRowFrom(issueTypeSchemesTable1, 1, new Object[] { "Test Issue Type Scheme", "Description for test issue type scheme", new TextCell(new String[] { "Improvement", "New Feature", "Bug", "(Default)", "Task" }), new LinkCell("/plugins/servlet/project-config/MKY/summary", PROJECT_MONKEY), new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10010", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10010", "Delete")) });
        }
        catch (final SAXException e1)
        {
            throw new RuntimeException(e1);
        }
    }

    //-------------------------------------------------------------------------------------------------------- Sub Tests
    private void _testIssueTypeSchemeCreateValidation()
    {
        gotoIssueTypeSchemes();
        submit("Add");
        assertFormElementHasValue("name", "");
        assertFormElementHasValue("description", "");
        assertIssueTypeSchemeFormValidation();
    }

    private void _testIssueTypeSchemeCreate()
    {
        //test that the name & description entered in the manage page is passed over properly
        gotoIssueTypeSchemes();
        setFormElement("name", "testIssueTypeSchemeCreate scheme");
        setFormElement("description", "description for testIssueTypeSchemeCreate scheme");
        submit("Add");
        assertFormElementHasValue("name", "testIssueTypeSchemeCreate scheme");
        assertFormElementHasValue("description", "description for testIssueTypeSchemeCreate scheme");

        /*
         * Cannot test creating issue type schemes further due to js limitation of jWebUnit.
         * Further testing is carried out in Selenium
         */
    }

    private void _testIssueTypeSchemeCopyValidation()
    {
        gotoIssueTypeSchemes();
        clickLink("copy_10000"); //Default issue type scheme
        assertFormElementHasValue("name", "Copy of " + DEFAULT_ISSUE_TYPE_SCHEME_NAME);
        assertFormElementHasValue("description", DEFAULT_ISSUE_TYPE_SCHEME_DESC);
        assertIssueTypeSchemeFormValidation();

        gotoIssueTypeSchemes();
        clickLink("copy_10010"); //Test Issue Type Scheme
        assertFormElementHasValue("name", "Copy of Test Issue Type Scheme");
        assertFormElementHasValue("description", "Description for test issue type scheme");
        assertIssueTypeSchemeFormValidation();

        gotoIssueTypeSchemes();
        clickLink("copy_10011"); //Associated Issue Type Scheme
        assertFormElementHasValue("name", "Copy of Associated Issue Type Scheme");
        assertFormElementHasValue("description", "Description for associated issue type scheme");
        assertIssueTypeSchemeFormValidation();
    }

    private void _testIssueTypeSchemeCopy()
    {
        gotoIssueTypeSchemes();
        assertCommonIssueTypeSchemesTable(4);

        clickLink("copy_10000"); //Default issue type scheme
        assertTextPresent("Add Issue Types Scheme");
        assertFormElementHasValue("name", "Copy of " + DEFAULT_ISSUE_TYPE_SCHEME_NAME);
        assertFormElementHasValue("description", DEFAULT_ISSUE_TYPE_SCHEME_DESC);

        /*
         * Cannot test copying issue type schemes further due to js limitation of jWebUnit.
         * Further testing is carried out in Selenium
         */
/*
        setFormElement("name", "copy of 10000");
        setFormElement("description", "description of 10000");
        submit("Save");
        assertTextPresent("Manage Issue Types");

        try
        {
            assertCommonIssueTypeSchemesTable(5);//check the common base schemes
            //check that the newly copied scheme is also there
            WebTable issueTypeSchemesTable = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertTableHasMatchingRowFrom(issueTypeSchemesTable, 1, new Object[]{ "copy of 10000", "description of 10000", new TextCell(new String[] {"Bug", "(Default)", "New Feature", "Task", "Improvement"}), "Global (all unconfigured projects)", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10000", "Edit"), new LinkCell("AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=issuetype&schemeId=10000", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10000", "Copy"), new NotCell(new TextCell("Delete")))});
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
*/

        gotoIssueTypeSchemes();
        clickLink("copy_10010"); //Test Issue Type Scheme
        assertTextPresent("Add Issue Types Scheme");
        assertFormElementHasValue("name", "Copy of Test Issue Type Scheme");
        assertFormElementHasValue("description", "Description for test issue type scheme");

        gotoIssueTypeSchemes();
        clickLink("copy_10011"); //Associated Issue Type Scheme
        assertTextPresent("Add Issue Types Scheme");
        assertFormElementHasValue("name", "Copy of Associated Issue Type Scheme");
        assertFormElementHasValue("description", "Description for associated issue type scheme");
    }

    private void _testIssueTypeSchemeEditValidation()
    {
        gotoIssueTypeSchemes();
        clickLink("edit_10000"); //Default issue type scheme
        assertFormElementHasValue("name", DEFAULT_ISSUE_TYPE_SCHEME_NAME);
        assertFormElementHasValue("description", DEFAULT_ISSUE_TYPE_SCHEME_DESC);
        assertIssueTypeSchemeFormValidation();

        gotoIssueTypeSchemes();
        clickLink("edit_10010"); //Test Issue Type Scheme
        assertFormElementHasValue("name", "Test Issue Type Scheme");
        assertFormElementHasValue("description", "Description for test issue type scheme");
        assertIssueTypeSchemeFormValidation();

        gotoIssueTypeSchemes();
        clickLink("edit_10011"); //Associated Issue Type Scheme
        assertFormElementHasValue("name", "Associated Issue Type Scheme");
        assertFormElementHasValue("description", "Description for associated issue type scheme");
        assertIssueTypeSchemeFormValidation();
    }

    //--------------------------------------------------------------------------------------------------- Helper Methods
    public void gotoIssueTypeSchemes()
    {
        gotoAdmin();
        clickLink("issue_types");
        clickLinkWithText("Issue Types Scheme");
    }

    public void assertCommonIssueTypeSchemesTable()
    {
        assertCommonIssueTypeSchemesTable(4);
    }

    public void assertCommonIssueTypeSchemesTable(final int numberOfRows)
    {
        //assert that the issue type schemes appear as it is in the import file.
        try
        {
            final WebTable issueTypeSchemesTable = getDialog().getResponse().getTableWithID("issuetypeschemes");
            assertEquals(numberOfRows, issueTypeSchemesTable.getRowCount());
            assertTableRowEquals(issueTypeSchemesTable, 0, new Object[] { "Name", "Description", "Options", "Projects", "Operations" });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable, 1, new Object[] { DEFAULT_ISSUE_TYPE_SCHEME_NAME, DEFAULT_ISSUE_TYPE_SCHEME_DESC, new TextCell(new String[] { "Bug", "(Default)", "New Feature", "Task", "Improvement" }), "Global (all unconfigured projects)", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10000", "Edit"), new LinkCell("AssociateIssueTypeSchemesWithDefault!default.jspa?fieldId=issuetype&schemeId=10000", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10000", "Copy"), new NotCell(new TextCell("Delete"))) });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable, 1, new Object[] { "Test Issue Type Scheme", "Description for test issue type scheme", new TextCell(new String[] { "Improvement", "New Feature", "Bug", "(Default)", "Task" }), "No projects", new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10010", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10010", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10010", "Delete")) });
            assertTableHasMatchingRowFrom(issueTypeSchemesTable, 1, new Object[] { "Associated Issue Type Scheme", "Description for associated issue type scheme", new AndCell(new TextCell(new String[] { "Bug", "Improvement" }), new NotCell(new TextCell("(Default)"))), new LinkCell("/plugins/servlet/project-config/MKY/summary", PROJECT_MONKEY), new AndCell(new LinkCell("ConfigureOptionSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Edit"), new LinkCell("AssociateIssueTypeSchemes!default.jspa?fieldId=issuetype&schemeId=10011", "Associate"), new LinkCell("ConfigureOptionSchemes!copy.jspa?fieldId=issuetype&schemeId=10011", "Copy"), new LinkCell("DeleteOptionScheme!default.jspa?fieldId=issuetype&schemeId=10011", "Delete")) });
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void assertIssueTypeSchemeFormValidation()
    {
        assertTextNotPresent("You must select at least one option");
        assertTextNotPresent("You must enter a valid name.");
        setFormElement("name", "");
        setFormElement("description", "");

        submit("Save");
        assertTextPresent("You must select at least one option");
        assertTextPresent("You must enter a valid name.");

        setFormElement("name", "test name");
        submit("Save");
        assertTextPresent("You must select at least one option");
        assertTextNotPresent("You must enter a valid name.");

        /*
         * Cannot test adding issue types to the schemes due to js limitation of jWebUnit.
         * Further testing is carried out in Selenium
         */
/*
        clickLinkWithText("add all");
        setFormElement("name", "");
        submit("Save");
        assertTextNotPresent("You must select at least one option");
        assertTextPresent("You must enter a valid name.");

        clickLinkWithText("remove all");
        setFormElement("name", "testIssueTypeSchemeCreateValidation");
        submit("Save");
        assertTextPresent("You must select at least one option");
        assertTextNotPresent("You must enter a valid name.");
*/
    }
}
