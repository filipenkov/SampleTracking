package com.atlassian.jira.webtest.selenium.setup;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Checks the JS for switching between the various database options.
 * Note: Only checks the JS (show/hide, auto-populate etc), does not check for logic of what the form validates.
 */
@WebTest({Category.SELENIUM_TEST })
public class TestSetupDatabase extends JiraSeleniumTest
{
    public void onSetUp()
    {
        super.onSetUp();
        getNavigator().gotoPage("secure/SetupDatabase!default.jspa", true);
        assertThat.textPresent("Step 1 of 4: Basic Settings");
    }

    /**
     * Options in the server language control should be localised to the
     * corresponding language (e.g. "German" should be in German).
     */
    public void testServerLanguage()
    {
        List<String> actualOptions = Arrays.asList(client.getSelectOptions("name=serverLanguage"));
        final List<String> requiredOptions = Arrays.asList(
                "English (UK)",
                "English (United States)",
                "fran\u00e7ais (France)",
                "Deutsch (Deutschland)",
                "\u65e5\u672c\u8a9e (\u65e5\u672c)",
                "espa\u00f1ol (Espa\u00f1a)"
        );
        assertTrue(actualOptions.containsAll(requiredOptions));
    }

    public void testSwitchInternalExternal()
    {
        // by default Internal should be selected (so no External fields visible)
        assertTrue(client.isChecked("name=databaseOption value=INTERNAL"));
        assertThat.elementNotVisible("databaseType");
        assertThat.elementNotVisible("databaseType_summary");
        assertThat.elementNotVisible("jdbcHostname");
        assertThat.elementNotVisible("schemaName");
        assertThat.elementNotVisible("testConnection");

        // change to External and check that the extra fields are visible
        client.click("jira-setupwizard-database-external", false);
        _checkDefaultJdbcFields();
    }

    public void testSwitchDatabaseType()
    {
        // change to external, default nothing is selected (empty fields)
        client.click("jira-setupwizard-database-external", false);
        _checkDefaultJdbcFields();

        // change database type - check for auto-populated fields
        client.selectOption("databaseType", "PostgreSQL");
        assertEquals("postgres72", client.getSelectedValue("databaseType"));
        assertThat.elementVisible("jdbcHostname");
        assertThat.elementVisible("jdbcPort");
        assertThat.elementVisible("jdbcDatabase");
        assertThat.elementNotVisible("jdbcSid");
        assertThat.elementVisible("jdbcUsername");
        assertThat.elementVisible("jdbcPassword");
        assertThat.elementVisible("schemaName");
        assertEquals("5432", client.getValue("jdbcPort"));
        assertEquals("public", client.getValue("schemaName"));
        assertEquals("", client.getValue("jdbcUsername"));
        
        // add custom fields
        client.type("jdbcUsername", "jira_user");
        assertEquals("jira_user", client.getValue("jdbcUsername"));

        // change database type again, check auto-populated and custom field
        client.selectOption("databaseType", "Oracle");
        assertEquals("oracle10g", client.getSelectedValue("databaseType"));
        assertThat.elementVisible("jdbcHostname");
        assertThat.elementVisible("jdbcPort");
        assertThat.elementNotVisible("jdbcDatabase");
        assertThat.elementVisible("jdbcSid");
        assertThat.elementVisible("jdbcUsername");
        assertThat.elementVisible("jdbcPassword");
        assertThat.elementNotVisible("schemaName");
        assertEquals("1521", client.getValue("jdbcPort"));
        assertEquals("", client.getValue("schemaName"));
        assertEquals("jira_user", client.getValue("jdbcUsername"));
    }

    private void _checkDefaultJdbcFields()
    {
        assertTrue(client.isChecked("name=databaseOption value=EXTERNAL"));
        assertEquals("", client.getSelectedValue("databaseType")); // the "pls select db" option
        assertThat.elementVisible("jdbcHostname");
        assertThat.elementVisible("jdbcPort");
        assertThat.elementVisible("jdbcDatabase");
        assertThat.elementNotVisible("jdbcSid");
        assertThat.elementVisible("jdbcUsername");
        assertThat.elementVisible("jdbcPassword");
        assertThat.elementNotVisible("schemaName");
        assertThat.elementVisible("testConnection");

        // by default everything is blank
        assertEquals("", client.getValue("jdbcHostname"));
        assertEquals("", client.getValue("jdbcPort"));
        assertEquals("", client.getValue("jdbcDatabase"));
        assertEquals("", client.getValue("jdbcUsername"));
        assertEquals("", client.getValue("jdbcPassword"));
        assertEquals("", client.getValue("schemaName"));
    }
}
