package com.sysbliss.diagram.geom
{

	import com.sysbliss.util.AbstractClassEnforcer;
	
	import flash.display.IGraphicsData;
	import flash.geom.Point;
	import flash.geom.Rectangle;

	public class AbstractLine implements Line
	{
		protected var _startPoint:Point;
		protected var _endPoint:Point;
		
		public function AbstractLine(startPoint:Point,endPoint:Point)
		{
			AbstractClassEnforcer.enforceConstructor(this,AbstractLine);
			this._startPoint = startPoint;
			this._endPoint = endPoint;
		}

		public function getGraphicsData():Vector.<IGraphicsData>
		{
			return new Vector.<IGraphicsData>();

		}
		
		public function set startPoint(p:Point):void
		{
			this._startPoint = p;
		}
		
		public function get startPoint():Point
		{
			return _startPoint;
		}
		
		public function moveStartPoint(newX:Number,newY:Number):void {
			_startPoint.x = newX;
			_startPoint.y = newY;
		}
		
		public function set endPoint(p:Point):void
		{
			this._endPoint = p;
		}
		
		public function get endPoint():Point
		{
			return _endPoint;
		}
		
		public function moveEndPoint(newX:Number,newY:Number):void {
			_endPoint.x = newX;
			_endPoint.y = newY;
		}
		
		public function moveEndPointPreview(newX:Number,newY:Number):void {
			_endPoint.x = newX;
			_endPoint.y = newY;
		}
		
		public function getBoundingRectangle():Rectangle {
			AbstractClassEnforcer.enforceMethod("getBoundingRectangle");
			return new Rectangle(0,0,0,0);
		}
		
	}
}