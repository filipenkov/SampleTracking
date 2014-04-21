package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Set;

/**
 * Extends the functionality of the standard GermanAnalyzer provided by Lucene by adding the SubtokenFilter.
 * <p>
 * Note: checked for Lucene 2.9 compatibility.
 * 
 * @see SubtokenFilter
 */
public class GermanAnalyzer extends AbstractLanguageAnalyser
{
    /**
     * Contains the stopwords used with the StopFilter.
     */
    private final Set<?> stopSet;
    private final Version version;

    public GermanAnalyzer(final Version version, final boolean includeSubTokenFilter)
    {
        super(includeSubTokenFilter);
        stopSet = StopFilter.makeStopSet(org.apache.lucene.analysis.de.GermanAnalyzer.GERMAN_STOP_WORDS);
        this.version = version;
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
        TokenStream result = new StandardTokenizer(version, reader);
        result = new StandardFilter(version, result);
        // If a user searches for "NullPointerException", then this analyser will stem that search term.
        // Hence we need to ensure that the subtokens of "java.lang.NullPointerException" are stemmed as well.
        // However, the GermanStemFilter will not stem words that contain full-stop characters.
        // Therefore the SubtokenFilter must occur BEFORE the GermanStemFilter.
        result = wrapStreamForIndexing(result);
        result = new LowerCaseFilter(version, result);
        result = new StopFilter(version, result, stopSet);
        // Note that we don't make use of the stem exclusion set.
        result = new GermanStemFilter(result);
        return result;
    }
}
