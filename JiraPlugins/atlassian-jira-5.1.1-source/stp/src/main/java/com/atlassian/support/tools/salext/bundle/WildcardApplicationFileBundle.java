package com.atlassian.support.tools.salext.bundle;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.log4j.Logger;

import com.atlassian.support.tools.ValidationLog;

public class WildcardApplicationFileBundle extends AbstractApplicationFileBundle
{
	private static final Logger log = Logger.getLogger(WildcardApplicationFileBundle.class);
	private final String wildcard;
	private final String directory;
	
	public WildcardApplicationFileBundle(BundleManifest bundleName, String title, String description, String directory, String wildcard)
	{
		super(bundleName, title, description);

		File dir = new File(directory);
		if(dir.exists() && ! dir.isDirectory())
		{
			throw new IllegalArgumentException(directory + " is a directory rather than a file.");
		}

		this.wildcard = wildcard;
		this.directory = directory;
	}

	@Override
	public List<String> getFiles()
	{
		File dir = new File(this.directory);
		if( ! dir.exists())
		{
			return Collections.emptyList();
		}

		List<String> filteredFiles = new ArrayList<String>();
		File[] files = dir.listFiles((FilenameFilter) new RegexFileFilter(this.wildcard));
		for(int i = 0; i < files.length; i++)
		{
			filteredFiles.add(files[i].getAbsolutePath());
		}
		return filteredFiles;
	}

	@Override
	public void validate(ValidationLog validationLog)
	{
		File dir = new File(this.directory);
		if(dir.exists())
		{
			List<String> files = getFiles();

			for(String filename: files)
			{
				File file = new File(filename);
				if(file.length() == 0)
				{
					log.warn("The file " + file.getAbsolutePath() + " does not contain any data, and was skipped.");
				}
			}
		}
		else
		{
			log.warn("The directory " + dir.getAbsolutePath() + " does not contain any usable files, and was skipped.");
		}
	}

}
