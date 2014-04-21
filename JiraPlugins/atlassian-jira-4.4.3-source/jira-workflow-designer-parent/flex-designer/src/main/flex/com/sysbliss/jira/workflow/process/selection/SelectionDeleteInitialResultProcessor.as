package com.sysbliss.jira.workflow.process.selection
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraResult;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	
	public class SelectionDeleteInitialResultProcessor extends AbstractProcessor
	{
		public function SelectionDeleteInitialResultProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var delRequest:WorkflowSelectionDeleteRequest = request as WorkflowSelectionDeleteRequest;
			
			var alertMessage:String = "";
			var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title_readonly");
			
			var containsInitialResult:Boolean = isInitialResultInSelection(delRequest.selections,delRequest.workflow);
			if(containsInitialResult){
				alertMessage = niceResourceManager.getString("json","workflow.designer.initial_result_delete",[delRequest.workflow.initialActions[0].name]);
				Alert.show(alertMessage,alertTitle,Alert.OK,null,null,null,Alert.OK);
				return;
			} else {
				successor.processRequest(request);
			}
		}
		
		private function isInitialResultInSelection(selections:ArrayCollection,workflow:FlexJiraWorkflow):Boolean {
			//first get the steps in our selections
			var i:int;
			var obj:Object;
			var step:FlexJiraStep;
			var stepIds:Array = new Array();
			for(i=0;i<selections.length;i++){
				obj = selections.getItemAt(i);
				step = obj as FlexJiraStep
				if(step){
					stepIds.push(step.id);
				}
			}
			//now see if any of the result steps are in our selection
			var containsInitialResult:Boolean = false;
			var initialActions:ArrayCollection = workflow.initialActions;
			var result:FlexJiraResult;
			var resultStep:FlexJiraStep;
			for each(var initialAction:FlexJiraAction in initialActions) {
				result = initialAction.unconditionalResult;
				resultStep = workflow.getStep(result.stepId);
				if(resultStep && (stepIds.indexOf(resultStep.id) > -1)){
					containsInitialResult = true;
					break;
				}
			}

			return containsInitialResult;
		}
		
	}
}