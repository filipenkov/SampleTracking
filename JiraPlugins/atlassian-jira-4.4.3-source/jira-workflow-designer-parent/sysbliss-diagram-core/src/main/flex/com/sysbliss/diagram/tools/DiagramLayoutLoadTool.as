package com.sysbliss.diagram.tools
{
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;

import mx.resources.ResourceManager;

public class DiagramLayoutLoadTool extends DiagramToolImpl implements DiagramTool
	{
		public function DiagramLayoutLoadTool()
		{
			super();
			_name = "loadLayout";
			_toolTip = niceResourceManager.getString('json','workflow.designer.load.saved.layout');
			_cursorDisplay = DiagramToolImpl.CURSOR_ATTACH;
		}
		
		override public function get cursor():Class {
			return null;
		}
	}
}