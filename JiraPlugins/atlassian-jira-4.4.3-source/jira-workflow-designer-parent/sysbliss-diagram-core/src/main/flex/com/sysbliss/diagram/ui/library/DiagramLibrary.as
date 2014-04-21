package com.sysbliss.diagram.ui.library
{
	import com.sysbliss.collections.HashMap;
	import com.sysbliss.diagram.Diagram;
    import com.sysbliss.diagram.renderer.EdgeLabelRenderer;
    import com.sysbliss.diagram.renderer.EdgeRenderer;
	import com.sysbliss.diagram.renderer.NodeRenderer;
	
	public interface DiagramLibrary
	{
		function addNodeRenderer(name:String,renderer:Class,group:String=null):void;
		function getNodeRendererInstance(name:String,diagram:Diagram,group:String=null):NodeRenderer;
		function getNodeRendererClass(name:String,group:String=null):Class;
		function getNodeGroup(group:String):HashMap;
		function getAllNodeGroups():HashMap;
		function hasNodeRenderer(type:String,group:String=null):Boolean;
		
		function addEdgeRenderer(name:String,renderer:Class,group:String=null):void;
		function getEdgeRendererInstance(name:String,diagram:Diagram,group:String=null):EdgeRenderer;
		function getEdgeRendererClass(name:String,group:String=null):Class;
		function getEdgeGroup(group:String):HashMap;
		function getAllEdgeGroups():HashMap;
		function hasEdgeRenderer(type:String,group:String=null):Boolean;

		function addEdgeLabelRenderer(name:String,renderer:Class,group:String=null):void;
		function getEdgeLabelRendererInstance(name:String,diagram:Diagram,group:String=null):EdgeLabelRenderer;
		function getEdgeLabelRendererClass(name:String,group:String=null):Class;
		function getEdgeLabelGroup(group:String):HashMap;
		function getAllEdgeLabelGroups():HashMap;
		function hasEdgeLabelRenderer(type:String,group:String=null):Boolean;
	}
}