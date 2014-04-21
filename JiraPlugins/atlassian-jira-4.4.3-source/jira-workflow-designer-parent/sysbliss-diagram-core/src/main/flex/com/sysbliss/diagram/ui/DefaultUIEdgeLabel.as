package com.sysbliss.diagram.ui
{
	import com.sysbliss.diagram.Diagram;
    import com.sysbliss.diagram.renderer.EdgeLabelRenderer;

	public class DefaultUIEdgeLabel extends AbstractUIEdgeLabel
	{
		public function DefaultUIEdgeLabel(diagram:Diagram,renderer:EdgeLabelRenderer)
		{
			super(diagram,renderer);
		}
	}
}