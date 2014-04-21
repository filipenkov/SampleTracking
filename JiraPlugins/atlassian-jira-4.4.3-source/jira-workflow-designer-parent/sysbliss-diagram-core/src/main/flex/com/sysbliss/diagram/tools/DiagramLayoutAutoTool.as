package com.sysbliss.diagram.tools
{
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;

import mx.resources.ResourceManager;

public class DiagramLayoutAutoTool extends DiagramToolImpl implements DiagramTool
	{
		public function DiagramLayoutAutoTool()
		{
			super();
			_name = "autoLayout";
			_toolTip = niceResourceManager.getString('json','workflow.designer.auto.layout');
			_cursorDisplay = DiagramToolImpl.CURSOR_ATTACH;
		}
		
		override public function get cursor():Class {
			return null;
		}
		
	}
}