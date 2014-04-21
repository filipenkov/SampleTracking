package com.sysbliss.jira.workflow.service
{
	public interface RemotingConfiguration
	{
		function get destination():String;
		function get endpoint():String;
		function get baseurl():String;
	}
}