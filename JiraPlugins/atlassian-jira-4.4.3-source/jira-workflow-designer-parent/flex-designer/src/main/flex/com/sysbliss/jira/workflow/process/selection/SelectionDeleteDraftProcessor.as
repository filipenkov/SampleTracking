package com.sysbliss.jira.workflow.process.selection
{
	import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	
	import mx.collections.ArrayCollection;
	import mx.controls.Alert;
	
	public class SelectionDeleteDraftProcessor extends AbstractProcessor
	{
		[Autowire]
		public var workflowDiagramManager:WorkflowDiagramManager;
		
		public function SelectionDeleteDraftProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var delRequest:WorkflowSelectionDeleteRequest = request as WorkflowSelectionDeleteRequest;
			if(delRequest.workflow.isDraftWorkflow){
				var alertMessage:String = "";
				var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title_readonly");
				var parentWorkflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowByName(delRequest.workflow.name,false);
				
				if(parentContainsDraftStep(parentWorkflow,delRequest.selections)){
					alertMessage = niceResourceManager.getString("json","workflow.designer.draft_step_on_parent_delete");
					Alert.show(alertMessage,alertTitle,Alert.OK,null,null,null,Alert.OK);
					return;
				} else {
					successor.processRequest(request);
				}
			} else {
				successor.processRequest(request);
			}
			
		}

		
		private function parentContainsDraftStep(parentWorkflow:FlexJiraWorkflow,selections:ArrayCollection):Boolean {
			var parentHasStep:Boolean = false;
			var i:int;
			var obj:Object;
			var step:FlexJiraStep;
			var parentStep:FlexJiraStep;
			for(i=0;i<selections.length;i++){
				obj = selections.getItemAt(i);
				step = obj as FlexJiraStep;
				if(step){
					parentStep = parentWorkflow.getStep(step.id);
					if(parentStep){
						parentHasStep = true;
						break;
					}
				}
			}
			return parentHasStep;
		}
	}
}