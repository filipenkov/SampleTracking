package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.th.ThaiWordFilter;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.RussianStemmer;

import java.io.Reader;
import java.util.Set;

/**
 * Extends the functionality of the standard ThaiAnalyser provided by Lucene by adding the SubtokenFilter.
 * <p>
 * Note: checked for Lucene 2.9 compatibility.
 * 
 * @see SubtokenFilter
 */
public class ThaiAnalyzer extends AbstractLanguageAnalyser
{
    /**
     * Contains the stopwords used with the StopFilter.
     */
    private final Set<?> stopWords;
    private final Version matchVersion;

    public ThaiAnalyzer(Version matchVersion, final boolean indexing)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        stopWords = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
    }

    /**
     * Creates a TokenStream which tokenizes all the text in the provided Reader.
     *
     * @return A TokenStream build from a ClassicTokenizer and appropriate filters for the
     * language. See
     */
    @Override
    public final TokenStream tokenStream(final String fieldName, final Reader reader)
    {
        TokenStream result = new ClassicTokenizer(matchVersion, reader);
        result = new StandardFilter(matchVersion, result);
        result = wrapStreamForIndexing(result);
        result = new LowerCaseFilter(matchVersion, result);
        result = new StopFilter( matchVersion, result, stopWords);
        result = new ThaiWordFilter(matchVersion, result);

        return result;
    }
}
