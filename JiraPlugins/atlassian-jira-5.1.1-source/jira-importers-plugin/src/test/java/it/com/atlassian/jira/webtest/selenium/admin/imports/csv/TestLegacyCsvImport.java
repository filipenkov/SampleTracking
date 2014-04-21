/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package it.com.atlassian.jira.webtest.selenium.admin.imports.csv;

import com.atlassian.jira.functest.framework.admin.TimeTracking;
import com.atlassian.jira.functest.framework.labels.Labels;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.plugins.importer.external.CustomFieldConstants;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvFieldMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvValueMappingsPage;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.NullProgressMonitor;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.LoginPage;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import junitx.framework.ListAssert;
import org.openqa.selenium.WebElement;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests CSV import wizard.
 */
@SuppressWarnings("deprecation")
public class TestLegacyCsvImport extends JIRAWebTest {
    private static final String TRIVIA_REPORTER = "Chai";
    private static final String TRIVIA_KEY = "TRV";
    private static final String TRIVIA_NAME = "Trivia";
    private static final String TRIVIA_TYPE = "Improvement";
    private static final String TRIVIA_SUMMARY = "CSV Import Func Test";
    private static final String TRIVIA_DESCRIPTION = "Write jwebunit functional test for the csv importer";
    private static final String KEY_MOZILLA = "MOZ";
    private static final String LABELS_CF_ID = "customfield_10010";
    private JiraTestedProduct product;
	private JiraRestClient restClient;

	public TestLegacyCsvImport(final String name)
    {
        super(name);
    }

    public void tearDown()
    {
        removeGlobalPermission(BULK_CHANGE, Groups.USERS);
        super.tearDown();
    }

    public void setUp()
    {
        super.setUp();

        restoreData("TestCsvImport.xml");

        product = TestedProductFactory.create(JiraTestedProduct.class);
		restClient = ITUtils.createRestClient(environmentData);

    }

    /**
     * A one-record import with 3 fields.
     */
    public void testTrivial()
    {
        importFile("trivial.csv", "trivial.properties");
        //check the import worked
        goToProject(TRIVIA_NAME);
        assertTextPresent(TRIVIA_NAME);
        assertElementPresent("project-config-header-avatar");
        assertTextPresent(TRIVIA_KEY);
        displayAllIssues();
        assertTextPresent("all 1 issue(s)");
        assertTextPresentBeforeText("Unassigned", TRIVIA_REPORTER);
        clickLinkWithText(TRIVIA_KEY + "-1");
        assertTextPresent(TRIVIA_SUMMARY);
        assertTextPresent(TRIVIA_DESCRIPTION);
        assertTextPresent(TRIVIA_TYPE);
    }

    /**
     * A one-record import with the user's name as an email address
     */
    public void testTrivialWithEmails()
    {
        final String reporter = "chai@funcmaster.com";
        importFile("trivial-with-emails.csv", "trivial.properties");
        //check the import worked
        goToProject(TRIVIA_NAME);
        assertTextPresent(TRIVIA_NAME);
        assertElementPresent("project-config-header-avatar");
        assertTextPresent(TRIVIA_KEY);
        displayAllIssues();
        assertTextPresent("all 1 issue(s)");
        assertTextPresentBeforeText("Unassigned", reporter);
        clickLinkWithText(TRIVIA_KEY + "-1");
        assertTextPresent(TRIVIA_SUMMARY);
        assertTextPresent(TRIVIA_DESCRIPTION);
        assertTextPresent(TRIVIA_TYPE);
        assertTextNotPresent(reporter + "@atlassian.com");
    }


    /**
     * A one-record import with the user's name as an email address
     */
    public void testTrivialWithEmailsNoSuffix()
    {
        final String reporter = "chai@funcmaster.com";
        importFile("trivial-with-emails.csv", "trivial-no-email-suffix.properties");
        //check the import worked
        goToProject(TRIVIA_NAME);
        assertTextPresent(TRIVIA_NAME);
        assertElementPresent("project-config-header-avatar");
        assertTextPresent(TRIVIA_KEY);
        displayAllIssues();
        assertTextPresent("all 1 issue(s)");
        assertTextPresentBeforeText("Unassigned", reporter);
        clickLinkWithText(TRIVIA_KEY + "-1");
        assertTextPresent(TRIVIA_SUMMARY);
        assertTextPresent(TRIVIA_DESCRIPTION);
        assertTextPresent(TRIVIA_TYPE);
        assertTextNotPresent(reporter + "@example.com");
    }

    /**
     * A one-record import with timetracking fields.
     */
    public void testTrivialWithTimeTracking()
    {
        importFile("trivial-with-timetracking.csv", "trivial-with-timetracking.properties");
        //check the import worked
        goToProject(TRIVIA_NAME);
        assertTextPresent(TRIVIA_NAME);
        assertElementPresent("project-config-header-avatar");
        assertTextPresent(TRIVIA_KEY);
        displayAllIssues();
        assertTextPresentBeforeText("Unassigned", TRIVIA_REPORTER);
        clickLinkWithText(TRIVIA_KEY + "-1");
        assertTextPresent(TRIVIA_SUMMARY);
        assertTextPresent(TRIVIA_DESCRIPTION);
        assertTextPresent(TRIVIA_TYPE);
        assertTextPresent("10 minutes");
        assertTextPresent("5 minutes");
        assertTextPresent("2 minutes");
    }


    /**
     * A one-record import with 3 fields and a comment.
     */
    public void testTrivialWithComments()
    {
        importFile("trivial-with-comments.csv", "trivial-with-comments.properties");
        //check the import worked
        goToProject(TRIVIA_NAME);
        assertTextPresent(TRIVIA_NAME);
        assertElementPresent("project-config-header-avatar");
        assertTextPresent(TRIVIA_KEY);
        displayAllIssues();
        assertTextPresent("all 1 issue(s)");
        assertTextPresentBeforeText("Unassigned", TRIVIA_REPORTER);
        clickLinkWithText(TRIVIA_KEY + "-1");
        assertTextPresent(TRIVIA_SUMMARY);
        assertTextPresent(TRIVIA_DESCRIPTION);
        assertTextPresent(TRIVIA_TYPE);

        // check the created and updated dates are as there are in the csv
        assertTextSequence(new String[] { "Created", "12/Nov/02 9:00 AM", "Updated", "10/Oct/03 5:44 PM" });

        //The order of the comments really doesn't matter, just that they exist with the correct updated date.
        assertCollapsedTextSequence(new String[]{
                ADMIN_FULLNAME, "10/Oct/03 5:44 PM", "Another comment that should not affect the issue updated date",
        });
        assertCollapsedTextSequence(new String[]{
                ADMIN_FULLNAME, "10/Oct/03 5:44 PM", "A comment that should not affect the issue updated date"
        });
    }

    /**
     * A one-record import with timetracking fields.
     */
    public void testTrivialWithTimeTrackingSpecifyingEstimateConverter()
    {
        importFile("trivial-with-timetracking.csv", "trivial-with-timetracking-estimate-converter-specified.properties");
        //check the import worked
        goToProject(TRIVIA_NAME);
        assertTextPresent(TRIVIA_NAME);
        assertElementPresent("project-config-header-avatar");
        assertTextPresent(TRIVIA_KEY);
        displayAllIssues();
        assertTextPresentBeforeText("Unassigned", TRIVIA_REPORTER);
        clickLinkWithText(TRIVIA_KEY + "-1");
        assertTextPresent(TRIVIA_SUMMARY);
        assertTextPresent(TRIVIA_DESCRIPTION);
        assertTextPresent(TRIVIA_TYPE);
        assertTextPresent("10 minutes");
        assertTextPresent("5 minutes");
        assertTextPresent("2 minutes");
    }

    /**
     * A four record import with labels to the system field and a custom field
     */
    public void testTrivialWithLabels()
    {
        importFile("trivial-with-labels.csv", "trivial-with-labels.properties");
        //check the import worked
        goToProject(TRIVIA_NAME);
        assertTextPresent(TRIVIA_NAME);
        assertElementPresent("project-config-header-avatar");
        assertTextPresent(TRIVIA_KEY);


        Set<String> labelsPresent = new HashSet<String>();
        labelsPresent.add("ComPlex");
        _assertLabelsForIssue("1", labelsPresent);

        labelsPresent.clear();
        labelsPresent.add("ComPlex");
        labelsPresent.add("a");
        labelsPresent.add("b");
        _assertLabelsForIssue("2",labelsPresent);

        labelsPresent.clear();
        labelsPresent.add("abc");
        _assertLabelsForIssue("3",labelsPresent);

        labelsPresent.clear();
        labelsPresent.add("a");
        labelsPresent.add("de");
        labelsPresent.add("b!c");
        _assertLabelsForIssue("4", labelsPresent);
    }

    private void _assertLabelsForIssue(String issueNumber, Set<String> labelsPresent)
    {
        navigation.issueNavigator().displayAllIssues();
        clickLinkWithText(TRIVIA_KEY + "-" + issueNumber);
        assertTextInElement("assignee-val", "Unassigned");
        assertTextInElement("reporter-val", TRIVIA_REPORTER.toLowerCase());
        assertTextPresent(TRIVIA_SUMMARY);
        assertTextPresent(TRIVIA_DESCRIPTION);
        assertTextPresent(TRIVIA_TYPE);

        final String id = String.valueOf(parse.issue().parseViewIssuePage().getId());
        final Labels expectedLabels = new Labels(true, true, true, labelsPresent);
        assertions.getLabelAssertions().assertSystemLabels(id, expectedLabels);
        assertions.getLabelAssertions().assertLabels(id, LABELS_CF_ID, expectedLabels);
    }

    /**
     * Tests to see that if timetracking is disabled that the wizard does not offer up the fields to map to.
     */
    public void testImportWizardWithTimetrackingEnabled()
    {
        administration.timeTracking().enable(TimeTracking.Format.PRETTY);

        CsvSetupPage setupPage = product.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial-with-timetracking.csv");
        CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
                .setReadFromCsv(false).setExistingProject("MKY").next();

        Collection<String> options = Collections2.transform(fieldMappingsPage.getSelectOptions(
				fieldMappingsPage.getTargetFieldName("OriginalEstimate")),
                new Function<WebElement, String>() {
                    @Override
                    public String apply(@Nullable WebElement webElement) {
                        return webElement != null ? webElement.getAttribute("value") : "";
                    }
                });

        // Make sure the time tracking options are there
        assertTrue(Iterables.contains(options, "timeoriginalestimate"));
        assertTrue(Iterables.contains(options, "timeestimate")); // remaining estimate
        assertTrue(Iterables.contains(options, "timespent"));
        assertTrue(Iterables.contains(options, "summary"));
    }

    public void testImportTimeTracking()
    {
        CsvSetupPage setupPage = product.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial-with-timetracking-2.csv");
        CsvFieldMappingsPage fieldMappingsPage = setupPage.next()
                .setReadFromCsv(false)
			   	.setUserEmailSuffix("@atlassian.com")
                .setExistingProject("HSP")
                .next();
        fieldMappingsPage.setFieldMapping("Title", "summary")
                .setFieldMapping("Description", "description")
                .setFieldMapping("Reporter", "reporter")
                .setFieldMapping("OriginalEstimate", "Original Estimate")
                .setFieldMapping("RemainingEstimate", "Remaining Estimate")
                .setFieldMapping("TimeSpent", "Time Spent");
        assertTrue(fieldMappingsPage.next().next().waitUntilFinished().isSuccess());

        gotoIssue("HSP-1");
        assertTextSequence(new String[] { "Original Estimate", "10 minutes", "Remaining Estimate", "5 minutes", "Time Spent", "2 minutes" });

        gotoIssue("HSP-2");
        assertTextSequence(new String[] { "Original Estimate", "9 minutes", "Remaining Estimate", "9 minutes", "Time Spent", "Not Specified" });

        gotoIssue("HSP-3");
        assertTextSequence(new String[]{"Original Estimate", "8 minutes", "Remaining Estimate", "7 minutes", "Time Spent", "Not Specified"});
    }

    /**
     * Do the import part for a given csv file and config file with a time limit for the import process. Don't make the
     * time limit too small!
     *
     * @param csvFile the csv file in the XML dir to be imported
     * @param configFile the config file to use for import mappings
     */
    private void importFile(final String csvFile, @Nullable final String configFile)
    {
        CsvSetupPage setupPage = product.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/" + csvFile);
        if (configFile != null) {
            setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/" + configFile);
        }

        assertTrue(setupPage.next().next().next().next().waitUntilFinished().isSuccess());
    }

    public void testInvalidFieldNameChars()
    {
        CsvSetupPage setupPage = product.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/invalidfieldnames.csv");
        setupPage.next();
        List<String> errors = setupPage.getGlobalErrors();

        List<String> expected = Lists.newArrayList(
            "The first header row of the CSV import file contains following errors for the field name 'open(date' on position 4: 'brackets mismatch'",
            "The first header row of the CSV import file contains following errors for the field name 'bug+id' on position 5: 'plus sign'",
            "The first header row of the CSV import file contains following errors for the field name '(reporter+real[name' on position 7: 'brackets mismatch', 'plus sign'",
            "The first header row of the CSV import file contains following errors for the field name '[key[words]' on position 10: 'brackets mismatch'",
            "The first header row of the CSV import file contains following errors for the field name '(short)(desc' on position 16: 'brackets mismatch'",
            "The first header row of the CSV import file contains following errors for the field name 'bug[severity' on position 17: 'brackets mismatch'");

        for(String e : expected) {
            ListAssert.assertContains(errors, e);
        }
    }

    /**
     * Test that the comment mapper can be changed by changing 'settings.advanced.mapper.comment' in the configuration
     * file. This test will change the {@link com.atlassian.jira.plugins.importer.imports.csv.mappers.ExternalCommentMapper} to
     * {@link com.atlassian.jira.plugins.importer.imports.csv.mappers.PvcsComment}
     */
    public void testImportCustomCommentMapper()
    {
        importFile("comment_mapper.csv", "comment_mapper.properties");

        displayAllIssues();
        clickLinkWithText("Test Issue 1");//summary
        assertTextPresent("Has comment by existing user");//description

        assertLinkPresentWithText(FRED_FULLNAME);//comment author
        text.assertTextPresent(locator.css("span.date"), "10/Jan/07 5:07 AM");//comment created
        assertTextPresent("comment:fred:01/10/07 05:07:33 AM:This comment is by fred (existing user)");//comment body

        displayAllIssues();
        clickLinkWithText("Test Issue 2");//summary
        assertTextPresent("Has comment by non-existing user");//description
        assertLinkPresentWithText(ADMIN_FULLNAME);//comment author
        text.assertTextPresent(locator.css("span.date"), "10/Jan/07 2:08 PM");//comment created
        assertTextPresent("comment:sam:01/10/07 02:08:33 PM:This comment is by sam (non-existing user)");//comment body
    }


    /**
     * A medium sized, medium complexity csv import.
     */
    public void testMedium()
    {
        importFile("medium.csv", "medium.properties");

        gotoPage("/browse/GDL");
        // check a sequence of key data on the project summary page
        assertTextSequence(new String[] {
                "Grendel",
                "import from bugzilla via csv",
                "Lead:", ADMIN_FULLNAME,
                "Key:", "GDL" });
        clickLink("issues-panel-panel");
        assertTextSequence(new String[]{
                "Unresolved: By Priority",
                "No priority", "8", "30%",
                "Blocker", "1", "4%",
                "Critical", "1", "4%",
                "Major", "3", "11%",
                "Minor", "13", "48%",
                "Trivial", "1", "4%",
                "Unresolved: By Assignee",
                "Kieran Maclean", "7", "26%",
                "R.J. Keller", "20", "74%",
                "Unresolved: By Version",
                "27", "Unscheduled",
                "Status Summary",
                "Open", "38", "46%",
                "Reopened", "4", "5%",
                "Resolved", "41", "49%",
                "Unresolved: By Component",
                "3", "Preferences",
                "6", "Protocols",
                "18", "User Interface"
        });
        gotoAdmin();
        clickLink("user_browser");
        // check a couple of users
        assertTextSequence(new String[]{
                "1", "to", "of", "25",
                "abhinav", "Abhinav", "abhinav@atlassian.com",
                "adamfelson", "Adam Felson", "afelson@atlassian.com",
        });
        gotoAdmin();
        clickLink("view_custom_fields");
        assertTextSequence(new String[]{
                "bug_id", "Text Field",
        });
        assertTextSequence(new String[] {
                "keywords", "Multi Select",
        });
        assertTextSequence(new String[] {
                "priority", "Select List"
        });
    }


    /**
     * Runs through the configuration wizard setting mappings, custom fields etc.
     */
    public void testConfigWizard() {
		CsvSetupPage setupPage = product.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

		CsvFieldMappingsPage fieldMappingsPage = setupPage
				.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/mozilla.csv")
				.setDelimiter("").next().setReadFromCsv(false)
				.createProject("Mozilla", "MOZ")
				.setUserEmailSuffix("@atlassian.com").setDateImportFormat("yyyy-mm-dd hh:mm:ss").next();

		fieldMappingsPage.newCustomField("bug_id").setFieldName("bugzilla_id").setFieldType(CustomFieldConstants.TEXT_FIELD_TYPE).submit();

		fieldMappingsPage.setFieldMappingByVal("opendate", IssueFieldConstants.CREATED);
        fieldMappingsPage.setFieldMappingByVal("bug_severity", IssueFieldConstants.PRIORITY);
		fieldMappingsPage.newCustomField("priority").setFieldName("priority").setFieldType(CustomFieldConstants.SELECT_FIELD_TYPE).submit();
        fieldMappingsPage.setFieldMappingByVal("rep_platform", IssueFieldConstants.ENVIRONMENT)
                .setFieldMappingByVal("assigned_to_realname", IssueFieldConstants.ASSIGNEE)
                .setFieldMappingByVal("reporter_realname", IssueFieldConstants.REPORTER)
		        .setFieldMappingByVal("bug_status", IssueFieldConstants.STATUS);
        fieldMappingsPage.setMapValues("bug_status", true);
        fieldMappingsPage.setMapValues("bug_severity", true);
        fieldMappingsPage.setFieldMappingByVal("resolution", IssueFieldConstants.RESOLUTION);
        fieldMappingsPage.setMapValues("resolution", true);
        fieldMappingsPage.newCustomField("product").setFieldName("product").setFieldType(CustomFieldConstants.TEXT_FIELD_TYPE).submit();
        fieldMappingsPage.setFieldMappingByVal("component", IssueFieldConstants.COMPONENTS);
        fieldMappingsPage.setFieldMappingByVal("version", IssueFieldConstants.AFFECTED_VERSIONS);
        fieldMappingsPage.newCustomField("target_milestone").setFieldName("target_milestone").setFieldType(CustomFieldConstants.SELECT_FIELD_TYPE).submit();
        fieldMappingsPage.newCustomField("status_whiteboard").setFieldName("status_whiteboard").setFieldType(CustomFieldConstants.TEXT_FIELD_TYPE).submit();
        fieldMappingsPage.setFieldMappingByVal("keywords", IssueFieldConstants.LABELS);
        fieldMappingsPage.setFieldMappingByVal("short_desc", IssueFieldConstants.SUMMARY);

		CsvValueMappingsPage valueMappingsPage = fieldMappingsPage.next();
		valueMappingsPage.setSelect("value.0", "Blocker");
        valueMappingsPage.setSelect("value.1", "Trivial");
        valueMappingsPage.setSelect("value.2", "Major");
        valueMappingsPage.setSelect("value.3", "Minor");
        valueMappingsPage.setSelect("value.4", "Minor");
        valueMappingsPage.setSelect("value.3", "Major");
        valueMappingsPage.setSelect("value.2", "Critical");
        valueMappingsPage.setSelect("value.5", "Open");
        valueMappingsPage.setSelect("value.6", "Open");
        valueMappingsPage.setSelect("value.5", "In Progress");
        valueMappingsPage.setSelect("value.7", "Reopened");

		assertTrue(valueMappingsPage.next().waitUntilFinished().isSuccess());

        // project page detailed summary data
        gotoProjectBrowse(KEY_MOZILLA);
        assertTextSequence(new String[] {
                "Mozilla",
                "Lead:", ADMIN_FULLNAME,
                "Key:", KEY_MOZILLA });

        clickLink("issues-panel-panel");
        assertTextSequence(new String[]{
                "Unresolved: By Priority",
                "Blocker", "1", "3%",
                "Critical", "15", "50%",
                "Major", "8", "27%",
                "Minor", "4", "13%",
                "Trivial", "2", "7%",


                "Unresolved: By Assignee",
                "Andrew Schultz", "1", "3%",
                "Christopher Blizzard (not doing reviews)", "5", "17%",
                "David Baron", "1", "3%",
                "Doron Rosenberg (IBM)", "1", "3%",
                "Emil Hesslow", "1", "3%",
                "jag (Peter Annema)", "1", "3%",
                "Jean-Francois Ducarroz", "1", "3%",
                "Jungshik Shin", "1", "3%",
                "Mike Shaver", "1", "3%",
                "Mostafa Hosseini", "1", "3%",
				"and 10 more",

                "Unresolved: By Version",
                "30", "Unscheduled",

                "Status Summary",
                "Open", "2", "7%",
                "In Progress", "24", "80%",
                "Reopened", "4", "13%",

                "Unresolved: By Component",
                "1", "Base",
                "2", "Build Config",
                "1", "DOM: Level 0",
                "1", "File Handling",
                "1", "General",
                "1", "GFX",
                "1", "GFX: Gtk",
                "1", "History: Global",
                "1", "Internationalization",
                "1", "Layout: Fonts and Text",
                "and 15 more"
        });
        gotoIssue(KEY_MOZILLA + "-12");
        assertTextPresent("context menu is not context sensitive");
        assertTextPresentBeforeText("bugzilla_id:", "282292");
        assertTextPresentBeforeText("Reporter:", "Jamie Zawinski");
    }

    /**
     * Checks that the existence of the Unicode Byte Order Marker (BOM) in a CSV file does not confuse the import
     * process.
     */
    public void testUtf8()
    {
        importFile("simple_utf-8.csv", "simple_utf-8.properties");
    }

    /**
     * Check that importing a CSV file works with <strong>datetime values</strong> (including time) mapped to an
     * existing <strong>datetime</strong> custom field. @see JRA-8825
     */
    public void testExistingCustomDateTimeField()
    {
        addCustomField(CUSTOM_FIELD_TYPE_DATETIME, "custom-datetime");
        importFile("medium.csv", "existingCustomDateTime.properties");
    }

    /**
     * Testing the import of a Version custom field JRA-9889
     */
    public void testVersionCustomFieldImport()
    {
        importFile("versioncfimport.csv", "versioncfimport.properties");
        displayAllIssues();
        assertTextPresent("all 1 issue(s)");
        clickLinkWithText(PROJECT_HOMOSAP_KEY + "-1");
        assertLinkPresentWithText("New Version 5");
        assertTextPresent("The description field");
    }

    /**
     * Test that the import wizard recognises a tab character as the delimiter
     */
    public void testImportWizardWithTabDelimiter()
    {
		CsvSetupPage setupPage = product.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

		setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/sample.tsv.txt");
		setupPage.setDelimiter("\\t");
		CsvFieldMappingsPage fieldMappingsPage = setupPage.next().setReadFromCsv(false).setExistingProject("homosapien")
				.setUserEmailSuffix("@atlassian.com")
				.next();

		// Select Fields to import

		fieldMappingsPage.setFieldMappingByVal("Issue type", IssueFieldConstants.ISSUE_TYPE);
		fieldMappingsPage.setMapValues("Issue type", true);

		fieldMappingsPage.setFieldMappingByVal("Assigned to", IssueFieldConstants.ASSIGNEE);
		fieldMappingsPage.setFieldMappingByVal("Priority", IssueFieldConstants.PRIORITY);
		fieldMappingsPage.setMapValues("Priority", true);
		fieldMappingsPage.setFieldMappingByVal("Resolution", IssueFieldConstants.RESOLUTION);
		fieldMappingsPage.setMapValues("Resolution", true);
		fieldMappingsPage.setFieldMappingByVal("Status", IssueFieldConstants.STATUS);
		fieldMappingsPage.setMapValues("Status", true);
		fieldMappingsPage.setFieldMappingByVal("Summary", IssueFieldConstants.SUMMARY);
		CsvValueMappingsPage valueMappingsPage = fieldMappingsPage.next();

		assertEquals("1", valueMappingsPage.getValue("value.2")); // mapping for Resolution.Fixed
		assertEquals("6", valueMappingsPage.getValue("value.3")); // mapping for Status.Closed
		assertEquals("6", valueMappingsPage.getValue("value.4")); // mapping for Status.New is set to first one

        assertTrue(valueMappingsPage.next().waitUntilFinished().isSuccess());

        displayAllIssues();
        assertTextPresent("TorqueSQLExec ignores lines starting with");
    }


//	@todo: restore it maybe?
//
//    public void testImportIntoNewProjectFieldValidation()
//    {
//		CsvSetupPage setupPage = product.visit(LoginPage.class)
//            .loginAsSysAdmin(CsvSetupPage.class)
//            .webSudo();
//
//		CsvProjectMappingsPage projectMappingsPage = setupPage
//				.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/mozilla.csv")
//				.setDelimiter("").next();
//
//		projectMappingsPage.setReadFromCsv(false);
//
//		projectMappingsPage.setExistingProject("").setProjectKey("").setProjectLead("").setProjectDescription(
//				"").setProjectUrl("");
//
//		assertFalse(projectMappingsPage.isNextEnabled());
//
//        //Duplicate fields
//
//		projectMappingsPage.setExistingProject("homosapien").setProjectKey("HSP")
//				.setProjectLead(ADMIN_USERNAME).setProjectDescription("").setProjectUrl("").next();
//
//		List<String> errors = projectMappingsPage.getFieldErrors();
//
//        ListAssert.assertContains(errors, "A project with that name already exists.");
//        ListAssert.assertContains(errors, "A project with that project key already exists.");
//
//        //Invalid fields
//
//		projectMappingsPage.setExistingProject("test project").setProjectKey("!")
//				.setProjectLead("not a real user").setProjectDescription("").setProjectUrl("NOT A VALID URL").next();
//
//		errors = projectMappingsPage.getFieldErrors();
//
//        ListAssert.assertContains(errors, "You must specify a unique project key, at least 2 characters long, containing only uppercase letters.");
//        ListAssert.assertContains(errors, "The user you have specified as project lead does not exist.");
//        ListAssert.assertContains(errors, "The URL specified is not valid - it must start with http://");
//
//        //Valid fields
//
//		projectMappingsPage.setExistingProject("test project").setProjectKey("TPT")
//				.setProjectLead(ADMIN_USERNAME).setProjectDescription("Test description")
//				.setProjectUrl("http://testproject.example.com").next();
//
//		assertTrue(projectMappingsPage.getFieldErrors().isEmpty());
//    }

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-439
	 */
    public void testTrivialWithPersonalLicenseNoUnknownUsers()
    {
        //lets import data with one too many users!
        restoreData("TestPersonalLicenseGlobalPermissions.xml");
        switchToPersonalLicense();

		CsvSetupPage setupPage = product.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial-no-unknown-users.csv");
        setupPage.setConfigurationFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/csv/trivial.properties");

		ImporterFinishedPage logs = setupPage.next().next().next().next().waitUntilFinished();
		assertTrue(logs.isSuccess());
		assertEquals("1 users associated with import. 0 will be imported as active due to license limits. Check log for details.",
				Iterables.getFirst(logs.getWarnings(), null));
		assertTrue(administration.usersAndGroups().userExists("chai"));

		JiraRestClient restClient = ITUtils.createRestClient(environmentData);
		Issue issue = restClient.getIssueClient().getIssue("TRV-1", new NullProgressMonitor());
		assertEquals("chai", issue.getReporter().getName());
    }

    public void testImportResolutionDate()
    {
        importFile("mozilla_resolution_date.csv", "mozilla_resolution_date.properties");

        //this issue is not resolved, but had a resolution date in the csv file (incorrectly).  Shouldn't show a resolution date
        gotoIssue("HSP-5");
        assertTextPresent("No resolution with resolution date");
        assertTextNotPresent("Resolved");

        //this issue is resolved but doesn't have a resolution date specified.  Should fall back to updated date.
        gotoIssue("HSP-4");
        assertions.getTextAssertions().assertTextPresentHtmlEncoded("Webcam doesn't work");
        assertTextSequence(new String[] {"Created","07/Sep/04 8:21 AM", "Updated", "07/Oct/04 11:45 AM", "Resolved", "07/Oct/04 11:45 AM"});

        //this issue is resolved and has a resolution date set.
        gotoIssue("HSP-3");
        assertTextPresent("Duplicate entries appear in feeds");
        assertTextSequence(new String[] {"Created", "08/Sep/04 9:21 AM", "Resolved", "10/Dec/04 10:22 AM"});

        // This issue had the resolution date slightly wrong in the csv file.  Issue should have been imported, and the resolution date should have
        //fallen back to the last updated date.
        gotoIssue("HSP-2");
        assertTextPresent("debugging and about: options not implemented !");
        assertTextSequence(new String[] {"Created", "30/Apr/00 11:16 PM", "Updated", "05/Jun/01 11:12 PM", "Resolved", "05/Jun/01 11:12 PM"});

        //An issue that's not resolved and doesn't have a resolution date.
        gotoIssue("HSP-1");
        assertTextPresent("Downloads are stored");
        assertTextNotPresent("Resolved");
    }
}
