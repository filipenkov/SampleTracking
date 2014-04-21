package com.sysbliss.jira.workflow.event
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class StatusListEvent extends Event
	{
		[Bindable]
		public var statuses:ArrayCollection;
		
		public function StatusListEvent(type:String, arrcoll:ArrayCollection, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.statuses = arrcoll;
		}
		
		override public function clone():Event {
			return new StatusListEvent(type, statuses, bubbles, cancelable);
		}
		
	}
}