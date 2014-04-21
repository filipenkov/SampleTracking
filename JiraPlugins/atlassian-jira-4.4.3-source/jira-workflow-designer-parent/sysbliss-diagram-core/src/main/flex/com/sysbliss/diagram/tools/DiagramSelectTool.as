package com.sysbliss.diagram.tools
{
	public class DiagramSelectTool extends DiagramToolImpl implements DiagramTool
	{
		public function DiagramSelectTool()
		{
			super();
			_name = "selectTool";
			_toolTip = niceResourceManager.getString('json','workflow.designer.select.tool');
			_cursorDisplay = DiagramToolImpl.CURSOR_REPLACE;
		}
		
	}
}