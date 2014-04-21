package com.sysbliss.jira.workflow.controller
{
	import flash.geom.Point;
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.tools.DiagramTool;
	import com.sysbliss.diagram.tools.DiagramZoomInTool;
	import com.sysbliss.diagram.tools.DiagramZoomOutTool;
	import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;

	
	
	import mx.controls.ButtonBar;
	import mx.events.ItemClickEvent;
	import mx.logging.ILogger;
	
	import org.swizframework.controller.AbstractController;

	public class ZoomToolbarController extends WorkflowAbstractController
	{
		private var _toolbar:ButtonBar;
		
		[Autowire]
		public var workflowDiagramManager:WorkflowDiagramManager;
		
		public function ZoomToolbarController()
		{
			super();
		}
		
		private function setupToolbarContainer():void {
			_toolbar.addEventListener(ItemClickEvent.ITEM_CLICK,onToolClick);
		}
		
		
		private function onToolClick(e:ItemClickEvent):void {
			var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();
			if(currentDiagram){

                if(currentDiagram.isLinking()) {
                    currentDiagram.cancelLink();
                }

				var tool:DiagramTool = e.item as DiagramTool;
				var centerPoint:Point = currentDiagram.centerPoint;
				if(tool is DiagramZoomInTool){
					currentDiagram.zoomIn(centerPoint.x,centerPoint.y);
				} else if(tool is DiagramZoomOutTool){
					currentDiagram.zoomOut(centerPoint.x,centerPoint.y);
				}
			}
		}
		
		public function set toolbar(t:ButtonBar):void {
			this._toolbar = t;
			setupToolbarContainer();
		}
		
		public function get toolbar():ButtonBar {
			return this._toolbar;
		}
		
	}
}