package com.sysbliss.diagram.tools
{
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;

import mx.resources.ResourceManager;

public class DiagramBezierTool extends DiagramLinkTool implements DiagramTool
	{
		public function DiagramBezierTool()
		{
			super();
			_name = "bezier";
			_toolTip = niceResourceManager.getString('json','workflow.designer.bezier.line');
			_cursorDisplay = DiagramToolImpl.CURSOR_ATTACH;
		}
		
		override public function get cursor():Class {
			return null;
		}
		
	}
}