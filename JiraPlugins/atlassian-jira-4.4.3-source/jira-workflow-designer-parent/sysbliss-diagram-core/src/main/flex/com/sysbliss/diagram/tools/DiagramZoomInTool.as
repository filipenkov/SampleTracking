package com.sysbliss.diagram.tools
{
	public class DiagramZoomInTool extends DiagramToolImpl implements DiagramTool
	{
		public function DiagramZoomInTool()
		{
			super();
			_name = "zoomInTool";
			_toolTip = niceResourceManager.getString('json','workflow.designer.zoom.in');
			_cursorDisplay = DiagramToolImpl.CURSOR_REPLACE;
		}
		
	}
}