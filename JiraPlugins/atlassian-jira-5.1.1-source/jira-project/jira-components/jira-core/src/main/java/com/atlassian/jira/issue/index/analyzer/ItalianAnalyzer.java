package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.fr.ElisionFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.FrenchStemmer;
import org.tartarus.snowball.ext.ItalianStemmer;

import java.io.Reader;
import java.util.Set;

/*
 * Extends the functionality of the Standard (language) Analyser provided by Lucene
 * by using the ClassicAnalyser and adding the SubtokenFilter.
 * <p>
 * Note: checked for Lucene 3.2 compatibility.
 */
public class ItalianAnalyzer extends AbstractLanguageAnalyser
{
    private final Set<?> stopWords;
    private final Version matchVersion;

    public ItalianAnalyzer(Version matchVersion, final boolean indexing)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        stopWords = org.apache.lucene.analysis.it.ItalianAnalyzer.getDefaultStopSet();
    }

    /*
     * Create a token stream for this analyzer.
     */
    @Override
    public final TokenStream tokenStream(final String fieldname, final Reader reader)
    {
        TokenStream result = new ClassicTokenizer(matchVersion, reader);
        result = new StandardFilter(matchVersion, result);
        result = wrapStreamForIndexing(result);
        result = new ElisionFilter(matchVersion, result);
        result = new LowerCaseFilter(matchVersion, result);
        result = new StopFilter(matchVersion, result, stopWords);
        result = new SnowballFilter(result, new ItalianStemmer());

        return result;
    }
}
