package com.atlassian.support.tools;

/**
 * @author aatkins A simple enum to look up the stage in the SupportAction life
 *         cycle.
 * 
 */
public enum Stage
{
	START, EXECUTE;

	public static Stage lookup(String name)
	{
		if("execute".equalsIgnoreCase(name))
			return EXECUTE;
		else
			return START;
	}
}
