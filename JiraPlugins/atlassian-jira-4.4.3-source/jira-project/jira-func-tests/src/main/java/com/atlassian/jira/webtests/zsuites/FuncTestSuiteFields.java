package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.index.TestReindexMessages;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkOperationCustomField;
import com.atlassian.jira.webtests.ztests.customfield.TestCascadingSelectCustomField;
import com.atlassian.jira.webtests.ztests.customfield.TestCustomFields;
import com.atlassian.jira.webtests.ztests.customfield.TestCustomFieldsNoSearcherPermissions;
import com.atlassian.jira.webtests.ztests.customfield.TestDeleteCustomField;
import com.atlassian.jira.webtests.ztests.customfield.TestEditCustomFieldDescription;
import com.atlassian.jira.webtests.ztests.customfield.TestVersionCustomField;
import com.atlassian.jira.webtests.ztests.email.TestCustomFieldNotifications;
import com.atlassian.jira.webtests.ztests.fields.TestCustomFieldSearcherVisibility;
import com.atlassian.jira.webtests.ztests.fields.TestFieldLayoutSchemes;
import com.atlassian.jira.webtests.ztests.fields.TestFieldLayoutSchemes2;
import com.atlassian.jira.webtests.ztests.fields.TestFieldRenderers;
import com.atlassian.jira.webtests.ztests.fields.TestFieldScreenTabs;
import com.atlassian.jira.webtests.ztests.fields.TestFieldScreens;
import com.atlassian.jira.webtests.ztests.fields.TestFieldScreensInvalidWorkflow;
import com.atlassian.jira.webtests.ztests.fields.TestResolutionDateField;
import com.atlassian.jira.webtests.ztests.issue.TestDefaultTextRenderer;
import com.atlassian.jira.webtests.ztests.issue.TestEnvironmentField;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithCustomFields;
import com.atlassian.jira.webtests.ztests.issue.TestWikiRendererXSS;
import com.atlassian.jira.webtests.ztests.issue.TestLabelsFormats;
import com.atlassian.jira.webtests.ztests.issue.move.TestMoveIssueAndRemoveFieldsEnterprise;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionSystemFields;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionWithFields;
import com.atlassian.jira.webtests.ztests.timetracking.legacy.TestTimeTrackingNavagableFields;
import junit.framework.Test;

/**
 * A suite of tests related to Fields (and especially custom fields)
 *
 * @since v4.0
 */
public class FuncTestSuiteFields extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteFields();

    /**
     * The pattern in JUnit/IDEA JUnit runner is that if a class has a static suite() method that returns a Test, then
     * this is the entry point for running your tests.  So make sure you declare one of these in the FuncTestSuite
     * implementation.
     *
     * @return a Test that can be run by as JUnit TestRunner
     */
    public static Test suite()
    {
        return SUITE.createTest();
    }

    public FuncTestSuiteFields()
    {
        addTest(TestCascadingSelectCustomField.class);
        addTest(TestCustomFields.class);
        addTest(TestFieldScreens.class);
        addTest(TestFieldScreensInvalidWorkflow.class);
        addTest(TestFieldScreenTabs.class);
        addTest(TestFieldLayoutSchemes.class);
        addTest(TestFieldLayoutSchemes2.class);
        addTest(TestIssueSecurityWithCustomFields.class);
        addTest(TestIssueToSubTaskConversionSystemFields.class);
        addTest(TestIssueToSubTaskConversionWithFields.class);
        addTest(TestFieldRenderers.class);
        addTest(TestBulkOperationCustomField.class);
        addTest(TestTimeTrackingNavagableFields.class);
        addTest(TestCustomFieldsNoSearcherPermissions.class);
        addTest(TestDeleteCustomField.class);
        addTest(TestMoveIssueAndRemoveFieldsEnterprise.class);
        addTest(TestCustomFieldNotifications.class);
        addTest(TestVersionCustomField.class);
        addTest(TestResolutionDateField.class);
        addTest(TestCustomFieldSearcherVisibility.class);
        addTest(TestEditCustomFieldDescription.class);
        addTest(TestEnvironmentField.class);
        
        addTest(TestWikiRendererXSS.class);
        addTest(TestDefaultTextRenderer.class);

        addTest(TestReindexMessages.class);

        addTest(TestLabelsFormats.class);        

        addTestsInPackage("com.atlassian.jira.webtests.ztests.screens.tabs", true);
    }
}