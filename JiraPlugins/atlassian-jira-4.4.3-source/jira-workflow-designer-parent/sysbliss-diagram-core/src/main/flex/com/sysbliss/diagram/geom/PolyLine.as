package com.sysbliss.diagram.geom
{

	import flash.display.GraphicsPath;
	import flash.display.GraphicsPathCommand;
	import flash.display.IGraphicsData;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	public class PolyLine extends AbstractSegmentedLine
	{
		private var _startPath:GraphicsPath;
		private var _endPath:GraphicsPath;
		private var _drawing:Vector.<IGraphicsData>;
		private var _segmentPaths:Vector.<GraphicsPath>;
		private var _minX:Number;
		private var _minY:Number;
		private var _maxX:Number;
		private var _maxY:Number;
		
		public function PolyLine(startPoint:Point,endPoint:Point,controlPoints:Vector.<Point>)
		{
			super(startPoint,endPoint,controlPoints);

			init();	
			calculateMinimumBounds();		
		}
		
		private function init():void {
			_startPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
			_startPath.commands.push(GraphicsPathCommand.MOVE_TO);
			_startPath.data.push(_startPoint.x, _startPoint.y);
			
			_endPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
			_endPath.commands.push(GraphicsPathCommand.LINE_TO);
			_endPath.data.push(_endPoint.x, _endPoint.y);
			
			_segmentPaths = new Vector.<GraphicsPath>();
			
			var i:int;
			var p:Point;
			for(i=0;i<_controlPoints.length;i++){
				p = _controlPoints[i];
				
				var gPath:GraphicsPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
				gPath.commands.push(GraphicsPathCommand.LINE_TO);
				gPath.data.push(p.x, p.y);
				_segmentPaths.push(gPath);
			}
		}
		
		override public function getGraphicsData():Vector.<IGraphicsData>
		{
			_drawing = new Vector.<IGraphicsData>();
			_drawing.push(_startPath);
			
			//if we have no controls, just make a line
			if(_controlPoints.length < 1){
				_drawing.push(_endPath);
			} else {
				var i:int;
				for(i=0;i<_segmentPaths.length;i++){
					_drawing.push(_segmentPaths[i]);
				}
				_drawing.push(_endPath);
			}
			
			return _drawing;
		}
		
		override public function moveEndPoint(newX:Number, newY:Number):void {
			super.moveEndPoint(newX,newY);
			calculateMinimumBounds();
			_endPath.data[0] = newX;
			_endPath.data[1] = newY;
		}
		
		override public function moveEndPointPreview(newX:Number, newY:Number):void {
			super.moveEndPointPreview(newX,newY);
			calculateMinimumBounds();
			_endPath.data[0] = newX;
			_endPath.data[1] = newY;
		}
		
		override public function moveStartPoint(newX:Number, newY:Number):void {
            calculateMinimumBounds();
			super.moveStartPoint(newX,newY);

			_startPath.data[0] = newX;
			_startPath.data[1] = newY;
		}
		
		override public function moveControlPoint(p:Point, newX:Number, newY:Number):void {
			if(p.x != newX || p.y != newY){
				var myIndex:int = _controlPoints.indexOf(p);
				if(myIndex > -1){
					super.moveControlPoint(p,newX,newY);
					calculateMinimumBounds();
					var gPath:GraphicsPath = _segmentPaths[myIndex];
					gPath.data[0] = newX;
					gPath.data[1] = newY;
				}
			}
			
		}
		
		override public function insertControlPointAt(p:Point,i:int):void {
			super.insertControlPointAt(p,i);
			calculateMinimumBounds();
			var gPath:GraphicsPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
			gPath.commands.push(GraphicsPathCommand.LINE_TO);
			gPath.data.push(p.x, p.y);
			
			_segmentPaths.splice(i,0,gPath);
		}
		
		override public function addControlPoint(p:Point):void {
			super.addControlPoint(p);
			calculateMinimumBounds();
			var gPath:GraphicsPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
			gPath.commands.push(GraphicsPathCommand.LINE_TO);
			gPath.data.push(p.x, p.y);
			
			_segmentPaths.push(gPath);		
		}
		
		override public function removeControlPointAt(i:int):void {
			super.removeControlPointAt(i);
			_segmentPaths.splice(i,1);
			calculateMinimumBounds();
		}
		
		override public function getBoundingRectangle():Rectangle {
            calculateMinimumBounds();
			return new Rectangle(_minX,_minY,_maxX-_minX,_maxY-_minY);
		}
		
		override public function findInsertionIndexForPoint(p:Point):int {
			var i:int;
			var foundIndex:int = -1;
			var currentPoint:Point;
			var nextPoint:Point;
			var rect:Rectangle;
			
			var minX:Number;
			var minY:Number;
			var maxX:Number;
			var maxY:Number;
			
			var _tmp:Vector.<Point> = new Vector.<Point>();
			_tmp.push(_startPoint);
			var _allPoints:Vector.<Point> = _tmp.concat(_controlPoints);
			_allPoints.push(_endPoint);
			for(i=0; i<_allPoints.length-1;i++){
				currentPoint = _allPoints[i];
				nextPoint = _allPoints[i+1];
				
				minX = Math.min(currentPoint.x,nextPoint.x);
				minY = Math.min(currentPoint.y,nextPoint.y);
				maxX = Math.max(currentPoint.x,nextPoint.x);
				maxY = Math.max(currentPoint.y,nextPoint.y);
				
				rect = new Rectangle(minX-5,minY-5,(maxX-minX)+5,(maxY-minY)+5);
				if(rect.containsPoint(p)){
					if(i<(_allPoints.length-1)){
						foundIndex = i;
					} else {
						foundIndex = (i - 1);
					}
					break;
				}
			}
			
			return foundIndex;
		}

		private function calculateMinimumBounds():void {
			_minX = Number.MAX_VALUE;
			_minY = Number.MAX_VALUE;
			_maxX = 0;
			_maxY = 0;
			
			var _tmp:Vector.<Point> = new Vector.<Point>();
			_tmp.push(_startPoint);
			var _allPoints:Vector.<Point> = _tmp.concat(_controlPoints);
			_allPoints.push(_endPoint);
			
			var i:int;
			var p:Point;
			for(i=0;i<_allPoints.length;i++){
				p = _allPoints[i];
				if(p.x < _minX){
					_minX = p.x;
				}
				if(p.x > _maxX){
					_maxX = p.x;
				}
				if(p.y < _minY){
					_minY = p.y;
				}
				if(p.y > _maxY){
					_maxY = p.y;
				}
			}
		}
	}
}