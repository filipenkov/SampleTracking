package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Field;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.util.List;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertNotNull;

public class TestCsvSummaryFieldIsRequired extends BaseJiraWebTest {

    private JiraRestClient restClient;

    @Before
    public void setUpTest() {
        restClient = ITUtils.createRestClient(jira.environmentData());
        backdoor.restoreData("TestCsvImport.xml");
    }

    /**
     * test that the following cases with no mapping to the summary field, fails its validation
     * case 1: existing config with redundant mappings to summary field (see redundant-field-mappings.properties)
     * case 2: CSV header called summary (see redundant-field-mappings.csv)
     * @throws org.xml.sax.SAXException
     */
    @Test

    public void testImportWizardValidatesSummaryFieldIsMapped() throws SAXException
    {
        CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCsvResource("redundant-field-mappings.csv"));
        setupPage.setConfigurationFile(ITUtils.getCsvResource("redundant-field-mappings.properties"));

        CsvFieldMappingsPage fieldMappingsPage = setupPage.next().setReadFromCsv(false).next();
        assertFieldMappings(fieldMappingsPage);

        assertFalse(fieldMappingsPage.isNextEnabled());
        assertEquals("The JIRA \"Summary\" field has not been mapped.", fieldMappingsPage.getHintSectionText());


        //check that selecting a summary field validates fine
        fieldMappingsPage.setFieldMapping("summary", "summary");

        //check that the field mappings has not changed
        assertFieldMappings(fieldMappingsPage);
        assertTrue(fieldMappingsPage.isNextEnabled());

        final ImporterFinishedPage importerFinishedPage = fieldMappingsPage.next().next().waitUntilFinished();
        assertTrue(importerFinishedPage.isSuccess());

        final Issue issue = restClient.getIssueClient().getIssue("TRV-1", new NullProgressMonitor());
        final Field userNewCf = issue.getFieldByName("userNewCF");
        assertNotNull(userNewCf);
        assertNotNull(userNewCf.getValue());
        // JRJC doesn't return type here assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", userNewCf.getType());

        final Field versionCf = issue.getFieldByName("Version Custom Field");
        assertNotNull(versionCf);
        assertNotNull(versionCf.getValue());
        // JRJC doesn't return type here assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:multiversion", versionCf.getType());
    }


    private void assertFieldMappings(CsvFieldMappingsPage fieldMappingsPage) throws SAXException
    {
        // assert that the custom field mappings are not ignored and properly mapped (JRA-12124)
        assertEquals("userNewCF", fieldMappingsPage.getDisplayedFieldMapping("userNewCF"));
        assertEquals("Version Custom Field", fieldMappingsPage.getDisplayedFieldMapping("versionExistingCF"));
        List<Pair<String, String>> unmappedFields = fieldMappingsPage.getUnmappedFields();
        assertEquals(4, unmappedFields.size());
        assertEquals("redundant0", unmappedFields.get(0).first());
        assertEquals("REDUNDANT0", unmappedFields.get(0).second());
        assertEquals("redundant1", unmappedFields.get(1).first());
        assertEquals("summary", unmappedFields.get(1).second());
        assertEquals("redundant2", unmappedFields.get(2).first());
        assertEquals("REDUNDANT2", unmappedFields.get(2).second());
        assertEquals("html", unmappedFields.get(3).first());
        assertEquals("<input id=\"wrong\"/>", unmappedFields.get(3).second());
    }

}
