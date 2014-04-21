package com.sysbliss.diagram.tools
{
	public class DiagramPolygonTool extends DiagramLinkTool implements DiagramTool
	{
		public function DiagramPolygonTool()
		{
			super();
			_name = "poly";
			_toolTip = niceResourceManager.getString('json','workflow.designer.polygonal.line');
			_cursorDisplay = DiagramToolImpl.CURSOR_ATTACH;
		}
		
		override public function get cursor():Class {
			return null;
		}
		
	}
}