package com.sysbliss.diagram
{

import com.sysbliss.diagram.data.Annotation;
import com.sysbliss.diagram.data.Edge;
	import com.sysbliss.diagram.data.Node;
	import com.sysbliss.diagram.tools.DiagramTool;
import com.sysbliss.diagram.ui.StickyNote;
import com.sysbliss.diagram.ui.UIEdge;
	import com.sysbliss.diagram.ui.UINode;
	import com.sysbliss.diagram.ui.selectable.SelectionManager;

    import flash.events.KeyboardEvent;
    import flash.geom.Point;
	import flash.geom.Rectangle;

	import mx.collections.ArrayCollection;
	import mx.core.IContainer;
	import mx.core.IUIComponent;
	import mx.styles.IStyleClient;

	public interface Diagram extends IUIComponent,IStyleClient,IContainer
	{
		
		function createNode(rendererClass:Class=null,data:Object=null,location:Point=null):Node;
		function createLink(startNode:Node,endNode:Node,edgeRendererClass:Class=null,edgeLabelRendererClass:Class=null,data:Object=null,lineType:String="",startPoint:Point=null,endPoint:Point=null,controlPoints:Vector.<Point>=null,knotRendererClass:Class=null):Edge;
		function createNodeQuietly(rendererClass:Class=null,data:Object=null,location:Point=null):Node;
		function createLinkQuietly(startNode:Node,endNode:Node,edgeRendererClass:Class=null,edgeLabelRendererClass:Class=null,data:Object=null,lineType:String="",startPoint:Point=null,endPoint:Point=null,controlPoints:Vector.<Point>=null,knotRendererClass:Class=null):Edge;
		
		
		function getNodes():Vector.<Node>;
		function getNode(id:String):Node;
		function getUINodes():Vector.<UINode>;
		function getUINode(id:String):UINode;
		
		function getEdges():Vector.<Edge>;
		function getEdge(id:String):Edge;
		function getUIEdges():Vector.<UIEdge>;
		function getUIEdge(id:String):UIEdge;
		
		function get currentTool():DiagramTool;
		function set currentTool(t:DiagramTool):void;
		function set currentLineType(s:String):void;
		function get currentLineType():String;
		function set previewEdgeRenderer(c:Class):void;
		function get previewEdgeRenderer():Class;
		
		function set defaultEdgeRenderer(c:Class):void;
		function get defaultEdgeRenderer():Class;

		function set defaultEdgeLabelRenderer(c:Class):void;
		function get defaultEdgeLabelRenderer():Class;
		
		function set defaultKnotRenderer(c:Class):void;
		function get defaultKnotRenderer():Class;
		
		function set defaultKnotControllerRenderer(c:Class):void;
		function get defaultKnotControllerRenderer():Class;
		
		function updateSelectedEdgesLineType(lineType:String):void;
		function updateAllEdgesLineType(lineType:String):void;
		
		function get selectionManager():SelectionManager;
		function selectAll():void;
		function deselectAll():void;
		function get centerPoint():Point;
		function get visibleCenterPoint():Point;
		function get visibleRectangle():Rectangle;
		
		function get edgeLayer():DiagramLayer;
		function get nodeLayer():DiagramLayer;
		function get controlsLayer():DiagramLayer;
		function get contentLayer():DiagramLayer;
        function get labelLayer():DiagramLayer
		function getRootUINode():UINode;
		
		function deleteEdge(edge:Edge):void;
		function deleteNode(node:Node):void;
		function forceDeleteEdge(edge:Edge):void;
		function forceDeleteNode(node:Node):void;
		function deleteSelected():void;
		function forceDeleteSelected():void;
		
		function zoomIn(centerX:Number=-1,centerY:Number=-1):void;
		function zoomOut(centerX:Number=-1,centerY:Number=-1):void;
		
		function scrollToPoint(p:Point):void;
		
		function get layout():ArrayCollection;
		function set layout(roots:ArrayCollection):void;
		
		function getRootUINodes():Array;

        function showLabels():void;
        function hideLabels():void;
        function toggleLabels():void;

        function passThruKeyHandler(e:KeyboardEvent):void;

        function isLinking():Boolean;

        function cancelLink():void;

        function startScrollTimer():void;
        function stopScrollTimer():void;

        function createAnnotation(annotation:Annotation):void;
        function removeAnnotation(sticky:StickyNote):void;
        function getStickyByAnnotationId(id:String):StickyNote;
        function updateStickyDisplay(sticky:StickyNote, rect:Rectangle):void;
        function getAllStickies():Array;

	}
}