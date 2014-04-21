package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.el.GreekLowerCaseFilter;
import org.apache.lucene.analysis.el.GreekStemFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Set;

/**
 * Extends the functionality of the Standard (language) Analyser provided by Lucene
 * by using the ClassicAnalyser and adding the SubtokenFilter.
 * <p>
 * Note: checked for Lucene 3.2 compatibility.
 * 
 * @see SubtokenFilter
 */
public class GreekAnalyzer extends AbstractLanguageAnalyser
{
    /**
     * Contains the stopwords used with the StopFilter.
     */
    private final Set<?> stopWords;
    private final Version matchVersion;

    public GreekAnalyzer(Version matchVersion, final boolean indexing)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        stopWords = org.apache.lucene.analysis.el.GreekAnalyzer.getDefaultStopSet();
    }

    /**
     * Creates a TokenStream which tokenizes all the text in the provided Reader.
     *
     * @return A TokenStream build from a StandardTokenizer filtered with
     *         StandardFilter, LowerCaseFilter, StopFilter, GermanStemFilter
     */
    @Override
    public final TokenStream tokenStream(final String fieldName, final Reader reader)
    {
        TokenStream result = new ClassicTokenizer(matchVersion, reader);
        result = new StandardFilter(matchVersion, result);
        result = wrapStreamForIndexing(result);
        result = new GreekLowerCaseFilter(matchVersion, result);
        result = new StopFilter(matchVersion, result, stopWords);
        result = new GreekStemFilter(result);

        return result;
    }
}
