package com.atlassian.jira.issue.search.searchers.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DescriptionIndexer;
import com.atlassian.jira.issue.index.indexers.impl.EnvironmentIndexer;
import com.atlassian.jira.issue.index.indexers.impl.SummaryIndexer;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.QuerySearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.QuerySearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class QuerySearcher extends AbstractInitializationSearcher implements IssueSearcher<SearchableField>
{
    /**
     * The ID of the query searcher. 
     */
    public static final String ID = SystemSearchConstants.QUERY_SEARCHER_ID;

    /**
     * The the key in the FieldValuesHolder that contains a list of fields to search.
     */
    public static final String QUERY_FIELDS_ID = "queryFields";

    /**
     * A set of all the JQL clause names that the searcher should recognise. This inclues clause names for environment,
     * comment, description and summary.
     */
    public static final Set<String> QUERY_JQL_FIELD_NAMES;

    /**
     * The set of URL parameters that deal with field selection.
     */
    public static final Set<String> QUERY_URL_FIELD_PARAMS;

    /**
     * The URL parameter that actually contains the text to search. 
     */
    public static final String QUERY_URL_PARAM = "query";

    private static final String NAME_KEY = "common.words.query";

    static
    {
        Set<String> queryJqlFieldName = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        queryJqlFieldName.addAll(SystemSearchConstants.forSummary().getJqlClauseNames().getJqlFieldNames());
        queryJqlFieldName.addAll(SystemSearchConstants.forDescription().getJqlClauseNames().getJqlFieldNames());
        queryJqlFieldName.addAll(SystemSearchConstants.forEnvironment().getJqlClauseNames().getJqlFieldNames());
        queryJqlFieldName.addAll(SystemSearchConstants.forComments().getJqlClauseNames().getJqlFieldNames());

        QUERY_JQL_FIELD_NAMES = Collections.unmodifiableSet(queryJqlFieldName);

        QUERY_URL_FIELD_PARAMS = CollectionBuilder.newBuilder(SystemSearchConstants.forSummary().getUrlParameter(),
                SystemSearchConstants.forDescription().getUrlParameter(),
                SystemSearchConstants.forEnvironment().getUrlParameter(),
                SystemSearchConstants.forComments().getUrlParameter()).asSet();
    }

    private final SearcherInformation<SearchableField> searcherInformation;
    private final SearchInputTransformer searchInputTransformer;
    private final SearchRenderer searchRenderer;

    public QuerySearcher(VelocityRequestContextFactory velocityRequestContextFactory,
            VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, FieldVisibilityManager fieldVisibilityManager,
            JqlOperandResolver operandResolver)
    {
        List<Class<? extends FieldIndexer>> indexers = CollectionBuilder.<Class<? extends FieldIndexer>>newBuilder(DescriptionIndexer.class, SummaryIndexer.class, EnvironmentIndexer.class).asList();
        this.searcherInformation = new GenericSearcherInformation<SearchableField>(ID, NAME_KEY, indexers, fieldReference, SearcherGroupType.TEXT);
        this.searchInputTransformer = new QuerySearchInputTransformer(applicationProperties, operandResolver);
        this.searchRenderer = new QuerySearchRenderer(velocityRequestContextFactory, NAME_KEY,
                applicationProperties, templatingEngine, operandResolver, fieldVisibilityManager);
    }

    public SearcherInformation<SearchableField> getSearchInformation()
    {
        return this.searcherInformation;
    }

    public SearchInputTransformer getSearchInputTransformer()
    {
        return this.searchInputTransformer;
    }

    public SearchRenderer getSearchRenderer()
    {
        return this.searchRenderer;
    }

}
