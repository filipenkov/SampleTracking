package com.sysbliss.diagram.geom
{
	import flash.geom.Point;
	
	public interface CubicBezierLine extends SegmentedLine
	{
		function getPositiveControllerForPoint(p:Point):Point;
		function getNegativeControllerForPoint(p:Point):Point;
		function getPositiveControllerAt(i:int):Point;
		function getNegativeControllerAt(i:int):Point;
		function movePositiveController(p:Point,newX:Number,newY:Number):void;
		function moveNegativeController(p:Point,newX:Number,newY:Number):void;
		function movePositiveControllerAt(i:int,newX:Number,newY:Number):void;
		function moveNegativeControllerAt(i:int,newX:Number,newY:Number):void;
		function get includeSegmentBoundries():Boolean;
		function set includeSegmentBoundries(b:Boolean):void;
		
	}
}