package com.atlassian.jira.webtests.zsuites;

import com.atlassian.jira.functest.framework.FuncTestSuite;
import com.atlassian.jira.webtests.ztests.admin.TestIssueLinkCheck;
import com.atlassian.jira.webtests.ztests.admin.issuetypes.TestIssueTypeSchemeMigration;
import com.atlassian.jira.webtests.ztests.admin.issuetypes.TestIssueTypeSchemes;
import com.atlassian.jira.webtests.ztests.admin.issuetypes.TestIssueTypes;
import com.atlassian.jira.webtests.ztests.admin.issuetypes.TestMultiIssueTypes;
import com.atlassian.jira.webtests.ztests.attachment.TestIssueFileAttachments;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkChangeIssues;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkDeleteIssues;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkEditIssues;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveIssues;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveIssuesForEnterprise;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkMoveMappingVersionsAndComponents;
import com.atlassian.jira.webtests.ztests.bulk.TestBulkOperationIssueNavigator;
import com.atlassian.jira.webtests.ztests.email.TestBulkDeleteIssuesNotifications;
import com.atlassian.jira.webtests.ztests.email.TestIssueNotifications;
import com.atlassian.jira.webtests.ztests.fields.TestResolutionDateField;
import com.atlassian.jira.webtests.ztests.hints.TestHints;
import com.atlassian.jira.webtests.ztests.issue.TestCloneIssueWithSubTasks;
import com.atlassian.jira.webtests.ztests.issue.TestCreateIssue;
import com.atlassian.jira.webtests.ztests.issue.TestCreateIssueForEnterprise;
import com.atlassian.jira.webtests.ztests.issue.TestCreateIssueViaDirectLink;
import com.atlassian.jira.webtests.ztests.issue.TestEditIssue;
import com.atlassian.jira.webtests.ztests.issue.TestEditIssueFields;
import com.atlassian.jira.webtests.ztests.issue.TestInlineIssueLinking;
import com.atlassian.jira.webtests.ztests.issue.TestIssueActionErrors;
import com.atlassian.jira.webtests.ztests.issue.TestIssueBrowseBadProjectRegex;
import com.atlassian.jira.webtests.ztests.issue.TestIssueConstants;
import com.atlassian.jira.webtests.ztests.issue.TestIssueHeader;
import com.atlassian.jira.webtests.ztests.issue.TestIssueOperations;
import com.atlassian.jira.webtests.ztests.issue.TestIssueOperationsOnDeletedIssue;
import com.atlassian.jira.webtests.ztests.issue.TestIssueOperationsWithLimitedPermissions;
import com.atlassian.jira.webtests.ztests.issue.TestIssuePrintableView;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityActions;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithCustomFields;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithGroupsAndRoles;
import com.atlassian.jira.webtests.ztests.issue.TestIssueSecurityWithRoles;
import com.atlassian.jira.webtests.ztests.issue.TestIssueTabPanels;
import com.atlassian.jira.webtests.ztests.issue.TestIssueTrackback;
import com.atlassian.jira.webtests.ztests.issue.TestIssueViews;
import com.atlassian.jira.webtests.ztests.issue.TestLabels;
import com.atlassian.jira.webtests.ztests.issue.TestLabelsFormats;
import com.atlassian.jira.webtests.ztests.issue.TestLinkIssue;
import com.atlassian.jira.webtests.ztests.issue.TestManageLinkClosedIssues;
import com.atlassian.jira.webtests.ztests.issue.TestOpsBarStructure;
import com.atlassian.jira.webtests.ztests.issue.TestSearchXmlCustomIssueView;
import com.atlassian.jira.webtests.ztests.issue.TestViewIssue;
import com.atlassian.jira.webtests.ztests.issue.TestXmlCustomIssueView;
import com.atlassian.jira.webtests.ztests.issue.TestXmlIssueView;
import com.atlassian.jira.webtests.ztests.issue.TestXmlIssueViewBackwardCompatibility;
import com.atlassian.jira.webtests.ztests.issue.TestXmlIssueViewErrors;
import com.atlassian.jira.webtests.ztests.issue.assign.TestAssignIssue;
import com.atlassian.jira.webtests.ztests.issue.clone.TestCloneIssueLinking;
import com.atlassian.jira.webtests.ztests.issue.links.TestReindexOnLinkChange;
import com.atlassian.jira.webtests.ztests.issue.move.TestMoveIssue;
import com.atlassian.jira.webtests.ztests.issue.move.TestMoveIssueAndRemoveFieldsEnterprise;
import com.atlassian.jira.webtests.ztests.issue.move.TestMoveIssueForEnterprise;
import com.atlassian.jira.webtests.ztests.issue.move.TestRedirectToMovedIssues;
import com.atlassian.jira.webtests.ztests.issue.trackbacks.TestTrackbackSending;
import com.atlassian.jira.webtests.ztests.misc.TestReplacedLocalVelocityMacros;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigator;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorColumnLinks;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorEncoding;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorExcelView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorFullContentView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorPrintableView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorRssView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorWordView;
import com.atlassian.jira.webtests.ztests.navigator.TestIssueNavigatorXmlViewRedirect;
import com.atlassian.jira.webtests.ztests.project.TestMultipleProjectsWithIssueSecurityWithRoles;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionParentPicker;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionSecurityLevel;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionStep1;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionStep2;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionSystemFields;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionVariousOperations;
import com.atlassian.jira.webtests.ztests.subtask.TestIssueToSubTaskConversionWithFields;
import com.atlassian.jira.webtests.ztests.subtask.TestSubTaskToIssueConversionSecurityLevel;
import com.atlassian.jira.webtests.ztests.subtask.TestSubTaskToIssueConversionStep1;
import com.atlassian.jira.webtests.ztests.subtask.move.TestMoveSubTaskIssueType;
import junit.framework.Test;

/**
 * A suite of tests around Issues.  Obviously this suite could be quite large
 *
 * @since v4.0
 */
public class FuncTestSuiteIssues extends FuncTestSuite
{
    /**
     * A static declaration of this particular FuncTestSuite
     */
    public static final FuncTestSuite SUITE = new FuncTestSuiteIssues();

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

    public FuncTestSuiteIssues()
    {
        addTest(TestIssueConstants.class);
        addTest(TestIssueTabPanels.class);
        addTest(TestIssueLinkCheck.class);
        addTest(TestAssignIssue.class);
        addTest(TestCreateIssue.class);
        addTest(TestIssueNavigator.class);
        addTest(TestIssueHeader.class);
        addTest(TestEditIssue.class);
        addTest(TestEditIssueFields.class);
        addTest(TestIssueOperations.class);
        addTest(TestIssueOperationsOnDeletedIssue.class);
        addTest(TestIssueOperationsWithLimitedPermissions.class);
        addTest(TestMoveIssue.class);
        addTest(TestLinkIssue.class);
        addTest(TestIssueFileAttachments.class);
        addTest(TestIssueTypes.class);
        addTest(TestIssueSecurityActions.class);
        addTest(TestIssueSecurityWithGroupsAndRoles.class);
        addTest(TestIssueSecurityWithCustomFields.class);
        addTest(TestIssueSecurityWithRoles.class);
        addTest(TestCreateIssueForEnterprise.class);
        addTest(TestMoveIssueForEnterprise.class);
        addTest(TestInlineIssueLinking.class);
        addTest(TestMoveSubTaskIssueType.class);
        addTest(TestIssueTypeSchemes.class);
        addTest(TestIssueToSubTaskConversionSystemFields.class);
        addTest(TestIssueToSubTaskConversionVariousOperations.class);
        addTest(TestIssueToSubTaskConversionStep1.class);
        addTest(TestIssueToSubTaskConversionStep2.class);
        addTest(TestIssueToSubTaskConversionWithFields.class);
        addTest(TestIssueToSubTaskConversionParentPicker.class);
        addTest(TestIssueToSubTaskConversionSecurityLevel.class);
        addTest(TestSubTaskToIssueConversionStep1.class);
        addTest(TestSubTaskToIssueConversionSecurityLevel.class);
        addTest(TestBulkChangeIssues.class);
        addTest(TestBulkMoveIssues.class);
        addTest(TestBulkMoveIssuesForEnterprise.class);
        addTest(TestBulkMoveMappingVersionsAndComponents.class);
        addTest(TestBulkEditIssues.class);
        addTest(TestBulkDeleteIssues.class);
        addTest(TestMultiIssueTypes.class);
        addTest(TestIssueTypeSchemeMigration.class);
        addTest(TestCreateIssueViaDirectLink.class);
        addTest(TestCloneIssueWithSubTasks.class);
        addTest(TestIssueNavigatorColumnLinks.class);
        addTest(TestIssueNavigatorPrintableView.class);
        addTest(TestIssueNavigatorExcelView.class);
        addTest(TestIssueNavigatorRssView.class);
        addTest(TestIssueNavigatorFullContentView.class);
        addTest(TestIssueNavigatorWordView.class);
        addTest(TestIssueViews.class);
        addTest(TestXmlIssueView.class);
        addTest(TestIssuePrintableView.class);
        addTest(TestViewIssue.class);
        addTest(TestIssueBrowseBadProjectRegex.class);
        addTest(TestRedirectToMovedIssues.class);
        addTest(TestManageLinkClosedIssues.class);
        addTest(TestMultipleProjectsWithIssueSecurityWithRoles.class);
        addTest(TestMoveIssueAndRemoveFieldsEnterprise.class);
        addTest(TestBulkOperationIssueNavigator.class);
        addTest(TestIssueNotifications.class);
        addTest(TestIssueTrackback.class);
        addTest(TestBulkDeleteIssuesNotifications.class);
        addTest(TestIssueNavigatorEncoding.class);
        addTest(TestIssueNavigatorXmlViewRedirect.class);
        addTest(TestResolutionDateField.class);
        addTest(TestXmlIssueViewBackwardCompatibility.class);
        addTest(TestSearchXmlCustomIssueView.class);
        addTest(TestXmlCustomIssueView.class);
        addTest(TestXmlIssueViewErrors.class);
        addTest(TestCloneIssueLinking.class);
        addTest(TestReindexOnLinkChange.class);
        addTest(TestOpsBarStructure.class);

        //Has tests for issue view.
        addTest(TestReplacedLocalVelocityMacros.class);

        addTest(TestLabels.class);
        addTest(TestIssueActionErrors.class);

        addTest(TestLabelsFormats.class);
        
        addTest(TestHints.class);
        addTest(TestTrackbackSending.class);
    }
}
