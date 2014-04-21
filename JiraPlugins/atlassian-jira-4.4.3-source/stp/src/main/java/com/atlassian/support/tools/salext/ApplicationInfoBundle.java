package com.atlassian.support.tools.salext;

import java.util.List;

import com.atlassian.support.tools.ValidationLog;

public interface ApplicationInfoBundle
{
	public String getTitle();

	public String getDescription();

	public List<String> getFiles();

	public String getKey();

	public void validate(ValidationLog validationLog);

	public void setSelected(boolean b);

	public boolean isSelected();
}