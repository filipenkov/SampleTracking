package com.sysbliss.diagram.event
{
	import flash.events.Event;

	public class SelectableEvent extends Event
	{
		public static const SELECTED:String = "selected";
		public static const SELECTION_ADDED:String = "selectionAdded";
		public static const SELECT_ALL:String = "selectAll";
		public static const DESELECTED:String = "deselected";
		public static const SELECTION_REMOVED:String = "selectionRemoved";
		public static const DESELECT_ALL:String = "deselectAll";
		
		[Bindable]
		public var data:Object;
		
		public function SelectableEvent(type:String, data:Object, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.data = data;
		}
		
		override public function clone():Event {
			return new SelectableEvent(type, data, bubbles, cancelable);
		}
		
	}
}