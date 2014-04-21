package com.sysbliss.jira.workflow.ui.dialog
{
	import flexlib.mdi.containers.MDIWindow;
	
	import mx.controls.ProgressBar;

	public class JiraProgressDialog extends MDIWindow
	{
		private var _progress:ProgressBar;
		public function JiraProgressDialog()
		{
			super();
			minWidth = width = 240;
			minHeight = height = 100;
			this.setStyle("horizontalAlign","center");
			this.setStyle("verticalAlign","middle");
			this.resizable = false;
            this.closeBtn.visible = false;
            this.minimizeBtn.visible = false;
		}
		
		override protected function createChildren():void {
			super.createChildren();
			if(!_progress){
				_progress = new ProgressBar();
				_progress.indeterminate = true;
				_progress.width = 210;
			}
			
			addChild(_progress);
		}
		
		public function set progressLabel(s:String):void {
			_progress.label = s;
		}
		
	}
}