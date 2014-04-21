package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bulkedit.operation.BulkWorkflowTransitionOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.MultipleCustomFieldType;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.impl.LabelsCFType;
import com.atlassian.jira.issue.customfields.impl.VersionCFType;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldClauseContextHandler;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldClauseSanitiserHandler;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.customfields.view.NullCustomFieldParams;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.persistence.FieldConfigPersister;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.IssueComparator;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.constants.DefaultClauseInformation;
import com.atlassian.jira.issue.search.parameters.lucene.sort.DocumentSortComparatorSource;
import com.atlassian.jira.issue.search.parameters.lucene.sort.IssueSortComparator;
import com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.DefaultClauseHandler;
import com.atlassian.jira.jql.DefaultValuesGeneratingClauseHandler;
import com.atlassian.jira.jql.NoOpClauseHandler;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.context.ClauseContextFactory;
import com.atlassian.jira.jql.context.ContextSetUtil;
import com.atlassian.jira.jql.context.CustomFieldClauseContextFactory;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.jql.permission.ClausePermissionChecker;
import com.atlassian.jira.jql.permission.ClausePermissionHandler;
import com.atlassian.jira.jql.permission.ClauseSanitiser;
import com.atlassian.jira.jql.permission.CustomFieldClausePermissionChecker;
import com.atlassian.jira.jql.permission.DefaultClausePermissionHandler;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import com.atlassian.jira.web.bean.DefaultBulkMoveHelper;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.ImmutableList;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.apache.lucene.search.SortComparatorSource;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notEmpty;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default CustomField implementation backed by the database (a GenericValue object).
 * Usually managed via {@link CustomFieldManager}.
 * <p/>
 * Remember to call {@link #store()} after calling any setter methods.
 */
public class CustomFieldImpl implements CustomField
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String ENTITY_CF_TYPE_KEY = "customfieldtypekey";
    public static final String ENTITY_CUSTOM_FIELD_SEARCHER = "customfieldsearcherkey";
    public static final String ENTITY_NAME = "name";
    public static final String ENTITY_ISSUETYPE = "issuetype";
    public static final String ENTITY_PROJECT = "project";
    public static final String ENTITY_ID = "id";
    public static final String ENTITY_DESCRIPTION = "description";
    public static final String ENTITY_TABLE_NAME = "CustomField";

    // ------------------------------------------------------------------------------------------------- Type Properties
    private static final Logger log = Logger.getLogger(CustomFieldImpl.class);

    private final GenericValue gv;

    /**
     * Lazily-initialised type for this CustomFieldImpl.
     */
    private final LazyReference<CustomFieldType> typeRef = new CustomFieldTypeLazyRef();

    /**
     * Lazily-initialised searcher for this CustomFieldImpl.
     */
    private final LazyReference<CustomFieldSearcher> searcherRef = new CustomFieldSearcherLazyRef();

    /**
     * Configuration schemes.
     */
    private final AtomicReference<List<FieldConfigScheme>> configurationSchemes = new AtomicReference<List<FieldConfigScheme>>(null);

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final JiraAuthenticationContext authenticationContext;
    private final CustomFieldManager customFieldManager;
    private final ConstantsManager constantsManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final PermissionManager permissionManager;
    private final RendererManager rendererManager;
    private final FieldConfigSchemeClauseContextUtil contextUtil;

    private final String customFieldId;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public CustomFieldImpl(GenericValue customField,
            CustomFieldManager customFieldManager,
            JiraAuthenticationContext authenticationContext,
            ConstantsManager constantsManager,
            FieldConfigSchemeManager fieldConfigSchemeManager,
            PermissionManager permissionManager,
            RendererManager rendererManager,
            FieldConfigSchemeClauseContextUtil contextUtil)
    {
        this.customFieldManager = customFieldManager;
        this.gv = customField;
        this.authenticationContext = authenticationContext;
        this.constantsManager = constantsManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.permissionManager = permissionManager;
        this.rendererManager = rendererManager;
        this.contextUtil = contextUtil;

        customFieldId = FieldManager.CUSTOM_FIELD_PREFIX + gv.getLong(ENTITY_ID);
    }

    // --------------------------------------------------------------------------------------------- Persistance Methods

    /**
     * Stores the generic value of this custom field and refreshes {@link com.atlassian.jira.issue.fields.FieldManager}.
     *
     * @throws DataAccessException if error of storing the generic value occurs
     */
    public void store() throws DataAccessException
    {
        try
        {
            gv.store();
            ComponentManager.getComponentInstanceOfType(FieldManager.class).refresh();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Exception whilst trying to store genericValue " + gv + ".", e);
        }
    }

    // --------------------------------------------------------------------------------- Configuration & Schemes & Scope
    public boolean isInScope(Project project, List<String> issueTypeIds)
    {
        List<IssueContext> issueContexts = CustomFieldUtils.convertToIssueContexts(project, issueTypeIds);
        return isInScope(issueContexts);
    }

    public final boolean isInScope(GenericValue project, List issueTypeIds)
    {
        List<IssueContext> issueContexts = CustomFieldUtils.convertToIssueContexts(project, issueTypeIds);
        return isInScope(issueContexts);
    }

    private boolean isInScope(final List<IssueContext> issueContexts)
    {
        for (final IssueContext issueContext : issueContexts)
        {
            final FieldConfig relevantConfig = getRelevantConfig(issueContext);
            if (relevantConfig != null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the relevant field config for the search context specified.
     * Checks that all configs within search context are the same - i.e. all null or all the same config.
     * <p/>
     * Returns null if any two configs are different.
     * <p/>
     * Note: null config is not equal to non-null config. Previously, a non-null config was returned even if the first
     * config(s) was null.
     *
     * @param searchContext search context
     * @return null if any two configs are different
     */
    public FieldConfig getReleventConfig(SearchContext searchContext)
    {
        List<IssueContext> issueContexts = searchContext.getAsIssueContexts();
        FieldConfig config = null;
        boolean firstRun = true;
        for (final IssueContext issueContext : issueContexts)
        {
            final FieldConfig relevantConfig = getRelevantConfig(issueContext);

            // Grab the first config - we will compare all other configs with this one.
            if (firstRun)
            {
                config = relevantConfig;
                firstRun = false;
            }
            // Compare the configurations - return the text if the configs are different
            else if (areDifferent(config, relevantConfig))
            {
                log.debug("Different configs found for search context. No configs are returned for " + getName());
                return null;
            }
        }

        return config;
    }

    public ClauseNames getClauseNames()
    {
        return ClauseNames.forCustomField(this);
    }

    public boolean isInScope(SearchContext searchContext)
    {
        return getReleventConfig(searchContext) != null;
    }

    /**
     * Determines wheteher this custom field is in scope.
     * The custom field is in scope if there is a relevant config for given search context.
     *
     * @param user          not used
     * @param searchContext search context
     * @return true if this field has a relevant config for given search context
     *
     * @deprecated The user parameter is ignored. Please call {@link #isInScope(SearchContext)}}. Since v4.3
     */
    public final boolean isInScope(User user, SearchContext searchContext)
    {
        return getReleventConfig(searchContext) != null;
    }

    // --------------------------------------------------------------- Methods forwarded to Custom Field Type & Searcher

    /**
     * Validates relevant parameters on custom field type of this custom field. Any errors found are added to the given
     * errorCollection.
     * See {@link CustomFieldType#validateFromParams(CustomFieldParams,ErrorCollection,FieldConfig)}
     *
     * @param actionParameters action parameters
     * @param errorCollection  error collection to add errors to
     * @param config           field config
     */
    public void validateFromActionParams(Map actionParameters, ErrorCollection errorCollection, FieldConfig config)
    {
        final CustomFieldParams relevantParams = getRelevantParams(actionParameters);
        getCustomFieldType().validateFromParams(relevantParams, errorCollection, config);
    }

    /**
     * Retrieves and returns the Object representing the this CustomField value for the given issue.
     * See {@link CustomFieldType#getValueFromIssue(CustomField,Issue)}
     *
     * @param issue issue to retrieve the value from
     * @return Object representing the this CustomField value for the given issue
     */
    public Object getValue(Issue issue)
    {
        return getCustomFieldType().getValueFromIssue(this, issue);
    }

    /**
     * This is the conjunction point with CustomFieldTypes and this is delegated off to customField Types.
     *
     * @return true if the custom field supports interaction with the renderers, false otherwise. Text based
     *         fields will be able to interact with the renderers.
     */
    public boolean isRenderable()
    {
        return getCustomFieldType().isRenderable();
    }

    /**
     * Returns the given value as a string. This can be a one-way transformation. That is, there is no requirement that
     * the value returned by this method can be used to reconstruct the value.
     * <p/>
     * For example, for {@link com.atlassian.jira.issue.customfields.impl.VersionCFType} it returns a comma-separated
     * list of version IDs as a string.
     *
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     * @return change log value
     */
    private String getChangelogValue(Object value)
    {
        return getCustomFieldType().getChangelogValue(this, value);
    }

    /**
     * Returns the given value as a string. This can be a one-way transformation. That is, there is no requirement that
     * the value returned by this method can be used to reconstruct the value.
     * <p/>
     * For example, for {@link com.atlassian.jira.issue.customfields.impl.VersionCFType} it returns a comma-separated
     * list of version names as a string.
     *
     * @param value <i>Transport Object</i> representing the value instance of the CustomField
     * @return change log value
     */
    private String getChangelogString(Object value)
    {
        return getCustomFieldType().getChangelogString(this, value);
    }

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        ChangeItemBean changeItemBean = updateValue(fieldLayoutItem, issue, modifiedValue.getNewValue());
        if (changeItemBean != null)
        {
            issueChangeHolder.addChangeItem(changeItemBean);
        }
    }

    private ChangeItemBean updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, Object newValue)
    {
        Object existingValue = getValue(issue);

        ChangeItemBean cib = null;

        if (existingValue == null)
        {
            if (newValue != null)
            {
                newValue = processValueThroughRenderer(fieldLayoutItem, newValue);
                createValue(issue, newValue);
                String changelogValue = getChangelogValue(newValue);
                if (changelogValue != null)
                {
                    String changelogString = getChangelogString(newValue);
                    // If the changelogString is null then fall back to the changelogValue
                    if (changelogString == null)
                    {
                        changelogString = changelogValue;
                        changelogValue = null;
                    }

                    cib = new ChangeItemBean(ChangeItemBean.CUSTOM_FIELD, getNameKey(), null, null, changelogValue, changelogString);
                }
            }
        }
        else
        {
            if (!valuesEqual(existingValue, newValue))
            {
                newValue = processValueThroughRenderer(fieldLayoutItem, newValue);
                getCustomFieldType().updateValue(this, issue, newValue);
                String changelogValue = getChangelogValue(newValue);
                if (changelogValue != null)
                {
                    String changelogString = getChangelogString(newValue);
                    // If the changelogString is null then fall back to the changelogValue
                    if (changelogString == null)
                    {
                        changelogString = changelogValue;
                        changelogValue = null;
                    }

                    String oldChangelogString = getChangelogString(existingValue);
                    String oldChangelogValue = getChangelogValue(existingValue);
                    // If the changelogString is null then fall back to the changelogValue
                    if (oldChangelogString == null)
                    {
                        oldChangelogString = oldChangelogValue;
                        oldChangelogValue = null;
                    }
                    cib = new ChangeItemBean(ChangeItemBean.CUSTOM_FIELD, getNameKey(), oldChangelogValue, oldChangelogString, changelogValue, changelogString);
                }
            }
        }

        return cib;
    }

    private Object processValueThroughRenderer(FieldLayoutItem fieldLayoutItem, Object value)
    {
        if (isRenderable())
        {
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            value = rendererManager.getRendererForType(rendererType).transformFromEdit(value);
        }
        return value;
    }

    /**
     * Returns the same string.
     *
     * @param changeHistory change history string
     * @return change history string
     */
    public String prettyPrintChangeHistory(String changeHistory)
    {
        return changeHistory;
    }

    /**
     * Returns the same string.
     *
     * @param changeHistory change history string
     * @param i18nHelper    not used
     * @return change history string
     */
    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        return changeHistory;
    }

    /**
     * Returns true if this custom field has an edit template, false otherwise.
     *
     * @return true if this custom field has an edit template, false otherwise.
     */
    public boolean isEditable()
    {
        return getCustomFieldType().getDescriptor().isEditTemplateExists();
    }


    /**
     * Returns options for this custom field if it is of {@link MultipleCustomFieldType} type. Otherwise returns null.
     *
     * @param key             not used
     * @param jiraContextNode JIRA context node
     * @return options for this custom field if it is of {@link MultipleCustomFieldType} type, null otherwise
     */
    public Options getOptions(String key, JiraContextNode jiraContextNode)
    {
        return getOptions(key, getRelevantConfig(jiraContextNode), jiraContextNode);
    }

    /**
     * Returns options for this custom field if it is of {@link MultipleCustomFieldType} type. Otherwise returns null.
     *
     * @param key         not used
     * @param config      relevant field config
     * @param contextNode JIRA context node
     * @return options for this custom field if it is of {@link MultipleCustomFieldType} type, null otherwise
     */
    public Options getOptions(String key, FieldConfig config, JiraContextNode contextNode)
    {
        CustomFieldType customFieldType = getCustomFieldType();
        if (customFieldType instanceof MultipleCustomFieldType)
        {
            final MultipleCustomFieldType multipleCustomFieldType = (MultipleCustomFieldType) customFieldType;

            return multipleCustomFieldType.getOptions(config, contextNode);
        }
        else
        {
            return null;
        }
    }

    public void populateDefaults(Map customFieldValuesHolder, Issue issue)
    {
        FieldConfig config = getRelevantConfig(issue);
        if (config != null)
        {
            Object defaultValues = getCustomFieldType().getDefaultValue(config);

            CustomFieldParams paramsFromIssue;
            if (defaultValues != null)
            {
                paramsFromIssue = new CustomFieldParamsImpl(this, defaultValues);
                paramsFromIssue.transformObjectsToStrings();
                customFieldValuesHolder.put(getId(), paramsFromIssue);
                customFieldValuesHolder.put(getId() + ":objects", new CustomFieldParamsImpl(this, defaultValues));
            }
        }
        else
        {
            log.info("No relevant config found for " + this + " for the issue " + issue);
        }
    }

    /**
     * Returns the relevant field config of this custom field for the give issue context
     *
     * @param issueContext issue context to find the relevant field config for
     * @return the relevant field config of this custom field for the give issue context
     */
    public FieldConfig getRelevantConfig(IssueContext issueContext)
    {
        return fieldConfigSchemeManager.getRelevantConfig(issueContext, this);
    }

    public FieldConfig getRelevantConfig(Issue issue)
    {
        // TODO: Issue extends IssueContext. This method is redundant.
        GenericValue issuetype = issue.getIssueType();
        GenericValue project = issue.getProject();

        IssueContext issueContext = new IssueContextImpl(project, issuetype);
        return getRelevantConfig(issueContext);
    }

    private FieldConfig getRelevantConfig(JiraContextNode contextNode)
    {
        return getRelevantConfig(new IssueContextImpl(contextNode.getProject(), contextNode.getIssueType()));
    }

    /**
     * Puts the relevant parameters from the given params map to the given customFieldValuesHolder map.
     *
     * @param customFieldValuesHolder map of custom field values
     * @param params                  map of parameters
     */
    public void populateFromParams(Map customFieldValuesHolder, Map params)
    {
        final CustomFieldParams relevantParams = getRelevantParams(params);
        customFieldValuesHolder.put(getId(), relevantParams);
    }

    /**
     * Puts the custom field parameters retrieved from the given issue to the given customFieldValuesHolder map.
     *
     * @param customFieldValuesHolder map of custom field values
     * @param issue                   issue to get the custom field parameters from
     */
    public void populateFromIssue(Map customFieldValuesHolder, Issue issue)
    {
        CustomFieldParams paramsFromIssue = getCustomFieldParamsFromIssue(issue);

        customFieldValuesHolder.put(getId(), paramsFromIssue);
    }


    public Object getValueFromParams(Map params) throws FieldValidationException
    {
        return getCustomFieldType().getValueFromCustomFieldParams((CustomFieldParams) params.get(getId()));
    }

    /**
     * Does nothing. Throws UnsupportedOperationException.
     *
     * @param fieldValuesHolder not used
     * @param stringValue       not used
     * @param issue             not used
     * @throws UnsupportedOperationException always
     */
    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue)
    {
        // TODO Need to proxy to the custom field type for conversion
        throw new UnsupportedOperationException("Not implemented.");
    }

    public List getConfigurationItemTypes()
    {
        return getCustomFieldType().getConfigurationItemTypes();
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        CustomFieldSearcher customFieldSearcher = getCustomFieldSearcher();
        final ClauseNames clauseNames = getClauseNames();
        if (customFieldSearcher == null)
        {
            // JRA-19106 - This is a special case where we can sort but not search so we will provide an no-op query and validator generators
            if (getCustomFieldType() instanceof SortableCustomField)
            {
                final ClauseHandler noOpClauseHandler = new NoOpClauseHandler(createClausePermissionHandler(null), getId(), clauseNames, "jira.jql.validation.field.not.searchable");
                final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(noOpClauseHandler);
                return new SearchHandler(Collections.<FieldIndexer>emptyList(), null, Collections.singletonList(clauseRegistration));
            }

            return null;
        }

        final CustomFieldSearcherClauseHandler searcherClauseHandler = customFieldSearcher.getCustomFieldSearcherClauseHandler();

        final ClauseContextFactory clauseContextFactory;
        if (searcherClauseHandler instanceof CustomFieldClauseContextHandler)
        {
            clauseContextFactory = ((CustomFieldClauseContextHandler) searcherClauseHandler).getClauseContextFactory();
        }
        else
        {
            final FieldConfigSchemeClauseContextUtil clauseContextUtil = ComponentManager.getComponentInstanceOfType(FieldConfigSchemeClauseContextUtil.class);
            clauseContextFactory = new CustomFieldClauseContextFactory(this, clauseContextUtil, ContextSetUtil.getInstance());
        }

        // if the custom field requires sanitising, the SearchClauseHandler should specify the sanitiser to be used
        ClauseSanitiser sanitiser = null;
        if (searcherClauseHandler instanceof CustomFieldClauseSanitiserHandler)
        {
            sanitiser = ((CustomFieldClauseSanitiserHandler) searcherClauseHandler).getClauseSanitiser();
        }

        ClauseInformation clauseInformation = new DefaultClauseInformation(getId(), clauseNames, getId(),
                searcherClauseHandler.getSupportedOperators(), searcherClauseHandler.getDataType());

        final ClauseHandler customFieldClauseHandler;
        if (searcherClauseHandler instanceof ValueGeneratingClauseHandler)
        {
            customFieldClauseHandler = new DefaultValuesGeneratingClauseHandler(clauseInformation, searcherClauseHandler.getClauseQueryFactory(), searcherClauseHandler.getClauseValidator(),
                    createClausePermissionHandler(sanitiser), clauseContextFactory,
                    ((ValueGeneratingClauseHandler)(searcherClauseHandler)).getClauseValuesGenerator());
        }
        else
        {
            customFieldClauseHandler = new DefaultClauseHandler(clauseInformation, searcherClauseHandler.getClauseQueryFactory(),
                searcherClauseHandler.getClauseValidator(), createClausePermissionHandler(sanitiser),
                clauseContextFactory);
        }
        final SearchHandler.ClauseRegistration clauseRegistration = new SearchHandler.ClauseRegistration(customFieldClauseHandler);

        return new SearchHandler(customFieldSearcher.getSearchInformation().getRelatedIndexers(),
                new SearchHandler.SearcherRegistration(customFieldSearcher, clauseRegistration));
    }

    /**
     * @param sanitiser if null, the {@link com.atlassian.jira.jql.permission.NoOpClauseSanitiser} will be used.
     * @return a clause permission handler
     */
    private ClausePermissionHandler createClausePermissionHandler(final ClauseSanitiser sanitiser)
    {
        final CustomFieldClausePermissionChecker.Factory factory = ComponentManager.getComponentInstanceOfType(CustomFieldClausePermissionChecker.Factory.class);

        final ClausePermissionChecker checker = factory.createPermissionChecker(this, contextUtil);
        if (sanitiser == null)
        {
            return new DefaultClausePermissionHandler(checker);
        }
        else
        {
            return new DefaultClausePermissionHandler(checker, sanitiser);
        }
    }

    public void createValue(Issue issue, Object value)
    {
        // Do not store null values
        if (value != null)
        {
            getCustomFieldType().createValue(this, issue, value);
        }
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map params = operationContext.getFieldValuesHolder();
        ErrorCollection errorCollection = new SimpleErrorCollection();
        FieldConfig config = getRelevantConfig(issue);

        if (params.containsKey(getId()))
        {
            getCustomFieldType().validateFromParams((CustomFieldParams) params.get(getId()), errorCollection, config);
        }

        // Only validate for 'requireness' if no errors have been found
        if (!errorCollection.hasAnyErrors())
        {
            try
            {
                // Check that if the field is 'required' that the value has been provided if it is editable
                if (isEditable() && fieldScreenRenderLayoutItem != null && fieldScreenRenderLayoutItem.isRequired())
                {
                    // If the value is not in the map or if the value is null add an error message.
                    if (!params.containsKey(getId()) || getCustomFieldType().getValueFromCustomFieldParams((CustomFieldParams) params.get(getId())) == null)
                    {
                        errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", getName()));
                    }
                }
            }
            catch (FieldValidationException e)
            {
                log.error("Error occurred while validating a custom field", e);
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        else
        {
            errorCollectionToAddTo.addErrorCollection(errorCollection);
        }
    }

    public CustomFieldParams getCustomFieldValues(Map customFieldValuesHolder)
    {
        if (customFieldValuesHolder == null)
        {
            return new NullCustomFieldParams();
        }

        final CustomFieldParams customFieldParams = (CustomFieldParams) customFieldValuesHolder.get(getId());
        if (customFieldParams == null)
        {
            return new NullCustomFieldParams();
        }
        else
        {
            return customFieldParams;
        }
    }


    /**
     * Removes this custom field and returns a set of issue IDs of all issues that are affected by removal of this
     * custom field.
     *
     * @return a set of issue IDs of affected issues
     * @throws DataAccessException if removal of generic value fails
     */
    public Set<Long> remove() throws DataAccessException
    {
        CustomFieldType customFieldType = getCustomFieldType();

        Set<Long> issueIds;
        if (customFieldType != null)
        {
            issueIds = customFieldType.remove(this);
        }
        else
        {
            issueIds = Collections.emptySet();
        }

        try
        {
            gv.remove();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        return issueIds;
    }

    //------------------------ HELPER METHODS ---------------------------//

    /**
     * Returns custom field parameter from the given map that are relevant to this custom field.
     *
     * @param params map of parameters
     * @return custom field parameter from the given map that are relevant to this custom field
     */
    protected CustomFieldParams getRelevantParams(Map params)
    {
        final CustomFieldParamsImpl customFieldParams = new CustomFieldParamsImpl(this);
        for (Iterator iterator = params.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            final String key = (String) entry.getKey();
            final String customFieldKey = CustomFieldUtils.getCustomFieldKey(key);
            if (key != null && getId().equals(customFieldKey))
            {
                String[] p = (String[]) entry.getValue();

                if (p != null && p.length > 0)
                {
                    for (int i = 0; i < p.length; i++)
                    {
                        if (p[i] != null && p[i].length() > 0)
                        {
                            customFieldParams.addValue(CustomFieldUtils.getSearchParamSuffix(key), Arrays.asList(p));
                            i = p.length; //exit for loop
                        }
                    }
                }

            }
        }
        return customFieldParams;
    }

    public String toString()
    {
        return getName();
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Object customFieldValue = getRendererCustomFieldValue(fieldLayoutItem, issue, displayParams);  // this is so that the DocumentIssueImpl can return optimised results JRA-7300
        return getCustomFieldType().getDescriptor().getColumnViewHtml(this, customFieldValue, issue, displayParams, fieldLayoutItem);
    }

    private Object getRendererCustomFieldValue(FieldLayoutItem fieldLayoutItem, Issue issue, Map displayParams)
    {
        Object customFieldValue;
        if (isRenderable() && displayParams.get("excel_view") == null)
        {
            customFieldValue = rendererManager.getRenderedContent(fieldLayoutItem, issue);
        }
        else
        {
            customFieldValue = issue.getCustomFieldValue(this);
        }
        return customFieldValue;
    }

    private Object getRendererCustomFieldValue(FieldLayoutItem fieldLayoutItem, Issue issue, Object value)
    {
        Object customFieldValue;
        if (isRenderable() && value instanceof String)
        {
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            customFieldValue = rendererManager.getRenderedContent(rendererType, (String) value, issue.getIssueRenderContext());
        }
        else
        {
            customFieldValue = value;
        }
        return customFieldValue;
    }

    protected I18nHelper getI18nHelper()
    {
        return getCustomFieldType().getDescriptor().getI18nBean();
    }

    public String getHiddenFieldId()
    {
        return getId();
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue)
    {
        return getCreateHtml(fieldLayoutItem, operationContext, action, issue, new HashMap());
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map dispayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, dispayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, new HashMap());
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem,
                              OperationContext operationContext,
                              Action action,
                              Issue issue,
                              Map dispayParameters)
    {
        return getCustomFieldType().getDescriptor().getEditHtml(getRelevantConfig(issue),
                operationContext.getFieldValuesHolder(),
                issue,
                action,
                dispayParameters,
                fieldLayoutItem);
    }

    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        notNull("bulkEditBean", bulkEditBean);
        notEmpty("selectedIssues", bulkEditBean.getSelectedIssues());

        FieldLayoutItem fieldLayoutItem = null;
        if (bulkEditBean.getTargetFieldLayout() != null)
        {
            // This means we are in a bulk move operation, so we will be using the first target issue to render the edit HTML.
            fieldLayoutItem = bulkEditBean.getTargetFieldLayout().getFieldLayoutItem(this);

            // JRA-21669
            // When the Bulk Move Mapping feature was written in JIRA 4.1, we did not intend for it to work with any
            // Custom Field Type other than VersionCFType. A bug was later found that UserCFTypes were not happy with the
            // way our mapping code assumed that custom field values were always Longs. So to quickly fix this bug and
            // keep with our initial promise of only working with VersionCFType, we will do a nasty check on the fieldLayoutItem
            // to ensure that the field it refers to is actually a VersionCFType. All other custom fields will render their
            // bulk edit HTML when they are bulk moving -- as we always intended. This can be revisited in future if we
            // decide to add mapping during Bulk Move for all custom field types / other system fields.
            if (isCustomFieldTypeSupportedForDistinctValueMapping(fieldLayoutItem))
            {
                return getBulkMoveHtmlWithMapping(fieldLayoutItem, operationContext, action, bulkEditBean, displayParameters);
            }
            else
            {
                // have to use a target issue here so that we get the correct rendering of the edit control
                return getEditHtml(fieldLayoutItem, operationContext, action, bulkEditBean.getFirstTargetIssueObject(), displayParameters);
            }
        }
        else
        {
            // This means we are in a bulk edit or bulk workflow transition operation.
            // Since we do not allow bulk edit of fields that have differing renderer types we can safely
            // pick any issue to serve as our context issue for the call to editHtml
            if (!bulkEditBean.getFieldLayouts().isEmpty())
            {
                fieldLayoutItem = bulkEditBean.getFieldLayouts().iterator().next().getFieldLayoutItem(this);
            }
        }
        // Use one of the selected issues to render the edit HTML, do not use a target issue since we are not in a bulk move operation here (only above).
        return getEditHtml(fieldLayoutItem, operationContext, action, bulkEditBean.getSelectedIssues().iterator().next(), displayParameters);
    }

    private String getBulkMoveHtmlWithMapping(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
    {
        // The HTML displayed for Bulk Move of Version Custom Fields needs to allow the user to specify mappings for
        // each old version present in the currently selected issues.
        final Issue issue = bulkEditBean.getFirstTargetIssueObject();

        final BulkMoveHelper bulkMoveHelper = new DefaultBulkMoveHelper();

        // this function will retrieve the custom field's values as a collection of Strings which represent in the case
        // of VersionCFType the ids of the versions selected.
        final Function<Issue, Collection<Object>> issueValueResolver = new Function<Issue, Collection<Object>>()
        {
            public Collection<Object> get(final Issue issue)
            {
                final Map fieldValuesHolder = new LinkedHashMap();
                populateFromIssue(fieldValuesHolder, issue);
                final Object o = fieldValuesHolder.get(getId());
                final CustomFieldParams customFieldParams = (CustomFieldParams) o;
                return customFieldParams.getAllValues();
            }
        };

        // this function needs to be able to resolve the values that come back from the issueValueResolver function
        // into names that can be displayed to the user.
        final Function<Object, String> nameResolver = new Function<Object, String>()
        {
            public String get(final Object input)
            {
                // BEGIN HACK
                // At the time of writing, only Version custom fields are supported for Bulk Move. Since there is no
                // way to get the "name" of a custom field value using the CustomFieldType interface, we have to
                // hardcode what value types we are expecting and explicitly retrieve their name value.
                Object result = getCustomFieldType().getSingularObjectFromString((String) input);
                if (result == null)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Could not resolve name for input '" + input + "'.");
                    }
                }
                else
                {
                    if (result instanceof Version)
                    {
                        return ((Version)result).getName();
                    }
                    else
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("Type '" + result.getClass() + "' currently not supported for bulk move.");
                        }
                    }
                }
                return null;
                // END HACK
            }
        };

        final Map<Long, BulkMoveHelper.DistinctValueResult> distinctValues = bulkMoveHelper.getDistinctValuesForMove(bulkEditBean, this, issueValueResolver, nameResolver);

        return getCustomFieldType().getDescriptor().getBulkMoveHtml(getRelevantConfig(issue),
                operationContext.getFieldValuesHolder(),
                issue,
                action,
                displayParameters,
                fieldLayoutItem,
                distinctValues,
                bulkMoveHelper);
    }

    /**
     * As part of JRA-21669, we need to prevent custom fields which are not of {@link VersionCFType} from trying to do
     * the mapping-specific part of the Bulk Move rendering code.
     *
     * @param fieldLayoutItem the field layout item that represents the custom field being rendered
     * @return true if the custom field can support distinct value mapping; false otherwise.
     */
    private boolean isCustomFieldTypeSupportedForDistinctValueMapping(final FieldLayoutItem fieldLayoutItem)
    {
        final CustomField customField = (CustomField) fieldLayoutItem.getOrderableField();
        return (customField.getCustomFieldType() instanceof VersionCFType);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue)
    {
        return getViewHtml(fieldLayoutItem, action, issue, new HashMap());
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        return getCustomFieldType().getDescriptor().getViewHtml(this, getRendererCustomFieldValue(fieldLayoutItem, issue, displayParameters), issue, fieldLayoutItem, displayParameters);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        return getCustomFieldType().getDescriptor().getViewHtml(this, getRendererCustomFieldValue(fieldLayoutItem, issue, value), issue, fieldLayoutItem, displayParameters);
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public Object getDefaultValue(Issue issue)
    {
        FieldConfig config = getRelevantConfig(issue);
        return getCustomFieldType().getDefaultValue(config);
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        try
        {
            if (fieldValueHolder.containsKey(getId()))
            {
                CustomFieldParams customFieldParams = (CustomFieldParams) fieldValueHolder.get(getId());
                issue.setCustomFieldValue(this, getCustomFieldType().getValueFromCustomFieldParams(customFieldParams));
            }
        }
        catch (FieldValidationException e)
        {
            // The exception should not be thrown here as before the issue is updated validation should have been done.
            throw new DataAccessException(e);
        }
    }


    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (Iterator iterator = originalIssues.iterator(); iterator.hasNext();)
        {
            Issue originalIssue = (Issue) iterator.next();

            // If the field was not originally in scope
            if (!isInScope(originalIssue.getProjectObject(), ImmutableList.of(originalIssue.getIssueTypeObject().getId())))
            {
                return new MessagedResult(true);
            }

            // Field required in target, blank in original
            if (!doesFieldHaveValue(originalIssue) && targetFieldLayoutItem != null && targetFieldLayoutItem.isRequired())
            {
                return new MessagedResult(true);
            }

            // If has value but fails error validation
            if (doesFieldHaveValue(originalIssue))
            {
                // Validates the value with the custom field
                CustomFieldParams customFieldParams = getCustomFieldParamsFromIssue(originalIssue);

                ErrorCollection errorCollection = new SimpleErrorCollection();
                FieldConfig config = getRelevantConfig(targetIssue);

                // BEGIN HACK
                // NOTE: This is a hack to temporarily fix the problem that a version custom field
                // needs to prompt a user for new values when doing a move operation and that operation
                // will end up moving the issue to a new project. This is because versions are a project
                // specific entity. The real fix is to somehow allow the CustomFieldType to determine if
                // it needs to move. At the moment the validateFromParams is being used for this but that
                // in itself is a hack and it does not get passed enough information, it needs the target
                // context so it can make context relevant decisions (like the versions changing because
                // of a project move).
                // See JRA-21726 for more info.
                if (getCustomFieldType() instanceof VersionCFType)
                {
                    if (!originalIssue.getProject().getLong("id").equals(targetIssue.getProject().getLong("id")))
                    {
                        return new MessagedResult(true);
                    }
                }
                // END HACK

                getCustomFieldType().validateFromParams(customFieldParams, errorCollection, config);

                if (errorCollection.hasAnyErrors())
                {
                    log.debug("Move required. Errors occurred in automatic moving: " +
                            ToStringBuilder.reflectionToString(errorCollection.getErrorMessages()));
                    return new MessagedResult(true);
                }
            }

            // Also if the field is renderable and the render types differ prompt with an edit or a warning
            if (isRenderable() && doesFieldHaveValue(originalIssue))
            {
                FieldLayoutItem fieldLayoutItem = null;
                try
                {
                    fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(originalIssue.getProject(), originalIssue.getIssueTypeObject().getId()).getFieldLayoutItem(getId());
                }
                catch (DataAccessException e)
                {
                    log.warn(getName() + " field was unable to resolve the field layout item for issue " + originalIssue.getId(), e);
                }

                String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
                String targetRendererType = (targetFieldLayoutItem != null) ? targetFieldLayoutItem.getRendererType() : null;
                if (!rendererTypesEqual(rendererType, targetRendererType))
                {
                    if (originalIssues.size() > 1)
                    {
                        return new MessagedResult(false, getI18nHelper().getText("renderer.bulk.move.warning"), MessagedResult.WARNING);
                    }
                    else
                    {
                        return new MessagedResult(true);
                    }
                }
            }

        }
        return new MessagedResult(false);
    }

    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // NOTE: this method should be delegated off to the CustomFieldType's so that they
        // can populate the value correctly. This is being left as is until the time can
        // be spent to implement each custom field types impl of this to-be-created-method
        // correctly.
        if (isRenderable())
        {
            // return the original value
            fieldValuesHolder.put(getId(), getCustomFieldParamsFromIssue(originalIssue));
        }
        else
        {
            // If the field needs to be updated then it should be populated with default values
            populateDefaults(fieldValuesHolder, targetIssue);
        }
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setCustomFieldValue(this, null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return (getValue(issue) != null);
    }

    /**
     * This is a hacked version of {@link #hasValue(com.atlassian.jira.issue.Issue)} which specifically looks at Labels
     * custom field types and determines if the value returned by them is an empty Collection. For the callers of this
     * method, the intent is that an empty Collection should not be considered as "having a value".
     * <p/>
     * This hack must exist as there is no way for custom field types to provide a way of telling if they "need moving".
     * See JRA-21726 for more info.
     *
     * @param issue the issue to look up the value for
     * @return true if the field has a value on the issue; for Labels custom field types this means it must be a non-empty
     * {@link Collection}.
     */
    private boolean doesFieldHaveValue(Issue issue)
    {
        final Object value = getValue(issue);
        if (value != null && getCustomFieldType() instanceof LabelsCFType && value instanceof Collection)
        {
            final Collection c = (Collection) value;
            return !c.isEmpty();
        }
        return value != null;
    }

    public String getId()
    {
        // This value is cached, as this method is called thousands of time for any search / sort,
        // and generates a *lot* of garbage otherwise (approx 250k garbage per 12k issues).
        return customFieldId;
    }

    public String getValueFromIssue(Issue issue)
    {
        Object value = getValue(issue);

        if (!(value instanceof String))
        {
            return null;
        }
        else
        {
            return (String) value;
        }
    }

    /**
     * Returns ID of this custom field.
     *
     * @return ID of this custom field
     */
    public Long getIdAsLong()
    {
        return gv.getLong(ENTITY_ID);
    }

    /**
     * Returns a list of configuration schemes.
     *
     * @return a list of {@link FieldConfigScheme} objects
     */
    public List<FieldConfigScheme> getConfigurationSchemes()
    {
        return configurationSchemes.get();
    }

    /**
     * Sets the configuration schemes for this custom field.
     *
     * @param configurationSchemes a list of {@link FieldConfigScheme} objects
     */
    public void setConfigurationSchemes(List<FieldConfigScheme> configurationSchemes)
    {
        this.configurationSchemes.set(configurationSchemes);
    }

    public String getNameKey()
    {
        return gv.getString(ENTITY_NAME);
    }


    private CustomFieldParams getCustomFieldParamsFromIssue(Issue issue)
    {
        //JRA-16915: The CustomFieldParams returned should NEVER be null, since this may break calls to validate() methods
        //lateron with a NPE.  This can cause problems for example, when deleting values in a workflow transition
        //via SOAP, if a customfield value for an issue is null.
        CustomFieldParams paramsFromIssue = new CustomFieldParamsImpl(this);

        Object valueFromIssue = getCustomFieldType().getValueFromIssue(this, issue);

        if (valueFromIssue != null)
        {
            paramsFromIssue = new CustomFieldParamsImpl(this, valueFromIssue);
            paramsFromIssue.transformObjectsToStrings();
        }
        return paramsFromIssue;
    }

    // ------------------------------------------------------------------------------------- Convenience Context Methods

    /**
     * Returns a list of associated project categories for this custom field.
     * It returns null if {@link #getConfigurationSchemes()} returns null.
     * It returns an empty list if the {@link #getConfigurationSchemes()} returns an empty list.
     * The returned list is sorted by name using {@link OfBizComparators#NAME_COMPARATOR}.
     *
     * @return a list of {@link org.ofbiz.core.entity.GenericValue} objects that represent associated project categories
     *         as {@link com.atlassian.jira.issue.context.ProjectCategoryContext} objects
     */
    public List<GenericValue> getAssociatedProjectCategories()
    {
        List<GenericValue> projectCategories = null;
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        if (configurations != null)
        {
            projectCategories = new LinkedList<GenericValue>();
            for (final FieldConfigScheme config : configurations)
            {
                List<GenericValue> configProject = config.getAssociatedProjectCategories();
                if (configProject != null)
                {
                    projectCategories.addAll(configProject);
                }
            }

            Collections.sort(projectCategories, OfBizComparators.NAME_COMPARATOR);
        }
        return projectCategories;
    }

    public List<GenericValue> getAssociatedProjects()
    {
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        List<GenericValue> projects = null;
        if (configurations != null)
        {
            projects = new LinkedList<GenericValue>();
            for (final FieldConfigScheme config : configurations)
            {
                List<GenericValue> configProject = config.getAssociatedProjects();
                if (configProject != null)
                {
                    projects.addAll(configProject);
                }
            }

            Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
        }

        return projects;
    }


    public List<GenericValue> getAssociatedIssueTypes()
    {
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        List<GenericValue> issueTypes = null;
        if (configurations != null)
        {
            Set<GenericValue> issueTypesSet = new HashSet<GenericValue>();
            for (final FieldConfigScheme config : configurations)
            {
                Set<GenericValue> configIssueType = config.getAssociatedIssueTypes();
                if (configIssueType != null)
                {
                    issueTypesSet.addAll(configIssueType);
                }
            }

            issueTypes = new ArrayList<GenericValue>(issueTypesSet);
            Collections.sort(issueTypes, OfBizComparators.NAME_COMPARATOR);
        }

        return issueTypes;
    }

    /**
     * Returns true if this custom field applies for all projects and all issue types.
     *
     * @return true if it is in all projects and all issue types, false otherwise.
     */
    public boolean isGlobal()
    {
        return isAllProjects() && isAllIssueTypes();
    }

    /**
     * Checks whether this custom field applies for all projects. It returns true if it applies for all projects
     * for any field configuration scheme, false otherwise.
     *
     * @return true if it applies for all projects for any field configuration scheme, false otherwise.
     */
    public boolean isAllProjects()
    {
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        if (configurations != null)
        {
            for (FieldConfigScheme configuration : configurations)
            {
                if (configuration.isAllProjects())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns true if it applies for all issue types, false otherwise. The actual check test if the list returned by
     * {@link #getAssociatedIssueTypes()} contains null - all issue types.
     *
     * @return true if it applies for all issue types, false otherwise.
     */
    public boolean isAllIssueTypes()
    {
        final List<GenericValue> issueTypes = getAssociatedIssueTypes();
        return issueTypes != null && issueTypes.contains(null);
    }

    /**
     * Returns true if all configuration schemes returned by {@link #getConfigurationSchemes()} are enabled.
     *
     * @return true if all configuration schemes are enabled, false otherwise
     */
    public boolean isEnabled()
    {
        final List<FieldConfigScheme> configurations = getConfigurationSchemes();
        if (configurations != null)
        {
            for (Iterator iterator = configurations.iterator(); iterator.hasNext();)
            {
                FieldConfigScheme config = (FieldConfigScheme) iterator.next();
                if (config.isEnabled())
                {
                    return true;
                }
            }
        }

        return false;
    }

    // ------------------------------------------------------------------------------------------------------- Bulk Edit

    /**
     * Checks if custom field is available for bulk edit operation, whether 'shown' and if user has bulk update permission.
     * Also checks that all selected issues have the same field config for this custom field. All field configs must be
     * the same or all null.
     *
     * @param bulkEditBean bulk edit bean
     * @return null if available for bulk edit or an appropriate 'unavailable' string
     */
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Check for custom field specific requirements
        CustomFieldType customFieldType = getCustomFieldType();
        String customFieldAvailabilityString = customFieldType.availableForBulkEdit(bulkEditBean);

        if (TextUtils.stringSet(customFieldAvailabilityString))
        {
            return customFieldAvailabilityString;
        }

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (final Object o : bulkEditBean.getFieldLayouts())
        {
            FieldLayout fieldLayout = (FieldLayout) o;
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }
        }

        FieldConfig config = null;
        boolean first = true;
        for (final Object o : bulkEditBean.getSelectedIssues())
        {
            Issue issue = (Issue) o;
            if (!(hasBulkUpdatePermission(bulkEditBean, issue)))
            {
                return "bulk.edit.unavailable.permission";
            }

            // Check if it's the same config
            FieldConfig currentConfig = getRelevantConfig(issue);
            // Grab the first config - we will compare all other configs with this one.
            if (first)
            {
                if (currentConfig == null)
                {
                    // Found an issue for which we do not have a relevant config - the field should not
                    // be available for bulk operation
                    return "bulk.edit.incompatible.customfields";
                }
                else
                {
                    config = currentConfig;
                    first = false;
                }
            }
            // Compare the configurations - return the text if the configs are different
            // Note: null config is not equal to non-null config
            else if (areDifferent(config, currentConfig))
            {
                return "bulk.edit.incompatible.customfields";
            }
        }

        // Make sure that if this field is renderable that it has no conflicting render types in any field layouts
        // that the select issues belong to.
        if (isRenderable())
        {
            String rendererType = null;
            for (final Object o : bulkEditBean.getFieldLayouts())
            {
                FieldLayout fieldLayout = (FieldLayout) o;
                String tempRendererType = fieldLayout.getRendererTypeForField(getId());
                if (rendererType == null)
                {
                    rendererType = tempRendererType;
                }
                else if (!rendererType.equals(tempRendererType))
                {
                    return "bulk.edit.unavailable.inconsistent.rendertypes";
                }
            }
        }

        return null;
    }

    /**
     * Compare two objects - return false if both are null or equal. Return true otherwise.
     *
     * @param obj1 the first object to compare
     * @param obj2 the second object to compare
     * @return false if both are null or equal. Return true otherwise.
     */
    protected static boolean areDifferent(Object obj1, Object obj2)
    {
        return (obj1 != null && !obj1.equals(obj2)) || (obj1 == null && obj2 != null);
    }

    /**
     * Checks whether the user has the permission to execute the bulk operation for the provided issue. In case of Bulk
     * Workflow Transition checks for nothing. In case of all others (e.g. Bulk Edit) checks for Edit permission.
     *
     * @param bulkEditBean bulk edit bean
     * @param issue        issue to check permission on
     * @return true if has permission, false otherwise
     */
    protected boolean hasBulkUpdatePermission(BulkEditBean bulkEditBean, Issue issue)
    {
        // Do not check the permission if we are doing a bulk workflow transition. Bulk Workflow
        // transition is only protected by the workflow conditions of the transition and should not
        // hardcode a check for a permission here.
        // For bulk edit we should check whether the user has the edit permission for the issue
        return BulkWorkflowTransitionOperation.NAME.equals(bulkEditBean.getOperationName()) ||
                permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue.getGenericValue(), authenticationContext.getUser());
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators

    /**
     * Returns the name of this custom field by reading {@link #ENTITY_NAME} of the underlying generic value.
     *
     * @return the name of this custom field
     */
    public String getName()
    {
        return gv.getString(ENTITY_NAME);
    }

    /**
     * Sets the name of this custom field by setting the {@link #ENTITY_NAME} of the underlying generic value.
     * The name is abbreviated to a number of characters equal to {@link FieldConfigPersister#ENTITY_LONG_TEXT_LENGTH}.
     *
     * @param name name to set
     */
    public void setName(String name)
    {
        gv.setString(ENTITY_NAME, StringUtils.abbreviate(name, FieldConfigPersister.ENTITY_LONG_TEXT_LENGTH));
    }

    /**
     * Returns the description of this custom field by reading {@link #ENTITY_DESCRIPTION} of the underlying generic value.
     *
     * @return the description of this custom field
     */
    public String getDescription()
    {
        return gv.getString(ENTITY_DESCRIPTION);
    }

    /**
     * Sets the description of this custom field by setting the {@link #ENTITY_DESCRIPTION} of the underlying generic
     * value.
     *
     * @param description description to set
     */
    public void setDescription(String description)
    {
        gv.setString(ENTITY_DESCRIPTION, description);
    }

    /**
     * Retrieves the {@link CustomFieldSearcher} for this custom field looking it up in the customFieldManager
     * by the searcher key retrieved from {@link #ENTITY_CUSTOM_FIELD_SEARCHER} underlying generic value attribute.
     * The seracher, if found is initialized with this custom field before it is returned.
     *
     * @return found custom field searcher or null, if none found
     */
    public CustomFieldSearcher getCustomFieldSearcher()
    {
        return searcherRef.get();
    }

    /**
     * Sets the {@link CustomFieldSearcher} for this custom field by setting the {@link #ENTITY_CUSTOM_FIELD_SEARCHER}
     * underlying generic value attribute to the value of the key retrieved from the searcher.
     *
     * @param searcher custom field searcher to associate with this custom field
     */
    public void setCustomFieldSearcher(CustomFieldSearcher searcher)
    {
        String key = null;
        if (searcher != null)
        {
            key = searcher.getDescriptor().getCompleteKey();
        }

        gv.setString(ENTITY_CUSTOM_FIELD_SEARCHER, key);
    }

    /**
     * Looks up the {@link com.atlassian.jira.issue.customfields.CustomFieldType} in the {@link #customFieldManager} by
     * the key retrieved from the {@link #ENTITY_CF_TYPE_KEY} attribute of the underlying generic value.
     * This only happens once if {@link #typeRef} is null, then the custom field type is set and returned each time.
     * It can return null if the custom field type cannot be found by that key.
     *
     * @return custom field type
     */
    public CustomFieldType getCustomFieldType()
    {
        return typeRef.get();
    }


    public String getColumnHeadingKey()
    {
        return gv.getString(ENTITY_NAME);
    }

    public String getColumnCssClass()
    {
        return getId();
    }

    /**
     * Returns {@link #ORDER_ASCENDING}.
     *
     * @return ascending order as {@link #ORDER_ASCENDING} value
     */
    public String getDefaultSortOrder()
    {
        return ORDER_ASCENDING;
    }

    // ---------------------------------------------------------------------------------------------- Compare & Equality
    private boolean isSortable()
    {
        return (getSortComparatorSource() != null);
    }

    boolean valuesEqual(Object v1, Object v2)
    {
        return getCustomFieldType().valuesEqual(v1, v2);
    }

    /**
     * This method compares the values of this custom field in two given issues.
     * <p/>
     * Returns a negative integer, zero, or a positive integer as the value of first issue is less than, equal to,
     * or greater than the value of the second issue.
     * <p/>
     * This method returns 0 if this custom field is not sortable, or its customFieldType is not an instance
     * of {@link SortableCustomField}
     * <p/>
     * If either of given issues is null a IllegalArgumentException is thrown.
     *
     * @param issue1 issue to compare
     * @param issue2 issue to compare
     * @return a negative integer, zero, or a positive integer as the value of first issue is less than, equal to, or
     *         greater than the value of the second issue
     * @throws IllegalArgumentException if any of given issues is null
     */
    public int compare(Issue issue1, Issue issue2) throws IllegalArgumentException
    {
        if (!isSortable())
        {
            log.error("Called compare method, even though not comparable");
            return 0;
        }

        if (issue1 == null && issue2 == null)
        {
            throw new IllegalArgumentException("issue1 and issue2 are null");
        }

        if (issue1 == null)
        {
            throw new IllegalArgumentException("issue1 is null");
        }

        if (issue2 == null)
        {
            throw new IllegalArgumentException("issue2 is null");
        }

        Object v1 = getValue(issue1);
        Object v2 = getValue(issue2);

        if (v1 == v2)
        {
            return 0;
        }

        if (v1 == null)
        {
            return 1; // null values at the end?
        }

        if (v2 == null)
        {
            return -1;
        }

        // Ensure that both of the contexts are the same and then compare
        final CustomFieldType customFieldType = getCustomFieldType();
        if (customFieldType instanceof SortableCustomField)
        {
            SortableCustomField sortable = (SortableCustomField) customFieldType;
            FieldConfig c1 = getRelevantConfig(issue1);
            FieldConfig c2 = getRelevantConfig(issue2);
            if (c1 == null || !c1.equals(c2))
            {
                log.info("Sort order for custom field " + this + " for issues " + issue1 + " and " + issue2 + " " +
                        "contexts did not match. Sort order may be incorrect");
            }
            return sortable.compare(v1, v2, c1);
        }

        return 0;
    }

    /**
     * If this field has a searcher, and this searcher implements {@link SortableCustomFieldSearcher} then return
     * {@link SortableCustomFieldSearcher#getSorter(CustomField)}.  Else return null.
     */
    public LuceneFieldSorter getSorter()
    {
        if (getCustomFieldSearcher() instanceof SortableCustomFieldSearcher)
        {
            return ((SortableCustomFieldSearcher) getCustomFieldSearcher()).getSorter(this);
        }
        else
        {
            return null;
        }
    }

    /**
     * Return a SortComparatorSource that uses either a custom field searcher that implements
     * {@link SortableCustomFieldSearcher} or a custom field that implements {@link SortableCustomField}.
     * If neither are found, this method returns null.
     */
    public SortComparatorSource getSortComparatorSource()
    {
        LuceneFieldSorter sorter = getSorter();
        if (sorter != null)
        {
            return new MappedSortComparator(sorter);
        }
        else if (getCustomFieldType() instanceof SortableCustomField)
        {
            return new DocumentSortComparatorSource(new IssueSortComparator(new CustomFieldIssueSortComparator(this)));
        }
        else
        {
            return null;
        }
    }

    static class CustomFieldIssueSortComparator implements IssueComparator
    {
        private final CustomField customField;

        public CustomFieldIssueSortComparator(CustomField customField)
        {
            if (customField == null)
            {
                throw new NullPointerException("Custom field cannot be null.");
            }

            this.customField = customField;
        }

        public int compare(Issue issue1, Issue issue2)
        {
            // Use the custom field to sort issues.
            return this.customField.compare(issue1, issue2);
        }

        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final CustomFieldIssueSortComparator that = (CustomFieldIssueSortComparator) o;

            return customField.getId().equals(that.customField.getId());
        }

        public int hashCode()
        {
            return customField.getId().hashCode();
        }
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Field))
        {
            return false;
        }

        final Field field = (Field) o;

        String id = getId();
        String fieldId = field.getId();
        return !(id != null ? !id.equals(fieldId) : fieldId != null);
    }

    public int hashCode()
    {
        return (getId() != null ? getId().hashCode() : 0);
    }

    /**
     * Constant lazy reference to the CustomFieldSearcher for this CustomFieldType.
     */
    class CustomFieldSearcherLazyRef extends LazyReference<CustomFieldSearcher>
    {
        @Override
        protected CustomFieldSearcher create() throws Exception
        {
            final String customFieldSearcherKey = gv.getString(ENTITY_CUSTOM_FIELD_SEARCHER);
            final CustomFieldSearcher customFieldSearcher = customFieldManager.getCustomFieldSearcher(customFieldSearcherKey);

            if (customFieldSearcher != null)
            {
                try
                {
                    customFieldSearcher.init(CustomFieldImpl.this);
                }
                catch (Exception exception)
                {
                    // JRA-18412
                    // we don't want to prevent JIRA from continuing to operate so we swallow exceptions during initialization.
                    // returning null from here will result in the custom field not being searchable which is acceptable
                    // when something catastrophic happens.
                    log.error(String.format("Exception during searcher initialization of the custom field %s:", customFieldSearcherKey), exception);
                    return null;
                }
            }

            return customFieldSearcher;
        }
    }

    /**
     * Constant lazy reference for this CustomFieldImpl's CustomFieldType.
     */
    class CustomFieldTypeLazyRef extends LazyReference<CustomFieldType>
    {
        @Override
        protected CustomFieldType create() throws Exception
        {
            final String customFieldKey = gv.getString(ENTITY_CF_TYPE_KEY);
            if (customFieldKey != null)
            {
                return customFieldManager.getCustomFieldType(customFieldKey);
            }

            return null;
        }
    }

    // -------------------------------------------------------------------------------------------------- Deprecated

    /**
     * Returns a generic value that represents this custom field
     *
     * @return generic value of this custom field
     * @deprecated
     */
    public GenericValue getGenericValue()
    {
        return gv;
    }

    public int compareTo(Object o)
    {
        if (o == null)
        {
            return 1;
        }
        else if (o instanceof Field)
        {
            Field field = (Field) o;
            if (getName() == null)
            {
                if (field.getName() == null)
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
            else
            {
                if (field.getName() == null)
                {
                    return 1;
                }
                else
                {
                    return getName().compareTo(field.getName());
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("Can only compare Field objects.");
        }

    }

    /**
     * Null-safe comparison of renderer type strings.
     *
     * @param oldRendererType old renderer type to compare
     * @param newRendererType new renderer type to compare
     * @return true if both are null or equal, false otherwise
     */
    private boolean rendererTypesEqual(String oldRendererType, String newRendererType)
    {
        return (oldRendererType == null && newRendererType == null)
                || ((oldRendererType != null) && oldRendererType.equals(newRendererType));
    }
}
