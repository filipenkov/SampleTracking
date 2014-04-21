package com.sysbliss.diagram.ui
{
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.renderer.KnotRenderer;
	import com.sysbliss.util.AbstractClassEnforcer;
	
	import flash.geom.Point;

	public class AbstractUIKnot extends InteractiveDiagramObject implements UIKnot
	{
		protected var _uiEdge:UIEdge;
		protected var _point:Point;
		protected var _knotRenderer:KnotRenderer;
		
		public function AbstractUIKnot(d:Diagram)
		{
			super(d);
			AbstractClassEnforcer.enforceConstructor(this,AbstractUIKnot);

			this._isSelected = false;
		}
		
		public function set uiEdge(e:UIEdge):void
		{
			this._uiEdge = e;
		}
		
		public function get uiEdge():UIEdge
		{
			return _uiEdge;
		}
		
		public function get knotRenderer():KnotRenderer
		{
			return _knotRenderer;
		}
		
		public function set knotRenderer(r:KnotRenderer):void {
			this._knotRenderer = r;
		}
		
		public function set point(p:Point):void
		{
			this._point = p;
		}
		
		public function get point():Point
		{
			return _point;
		}
		
		
		
	}
}