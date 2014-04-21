package com.sysbliss.diagram.tools
{
	public class DiagramLinkTool extends DiagramToolImpl implements DiagramTool
	{
		public function DiagramLinkTool()
		{
			super();
			_name = "linkTool";
			_toolTip = niceResourceManager.getString('json','workflow.designer.link.tool');
			_cursorDisplay = DiagramToolImpl.CURSOR_ATTACH;
		}
		
	}
}