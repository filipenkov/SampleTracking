/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.trackback;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class PingUrlFilterer
{
    private static final Logger log = Logger.getLogger(JiraTrackbackFinder.class);

    final ApplicationProperties appProps;

    public PingUrlFilterer(ApplicationProperties appProps)
    {
        this.appProps = appProps;
    }

    public List filterPingUrls(List urls)
    {
        List validPingUrls = new ArrayList();
        for (Iterator iterator = urls.iterator(); iterator.hasNext();)
        {
            String url = (String) iterator.next();
            if (isValidPingUrl(url))
                validPingUrls.add(url);
        }
        return validPingUrls;
    }

    private boolean isValidPingUrl(String url)
    {
        PatternMatcher patternMatcher = new Perl5Matcher();

        for (Iterator iterator = getPatternsToLimitBy().iterator(); iterator.hasNext();)
        {
            Pattern pattern = (Pattern) iterator.next();
            if (patternMatcher.matches(url, pattern))
            {
                log.info("The URL: " + url + " has been excluded as it matches the following regular expression pattern : " + pattern.getPattern());
                return false;
            }
        }
        return true;
    }

    /**
     * Get the regular expressions to limit by
     *
     * @return a list of {@link Pattern}s
     */
    private Collection getPatternsToLimitBy()
    {
        Collection patterns = new ArrayList();
        PatternCompiler patternCompiler = new Perl5Compiler();
        Pattern pattern;

        for (Iterator iterator = getStringExpressions().iterator(); iterator.hasNext();)
        {
            String expression = (String) iterator.next();
            if (expression != null)
            {
                try
                {
                    pattern = patternCompiler.compile(expression, Perl5Compiler.CASE_INSENSITIVE_MASK);
                    patterns.add(pattern);
                }
                catch (MalformedPatternException e)
                {
                    log.error("Error compiling regular expression for : " + expression + ".", e);
                }
            }
        }
        return patterns;
    }

    private Collection getStringExpressions()
    {
        String strings = appProps.getDefaultBackedString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN);
        String baseUrl = appProps.getDefaultBackedString(APKeys.JIRA_BASEURL);

        List stringList = new ArrayList();
        if (TextUtils.stringSet(strings))
        {
            StringTokenizer tokenizer = new StringTokenizer(strings, "\n\f\r"); //carriage returns, form feeds and new lines are valid token enders
            while (tokenizer.hasMoreTokens())
            {
                stringList.add(tokenizer.nextToken());
            }
        }

        if (TextUtils.stringSet(baseUrl))
        {
            stringList.add(baseUrl + ".*");
        }

        return stringList;
    }
}
