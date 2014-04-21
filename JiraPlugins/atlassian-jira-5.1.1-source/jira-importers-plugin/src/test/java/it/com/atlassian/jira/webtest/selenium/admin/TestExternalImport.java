package it.com.atlassian.jira.webtest.selenium.admin;

import com.atlassian.jira.plugins.importer.po.ExternalImportPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestExternalImport extends BaseJiraWebTest {

    /**
     * Test order in which we present importers https://studio.atlassian.com/browse/JIM-733
     */
    @Test
    public void importersInOrder() {
        assertEquals(ImmutableList.of("Comma-separated values (CSV)", "Bugzilla", "FogBugz", "FogBugz On Demand",
                "Pivotal Tracker", "Mantis", "Trac", "JSON", "Jelly"), jira.gotoLoginPage().loginAsSysAdmin(ExternalImportPage.class).getImportersOrder());
    }
}
