package com.sysbliss.jira.workflow.process.transition
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	
	import mx.controls.Alert;

	public class EditTransitionInitialActionProcessor extends AbstractProcessor
	{
		public function EditTransitionInitialActionProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var editRequest:WorkflowEditTransitionRequest = request as WorkflowEditTransitionRequest;
			var alertMessage:String = "";
			var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title_readonly");
			
			var startAction:FlexJiraAction = editRequest.edge.data as FlexJiraAction;
			if(startAction && editRequest.workflow.isInitialAction(startAction.id)){
				alertMessage = niceResourceManager.getString("json","workflow.designer.initial_action_edit",[startAction.name]);
				Alert.show(alertMessage,alertTitle,Alert.OK,null,null,null,Alert.OK);
				return;
			} else {
				successor.processRequest(request);
			}
		}
		
	}
}