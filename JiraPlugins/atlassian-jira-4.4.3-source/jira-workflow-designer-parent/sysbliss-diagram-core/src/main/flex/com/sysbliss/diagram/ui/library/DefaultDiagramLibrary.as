package com.sysbliss.diagram.ui.library
{
    import com.sysbliss.diagram.renderer.DefaultEdgeLabelRenderer;
    import com.sysbliss.diagram.renderer.DefaultEdgeRenderer;
	import com.sysbliss.diagram.renderer.DefaultNodeRenderer;
	
	public class DefaultDiagramLibrary extends AbstractDiagramLibrary
	{
		public static const DEFAULT:String = "default";
		
		public function DefaultDiagramLibrary()
        {
			super();
			
			addNodeRenderer(DEFAULT, DefaultNodeRenderer);
			addEdgeRenderer(DEFAULT, DefaultEdgeRenderer);
            addEdgeLabelRenderer(DEFAULT, DefaultEdgeLabelRenderer);
		}
	}
}

