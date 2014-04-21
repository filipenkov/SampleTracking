package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.AbstractVersionsSystemField;
import com.atlassian.jira.issue.fields.ComponentsSystemField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.PrioritySystemField;
import com.atlassian.jira.issue.fields.ResolutionSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContextImpl;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.ComponentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.PriorityJsonBean;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for {@link com.atlassian.jira.rest.v2.issue.FieldMetaBean} instances..
 *
 * @since v5.0
 */
public abstract class AbstractMetaFieldBeanBuilder
{
    protected final FieldLayoutManager fieldLayoutManager;
    protected Project project;
    protected IssueType issueType;
    protected User user;
    protected Issue issue;

    private final VersionBeanFactory versionBeanFactory;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ContextUriInfo contextUriInfo;
    protected JiraBaseUrls baseUrls;
    protected IncludedFields includeFields;

    public AbstractMetaFieldBeanBuilder(final FieldLayoutManager fieldLayoutManager, final Project project, final Issue issue, final IssueType issueType, final User user, VersionBeanFactory versionBeanFactory, VelocityRequestContextFactory velocityRequestContextFactory, ContextUriInfo contextUriInfo, JiraBaseUrls baseUrls)
    {
        this.fieldLayoutManager = fieldLayoutManager;
        this.project = project;
        this.issue = issue;
        this.issueType = issueType;
        this.user = user;
        this.versionBeanFactory = versionBeanFactory;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.contextUriInfo = contextUriInfo;
        this.baseUrls = baseUrls;
    }

    public void fieldsToInclude(IncludedFields includeFields)
    {
        this.includeFields = includeFields;
    }

    public Map<String, FieldMetaBean> build()
    {
        // Get all the fields for the given project and issue type
        final Map<String, FieldMetaBean> fields = new HashMap<String, FieldMetaBean>();

        if (hasPermissionToPerformOperation())
        {
            for (final FieldScreenRenderTab fieldScreenRenderTab : getFieldScreenRenderer(user, issue).getFieldScreenRenderTabs())
            {
                for (final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
                {
                    FieldLayoutItem fieldLayoutItem = fieldScreenRenderLayoutItem.getFieldLayoutItem();
                    final OrderableField field = fieldLayoutItem.getOrderableField();
                    if (field.isShown(issue))
                    {
                        String id = field.getId();
                        if (includeFields == null || includeFields.included(field))
                        {
                            FieldMetaBean fieldMetaBean = getFieldMetaBean(fieldLayoutItem.isRequired(), field, false);
                            fields.put(id, fieldMetaBean);
                        }
                    }
                }
            }
            addAdditionalFields(fields);
        }
        return fields;
    }

    protected void addAdditionalFields(Map<String, FieldMetaBean> fields)
    {
    }


    protected FieldMetaBean getFieldMetaBean(boolean required, OrderableField field, boolean forceRequired)
    {
        Collection<String> operations;
        if (field instanceof RestFieldOperations)
        {
            operations = ((RestFieldOperations) field).getRestFieldOperation().getSupportedOperations();
        }
        else
        {
            operations = Collections.emptyList();
        }
        Collection<?> allowedValues = null;
        FieldTypeInfo fieldTypeInfo;
        JsonType jsonType;

        if (field instanceof RestAwareField)
        {
            FieldTypeInfoContext fieldTypeInfoContext = new FieldTypeInfoContextImpl(field, issue, new IssueContextImpl(project.getId(), issueType.getId()), getOperationContext());
            fieldTypeInfo = ((RestAwareField) field).getFieldTypeInfo(fieldTypeInfoContext);
            jsonType =  getJsonType((RestAwareField) field);
            allowedValues = getAllowedValueBeans(field, fieldTypeInfo.getAllowedValues());
        }
        else if(field instanceof CustomField)
        {
            CustomField customField = (CustomField) field;
            fieldTypeInfo = new FieldTypeInfo(null, null);
            jsonType =  getJsonType(customField);
        }
        else
        {
            fieldTypeInfo = new FieldTypeInfo(null, null);
            jsonType =  null;
        }
        return new FieldMetaBean(required || forceRequired, jsonType, field.getName(), fieldTypeInfo.getAutoCompleteUrl(), operations, allowedValues);
    }

    protected JsonType getJsonType(RestAwareField field)
    {
        return field.getJsonSchema();
    }

    private JsonType getJsonType(CustomField field)
    {
        return JsonTypeBuilder.customArray(JsonType.STRING_TYPE, field.getCustomFieldType().getKey(), field.getIdAsLong());
    }

    protected Collection<?> getAllowedValueBeans(OrderableField field, Collection<?> allowedValues)
    {
        if (allowedValues == null)
        {
            return null;
        }
        if (field instanceof IssueTypeSystemField)
        {
            return IssueTypeJsonBean.shortBeans((Collection<IssueType>) allowedValues, baseUrls);
        }
        if (field instanceof PrioritySystemField)
        {
            return PriorityJsonBean.shortBeans((Collection<Priority>) allowedValues, baseUrls);
        }
        if (field instanceof AbstractVersionsSystemField)
        {
            return versionBeanFactory.createVersionBeans((Collection<? extends Version>) allowedValues, false);
        }
        if (field instanceof ComponentsSystemField)
        {
            return ComponentJsonBean.shortBeans((Collection<ProjectComponent>) allowedValues, baseUrls);
        }
        if (field instanceof ResolutionSystemField)
        {
            return ResolutionBean.asBeans((Collection<Resolution>) allowedValues, contextUriInfo, velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl());
        }
        // Fallback to just return what comes in.
        return allowedValues;
    }

    public abstract OperationContext getOperationContext();

    public abstract boolean hasPermissionToPerformOperation();

    abstract FieldScreenRenderer getFieldScreenRenderer(User user, Issue issue);
}
