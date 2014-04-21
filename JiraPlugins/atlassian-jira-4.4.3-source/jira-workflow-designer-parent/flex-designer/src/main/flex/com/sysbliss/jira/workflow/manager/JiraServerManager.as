package com.sysbliss.jira.workflow.manager
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraServerInfo;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraUserPrefs;
	
	public interface JiraServerManager
	{
		function getUserPrefs():FlexJiraUserPrefs;
		function setUserPrefs(prefs:FlexJiraUserPrefs):void;
		function getServerInfo():FlexJiraServerInfo;
		function setServerInfo(info:FlexJiraServerInfo):void;
	}
}