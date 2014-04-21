package com.sysbliss.diagram.renderer
{
	import com.sysbliss.diagram.geom.Line;
	import com.sysbliss.diagram.ui.UIEdge;
	import com.sysbliss.util.AbstractClassEnforcer;
	
	import flash.display.Graphics;
	import flash.geom.Point;

	public class AbstractEdgeRenderer implements EdgeRenderer
	{

        public static const DEFAULT_COLOR:Number = 0xbbbbbb;
        public static const HIGHLIGHT_COLOR:Number = 0xFF0000;

        protected var _lineColor:Number;
		
		public function AbstractEdgeRenderer()
		{
			AbstractClassEnforcer.enforceConstructor(this,AbstractEdgeRenderer);
            _lineColor = DEFAULT_COLOR;
		}
		
		public function draw(g:Graphics,line:Line,uiEdge:UIEdge):void{
			AbstractClassEnforcer.enforceMethod("draw");
		}
		
		public function drawSelected(g:Graphics,line:Line,uiEdge:UIEdge,selectedControlPoint:Point=null):void{
			AbstractClassEnforcer.enforceMethod("drawSelected");
		}


        public function set lineColor(n:Number):void
        {
            this._lineColor = n;
        }

        public function get lineColor():Number
        {
            return _lineColor;
        }
    }
}