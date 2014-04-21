/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.index.analyzer.EnglishAnalyzer;
import com.atlassian.jira.issue.index.analyzer.GermanAnalyzer;
import com.atlassian.jira.issue.index.analyzer.SimpleAnalyzer;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class JiraAnalyzer extends Analyzer
{
    private static final Logger log = Logger.getLogger(JiraAnalyzer.class);

    public static final Analyzer ANALYZER_FOR_INDEXING = new JiraAnalyzer(true);
    public static final Analyzer ANALYZER_FOR_SEARCHING = new JiraAnalyzer(false);

    private final Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
    private final Analyzer fallbackAnalyzer;

    public JiraAnalyzer(final boolean indexing)
    {
        analyzers.put(APKeys.Languages.BRAZILIAN, wrapIfNeeded(new BrazilianAnalyzer(), indexing));
        analyzers.put(APKeys.Languages.CHINESE, wrapIfNeeded(new ChineseAnalyzer(), indexing));
        analyzers.put(APKeys.Languages.CJK, wrapIfNeeded(new CJKAnalyzer(), indexing));
        analyzers.put(APKeys.Languages.CZECH, wrapIfNeeded(new CzechAnalyzer(), indexing));
        analyzers.put(APKeys.Languages.DUTCH, wrapIfNeeded(new DutchAnalyzer(), indexing));
        analyzers.put(APKeys.Languages.ENGLISH, new EnglishAnalyzer(indexing));
        analyzers.put(APKeys.Languages.FRENCH, wrapIfNeeded(new FrenchAnalyzer(), indexing));
        analyzers.put(APKeys.Languages.GERMAN, new GermanAnalyzer(indexing));
        analyzers.put(APKeys.Languages.GREEK, wrapIfNeeded(new GreekAnalyzer(), indexing));
        analyzers.put(APKeys.Languages.RUSSIAN, wrapIfNeeded(new RussianAnalyzer(), indexing));
        analyzers.put(APKeys.Languages.THAI, wrapIfNeeded(new ThaiAnalyzer(), indexing));
        // special case
        analyzers.put(APKeys.Languages.OTHER, fallbackAnalyzer = new SimpleAnalyzer(indexing));
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
        return ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_I18N_LANGUAGE_INPUT);
    }
}
