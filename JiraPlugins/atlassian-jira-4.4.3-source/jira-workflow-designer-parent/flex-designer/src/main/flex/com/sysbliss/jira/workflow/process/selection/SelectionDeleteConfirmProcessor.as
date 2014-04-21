package com.sysbliss.jira.workflow.process.selection
{
import com.sysbliss.diagram.ui.DiagramUIObject;
import com.sysbliss.diagram.ui.UIEdge;
import com.sysbliss.diagram.ui.UINode;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
import com.sysbliss.jira.plugins.workflow.model.FlexWorkflowObject;
import com.sysbliss.jira.workflow.manager.JiraServerManager;
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	
	import mx.controls.Alert;
	import mx.events.CloseEvent;
	
	public class SelectionDeleteConfirmProcessor extends AbstractProcessor
	{
		[Autowire]
		public var jiraServerManager:JiraServerManager;
		
		private var _currentRequest:WorkflowSelectionDeleteRequest;
		
		public function SelectionDeleteConfirmProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			_currentRequest = request as WorkflowSelectionDeleteRequest;
			
			var alertMessage:String = "";
			var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title_readonly");
			
			if(jiraServerManager.getUserPrefs().confirmDeleteSelection){
                alertTitle = niceResourceManager.getString("json","workflow.designer.title.confirm_delete_selection");

                var workflowObject:FlexWorkflowObject  = _currentRequest.selections.getItemAt(0) as FlexWorkflowObject;
                if ((workflowObject is FlexJiraStep)) {
                    var step:FlexJiraStep = workflowObject as FlexJiraStep;
                    alertMessage = niceResourceManager.getString("json","workflow.designer.confirm_delete_step",[step.name]);
                } else if ((workflowObject is FlexJiraAction)) {
                    var action:FlexJiraAction = workflowObject as FlexJiraAction;
                    alertMessage = niceResourceManager.getString("json","workflow.designer.confirm_delete_transition",[action.name]);
                }

                Alert.buttonWidth = 80;
				var alert:Alert = Alert.show(alertMessage,alertTitle,Alert.OK|Alert.CANCEL,null,confirmDeleteSelectionHandler,null,Alert.OK);

			}
		}
		
		private function confirmDeleteSelectionHandler(e:CloseEvent):void {
			if (e.detail==Alert.OK){
				successor.processRequest(_currentRequest);
			} else {
				return;
			}
		}
		
	}
}