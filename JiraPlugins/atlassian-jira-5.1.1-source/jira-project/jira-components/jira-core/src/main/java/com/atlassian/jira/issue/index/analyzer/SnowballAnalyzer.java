package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.SpanishStemmer;

import java.io.Reader;
import java.util.Set;

/*
 * Extends the functionality of the Standard (language) Analyser provided by Lucene
 * by using the ClassicAnalyser and adding the SubtokenFilter.
 * <p>
 * Note: checked for Lucene 3.2 compatibility.
 */
public class SnowballAnalyzer extends AbstractLanguageAnalyser
{
    private final Version matchVersion;
    private final Set<?> stopWords;
    private final Class<SnowballProgram> stemmerClass;

    public SnowballAnalyzer(Version matchVersion, final boolean indexing, final Set<?> stopWords, Class stemmerClass)
    {
        super(indexing);
        this.matchVersion = matchVersion;
        this.stopWords = stopWords;
        this.stemmerClass = stemmerClass;
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
        result = new SnowballFilter(result, getStemmer());

        return result;
    }

    private SnowballProgram getStemmer()
    {
        try
        {
            return stemmerClass.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }
}
