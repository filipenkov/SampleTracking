package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.cache.GoogleCacheInstruments;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.issue.field.CustomFieldUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.map.CacheObject;
import com.atlassian.jira.web.bean.I18nBean;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

@EventComponent
abstract public class AbstractFieldLayoutManager implements FieldLayoutManager, Startable
{
    private static final Logger log = Logger.getLogger(AbstractFieldLayoutManager.class);
    public static final String SCHEME = "FieldLayoutScheme";
    public static final String SCHEME_ASSOCIATION = "ProjectFieldLayoutScheme";
    private final FieldManager fieldManager;
    private List<FieldLayoutItem> defaultFieldLayoutItems;

    // Stores the scheme's field layouts using scheme as a key
    private final Cache<CacheObject<Long>, FieldLayout> fieldLayoutCache = CacheBuilder.newBuilder().build(new CacheLoader<CacheObject<Long>, FieldLayout>()
            {
                @Override
        public FieldLayout load(CacheObject<Long> from) throws Exception
                {
                    return loadFieldLayout(from.getValue());
                }
            });

    protected OfBizDelegator ofBizDelegator;

    public AbstractFieldLayoutManager(final FieldManager fieldManager, final OfBizDelegator ofBizDelegator, I18nHelper.BeanFactory i18n)
    {
        this.fieldManager = notNull("fieldManager", fieldManager);
        this.ofBizDelegator = ofBizDelegator;
    }

    abstract public FieldLayout getFieldLayout(final Long id);

    abstract public FieldLayout getFieldLayout(final GenericValue issue);

    abstract public FieldLayout getFieldLayout(Project project, String issueTypeId);

    abstract public FieldLayout getFieldLayout(final GenericValue project, final String issueTypeId);

    abstract public List<FieldLayoutScheme> getFieldLayoutSchemes();

    abstract public void updateFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme);

    abstract public void deleteFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme);

    abstract public void restoreSchemeFieldLayout(final GenericValue scheme);

    abstract public Collection<GenericValue> getProjects(FieldConfigurationScheme fieldConfigurationScheme);

    abstract public Collection<GenericValue> getProjects(final FieldLayoutScheme fieldLayoutScheme);

    abstract public FieldLayoutScheme createFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme);

    abstract public FieldConfigurationScheme getFieldConfigurationScheme(final Long schemeId);

    abstract public FieldConfigurationScheme getFieldConfigurationScheme(final Project project);

    abstract public FieldConfigurationScheme getFieldConfigurationScheme(final GenericValue project);

    abstract public Set<FieldLayout> getUniqueFieldLayouts(final Project project);

    abstract public FieldLayoutScheme getMutableFieldLayoutScheme(Long schemeId);

    abstract public boolean fieldConfigurationSchemeExists(String schemeName);

    abstract public List<EditableFieldLayout> getEditableFieldLayouts();

    abstract public void addSchemeAssociation(final GenericValue project, final Long fieldLayoutSchemeId);

    abstract public void removeSchemeAssociation(GenericValue project, Long fieldLayoutSchemeId);

    abstract public EditableFieldLayout getEditableFieldLayout(final Long id);

    abstract public void deleteFieldLayout(final FieldLayout fieldLayout);

    abstract public Collection<FieldLayoutSchemeEntity> getFieldLayoutSchemeEntities(final FieldLayoutScheme fieldLayoutScheme);

    abstract public void createFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity);

    abstract public void updateFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity);

    abstract public void removeFieldLayoutSchemeEntity(final FieldLayoutSchemeEntity fieldLayoutSchemeEntity);

    abstract public void removeFieldLayoutScheme(final FieldLayoutScheme fieldLayoutScheme);

    abstract public Collection<FieldConfigurationScheme> getFieldConfigurationSchemes(FieldLayout fieldLayout);

    abstract public Collection<GenericValue> getRelatedProjects(FieldLayout fieldLayout);

    abstract public boolean isFieldLayoutSchemesVisiblyEquivalent(Long fieldConfigurationSchemeId1, Long fieldConfigurationSchemeId2);

    abstract public boolean isFieldLayoutsVisiblyEquivalent(final Long fieldLayoutId1, final Long fieldLayoutId2);

    @Override
    public void start() throws Exception
    {
        // Setup the default order of the fields in create issue;
        this.defaultFieldLayoutItems = getDefaultFieldLayoutItems();
        new GoogleCacheInstruments(getClass().getSimpleName()).addCache(fieldLayoutCache).install();
    }

    @EventListener
    public void onCustomFieldUpdated(CustomFieldUpdatedEvent event)
    {
        invalidateFieldLayoutItemsContaining(event.getCustomFieldId());
    }

    protected List<FieldLayoutItem> getDefaultFieldLayoutItems()
    {
        return CollectionBuilder.newBuilder(
                createFieldLayoutItemImpl(IssueFieldConstants.SUMMARY, true),
                createFieldLayoutItemImpl(IssueFieldConstants.ISSUE_TYPE, true),
                createFieldLayoutItemImpl(IssueFieldConstants.SECURITY, false),
                createFieldLayoutItemImpl(IssueFieldConstants.PRIORITY, false),
                createFieldLayoutItemImpl(IssueFieldConstants.DUE_DATE, false),
                createFieldLayoutItemImpl(IssueFieldConstants.COMPONENTS, false),
                createFieldLayoutItemImpl(IssueFieldConstants.AFFECTED_VERSIONS, false),
                createFieldLayoutItemImpl(IssueFieldConstants.FIX_FOR_VERSIONS, false),
                createFieldLayoutItemImpl(IssueFieldConstants.ASSIGNEE, false),
                createFieldLayoutItemImpl(IssueFieldConstants.REPORTER, true),
                createFieldLayoutItemImpl(IssueFieldConstants.ENVIRONMENT, false),
                createFieldLayoutItemImpl(IssueFieldConstants.DESCRIPTION, false),
                createFieldLayoutItemImpl(IssueFieldConstants.TIMETRACKING, false),
                createFieldLayoutItemImpl(IssueFieldConstants.RESOLUTION, false),
                createFieldLayoutItemImpl(IssueFieldConstants.ATTACHMENT, false),
                createFieldLayoutItemImpl(IssueFieldConstants.COMMENT, false),
                createFieldLayoutItemImpl(IssueFieldConstants.LABELS, false),
                createFieldLayoutItemImpl(IssueFieldConstants.WORKLOG, false),
                createFieldLayoutItemImpl(IssueFieldConstants.ISSUE_LINKS, false)
        ).asList();
    }

    private FieldLayoutItem createFieldLayoutItemImpl(final String fieldId, final boolean required)
    {
        return new FieldLayoutItemImpl.Builder()
                .setOrderableField(fieldManager.getOrderableField(fieldId))
                .setFieldDescription(getDefaultDescription(fieldId))
                .setHidden(false)
                .setRequired(required)
                .setFieldManager(fieldManager)
                .build();
    }

    public FieldLayout getFieldLayout()
    {
        return getRelevantFieldLayout(null);
    }

    public FieldLayout getFieldLayout(final Issue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        return getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId());
    }

    public EditableDefaultFieldLayout getEditableDefaultFieldLayout()
    {
        final FieldLayout relevantFieldLayout = getRelevantFieldLayout(null);
        return new EditableDefaultFieldLayoutImpl(relevantFieldLayout.getGenericValue(), relevantFieldLayout.getFieldLayoutItems());
    }

    public void storeEditableDefaultFieldLayout(final EditableDefaultFieldLayout editableDefaultFieldLayout)
    {
        storeEditableFieldLayout(editableDefaultFieldLayout);
        refreshCaches(editableDefaultFieldLayout.getId());
        refreshCaches(null);
    }

    /*
     * THIS METHOD MUST BE SYNCHRONIZED!!!! So that only one thread updates the database at any one time. "Fields are
     * duplicated" if this method is not synchronized.
     */
    @Override
    public synchronized EditableFieldLayout storeAndReturnEditableFieldLayout(final EditableFieldLayout editableFieldLayout)
    {
        // FieldLayout (id, layoutscheme)
        // FieldLayoutItem (id, fieldlayout, fieldidentifier, verticalposition, ishidden, isrequired)
        // Find the default field layout in the database if it exists

        try
        {
            final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();

            GenericValue fieldLayoutGV = editableFieldLayout.getGenericValue();

            if (editableFieldLayout.getGenericValue() == null)
            {
                // There is no default, create a new one
                fieldLayoutGV = EntityUtils.createValue("FieldLayout", EasyMap.build("name", editableFieldLayout.getName(), "description",
                        editableFieldLayout.getDescription(), "type", editableFieldLayout.getType()));
            }
            else
            {
                fieldLayoutGV.store();
            }

            // Remove Field Layout Items. The removeRalted method seems to cause problems (duplicated records) on Tomcat, hence it is not used.
            final List<GenericValue> fieldLayoutItemGVs = fieldLayoutGV.getRelated("ChildFieldLayoutItem");
            ofBizDelegator.removeAll(fieldLayoutItemGVs);

            // Retrieve a list of Field Layout Items for this layout
            final List<FieldLayoutItem> fieldLayoutItems = editableFieldLayout.getFieldLayoutItems();
            final Long newId = fieldLayoutGV.getLong("id");
            for (FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
            {
                EntityUtils.createValue("FieldLayoutItem", EasyMap.build("fieldlayout", newId, "description",
                        fieldLayoutItem.getRawFieldDescription(), "fieldidentifier", fieldLayoutItem.getOrderableField().getId(), "ishidden",
                        Boolean.toString(fieldLayoutItem.isHidden()), "isrequired", Boolean.toString(fieldLayoutItem.isRequired()), "renderertype",
                        fieldLayoutItem.getRendererType()));
            }

            refreshCaches(newId);
            return getEditableFieldLayout(newId);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException("Could not load the default FieldLayout", e);
        }
    }

    public void storeEditableFieldLayout(final EditableFieldLayout editableFieldLayout)
    {
        storeAndReturnEditableFieldLayout(editableFieldLayout);
    }

    protected void refreshCaches(final Long id)
    {
        // Remove the scheme's field layout from the cache
        fieldLayoutCache.invalidate(CacheObject.wrap(id));

        // Clear the ColumnLayout cache
        fieldManager.getColumnLayoutManager().refresh();
    }

    public boolean hasDefaultFieldLayout()
    {
        final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        final GenericValue fieldLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", EasyMap.build("type", TYPE_DEFAULT)));
            return (fieldLayoutGV == null);
        }

    public void restoreDefaultFieldLayout()
    {
        final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        try
        {
            final GenericValue fieldLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", EasyMap.build("type", TYPE_DEFAULT)));
            if (fieldLayoutGV != null)
            {
                fieldLayoutGV.removeRelated("ChildFieldLayoutItem");
                fieldLayoutGV.remove();
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // Clear the cache
        refresh();
    }

    protected synchronized void restoreFieldLayout(final Long id)
    {
        try
        {
            // Remove the records from the database
            final OfBizDelegator ofBizDelegator = ComponentAccessor.getOfBizDelegator();
            final GenericValue fieldLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", EasyMap.build("id", id)));
            if (fieldLayoutGV != null)
            {
                // Remove Field Layout Items. The removeRalted method seems to cause problems (duplicated records) on Tomcat, hence it is not used.
                final List fieldLayoutItemGVs = fieldLayoutGV.getRelated("ChildFieldLayoutItem");
                ofBizDelegator.removeAll(fieldLayoutItemGVs);
                fieldLayoutGV.remove();
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // Clear the cache
        refresh();
    }

    public void refresh()
    {
        fieldLayoutCache.invalidateAll();
    }

    /**
     * Retrieves the field layout given a given id. If the id is null the default field layout is retrieved
     *
     * @param id field layout id
     * @return field layout
     */
    protected FieldLayout getRelevantFieldLayout(final Long id)
    {
        return fieldLayoutCache.getUnchecked(CacheObject.wrap(id));
    }

    private FieldLayout loadFieldLayout(Long id)
    {
        try
        {
            final Set unavailableFields = fieldManager.getUnavailableFields();
            List<FieldLayoutItem> fieldLayoutItems;

            GenericValue fieldLayoutGV = null;

            // TODO Maybe rewrite this
            if (id != null)
            {
                fieldLayoutGV = ofBizDelegator.findById("FieldLayout", id);
            }

            if (fieldLayoutGV == null)
            {
                // If no such record exists then use the default field layout record (i.e. the record with type "default")
                fieldLayoutGV = EntityUtil.getOnly(ofBizDelegator.findByAnd("FieldLayout", EasyMap.build("type", TYPE_DEFAULT)));
            }

            final FieldLayoutImpl resultingLayout = new FieldLayoutImpl(fieldLayoutGV, null);

            if (fieldLayoutGV == null)
            {
                // There is no default saved, return the system default
                fieldLayoutItems = new ArrayList<FieldLayoutItem>(defaultFieldLayoutItems);

                // set the FieldLayout on the items in the default list
                for (int i = 0; i < fieldLayoutItems.size(); i++)
                {
                    fieldLayoutItems.set(i, new FieldLayoutItemImpl.Builder(fieldLayoutItems.get(i)).setFieldLayout(resultingLayout).build());
                }

                // Get all custom fields
                final List<CustomField> customFieldObjects = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects();
                for (CustomField customField : customFieldObjects)
                {
                    // Always create FieldLayoutItems for custom fields with null descriptions as custom fields have
                    // their own descriptions.
                    final FieldLayoutItemImpl.Builder builder = new FieldLayoutItemImpl.Builder()
                            .setOrderableField(customField)
                            .setFieldDescription(null)
                            .setFieldLayout(resultingLayout);
                    fieldLayoutItems.add(builder.build());
                }
            }
            else
            {
                fieldLayoutItems = new ArrayList<FieldLayoutItem>();

                final List<GenericValue> related = fieldLayoutGV.getRelated("ChildFieldLayoutItem");
                for (GenericValue fieldLayoutItemGV : related)
                {
                    final String fieldId = fieldLayoutItemGV.getString("fieldidentifier");
                    if (fieldManager.isOrderableField(fieldId))
                    {
                        final FieldLayoutItemImpl.Builder builder = new FieldLayoutItemImpl.Builder()
                                .setOrderableField(fieldManager.getOrderableField(fieldId))
                                .setFieldDescription(fieldLayoutItemGV.getString("description"))
                                .setHidden(Boolean.valueOf(fieldLayoutItemGV.getString("ishidden")))
                                .setRequired(Boolean.valueOf(fieldLayoutItemGV.getString("isrequired")))
                                .setRendererType(fieldLayoutItemGV.getString("renderertype"))
                                .setFieldLayout(resultingLayout)
                                .setFieldManager(fieldManager);
                        fieldLayoutItems.add(builder.build());
                    }
                    else
                    {
                        // JRA-4423
                        log.info("Field layout contains non-orderable field with id '" + fieldId + "'.");
                    }
                }

                final Set<OrderableField> orderableFields = fieldManager.getOrderableFields();
                for (final OrderableField orderableField : orderableFields)
                {
                    boolean found = false;
                    for (final FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
                    {
                        if (orderableField.equals(fieldLayoutItem.getOrderableField()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        final FieldLayoutItemImpl.Builder builder = new FieldLayoutItemImpl.Builder()
                                .setOrderableField(orderableField)
                                .setFieldDescription(getDefaultDescription(orderableField.getId()))
                                .setHidden(false)
                                .setRequired(fieldManager.isMandatoryField(orderableField))
                                .setFieldLayout(resultingLayout)
                                .setFieldManager(fieldManager);
                        fieldLayoutItems.add(builder.build());
                    }
                }
            }

            // Remove unavailable fields
            for (final Iterator iterator = fieldLayoutItems.iterator(); iterator.hasNext();)
            {
                final FieldLayoutItem fieldLayoutItem = (FieldLayoutItem) iterator.next();
                if (unavailableFields.contains(fieldLayoutItem.getOrderableField()))
                {
                    iterator.remove();
                }
            }

            // associate all FieldLayoutItemImpl with this FieldLayout
            resultingLayout.setFieldLayoutItems(new ArrayList<FieldLayoutItem>(fieldLayoutItems));

            return resultingLayout;
        }
        catch (final GenericEntityException e)
        {
            log.error(e, e);
            throw new DataAccessException("Could not retrieve Field Layout.", e);
        }
    }

    protected String getDefaultDescription(final String fieldId)
    {
        final I18nHelper i18n = getI18nHelper();

        // TODO : Should get these strings on a per-user basis (i.e. get locale of user and return corresponding string).
        // TODO : At present, the default locale is used.
        if (IssueFieldConstants.ENVIRONMENT.equals(fieldId))
        {
            return i18n.getText("environment.field.description");
        }
        else if (IssueFieldConstants.TIMETRACKING.equals(fieldId))
        {
            return i18n.getText("timetracking.field.description", "*w *d *h *m", "4d, 5h 30m, 60m", "3w");
        }
        else if (IssueFieldConstants.WORKLOG.equals(fieldId))
        {
            return i18n.getText("worklog.field.description");
        }

        return null;
    }

    ///CLOVER:OFF
    protected I18nHelper getI18nHelper()
    {
        return new I18nBean(ComponentAccessor.getApplicationProperties().getDefaultLocale());
    }
    ///CLOVER:ON

    /**
     * Invalidates any cache entry whose FieldLayoutItem instances reference the custom field having the given {@code
     * customFieldId}. This is necessary to keep the cache up to date with changes in custom field names.
     * <p/>
     * This method iterates through the cache entries so it may be a bit racy but should mostly work.
     *
     * @param customFieldId a String containing the custom field's id
     */
    private void invalidateFieldLayoutItemsContaining(String customFieldId)
    {
        for (Map.Entry<CacheObject<Long>, FieldLayout> entry : fieldLayoutCache.asMap().entrySet())
        {
            if (entry.getValue().getFieldLayoutItem(customFieldId) != null)
            {
                // refresh the cache entry
                refreshCaches(entry.getKey().getValue());
            }
        }
    }
}
