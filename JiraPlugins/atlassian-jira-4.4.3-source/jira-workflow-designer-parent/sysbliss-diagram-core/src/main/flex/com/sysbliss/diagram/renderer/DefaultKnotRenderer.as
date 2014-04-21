package com.sysbliss.diagram.renderer
{
	import flash.display.Graphics;
	import flash.geom.Point;
	import flash.geom.Rectangle;

	public class DefaultKnotRenderer extends AbstractKnotRenderer
	{
		public function DefaultKnotRenderer()
		{
			super();
		}
		
		override public function draw(g:Graphics,selected:Boolean,rotation:Number=0):void
		{
			var fill:Number = 0xcc0000;
			if(selected){
				fill = 0x00ff00;
			}
			g.clear();
			g.lineStyle(1,0x000000);
			g.beginFill(fill,1);
			g.drawCircle(0,0,4);
			g.endFill();
		}
		
		override public function getBounds():Rectangle {
			return new Rectangle(0,0,8,8);
		}
		
	}
}