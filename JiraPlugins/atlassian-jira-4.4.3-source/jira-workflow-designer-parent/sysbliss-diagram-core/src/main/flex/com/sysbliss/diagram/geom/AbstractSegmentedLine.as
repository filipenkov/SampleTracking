package com.sysbliss.diagram.geom
{

	import com.sysbliss.util.AbstractClassEnforcer;
	
	import flash.geom.Point;

	public class AbstractSegmentedLine extends AbstractLine implements SegmentedLine
	{
		protected var _controlPoints:Vector.<Point>;
		
		public function AbstractSegmentedLine(startPoint:Point,endPoint:Point,controlPoints:Vector.<Point>)
		{
			super(startPoint,endPoint);
			AbstractClassEnforcer.enforceConstructor(this,AbstractSegmentedLine);
			this._controlPoints = controlPoints;
			
			if(!_controlPoints){
				_controlPoints = new Vector.<Point>();
			}
		}
		
		public function set controlPoints(points:Vector.<Point>):void
		{
			this._controlPoints = points;
			if(!_controlPoints){
				_controlPoints = new Vector.<Point>();
			}
		}
		
		public function get controlPoints():Vector.<Point>
		{
			return this._controlPoints;
		}
		
		public function moveControlPoint(p:Point,newX:Number,newY:Number):void {
			if(_controlPoints.indexOf(p) > -1){
				p.x = newX;
				p.y = newY;
			}
		}
		
		public function moveControlPointAt(i:int,newX:Number,newY:Number):void {
			var p:Point = _controlPoints[i];
			if(p){
				p.x = newX;
				p.y = newY;
			}
		}
		
		public function addControlPoint(p:Point):void {
			_controlPoints.push(p);
		}
		
		public function pushControlPoint(p:Point):void
		{
			_controlPoints.push(p);
		}
		
		public function insertControlPoint(p:Point):void {
			var index:int = findInsertionIndexForPoint(p);
			insertControlPointAt(p,index);			
		}
		
		public function insertControlPointAt(p:Point,i:int):void {
			if(i > -1){
				_controlPoints.splice(i,0,p);
			}
		}
		
		public function removeControlPointAt(i:int):void {
			_controlPoints.splice(i,1);
		}
		
		public function removeControlPoint(p:Point):void
		{
			var i:int = _controlPoints.indexOf(p);
			if(i>-1){
				removeControlPointAt(i);
			}
		}
		
		public function findInsertionIndexForPoint(p:Point):int {
			AbstractClassEnforcer.enforceMethod("findInsertionIndexForPoint");
			return -1;
		}
		
				
	}
}