package com.sysbliss.jira.workflow.event
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	
	import flash.events.Event;

	public class WorkflowEvent extends Event
	{
		public static const OBJECT_DELETION:String = "objectDeletion";
		public static const TRANSITION_ADD:String = "transitionAdd";
		public static const EDIT_TRANSITION:String = "editTransition";
		public static const STATUS_REFRESH:String = "statusRefresh";
		
		[Bindable]
		public var workflow:FlexJiraWorkflow;
		
		[Bindable]
		public var reason:String;
		
		public function WorkflowEvent(type:String, wf:FlexJiraWorkflow, why:String="", bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.workflow = wf;
			this.reason = why;
		}
		
		override public function clone():Event {
			return new WorkflowEvent(type, workflow, reason, bubbles, cancelable);
		}
		
	}
}