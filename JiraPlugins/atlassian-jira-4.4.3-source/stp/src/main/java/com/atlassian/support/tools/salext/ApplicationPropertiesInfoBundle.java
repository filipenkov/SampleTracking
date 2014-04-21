package com.atlassian.support.tools.salext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.support.tools.ValidationLog;

public class ApplicationPropertiesInfoBundle implements ApplicationInfoBundle
{
	private String key;
	private String title;
	private String description;
	private boolean selected = true;
	private SupportApplicationInfo info;
	
	private static final Logger log = Logger.getLogger(ApplicationPropertiesInfoBundle.class);
	
	public ApplicationPropertiesInfoBundle(String key, String title, String description, SupportApplicationInfo info)
	{
		this.key = key;
		this.title = title;
		this.description = description;
		this.info = info;
	}

	@Override
	public String getTitle()
	{
		return this.title;
	}

	@Override
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public List<String> getFiles()
	{
		List<String> files = new ArrayList<String>();
		
		// Generate a temporary file with the current properties and return its location
		final File supportDir = new File(this.info.getApplicationHome(), "logs/support");
		
		if( ! supportDir.exists() && ! supportDir.mkdirs())
		{
			log.error("Couldn't create export directory " + supportDir.getAbsolutePath());
			return null; 
		}

		File propertiesFile = new File(supportDir,this.info.getApplicationName()+"-application.properties");
		try
		{
			FileWriter out = new FileWriter(propertiesFile);
			try
			{
				String propertiesString = info.savePropertiesForZip();
				out.write(propertiesString);
			}
			catch(IOException e)
			{
				log.error("Failed to write applicaiton properties to "+propertiesFile.getPath()+".", e);
			}
			finally
			{
				out.flush();
				out.close();
			}
			
			files.add(propertiesFile.getAbsolutePath());
		}
		catch(Exception e)
		{
			log.error("Can't generate properties file.", e);
			
		}
		
		return files;
	}

	@Override
	public String getKey()
	{
		return this.key;
	}

	@Override
	public void validate(ValidationLog validationLog)
	{
	}

	@Override
	public void setSelected(boolean b)
	{
		this.selected = b;
	}

	@Override
	public boolean isSelected()
	{
		return this.selected;
	}

}
