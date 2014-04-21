package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.Reader;

/**
 * This analyzer is used when "other" is chosen as the indexing language.
 *
 * It is used instead of the Lucene SimpleAnalyzer because it indexes numbers as well as letters, and includes the SubtokenFilter.
 *
 * @see com.atlassian.jira.issue.index.analyzer.SubtokenFilter
 */
public class SimpleAnalyzer extends AbstractLanguageAnalyser
{
    public SimpleAnalyzer(boolean includeSubTokenFilter)
    {
        super(includeSubTokenFilter);
    }

    public final TokenStream tokenStream(String fieldname, Reader reader)
    {
        // Use StandardFilter to index numbers as well as letters (JSP-2483)
        TokenStream result = new StandardTokenizer(reader, true);

        result = new StandardFilter(result);
        result = wrapStreamForIndexing(result);
        result = new LowerCaseFilter(result);

        return result;
    }
}
