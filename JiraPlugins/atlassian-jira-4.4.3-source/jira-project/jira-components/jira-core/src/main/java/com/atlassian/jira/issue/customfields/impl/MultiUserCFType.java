package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.core.util.collection.EasyList;
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
import com.atlassian.jira.issue.customfields.converters.MultiUserConverter;
import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.notification.type.UserCFNotificationTypeAware;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.CaseFolding;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.opensymphony.user.User;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>Multiple Select Type</p>
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link java.util.Collection}</dd>
 * <dt><strong>Singular Object Type</strong></dt>
 * <dd>{@link String}</dd>
 * </dl>
 */
public class MultiUserCFType extends AbstractMultiCFType implements UserCFNotificationTypeAware, ProjectImportableCustomField, UserField
{
    protected final MultiUserConverter multiUserConverter;
    private ApplicationProperties applicationProperties;
    private JiraAuthenticationContext authenticationContext;
    private UserPickerSearchService searchService;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final UserCustomFieldImporter userCustomFieldImporter;

    public MultiUserCFType(CustomFieldValuePersister customFieldValuePersister, StringConverter stringConverter,
            GenericConfigManager genericConfigManager, MultiUserConverter multiUserConverter, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, UserPickerSearchService searchService,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(customFieldValuePersister, stringConverter, genericConfigManager);
        this.multiUserConverter = multiUserConverter;
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.userCustomFieldImporter = new UserCustomFieldImporter();
    }

    protected Comparator getTypeComparator()
    {
        return new UserBestNameComparator(authenticationContext.getLocale());
    }

    public Object getValueFromCustomFieldParams(CustomFieldParams parameters) throws FieldValidationException
    {
        Collection userSet = new HashSet();
        Collection values = parameters.getValuesForNullKey();
        if (values == null || values.isEmpty())
        {
            return null;
        }
        for (Iterator i = values.iterator(); i.hasNext();)
        {
            userSet.addAll(convertStringsToTypes(multiUserConverter.extractUserStringsFromString((String) i.next())));
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
                    errorCollectionToAddTo.addError(config.getCustomField().getId(), getI18nBean().getText("admin.errors.could.not.find.usernames", errors));
                }
            }
        }
    }

    public String getStringFromSingularObject(Object o)
    {
        return multiUserConverter.getString((User) o);
    }

    public Object getSingularObjectFromString(String s) throws FieldValidationException
    {
        return multiUserConverter.getUser(s);
    }

    public List getRelatedIndexers(final CustomField customField)
    {
        return EasyList.build(new MultiUserCustomFieldIndexer(fieldVisibilityManager, customField, multiUserConverter));
    }

    protected String convertTypeToString(Object value)
    {
        return multiUserConverter.getString((User) value);
    }


    protected Object convertStringToType(String string)
    {
        return multiUserConverter.getUser(string);
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

    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> velocityParams = super.getVelocityParameters(issue, field, fieldLayoutItem);

        JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getUser());

        boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);
        if (canPerformAjaxSearch)
        {
            velocityParams.put("canPerformAjaxSearch", "true");
            velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
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
            addDocumentFields(doc, issue, Field.Index.UN_TOKENIZED);
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
                    String userId = CaseFolding.foldString(multiUserConverter.getString(user), Locale.ENGLISH);
                    doc.add(new Field(getDocumentFieldId(), userId, Field.Store.YES, indexType));
                }
            }
        }
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
}
