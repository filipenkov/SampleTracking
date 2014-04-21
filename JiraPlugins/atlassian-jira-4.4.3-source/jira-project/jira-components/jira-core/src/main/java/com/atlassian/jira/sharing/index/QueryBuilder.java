/**
 * Copyright 2008 Atlassian Pty Ltd
 */
package com.atlassian.jira.sharing.index;

import com.atlassian.jira.issue.index.analyzer.WildcardFilter;
import com.atlassian.jira.sharing.SharedEntityColumn;
import com.atlassian.jira.sharing.search.SearchParseException;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import java.io.Reader;

/**
 * @since v3.13
 */
public class QueryBuilder
{
    static final class Analyzers
    {
        /**
         * Stemming Analyzer.
         */
        static final Analyzer STEMMER = new Analyzer()
        {
            public TokenStream tokenStream(final String arg0, final Reader reader)
            {
                return new PorterStemFilter(new LowerCaseFilter(new StandardFilter(new StandardTokenizer(reader))));
            }
        };

        /**
         * wildcard stemming analyzer
         */

        static final Analyzer WILDCARD_STEMMER = new Analyzer()
        {
            public TokenStream tokenStream(final String arg0, final Reader reader)
            {
                return new WildcardFilter(new PorterStemFilter(new LowerCaseFilter(new StandardFilter(new StandardTokenizer(reader)))));
            }
        };

        /**
         * wildcard analyzer
         */
        static final Analyzer WILDCARD = new Analyzer()
        {
            public TokenStream tokenStream(final String arg0, final Reader reader)
            {
                return new WildcardFilter(new LowerCaseFilter(new StandardFilter(new StandardTokenizer(reader))));
            }
        };

        /**
         * lowercase analyzer
         */
          static final Analyzer LOWERCASE = new Analyzer()
        {
            public TokenStream tokenStream(final String arg0, final Reader reader)
            {
                return new LowerCaseFilter(new StandardFilter(new StandardTokenizer(reader)));
            }
        };

        /**
         *  simple tokenizer analyzer
         *
         */
        static final Analyzer STANDARD = new Analyzer()
        {
            public TokenStream tokenStream(final String arg0, final Reader reader)
            {
                return (new StandardFilter(new StandardTokenizer(reader)));
            }
        };
    }

    final BooleanQuery result = new BooleanQuery();

    void addParsedQuery(final SharedEntityColumn column, final String value, final Occur occurance) throws SearchParseException
    {
        if (!StringUtils.isBlank(value))
        {
            add(parseQuery(column, value), occurance);
        }
    }

    void addParsedWildcardQuery(final SharedEntityColumn column, final String value, final Occur occurance) throws SearchParseException
    {
        if (!StringUtils.isBlank(value))
        {
            add(parseWildcardQuery(column, value), occurance);
        }
    }

    void add(final SharedEntityColumn column, final String value, final Occur occurance) throws SearchParseException
    {
        if (!StringUtils.isBlank(value))
        {
            add(new Term(column.getName(), value), occurance);
        }
    }

    void add(final Query query, final BooleanClause.Occur occurance)
    {
        if (query != null)
        {
            result.add(query, occurance);
        }
    }

    void add(final Term[] terms, final BooleanClause.Occur occurance)
    {
        for (int i = 0; i < terms.length; i++)
        {
            add(terms[i], occurance);
        }
    }

    void add(final Term term, final BooleanClause.Occur occurance)
    {
        if (term != null)
        {
            result.add(new TermQuery(term), occurance);
        }
    }

    void add(final QueryBuilder builder, final BooleanClause.Occur occurance)
    {
        if ((builder != null) && builder.hasClauses())
        {
            result.add(builder.toQuery(), occurance);
        }
    }

    boolean hasClauses()
    {
        return !result.clauses().isEmpty();
    }

    final Query toQuery()
    {
        if (hasClauses())
        {
            return result;
        }
        return new MatchAllDocsQuery();
    }

    /**
     * Template method for extension.
     *
     * @return this for fluent interface style chaining
     */
    QueryBuilder build()
    {
        return this;
    }

    /**
     * Parse a query parameter.
     *
     * @param column the column to query
     * @param value the value
     * @return the parsed query
     *
     * @throws SearchParseException if there are problems
     */
    static Query parseQuery(final SharedEntityColumn column, final String value) throws SearchParseException
    {
        if (StringUtils.isBlank(value))
        {
            return new MatchAllDocsQuery();
        }
        final QueryParser parser = new QueryParser(column.getName(), Analyzers.LOWERCASE);
        parser.setAllowLeadingWildcard(false);
        try
        {
            return parser.parse(value);
        }
        catch (final ParseException e)
        {
            throw new SearchParseException(e, column);
        }
    }

    /**
     * Parse a query parameter.
     *
     * @param @param column the column to query
     * @param value the value
     * @return the parsed query which should be a PrefixQuery due to the wildcards
     *
     * @throws SearchParseException if there are problems
     */
    static Query parseWildcardQuery(final SharedEntityColumn column, final String value) throws SearchParseException
    {
        if (StringUtils.isBlank(value))
        {
            return new MatchAllDocsQuery();
        }
        final QueryParser wildCardParser = new QueryParser(column.getName(), Analyzers.WILDCARD);
        final QueryParser parser = new QueryParser(column.getName(),Analyzers.STANDARD);
        parser.setAllowLeadingWildcard(false);
        try
        {
            final Query interimQuery =  wildCardParser.parse(value);
            return parser.parse(interimQuery.toString());
        }
        catch (final ParseException e)
        {
            throw new SearchParseException(e, column);
        }
    }

    static void checkQueryParameter(final SharedEntityColumn column, final String value) throws SearchParseException
    {
        parseQuery(column, value);
    }

    public static void validate(final SharedEntitySearchParameters searchParameters) throws SearchParseException
    {
        checkQueryParameter(SharedEntityColumn.NAME, searchParameters.getName());
        checkQueryParameter(SharedEntityColumn.DESCRIPTION, searchParameters.getDescription());
    }
}
