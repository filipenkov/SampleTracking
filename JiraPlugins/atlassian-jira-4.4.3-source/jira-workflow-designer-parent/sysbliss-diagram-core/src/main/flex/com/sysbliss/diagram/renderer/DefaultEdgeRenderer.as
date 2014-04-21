package com.sysbliss.diagram.renderer
{
	
	import com.sysbliss.diagram.geom.CubicBezier;
	import com.sysbliss.diagram.geom.Line;
	import com.sysbliss.diagram.ui.UIEdge;
	
	import flash.display.Graphics;
	import flash.display.IGraphicsData;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	


	public class DefaultEdgeRenderer extends AbstractEdgeRenderer
	{
		
		public function DefaultEdgeRenderer(){
			super();
            _lineColor = 0xbbbbbb;
		}
		
		override public function draw(g:Graphics,line:Line,uiEdge:UIEdge):void {
			var gData:Vector.<IGraphicsData> = line.getGraphicsData();

            g.clear();
            drawBoundingRectangle(g,line);

			drawGutter(g,gData);
			
			g.lineStyle(1,_lineColor,1);
			g.drawGraphicsData(gData);
			

		}
		
		override public function drawSelected(g:Graphics,line:Line,uiEdge:UIEdge,selectedControlPoint:Point=null):void {
			var gData:Vector.<IGraphicsData> = line.getGraphicsData();
            g.clear();
            drawBoundingRectangle(g,line);

			drawGutter(g,gData);
			
			g.lineStyle(1,HIGHLIGHT_COLOR,1);
			g.drawGraphicsData(gData);
			
			//g.lineStyle(1,_lineColor,1);
			//g.drawGraphicsData(gData);
			
			if(selectedControlPoint && (line is CubicBezier)){
				var bezier:CubicBezier = line as CubicBezier;
				g.lineStyle(1,0x000000,1);
				g.drawGraphicsData(bezier.getControlLineData(selectedControlPoint));
			}

		}
		
		protected function drawGutter(g:Graphics,gData:Vector.<IGraphicsData>):void {
			g.lineStyle(10,0x000000,0);
			g.drawGraphicsData(gData);
		}
		
		protected function drawBoundingRectangle(g:Graphics,line:Line):void {
			g.lineStyle(.5,0x0000ff,0);
			var r:Rectangle = line.getBoundingRectangle();
			g.drawRect(r.x,r.y,r.width,r.height);
		}
		
	}
}