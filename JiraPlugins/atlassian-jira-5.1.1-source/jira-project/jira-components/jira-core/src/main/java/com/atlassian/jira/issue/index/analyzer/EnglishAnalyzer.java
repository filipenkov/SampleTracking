package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Set;

/*
 * Note: checked for Lucene 2.9 compatibility.
 */
public class EnglishAnalyzer extends AbstractLanguageAnalyser
{
    private final Set<?> stopWords;
    private final Version version;

    public EnglishAnalyzer(Version version, final boolean indexing)
    {
        super(indexing);
        stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
        this.version = version;
    }

    /*
     * Create a token stream for this analyzer.
     */
    @Override
    public final TokenStream tokenStream(final String fieldname, final Reader reader)
    {
        TokenStream result = new ClassicTokenizer(version, reader);
        result = new StandardFilter(version, result);
        result = wrapStreamForIndexing(result);

        result = new LowerCaseFilter(version, result);
        result = new StopFilter(version, result, stopWords);
        result = new PorterStemFilter(result);

        return result;
    }
}
