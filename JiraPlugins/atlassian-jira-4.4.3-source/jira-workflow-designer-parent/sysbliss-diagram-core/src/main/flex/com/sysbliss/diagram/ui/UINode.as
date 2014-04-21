package com.sysbliss.diagram.ui
{
	import com.sysbliss.diagram.data.Node;
	import com.sysbliss.diagram.renderer.NodeRenderer;
	
	import flash.geom.Point;
	
	public interface UINode extends DiagramUIObject
	{
		function get nodeRenderer():NodeRenderer;
		function get node():Node;
		function get centerPoint():Point;
		function set nodeRendererClass(c:Class):void;
	}
}