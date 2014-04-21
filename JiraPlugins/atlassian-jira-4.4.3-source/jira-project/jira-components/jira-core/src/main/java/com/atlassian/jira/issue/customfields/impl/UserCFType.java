package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.UserCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.notification.type.UserCFNotificationTypeAware;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.user.User;

import java.util.Map;

public class UserCFType extends StringCFType implements SortableCustomField<User>, UserCFNotificationTypeAware, ProjectImportableCustomField, UserField
{
    private final ProjectCustomFieldImporter userCustomFieldImporter = new UserCustomFieldImporter();
    private final UserConverter userConverter;
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext authenticationContext;
    private final UserPickerSearchService searchService;

    public UserCFType(final CustomFieldValuePersister customFieldValuePersister, final UserConverter userConverter, final GenericConfigManager genericConfigManager, final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext, final UserPickerSearchService searchService)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.userConverter = userConverter;
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
    }

    public String getStringFromSingularObject(final Object value)
    {
        assertObjectImplementsType(User.class, value);
        return userConverter.getString((User) value);
    }

    public Object getSingularObjectFromString(final String string) throws FieldValidationException
    {
        return userConverter.getUser(string);
    }

    @Override
    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        try
        {
            super.getValueFromCustomFieldParams(relevantParams);
        }
        catch (final FieldValidationException e)
        {
            errorCollectionToAddTo.addError(config.getCustomField().getId(), e.getMessage());
        }
    }

    @Override
    public Object getValueFromCustomFieldParams(final CustomFieldParams relevantParams) throws FieldValidationException
    {
        Object value = null;
        try
        {
            value = super.getValueFromCustomFieldParams(relevantParams);
        }
        catch (final FieldValidationException e)
        {
            //ignore
        }

        return value;

    }

    public int compare(final User customFieldObjectValue1, final User customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return new UserBestNameComparator(authenticationContext.getLocale()).compare(customFieldObjectValue1, customFieldObjectValue2);
    }

    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> velocityParams = super.getVelocityParameters(issue, field, fieldLayoutItem);

        final JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getUser());

        final boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);
        if (canPerformAjaxSearch)
        {
            velocityParams.put("canPerformAjaxSearch", "true");
        }
        final WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:autocomplete");
        velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        return velocityParams;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return userCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor) {
            return ((Visitor) visitor).visitUser(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitUser(UserCFType userCustomFieldType);
    }
}
