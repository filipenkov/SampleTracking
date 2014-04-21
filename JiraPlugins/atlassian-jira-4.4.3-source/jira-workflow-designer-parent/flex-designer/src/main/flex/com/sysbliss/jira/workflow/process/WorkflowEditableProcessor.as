package com.sysbliss.jira.workflow.process
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	
	import mx.controls.Alert;
	
	public class WorkflowEditableProcessor extends AbstractProcessor
	{
		
		public function WorkflowEditableProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var wfRequest:WorkflowRequest = request as WorkflowRequest;
			
			var alertMessage:String = "";
			var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title_readonly");
			
			if(!wfRequest.workflow.isEditable){
				alertMessage = getReadOnlyAlertMessage(wfRequest.workflow);
				Alert.show(alertMessage,alertTitle,Alert.OK,null,null,null,Alert.OK);
				return;
			} else {
				successor.processRequest(request);
			}
		}
		
		private function getReadOnlyAlertMessage(workflow:FlexJiraWorkflow):String {
			var msg:String = niceResourceManager.getString("json","workflow.designer.workflow_readonly",[workflow.name]);
			if(workflow.isActive){
				msg = niceResourceManager.getString("json","workflow.designer.workflow_readonly_active",[workflow.name]);
			}
			if(workflow.isSystemWorkflow){
				msg = niceResourceManager.getString("json","workflow.designer.workflow_readonly_system",[workflow.name]);
			}
			
			return msg;
		}
		
	}
}