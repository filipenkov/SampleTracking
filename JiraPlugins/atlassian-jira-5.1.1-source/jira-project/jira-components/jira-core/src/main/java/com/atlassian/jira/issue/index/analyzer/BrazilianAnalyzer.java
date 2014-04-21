package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.br.BrazilianStemFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

import java.io.Reader;
import java.util.Set;

/*
 * Extends the functionality of the Standard (language) Analyser provided by Lucene
 * by using the ClassicAnalyser and adding the SubtokenFilter.
 * <p>
 * Note: checked for Lucene 3.2 compatibility.
 */
public class BrazilianAnalyzer extends AbstractLanguageAnalyser
{
    private final Set<?> stopWords;
    private final Version matchVersion;

    public BrazilianAnalyzer(Version matchVersion, final boolean indexing)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        stopWords = org.apache.lucene.analysis.br.BrazilianAnalyzer.getDefaultStopSet();
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
        result = new LowerCaseFilter(matchVersion, result);
        result = new StopFilter(matchVersion, result, stopWords);
        result = new BrazilianStemFilter(result);

        return result;
    }
}
