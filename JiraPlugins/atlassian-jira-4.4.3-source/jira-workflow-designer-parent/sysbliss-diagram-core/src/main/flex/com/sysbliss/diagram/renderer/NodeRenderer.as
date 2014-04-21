package com.sysbliss.diagram.renderer
{
	import com.sysbliss.diagram.data.Node;
	
	import mx.core.IUIComponent;
	
	public interface NodeRenderer extends IUIComponent,DiagramObjectRenderer
	{
		function set node(value:Node):void;
		function get node():Node;
	}
}