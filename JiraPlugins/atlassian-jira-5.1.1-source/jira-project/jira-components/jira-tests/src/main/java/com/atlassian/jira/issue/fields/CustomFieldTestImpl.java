package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;
import org.ofbiz.core.entity.GenericValue;

/**
 * The only purpose of this class is to increase the visibility of #copyGenericValue so we can mock it in tests.
 */
public class CustomFieldTestImpl extends CustomFieldImpl
{
    public CustomFieldTestImpl(GenericValue customField, CustomFieldManager customFieldManager, JiraAuthenticationContext authenticationContext, ConstantsManager constantsManager, FieldConfigSchemeManager fieldConfigSchemeManager, PermissionManager permissionManager, RendererManager rendererManager, FieldConfigSchemeClauseContextUtil contextUtil, CustomFieldDescription customFieldDescription, I18nHelper.BeanFactory i18nFactory)
    {
        super(customField, customFieldManager, authenticationContext, constantsManager, fieldConfigSchemeManager, permissionManager, rendererManager, contextUtil, customFieldDescription, i18nFactory);
    }

    public CustomFieldTestImpl(CustomField customField)
    {
        super(customField);
    }

    public CustomFieldTestImpl(CustomFieldImpl customField)
    {
        super(customField);
    }

    @Override
    public GenericValue copyGenericValue()
    {
        return super.copyGenericValue();
    }
}
