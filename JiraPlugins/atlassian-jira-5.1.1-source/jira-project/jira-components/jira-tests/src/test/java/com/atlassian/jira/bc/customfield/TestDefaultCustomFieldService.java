package com.atlassian.jira.bc.customfield;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockUser;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.MockJiraServiceContext;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 */
public class TestDefaultCustomFieldService extends ListeningTestCase
{
    private User testUser;
    private MockJiraServiceContext jiraServiceContext;
    private CustomField customField;

    @Before
    public void setUp() throws Exception
    {
        testUser = new MockUser("TestUser");
        jiraServiceContext = new MockJiraServiceContext(testUser);
        Mock mockCustomField = new Mock(CustomField.class);
        customField = (CustomField) mockCustomField.proxy();
    }

    @Test
    public void testValidateDeleteNotAdmin()
    {
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.FALSE);

        // Test User is not admin
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(), null, null, null);
        // run validate
        defaultCustomFieldService.validateDelete(jiraServiceContext, new Long(1));
        // we expect the jira service context to contain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You do not have the JIRA Administrator permission required to perform this operation.",
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteCustomFieldThatDoesntExist()
    {
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.TRUE);

        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(new Long(1)) }, null);

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(),
                (CustomFieldManager) mockCustomFieldManager.proxy(), null, null);
        // run validate
        defaultCustomFieldService.validateDelete(jiraServiceContext, new Long(1));
        // we expect the jira service context to contiain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("Invalid custom field specified",
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateDeleteSuccess()
    {
        final MutableBoolean validateNotUsedInPermissionSchemes = new MutableBoolean();
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.TRUE);

        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(new Long(1)) }, customField);

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(),
                (CustomFieldManager) mockCustomFieldManager.proxy(), null, null)
        {

            void validateNotUsedInPermissionSchemes(JiraServiceContext jiraServiceContext, Long customFieldId, boolean forUpdate)
            {
                //do nothing
                validateNotUsedInPermissionSchemes.called = true;
            }
        };
        // run validate
        defaultCustomFieldService.validateDelete(jiraServiceContext, new Long(1));
        // we expect the jira service context to contiain an error message
        jiraServiceContext.assertNoErrors();
        assertTrue(validateNotUsedInPermissionSchemes.called);
    }

    @Test
    public void testValidateDeleteUsedInPermissionSchemes()
    {
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.TRUE);

        Mock mockCustomField = new Mock(CustomField.class);
        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(new Long(1)) }, mockCustomField.proxy());

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(),
                (CustomFieldManager) mockCustomFieldManager.proxy(), null, null)
        {

            void validateNotUsedInPermissionSchemes(JiraServiceContext jiraServiceContext, Long customFieldId, boolean forUpdate)
            {
                // Pretend we are used.
                jiraServiceContext.getErrorCollection().addErrorMessage("UsedInPermissionSchemes");
            }
        };
        // run validate
        defaultCustomFieldService.validateDelete(jiraServiceContext, new Long(1));
        // we expect the jira service context to contiain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("UsedInPermissionSchemes", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUpdateWithNullCustomFieldId()
    {
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService(null, null, null, null);
        try
        {
            defaultCustomFieldService.validateUpdate(null, null, null, null, null);
            fail("Should have failed with IllegalArgumentException.");
        }
        catch (IllegalArgumentException e)
        {
            //Expected error if the customFieldId is null
        }
    }

    @Test
    public void testValidateUpdateNotAdmin()
    {
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.FALSE);

        // Test User is not admin
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(), null, null, null);
        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), null, null, null);
        // we expect the jira service context to contiain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("You do not have the JIRA Administrator permission required to perform this operation.",
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUpdateCustomFieldThatDoesntExist()
    {
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.TRUE);

        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(new Long(1)) }, null);

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(),
                (CustomFieldManager) mockCustomFieldManager.proxy(), null, null);
        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), null, null, null);
        // we expect the jira service context to contiain an error message
        assertEquals(1, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        assertEquals("Invalid custom field specified",
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateUpdateWithNoName()
    {
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.TRUE);

        Mock mockCustomField = new Mock(CustomField.class);
        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(new Long(1)) }, mockCustomField.proxy());

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(),
                (CustomFieldManager) mockCustomFieldManager.proxy(), null, null);
        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), null, null, null);
        // we expect the jira service context to contiain an error message
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("You must specify a name for this custom field.",
                jiraServiceContext.getErrorCollection().getErrors().get("name"));
    }

    @Test
    public void testValidateUpdateWithInvalidSearcher()
    {
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.TRUE);

        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(new Long(1)) }, customField);
        mockCustomFieldManager.expectAndReturn("getCustomFieldSearcher", new Constraint[] { P.eq("InvalidSearcherKey") }, null);

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(),
                (CustomFieldManager) mockCustomFieldManager.proxy(), null, null);
        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), "customFieldName", null, "InvalidSearcherKey");
        // we expect the jira service context to contiain an error message
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("Invalid searcher chosen",
                jiraServiceContext.getErrorCollection().getErrors().get("searcher"));
    }

    @Test
    public void testValidateUpdateWithValidSearcher()
    {
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.TRUE);

        Mock mockCustomFieldSearcher = new Mock(CustomFieldSearcher.class);
        Mock mockCustomField = new Mock(CustomField.class);
        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(new Long(1)) }, mockCustomField.proxy());
        mockCustomFieldManager.expectAndReturn("getCustomFieldSearcher", new Constraint[] { P.eq("ValidSearcherKey") }, mockCustomFieldSearcher.proxy());

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(),
                (CustomFieldManager) mockCustomFieldManager.proxy(), null, null)
        {
            @Override
            void validateNotUsedInPermissionSchemes(final JiraServiceContext jiraServiceContext, final Long customFieldId, final boolean forUpdate)
            {
                fail("validateNotUsedInPermissionSchemes should not have been called, as we are not removing a searcher");
            }
        };
        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), "customFieldName", null, "ValidSearcherKey");
        // we expect the jira service context to contiain an error message
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testValidateUpdateRemovingSearcher()
    {
        final MutableBoolean validateNotUsedInPermissionSchemes = new MutableBoolean();
        Mock mockPermissionManager = new Mock(GlobalPermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", new Constraint[] { P.eq(Permissions.ADMINISTER), P.eq(testUser) }, Boolean.TRUE);

        Mock mockCustomField = new Mock(CustomField.class);
        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(new Long(1)) }, mockCustomField.proxy());

        // Test User customfield is null
        DefaultCustomFieldService defaultCustomFieldService = new DefaultCustomFieldService((GlobalPermissionManager) mockPermissionManager.proxy(),
                (CustomFieldManager) mockCustomFieldManager.proxy(), null, null)
        {

            void validateNotUsedInPermissionSchemes(JiraServiceContext jiraServiceContext, Long customFieldId, boolean forUpdate)
            {
                validateNotUsedInPermissionSchemes.called = true;
            }
        };
        // run validate
        defaultCustomFieldService.validateUpdate(jiraServiceContext, new Long(1), "customFieldName", null, null);
        // we expect the jira service context to contiain an error message
        jiraServiceContext.assertNoErrors();
        assertTrue(validateNotUsedInPermissionSchemes.called);
    }

    @Test
    public void testValidateNotUsedInPermissionSchemes()
    {
        Long customFieldID = new Long(1);
        DefaultCustomFieldService service = new DefaultCustomFieldService(null, null, null, null)
        {

            Set<GenericValue> getUsedPermissionSchemes(Long customFieldId)
            {
                return Collections.emptySet();
            }


            Set<GenericValue> getUsedIssueSecuritySchemes(Long customFieldId)
            {
                return Collections.emptySet();
            }
        };

        MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        service.validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldID, true);
        jiraServiceContext.assertNoErrors();
    }

    @Test
    public void testValidateNotUsedInPermissionSchemesWithPermissionsEnterprise()
    {
        Long customFieldID = new Long(1);
        DefaultCustomFieldService service = new DefaultCustomFieldService(null, null, null, null)
        {
            @Override
            Set<GenericValue> getUsedPermissionSchemes(Long customFieldId)
            {
                HashSet<GenericValue> set = new HashSet<GenericValue>();
                MockGenericValue mockGenericValue = new MockGenericValue("PermissionScheme", EasyMap.build("name", "TestPermScheme"));
                set.add(mockGenericValue);
                return set;
            }

            @Override
            Set<GenericValue> getUsedIssueSecuritySchemes(Long customFieldId)
            {
                HashSet<GenericValue> set = new HashSet<GenericValue>();
                MockGenericValue mockGenericValue = new MockGenericValue("IssueSecurityScheme", EasyMap.build("name", "IssueSecScheme"));
                set.add(mockGenericValue);
                return set;
            }
        };

        MockJiraServiceContext jiraServiceContext = new MockJiraServiceContext();
        // check for update
        service.validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldID, true);
        assertEquals(2, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        Iterator errorIterator = jiraServiceContext.getErrorCollection().getErrorMessages().iterator();
        assertEquals("Search Template cannot be set to 'None' because this custom field is used in the following Permission Scheme(s): TestPermScheme", errorIterator.next());
        assertEquals("Search Template cannot be set to 'None' because this custom field is used in the following Issue Level Security Scheme(s): IssueSecScheme", errorIterator.next());

        jiraServiceContext = new MockJiraServiceContext();
        // Check for delete
        service.validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldID, false);
        assertEquals(2, jiraServiceContext.getErrorCollection().getErrorMessages().size());
        errorIterator = jiraServiceContext.getErrorCollection().getErrorMessages().iterator();
        assertEquals("Custom field cannot be deleted because it is used in the following Permission Scheme(s): TestPermScheme", errorIterator.next());
        assertEquals("Custom field cannot be deleted because it is used in the following Issue Level Security Scheme(s): IssueSecScheme", errorIterator.next());
    }

    private class MutableBoolean
    {
        public boolean called = false;
    }
}
