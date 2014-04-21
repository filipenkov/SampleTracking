package com.sysbliss.diagram.renderer
{
	import com.sysbliss.diagram.geom.CubicBezier;
	import com.sysbliss.diagram.geom.Line;
	import com.sysbliss.diagram.geom.SegmentedLine;
	import com.sysbliss.diagram.ui.UIEdge;
	import com.sysbliss.diagram.ui.UINode;
	
	import flash.display.DisplayObject;
	import flash.display.Graphics;
	import flash.display.GraphicsEndFill;
	import flash.display.GraphicsPath;
	import flash.display.GraphicsSolidFill;
	import flash.display.GraphicsStroke;
	import flash.display.IGraphicsData;
	import flash.geom.Matrix;
	import flash.geom.Point;

	public class DirectedEdgeRenderer extends DefaultEdgeRenderer
	{
		protected var arrowBaseSize:Number = 7;
		protected var arrowHeadLength:Number = 14;
		protected var arrowColor:Number = 0xbbbbbb;
        public var eraseUnderArrow:Boolean = true;
		
		public function DirectedEdgeRenderer()
		{
			super();
		}
		
		override public function draw(g:Graphics,line:Line,uiEdge:UIEdge):void {
			super.draw(g,line,uiEdge);
			drawArrow(g,line,uiEdge,false);
		}
		
		override public function drawSelected(g:Graphics,line:Line,uiEdge:UIEdge,selectedControlPoint:Point=null):void {
			super.drawSelected(g,line,uiEdge,selectedControlPoint);
			drawArrow(g,line,uiEdge,true);
		}
		
		protected function drawArrow(g:Graphics,line:Line,uiEdge:UIEdge,selected:Boolean):void {
			
			var lastPoint:Point = line.endPoint;
			var half:Number = (arrowBaseSize/2);
			
			var edgeAngle:Number = getEdgeAngle(line,lastPoint);
			var _bezier:CubicBezier = line as CubicBezier;
			
			//get intersection
			var i:int;
			var intersect:Point = new Point(line.endPoint.x,line.endPoint.y);
			if(uiEdge.edge && uiEdge.edge.endNode){
				var endNode:UINode = uiEdge.diagram.getUINode(uiEdge.edge.endNode.id);
				
				for(i=1;true;i++){
					intersect.x = line.endPoint.x-i*Math.cos(edgeAngle);
					intersect.y = line.endPoint.y-i*Math.sin(edgeAngle);
					if((line is CubicBezier)){
						//intersect = _bezier.closestFromEnd(intersect);	
					}
					
					if(!endNode.getRect(DisplayObject(uiEdge)).containsPoint(intersect)){
						lastPoint = intersect;
						/* g.lineStyle(1,0x00ff00);
						g.beginFill(0x00ff00);
						g.drawCircle(lastPoint.x,lastPoint.y,10);
						g.endFill(); */
						break;
					}
				}
			}
			if((line is CubicBezier)){
				edgeAngle = getEdgeAngle(line,lastPoint);
			}
			
			//setup the arrow pointing east
			var p1:Point = new Point(-arrowHeadLength, -half);
            var p2:Point = new Point(0, 0);
            var p3:Point = new Point(-arrowHeadLength, half);
            var p4:Point = new Point(-arrowHeadLength, -half);
                        
            var matrix:Matrix = new Matrix();
            matrix.rotate(edgeAngle);
            matrix.translate(lastPoint.x, lastPoint.y);
            
            p1 = matrix.transformPoint(p1);
            p2 = matrix.transformPoint(p2);
            p3 = matrix.transformPoint(p3);
            p4 = matrix.transformPoint(p4);
						
			var drawing:Vector.<IGraphicsData> = new Vector.<IGraphicsData>();

            var myColor:Number = lineColor;
            if(selected) {
                myColor = HIGHLIGHT_COLOR;
            }
			var fill:GraphicsSolidFill = new GraphicsSolidFill(myColor);

			var stroke:GraphicsStroke = new GraphicsStroke(1);			
			stroke.fill = fill;
						
			var gdata:GraphicsPath = new GraphicsPath(Vector.<int>([1,2,2,2]), Vector.<Number>([p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y]));
			
			//create white line to cover existing line under arrow
			if(eraseUnderArrow){
				var eraserP1:Point = new Point(-arrowHeadLength,0);
            	var eraserP2:Point = new Point(5,0);
				eraserP1 = matrix.transformPoint(eraserP1);
				eraserP2 = matrix.transformPoint(eraserP2);
				
				var eraserFill:GraphicsSolidFill = new GraphicsSolidFill(0xffffff);
				var eraserStroke:GraphicsStroke = new GraphicsStroke(10);
				eraserStroke.fill = eraserFill;
				var eraserData:GraphicsPath = new GraphicsPath(Vector.<int>([1,2]), Vector.<Number>([eraserP1.x,eraserP1.y, eraserP2.x,eraserP2.y]));
				
				drawing.push(eraserStroke,eraserData,new GraphicsEndFill());
			} else {
				eraseUnderArrow = true;
			}
			
			drawing.push(stroke,fill,gdata,new GraphicsEndFill());
			
			g.drawGraphicsData(drawing);

		}

		private function getEdgeAngle(line:Line,endPoint:Point):Number {
			var regPoint:Point = line.startPoint;
			var segLine:SegmentedLine = line as SegmentedLine;
			if(segLine && segLine.controlPoints.length > 0){
				regPoint = segLine.controlPoints[segLine.controlPoints.length-1];
			}
			
			var midPoint:Point = new Point(-arrowHeadLength,0);
			
			var deltaX:Number = Math.round((endPoint.x - regPoint.x));
			var deltaY:Number = Math.round((endPoint.y - regPoint.y));
			var edgeAngle:Number = Math.atan2(deltaY,deltaX);
			
			if((line is CubicBezier) && segLine.controlPoints.length > 0){
				var _bezier:CubicBezier = line as CubicBezier;
				var closest:Point = null;
				var tPoint:Point = _bezier.getPointFromEnd(1-(.05*2));

				deltaX = Math.round((line.endPoint.x - tPoint.x));
				deltaY = Math.round((line.endPoint.y - tPoint.y));
				edgeAngle = Math.atan2(deltaY,deltaX);
				
				var matrix:Matrix = new Matrix();
				matrix.rotate(edgeAngle);
        		matrix.translate(endPoint.x, endPoint.y);
        		
        		midPoint = matrix.transformPoint(midPoint);
        		closest = _bezier.closestFromEnd(midPoint);
        		
            	deltaX = Math.round((endPoint.x - closest.x));
				deltaY = Math.round((endPoint.y - closest.y));
				edgeAngle = Math.atan2(deltaY,deltaX);

			}
			
			return edgeAngle;
		}
		
	}
}
