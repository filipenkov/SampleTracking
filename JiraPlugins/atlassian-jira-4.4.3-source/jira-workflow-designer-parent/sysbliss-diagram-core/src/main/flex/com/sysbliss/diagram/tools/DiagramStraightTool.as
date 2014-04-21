package com.sysbliss.diagram.tools
{
	public class DiagramStraightTool extends DiagramLinkTool implements DiagramTool
	{
		public function DiagramStraightTool()
		{
			super();
			_name = "straight";
			_toolTip = niceResourceManager.getString('json','workflow.designer.straight.line');
			_cursorDisplay = DiagramToolImpl.CURSOR_ATTACH;
		}
		
		override public function get cursor():Class {
			return null;
		}
	}
}