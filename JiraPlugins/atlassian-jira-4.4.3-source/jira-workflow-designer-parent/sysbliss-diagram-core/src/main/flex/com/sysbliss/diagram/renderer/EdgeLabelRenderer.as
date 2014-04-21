package com.sysbliss.diagram.renderer
{
    import com.sysbliss.diagram.data.Edge;
    import com.sysbliss.diagram.data.EdgeLabel;
	
	import mx.core.IUIComponent;
	
	public interface EdgeLabelRenderer extends IUIComponent,DiagramObjectRenderer
	{
		function set edgeLabel(value:EdgeLabel):void;
		function get edgeLabel():EdgeLabel;

		function set edge(value:Edge):void;
		function get edge():Edge;

        function highlight():void;
        function unhighlight():void;
	}
}