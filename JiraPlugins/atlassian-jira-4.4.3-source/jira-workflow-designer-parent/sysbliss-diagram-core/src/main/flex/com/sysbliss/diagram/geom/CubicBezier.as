package com.sysbliss.diagram.geom {
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;

import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;

import flash.display.GraphicsPath;
	import flash.display.GraphicsPathCommand;
	import flash.display.GraphicsSolidFill;
	import flash.display.GraphicsStroke;
	import flash.display.IGraphicsData;
	import flash.geom.Point;
	import flash.geom.Rectangle;

	public class CubicBezier extends AbstractSegmentedLine implements CubicBezierLine{
		
		private var _segments:Vector.<BezierSegment>;
		private var _posControlPoints:Vector.<Point>;
		private var _negControlPoints:Vector.<Point>;
		private var _segmentPaths:Vector.<GraphicsPath>;
		private var _startPath:GraphicsPath;
		private var _endPath:GraphicsPath;
		private var _drawing:Vector.<IGraphicsData>;
		private var _allPoints:Vector.<Point>;
		private var segColor:Number = 0x0000ff;
		protected var _includeSegmentBoundries:Boolean;

        private const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

		private var _minX:Number;
		private var _minY:Number;
		private var _maxX:Number;
		private var _maxY:Number;
		
		public function CubicBezier(startPoint:Point,endPoint:Point,controlPoints:Vector.<Point>){

			super(startPoint,endPoint,controlPoints);
			this._segments = new Vector.<BezierSegment>();	
			_posControlPoints = new Vector.<Point>();
			_negControlPoints = new Vector.<Point>();
			_segments = new Vector.<BezierSegment>();
			_segmentPaths = new Vector.<GraphicsPath>();
			_drawing = new Vector.<IGraphicsData>();
			this._includeSegmentBoundries = false;
			
			init();
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
				
				if(includeSegmentBoundries){
					addSegmentBoundriesToDrawing(_drawing);
				}
			}

			return _drawing;
		}
		
		public function getControlLineData(p:Point):Vector.<IGraphicsData> {
			var controlLine:Vector.<IGraphicsData> = new Vector.<IGraphicsData>();
			var gPath:GraphicsPath = new GraphicsPath(new Vector.<int>(),new Vector.<Number>);
			var pIndex:int = _allPoints.indexOf(p);
			
			if(pIndex > -1){
				gPath.commands.push(GraphicsPathCommand.MOVE_TO);
				gPath.data.push(_negControlPoints[pIndex].x,_negControlPoints[pIndex].y);
				
				gPath.commands.push(GraphicsPathCommand.LINE_TO);
				gPath.data.push(_posControlPoints[pIndex].x,_posControlPoints[pIndex].y);
			}
			
			controlLine.push(gPath);
			
			return controlLine;
		}
		
		private function addSegmentBoundriesToDrawing(drawing:Vector.<IGraphicsData>):void {
			var stroke1:GraphicsStroke = new GraphicsStroke(.5);
			stroke1.fill = new GraphicsSolidFill(0x0000ff);
			
			var stroke2:GraphicsStroke = new GraphicsStroke(.5);
			stroke2.fill = new GraphicsSolidFill(0x00ff00);
			
			var myStroke:GraphicsStroke = stroke2;
			
			var s:BezierSegment;
			var sb:Rectangle;
			var i:int;
			for(i=0;i<_segments.length;i++){
				
				if(myStroke == stroke2){
					myStroke = stroke1;
				} else {
					myStroke = stroke2;
				}
				drawing.push(myStroke);
				
				s = _segments[i];
				sb = s.getBoundingRectangle();
				var drawW:Number = (sb.x + sb.width);
				var drawH:Number = (sb.y + sb.height);
				var box:GraphicsPath = new GraphicsPath(new Vector.<int>(),new Vector.<Number>());
				box.commands.push(GraphicsPathCommand.MOVE_TO);
				box.data.push(sb.x,sb.y);
				
				box.commands.push(GraphicsPathCommand.LINE_TO);
				box.data.push(drawW,sb.y);
				box.commands.push(GraphicsPathCommand.LINE_TO);
				box.data.push(drawW,drawH);
				box.commands.push(GraphicsPathCommand.LINE_TO);
				box.data.push(sb.x,drawH);
				box.commands.push(GraphicsPathCommand.LINE_TO);
				box.data.push(sb.x,sb.y);
				drawing.push(box);
			}	
		}
		
		private function init():void {			
			if(!_startPath){
				_startPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
				_startPath.commands.push(GraphicsPathCommand.MOVE_TO);
				_startPath.data.push(_startPoint.x, _startPoint.y);
			}
			
			if(!_endPath) {
				_endPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
				_endPath.commands.push(GraphicsPathCommand.LINE_TO);
				_endPath.data.push(_endPoint.x, _endPoint.y);
			}
			
			var z:Number = 1;
			if(_startPoint == null || _endPoint == null){ return;}
			
			// set the initial control points to the actual points (basically no initial control point)
			_posControlPoints[0] = new Point(_startPoint.x, _startPoint.y);
			_negControlPoints[0] = new Point(_startPoint.x, _startPoint.y);
			
			calculatePositiveAndNegativeControls(0,_controlPoints.length);

			// set the final control points to the actual points (basically no final control point)
			_posControlPoints.push(new Point(_endPoint.x, _endPoint.y));
			_negControlPoints.push(new Point(_endPoint.x, _endPoint.y));
			
			// create our all points vector
			var _tmp:Vector.<Point> = new Vector.<Point>();
			_tmp.push(_startPoint);
			_allPoints = _tmp.concat(_controlPoints);
			_allPoints.push(_endPoint);
			
			initSegments();
			calculateMinimumBounds();

		}
		
		private function initSegments():void {
			var eps:Number = 0.05;
			var t:Number;
			var i:int;
			var end:int = (_allPoints.length - 1);

			for(i=0; i < end; ++i) {
				var gPath:GraphicsPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
				var seg:BezierSegment = new BezierSegment(_allPoints[i], _posControlPoints[i], _negControlPoints[i+1], _allPoints[i+1]);

				for(t=eps; t < 1.0; t += eps) {
					var p:Point = seg.getValue(t);
					gPath.commands.push(GraphicsPathCommand.LINE_TO);
					gPath.data.push(p.x, p.y);
				}
				gPath.commands.push(GraphicsPathCommand.LINE_TO);
				gPath.data.push(_allPoints[i+1].x, _allPoints[i+1].y);
				_segments.push(seg);
				_segmentPaths.push(gPath);
			}
		}
		
		private function updateBezierSegments(start:int,length:int):void {
			/* for each pair of points i and i+1 the four points used will be 
			   {points[i], controlPoint[i], negControlPoint[i+1], points[i+1]} */
			var eps:Number = 0.05;
			var t:Number;
			var i:int;
			for(i=start; i < length; ++i) {
				var seg:BezierSegment = _segments[i];
				var gPath:GraphicsPath = _segmentPaths[i];
				
				seg.updatePoints(_allPoints[i], _posControlPoints[i], _negControlPoints[i+1], _allPoints[i+1]);

				gPath.commands = new Vector.<int>();
				gPath.data = new Vector.<Number>();
				for(t=eps; t < 1.0; t += eps) {
					var p:Point = seg.getValue(t);
					gPath.commands.push(GraphicsPathCommand.LINE_TO);
					gPath.data.push(p.x,p.y);
				}
				gPath.commands.push(GraphicsPathCommand.LINE_TO);
				gPath.data.push(_allPoints[i+1].x,_allPoints[i+1].y);
			}
			calculateMinimumBounds();
		}
		
		private function calculatePositiveAndNegativeControls(startIndex:int,length:int):void {
			var z:Number = 1;
			var i:Number;
			var prevX:Number;
			var prevY:Number;
			var nextX:Number;
			var nextY:Number;
			for(i=startIndex; i<length; i++) {
				if(i == 0){
					prevX = _startPoint.x;
					prevY = _startPoint.y;
				} else {
					prevX = _controlPoints[i-1].x;
					prevY = _controlPoints[i-1].y;
				}
				
				if(i == (_controlPoints.length - 1)){
					nextX = _endPoint.x;
					nextY = _endPoint.y;
				} else {
					nextX = _controlPoints[i+1].x;
					nextY = _controlPoints[i+1].y;
				}
				/* use middle of parabola */
				var dx:Number = 0.25 * (nextX - prevX);
				var dy:Number = 0.25 * (nextY - prevY);
				_posControlPoints[i+1] = new Point(_controlPoints[i].x + dx, _controlPoints[i].y + dy);
				_negControlPoints[i+1] = new Point(_controlPoints[i].x - dx, _controlPoints[i].y - dy);
			}
			if(i == _controlPoints.length){
				// set the final control points to the actual points (basically no final control point)
				_posControlPoints[i+1] = new Point(_endPoint.x, _endPoint.y);
				_negControlPoints[i+1] = new Point(_endPoint.x, _endPoint.y);
			}
		}
		
		private function addBezierSegmentForNewControlPoint():void {
			//splice in our new pos/neg controls
			//we assume we already have at least the controls for start and end
			_posControlPoints.splice((_posControlPoints.length - 1),0,new Point(0,0));
			_negControlPoints.splice((_negControlPoints.length - 1),0,new Point(0,0));
			
			//add an empty segment for new point
			var gPath:GraphicsPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
			var seg:BezierSegment = new BezierSegment(new Point(0,0), new Point(0,0), new Point(0,0), new Point(0,0));
			_segments.push(seg);
			_segmentPaths.push(gPath);
			
			//figure out our start point for pos/neg calculations
			var start:int = 0;
			if(_controlPoints.length > 1){
				start = (_controlPoints.length - 2);
			}
			
			calculatePositiveAndNegativeControls(start,_controlPoints.length);
			updateBezierSegments((_controlPoints.length - 1),(_allPoints.length - 1));
		}
		
		private function addBezierSegmentForInsertedControlPoint(p:Point,i:int):void {
			//splice in our new pos/neg controls
			//we assume we already have at least the controls for start and end
			_posControlPoints.splice(i,0,p);
			_negControlPoints.splice(i,0,p);
			
			//add an empty segment for new point
			var gPath:GraphicsPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
			var seg:BezierSegment = new BezierSegment(new Point(0,0), new Point(0,0), new Point(0,0), new Point(0,0));
			_segments.splice(i,0,seg);
			_segmentPaths.splice(i,0,gPath);
			
			//figure out our start point for pos/neg calculations
			var start:int = i-1;
			var end:int = i+2;
			if(start < 0){
				start = 0;
			}
			if(end>_controlPoints.length){
				end = _controlPoints.length;
			}
			
			calculatePositiveAndNegativeControls(start,end);
			updateBezierSegments(i-1,end+1);
		}
		
		private function removeBezierSegmentForRemovedControlPoint(i:int):void {
			//splice in our new pos/neg controls
			//we assume we already have at least the controls for start and end
			_posControlPoints.splice(i,1);
			_negControlPoints.splice(i,1);
			_segments.splice(i,1);
			_segmentPaths.splice(i,1);
			
			//figure out our start point for pos/neg calculations
			var start:int = i-1;
			var end:int = i+2;
			if(start < 0){
				start = 0;
			}
			if(end>_controlPoints.length){
				end = _controlPoints.length;
			}
			
			calculatePositiveAndNegativeControls(start,end);
			updateBezierSegments(i-1,end+1);
		}

		
		public function closestPointToPoint(p:Point):Point {
			var returnPoint:Point;
			// distances from start and end points
			var deltaX:Number = _startPoint.x-p.x;
			var deltaY:Number = _startPoint.y-p.y;
			var distanceFromStart:Number = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
			
			deltaX = _endPoint.x-p.x;
			deltaY = _endPoint.y-p.y;
			var distanceFromEnd:Number = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
			
			var closestControl:Point;
			//if we don't have control points, just return the start or end
			if(_controlPoints.length < 1){
				if(distanceFromStart < distanceFromEnd){
					returnPoint = _startPoint;
				} else {
					returnPoint = _endPoint;
				}
			} else {
				//figure out which control point it's closest to
				//take a shortcut by starting at the closest end of the line
				if(distanceFromStart < distanceFromEnd){
					//start from the start
                    /*
					var controlPoint:Point;
					var controlDistance:Number;
					var prevDistance:Number = distanceFromStart;
					closestControl = _startPoint;
					for(var i:int=0;i<_controlPoints.length;i++){
						controlPoint = _controlPoints[i];
						deltaX = controlPoint.x-p.x;
						deltaY = controlPoint.y-p.y;
						controlDistance = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
						if(controlDistance < prevDistance){
							closestControl = controlPoint;
							prevDistance = controlDistance;
						} else {
							break;
						}
					}*/
                    var closestPoint:SegmentPoint = linearSegmentPointSearch(p,_segments.concat());
                    returnPoint = closestPoint.point;
				} else {
					returnPoint = closestFromEnd(p);
				}
			}
			return returnPoint;
		}
		
		//gets the closest point in the last segment for the given point
		public function closestFromEnd(p:Point):Point {
			var reversedSegments:Vector.<BezierSegment> = _segments.concat();
			reversedSegments.reverse();
			var closestPoint:SegmentPoint = linearSegmentPointSearch(p,reversedSegments);
			
			return closestPoint.point;
		}
		
		private function linearSegmentPointSearch(origin:Point,segmentsToTest:Vector.<BezierSegment>):SegmentPoint {
			var i:int;
			var segment:BezierSegment;
			var distance:Number;
			var prevDistance:Number=Number.MAX_VALUE;
			var closestPoint:SegmentPoint = new SegmentPoint();
			for(i=0;i<segmentsToTest.length;i++){
				segment = segmentsToTest[i];
				distance = Point.distance(segment.D,origin);
				if(prevDistance < distance){
					//distance to next control is farther, so we don't need to process anymore segments
					break;
				} else {
					var closestOnSegment:SegmentPoint = closestPointOnSegment(origin,segment);
					distance = Point.distance(closestOnSegment.point,origin);
					if(distance < prevDistance){
						closestPoint.point = closestOnSegment.point;
						closestPoint.segment = segment;
						closestPoint.segmentIndex = i;
						closestPoint.t = closestOnSegment.t;
						prevDistance = distance;
					} else {
						break;
					}
				}
			}
			return closestPoint;
		}
		
		private function boundedSegmentPointSearch(origin:Point,segmentsToTest:Vector.<BezierSegment>):SegmentPoint {
			var i:int;
			var segment:BezierSegment;
			var segBounds:Rectangle;
			var distance:Number;
			var prevDistance:Number=Number.MAX_VALUE;
			var closestPoint:SegmentPoint = new SegmentPoint();
			for(i=0;i<segmentsToTest.length;i++){
				segment = segmentsToTest[i];
				segBounds = segment.getBoundingRectangle();
				
				if(segBounds.containsPoint(origin)){

					var closestOnSegment:SegmentPoint = closestPointOnSegment(origin,segment);
					distance = Point.distance(closestOnSegment.point,origin);
					if(distance < prevDistance){
						closestPoint.point = closestOnSegment.point;
						closestPoint.segment = segment;
						closestPoint.segmentIndex = i;
						closestPoint.t = closestOnSegment.t;
						prevDistance = distance;
					}
				}
			}
			return closestPoint;
		}
		
		private function closestPointOnSegment(origin:Point,segment:BezierSegment):SegmentPoint {
			var eps:Number = 0.05;
			var t:Number;
			var distance:Number;
			var prevDistance:Number=Number.MAX_VALUE;
			var closestPoint:SegmentPoint = new SegmentPoint();
			
			for(t=1; t > 0; t -= eps) {
				var tPoint:Point = segment.getValue(t);
				distance = Point.distance(tPoint,origin);
				if(distance < prevDistance){
					closestPoint.point = tPoint;
					closestPoint.segment = segment;
					closestPoint.t = t;
					prevDistance = distance;
				} else {
					break;
				}
			}
			
			return closestPoint;
		}
		
		public function getPointFromEnd(t:Number):Point {
			var seg:BezierSegment = _segments[_segments.length-1];
			return seg.getValue(t);			
		}
		
		override public function moveEndPoint(newX:Number, newY:Number):void {
			super.moveEndPoint(newX,newY);
			_endPath.data[0] = newX;
			_endPath.data[1] = newY;
			if(_controlPoints.length > 0){
				var start:int = 0;
				if(_controlPoints.length > 1){
					start = (_controlPoints.length - 2);
				}
				//calculatePositiveAndNegativeControls(start,_controlPoints.length);
				updateBezierSegments((_controlPoints.length - 1),(_allPoints.length - 1));
			}
		}
		
		override public function moveEndPointPreview(newX:Number, newY:Number):void {
			super.moveEndPointPreview(newX,newY);
			_endPath.data[0] = newX;
			_endPath.data[1] = newY;
			if(_controlPoints.length > 0){
				var start:int = 0;
				if(_controlPoints.length > 1){
					start = (_controlPoints.length - 2);
				}
				calculatePositiveAndNegativeControls(start,_controlPoints.length);
				updateBezierSegments((_controlPoints.length - 1),(_allPoints.length - 1));
			}
		}
		
		override public function moveStartPoint(newX:Number, newY:Number):void {
			super.moveStartPoint(newX,newY);
			_startPath.data[0] = newX;
			_startPath.data[1] = newY;
			if(_controlPoints.length > 0){
				var end:int = 1;
				if(_controlPoints.length > 1){
					end = 2;
				}
				//calculatePositiveAndNegativeControls(0,end);
				updateBezierSegments(0,end);
			}
		}
		
		override public function moveControlPoint(p:Point, newX:Number, newY:Number):void {
			if(p.x != newX || p.y != newY){
				updateLineForControlPoint(p,newX,newY);
			}
			
		}
		
		private function updateLineForControlPoint(p:Point, newX:Number, newY:Number):void {
			var dx:Number = (newX - p.x);
			var dy:Number = (newY - p.y);
			
			p.x = newX;
			p.y = newY;
			
			var myIndex:int = _allPoints.indexOf(p);
			var calcStart:int = (myIndex - 1);
			var calcEnd:int = myIndex + 1;
			if(calcStart < 0){
				calcStart = 0;
			}
			
			if(calcEnd > _controlPoints.length){
				calcEnd = _controlPoints.length;
			}
			
			var end:int = myIndex + 1;
			if(end > (_allPoints.length - 1)){
				end = (_allPoints.length - 1);
			}
			
			//calculatePositiveAndNegativeControls(calcStart,calcEnd);
			
			//var dx:Number = 0.25 * (_allPoints[myIndex+1].x - _allPoints[myIndex-1].x);
        	//var dy:Number = 0.25 * (_allPoints[myIndex+1].y - _allPoints[myIndex-1].y);
			
        	_negControlPoints[myIndex].x = (_negControlPoints[myIndex].x + dx);
        	_negControlPoints[myIndex].y = (_negControlPoints[myIndex].y + dy);
        	_posControlPoints[myIndex].x = (_posControlPoints[myIndex].x + dx);
        	_posControlPoints[myIndex].y = (_posControlPoints[myIndex].y + dy);
        	
			updateBezierSegments((myIndex - 1),end);
		}
		
		private function updateSegmentsForControlPoint(p:Point):void {
			var myIndex:int = _allPoints.indexOf(p);
			
			var end:int = myIndex + 1;
			if(end > (_allPoints.length - 1)){
				end = (_allPoints.length - 1);
			}

			updateBezierSegments((myIndex - 1),end);
		}
		
		override public function addControlPoint(p:Point):void {
			super.addControlPoint(p);
			
			//add to all points
			_allPoints.splice((_allPoints.length - 1),0,p);
			addBezierSegmentForNewControlPoint();	

		}
		
		override public function insertControlPointAt(p:Point,i:int):void {
			super.insertControlPointAt(p,i);
			_allPoints.splice(i+1,0,p);
			addBezierSegmentForInsertedControlPoint(p,(i+1));
		}
		
		override public function removeControlPointAt(i:int):void {
			super.removeControlPointAt(i);
			_allPoints.splice(i+1,1);
			removeBezierSegmentForRemovedControlPoint((i+1));
		}
		
		public function getPositiveControllerForPoint(p:Point):Point { 
			var index:int = _allPoints.indexOf(p);
			
			return getPositiveControllerAt(index);
		}
		
		public function getNegativeControllerForPoint(p:Point):Point { 
			var index:int = _allPoints.indexOf(p);
			
			return getNegativeControllerAt(index);
		}
		
		public function getPositiveControllerAt(i:int):Point {
			if(i<0){
				return null;
			}
			
			return _posControlPoints[i];
		}
		
		public function getNegativeControllerAt(i:int):Point {
			if(i<0){
				return null;
			}
			return _negControlPoints[i];
		}
		
		public function movePositiveController(p:Point,newX:Number,newY:Number):void {
			var index:int = _allPoints.indexOf(p);
			
			movePositiveControllerAt(index,newX,newY);
		}
		
		public function moveNegativeController(p:Point,newX:Number,newY:Number):void {
			var index:int = _allPoints.indexOf(p);
			
			moveNegativeControllerAt(index,newX,newY);
		}
		
		public function movePositiveControllerAt(i:int,newX:Number,newY:Number):void {
			if(i<0){
				return;
			}
			var posPoint:Point = _posControlPoints[i];
			var negPoint:Point = _negControlPoints[i];
			
			if(posPoint && negPoint && (!(newX == posPoint.x) || !(newY == posPoint.y))){
				var diffX:Number = (newX - posPoint.x);
				var diffY:Number = (newY - posPoint.y);
				posPoint.x = newX;
				posPoint.y = newY;
				
				negPoint.x = (negPoint.x - diffX);
				negPoint.y = (negPoint.y - diffY);
				
				var controlPoint:Point = _allPoints[i];
				updateSegmentsForControlPoint(controlPoint);
			}
			
		}
		
		public function moveNegativeControllerAt(i:int,newX:Number,newY:Number):void {
			if(i<0){
				return;
			}
			var posPoint:Point = _posControlPoints[i];
			var negPoint:Point = _negControlPoints[i];
			
			if(posPoint && negPoint && (!(newX == negPoint.x) || !(newY == negPoint.y))){
				var diffX:Number = (newX - negPoint.x);
				var diffY:Number = (newY - negPoint.y);
				negPoint.x = newX;
				negPoint.y = newY;
				
				posPoint.x = (posPoint.x - diffX);
				posPoint.y = (posPoint.y - diffY);
				
				var controlPoint:Point = _allPoints[i];
				updateSegmentsForControlPoint(controlPoint);
			}
		}
		
		override public function getBoundingRectangle():Rectangle {
			return new Rectangle(_minX,_minY,_maxX-_minX,_maxY-_minY);
		}
		
		override public function findInsertionIndexForPoint(p:Point):int {
			var i:int;
			var segmentPoint:SegmentPoint = boundedSegmentPointSearch(p,_segments);
				
			return segmentPoint.segmentIndex;
		}
		
				
		public function get includeSegmentBoundries():Boolean {
			return _includeSegmentBoundries;
		}
		public function set includeSegmentBoundries(b:Boolean):void {
			this._includeSegmentBoundries = b;
		}
		
		private function calculateMinimumBounds():void {
			_minX = Math.min(_startPoint.x,_endPoint.x);
			_minY = Math.min(_startPoint.y,_endPoint.y);
				
			_maxX = Math.max(_startPoint.x,_endPoint.x);
			_maxY = Math.max(_startPoint.y,_endPoint.y);
			
			if(_segments.length > 0){
				var seg:BezierSegment;
				var segBounds:Rectangle;
				var i:int;
				for(i=0;i<_segments.length;i++){
					seg = _segments[i];
					segBounds = seg.getBoundingRectangle();
					
					_minX = Math.min(_minX,segBounds.x);
					_minY = Math.min(_minY,segBounds.y);
					_maxX = Math.max(_maxX,(segBounds.x+segBounds.width));
					_maxY = Math.max(_maxY,(segBounds.y+segBounds.height));
				}
			}
		}
		
	}
}
import com.sysbliss.diagram.geom.BezierSegment;
import flash.geom.Point;

class SegmentPoint {
	public var segment:BezierSegment = null;
	public var segmentIndex:int = -1;
	public var point:Point = null;
	public var t:Number = -1;

    public function SegmentPoint() {

    }

	public function toString():String {
		var s:String = "SegmentPoint[\n";
		s = s + "segmentIndex: " + segmentIndex + "\n";
		s = s + "point: " + point + "\n";
		s = s + "t: " + t + "\n]\n";
		
		return s;
	}
}
