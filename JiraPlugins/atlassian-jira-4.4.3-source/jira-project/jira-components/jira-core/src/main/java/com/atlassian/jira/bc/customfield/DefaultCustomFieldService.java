package com.atlassian.jira.bc.customfield;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupCF;
import com.atlassian.jira.security.type.UserCF;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.ofbiz.GenericValueUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashSet;
import java.util.Set;

/** 
 * @since v3.13
 */
public class DefaultCustomFieldService implements CustomFieldService
{
    private static final String CUSTOMFIELD_PREFIX = "customfield_";
    private static final String NONE_VALUE = "-1";

    private final GlobalPermissionManager permissionManager;
    private final CustomFieldManager customFieldManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final IssueSecuritySchemeManager issueSecuritySchemeManager;

    public DefaultCustomFieldService(final GlobalPermissionManager permissionManager, final CustomFieldManager customFieldManager, final PermissionSchemeManager permissionSchemeManager, final IssueSecuritySchemeManager issueSecuritySchemeManager)
    {
        this.permissionManager = permissionManager;
        this.customFieldManager = customFieldManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
    }

    public void validateDelete(final JiraServiceContext jiraServiceContext, final Long customFieldId)
    {
        final I18nHelper i18nBean = jiraServiceContext.getI18nBean();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, jiraServiceContext.getUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.customfields.service.no.admin.permission"));
            return;
        }

        final CustomField customField = customFieldManager.getCustomFieldObject(customFieldId);
        if (customField == null)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.errors.customfields.invalid.custom.field"));
            return;
        }
        validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldId, false);
    }

    public void validateUpdate(final JiraServiceContext jiraServiceContext, final Long customFieldId, final String name, final String description, final String searcherKey)
    {
        if (customFieldId == null)
        {
            throw new IllegalArgumentException("customFieldId can not be null.");
        }

        final I18nHelper i18nBean = jiraServiceContext.getI18nBean();
        if (!permissionManager.hasPermission(Permissions.ADMINISTER, jiraServiceContext.getUser()))
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.customfields.service.no.admin.permission"));
            return;
        }

        //ensure we're not updating a custom field that doesn't exist.
        final CustomField originalCustomField = customFieldManager.getCustomFieldObject(customFieldId);
        if (originalCustomField == null)
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(i18nBean.getText("admin.errors.customfields.invalid.custom.field"));
            return;
        }

        if (StringUtils.isEmpty(name))
        {
            jiraServiceContext.getErrorCollection().addError("name", i18nBean.getText("admin.errors.customfields.no.name"));
            return;
        }

        if (StringUtils.isNotEmpty(searcherKey) && !NONE_VALUE.equals(searcherKey) && (customFieldManager.getCustomFieldSearcher(searcherKey) == null))
        {
            jiraServiceContext.getErrorCollection().addError("searcher", i18nBean.getText("admin.errors.customfields.invalid.searcher"));
            return;
        }

        //setting the searcher to none...we need to check if it is used anywhere in a permission or issuelevel scheme.
        //see JRA-13808 for more info.
        if (StringUtils.isEmpty(searcherKey) || NONE_VALUE.equals(searcherKey))
        {
            validateNotUsedInPermissionSchemes(jiraServiceContext, customFieldId, true);
        }
    }

    /**
     * This method checks that the custom field provided is not used in any permission schemes or issue level security
     * schemes.  If it is used, an appropriate error message with a complete list of scheme names will be added to the
     * JiraServiceContext.
     *
     * @param jiraServiceContext JiraServiceContext
     * @param customFieldId      ID of the CustomField
     * @param forUpdate          used to determine the correct error message
     */
    void validateNotUsedInPermissionSchemes(final JiraServiceContext jiraServiceContext, final Long customFieldId, final boolean forUpdate)
    {
        final I18nHelper i18nBean = jiraServiceContext.getI18nBean();
        // We need to find if this is used in any security schemes.
        final Set<GenericValue> usedPermissionSchemes = getUsedPermissionSchemes(customFieldId);
        // if we found any permissionSchemes that use this custom field, show an error.
        if (!usedPermissionSchemes.isEmpty())
        {
            String messageKey;
            if (forUpdate)
            {
                messageKey = "admin.errors.customfields.used.in.permission.scheme.update";
            }
            else
            {
                messageKey = "admin.errors.customfields.used.in.permission.scheme.delete";
            }
            jiraServiceContext.getErrorCollection().addErrorMessage(
                i18nBean.getText(messageKey, GenericValueUtils.getCommaSeparatedList(usedPermissionSchemes, "name")));
        }
        //issuelevel security checks should only be done in enterprise edition!
        final Set<GenericValue> usedIssueLevelSecuritySchemes = getUsedIssueSecuritySchemes(customFieldId);
        // if we found any IssueLevelSecuritySchemes that use this Custom Field - then show error.
        if (!usedIssueLevelSecuritySchemes.isEmpty())
        {
            String messageKey;
            if (forUpdate)
            {
                messageKey = "admin.errors.customfields.used.in.issuelevelschemes.update";
            }
            else
            {
                messageKey = "admin.errors.customfields.used.in.issuelevelschemes.delete";
            }
            jiraServiceContext.getErrorCollection().addErrorMessage(
                i18nBean.getText(messageKey, GenericValueUtils.getCommaSeparatedList(usedIssueLevelSecuritySchemes, "name")));
        }
    }

    /**
     * Finds all the permission schemes where this customfield is used in a particular permission
     *
     * @param customFieldId ID of the CustomField
     * @return Set of {@link org.ofbiz.core.entity.GenericValue}s
     */
    Set<GenericValue> getUsedPermissionSchemes(final Long customFieldId)
    {
        final Set<GenericValue> ret = new HashSet<GenericValue>();
        //Note: This is a bit of a hack, since there may be other custom field types than user and group, however
        // currently there's no better way of doing this, as there's no way to figure out the type from a customFieldId
        ret.addAll(permissionSchemeManager.getSchemesContainingEntity(UserCF.TYPE, CUSTOMFIELD_PREFIX + customFieldId));
        ret.addAll(permissionSchemeManager.getSchemesContainingEntity(GroupCF.TYPE, CUSTOMFIELD_PREFIX + customFieldId));
        return ret;
    }

    /**
     * Finds all the issue level security schemes where this customfield is used in a particular issue level.
     *
     * @param customFieldId ID of the CustomField
     * @return Set of {@link org.ofbiz.core.entity.GenericValue}s
     */
    Set<GenericValue> getUsedIssueSecuritySchemes(final Long customFieldId)
    {
        final Set<GenericValue> ret = new HashSet<GenericValue>();
        //Note: This is a bit of a hack, since there may be other custom field types than user and group, however
        // currently there's no better way of doing this, as there's no way to figure out the type from a customFieldId
        ret.addAll(issueSecuritySchemeManager.getSchemesContainingEntity(UserCF.TYPE, CUSTOMFIELD_PREFIX + customFieldId));
        ret.addAll(issueSecuritySchemeManager.getSchemesContainingEntity(GroupCF.TYPE, CUSTOMFIELD_PREFIX + customFieldId));
        return ret;
    }
}
