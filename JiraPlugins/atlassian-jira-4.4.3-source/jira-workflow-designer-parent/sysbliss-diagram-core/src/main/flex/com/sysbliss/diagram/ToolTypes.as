package com.sysbliss.diagram
{
import com.sysbliss.diagram.tools.DiagramAnnotationTool;
import com.sysbliss.diagram.tools.DiagramBezierTool;
	import com.sysbliss.diagram.tools.DiagramLayoutAutoTool;
	import com.sysbliss.diagram.tools.DiagramLayoutLoadTool;
	import com.sysbliss.diagram.tools.DiagramLayoutSaveTool;
	import com.sysbliss.diagram.tools.DiagramLinkTool;
	import com.sysbliss.diagram.tools.DiagramPolygonTool;
	import com.sysbliss.diagram.tools.DiagramSelectTool;
	import com.sysbliss.diagram.tools.DiagramStraightTool;
	import com.sysbliss.diagram.tools.DiagramTool;
	import com.sysbliss.diagram.tools.DiagramZoomInTool;
	import com.sysbliss.diagram.tools.DiagramZoomOutTool;
	
	public class ToolTypes
	{
		public static const TOOL_SELECT:DiagramTool = new DiagramSelectTool();
		public static const TOOL_LINK:DiagramTool = new DiagramLinkTool();
		public static const TOOL_ZOOM_IN:DiagramTool = new DiagramZoomInTool();
		public static const TOOL_ZOOM_OUT:DiagramTool = new DiagramZoomOutTool();

		public static const LINK_BEZIER:DiagramTool = new DiagramBezierTool();
		public static const LINK_POLY:DiagramTool = new DiagramPolygonTool();
		public static const LINK_STRAIGHT:DiagramTool = new DiagramStraightTool();

        public static const TOOL_ANNOTATION:DiagramTool = new DiagramAnnotationTool();
		
		public static const LAYOUT_SAVE:DiagramTool = new DiagramLayoutSaveTool();
		public static const LAYOUT_LOAD:DiagramTool = new DiagramLayoutLoadTool();
		public static const LAYOUT_AUTO:DiagramTool = new DiagramLayoutAutoTool();

	}
}