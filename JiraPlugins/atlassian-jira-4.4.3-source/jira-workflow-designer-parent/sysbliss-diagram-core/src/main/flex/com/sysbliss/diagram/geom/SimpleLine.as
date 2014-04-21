package com.sysbliss.diagram.geom
{

	import flash.display.GraphicsPath;
	import flash.display.GraphicsPathCommand;
	import flash.display.IGraphicsData;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	public class SimpleLine extends AbstractLine
	{
		private var _graphicsPath:GraphicsPath;
		private var _drawing:Vector.<IGraphicsData>;
		
		public function SimpleLine(startPoint:Point,endPoint:Point)
		{
			super(startPoint,endPoint);
			this._drawing = new Vector.<IGraphicsData>();
			this._graphicsPath = new GraphicsPath(new Vector.<int>(), new Vector.<Number>());
			
			_graphicsPath.commands.push(GraphicsPathCommand.MOVE_TO);
			_graphicsPath.commands.push(GraphicsPathCommand.LINE_TO);
			
			_graphicsPath.data.push(_startPoint.x,_startPoint.y,_endPoint.x,_endPoint.y);
		}
		
		override public function getGraphicsData():Vector.<IGraphicsData>
		{
			_drawing = new Vector.<IGraphicsData>();
			_graphicsPath.data[0] = _startPoint.x;
			_graphicsPath.data[1] = _startPoint.y;
			_graphicsPath.data[2] = _endPoint.x;
			_graphicsPath.data[3] = _endPoint.y;
			
			_drawing.push(_graphicsPath);
			
			return _drawing;
		}
		
		override public function getBoundingRectangle():Rectangle {
			var minX:Number = Math.min(_startPoint.x,_endPoint.x);
			var minY:Number = Math.min(_startPoint.y,_endPoint.y);
			
			var maxX:Number = Math.max(_startPoint.x,_endPoint.x);
			var maxY:Number = Math.max(_startPoint.y,_endPoint.y);
			
			return new Rectangle(minX,minY,maxX-minX,maxY-minY);
		}
		
	}
}