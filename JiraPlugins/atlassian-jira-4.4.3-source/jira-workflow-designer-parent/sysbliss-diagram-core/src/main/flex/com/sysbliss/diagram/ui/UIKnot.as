package com.sysbliss.diagram.ui
{
	import com.sysbliss.diagram.renderer.KnotRenderer;
	import com.sysbliss.diagram.ui.selectable.Selectable;
	
	import flash.geom.Point;
	
	import mx.core.IUIComponent;
	
	public interface UIKnot extends IUIComponent,Selectable
	{
		function get uiEdge():UIEdge;
		function set uiEdge(e:UIEdge):void
		function get knotRenderer():KnotRenderer;
		function set knotRenderer(r:KnotRenderer):void;
		function get point():Point;
		function set point(p:Point):void
	}
}