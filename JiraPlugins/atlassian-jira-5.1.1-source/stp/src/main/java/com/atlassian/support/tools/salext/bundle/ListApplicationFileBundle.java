package com.atlassian.support.tools.salext.bundle;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.support.tools.ValidationLog;

public class ListApplicationFileBundle extends AbstractApplicationFileBundle
{
	private final List<String> files;
	private static final Logger log = Logger.getLogger(ListApplicationFileBundle.class);
	
	public ListApplicationFileBundle(BundleManifest bundle, String title, String description, String baseDirectory, List<String> files)
	{
		super(bundle, title, description);
		this.files = files;
	}

	@Override
	public List<String> getFiles()
	{
		return files;
	}

	@Override
	public void validate(ValidationLog validationLog)
	{
		for(String filename: files)
		{
			File file = new File(filename);
			if(file.length() == 0)
			{
				log.warn("The file " + file.getAbsolutePath() + " does not contain any data, and was skipped.");
			}
		}
	}

}
