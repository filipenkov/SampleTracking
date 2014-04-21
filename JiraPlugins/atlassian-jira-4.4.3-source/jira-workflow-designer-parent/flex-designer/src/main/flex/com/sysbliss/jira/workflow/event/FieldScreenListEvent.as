package com.sysbliss.jira.workflow.event
{
	import flash.events.Event;
	
	import mx.collections.ArrayCollection;

	public class FieldScreenListEvent extends Event
	{
		[Bindable]
		public var screens:ArrayCollection;
		
		public function FieldScreenListEvent(type:String, screenList:ArrayCollection, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.screens = screenList;
		}
		
		override public function clone():Event {
			return new StatusListEvent(type, screens, bubbles, cancelable);
		}
		
	}
}