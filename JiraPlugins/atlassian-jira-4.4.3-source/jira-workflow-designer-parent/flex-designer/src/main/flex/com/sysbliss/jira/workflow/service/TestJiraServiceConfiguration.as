package com.sysbliss.jira.workflow.service
{

	import mx.logging.ILogger;
	
	public class TestJiraServiceConfiguration implements RemotingConfiguration
	{
		private var _server:String;
		private var _protocol:String;
		private var _endpointServer:String;
			
		public function TestJiraServiceConfiguration()
		{

			this._protocol = "http";
			this._server = "localhost:2990/jira";
			this._endpointServer = _protocol + "://" + _server;
		}
		
		public function get endpoint():String {
			return _endpointServer + "/plugins/servlet/jwd/amf/";
		}
		
		public function get destination():String {
			return "com.atlassian.jira.plugins.jira-workflow-designer:workflowDesignerService";
		}
		
		public function get baseurl():String {
			return _endpointServer;
		}
	}
}