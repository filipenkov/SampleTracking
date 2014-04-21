package com.sysbliss.diagram.event {
import flash.events.Event;

public class InteractiveDiagramObjectEvent extends Event {
    /** Dispatched once a move action has completed.
		 **/
		public static const OBJECT_MOVED_EVENT:String = "objectMovedEvent";

		/** Dispatched once a resize action has completed.
		 **/
		public static const OBJECT_RESIZED_EVENT:String = "objectResizedEvent";

		/** Dispatched while the object is resizing for each incremental resize.
		 **/
		public static const OBJECT_RESIZING_EVENT:String = "objectResizingEvent";

		/** Dispatched once a resize action has completed.
		 **/
		public static const OBJECT_ROTATED_EVENT:String = "objectRotatedEvent";

		/** Dispatched while the object is resizing for each incremental resize.
		 **/
		public static const OBJECT_ROTATING_EVENT:String = "objectRotatingEvent";


		/** Dispatched while the object is moving for each incremental move.
		 **/
		public static const OBJECT_MOVING_EVENT:String = "objectMovingEvent";

		/**
		 * Dispatched when the user selects the object.
		 **/
		public static const OBJECT_SELECTED:String = "objectSelected";

		public static const OBJECT_DELETE:String = "objectDelete";

		/** Dispatched when the user deselects the object.
		 **/
		public static const OBJECT_DESELECTED:String = "objectDeselected";

		public function InteractiveDiagramObjectEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
}
}
