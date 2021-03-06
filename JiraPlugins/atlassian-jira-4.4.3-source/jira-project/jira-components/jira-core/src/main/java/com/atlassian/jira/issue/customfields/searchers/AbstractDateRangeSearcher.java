package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.converters.DateConverter;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.DateCustomFieldSearchRenderer;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DateCustomFieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.DateSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.issue.statistics.DateFieldSorter;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.DateClauseQueryFactory;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.validator.DateValidator;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.query.operator.Operator;
import com.atlassian.velocity.VelocityManager;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractDateRangeSearcher extends AbstractInitializationCustomFieldSearcher implements SortableCustomFieldSearcher, CustomFieldSearcher
{
    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    final JqlOperandResolver jqlOperandResolver;
    private final TimeZoneManager timeZoneManager;
    final FieldVisibilityManager fieldVisibilityManager;
    final VelocityRequestContextFactory velocityRenderContext;
    final ApplicationProperties applicationProperties;
    final VelocityManager velocityManager;
    final CalendarLanguageUtil calendarUtils;
    final DateConverter dateConverter;
    final JqlDateSupport dateSupport;

    public AbstractDateRangeSearcher(final JqlOperandResolver jqlOperandResolver, final VelocityRequestContextFactory velocityRenderContext, final ApplicationProperties applicationProperties, final VelocityManager velocityManager, final CalendarLanguageUtil calendarUtils, final DateConverter dateConverter, final JqlDateSupport dateSupport, TimeZoneManager timeZoneManager)
    {
        this.jqlOperandResolver = jqlOperandResolver;
        this.timeZoneManager = timeZoneManager;
        this.fieldVisibilityManager = new FieldVisibilityBean();
        this.velocityRenderContext = velocityRenderContext;
        this.applicationProperties = applicationProperties;
        this.velocityManager = velocityManager;
        this.calendarUtils = calendarUtils;
        this.dateConverter = dateConverter;
        this.dateSupport = dateSupport;
    }

    public void init(final CustomField field)
    {
        final ClauseNames names = field.getClauseNames();

        final Set<Operator> supportedOperators = OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY;
        final SimpleFieldSearchConstants constants = new SimpleFieldSearchConstants(field.getId(), names, field.getId(),
                field.getId(), field.getId(), supportedOperators, JiraDataTypes.DATE);
        final FieldIndexer indexer = new DateCustomFieldIndexer(fieldVisibilityManager, field);
        final String nameKey = "navigator.filter." + field.getId();
        final DateSearcherConfig config = new DateSearcherConfig(field.getId(), names, field.getName());

        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer>singletonList(indexer), new AtomicReference<CustomField>(field));
        this.searchInputTransformer = createSearchInputTransformer(config, timeZoneManager);
        this.searchRenderer = createSearchRenderer(field, constants, nameKey, config);

        this.customFieldSearcherClauseHandler = new SimpleCustomFieldSearcherClauseHandler(new DateValidator(jqlOperandResolver, timeZoneManager),
                new DateClauseQueryFactory(constants, dateSupport, jqlOperandResolver),
                supportedOperators, JiraDataTypes.DATE);
    }

    abstract DateSearchInputTransformer createSearchInputTransformer(final DateSearcherConfig config, TimeZoneManager timeZoneManager);

    abstract DateCustomFieldSearchRenderer createSearchRenderer(final CustomField field, final SimpleFieldSearchConstants constants, final String nameKey, final DateSearcherConfig config);

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
        if (searcherInformation == null)
        {
            throw new IllegalStateException("Attempt to retrieve Sorter off uninitialised custom field searcher.");
        }
        return new DateFieldSorter(searcherInformation.getId());
    }
}
