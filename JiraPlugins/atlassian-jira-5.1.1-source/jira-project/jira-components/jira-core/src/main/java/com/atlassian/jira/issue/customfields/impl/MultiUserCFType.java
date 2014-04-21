package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.UserCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.MultiUserConverter;
import com.atlassian.jira.issue.customfields.impl.rest.MultiUserCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareCustomFieldType;
import com.atlassian.jira.issue.fields.rest.RestCustomFieldTypeOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.notification.type.UserCFNotificationTypeAware;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>Multiple User Type allows selection of multiple users.  For single User select use {@link UserCFType}</p>
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link java.util.Collection}</dd>
 * <dt><strong>Singular Object Type</strong></dt>
 * <dd>{@link User}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link String} of user name</dd>
 * </dl>
 */
public class MultiUserCFType extends AbstractMultiCFType<User> implements UserCFNotificationTypeAware, ProjectImportableCustomField, UserField, SortableCustomField<String>, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    protected final MultiUserConverter multiUserConverter;
    private ApplicationProperties applicationProperties;
    private JiraAuthenticationContext authenticationContext;
    private UserPickerSearchService searchService;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final UserCustomFieldImporter userCustomFieldImporter;
    private final JiraBaseUrls jiraBaseUrls;


    public MultiUserCFType(CustomFieldValuePersister customFieldValuePersister,
            GenericConfigManager genericConfigManager, MultiUserConverter multiUserConverter, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, UserPickerSearchService searchService,
            final FieldVisibilityManager fieldVisibilityManager, JiraBaseUrls jiraBaseUrls)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.multiUserConverter = multiUserConverter;
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.userCustomFieldImporter = new UserCustomFieldImporter();
    }

    protected Comparator<User> getTypeComparator()
    {
        return new UserBestNameComparator(authenticationContext.getLocale());
    }

    public Collection<User> getValueFromCustomFieldParams(CustomFieldParams parameters) throws FieldValidationException
    {
        Collection userSet = new HashSet();
        Collection values = parameters.getValuesForNullKey();
        if (values == null || values.isEmpty())
        {
            return null;
        }
        for (Object value : values)
        {
            userSet.addAll(convertDbObjectToTypes(new ArrayList<Object>(multiUserConverter.extractUserStringsFromString((String) value))));
        }
        List l = new ArrayList(userSet);
        Collections.sort(l, new UserBestNameComparator(authenticationContext.getLocale()));
        return l;
    }

    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters)
    {
        Collection users = parameters.getValuesForNullKey();
        if (users == null || users.isEmpty())
        {
            return null;
        }

        return putInvalidUsersAtFront(users);
    }

    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config)
    {
        StringBuffer errors = null;
        Collection userStrings;
        String user;
        String singleParam;
        for (Iterator it = relevantParams.getValuesForNullKey().iterator(); it.hasNext();)
        {
            singleParam = (String) it.next();

            userStrings = multiUserConverter.extractUserStringsFromString(singleParam);

            if (userStrings == null)
            {
                return;
            }
            for (Iterator i = userStrings.iterator(); i.hasNext();)
            {
                user = (String) i.next();
                try
                {
                    multiUserConverter.getUser(user);
                }
                catch (FieldValidationException e)
                {
                    if (errors == null)
                    {
                        errors = new StringBuffer(user);
                    }
                    else
                    {
                        errors.append(", ").append(user);
                    }
                }
                if (errors != null)
                {
                    errorCollectionToAddTo.addError(config.getCustomField().getId(), getI18nBean().getText("admin.errors.could.not.find.usernames", errors), Reason.VALIDATION_FAILED);
                }
            }
        }
    }

    @Override
    public String getStringFromSingularObject(User o)
    {
        return multiUserConverter.getString(o);
    }

    @Override
    public User getSingularObjectFromString(String s) throws FieldValidationException
    {
        return multiUserConverter.getUser(s);
    }

    @NotNull
    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    public List getRelatedIndexers(final CustomField customField)
    {
        return EasyList.build(new MultiUserCustomFieldIndexer(fieldVisibilityManager, customField, multiUserConverter));
    }

    @Override
    protected Object convertTypeToDbValue(User value)
    {
        return multiUserConverter.getString(value);
    }

    @Override
    protected User convertDbValueToType(Object string)
    {
        return multiUserConverter.getUserEvenWhenUnknown((String) string);
    }

    private Collection putInvalidUsersAtFront(Collection users)
    {
        final HashSet retSet = new HashSet();
        StringBuffer errorString = null;
        String userString;
        if (users != null)
        {
            for (Iterator it = users.iterator(); it.hasNext();)
            {
                String userList = (String) it.next();
                for (Iterator innerIt = multiUserConverter.extractUserStringsFromString(userList).iterator(); innerIt.hasNext();)
                {
                    userString = (String) innerIt.next();
                    try
                    {
                        multiUserConverter.getUser(userString);
                        retSet.add(userString);
                    }
                    catch (FieldValidationException e)
                    {
                        if (errorString == null)
                        {
                            errorString = new StringBuffer(userString);
                        }
                        else
                        {
                            errorString.append(", ").append(userString);
                        }
                    }
                }
            }
        }

        List l = new ArrayList(retSet);
        Collections.sort(l); //, NAME_COMPARATOR);
        if (errorString != null)
        {
            l.add(0, errorString.toString());
        }

        return l;
    }

    @NotNull
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> velocityParams = super.getVelocityParameters(issue, field, fieldLayoutItem);

        JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getLoggedInUser());

        boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);
        if (canPerformAjaxSearch)
        {
            velocityParams.put("canPerformAjaxSearch", "true");
            velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        WebResourceManager webResourceManager = ComponentAccessor.getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:autocomplete");
        return velocityParams;
    }

    //------------------------------------------------------------------------------------------------------------------
    //  Implementation of ProjectImportableCustomField
    //------------------------------------------------------------------------------------------------------------------
    public ProjectCustomFieldImporter getProjectImporter()
    {
        return this.userCustomFieldImporter;
    }
    //------------------------------------------------------------------------------------------------------------------

    static class MultiUserCustomFieldIndexer extends AbstractCustomFieldIndexer
    {
        private final CustomField customField;
        private final MultiUserConverter multiUserConverter;

        public MultiUserCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField, final MultiUserConverter multiUserConverter)
        {
            super(fieldVisibilityManager, customField);
            this.customField = customField;
            this.multiUserConverter = multiUserConverter;
        }

        public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
        {
            addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
        }

        public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
        {
            addDocumentFields(doc, issue, Field.Index.NO);
        }

        public void addDocumentFields(Document doc, Issue issue, final Field.Index indexType)
        {
            List o = (List) customField.getValue(issue);
            if (o != null)
            {
                for (Iterator iterator = o.iterator(); iterator.hasNext();)
                {
                    User user = (User) iterator.next();
                    String userId = CaseFolding.foldUsername(multiUserConverter.getString(user));
                    doc.add(new Field(getDocumentFieldId(), userId, Field.Store.YES, indexType));
                }
            }
        }
    }

    public int compare(@NotNull final String customFieldObjectValue1, @NotNull final String customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return customFieldObjectValue1.compareTo(customFieldObjectValue2);
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitMultiUser(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitMultiUser(MultiUserCFType multiUserCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        final String userPickerAutoCompleteUrl = String.format("%s/rest/api/1.0/users/picker?fieldName=%s&query=", jiraBaseUrls.baseUrl(), fieldTypeInfoContext.getOderableField().getId());
        return new FieldTypeInfo(null, userPickerAutoCompleteUrl);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.customArray(JsonType.USER_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        return new FieldJsonRepresentation(new JsonData(UserJsonBean.shortBeans(getValueFromIssue(field, issue), jiraBaseUrls)));
    }
    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return new MultiUserCustomFieldOperationsHandler(field, getI18nBean());
    }

    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field)
    {
        FieldConfig config = field.getRelevantConfig(issueCtx);
        return new JsonData(UserJsonBean.shortBeans(getDefaultValue(config), jiraBaseUrls));
    }
}
