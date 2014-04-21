package com.atlassian.support.tools.salext.bundle;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.support.tools.ValidationLog;

public class DefaultApplicationFileBundle extends AbstractApplicationFileBundle
{
	private static final Logger log = Logger.getLogger(DefaultApplicationFileBundle.class);
	private final List<String> files;

	/**
	 * @param bundle
	 *            A BundleManifest object.
	 * @param title
	 *            A text string or i18n key that will be displayed as the title
	 *            for this group of files.
	 * @param description
	 *            A text string or i18n key that will be displayed at the
	 *            description for this group of files.
	 * @param files
	 *            One or more strings pointing to a file location.
	 */
	public DefaultApplicationFileBundle(BundleManifest bundle, String title, String description, String... files)
	{
		super(bundle, title, description);
		this.files = Arrays.asList(files);
	}

	@Override
	public List<String> getFiles()
	{
		return this.files;
	}

	@Override
	public void validate(ValidationLog validationLog)
	{
		for(String filename: this.files)
		{
			File file = new File(filename);
			if( ! file.exists())
			{
				log.warn("The file " + file.getAbsolutePath()
						+ " could not be found or is not readable, and was skipped.");
			}
		}
	}
}
