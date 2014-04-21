/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.index.analyzer.BrazilianAnalyzer;
import com.atlassian.jira.issue.index.analyzer.BulgarianAnalyzer;
import com.atlassian.jira.issue.index.analyzer.CJKAnalyzer;
import com.atlassian.jira.issue.index.analyzer.CzechAnalyzer;
import com.atlassian.jira.issue.index.analyzer.EnglishAnalyzer;
import com.atlassian.jira.issue.index.analyzer.FrenchAnalyzer;
import com.atlassian.jira.issue.index.analyzer.GermanAnalyzer;
import com.atlassian.jira.issue.index.analyzer.GreekAnalyzer;
import com.atlassian.jira.issue.index.analyzer.ItalianAnalyzer;
import com.atlassian.jira.issue.index.analyzer.SimpleAnalyzer;
import com.atlassian.jira.issue.index.analyzer.SnowballAnalyzer;
import com.atlassian.jira.issue.index.analyzer.ThaiAnalyzer;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.ArmenianStemmer;
import org.tartarus.snowball.ext.BasqueStemmer;
import org.tartarus.snowball.ext.CatalanStemmer;
import org.tartarus.snowball.ext.DanishStemmer;
import org.tartarus.snowball.ext.DutchStemmer;
import org.tartarus.snowball.ext.FinnishStemmer;
import org.tartarus.snowball.ext.HungarianStemmer;
import org.tartarus.snowball.ext.NorwegianStemmer;
import org.tartarus.snowball.ext.PortugueseStemmer;
import org.tartarus.snowball.ext.RomanianStemmer;
import org.tartarus.snowball.ext.SpanishStemmer;
import org.tartarus.snowball.ext.SwedishStemmer;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class JiraAnalyzer extends Analyzer
{
    private static final Logger log = Logger.getLogger(JiraAnalyzer.class);

    // Just put this here to make the code a bit briefer lower down.
    private static final Version LUCENE_VERSION = DefaultIndexManager.LUCENE_VERSION;

    public static final Analyzer ANALYZER_FOR_INDEXING = new JiraAnalyzer(true);
    public static final Analyzer ANALYZER_FOR_SEARCHING = new JiraAnalyzer(false);

    private final Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
    private final Analyzer fallbackAnalyzer;
    private final boolean indexing;

    public JiraAnalyzer(final boolean indexing)
    {
        analyzers.put(APKeys.Languages.ARMENIAN, new SnowballAnalyzer(LUCENE_VERSION, indexing, ArmenianAnalyzer.getDefaultStopSet(), ArmenianStemmer.class));
        analyzers.put(APKeys.Languages.BASQUE, new SnowballAnalyzer(LUCENE_VERSION, indexing, BasqueAnalyzer.getDefaultStopSet(), BasqueStemmer.class));
        analyzers.put(APKeys.Languages.BULGARIAN, new BulgarianAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.BRAZILIAN, new BrazilianAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.CATALAN, new SnowballAnalyzer(LUCENE_VERSION, indexing, CatalanAnalyzer.getDefaultStopSet(), CatalanStemmer.class));
        analyzers.put(APKeys.Languages.CHINESE, new SimpleAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.CJK, new CJKAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.CZECH, new CzechAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.DANISH, new SnowballAnalyzer(LUCENE_VERSION, indexing, DanishAnalyzer.getDefaultStopSet(), DanishStemmer.class));
        analyzers.put(APKeys.Languages.DUTCH, new SnowballAnalyzer(LUCENE_VERSION, indexing, DutchAnalyzer.getDefaultStopSet(), DutchStemmer.class));
        analyzers.put(APKeys.Languages.ENGLISH, new EnglishAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.FINNISH, new SnowballAnalyzer(LUCENE_VERSION, indexing, FinnishAnalyzer.getDefaultStopSet(), FinnishStemmer.class));
        analyzers.put(APKeys.Languages.FRENCH, new FrenchAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.GERMAN, new GermanAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.GREEK, new GreekAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.HUNGARIAN, new SnowballAnalyzer(LUCENE_VERSION, indexing, HungarianAnalyzer.getDefaultStopSet(), HungarianStemmer.class));
        analyzers.put(APKeys.Languages.ITALIAN, new ItalianAnalyzer(LUCENE_VERSION, indexing));
        analyzers.put(APKeys.Languages.NORWEGIAN, new SnowballAnalyzer(LUCENE_VERSION, indexing, NorwegianAnalyzer.getDefaultStopSet(), NorwegianStemmer.class));
        analyzers.put(APKeys.Languages.PORTUGUESE, new SnowballAnalyzer(LUCENE_VERSION, indexing, PortugueseAnalyzer.getDefaultStopSet(), PortugueseStemmer.class));
        analyzers.put(APKeys.Languages.ROMANIAN, new SnowballAnalyzer(LUCENE_VERSION, indexing, RomanianAnalyzer.getDefaultStopSet(), RomanianStemmer.class));
        analyzers.put(APKeys.Languages.RUSSIAN, new SnowballAnalyzer(LUCENE_VERSION, indexing, RussianAnalyzer.getDefaultStopSet(), RomanianStemmer.class));
        analyzers.put(APKeys.Languages.SPANISH, new SnowballAnalyzer(LUCENE_VERSION, indexing, SpanishAnalyzer.getDefaultStopSet(), SpanishStemmer.class));
        analyzers.put(APKeys.Languages.SWEDISH, new SnowballAnalyzer(LUCENE_VERSION, indexing, SwedishAnalyzer.getDefaultStopSet(), SwedishStemmer.class));
        analyzers.put(APKeys.Languages.THAI, new ThaiAnalyzer(LUCENE_VERSION, indexing));
        // special case
        analyzers.put(APKeys.Languages.OTHER, fallbackAnalyzer = new SimpleAnalyzer(LUCENE_VERSION, indexing));
        this.indexing = indexing;
    }

    private Analyzer wrapIfNeeded(final Analyzer analyzer, final boolean indexing)
    {
        if (indexing)
        {
            return new JavaExceptionAnalyzer(analyzer);
        }
        return analyzer;
    }

    /*
     * Create a token stream for this analyzer.
     */
    @Override
    public final TokenStream tokenStream(String fieldname, final Reader reader)
    {
        // workaround for https://issues.apache.org/jira/browse/LUCENE-1359
        // reported here: http://jira.atlassian.com/browse/JRA-16239
        if (fieldname == null)
        {
            fieldname = "";
        }
        // end workaround
        return findAnalyzer().tokenStream(fieldname, reader);
    }

    /*
     * We do this because Lucene insists we subclass this and make it final.
     */
    @Override
    public final TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException
    {
        return super.reusableTokenStream(fieldName, reader);
    }

    private Analyzer findAnalyzer()
    {
        final String language = getLanguage();
        Analyzer analyzer = analyzers.get(language);
        if (analyzer == null)
        {
            log.error("Invalid indexing language: '" + language + "', defaulting to '" + APKeys.Languages.OTHER + "'.");
            analyzer = fallbackAnalyzer;
        }
        return analyzer;
    }

    String getLanguage()
    {
        return ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_I18N_LANGUAGE_INPUT);
    }
}
