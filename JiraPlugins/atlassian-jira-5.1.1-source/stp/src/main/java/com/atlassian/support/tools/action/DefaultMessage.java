package com.atlassian.support.tools.action;

import com.atlassian.templaterenderer.annotations.HtmlSafe;

public class DefaultMessage implements Message
{
	private final String name;
	private final String body;

	public DefaultMessage(String name, String body)
	{
		this.name = name;
		this.body = body;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	@HtmlSafe
	public String getBody()
	{
		return this.body;
	}
	
	@Override
	public String toString() {
		return name + ":" + body;
	}
}
