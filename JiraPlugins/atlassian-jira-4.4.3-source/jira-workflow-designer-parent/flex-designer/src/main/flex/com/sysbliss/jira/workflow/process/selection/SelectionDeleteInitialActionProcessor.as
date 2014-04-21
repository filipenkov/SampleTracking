package com.sysbliss.jira.workflow.process.selection
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	
	public class SelectionDeleteInitialActionProcessor extends AbstractProcessor
	{
		public function SelectionDeleteInitialActionProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var delRequest:WorkflowSelectionDeleteRequest = request as WorkflowSelectionDeleteRequest;
			var alertMessage:String = "";
			var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title_readonly");
			
			var containsInitialAction:Boolean = isInitialActionInSelection(delRequest.selections,delRequest.workflow);
			if(containsInitialAction){
				alertMessage = niceResourceManager.getString("json","workflow.designer.initial_action_delete",[delRequest.workflow.initialActions[0].name]);
				Alert.show(alertMessage,alertTitle,Alert.OK,null,null,null,Alert.OK);
				return;
			} else {
				successor.processRequest(request);
			}
		}
		
		private function isInitialActionInSelection(selections:ArrayCollection,workflow:FlexJiraWorkflow):Boolean {
			var containsInitialAction:Boolean = false;
			var initialActions:ArrayCollection = workflow.initialActions;
			var i:int;
			var obj:Object;
			var action:FlexJiraAction;
			for(i=0;i<selections.length;i++){
				obj = selections.getItemAt(i);
				action = obj as FlexJiraAction;
				if(action && workflow.isInitialAction(action.id)){
					containsInitialAction = true;
					break;
				}
			}
			return containsInitialAction;
		}
		
	}
}