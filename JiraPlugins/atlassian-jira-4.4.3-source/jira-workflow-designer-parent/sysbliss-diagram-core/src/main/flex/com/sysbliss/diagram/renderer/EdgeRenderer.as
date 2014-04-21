package com.sysbliss.diagram.renderer
{
	import com.sysbliss.diagram.geom.Line;
	import com.sysbliss.diagram.ui.UIEdge;
	
	import flash.display.Graphics;
	import flash.geom.Point;
	
	public interface EdgeRenderer extends DiagramObjectRenderer
	{
		function draw(g:Graphics,line:Line,uiEdge:UIEdge):void;
		function drawSelected(g:Graphics,line:Line,uiEdge:UIEdge,selectedControlPoint:Point=null):void;
        function set lineColor(n:Number):void;
        function get lineColor():Number;
	}
}