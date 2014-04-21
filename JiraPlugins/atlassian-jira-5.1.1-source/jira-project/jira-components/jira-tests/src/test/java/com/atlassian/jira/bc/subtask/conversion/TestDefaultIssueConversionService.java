package com.atlassian.jira.bc.subtask.conversion;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.IssueTypeImpl;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.UserTestUtil;
import com.atlassian.jira.workflow.WorkflowManager;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the DefaultIssueConversionService
 */
public class TestDefaultIssueConversionService extends ListeningTestCase
{
    private DefaultIssueConversionService service;
    private User bob;
    private MockIssue mockIssue;
    private Mock customFieldControl;
    private static final Long ID_0 = new Long(0);
    private Mock fieldManagerControl;

    @Before
    public void setUp() throws Exception
    {
        bob = UserTestUtil.getUser("bob");
        mockIssue = new MockIssue(ID_0);
        mockIssue.setIssueTypeObject(new IssueTypeImpl(new MockGenericValue("issueType", EasyMap.build("id", ID_0)), null, null));

        customFieldControl = new Mock(CustomField.class);
        MockControl.createControl(CustomField.class);

        fieldManagerControl = new Mock(FieldManager.class);
        fieldManagerControl.expectAndReturn("isCustomField", P.ANY_ARGS, Boolean.TRUE);

        service = new MockDefaultIssueConversionService(null, null, null, null, null, (FieldManager) fieldManagerControl.proxy());

        MultiTenantContextTestUtils.setupMultiTenantSystem();
    }

    @Test
    public void testIsShouldCheckFieldValueNonCustomField()
    {
        fieldManagerControl.expectAndReturn("isCustomField", P.ANY_ARGS, Boolean.FALSE);
        customFieldControl.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.TRUE);

        assertTrue(service.isShouldCheckFieldValue( mockIssue, (CustomField) customFieldControl.proxy()));
    }

    @Test
    public void testIsShouldCheckFieldValueTrue()
    {
        customFieldControl.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.TRUE);

        assertTrue(service.isShouldCheckFieldValue(mockIssue, (CustomField) customFieldControl.proxy()));
    }

    @Test
    public void testIsShouldCheckFieldValueFalse()
    {
        customFieldControl.expectAndReturn("isInScope", P.ANY_ARGS, Boolean.FALSE);

        assertFalse(service.isShouldCheckFieldValue(mockIssue, (CustomField) customFieldControl.proxy()));
    }

    /**
     * We only want to test methods from the abstract class here.  We therefore need this Mock implementation,
     * such that we can instantiate the abstract class.
     */
    private class MockDefaultIssueConversionService extends DefaultIssueConversionService
    {
        public MockDefaultIssueConversionService(PermissionManager permissionManager, WorkflowManager workflowManager, FieldLayoutManager fieldLayoutManager, IssueTypeSchemeManager issueTypeSchemeManager, JiraAuthenticationContext jiraAuthenticationContext, FieldManager fieldManager)
        {
            super(permissionManager, workflowManager, fieldLayoutManager, issueTypeSchemeManager, jiraAuthenticationContext, fieldManager);
        }

        protected boolean canIssueSecurityFieldIgnore()
        {
            return false;
        }

        public boolean canConvertIssue(JiraServiceContext context, Issue issue)
        {
            return false;
        }

        public void validateTargetIssueType(JiraServiceContext context, Issue issue, IssueType issueType, final String fieldNameIssueTypeId)
        {

        }

        public void preStoreUpdates(JiraServiceContext context, IssueChangeHolder changeHolder, Issue currentIssue, MutableIssue targetIssue)
        {

        }
    }
}
