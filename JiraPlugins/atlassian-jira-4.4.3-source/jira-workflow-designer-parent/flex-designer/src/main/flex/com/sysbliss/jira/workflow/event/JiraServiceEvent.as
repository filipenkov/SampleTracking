package com.sysbliss.jira.workflow.event
{
	import flash.events.Event;

	public class JiraServiceEvent extends Event
	{
		static public const GET_WORKFLOWS:String = "getWorkflowsResult";
		static public const LOAD_WORKFLOW:String = "loadWorkflowResult";
		static public const GET_ALL_STATUSES:String = "getAllStatusesResult";
		
		[Bindable]
		public var data:*;
		
		public function JiraServiceEvent(type:String, data:*, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.data = data;
		}
		
		override public function clone():Event {
			return new JiraServiceEvent(type, data, bubbles, cancelable);
		}
		
	}
}