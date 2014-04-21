package com.sysbliss.jira.workflow.manager
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraServerInfo;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraUserPrefs;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraUserPrefsImpl;

	public class JiraServerManagerImpl implements JiraServerManager
	{
		private var _prefs:FlexJiraUserPrefs;
		private var _info:FlexJiraServerInfo;
		
		public function JiraServerManagerImpl()
		{
		}

		public function getUserPrefs():FlexJiraUserPrefs{
			var prefs:FlexJiraUserPrefs = new FlexJiraUserPrefsImpl();
			prefs.confirmDeleteSelection = true;
			prefs.confirmDeleteWorkflow = true;
			return prefs;
			//return _prefs;
		}
		
		public function setUserPrefs(prefs:FlexJiraUserPrefs):void{
			this._prefs = prefs;
		}
		
		public function getServerInfo():FlexJiraServerInfo{
			return _info;
		}
		
		public function setServerInfo(info:FlexJiraServerInfo):void{
			this._info = info;
		}
		
	}
}