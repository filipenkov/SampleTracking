package com.atlassian.jira.jql.permission;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.jql.context.ClauseContextImpl;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;

/**
 * @since v4.0
 */
public class TestCustomFieldClausePermissionChecker extends MockControllerTestCase
{
    private CustomField customField;
    private FieldManager fieldManager;
    private FieldConfigSchemeClauseContextUtil contextUtil;

    @Before
    public void setUp() throws Exception
    {
        customField = mockController.getMock(CustomField.class);
        fieldManager = mockController.getMock(FieldManager.class);
        contextUtil = mockController.getMock(FieldConfigSchemeClauseContextUtil.class);

    }

    @Test
    public void testFieldIsHidden() throws Exception
    {
        fieldManager.isFieldHidden((User) null,customField);
        mockController.setReturnValue(true);
        mockController.replay();

        final CustomFieldClausePermissionChecker permissionChecker = new CustomFieldClausePermissionChecker(customField, fieldManager, contextUtil);

        final boolean hasPermission = permissionChecker.hasPermissionToUseClause(null);
        assertFalse("User should not see this custom field",hasPermission);
        mockController.verify();
    }

    @Test
    public void testFieldNotHiddenTwoConfigSchemesOneVisible() throws Exception
    {
       final FieldConfigScheme configScheme1 = mockController.getMock(FieldConfigScheme.class);
       final FieldConfigScheme configScheme2 = mockController.getMock(FieldConfigScheme.class);

       fieldManager.isFieldHidden((User) null,customField);
       mockController.setReturnValue(false);

       customField.getConfigurationSchemes();

       mockController.setReturnValue(CollectionBuilder.newBuilder(configScheme1,configScheme2).asList());

       contextUtil.getContextForConfigScheme(null,configScheme1);
       mockController.setReturnValue(new ClauseContextImpl());

       contextUtil.getContextForConfigScheme(null,configScheme2);
       mockController.setReturnValue(ClauseContextImpl.createGlobalClauseContext());

       mockController.replay();

       final CustomFieldClausePermissionChecker permissionChecker = new CustomFieldClausePermissionChecker(customField, fieldManager, contextUtil);

       final boolean hasPermission = permissionChecker.hasPermissionToUseClause(null);

       assertTrue("User has permissions to see this custom field",hasPermission);

       mockController.verify();
    }

    @Test
    public void testFieldNotHiddenTwoConfigSchemesNoneVisible() throws Exception
    {
       final FieldConfigScheme configScheme1 = mockController.getMock(FieldConfigScheme.class);
       final FieldConfigScheme configScheme2 = mockController.getMock(FieldConfigScheme.class);

       fieldManager.isFieldHidden((User) null,customField);
       mockController.setReturnValue(false);

       customField.getConfigurationSchemes();

       mockController.setReturnValue(CollectionBuilder.newBuilder(configScheme1,configScheme2).asList());

       contextUtil.getContextForConfigScheme(null,configScheme1);
       mockController.setReturnValue(new ClauseContextImpl());

       contextUtil.getContextForConfigScheme(null,configScheme2);
       mockController.setReturnValue(new ClauseContextImpl());

       mockController.replay();

       final CustomFieldClausePermissionChecker permissionChecker = new CustomFieldClausePermissionChecker(customField, fieldManager, contextUtil);

       final boolean hasPermission = permissionChecker.hasPermissionToUseClause(null);

       assertFalse("User has no permissions to see this custom field",hasPermission);

       mockController.verify();
    }

    @Test
    public void testFieldNotHiddenNoConfigSchemes() throws Exception
    {
       fieldManager.isFieldHidden((User) null,customField);
       mockController.setReturnValue(false);

       customField.getConfigurationSchemes();

       mockController.setReturnValue(Collections.emptyList());

       mockController.replay();

       final CustomFieldClausePermissionChecker permissionChecker = new CustomFieldClausePermissionChecker(customField, fieldManager, contextUtil);

       final boolean hasPermission = permissionChecker.hasPermissionToUseClause(null);

       assertFalse("User has no permissions to see this custom field",hasPermission);

       mockController.verify();
    }
}
