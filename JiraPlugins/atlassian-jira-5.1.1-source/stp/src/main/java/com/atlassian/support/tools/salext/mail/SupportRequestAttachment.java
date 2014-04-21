package com.atlassian.support.tools.salext.mail;

import java.io.Serializable;

public class SupportRequestAttachment implements Serializable
{
	private final String name;
	private final String type;
	private final Serializable data;
	
	public SupportRequestAttachment(String name, String type, Serializable data)
	{
		this.name = name;
		this.type = type;
		this.data = data;
	}

	public String getName()
	{
		return name;
	}

	public String getType()
	{
		return type;
	}

	public Serializable getData()
	{
		return data;
	}
}
