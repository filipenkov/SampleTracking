package com.sysbliss.diagram.tools
{
	public class DiagramZoomOutTool extends DiagramToolImpl implements DiagramTool
	{
		public function DiagramZoomOutTool()
		{
			super();
			_name = "zoomOutTool";
			_toolTip = niceResourceManager.getString('json','workflow.designer.zoom.out');
			_cursorDisplay = DiagramToolImpl.CURSOR_REPLACE;
		}
		
	}
}