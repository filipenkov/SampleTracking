package com.sysbliss.diagram.renderer
{
	import com.sysbliss.util.AbstractClassEnforcer;
	
	import flash.display.Graphics;
	import flash.geom.Point;
	import flash.geom.Rectangle;

	public class AbstractKnotRenderer implements KnotRenderer
	{
		
		public function AbstractKnotRenderer()
		{
			AbstractClassEnforcer.enforceConstructor(this,AbstractKnotRenderer);
		}
		
		public function draw(g:Graphics,selected:Boolean,rotation:Number=0):void
		{
			AbstractClassEnforcer.enforceMethod("draw");
		}
		
		public function getBounds():Rectangle {
			AbstractClassEnforcer.enforceMethod("getBounds");
			return null;
		}
		
	}
}