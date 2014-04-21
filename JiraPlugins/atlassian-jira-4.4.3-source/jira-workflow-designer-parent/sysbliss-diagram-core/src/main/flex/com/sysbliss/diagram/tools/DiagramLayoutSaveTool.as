package com.sysbliss.diagram.tools
{
	public class DiagramLayoutSaveTool extends DiagramToolImpl implements DiagramTool
	{
		public function DiagramLayoutSaveTool()
		{
			super();
			_name = "saveLayout";
			_toolTip = niceResourceManager.getString('json','workflow.designer.save.layout');
			_cursorDisplay = DiagramToolImpl.CURSOR_ATTACH;
		}
		
		override public function get cursor():Class {
			return null;
		}
	}
}