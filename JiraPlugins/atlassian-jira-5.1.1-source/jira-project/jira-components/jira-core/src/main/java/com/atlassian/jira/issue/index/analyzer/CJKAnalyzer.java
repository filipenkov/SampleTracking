package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cjk.CJKTokenizer;
import org.apache.lucene.analysis.fr.ElisionFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.FrenchStemmer;

import java.io.Reader;
import java.util.Set;

/*
 * Extends the functionality of the Standard (language) Analyser provided by Lucene
 * by using the ClassicAnalyser and adding the SubtokenFilter.
 * <p>
 * This is useful for Chinese, Japanese and Korean languages.
 * <p>
 * Note: checked for Lucene 3.2 compatibility.
 */
public class CJKAnalyzer extends AbstractLanguageAnalyser
{
    private final Set<?> stopWords;
    private final Version matchVersion;

    public CJKAnalyzer(Version matchVersion, final boolean indexing)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        stopWords = org.apache.lucene.analysis.cjk.CJKAnalyzer.getDefaultStopSet();
    }

    /*
     * Create a token stream for this analyzer.
     */
    @Override
    public final TokenStream tokenStream(final String fieldname, final Reader reader)
    {
        TokenStream result = new CJKTokenizer(reader);
        result = wrapStreamForIndexing(result);
        result = new StopFilter(matchVersion, result, stopWords);

        return result;
    }
}
