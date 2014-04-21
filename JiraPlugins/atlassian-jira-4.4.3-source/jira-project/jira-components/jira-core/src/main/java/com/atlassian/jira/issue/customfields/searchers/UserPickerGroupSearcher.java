package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.converters.UserConverter;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.UserPickerGroupCustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.UserCustomFieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.UserSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.UserFitsNavigatorHelper;
import com.atlassian.jira.issue.statistics.CustomFieldUserStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.UserCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.resolver.UserResolver;
import com.atlassian.jira.jql.validator.UserCustomFieldValidator;
import com.atlassian.jira.jql.values.UserClauseValuesGenerator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.velocity.VelocityManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class UserPickerGroupSearcher extends AbstractInitializationCustomFieldSearcher
        implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable
{
    private final String USER_SELECT_SUFFIX = "Select";

    private final UserConverter userConverter;
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final VelocityManager velocityManager;
    private final ApplicationProperties applicationProperties;
    private final UserPickerSearchService userPickerSearchService;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JqlOperandResolver jqlOperandResolver;
    private final UserResolver userResolver;
    private final UserManager userManager;
    private CustomFieldInputHelper customFieldInputHelper;
    private SearcherInformation<CustomField> searcherInformation;
    private SearchRenderer searchRenderer;
    private SearchInputTransformer searchInputTransformer;
    private SimpleCustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private final GroupManager groupManager;

    public UserPickerGroupSearcher(final UserConverter userConverter, final JiraAuthenticationContext jiraAuthenticationContext,
            VelocityRequestContextFactory velocityRequestContextFactory, VelocityManager velocityManager, ApplicationProperties applicationProperties,
            UserPickerSearchService userPickerSearchService, FieldVisibilityManager fieldVisibilityManager, JqlOperandResolver jqlOperandResolver,
            UserResolver userResolver, UserManager userManager, final CustomFieldInputHelper customFieldInputHelper, GroupManager groupManager)
    {
        this.userConverter = userConverter;
        this.authenticationContext = jiraAuthenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.velocityManager = velocityManager;
        this.applicationProperties = applicationProperties;
        this.userPickerSearchService = userPickerSearchService;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jqlOperandResolver = jqlOperandResolver;
        this.userResolver = userResolver;
        this.userManager = userManager;
        this.customFieldInputHelper = customFieldInputHelper;
        this.groupManager = groupManager;
    }

    /**
     * This is the first time the searcher knows what its ID and names are
     *
     * @param field the Custom Field for this searcher
     */
    public void init(CustomField field)
    {
        final ClauseNames names = field.getClauseNames();
        UserFieldSearchConstants searchConstants = new UserFieldSearchConstants(field.getId(), names, field.getId(), field.getId()+USER_SELECT_SUFFIX, field.getId(),
                field.getId(), DocumentConstants.ISSUE_CURRENT_USER, DocumentConstants.SPECIFIC_USER,
                DocumentConstants.SPECIFIC_GROUP, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);

        final UserCustomFieldIndexer indexer = new UserCustomFieldIndexer(fieldVisibilityManager, field, userConverter);
        final UserFitsNavigatorHelper userFitsNavigatorHelper = new UserFitsNavigatorHelper(userPickerSearchService);

        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchRenderer = new UserPickerGroupCustomFieldRenderer(field, searchConstants, field.getNameKey(), velocityRequestContextFactory, applicationProperties, velocityManager, userPickerSearchService, fieldVisibilityManager);

        searchInputTransformer = new UserSearchInputTransformer(searchConstants, userFitsNavigatorHelper, groupManager, userManager);

        this.customFieldSearcherClauseHandler = new SimpleCustomFieldValueGeneratingClauseHandler(new UserCustomFieldValidator(userResolver, jqlOperandResolver),
                        new UserCustomFieldClauseQueryFactory(field.getId(), userResolver, jqlOperandResolver),
                new UserClauseValuesGenerator(userPickerSearchService),
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
                JiraDataTypes.USER);
    }

    public SearcherInformation<CustomField> getSearchInformation()
    {
        if (searcherInformation == null)
        {
            throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
        }
        return searcherInformation;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        if (searchInputTransformer == null)
        {
            throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
        }
        return searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer()
    {
        if (searchRenderer == null)
        {
            throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
        }
        return searchRenderer;
    }

    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler()
    {
        if (customFieldSearcherClauseHandler == null)
        {
            throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
        }
        return customFieldSearcherClauseHandler;
    }

    public LuceneFieldSorter getSorter(CustomField customField)
    {
        return new CustomFieldUserStatisticsMapper(customField, userManager, authenticationContext, customFieldInputHelper);
    }

    public StatisticsMapper getStatisticsMapper(CustomField customField)
    {
        return new CustomFieldUserStatisticsMapper(customField, userManager, authenticationContext, customFieldInputHelper);
    }
}