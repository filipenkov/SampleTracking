package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.Reader;
import java.util.Set;

/*
 * Note: checked for Lucene 2.9 compatibility.
 */
public class EnglishAnalyzer extends AbstractLanguageAnalyser
{
    private final Set<?> stopWords;

    public EnglishAnalyzer(final boolean indexing)
    {
        super(indexing);
        stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    }

    /*
     * Create a token stream for this analyzer.
     */
    @Override
    public TokenStream tokenStream(final String fieldname, final Reader reader)
    {
        TokenStream result = new StandardTokenizer(reader, true);

        result = new StandardFilter(result);
        result = wrapStreamForIndexing(result);

        result = new LowerCaseFilter(result);
        result = new StopFilter(false, result, stopWords);
        result = new PorterStemFilter(result);

        return result;
    }
}
