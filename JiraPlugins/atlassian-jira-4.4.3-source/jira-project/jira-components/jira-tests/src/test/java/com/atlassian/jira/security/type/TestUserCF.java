package com.atlassian.jira.security.type;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.permission.MockPermissionContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestUserCF extends MockControllerTestCase
{
    private static final String CUSTOMFIELDID = "customfield_10000";

    @Test
    public void testConvertToValueSetNull() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);
        final Set ret = userCF.convertToValueSet(null);
        assertNotNull(ret);
        assertTrue(ret.isEmpty());
    }

    @Test
    public void testConvertToValueSetObject() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);
        final Object object = new Object();
        final Set ret = userCF.convertToValueSet(object);
        assertNotNull(ret);
        assertEquals(1, ret.size());
        assertTrue(ret.contains(object));
    }

    @Test
    public void testConvertToValueSetString() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);
        final Set ret = userCF.convertToValueSet("test");
        assertNotNull(ret);
        assertEquals(1, ret.size());
        assertTrue(ret.contains("test"));
    }

    @Test
    public void testConvertToValueSetCollections() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);

        List list = EasyList.build("test", "this");
        Set ret = userCF.convertToValueSet(list);
        assertEquals(2, ret.size());
        assertTrue(ret.contains("test"));
        assertTrue(ret.contains("this"));

        Set set = new HashSet();
        set.add("test");
        set.add("this");
        ret = userCF.convertToValueSet(set);
        assertEquals(2, ret.size());
        assertTrue(ret.contains("test"));
        assertTrue(ret.contains("this"));
    }

    @Test
    public void testConvertToValueMap() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);

        Map map = EasyMap.build("1", "test");
        final Set ret = userCF.convertToValueSet(map);
        assertEquals(1, ret.size());
        assertTrue(ret.contains(map)); // maps are not handled at the moment
    }

    @Test
    public void testConvertToValueArray() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);

        String[] array = { "1", "test" };
        final Set ret = userCF.convertToValueSet(array);
        assertEquals(1, ret.size());
        assertTrue(ret.contains(array)); // arrays are not handled at the moment
    }

    @Test
    public void testDoValidationWithCustomFieldWithSearcher()
    {
        Mock mockCustomFieldSearcher = new Mock(CustomFieldSearcher.class);
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };
        testDoValidation((CustomFieldSearcher) mockCustomFieldSearcher.proxy(), CUSTOMFIELDID, jiraServiceContext);
        assertFalse(jiraServiceContext.getErrorCollection().hasAnyErrors());
    }

    @Test
    public void testDoValidationWithCustomFieldWithoutSearcher()
    {
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };
        testDoValidation(null, CUSTOMFIELDID, jiraServiceContext);
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("Custom field 'TestCustomField' is not indexed for searching - please add a Search Template to this Custom Field.",
                jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testDoValidationWithNoCustomField()
    {
        JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(null, new SimpleErrorCollection())
        {
            public I18nHelper getI18nBean()
            {
                return new MockI18nBean();
            }
        };
        testDoValidation(null, null, jiraServiceContext);
        assertTrue(jiraServiceContext.getErrorCollection().hasAnyErrors());
        assertEquals("Please select a valid user custom field.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    private void testDoValidation(CustomFieldSearcher customFieldSearcher, String customFieldId, JiraServiceContext jiraServiceContext)
    {
        Map parameters = new HashMap();
        parameters.put("userCF", customFieldId);

        Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.expectAndReturn("getCustomFieldSearcher", P.ANY_ARGS, customFieldSearcher);
        mockCustomField.expectAndReturn("getName", P.ANY_ARGS, "TestCustomField");

        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(CUSTOMFIELDID) }, (CustomField) mockCustomField.proxy());

        final UserCF userCF = new UserCF(null, (CustomFieldManager) mockCustomFieldManager.proxy());
        userCF.doValidation(null, parameters, jiraServiceContext);
    }

    @Test
    public void testGetUsersNoIssueInContext() throws Exception
    {
        final UserCF userCF = new UserCF(null, null);
        final Set<com.opensymphony.user.User> users = userCF.getUsers(new MockPermissionContext(null, null, null), "customField");
        assertTrue(users.isEmpty());
    }

    @Test
    public void testGetUsersIssueInContext() throws Exception
    {
        final Object expectedValue = new Object();
        final CustomField mockCustomField = mockController.getMock(CustomField.class);
        final MockIssue mockIssue = new MockIssue();
        final FieldManager fieldManager = mockController.getMock(FieldManager.class);

        fieldManager.getCustomField("customField");
        mockController.setReturnValue(mockCustomField);

        final UserCF userCF = new UserCF(null, null)
        {
            FieldManager getFieldManager()
            {
                return fieldManager;
            }

            Object getValuesFromIssue(final CustomField field, final Issue issue)
            {
                assertEquals(mockIssue, issue);
                return expectedValue;
            }
        };

        mockController.replay();

        final Set users = userCF.getUsers(new MockPermissionContext(mockIssue, null, null), "customField");
        assertEquals(1, users.size());
        assertEquals(expectedValue, users.iterator().next());

        mockController.verify();
    }
}
