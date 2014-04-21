package com.atlassian.jira.security.type;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestGroupCF extends ListeningTestCase
{
    private static final String CUSTOMFIELDID = "customfield_10000";

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
        assertEquals("Please select a valid group custom field.", jiraServiceContext.getErrorCollection().getErrorMessages().iterator().next());
    }

    private void testDoValidation(CustomFieldSearcher customFieldSearcher, String customFieldId, JiraServiceContext jiraServiceContext)
    {
        Map parameters = new HashMap();
        parameters.put("groupCF", customFieldId);

        Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.expectAndReturn("getCustomFieldSearcher", P.ANY_ARGS, customFieldSearcher);
        mockCustomField.expectAndReturn("getName", P.ANY_ARGS, "TestCustomField");

        Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", new Constraint[] { P.eq(CUSTOMFIELDID) }, (CustomField) mockCustomField.proxy());

        final GroupCF groupCF = new GroupCF(null, null, (CustomFieldManager) mockCustomFieldManager.proxy());
        groupCF.doValidation(null, parameters, jiraServiceContext);
    }

}
