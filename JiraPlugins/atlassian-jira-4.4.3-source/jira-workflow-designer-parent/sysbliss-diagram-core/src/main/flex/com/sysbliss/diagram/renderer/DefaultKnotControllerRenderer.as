package com.sysbliss.diagram.renderer
{
	import flash.display.Graphics;
	import flash.geom.Matrix;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	public class DefaultKnotControllerRenderer extends AbstractKnotRenderer
	{
		public function DefaultKnotControllerRenderer()
		{
			super();
		}
		
		override public function draw(g:Graphics,selected:Boolean,rotation:Number=0):void
		{
			var fill:Number = 0x000000;
			if(selected){
				//fill = 0xcc0000;
			}
			
			var halfW:Number = (getBounds().width/2);
			
			var gutterP1:Point = new Point(-getBounds().width,0);
            var gutterP2:Point = new Point(getBounds().width, 0);
            var gutterP3:Point = new Point(getBounds().width,getBounds().height*2);
            var gutterP4:Point = new Point(-getBounds().width,getBounds().height*2);
            
			var shapeP1:Point = new Point(-halfW,0);
            var shapeP2:Point = new Point(halfW, 0);
            var shapeP3:Point = new Point(halfW,getBounds().height);
            var shapeP4:Point = new Point(-halfW,getBounds().height);
            
			g.clear();
			
			if(rotation != 0){
				var gutterMatrix:Matrix = new Matrix();
				gutterMatrix.translate(-getBounds().width,-getBounds().width);
				gutterMatrix.rotate(rotation);
				gutterP1 = gutterMatrix.transformPoint(gutterP1);
            	gutterP2 = gutterMatrix.transformPoint(gutterP2);
            	gutterP3 = gutterMatrix.transformPoint(gutterP3);
            	gutterP4 = gutterMatrix.transformPoint(gutterP4);
            	
				var shapeMatrix:Matrix = new Matrix();
				shapeMatrix.translate(-halfW,-halfW);
				shapeMatrix.rotate(rotation);
				shapeP1 = shapeMatrix.transformPoint(shapeP1);
            	shapeP2 = shapeMatrix.transformPoint(shapeP2);
            	shapeP3 = shapeMatrix.transformPoint(shapeP3);
            	shapeP4 = shapeMatrix.transformPoint(shapeP4);
			}
			
			//draw a gutter
			g.lineStyle(1,0x000000,0);
			g.beginFill(fill,0);
			g.moveTo(gutterP1.x,gutterP1.y);
			g.lineTo(gutterP2.x,gutterP2.y);
			g.lineTo(gutterP3.x,gutterP3.y);
			g.lineTo(gutterP4.x,gutterP4.y);
			g.lineTo(gutterP1.x,gutterP1.y);
			g.endFill();
			
			//draw handle
			g.lineStyle(1,0x000000);
			g.beginFill(fill,1);
			g.moveTo(shapeP1.x,shapeP1.y);
			g.lineTo(shapeP2.x,shapeP2.y);
			g.lineTo(shapeP3.x,shapeP3.y);
			g.lineTo(shapeP4.x,shapeP4.y);
			g.lineTo(shapeP1.x,shapeP1.y);
			g.endFill();

			
		}
		
		override public function getBounds():Rectangle {
			return new Rectangle(0,0,5,5);
		}
		
	}
}