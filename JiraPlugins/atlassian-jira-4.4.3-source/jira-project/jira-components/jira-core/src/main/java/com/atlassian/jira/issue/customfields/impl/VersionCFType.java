package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.VersionCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.comparator.VersionComparator;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.MultipleCustomFieldType;
import com.atlassian.jira.issue.customfields.RequiresProjectSelectedMarker;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.VersionOptionsConfigItem;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.option.GenericImmutableOptions;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.HackyRendererType;
import com.atlassian.jira.issue.fields.util.VersionHelperBean;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VersionCFType extends AbstractCustomFieldType implements MultipleCustomFieldType, RequiresProjectSelectedMarker, SortableCustomField, ProjectImportableCustomField
{
    private final Logger log = Logger.getLogger(VersionCFType.class);

    // ------------------------------------------------------------------------------------------------------- Constants
    private static final String NO_VERSION_STRING = "-1";
    private static final PersistenceFieldType DB_TYPE = PersistenceFieldType.TYPE_DECIMAL;
    private static final String MULTIPLE_PARAM_KEY = "multiple";

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VersionManager versionManager;
    private final CustomFieldValuePersister persister;
    private final GenericConfigManager genericConfigManager;
    private final VersionHelperBean versionHelperBean;
    private final VersionCustomFieldImporter versionCustomFieldImporter;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public VersionCFType(final PermissionManager permissionManager, final JiraAuthenticationContext jiraAuthenticationContext, final VersionManager versionManager, final CustomFieldValuePersister customFieldValuePersister, final GenericConfigManager genericConfigManager, final VersionHelperBean versionHelperBean)
    {
        this.permissionManager = permissionManager;
        authenticationContext = jiraAuthenticationContext;
        this.versionManager = versionManager;
        persister = customFieldValuePersister;
        this.genericConfigManager = genericConfigManager;
        this.versionHelperBean = versionHelperBean;
        versionCustomFieldImporter = new VersionCustomFieldImporter();
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public Set remove(final CustomField field)
    {
        return persister.removeAllValues(field.getId());
    }

    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        final Collection versionIds = relevantParams.getAllValues();
        if ((versionIds != null) && versionIds.isEmpty())
        {
            final CustomField customField = config.getCustomField();
            versionHelperBean.validateVersionIds(versionIds, errorCollectionToAddTo, getI18nBean(), customField.getId());
        }
    }

    /**
     * Persists the object to the datastore for the given issue.
     * @param field
     * @param issue
     * @param value
     */
    public void createValue(final CustomField field, final Issue issue, final Object value)
    {
        if (value instanceof Collection)
        {
            persister.createValues(field, issue.getId(), DB_TYPE, getDbValueFromCollection(value));
        }
        else
        {
            persister.createValues(field, issue.getId(), DB_TYPE, getDbValueFromCollection(EasyList.build(value)));
        }
    }

    public void updateValue(final CustomField field, final Issue issue, final Object value)
    {
        persister.updateValues(field, issue.getId(), DB_TYPE, getDbValueFromCollection(value));
    }

    public Object getValueFromCustomFieldParams(final CustomFieldParams parameters) throws FieldValidationException
    {
        final Collection allValues = parameters.getAllValues();
        final Collection collection = CollectionUtils.collect(allValues, new Transformer()
        {
            public Object transform(final Object input)
            {
                final String versionIdString = (String) input;
                final Long versionId = getLongFromString(versionIdString);
                return versionManager.getVersion(versionId);
            }
        });

        if (CustomFieldUtils.isCollectionNotEmpty(collection))
        {
            return collection;
        }
        else
        {
            return null;
        }
    }

    public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
    {
        return parameters.getAllValues();
    }

    public Object getValueFromIssue(final CustomField field, final Issue issue)
    {
        final List values = persister.getValues(field, issue.getId(), DB_TYPE);
        if ((values != null) && !values.isEmpty())
        {
            return getVersionFromDoubles(values);
        }
        else
        {
            return null;
        }
    }

    public void setDefaultValue(final FieldConfig fieldConfig, final Object value)
    {
        Collection versionIds = getDbValueFromCollection(value);
        if (versionIds != null)
        {
            versionIds = new ArrayList(versionIds);
            genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), versionIds);
        }
    }

    public Object getDefaultValue(final FieldConfig fieldConfig)
    {
        final Object o = genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());

        Collection collectionOfDoubles = null;
        if (o instanceof Collection)
        {
            collectionOfDoubles = (Collection) o;
        }
        else if (o instanceof Double)
        {
            // This is done for backwords compatability
            collectionOfDoubles = EasyList.build(o);
        }

        if (collectionOfDoubles != null)
        {
            return getVersionFromDoubles(collectionOfDoubles);
        }
        else
        {
            return null;
        }
    }

    public String getChangelogValue(final CustomField field, final Object value)
    {
        if (value != null)
        {
            final Collection versions = (Collection) value;
            final StringBuffer sb = new StringBuffer();
            for (final Iterator iterator = versions.iterator(); iterator.hasNext();)
            {
                final Version version = (Version) iterator.next();
                if (version != null)
                {
                    sb.append(version.getId());

                    if (iterator.hasNext())
                    {
                        sb.append(", ");
                    }
                }
            }
            return sb.toString();
        }
        else
        {
            return "";
        }
    }

    @Override
    public String getChangelogString(final CustomField field, final Object value)
    {
        if (value != null)
        {
            final Collection versions = (Collection) value;
            final StringBuffer sb = new StringBuffer();
            for (final Iterator iterator = versions.iterator(); iterator.hasNext();)
            {
                final Version version = (Version) iterator.next();
                if (version != null)
                {
                    sb.append(version.getName());

                    if (iterator.hasNext())
                    {
                        sb.append(", ");
                    }
                }
            }
            return sb.toString();
        }
        else
        {
            return null;
        }
    }

    // ----------------------------------------------------------------------------------------------------- Old Methods
    public String getStringFromSingularObject(final Object customFieldObject)
    {
        assertObjectImplementsType(Version.class, customFieldObject);
        final Version version = (Version) customFieldObject;
        if (version == null)
        {
            return NO_VERSION_STRING;
        }
        else
        {
            return String.valueOf(version.getId());
        }
    }

    public Object getSingularObjectFromString(final String string) throws FieldValidationException
    {
        if (StringUtils.isEmpty(string) || NO_VERSION_STRING.equals(string))
        {
            return null;
        }
        else
        {
            final Long versionId = getLongFromString(string);

            return versionManager.getVersion(versionId);
        }
    }

    public int compare(final Object customFieldObjectValue1, final Object customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        // A bit, actually a lot, of a hack, but to ensure backwards compatibility test what object we have been given.
        if ((customFieldObjectValue1 instanceof GenericValue) && (customFieldObjectValue2 instanceof GenericValue))
        {
            log.debug("Comparing generic values instead of versions!");
            return OfBizComparators.NAME_COMPARATOR.compare((GenericValue) customFieldObjectValue1, (GenericValue) customFieldObjectValue2);
        }
        else if ((customFieldObjectValue1 instanceof Version) && (customFieldObjectValue2 instanceof Version))
        {
            return new VersionComparator().compare((Version) customFieldObjectValue1, (Version) customFieldObjectValue2);
        }
        else
        {
            throw new IllegalArgumentException("The objects are not of the expected type.");
        }
    }

    @Override
    public List getConfigurationItemTypes()
    {
        final List configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new VersionOptionsConfigItem(versionManager));
        return configurationItemTypes;
    }

    public Options getOptions(final FieldConfig config, final JiraContextNode jiraContextNode)
    {
        if ((jiraContextNode != null) && (jiraContextNode.getProject() != null))
        {
            return new GenericImmutableOptions(versionManager.getVersions(jiraContextNode.getProject()), config);
        }
        else
        {
            return new GenericImmutableOptions(Collections.EMPTY_LIST, config);
        }
    }

    @Override
    public String availableForBulkEdit(final BulkEditBean bulkEditBean)
    {
        // Can bulk-edit this field only if all selected issue belong to one project
        if (!bulkEditBean.isMultipleProjects() && (bulkEditBean.getProject() != null))
        {
            // Ensure that the project has versions
            if (versionManager.getVersions(bulkEditBean.getProject()).isEmpty())
            {
                return "bulk.edit.unavailable.noversions";
            }

            // Field specific check complete - return available for bulk edit
            // The CustomFieldImpl will perform further checks
            return null;
        }
        else
        {
            // Let the user know that selected issues belong to more than one project so the action is not available
            return "bulk.edit.unavailable.multipleprojects";
        }

    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> velocityParameters = super.getVelocityParameters(issue, field, fieldLayoutItem);

        if (issue != null)
        {
            // JRA-15007: released versions must always be reversed (descending order)
            final Collection releasedversion = versionManager.getVersionsReleasedDesc(issue.getProject(), false);
            final Collection unreleasedversion = versionManager.getVersionsUnreleased(issue.getProject(), false);
            final Collection currentlySelectedArchivedVersions = getCurrentlySelectedArchivedVersions(issue, field);

            velocityParameters.put("unknownVersionId", -1L);
            velocityParameters.put("releasedVersion", releasedversion);
            velocityParameters.put("unreleasedVersion", unreleasedversion);
            velocityParameters.put("archivedVersions", currentlySelectedArchivedVersions);           
            if (fieldLayoutItem != null)
            {
                velocityParameters.put("isFrotherControl", HackyRendererType.fromKey(fieldLayoutItem.getRendererType()) == HackyRendererType.FROTHER_CONTROL);
            }
        }

        velocityParameters.put("collection", new CollectionUtils());
        velocityParameters.put("multiple", Boolean.valueOf((String) getDescriptor().getParams().get("multiple")));

        return velocityParameters;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return versionCustomFieldImporter;
    }

    public boolean isMultiple()
    {
        return Boolean.valueOf(getDescriptor().getParams().get(MULTIPLE_PARAM_KEY)).booleanValue();
    }

    private Collection getCurrentlySelectedArchivedVersions(final Issue issue, final CustomField field)
    {
        final Collection selectedVersions = (Collection) getValueFromIssue(field, issue);
        if ((selectedVersions != null) && !selectedVersions.isEmpty())
        {
            return CollectionUtils.select(selectedVersions, new Predicate()
            {
                public boolean evaluate(final Object object)
                {
                    final Version version = (Version) object;
                    return version.isArchived();
                }
            });
        }

        return Collections.EMPTY_LIST;
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    private Collection getDbValueFromCollection(final Object value)
    {
        if (value != null)
        {
            final Collection collection = (Collection) value;
            final Collection verisionIdDoubles = CollectionUtils.collect(collection, new Transformer()
            {
                public Object transform(final Object input)
                {
                    if (input != null)
                    {
                        final Version version = (Version) input;
                        return new Double(version.getId().longValue());
                    }
                    else
                    {
                        return null;
                    }
                }
            });

            return CollectionUtils.select(verisionIdDoubles, NotNullPredicate.getInstance());
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    private Collection getVersionFromDoubles(final Collection collectionOfDoubles)
    {
        final Collection collection = CollectionUtils.collect(collectionOfDoubles, new Transformer()
        {
            public Object transform(final Object input)
            {
                if (input != null)
                {
                    final Double versionIdDouble = (Double) input;
                    final Long versionId = new Long(versionIdDouble.longValue());
                    return versionManager.getVersion(versionId);
                }
                else
                {
                    return null;
                }
            }
        });
        CollectionUtils.filter(collection, NotNullPredicate.getInstance());
        return collection;
    }

    private Long getLongFromString(final String stringValue) throws FieldValidationException
    {
        try
        {
            return Long.valueOf(stringValue);
        }
        catch (final NumberFormatException e)
        {
            log.error(e.getMessage(), e);
            throw new FieldValidationException("Version Id is not a number '" + stringValue + "'");
        }
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitVersion(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitVersion(VersionCFType versionCustomFieldType);
    }
}
