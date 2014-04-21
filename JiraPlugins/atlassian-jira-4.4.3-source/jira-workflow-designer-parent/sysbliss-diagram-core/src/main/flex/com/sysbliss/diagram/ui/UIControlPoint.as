package com.sysbliss.diagram.ui
{
	import com.sysbliss.diagram.renderer.EdgeRenderer;
	import com.sysbliss.diagram.renderer.KnotRenderer;
	import com.sysbliss.diagram.ui.selectable.Selectable;
	
	import flash.geom.Point;
	
	import mx.core.IUIComponent;
	
	public interface UIControlPoint extends UIKnot
	{
		function set isStart(b:Boolean):void;
		function get isStart():Boolean;
		function set isEnd(b:Boolean):void;
		function get isEnd():Boolean;
	}
}