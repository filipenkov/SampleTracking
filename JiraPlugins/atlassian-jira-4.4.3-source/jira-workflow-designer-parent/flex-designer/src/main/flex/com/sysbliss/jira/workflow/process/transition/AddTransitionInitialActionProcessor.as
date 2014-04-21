package com.sysbliss.jira.workflow.process.transition
{
import com.sysbliss.diagram.util.CursorUtil;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	
	import mx.controls.Alert;
import mx.events.CloseEvent;

public class AddTransitionInitialActionProcessor extends AbstractProcessor
	{
		[Autowire]
		public var workflowDiagramManager:WorkflowDiagramManager;
		
		public function AddTransitionInitialActionProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var addRequest:WorkflowAddTransitionRequest = request as WorkflowAddTransitionRequest;
			var alertMessage:String = "";
			var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title_readonly");
			
			var startAction:FlexJiraAction = addRequest.edge.startNode.data as FlexJiraAction;
            var endAction:FlexJiraAction = addRequest.edge.endNode.data as FlexJiraAction;
			if((startAction && addRequest.workflow.isInitialAction(startAction.id)) || (endAction && addRequest.workflow.isInitialAction(endAction.id))){
				alertMessage = niceResourceManager.getString("json","workflow.designer.initial_action_add_transition",["Create"]);
                CursorUtil.forcePointer();
				Alert.show(alertMessage,alertTitle,Alert.OK,null,onAlertClosed,null,Alert.OK);
				workflowDiagramManager.getCurrentDiagram().forceDeleteEdge(addRequest.edge);
				return;
			} else {
				successor.processRequest(request);
			}
		}

        private function onAlertClosed(e:CloseEvent):void {
            CursorUtil.showPointer();
        }
		
	}
}