package com.atlassian.support.tools.salext.bundle;

public abstract class AbstractApplicationFileBundle implements ApplicationInfoBundle
{
	private final String title;
	private final String description;
	private boolean selected = true;
	private final BundleManifest bundle;

	/**
	 * 
	 * @param bundle
	 *            A BundleManifest object.
	 * 
	 * @param title
	 *            A text string or i18n key that will be displayed as the title
	 *            for this group of files.
	 * @param description
	 *            A text string or i18n key that will be displayed at the
	 *            description for this group of files.
	 */
	public AbstractApplicationFileBundle(BundleManifest bundle, String title, String description)
	{
		this.bundle = bundle;
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
		return bundle.getKey();
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
	
	public String getBundlePriorityKey()
	{
		return bundle.getPriority().getPriorityKey();
	}

	public boolean isRequired() {
		return bundle.getPriority().equals(BundlePriority.REQUIRED);
	}
}
