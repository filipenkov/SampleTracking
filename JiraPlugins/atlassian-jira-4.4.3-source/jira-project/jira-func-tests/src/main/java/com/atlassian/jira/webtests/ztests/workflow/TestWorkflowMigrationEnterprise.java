package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowMigrationEnterprise extends AbstractTestWorkflowMigration
{
    private static final String TEST_PROJECT_NAME = "Test Project";
    private static final String DESTINATION_WORKFLOW_SCHEME = "Destination Workflow Scheme";
    private static final String REOPENED_STATUS_NAME = "Reopened";

    private static final String WORFKLOW_HOMOSAPIEN_SOURCE_1 = "Homosapien Source 1";
    private static final String WORKFLOW_HOMOSPIEN_DESTINATION = "Homospien Destination";
    private static final String WORKFLOW_HOMOSAPIEN_SOURCE_2 = "Homosapien Source 2";
    private static final String WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE = "Homosapien Custom Issue Type Source";
    private static final String WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION = "Homosapien Custom Issue Type Destination";

    private static final String SUMMARY_FIELD_ID = "Summary";

    private static final String TRANSITION_NAME_GO_CUSTOM = "Go Custom";
    private static final String ACKNOWLEDGE = "Acknowledge";
    private static final String DONE = "Done";


    public TestWorkflowMigrationEnterprise(String name)
    {
        super(name);
    }

    /**
     * Tests simple workflow migration
     */
    public void testWorkflowMigration() throws SAXException
    {
        // Restore clean data to perform a workflow migration
        restoreData("WorkflowMigrationTest.xml");

        Map statusMapping = new HashMap();
        // For Bug Issue Type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_1_1", CUSTOM_STATUS_3);
        // - For Custom Status 2 - select Custom Status 4
        statusMapping.put("mapping_1_10001", CUSTOM_STATUS_4);

        // For Improvement issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_4_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_4_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_4_6", CUSTOM_STATUS_4);

        // For New Feature issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_2_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_2_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_2_6", CUSTOM_STATUS_4);

        // Try to migrate the project again
        associateWorkFlowSchemeToProject(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, statusMapping);

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME);

        // Ensure the issues have been migrated to the workflow properly

        // Ensure the correct workflow actions are available for each issue and that the last change item for each issue is correct.
        // TST-1, Task, should be migrated from Source Workflow 1 to default workflow and stay in Open status
        assertIssuesWorkflowState("TST-1", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("TST-1", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-2, Task, should be migrated from Source Workflow 1 to default workflow and stay in Open status
        assertIssuesWorkflowState("TST-2", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("TST-2", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-3, Improvement, should be migrated from Source Workflow to Destination Workflow, from In Progress status to Resolved Status
        assertIssuesWorkflowState("TST-3", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go 4" }));
        assertLastChangeHistoryRecords("TST-3", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME)
        })));

        // TST-4, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        assertIssuesWorkflowState("TST-4", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-4", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4)
        })));

        // TST-5, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Open to Custom Status 3
        assertIssuesWorkflowState("TST-5", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-5", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3)
        })));

        // TST-6, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        assertIssuesWorkflowState("TST-6", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-6", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4)
        })));

        // TST-7, New Feature, should be migrated from Source Workflow 1 to Destination Workflow, from Closed to Custom Status 4
        assertIssuesWorkflowState("TST-7", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-7", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CLOSED_STATUS_NAME, CUSTOM_STATUS_4)
        })));

        // TST-8, Improvement, should be migrated from Source Workflow 1 to Destination Workflow, from In Progress to Resolved
        assertIssuesWorkflowState("TST-8", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go 4" }));
        assertLastChangeHistoryRecords("TST-8", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME)
        })));

        // TST-9, Task, should be migrated from Source Workflow 1 to default jira workflow, and stay in Closed status
        assertIssuesWorkflowState("TST-9", CLOSED_STATUS_NAME, Arrays.asList(new String[] { "Reopen Issue" }));
        assertLastChangeHistoryRecords("TST-9", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-10, Bug, should be migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        assertIssuesWorkflowState("TST-10", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-10", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3)
        })));

        // TST-11, Bug, should be migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        assertIssuesWorkflowState("TST-11", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-11", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3)
        })));
    }

    /**
     * Tests workflow migration with previously failed migration. <p/> Imports a project which is still associated with
     * its 'old' workflow scheme but issues TST-1, TST-2, TST-5 and TST-10 have been migrated to new workflows in the
     * new workflow scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationHalfMigratedData() throws SAXException
    {
        // Restore data where a workflow migration dies half way
        // So we have a project which is still associated with its 'old' workflow scheme
        // But issues TST-1, TST-2, TST-5 and TST-10 have been migrated to new workflows in the new workflow scheme
        restoreData("WorkflowMigrationTestBrokenHalfWay.xml");

        Map statusMapping = new HashMap();
        // For Bug Issue Type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_1_1", CUSTOM_STATUS_3);
        // - For Custom Status 2 - select Custom Status 4
        statusMapping.put("mapping_1_10001", CUSTOM_STATUS_4);

        // For Improvement issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_4_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_4_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_4_6", CUSTOM_STATUS_4);

        // For New Feature issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_2_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_2_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_2_6", CUSTOM_STATUS_4);

        // Try to migrate the project again
        associateWorkFlowSchemeToProject(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, statusMapping);

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME);

        // Ensure the issues have been migrated to the workflow properly
        assertIssuesMigratedAndChangeHistory();
    }

    /**
     * Tests workflow migration with previously failed migtaion. <p/> Restores data where a workflow migration migrates
     * issues TST-1, TST-2, TST-5 and TST-10 to new workflow then for TST-4 creates an new wf entry, marks the existing
     * one as 'killed' and then dies. So we have a project which is still associated with its 'old' workflow scheme But
     * some issues have been migrated to new workflow in the new workflow scheme </p> <p/> This test migrates the issues
     * again. </p>
     */
    public void testWorkflowMigrationWithKilledWFEntry() throws SAXException
    {
        // Restore data where a workflow migration migrates issues TST-1, TST-2, TST-5 and TST-10 to new workflow
        // then for TST-4 creates an new wf entry, marks the existing one as 'killed' and then dies.
        // So we have a project which is still associated with its 'old' workflow scheme
        // But some issues have been migrated to new workflow in the new workflow scheme
        restoreData("WorkflowMigrationTestBrokenHalfWay.xml");

        Map statusMapping = new HashMap();
        // For Bug Issue Type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_1_1", CUSTOM_STATUS_3);
        // - For Custom Status 2 - select Custom Status 4
        statusMapping.put("mapping_1_10001", CUSTOM_STATUS_4);

        // For Improvement issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_4_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_4_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_4_6", CUSTOM_STATUS_4);

        // For New Feature issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_2_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_2_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_2_6", CUSTOM_STATUS_4);

        // Try to migrate the project again
        associateWorkFlowSchemeToProject(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, statusMapping);

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME);

        // Ensure the issues ahave been migarted to the workflow properly
        assertIssuesMigratedAndChangeHistory();
    }

    /**
     * Tests workflow migration with previously failed migtaion. <p/> Restore data where a workflow migration migrates
     * issues TST-1, TST-2, TST-5 and TST-10 then creates a new wf entry for TST-4, marks an existing wf entry as
     * killed, creates a new current step, removes the old one, and then dies. So we have a project which is still
     * associated with its 'old' workflow scheme But some issues have been migrated to workflows in the new workflow
     * scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationWithUnupdatedIssue() throws SAXException
    {
        // Restore data where a workflow migration migrates issues TST-1, TST-2, TST-5 and TST-10 then
        // creates a new wf entry for TST-4, marks an existing wf entry as killed, creates a new current step, removes the
        // old one, and then dies.
        // So we have a project which is still associated with its 'old' workflow scheme
        // But some issues have been migrated to workflows in the new workflow scheme
        restoreData("WorkflowMigrationTestIssueWithUnupdatedIssue.xml");

        Map statusMapping = new HashMap();
        // For Bug Issue Type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_1_1", CUSTOM_STATUS_3);
        // - For Custom Status 2 - select Custom Status 4
        statusMapping.put("mapping_1_10001", CUSTOM_STATUS_4);

        // For Improvement issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_4_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_4_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_4_6", CUSTOM_STATUS_4);

        // For New Feature issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_2_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_2_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_2_6", CUSTOM_STATUS_4);

        // Try to migrate the project again
        associateWorkFlowSchemeToProject(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, statusMapping);

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME);

        // Ensure the issues ahave been migarted to the workflow properly
        assertIssuesMigratedAndChangeHistory();
    }

    private void assertIssuesMigratedAndChangeHistory() throws SAXException
    {
        // Ensure the correct workflow actions are available for each issue and that the last change item for each issue is correct.

        // TST-1, Task, has already been migrated from Source Workflow 1 to the default workflow. The issue should be migrated again and stay in Open status
        assertIssuesWorkflowState("TST-1", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("TST-1", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-2, Task, has already been migrated from Source Workflow 1 to the default workflow. The issue should be migrated again and stay in Open status
        assertIssuesWorkflowState("TST-2", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("TST-2", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-3, Improvement, should be migrated from Source Workflow to Destination Workflow, from In Progress status to Resolved Status
        assertIssuesWorkflowState("TST-3", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go 4" }));
        assertLastChangeHistoryRecords("TST-3", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME)
        })));

        // TST-4, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        assertIssuesWorkflowState("TST-4", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-4", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4)
        })));

        // TST-5, Bug, has already been migrated from from Source Workflow 2 to Destination Workflow, from Open to Custom Status 3
        // The issue should be migrated again and stay in Custom Status 3
        assertIssuesWorkflowState("TST-5", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-5", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow")));

        // TST-6, Bug, should be migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        assertIssuesWorkflowState("TST-6", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-6", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4)
        })));

        // TST-7, New Feature, should be migrated from Source Workflow 1 to Destination Workflow, from Closed to Custom Status 4
        assertIssuesWorkflowState("TST-7", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-7", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CLOSED_STATUS_NAME, CUSTOM_STATUS_4)
        })));

        // TST-8, Improvement, should be migrated from Source Workflow 1 to Destination Workflow, from In Progress to Resolved
        assertIssuesWorkflowState("TST-8", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go 4" }));
        assertLastChangeHistoryRecords("TST-8", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME)
        })));

        // TST-9, Task, should be migrated from Source Workflow 1 to default jira workflow, and stay in Closed status
        assertIssuesWorkflowState("TST-9", CLOSED_STATUS_NAME, Arrays.asList(new String[] { "Reopen Issue" }));
        assertLastChangeHistoryRecords("TST-9", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-10, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        assertIssuesWorkflowState("TST-10", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-10", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow")));

        // TST-11, Bug, should be migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        assertIssuesWorkflowState("TST-11", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-11", new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow"),
                new ExpectedChangeHistoryItem(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3)
        })));
    }

    /**
     * Tests workflow migration with previously failed migration. <p/> Restores data where a workflow migration dies
     * after migrating all issues but still leaving behind the association to the old workflow scheme So we have a
     * project which is still associated with its 'old' workflow scheme But all issues have been migrated to workflows
     * in the new workflow scheme </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationWithUnupdatedWorkflowScheme() throws SAXException
    {
        // Restore data where a workflow migration dies after migrating all issues but still leaving
        // behind the association to the old workflow scheme
        // So we have a project which is still associated with its 'old' workflow scheme
        // But all issues have been migrated to workflows in the new workflow scheme
        restoreData("WorkflowMigrationTestUnchangedScheme.xml");

        Map statusMapping = new HashMap();
        // For Bug Issue Type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_1_1", CUSTOM_STATUS_3);
        // - For Custom Status 2 - select Custom Status 4
        statusMapping.put("mapping_1_10001", CUSTOM_STATUS_4);

        // For Improvement issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_4_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_4_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_4_6", CUSTOM_STATUS_4);

        // For New Feature issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_2_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_2_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_2_6", CUSTOM_STATUS_4);

        // Try to migrate the project again
        associateWorkFlowSchemeToProject(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, statusMapping);

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME);

        // Ensure the issues have been migrated to the workflow properly

        // Ensure the correct workflow actions are available for each issue and that the last change item for each issue is correct.

        // TST-1, Task, has already been migrated from Source Workflow 1 to the default workflow. The issue should be migrated again and stay in Open status
        assertIssuesWorkflowState("TST-1", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("TST-1", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-2, Task, has already been migrated from Source Workflow 1 to the default workflow. The issue should be migrated again and stay in Open status
        assertIssuesWorkflowState("TST-2", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("TST-2", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-3, Improvement, has already been migrated from Source Workflow to Destination Workflow, from In Progress status to Resolved Status
        // Should be migrated again and stay in Resolved status
        assertIssuesWorkflowState("TST-3", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go 4" }));
        assertLastChangeHistoryRecords("TST-3", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "Destinatiom Workflow")));

        // TST-4, Bug, has alreaddy been migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        assertIssuesWorkflowState("TST-4", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-4", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow")));

        // TST-5, Bug, has already been migrated from from Source Workflow 2 to Destination Workflow, from Open to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        assertIssuesWorkflowState("TST-5", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-5", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow")));

        // TST-6, Bug, has already been migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        assertIssuesWorkflowState("TST-6", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-6", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow")));

        // TST-7, New Feature, has already been migrated from Source Workflow 1 to Destination Workflow, from Closed to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        assertIssuesWorkflowState("TST-7", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-7", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "Destinatiom Workflow")));

        // TST-8, Improvement, has already been migrated from Source Workflow 1 to Destination Workflow, from In Progress to Resolved
        // This issue should be migrated again and stay in Resolved status
        assertIssuesWorkflowState("TST-8", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go 4" }));
        assertLastChangeHistoryRecords("TST-8", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "Destinatiom Workflow")));

        // TST-9, Task, has already been migrated from Source Workflow 1 to default jira workflow, and stay in Closed status
        // This issue should be migrated again and stay in Closed status
        assertIssuesWorkflowState("TST-9", CLOSED_STATUS_NAME, Arrays.asList(new String[] { "Reopen Issue" }));
        assertLastChangeHistoryRecords("TST-9", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-10, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        assertIssuesWorkflowState("TST-10", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-10", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow")));

        // TST-11, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        assertIssuesWorkflowState("TST-11", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-11", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 2", "Destinatiom Workflow")));
    }

    /**
     * Tests workflow migration with previously failed migration. <p/> Restores data where a workflow migration migrates
     * all issues, removes the old workflow scheme association from a project and dies without adding an association
     * with new workflow scheme So we have a project which is not associated with any workflow schemes, and JIRA will
     * think that the project is using the default jira workflow. </p> <p/> This test migrates the issues again. </p>
     */
    public void testWorkflowMigrationWithRemovedWorkflowScheme() throws SAXException
    {
        // Restore data where a workflow migration migrates all issues, removes the old
        // workflow scheme association from a project and dies without adding an association
        // with new workflow scheme
        // So we have a project which is not associated with any workflow schemes, and JIRA will think that the
        // project is using the default jira workflow.
        restoreData("WorkflowMigrationTestRemovedWorkflowScheme.xml");

        // All of the issues have been migrated to new workflows, the association from the old
        // workflow scheme has been removed, but the association to the new workflow scheme has not been added.
        // So JIRA thinks that the project is using the default JIRA workflow. So provide the following status mappings:
        Map statusMapping = new HashMap();
        // For Bug Issue Type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_1_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved status
        statusMapping.put("mapping_1_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_1_6", CUSTOM_STATUS_4);
        // - For Reopened status - select Custom Status 3
        statusMapping.put("mapping_1_4", CUSTOM_STATUS_3);

        // For Improvement issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_4_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved status
        statusMapping.put("mapping_4_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_4_6", CUSTOM_STATUS_4);
        // - For Reopened status - select Custom Status 3
        statusMapping.put("mapping_4_4", CUSTOM_STATUS_3);

        // For New Feature issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_2_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved status
        statusMapping.put("mapping_2_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_2_6", CUSTOM_STATUS_4);
        // - For Reopened status - select Custom Status 3
        statusMapping.put("mapping_2_4", CUSTOM_STATUS_3);

        // Try to migrate the project again
        associateWorkFlowSchemeToProject(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, statusMapping);

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME);

        // Ensure the issues have been migrated to the workflow properly

        // Ensure the correct workflow actions are available for each issue and that the last change item for each issue is correct.

        // TST-1, Task, has already been migrated from Source Workflow 1 to the default workflow.
        // This issue should not be migrated again as all Tasks in the project use the default workflow (due to the previous migration), and as no scheme
        // is associated with the project, the Tasks do not need to be migarted again
        // So assert that the last change item is how it was from the previous workflow migration
        assertIssuesWorkflowState("TST-1", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("TST-1", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-2, Task, has already been migrated from Source Workflow 1 to the default workflow.
        // This issue should not be migrated again as all Tasks in the project use the default workflow (due to the previous migration), and as no scheme
        // is associated with the project, the Tasks do not need to be migarted again
        // So assert that the last change item is how it was from the previous workflow migration
        assertIssuesWorkflowState("TST-2", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("TST-2", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-3, Improvement, has already been migrated from Source Workflow to Destination Workflow, from In Progress status to Resolved Status
        // Should be migrated again and stay in Resolved status.
        // As there is no association with the project the change item will have "jira" as the source workflow
        assertIssuesWorkflowState("TST-3", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go 4" }));
        assertLastChangeHistoryRecords("TST-3", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "jira", "Destinatiom Workflow")));

        // TST-4, Bug, has alreaddy been migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        // As there is no association with the project the change item will have "jira" as the source workflow
        assertIssuesWorkflowState("TST-4", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-4", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "jira", "Destinatiom Workflow")));

        // TST-5, Bug, has already been migrated from from Source Workflow 2 to Destination Workflow, from Open to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        // As there is no association with the project the change item will have "jira" as the source workflow
        assertIssuesWorkflowState("TST-5", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-5", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "jira", "Destinatiom Workflow")));

        // TST-6, Bug, has already been migrated from Source Workflow 2 to Destination Workflow, from Custom Status 2 to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        // As there is no association with the project the change item will have "jira" as the source workflow
        assertIssuesWorkflowState("TST-6", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-6", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "jira", "Destinatiom Workflow")));

        // TST-7, New Feature, has already been migrated from Source Workflow 1 to Destination Workflow, from Closed to Custom Status 4
        // This issue should be migrated again and stay in Custom Status 4
        // As there is no association with the project the change item will have "jira" as the source workflow
        assertIssuesWorkflowState("TST-7", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Go 3" }));
        assertLastChangeHistoryRecords("TST-7", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "jira", "Destinatiom Workflow")));

        // TST-8, Improvement, has already been migrated from Source Workflow 1 to Destination Workflow, from In Progress to Resolved
        // This issue should be migrated again and stay in Resolved status
        // As there is no association with the project the change item will have "jira" as the source workflow
        assertIssuesWorkflowState("TST-8", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go 4" }));
        assertLastChangeHistoryRecords("TST-8", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "jira", "Destinatiom Workflow")));

        // TST-9, Task, has already been migrated from Source Workflow 1 to default jira workflow, and stay in Closed status
        // This issue should not be migrated again as all Tasks in the project use the default workflow (due to the previous migration), and as no scheme
        // is associated with the project, the Tasks do not need to be migarted again
        // So assert that the last change item is how it was from the previous workflow migration
        assertIssuesWorkflowState("TST-9", CLOSED_STATUS_NAME, Arrays.asList(new String[] { "Reopen Issue" }));
        assertLastChangeHistoryRecords("TST-9", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "Source Workflow 1", "jira")));

        // TST-10, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        // As there is no association with the project the change item will have "jira" as the source workflow
        assertIssuesWorkflowState("TST-10", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-10", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "jira", "Destinatiom Workflow")));

        // TST-11, Bug, has already been migrated from Source Workflow 2 to Destination workflow, from Open status to Custom Status 3
        // This issue should be migrated again and stay in Custom Status 3
        // As there is no association with the project the change item will have "jira" as the source workflow
        assertIssuesWorkflowState("TST-11", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("TST-11", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, "jira", "Destinatiom Workflow")));
    }

    /**
     * A general workflow migration test that tests quite a few cases. The test begins with imported data containing
     * issues in Test project and Homosapien project and then migrates issues in Homosapien project from Homosapien
     * Source Scheme workflow scheme to Homosapien Destination Scheme. The Homosapien Source Scheme and Homosapien
     * Destination Scheme use different workflows for different issue types. Some issues in the Homosapien project are
     * also 'broken' due to a previously failed workflow migration.
     * <p/>
     * <p/>
     * The state of issues in the Homospaien project and where they are being migrated is discussed below:
     * <p/>
     * <dl> <dt>Bugs</dt> <dd> Some of the Bugs have been migrated from Homosapien Source 1 workflow to Homospien
     * Destination Failure workflow. The migration failed before completion. When project workflow migration failed it
     * left some issues using Homospien Destination Failure and some issues using the old (Homosapien Source 1)
     * workflow. The data is in the state such that the issues that are using the Homospien Destination Failure workflow
     * are in statuses that do not exist in the Homosapien Source 1 workflow. These statuses are: <ul> <li>In
     * Progress</li> <li>Closed</li> <li>Custom Status 4</li> </ul>
     * <p/>
     * Bugs will be migrated to a completely new workflow - Homospien Destination. That is, not the same workflow to
     * which migration failed (Homospien Destination Failure). <br /><br />
     * <p/>
     * <p/>
     * <dt>Improvements</dt> <dd> Improvements are being migrated from Homosapien Source 2 to the default jira workflow.
     * <br /><br /> </dd>
     * <p/>
     * <dt>New Features</dt> <dd> New Features are already using Homospien Destination workflow, so they should not be
     * migrated at all. <br /><br /> </dd>
     * <p/>
     * <dt>Tasks</dt> <dd> The source workflow for the Task is 'Homospien Destination' so normally we would not need to
     * do any migration (as the destination workflow is also Homospien Destination. However, the previous ly failed
     * migration has moved some of the Tasks to the Homospien Destination Failure workflow. These issues need to be
     * moved (back) to the 'Homospien Destination' workflow. Hence we will need to get mapping for statuses: <ul> <li>In
     * Progress</li> <li>Closed</li> </ul>
     * <p/>
     * which exist in Homospien Destination Failure workflow, but do not exist in Homospien Destination workflow. <br
     * /><br /> </dd>
     * <p/>
     * <dt>Sub-Tasks</dt> <dd> Sub-Tasks are using the Homosapien Source 1 workflow and are being migrated to the
     * Homospien Destination workflow. There are no broken Sub-Tasks. <br /><br /> </dd>
     * <p/>
     * <dt>Custom Issue Type</dt> <dd> Issues of Custom Issue Type are moving between Homosapien Custom Issue Type
     * Source workflow and Homosapien Custom Issue Type Destination. All the statuses in Custom Issue Type Source
     * workflow are in Homosapien Custom Issue Type Destination as well, so no status mapping should appear for Custom
     * Issue Type issues </dd> <dl>
     * <p/>
     * The test also ensures that while migrating issues for the Homosapien project, the issues in Test project were not
     * touched.
     */
    public void testWorkflowMigrationHalfMigratedDataNewDestination() throws SAXException
    {
        // Restore data where some issues are using new workflow
        restoreData("WorkflowMigrationHalfMigratedDataNewDestination.xml");

        gotoPage("/plugins/servlet/project-config/HSP/workflows");
        // Try to associate the project with the new workflow scheme
        clickLink("project-config-workflows-scheme-change");

        assertLinkNotPresentWithText("Add");
        assertTextNotPresent("There are currently no workflow schemes setup.");

        String destinationSchemeName = "Homosapien Destination Scheme";
        selectOption("schemeId", destinationSchemeName);

        submit("Associate");

        assertTextPresent("Step 2 of 3");
        assertTextPresent("Migrate statuses from the old workflow scheme to the new one.");

        // Bug Issue Type
        // Ensure the correct mappings are present
        WebTable statusTable = getDialog().getResponse().getTableWithID("statusmapping_1");
        // Test for the correct mapping of workflows
        assertEquals(WORFKLOW_HOMOSAPIEN_SOURCE_1, statusTable.getCellAsText(0, 0).trim());
        assertEquals(WORKFLOW_HOMOSPIEN_DESTINATION, statusTable.getCellAsText(0, 2).trim());

        // Now go through the table and ensure that we have the correct statuses showing on the mapping screen
        // and that statuses appear in correct order. The statuses should appear sorted by sequence. Remember that
        // each issue type might need a different mapping
        assertStatusMapping(statusTable, IN_PROGRESS_STATUS_NAME, 1, 1, 3, new String[] { STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME }, STATUS_OPEN);
        assertStatusMapping(statusTable, CLOSED_STATUS_NAME, 2, 1, 6, new String[] { STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME }, RESOLVED_STATUS_NAME);
        assertStatusMapping(statusTable, CUSTOM_STATUS_1, 3, 1, 10000, new String[] { STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME }, CUSTOM_STATUS_3);
        assertStatusMapping(statusTable, CUSTOM_STATUS_2, 4, 1, 10001, new String[] { STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME }, CUSTOM_STATUS_4);
        // We have issues that have failed migration to Homospien Destination Failure workflow and have been migrated to
        // Custom Status 4. Custom Status 4 is present in Homospien Destination workflow, so the user should not get asked for the mapping
        assertFormElementNotPresent("mapping_1_10003");

        // Improvement Issue Type
        // This issue type is using Homosapien Source 2 and is being migrated to the default JIRA workflow
        statusTable = getDialog().getResponse().getTableWithID("statusmapping_4");
        assertEquals(WORKFLOW_HOMOSAPIEN_SOURCE_2, statusTable.getCellAsText(0, 0).trim());
        assertEquals("jira", statusTable.getCellAsText(0, 2).trim());

        assertStatusMapping(statusTable, CUSTOM_STATUS_1, 1, 4, 10000, new String[] { STATUS_OPEN, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME, REOPENED_STATUS_NAME, CLOSED_STATUS_NAME }, IN_PROGRESS_STATUS_NAME);
        assertStatusMapping(statusTable, CUSTOM_STATUS_3, 2, 4, 10002, new String[] { STATUS_OPEN, IN_PROGRESS_STATUS_NAME, RESOLVED_STATUS_NAME, REOPENED_STATUS_NAME, CLOSED_STATUS_NAME }, RESOLVED_STATUS_NAME);

        // New Feature Issue Type
        // The issue type is already using the destination workflow so we should not get any mappings for it.
        assertTableNotPresent("statusmapping_2");
        assertTextNotPresent("New Feature");

        // Task Issue Type
        // The source workflow for the task is 'Homospien Destination' so normally we would not need to
        // do any migration. However, the previous migration has moved some of the Tasks to
        // the 'Homospien Destination Failure' workflow. This issues need to be moved (back) to
        // the 'Homospien Destination' workflow. Hence we will need to get mapping for
        // 'In Progress' and 'Closed' stauses which exist in 'Homospien Destination Failure' workflow, but
        // do not exist in 'Homospien Destination' workflow.
        statusTable = getDialog().getResponse().getTableWithID("statusmapping_3");
        assertEquals(WORKFLOW_HOMOSPIEN_DESTINATION, statusTable.getCellAsText(0, 0).trim());
        assertEquals(WORKFLOW_HOMOSPIEN_DESTINATION, statusTable.getCellAsText(0, 2).trim());

        assertStatusMapping(statusTable, IN_PROGRESS_STATUS_NAME, 1, 3, 3, new String[] { STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME }, CUSTOM_STATUS_3);
        assertStatusMapping(statusTable, CLOSED_STATUS_NAME, 2, 3, 6, new String[] { STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME }, RESOLVED_STATUS_NAME);

        // Sub-Tasks
        // Sub Tasks (like Bugs) are using Hamosapien Source 1 and are being migrated to Homosapien Destination workflow
        statusTable = getDialog().getResponse().getTableWithID("statusmapping_5");
        // Test for the correct mapping of workflows
        assertEquals(WORFKLOW_HOMOSAPIEN_SOURCE_1, statusTable.getCellAsText(0, 0).trim());
        assertEquals(WORKFLOW_HOMOSPIEN_DESTINATION, statusTable.getCellAsText(0, 2).trim());

        // Now go through the table and ensure that we have the correct statuses showing on the mapping screen
        // and that statuses appear in correct order. There should be no corrupt sub tasks (i.e. all sub tasks should be on the correct
        // workflow). So we should only be asked for mappings for statuses that exist in Homospien Source 1 workflow and do *not* exist in
        // Homosapien Destination workflow.
        assertStatusMapping(statusTable, CUSTOM_STATUS_1, 1, 5, 10000, new String[] { STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME }, CUSTOM_STATUS_3);
        assertStatusMapping(statusTable, CUSTOM_STATUS_2, 2, 5, 10001, new String[] { STATUS_OPEN, CUSTOM_STATUS_3, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME }, CUSTOM_STATUS_4);

        // Custom Issue Type
        // Issues of Custom Issue Type are moving between Homosapien Custom Issue Type Source workflow and Homosapien Custom Issue Type Destination
        // All the statuses in Custom Issue Type Source workflow (Open, Resolved, Custom Status 3) are in Homosapien Custom Issue Type Destination as well (Open,
        // Custom Status 1, Resolved, Custom Status 3, Closed), so no status mapping should appear on the page for Custom Issue Type issues.
        assertTableNotPresent("statusmapping_6");
        assertTextNotPresent("Custom Issue Type");

        // Associate the new scheme with the project
        submit("Associate");

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration("homosapien", destinationSchemeName);

        /*******************************/
        /* Ensure migration has worked */
        /*******************************/

        // Go to each issue and ensure that:
        // 1. It is on the correct status
        // 2. It has the correct Workflow Transitions available for execution (which we hope means it is actually using the correct workflow)
        // 3. It has the expected change history records

        // HSP-1, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow and left in Open status
        assertIssuesWorkflowState("HSP-1", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-1", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-2, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow and left in Open status
        assertIssuesWorkflowState("HSP-2", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-2", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-3, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and from In Progress status to Open status
        assertIssuesWorkflowState("HSP-3", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-3",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, STATUS_OPEN)
                })));

        // HSP-4, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and from In Progress status to Open status
        assertIssuesWorkflowState("HSP-4", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-4",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, STATUS_OPEN)
                })));

        // HSP-5, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and stay in Custom Status 4
        assertIssuesWorkflowState("HSP-5", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("HSP-5", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-6, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and stay in Custom Status 4
        assertIssuesWorkflowState("HSP-6", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("HSP-6", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-7, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and from Closed status to Resolved Status
        assertIssuesWorkflowState("HSP-7", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Reopen" }));
        assertLastChangeHistoryRecords("HSP-7",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CLOSED_STATUS_NAME, RESOLVED_STATUS_NAME)
                })));

        // HSP-8, Bug, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Source 1
        // This issue should be migrated from  Homospien Destination Failure workflow to Homospien Destination workflow
        // and from Closed status to Resolved Status
        assertIssuesWorkflowState("HSP-8", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Reopen" }));
        assertLastChangeHistoryRecords("HSP-8",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CLOSED_STATUS_NAME, RESOLVED_STATUS_NAME)
                })));

        // HSP-9, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Open status
        assertIssuesWorkflowState("HSP-9", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-9", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-10, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Open status
        assertIssuesWorkflowState("HSP-10", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-10", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-11, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 1 to Custom Status 3
        assertIssuesWorkflowState("HSP-11", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        assertLastChangeHistoryRecords("HSP-11",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3)
                })));

        // HSP-12, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 1 to Custom Status 3
        assertIssuesWorkflowState("HSP-12", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        assertLastChangeHistoryRecords("HSP-12",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3)
                })));

        // HSP-13, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 2 to Custom Status 4
        assertIssuesWorkflowState("HSP-13", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("HSP-13",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4)
                })));

        // HSP-14, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 2 to Custom Status 4
        assertIssuesWorkflowState("HSP-14", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("HSP-14",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4)
                })));

        // HSP-15, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Custom Status 3
        assertIssuesWorkflowState("HSP-15", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        assertLastChangeHistoryRecords("HSP-15", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-16, Bug, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Custom Status 3
        assertIssuesWorkflowState("HSP-16", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        assertLastChangeHistoryRecords("HSP-16", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // Tasks - the issues are moving to the same workflow as their source workflow. However, there are issues here that
        // are on the wrong workflow. We need to ensure that these issues have been fixed up.

        // HSP-17, Task, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Destination
        // This issue should be migrated from Homospien Destination Failure workflow to Homospien Destination workflow
        // and left in Open status
        assertIssuesWorkflowState("HSP-17", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-17", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSPIEN_DESTINATION, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-18, Task, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Destination
        // This issue should be migrated from Homospien Destination Failure workflow to Homospien Destination workflow
        // and from In Progress status to Custom Status 3
        assertIssuesWorkflowState("HSP-18", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        assertLastChangeHistoryRecords("HSP-18",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSPIEN_DESTINATION, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, CUSTOM_STATUS_3)
                })));

        // HSP-19, Task, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Destination
        // This issue should be migrated from Homospien Destination Failure workflow to Homospien Destination workflow
        // and left in Custom Status 4
        assertIssuesWorkflowState("HSP-19", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("HSP-19", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSPIEN_DESTINATION, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-20, Task, this issue is actually broken as it is using Homospien Destination Failure instead of Homosapien Destination
        // This issue should be migrated from Homospien Destination Failure workflow to Homospien Destination workflow
        // and from Closed status to Resolved status
        assertIssuesWorkflowState("HSP-20", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Reopen" }));
        assertLastChangeHistoryRecords("HSP-20",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSPIEN_DESTINATION, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CLOSED_STATUS_NAME, RESOLVED_STATUS_NAME)
                })));

        // HSP-20, Task, this issue is not broken and is already using Homosapien Destination workflow
        // Nothing should be done to this issue
        assertIssuesWorkflowState("HSP-21", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-21", (List) null);

        // HSP-21, Task, this issue is not broken and is already using Homosapien Destination workflow
        // Nothing should be done to this issue
        assertIssuesWorkflowState("HSP-22", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        // Assert that the last change record is not the worflow one
        assertLastChangeHistoryRecords("HSP-22", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_3)));

        // HSP-23, Task, this issue is not broken and is already using Homosapien Destination workflow
        // Nothing should be done to this issue
        assertIssuesWorkflowState("HSP-23", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        // Assert that the last change record is not the worflow one
        assertLastChangeHistoryRecords("HSP-23", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_3, CUSTOM_STATUS_4)));

        // HSP-24, Task, this issue is not broken and is already using Homosapien Destination workflow
        // Nothing should be done to this issue
        assertIssuesWorkflowState("HSP-24", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Reopen" }));
        // Assert that the last change record is not the worflow one
        assertLastChangeHistoryRecords("HSP-24",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME),
                        new ExpectedChangeHistoryItem(RESOLUTION_FIELD_ID, "", "Fixed")
                })));

        // Improvements
        // These should all be migrated to the default JIRA workflow

        // HSP-25, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and stay in Open status
        assertIssuesWorkflowState("HSP-25", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("HSP-25", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, "jira")));

        // HSP-26, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and stay in Open status
        assertIssuesWorkflowState("HSP-26", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("HSP-26", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, "jira")));

        // HSP-27, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and from Custom Status 1 to In Progres status
        assertIssuesWorkflowState("HSP-27", IN_PROGRESS_STATUS_NAME, Arrays.asList(new String[] { TRANSIION_NAME_STOP_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("HSP-27",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, "jira"),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_1, IN_PROGRESS_STATUS_NAME)
                })));

        // HSP-28, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and from Custom Status 1 to In Progres status
        assertIssuesWorkflowState("HSP-28", IN_PROGRESS_STATUS_NAME, Arrays.asList(new String[] { TRANSIION_NAME_STOP_PROGRESS, TRANSIION_NAME_RESOLVE, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("HSP-28",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, "jira"),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_1, IN_PROGRESS_STATUS_NAME)
                })));

        // HSP-29, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and from Custom Status 3 to Resolved status
        assertIssuesWorkflowState("HSP-29", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { TRANSIION_NAME_REOPEN, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("HSP-29",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, "jira"),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_3, RESOLVED_STATUS_NAME)
                })));

        // HSP-30, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and from Custom Status 3 to Resolved status
        assertIssuesWorkflowState("HSP-30", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { TRANSIION_NAME_REOPEN, TRANSIION_NAME_CLOSE }));
        assertLastChangeHistoryRecords("HSP-30",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, "jira"),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_3, RESOLVED_STATUS_NAME)
                })));

        // HSP-31, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and stay in Closed status
        assertIssuesWorkflowState("HSP-31", CLOSED_STATUS_NAME, Arrays.asList(new String[] { TRANSIION_NAME_REOPEN }));
        assertLastChangeHistoryRecords("HSP-31", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, "jira")));

        // HSP-32, Improvement, this issue is not broken and is using Homosapien Source 2 and should be migrated to the default JIRA workflow
        // and stay in Closed status
        assertIssuesWorkflowState("HSP-32", CLOSED_STATUS_NAME, Arrays.asList(new String[] { TRANSIION_NAME_REOPEN }));
        assertLastChangeHistoryRecords("HSP-32", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_SOURCE_2, "jira")));

        // New Features
        // These issues should all be untouched - left on the Homospien Destination workflow
        // HSP-33, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        assertIssuesWorkflowState("HSP-33", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-33", (List) null);

        // HSP-34, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        assertIssuesWorkflowState("HSP-34", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-34", (List) null);

        // HSP-35, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        assertIssuesWorkflowState("HSP-35", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        // Ensure that the issues last change history record is the same as the one after the import. That is,
        // test that workflow migration did not touch this issue and hence did not add any change history records
        assertLastChangeHistoryRecords("HSP-35", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(SUMMARY_FIELD_ID, "In Progress New Feature 1", "Custom Status 3 New Feature 1")));

        // HSP-36, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        assertIssuesWorkflowState("HSP-36", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        // Ensure that the issues last change history record is the same as the one after the import. That is,
        // test that workflow migration did not touch this issue and hence did not add any change history records
        assertLastChangeHistoryRecords("HSP-36", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(SUMMARY_FIELD_ID, "In Progress New Feature 2", "Custom Status 3 New Feature 2")));

        // HSP-37, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        assertIssuesWorkflowState("HSP-37", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        // Ensure that the issues last change history record is the same as the one after the import. That is,
        // test that workflow migration did not touch this issue and hence did not add any change history records
        assertLastChangeHistoryRecords("HSP-37", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_3, CUSTOM_STATUS_4)));

        // HSP-38, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        assertIssuesWorkflowState("HSP-38", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        // Ensure that the issues last change history record is the same as the one after the import. That is,
        // test that workflow migration did not touch this issue and hence did not add any change history records
        assertLastChangeHistoryRecords("HSP-38", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_3, CUSTOM_STATUS_4)));

        // HSP-39, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        assertIssuesWorkflowState("HSP-39", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Reopen" }));
        assertLastChangeHistoryRecords("HSP-39",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME),
                        new ExpectedChangeHistoryItem(RESOLUTION_FIELD_ID, "", "Fixed")
                })));

        // HSP-40, New Feature, this issue is not broken and is already using Homospien Destination workflow
        // this issue should be left untouched
        assertIssuesWorkflowState("HSP-40", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Reopen" }));
        assertLastChangeHistoryRecords("HSP-40",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_4, RESOLVED_STATUS_NAME),
                        new ExpectedChangeHistoryItem(RESOLUTION_FIELD_ID, "", "Fixed")
                })));

        // Sub-Tasks
        // Sub-Tasks should be migrated to the Homosapien Destination Workflow

        // HSP-41, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and stay in Open status
        assertIssuesWorkflowState("HSP-41", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-41", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-42, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 2 to Custom Status 4
        assertIssuesWorkflowState("HSP-42", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("HSP-42",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4)
                })));

        // HSP-43, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 1 to Custom Status 3
        assertIssuesWorkflowState("HSP-43", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        assertLastChangeHistoryRecords("HSP-43",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3)
                })));

        // HSP-44, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and should stay on Open status
        assertIssuesWorkflowState("HSP-44", STATUS_OPEN, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-44", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-45, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and should stay on Custom Status 3
        assertIssuesWorkflowState("HSP-45", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        assertLastChangeHistoryRecords("HSP-45", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-46, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 1 to Custom Status 3
        assertIssuesWorkflowState("HSP-46", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        assertLastChangeHistoryRecords("HSP-46",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_1, CUSTOM_STATUS_3)
                })));

        // HSP-47, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and should stay on Custom Status 3
        assertIssuesWorkflowState("HSP-47", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Go custom 4" }));
        assertLastChangeHistoryRecords("HSP-47", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION)));

        // HSP-48, Sub-Task, this issue is not broken and should be migrated from Homospien Source 1 workflow to Homospien Destination workflow
        // and from Custom Status 2 to Custom Status 4
        assertIssuesWorkflowState("HSP-48", CUSTOM_STATUS_4, Arrays.asList(new String[] { "Resolve" }));
        assertLastChangeHistoryRecords("HSP-48",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORFKLOW_HOMOSAPIEN_SOURCE_1, WORKFLOW_HOMOSPIEN_DESTINATION),
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, CUSTOM_STATUS_2, CUSTOM_STATUS_4)
                })));

        // Custom Issue Type
        // Issues of Custom Issue Type should be migrated from Homosapien Custom Issue Type Source workflow to Homosapien Custom Issue Type Destination workflow

        // HSP-49, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Open status
        assertIssuesWorkflowState("HSP-49", STATUS_OPEN, Arrays.asList(new String[] { "Go custom" }));
        assertLastChangeHistoryRecords("HSP-49", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION)));

        // HSP-50, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Custom Status 3
        assertIssuesWorkflowState("HSP-50", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Close" }));
        assertLastChangeHistoryRecords("HSP-50", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION)));

        // HSP-51, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Open status
        assertIssuesWorkflowState("HSP-51", STATUS_OPEN, Arrays.asList(new String[] { "Go custom" }));
        assertLastChangeHistoryRecords("HSP-51", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION)));

        // HSP-52, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Resolved status
        assertIssuesWorkflowState("HSP-52", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-52", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION)));

        // HSP-53, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Resolved status
        assertIssuesWorkflowState("HSP-53", RESOLVED_STATUS_NAME, Arrays.asList(new String[] { "Go Custom 3" }));
        assertLastChangeHistoryRecords("HSP-53", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION)));

        // HSP-54, Custom Issue Type, this issue is using Custom Issue Type Source workflow and should be migrated to Homosapien Custom Issue Type Destination
        // and left in Custom Status 3
        assertIssuesWorkflowState("HSP-54", CUSTOM_STATUS_3, Arrays.asList(new String[] { "Close" }));
        assertLastChangeHistoryRecords("HSP-54", new ExpectedChangeHistoryRecord(new ExpectedChangeHistoryItem(WORKFLOW_FIELD_ID, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_SOURCE, WORKFLOW_HOMOSAPIEN_CUSTOM_ISSUETYPE_DESTINATION)));

        // Check issues in another project and ensure they have not been touched by the migration
        checkIssuesInTestProject();
    }

    private void checkIssuesInTestProject() throws SAXException
    {
        assertIssuesWorkflowState("TST-1", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS }));
        assertLastChangeHistoryRecords("TST-1", (List) null);

        assertIssuesWorkflowState("TST-2", STATUS_OPEN, Arrays.asList(new String[] { TRANSIION_NAME_START_PROGRESS }));
        assertLastChangeHistoryRecords("TST-2",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem("Assignee", "Developer User", "Admin")
                })));

        assertIssuesWorkflowState("TST-3", IN_PROGRESS_STATUS_NAME, Arrays.asList(new String[] { TRANSIION_NAME_STOP_PROGRESS, "Close" }));
        assertLastChangeHistoryRecords("TST-3",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, STATUS_OPEN, IN_PROGRESS_STATUS_NAME)
                })));

        assertIssuesWorkflowState("TST-4", CUSTOM_STATUS_2, Arrays.asList(new String[] { "Reopen" }));
        assertLastChangeHistoryRecords("TST-4",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_2),
                        new ExpectedChangeHistoryItem(RESOLUTION_FIELD_ID, "", "Incomplete")
                })));

        assertIssuesWorkflowState("TST-5", STATUS_OPEN, Arrays.asList(new String[] { TRANSITION_NAME_GO_CUSTOM }));
        assertLastChangeHistoryRecords("TST-5", (List) null);

        assertIssuesWorkflowState("TST-6", CUSTOM_STATUS_2, Arrays.asList(new String[] { "Reopen" }));
        assertLastChangeHistoryRecords("TST-6",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, STATUS_OPEN, CUSTOM_STATUS_2),
                        new ExpectedChangeHistoryItem(FIX_VERSIONS_FIELD_ID, "", "Version 1"),
                        new ExpectedChangeHistoryItem(RESOLUTION_FIELD_ID, "", "Fixed")
                })));

        assertIssuesWorkflowState("TST-7", CLOSED_STATUS_NAME, null);
        assertLastChangeHistoryRecords("TST-7",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, CLOSED_STATUS_NAME),
                        new ExpectedChangeHistoryItem(FIX_VERSIONS_FIELD_ID, "", "Version 1"),
                        new ExpectedChangeHistoryItem(RESOLUTION_FIELD_ID, "", "Cannot Reproduce")
                })));


        assertIssuesWorkflowState("TST-8", IN_PROGRESS_STATUS_NAME, Arrays.asList(new String[] { TRANSIION_NAME_STOP_PROGRESS, "Close" }));
        assertLastChangeHistoryRecords("TST-8",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, STATUS_OPEN, IN_PROGRESS_STATUS_NAME)
                })));


        assertIssuesWorkflowState("TST-9", CLOSED_STATUS_NAME, null);
        assertLastChangeHistoryRecords("TST-9",
                new ExpectedChangeHistoryRecord(Arrays.asList(new ExpectedChangeHistoryItem[] {
                        new ExpectedChangeHistoryItem(STATUS_FIELD_ID, IN_PROGRESS_STATUS_NAME, CLOSED_STATUS_NAME),
                        new ExpectedChangeHistoryItem(FIX_VERSIONS_FIELD_ID, "", "Version 3"),
                        new ExpectedChangeHistoryItem(RESOLUTION_FIELD_ID, "", "Won't Fix")
                })));

        assertIssuesWorkflowState("TST-10", STATUS_OPEN, Arrays.asList(new String[] { TRANSITION_NAME_GO_CUSTOM }));
        assertLastChangeHistoryRecords("TST-10", (List) null);

        assertIssuesWorkflowState("TST-11", STATUS_OPEN, Arrays.asList(new String[] { TRANSITION_NAME_GO_CUSTOM }));
        assertLastChangeHistoryRecords("TST-11", (List) null);
    }

    /**
     * Ensure the issue verifier detects problems before issues are migrated through workflow
     */
    public void testIssueVerifier()
    {
        // Import data where:
        // TST-2 - does not have a workflow id
        // TST-3 - has an invlid status (with id 30)
        // TST-6 - does not have a workflow id
        // TST-7 - has invalid issue type (with id 10)
        // TST-9 - has invalid issue type (with id 14)
        // TST-11 - has invalid status (with id 20)
        restoreData("WorkflowMigrationTestIssueVerifier.xml");

        // Make an association between the workflow and the project
        administration.project().associateWorkflowScheme(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, Collections.EMPTY_MAP, false);

        waitForFailedMigration("WORKFLOW ASSOCIATION ERROR");

        // Ensure that the Issue Verifier found problems.
        // TODO unfortunately XPath does not understand this pseudo-HTML page
        // if you have idea hot to make it XPath-searchable - try
        // for now, fe have to fall back to asserting against raw HTML
//        getAssertions().getJiraFormAssertions().assertFormNotificationMsg("There are errors associated with issues that are to"
//                + " be migrated to the new workflow association");
        assertTextPresent("There are errors associated with issues that are to"
                + " be migrated to the new workflow association");


        // Assert the correct error messages are found
        assertTextPresent("Unable to determine the current workflow entry for issue &#39;TST-2&#39;");
        assertTextPresent("Unable to determine the current status for issue &#39;TST-3&#39;");
        assertTextPresent("Unable to determine the current workflow entry for issue &#39;TST-6&#39;");
        assertTextPresent("Unable to determine the current issue type for issue &#39;TST-7&#39;");
        assertTextPresent("Unable to determine the current issue type for issue &#39;TST-9&#39;");
        assertTextPresent("Unable to determine the current status for issue &#39;TST-11&#39;");

        // NOTE: the issue verifier produces extra messages:
        // 1. Encountered an error processing the issue 'TST-3' - please refer to the logs.
        // 2. Encountered an error processing the issue 'TST-11' - please refer to the logs.
        // These messages do not really add any value, but the way the code is written it is not easy to take them out.
        // Therefore, we do not assert for their presentce in this test. But do not be scared if you see the messages.
    }

    public void testMultipleActiveWorkflowErrorMessageNotPresentInEnterprise()
    {
        restoreData("WorkflowMigrationTest.xml");
        gotoPage("secure/admin/workflows/ListWorkflows.jspa");
        assertTextNotPresent("An error has occured during workflow activation and the result is multiple active workflows");
    }

    /**
     * Tests that 2 users can look at the same task and get their proper view of it, eg the starter can acknowledge and
     * the other person cant
     */
    public void testMultiAdminTaskProgressFlow()
    {
        restoreData("WorkflowMigrationTwoAdmins.xml");

        Map statusMapping = new HashMap();
        // For Bug Issue Type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_1_1", CUSTOM_STATUS_3);
        // - For Custom Status 2 - select Custom Status 4
        statusMapping.put("mapping_1_10001", CUSTOM_STATUS_4);

        // For Improvement issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_4_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_4_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_4_6", CUSTOM_STATUS_4);

        // For New Feature issue type
        // - For Open status - select Custom Status 3
        statusMapping.put("mapping_2_1", CUSTOM_STATUS_3);
        // - For In Progress status - select Resolved
        statusMapping.put("mapping_2_3", RESOLVED_STATUS_NAME);
        // - For Closed status - select Custom Status 4
        statusMapping.put("mapping_2_6", CUSTOM_STATUS_4);

        // Try to migrate the project again
        administration.project().associateWorkflowScheme(TEST_PROJECT_NAME, DESTINATION_WORKFLOW_SCHEME, statusMapping, false);

        // ok find out what the task id is
        long taskId = getSubmittedTaskId();

        waitForTaskAcknowledgement(taskId);
        assertTextPresent("input type=\"submit\" name=\"Acknowledge\"");
        assertTextNotPresent("input type=\"submit\" name=\"Done\"");
        validateProgressBarUI(ACKNOWLEDGE);

        // ok connect as another user and have a look at the task
        logout();
        login("admin2", "admin2");
        navigation.gotoAdmin();
        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId + "&schemeId=10001");

        validateProgressBarUI(DONE);
        assertTextNotPresent("input type=\"submit\" name=\"Acknowledge\"");
        assertTextPresent("input type=\"submit\" name=\"Done\"");

        // ok go back and acknowledge as the task starter
        logout();
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
        navigation.gotoAdmin();
        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId + "&schemeId=10001");

        assertTextPresent("input type=\"submit\" name=\"Acknowledge\"");
        assertTextNotPresent("input type=\"submit\" name=\"Done\"");
        validateProgressBarUI(ACKNOWLEDGE);

        submit(ACKNOWLEDGE);

        // now the task should be cleaned up
        gotoPage("/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=10000&taskId=" + taskId + "&schemeId=10001");
        assertTextPresent("The task could not be found. Perhaps it has finished and has been acknowledged?");
        assertTextPresent("input type=\"submit\" name=\"Done\"");

    }

    private void assertStatusMapping(WebTable statusTable, String sourceStatusName, int tableRow, int issueTypeId, int sourceStatusId, String[] destinationStatuses, String optionToSelect)
    {
        assertEquals(sourceStatusName, statusTable.getCellAsText(tableRow, 0).trim());
        TableCell tableCell = statusTable.getTableCell(tableRow, 2);
        // Mapping for In Progress status for Bug issue type
        String selectName = "mapping_" + issueTypeId + "_" + sourceStatusId;
        HTMLElement[] selectList = tableCell.getElementsWithName(selectName);
        assertNotNull(selectList);
        assertEquals(1, selectList.length);
        assertOptionsEqual(selectName, destinationStatuses);

        selectOption(selectName, optionToSelect);
    }

    private void waitForFailedMigration(String typeOfErrorPage)
    {
        final int MAX_ITERATIONS = 100;
        int its = 0;
        while (true)
        {
            its++;
            if (its > MAX_ITERATIONS)
            {
                fail("The Workflow Migration took longer than " + MAX_ITERATIONS + " attempts!  Why?");
            }
            // are we on the "still working" page or the "error" page
            // if its neither then fail
            if (getResponseText().indexOf("type=\"submit\" name=\"Refresh\"") != -1)
            {
                // we are on the "still working page"
                // click on the Refresh Button
                submit("Refresh");
            }
            else if (getDialog().getResponsePageTitle().indexOf(typeOfErrorPage) != -1)
            {
                // we are on the "error" page"
                return;
            }
            else
            {
                // we are on a page we dont expect
                fail("Page encountered during migration that was not expected");
            }

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                fail("Test interupted");
            }
        }
    }

}
