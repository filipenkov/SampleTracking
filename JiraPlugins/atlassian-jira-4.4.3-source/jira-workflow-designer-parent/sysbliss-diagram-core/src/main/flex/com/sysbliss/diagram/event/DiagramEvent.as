package com.sysbliss.diagram.event
{
	import flash.events.Event;

	public class DiagramEvent extends Event
	{
		public static const NODE_CLICK:String = "nodeClick";
		public static const NODE_DOUBLE_CLICK:String = "nodeDoubleClick";
		public static const NODE_ENTER_KEY:String = "nodeEnterKey";

		public static const EDGE_LABEL_CLICK:String = "edgeLabelClick";
		public static const EDGE_LABEL_DOUBLE_CLICK:String = "edgeLabelDoubleClick";
		public static const EDGE_LABEL_ENTER_KEY:String = "edgeLabelEnterKey";

		public static const EDGE_CLICK:String = "edgeClick";
		public static const EDGE_CONTROL_POINT_INSERTED:String = "edgeControlPointInserted";
		public static const EDGE_DOUBLE_CLICK:String = "edgeDoubleClick";
		public static const EDGE_ENTER_KEY:String = "edgeEnterKey";
		public static const SELECTIONS_DELETED:String = "selectionsDeleted";
		public static const NODE_DELETED:String = "nodeDeleted";
		public static const NODE_CREATED:String = "nodeCreated";
		public static const EDGE_DELETED:String = "edgeDeleted";
		public static const EDGE_CREATED:String = "edgeCreated";
		public static const DIAGRAM_CHANGED:String = "diagramChanged";
		public static const DIAGRAM_ZOOMED:String = "diagramZoomed";

        public static const ANNOTATION_DELETED:String = "annotationDeleted";
        public static const ANNOTATION_UPDATED:String = "annotationUpdated";
		
		
		[Bindable]
		public var data:Object;
		
		public function DiagramEvent(type:String, data:Object, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.data = data;
		}
		
		override public function clone():Event {
			return new DiagramEvent(type, data, bubbles, cancelable);
		}
		
	}
}