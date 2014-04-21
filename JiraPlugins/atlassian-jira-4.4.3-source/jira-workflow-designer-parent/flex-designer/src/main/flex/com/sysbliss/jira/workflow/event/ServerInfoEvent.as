package com.sysbliss.jira.workflow.event
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraServerInfo;
	
	import flash.events.Event;

	public class ServerInfoEvent extends Event
	{
		public var serverInfo:FlexJiraServerInfo;
		
		public function ServerInfoEvent(type:String, info:FlexJiraServerInfo, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.serverInfo = info;
		}
		
		override public function clone():Event {
			return new ServerInfoEvent(type,serverInfo,bubbles,cancelable);
		}
		
	}
}