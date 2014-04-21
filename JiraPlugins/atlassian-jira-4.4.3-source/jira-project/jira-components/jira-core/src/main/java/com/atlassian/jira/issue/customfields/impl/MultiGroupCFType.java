package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.imports.project.customfield.GroupCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.GroupSelectorField;
import com.atlassian.jira.issue.customfields.converters.MultiGroupConverter;
import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.GroupNameComparator;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.opensymphony.user.Group;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Multiple User Group Select Type</p>
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link java.util.Collection} of {@link com.opensymphony.user.Group}s</dd>
 * </dl>
 */
public class MultiGroupCFType extends AbstractMultiCFType<Group> implements GroupSelectorField, ProjectImportableCustomField, UserField
{
    protected final MultiGroupConverter multiGroupConverter;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final FieldVisibilityManager fieldVisibilityManager;
    private static final Comparator<Group> NAME_COMPARATOR = new GroupNameComparator();

    private static final String MULTIPLE_PARAM_KEY = "multiple";
    private final GroupCustomFieldImporter groupCustomFieldImporter;

    public MultiGroupCFType(final CustomFieldValuePersister customFieldValuePersister, final StringConverter stringConverter, final GenericConfigManager genericConfigManager, final MultiGroupConverter multiGroupConverter, final PermissionManager permissionManager, final JiraAuthenticationContext authenticationContext, final GroupManager groupManager, final FieldVisibilityManager fieldVisibilityManager)

    {
        super(customFieldValuePersister, stringConverter, genericConfigManager);
        this.multiGroupConverter = multiGroupConverter;
        this.permissionManager = permissionManager;
        this.authenticationContext = authenticationContext;
        this.fieldVisibilityManager = fieldVisibilityManager;
        groupCustomFieldImporter = new GroupCustomFieldImporter(groupManager);
    }

    @Override
    protected Comparator<Group> getTypeComparator()
    {
        return NAME_COMPARATOR;
    }

    @Override
    public Object getValueFromCustomFieldParams(final CustomFieldParams parameters) throws FieldValidationException
    {
        final Collection groupSet = new HashSet();
        final Collection values = parameters.getValuesForNullKey();
        if ((values == null) || values.isEmpty())
        {
            return null;
        }
        for (final Iterator i = values.iterator(); i.hasNext();)
        {
            Collection groupNames;
            if (isMultiple())
            {
                groupNames = multiGroupConverter.extractGroupStringsFromString((String) i.next());
            }
            else
            {
                groupNames = EasyList.build(i.next());
            }

            groupSet.addAll(convertStringsToTypes(groupNames));
        }
        final List l = new ArrayList(groupSet);
        Collections.sort(l, NAME_COMPARATOR);
        return l;
    }

    @Override
    public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
    {
        final Collection groups = parameters.getValuesForNullKey();
        if ((groups == null) || groups.isEmpty())
        {
            return null;
        }

        return putInvalidGroupsAtFront(groups);
    }

    @Override
    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        final StringBuffer errors = new StringBuffer();
        Collection groupStrings;
        String group;
        String singleParam;
        for (final Iterator it = relevantParams.getValuesForNullKey().iterator(); it.hasNext();)
        {
            singleParam = (String) it.next();

            if (isMultiple())
            {
                groupStrings = multiGroupConverter.extractGroupStringsFromString(singleParam);
            }
            else
            {
                groupStrings = EasyList.build(singleParam);
            }

            if (groupStrings == null)
            {
                return;
            }
            for (final Iterator i = groupStrings.iterator(); i.hasNext();)
            {
                group = (String) i.next();
                try
                {
                    multiGroupConverter.getGroup(group);
                }
                catch (final FieldValidationException e)
                {
                    if (errors.length() > 0)
                    {
                        errors.append(", ");
                    }

                    errors.append(group);
                }
            }

            if ((errors != null) && (errors.length() > 0))
            {
                String message;
                if (isMultiple())
                {
                    message = getI18nBean().getText("admin.errors.could.not.find.groupnames", errors);
                }
                else
                {
                    message = getI18nBean().getText("admin.errors.could.not.find.groupname", errors);
                }

                errorCollectionToAddTo.addError(config.getCustomField().getId(), message);
            }
        }
    }

    @Override
    public String getStringFromSingularObject(final Object o)
    {
        return multiGroupConverter.getString((Group) o);
    }

    @Override
    public Object getSingularObjectFromString(final String s) throws FieldValidationException
    {
        return multiGroupConverter.getGroup(s);
    }

    @Override
    public List getRelatedIndexers(final CustomField customField)
    {
        return EasyList.build(new MultiGroupCustomFieldIndexer(fieldVisibilityManager, customField, multiGroupConverter));
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
        if (params == null)
        {
            params = new HashMap<String, Object>();
        }

        params.put("hasAdminPermission", Boolean.valueOf(permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser())));
        params.put("multiple", Boolean.valueOf(isMultiple()));
        return params;
    }

    @Override
    protected String convertTypeToString(final Object value)
    {
        return multiGroupConverter.getString((Group) value);
    }

    @Override
    protected Group convertStringToType(final String string)
    {
        return multiGroupConverter.getGroup(string);
    }

    private Collection<String> putInvalidGroupsAtFront(final Collection<String> groups)
    {
        final Set<String> retSet = new HashSet<String>();
        final StringBuffer errorString = new StringBuffer();
        if (groups != null)
        {
            for (final String groupList : groups)
            {
                if (isMultiple())
                {
                    for (final String groupString : multiGroupConverter.extractGroupStringsFromString(groupList))
                    {
                        populateGroupString(groupString, retSet, errorString);
                    }
                }
                else
                {
                    populateGroupString(groupList, retSet, errorString);
                }
            }
        }

        final List<String> l = new ArrayList<String>(retSet);
        Collections.sort(l);
        if (errorString.length() > 0)
        {
            l.add(0, errorString.toString());
        }
        return l;
    }

    private void populateGroupString(final String groupString, final Set<String> retSet, final StringBuffer errorString)
    {
        try
        {
            multiGroupConverter.getGroup(groupString);
            retSet.add(groupString);
        }
        catch (final FieldValidationException e)
        {
            if (errorString.length() > 0)
            {
                errorString.append(", ");
            }

            errorString.append(groupString);
        }
    }

    public boolean isMultiple()
    {
        return Boolean.valueOf(getDescriptor().getParams().get(MULTIPLE_PARAM_KEY)).booleanValue();
    }

    //------------------------------------------------------------------------------------------------------------------
    //  Implementation of ProjectImportableCustomField
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public ProjectCustomFieldImporter getProjectImporter()
    {
        return groupCustomFieldImporter;
    }

    public Query getQueryForGroup(final String fieldName, String groupName)
    {
        return new TermQuery(new Term(fieldName,groupName));
    }

    //------------------------------------------------------------------------------------------------------------------

    static class MultiGroupCustomFieldIndexer extends AbstractCustomFieldIndexer
    {
        private final CustomField customField;
        private final MultiGroupConverter multiGroupConverter;

        public MultiGroupCustomFieldIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField, final MultiGroupConverter multiGroupConverter)
        {
            super(fieldVisibilityManager, customField);
            this.customField = customField;
            this.multiGroupConverter = multiGroupConverter;
        }

        @Override
        public void addDocumentFieldsSearchable(final Document doc, final Issue issue)
        {
            addDocumentFields(doc, issue, Field.Index.UN_TOKENIZED);
        }

        @Override
        public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue)
        {
            addDocumentFields(doc, issue, Field.Index.NO);
        }

        void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType)
        {
            @SuppressWarnings("unchecked")
            final List<Group> o = (List) customField.getValue(issue);
            if (o != null)
            {
                for (final Group group : o)
                {
                    doc.add(new Field(getDocumentFieldId(), multiGroupConverter.getString(group), Field.Store.YES, indexType));
                }
            }
        }
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitMultiGroup(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitMultiGroup(MultiGroupCFType multiGroupCustomFieldType);
    }
}
