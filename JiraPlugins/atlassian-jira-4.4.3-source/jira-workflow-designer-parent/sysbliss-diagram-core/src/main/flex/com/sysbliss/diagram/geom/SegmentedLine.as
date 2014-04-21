package com.sysbliss.diagram.geom
{

	import flash.geom.Point;
	
	public interface SegmentedLine extends Line
	{
		function set controlPoints(points:Vector.<Point>):void;
		function get controlPoints():Vector.<Point>;
		function moveControlPoint(p:Point,newX:Number,newY:Number):void;
		function moveControlPointAt(i:int,newX:Number,newY:Number):void;
		function addControlPoint(p:Point):void;
		function pushControlPoint(p:Point):void;
		function insertControlPoint(p:Point):void;
		function insertControlPointAt(p:Point,i:int):void;
		function findInsertionIndexForPoint(p:Point):int;
		function removeControlPoint(p:Point):void;
		function removeControlPointAt(i:int):void;		
	}
}