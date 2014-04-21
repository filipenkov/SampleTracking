package com.sysbliss.jira.workflow.event
{
	import flash.events.Event;

	public class GenericDataEvent extends Event
	{
		[Bindable]
		public var data:Object;
		
		public function GenericDataEvent(type:String, d:Object, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.data = d;
		}
		
		override public function clone():Event {
			return new GenericDataEvent(type, data, bubbles, cancelable);
		}
		
	}
}