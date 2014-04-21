/**
 * Copyright 2008 Atlassian Pty Ltd
 */

package com.atlassian.jira.sharing.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.gzipfilter.org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import com.atlassian.jira.issue.index.indexers.impl.FieldIndexerUtil;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.SharedEntityColumnDefinition;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.SharedEntitySearchContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

import java.util.Map;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * @since v3.13
 */
public class DefaultQueryFactory implements QueryFactory
{
    private final ShareTypeFactory shareTypeFactory;

    private final Map<SharedEntitySearchContext, QueryFactory> searchContextToQueryFactoryMap;

    // Used by PICO in production
    public DefaultQueryFactory(final ShareTypeFactory shareTypeFactory,
            final SharedEntitySearchContextToQueryFactoryMap searchContextToQueryFactoryMap)
    {
        this.shareTypeFactory = shareTypeFactory;
        this.searchContextToQueryFactoryMap = searchContextToQueryFactoryMap;
    }

    /* permission checks - used in the UI */
    public Query create(final SharedEntitySearchParameters params, final User user)
    {
        final QueryBuilder builder = new Builder(params)
        {
            void add(final ShareTypeSearchParameter shareTypeSearchParameter, final BooleanClause.Occur occurance)
            {
                if (shareTypeSearchParameter != null)
                {
                    add(shareTypeFactory.getShareType(shareTypeSearchParameter.getType()).getQueryFactory().getQuery(shareTypeSearchParameter, user),
                        occurance);
                }
            }
        }.build();
        builder.add(getQueryFactoryFor(params.getEntitySearchContext()).create(params, user), BooleanClause.Occur.MUST);
        return builder.toQuery();
    }

    private QueryFactory getQueryFactoryFor(final SharedEntitySearchContext searchContext)
    {
        return searchContextToQueryFactoryMap.get(searchContext);
    }

    /* no permission checks - used when deleting Shares for instance */
    public Query create(final SharedEntitySearchParameters params)
    {
        return new Builder(params)
        {
            void add(final ShareTypeSearchParameter shareTypeSearchParameter, final BooleanClause.Occur occurance)
            {
                if (shareTypeSearchParameter != null)
                {
                    add(shareTypeFactory.getShareType(shareTypeSearchParameter.getType()).getQueryFactory().getQuery(shareTypeSearchParameter),
                        occurance);
                }
            }
        }.build().toQuery();
    }

    /**
     * Abstract query builder. We override the addShareTypeParameter as the query factories need different parameters (with or without the user) to
     * perform system searching or user searching.
     */
    private abstract class Builder extends QueryBuilder
    {
        private final SharedEntitySearchParameters params;

        private Builder(final SharedEntitySearchParameters params)
        {
            this.params = params;
        }

        QueryBuilder build()
        {
            final QueryBuilder nameOrDescription = new QueryBuilder();
            if (params.getTextSearchMode() == SharedEntitySearchParameters.TextSearchMode.EXACT)
            {
                if (!StringUtils.isBlank(params.getName()))
                {
                    nameOrDescription.add(new Term(SharedEntityColumnDefinition.NAME.getCaseInsensitiveColumn(),
                            params.getName().toLowerCase()), BooleanClause.Occur.MUST);
                }
                if (!StringUtils.isBlank(params.getDescription()))
                {
                    // For description we only use the first 50 characters as provided by the sort column
                    nameOrDescription.add(new Term(SharedEntityColumnDefinition.DESCRIPTION.getSortColumn(),
                            FieldIndexerUtil.getValueForSorting(params.getDescription().toLowerCase())), BooleanClause.Occur.MUST);
                }
            }
            else
            {
                // JRA-19918
                if  (params.getTextSearchMode() == SharedEntitySearchParameters.TextSearchMode.WILDCARD )
                {
                     addParsedWildcardQueries(nameOrDescription, BooleanClause.Occur.SHOULD);
                }
                else
                {
                    final BooleanClause.Occur clauseOccurence = params.getTextSearchMode() == SharedEntitySearchParameters.TextSearchMode.OR ?  BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
                    addParsedQueries(nameOrDescription, clauseOccurence);
                }
            }
            add(nameOrDescription, BooleanClause.Occur.MUST);
            final String userName = params.getUserName();
            if (!StringUtils.isBlank(userName))
            {
                add(SharedEntityColumn.OWNER, toLowerCase(userName), BooleanClause.Occur.MUST);
            }
            add(params.getShareTypeParameter(), BooleanClause.Occur.MUST);

            return this;
        }

        private void addParsedQueries(QueryBuilder nameOrDescription, final BooleanClause.Occur clauseOccurence)
        {
            nameOrDescription.addParsedQuery(SharedEntityColumn.NAME, params.getName(), clauseOccurence);
            nameOrDescription.addParsedQuery(SharedEntityColumn.DESCRIPTION, params.getDescription(), clauseOccurence);
        }


        
        private void addParsedWildcardQueries(QueryBuilder nameOrDescription, final BooleanClause.Occur clauseOccurence)
        {
            // JRA-19918 Wildcard queries don't play well with stemmer
            nameOrDescription.addParsedWildcardQuery(SharedEntityColumn.NAME, params.getName(), clauseOccurence);
            nameOrDescription.addParsedWildcardQuery(SharedEntityColumn.DESCRIPTION, params.getDescription(), clauseOccurence);
        }

        abstract void add(final ShareTypeSearchParameter shareTypeSearchParameter, final BooleanClause.Occur occurance);
    }
}
