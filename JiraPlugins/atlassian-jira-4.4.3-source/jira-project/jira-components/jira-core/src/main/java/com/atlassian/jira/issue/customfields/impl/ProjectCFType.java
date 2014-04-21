package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.ProjectPickerCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.MultipleCustomFieldType;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.ProjectOptionsConfigItem;
import com.atlassian.jira.issue.customfields.converters.ProjectConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.option.GenericImmutableOptions;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectCFType extends AbstractSingleFieldType implements MultipleCustomFieldType, SortableCustomField, ProjectImportableCustomField
{
    private final Logger log = Logger.getLogger(ProjectCFType.class);
    private final ProjectConverter projectConverter;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectCustomFieldImporter projectCustomFieldImporter;

    public ProjectCFType(CustomFieldValuePersister customFieldValuePersister, ProjectConverter projectConverter, PermissionManager permissionManager,
                         JiraAuthenticationContext jiraAuthenticationContext, GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.projectConverter = projectConverter;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.projectCustomFieldImporter = new ProjectPickerCustomFieldImporter();
    }

    public int compare(Object customFieldObjectValue1, Object customFieldObjectValue2, FieldConfig fieldConfig)
    {
        return OfBizComparators.NAME_COMPARATOR.compare((GenericValue)customFieldObjectValue1, (GenericValue)customFieldObjectValue2);
    }

    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_DECIMAL;
    }

    protected Object getDbValueFromObject(Object customFieldObject)
    {
        if (customFieldObject == null)
            return null;

        assertObjectImplementsType(GenericValue.class, customFieldObject);
        final GenericValue project = (GenericValue) customFieldObject;
        return new Double(project.getLong("id").longValue());
    }

    protected Object getObjectFromDbValue(Object databaseValue) throws FieldValidationException
    {
        Double projectId = (Double) databaseValue;
        return projectConverter.getProject(new Long(projectId.intValue()));
    }

    public String getStringFromSingularObject(Object customFieldObject)
    {
        assertObjectImplementsType(GenericValue.class, customFieldObject);
        return projectConverter.getString((GenericValue) customFieldObject);
    }

    public Object getSingularObjectFromString(String string) throws FieldValidationException
    {
        return projectConverter.getProject(string);
    }

    public List getConfigurationItemTypes()
    {
        final List configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new ProjectOptionsConfigItem(projectConverter, permissionManager, jiraAuthenticationContext));
        return configurationItemTypes;
    }


    /**
     * @return A list of GenericValues representing projects
     */
    public Options getOptions(FieldConfig config, JiraContextNode jiraContextNode)
    {
        try
        {
            List originalList = new ArrayList(permissionManager.getProjects(Permissions.BROWSE, jiraAuthenticationContext.getUser()));
            return new GenericImmutableOptions(originalList, config);
        }
        catch (UnsupportedOperationException e)
        {
            log.error("Unable to retreive projects. Likely to be an issue with SubvertedPermissionManager. Please restart to resolve the problem.", e);
            return null;
        }
    }

    public String getChangelogString(CustomField field, Object value)
    {
        if(value == null)
            return null;
        else
            return ((GenericValue)value).getString("name");
    }

    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
        if (issue != null)
        {
            final GenericValue project = (GenericValue) getValueFromIssue(field, issue);
            if (project != null)
            {
                boolean hasPermission = permissionManager.hasPermission(Permissions.BROWSE, project, jiraAuthenticationContext.getUser());
                params.put("isProjectVisible", hasPermission ? Boolean.TRUE : Boolean.FALSE);
            }
        }
        return params;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return this.projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitProject(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitProject(ProjectCFType projectCustomFieldType);
    }
}
