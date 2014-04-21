package com.atlassian.support.tools.salext;

public abstract class AbstractApplicationFileBundle implements ApplicationInfoBundle
{
	private final String key;
	private final String title;
	private final String description;
	private boolean selected = true;

	/**
	 * @param title
	 *            A text string or i18n key that will be displayed as the title
	 *            for this group of files.
	 * @param description
	 *            A text string or i18n key that will be displayed at the
	 *            description for this group of files.
	 * @param files
	 *            One or more strings pointing to a file location.
	 */
	public AbstractApplicationFileBundle(String key, String title, String description)
	{
		if( ! key.matches("[a-zA-Z-]+"))
		{
			throw new IllegalArgumentException(key);
		}

		this.key = key;
		this.title = title;
		this.description = description;
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
	public String getKey()
	{
		return this.key;
	}

	@Override
	public boolean isSelected()
	{
		return this.selected;
	}

	@Override
	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}
}
