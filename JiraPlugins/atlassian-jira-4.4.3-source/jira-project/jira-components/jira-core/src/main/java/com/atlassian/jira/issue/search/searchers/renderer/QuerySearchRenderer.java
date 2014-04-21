package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
import com.atlassian.jira.issue.search.searchers.util.DefaultQuerySearcherInputHelper;
import com.atlassian.jira.issue.search.searchers.util.QuerySearcherInputHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A search renderer for the system text fields, Comment, Summary, Description and Environment.
 *
 * @since v4.0
 */
public class QuerySearchRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    private final JqlOperandResolver operandResolver;
    private final FieldVisibilityManager fieldVisibilityManager;
    private static final Set<String> QUERY_FIELDS_DEFAULT = CollectionBuilder.newBuilder(SystemSearchConstants.forSummary().getJqlClauseNames().getPrimaryName(),
            SystemSearchConstants.forDescription().getJqlClauseNames().getPrimaryName()).asSet();

    public QuerySearchRenderer(VelocityRequestContextFactory velocityRequestContextFactory,
            String nameKey, ApplicationProperties applicationProperties, VelocityManager velocityManager,
            JqlOperandResolver operandResolver, FieldVisibilityManager fieldVisibilityManager)
    {
        super(velocityRequestContextFactory, applicationProperties, velocityManager, QuerySearcher.ID, nameKey);
        this.operandResolver = operandResolver;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public String getEditHtml(final User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        Map velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("searchRenderer", this);
        return renderEditTemplate(QuerySearcher.ID + EDIT_TEMPLATE_SUFFIX, velocityParams);
    }

    public boolean isShown(final User searcher, final SearchContext searchContext)
    {
        return true;
    }

    public String getViewHtml(final User searcher, SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action)
    {
        Map velocityParams = getVelocityParams(searcher, searchContext, null, fieldValuesHolder, displayParameters, action);
        velocityParams.put("queryFieldNames", getQueryFieldNames(searcher, fieldValuesHolder));
        return renderViewTemplate(QuerySearcher.ID + VIEW_TEMPLATE_SUFFIX, velocityParams);
    }

    public boolean isRelevantForQuery(final User searcher, final Query query)
    {
        if (query != null && query.getWhereClause() != null)
        {
            QuerySearcherInputHelper querySearcherInputHelper = new DefaultQuerySearcherInputHelper(QuerySearcher.QUERY_URL_PARAM, operandResolver);
            return querySearcherInputHelper.convertClause(query.getWhereClause(), searcher) != null;
        }
        return false;
    }

    public Collection getQueryFieldNames(final User searcher, FieldValuesHolder fieldValuesHolder)
    {

        List queryFields = (List) fieldValuesHolder.get(QuerySearcher.QUERY_FIELDS_ID);
        if (queryFields != null && !queryFields.isEmpty())
        {
            final Map ID_TO_NAME_MAP = EasyMap.build(SystemSearchConstants.forSummary().getUrlParameter(), getI18n(searcher).getText("issue.field.summary"),
                                                     SystemSearchConstants.forDescription().getUrlParameter(), getI18n(searcher).getText("issue.field.description"),
                                                     SystemSearchConstants.forEnvironment().getUrlParameter(), getI18n(searcher).getText("issue.field.environment"),
                                                     SystemSearchConstants.forComments().getUrlParameter(), getI18n(searcher).getText("common.words.comments"));

            final ArrayList queryFieldNames = new ArrayList(queryFields);
            CollectionUtils.transform(queryFieldNames, new Transformer()
            {
                public Object transform(Object input)
                {
                    return ID_TO_NAME_MAP.get(input);
                }
            });
            return queryFieldNames;
        }
        else
        {
            return null;
        }

    }

    // ------------------------------------------------------------------------------------------------ Velocity Helpers
    public boolean isFieldSelected(String fieldId, FieldValuesHolder fieldValuesHolder)
    {
        List queryFields = (List) fieldValuesHolder.get(QuerySearcher.QUERY_FIELDS_ID);
        if (queryFields != null && queryFields.contains(fieldId))
        {
            return true;
        }

        if (StringUtils.isBlank((String) fieldValuesHolder.get(QuerySearcher.QUERY_URL_PARAM)) && QUERY_FIELDS_DEFAULT.contains(fieldId))
        {
            // Defaults
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isFieldVisible(final User searcher, String fieldId, SearchContext searchContext)
    {
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(fieldId, searchContext, searcher);
    }
}
