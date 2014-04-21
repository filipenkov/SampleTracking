package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.IOException;
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
    private final Version matchVersion;

    public SimpleAnalyzer(Version matchVersion, final boolean indexing)
    {
        super(indexing);
        this.matchVersion = matchVersion;
    }

    public final TokenStream tokenStream(String fieldname, Reader reader)
    {
        TokenStream result = new ClassicTokenizer(matchVersion, reader);
        result = new StandardFilter(matchVersion, result);
        result = wrapStreamForIndexing(result);
        result = new LowerCaseFilter(matchVersion, result);

        return result;
    }
}
