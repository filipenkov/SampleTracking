package com.atlassian.sisyphus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class DefaultSisyphusPatternMatcher implements SisyphusPatternMatcher
{
    private static final Logger log = Logger.getLogger(DefaultSisyphusPatternMatcher.class);
	private final SisyphusPatternSource patternSource;

    public DefaultSisyphusPatternMatcher(final SisyphusPatternSource patternSource)
    {
		this.patternSource = patternSource;
    }
    
    /* (non-Javadoc)
     * @see com.atlassian.sisyphus.SisyphusPatternMatcher#match(java.io.BufferedReader)
     */
    public Map<String, PatternMatchSet> match(final BufferedReader reader) throws IOException, InterruptedException
    {
        DefaultMatchResultVisitor visitor = new DefaultMatchResultVisitor();
		match(reader, visitor);
        return visitor.getResults();
    }

    protected void matchAttachmentLine(final String thisLine, final int currentLineNum, MatchResultVisitor visitor) throws InterruptedException
    {
        final long lineTime = System.currentTimeMillis();
        for (final Iterator<SisyphusPattern> iterator = patternSource.iterator(); iterator.hasNext();)
        {
            if (Thread.currentThread().isInterrupted() || visitor.isCancelled())
            {
                throw new InterruptedException();
            }

            final SisyphusPattern sPattern = iterator.next();
            
            // If this is a partial pattern based on a bogus entry, don't bother scanning
            if (sPattern.isBrokenPattern())
            {
                continue;
            }

            final Pattern pat = sPattern.getPattern();

            // Patterns that don't compile successfully will be null.  Don't use them
            if (pat != null) {
            	log.debug("Current Pattern being evaluated: " + sPattern.toString());
            	final long matchtime = System.currentTimeMillis();
            	final Matcher m = pat.matcher(thisLine);
            	if (m.find())
            	{
            		visitor.patternMatched(thisLine, currentLineNum, sPattern);
            	}
            	final long matchDuration = System.currentTimeMillis() - matchtime;
            	if (matchDuration >= 5)
            	{
            		log.debug("Slow match. Time from find() method: " + matchDuration + " milliseconds. Regex being used is: '" + sPattern.getRegex() + "' and 'thisLine' is " + thisLine.length() + " characters long.");
            	}
            }
            else {
            	log.debug("Regexp would not compile and was skipped: " + sPattern.getRegex());
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("Time from line scan (all regexs): " + (System.currentTimeMillis() - lineTime) + " milliseconds.");
        }
    }

	public void match(BufferedReader reader, MatchResultVisitor visitor) throws IOException, InterruptedException 
	{
        // Now scan in the file and check each line against all the patterns
        String thisLine;
        int lineCount = 1;
        while ((thisLine = reader.readLine()) != null)
        {
            matchAttachmentLine(thisLine, lineCount, visitor);
            lineCount++;
        }
	}
}