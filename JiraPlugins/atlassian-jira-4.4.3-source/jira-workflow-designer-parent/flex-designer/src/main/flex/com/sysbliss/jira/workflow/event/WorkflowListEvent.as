package com.sysbliss.jira.workflow.event
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;
	
	public class WorkflowListEvent extends Event
	{
		[Bindable]
		public var workflows:ArrayCollection;
		
		public function WorkflowListEvent(type:String, arrcoll:ArrayCollection, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.workflows = arrcoll;
		}
		
		override public function clone():Event {
			return new WorkflowListEvent(type, workflows, bubbles, cancelable);
		}
		
	}
}