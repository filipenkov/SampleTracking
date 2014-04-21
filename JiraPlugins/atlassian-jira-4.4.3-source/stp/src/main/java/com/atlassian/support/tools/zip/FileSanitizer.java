package com.atlassian.support.tools.zip;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Copied from Confluence trunk as of 05 August 2010 (revision 123688).
 */

public class FileSanitizer
{
	private static final Logger log = Logger.getLogger(FileSanitizer.class);
	private static final String SANITIZER_MESSAGE = "Sanitized by Support Utility";

	private Map<String, List<Pattern>> filePatterns;
	private Set<File> tmpFiles;

	public FileSanitizer(Map<String, List<Pattern>> filePatterns)
	{
		this.filePatterns = filePatterns;
		this.tmpFiles = new HashSet<File>();
	}

	public File sanitize(File file) throws IOException
	{
		if (file == null) 
			return file;

		if(!this.filePatterns.containsKey(file.getName()))
			return file;

		final List<Pattern> patterns = this.filePatterns.get(file.getName());
		if(patterns.isEmpty())
			return file;
		
		File outputFile = File.createTempFile("sanitizer", "out");
		this.tmpFiles.add(outputFile);
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			try
			{
				String line;
				while((line = reader.readLine()) != null)
				{
					String outLine = sanitizeLine(patterns, line);
					writer.write(outLine + "\n");
				}
			}
			finally
			{
				IOUtils.closeQuietly(reader);
			}
		}
		finally
		{
			IOUtils.closeQuietly(writer);
		}
		return outputFile;
	}

	public String sanitizeLine(List<Pattern> patterns, final String originalLine)
	{
		String substitutedLine = originalLine;
		
		for(Pattern pattern: patterns)
		{
			List<MatcherGroup> matches = new ArrayList<MatcherGroup>();
			Matcher m = pattern.matcher(substitutedLine);
			int searchStart = 0;
			while(m.find(searchStart))
			{
				// Collect all the matching groups
				for(int a = m.groupCount(); a > 0; a--)
				{
					if(null != m.group(a))
					{
						final int end = m.end(a);
						matches.add(new MatcherGroup(m.start(a), end));
						searchStart = Math.max(searchStart, end);
					}
				}
			}
			// Sort by start position
			Collections.sort(matches);
			// Build the replacement string based on the start and
			// end indices of the *original* string
			StringBuilder sb = new StringBuilder();
			int idx = 0;
			for(final MatcherGroup g: matches)
			{
				sb.append(substitutedLine.substring(idx, g.start));
				sb.append(SANITIZER_MESSAGE);
				idx = g.end;
			}
			sb.append(substitutedLine.substring(idx, substitutedLine.length()));
			substitutedLine = sb.toString();
		}
		return substitutedLine;
	}

	private static class MatcherGroup implements Comparable<MatcherGroup>
	{
		private final Integer start;
		private final int end;

		MatcherGroup(int start, int end)
		{
			this.start = start;
			this.end = end;
		}

		@Override
		public int compareTo(MatcherGroup o)
		{
			return this.start.compareTo(o.start);
		}

		@Override
		public String toString()
		{
			return "MatcherGroup{" + "start=" + this.start + ", end=" + this.end + '}';
		}
	}

	public void cleanUpTempFiles()
	{
		for(Iterator<File> fileIterator = this.tmpFiles.iterator(); fileIterator.hasNext();)
		{
			File file = fileIterator.next();
			if( ! file.delete())
			{
				log.warn("Unable to delete temp file: " + file.getAbsolutePath());
				file.deleteOnExit();
			}
			fileIterator.remove();
		}
	}
}
