package com.sysbliss.diagram.renderer
{
	import flash.display.Graphics;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	public interface KnotRenderer
	{
		function draw(g:Graphics,selected:Boolean,rotation:Number=0):void;
		function getBounds():Rectangle;
	}
}