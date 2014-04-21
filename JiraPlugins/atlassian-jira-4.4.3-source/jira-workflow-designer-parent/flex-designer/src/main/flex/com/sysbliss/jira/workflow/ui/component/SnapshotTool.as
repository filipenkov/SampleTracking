package com.sysbliss.jira.workflow.ui.component
{
import com.sysbliss.diagram.tools.DiagramTool;
import com.sysbliss.diagram.tools.DiagramToolImpl;

public class SnapshotTool extends DiagramToolImpl implements DiagramTool
	{
		public function SnapshotTool()
		{
			super();
			_name = niceResourceManager.getString('json','workflow.designer.snapshot');
			_toolTip = niceResourceManager.getString('json','workflow.designer.save.snapshot.image');
			_cursorDisplay = DiagramToolImpl.CURSOR_ATTACH;
		}

		override public function get cursor():Class {
			return null;
		}
}
}