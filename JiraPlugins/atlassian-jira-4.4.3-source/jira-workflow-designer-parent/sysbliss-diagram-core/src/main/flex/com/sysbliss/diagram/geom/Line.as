package com.sysbliss.diagram.geom
{

	import flash.display.IGraphicsData;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	public interface Line
	{
		function getGraphicsData():Vector.<IGraphicsData>;
		function set startPoint(p:Point):void;
		function get startPoint():Point;
		function moveStartPoint(newX:Number,newY:Number):void;
		function set endPoint(p:Point):void;
		function get endPoint():Point;
		function moveEndPoint(newX:Number,newY:Number):void;
		function moveEndPointPreview(newX:Number, newY:Number):void;
		function getBoundingRectangle():Rectangle;
	}
}