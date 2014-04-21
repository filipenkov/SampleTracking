package com.sysbliss.jira.workflow.service
{

	import mx.core.Application;
	import mx.logging.ILogger;
	import mx.utils.URLUtil;
	
	public class JiraServiceConfiguration implements RemotingConfiguration
	{
		
		private var _server:String;
		private var _protocol:String;
		private var _endpointServer:String;
			
		public function JiraServiceConfiguration()
		{ 
			this._protocol = URLUtil.getProtocol(Application.application.url);
			this._server = URLUtil.getServerNameWithPort(Application.application.url);
			
			var start:int = Application.application.url.indexOf(_server) + (_server.length+1);
			var end:int = Application.application.url.indexOf("/s/");
			var context:String = Application.application.url.substring(start,end);
			
			var srv:String = _protocol + "://" + _server;
			if(context != null && context != ""){
				this._endpointServer = srv + "/" + context;
			} else {
				this._endpointServer = srv;
			}
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

class SingletonLock{};
