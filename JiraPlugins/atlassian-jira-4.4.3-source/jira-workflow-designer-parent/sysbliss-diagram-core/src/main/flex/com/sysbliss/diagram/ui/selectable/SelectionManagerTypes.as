package com.sysbliss.diagram.ui.selectable
{
	public class SelectionManagerTypes
	{
		public static const DEFAULT:String = "default";
		public static const DIAGRAM_OBJECTS:String = "diagramObjects";
		public static const EDGE_CONTROLS:String = "edgeControls";
        public static const EDGE_LABELS:String = "edgeLabels";
		public static const POINT_CONTROLS:String = "pointControls";
		
		public function SelectionManagerTypes(lock:Class)
		{
			if(lock != PrivateLock){  
				throw new Error( "Invalid private access." );  
			}
		}

	}
}

class PrivateLock{}
