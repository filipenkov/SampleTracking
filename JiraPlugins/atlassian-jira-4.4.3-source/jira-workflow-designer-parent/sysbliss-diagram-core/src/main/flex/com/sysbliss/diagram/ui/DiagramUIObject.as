package com.sysbliss.diagram.ui
{
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.renderer.DiagramObjectRenderer;
	import com.sysbliss.diagram.ui.selectable.Selectable;
	
	import mx.core.IUIComponent;
	
	public interface DiagramUIObject extends IUIComponent,Selectable
	{
		function get renderer():DiagramObjectRenderer;
		function get diagram():Diagram;
	}
}