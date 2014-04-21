package com.sysbliss.jira.workflow.ui.component
{
import com.sysbliss.diagram.tools.DiagramTool;
import com.sysbliss.diagram.tools.DiagramToolImpl;

public class PrintTool extends DiagramToolImpl implements DiagramTool
	{
		public function PrintTool()
		{
			super();
			_name = niceResourceManager.getString('json','workflow.designer.print');
			_toolTip = niceResourceManager.getString('json','workflow.designer.print.diagram');
			_cursorDisplay = DiagramToolImpl.CURSOR_ATTACH;
		}

		override public function get cursor():Class {
			return null;
		}
}
}