package com.sysbliss.jira.workflow.process.transition
{
import com.sysbliss.jira.plugins.workflow.model.FlexWorkflowObject;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	
	import mx.controls.Alert;
	
	public class AddTransitionDraftProcessor extends AbstractProcessor
	{
		[Autowire]
		public var workflowDiagramManager:WorkflowDiagramManager;
		
		public function AddTransitionDraftProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var addRequest:WorkflowAddTransitionRequest = request as WorkflowAddTransitionRequest;
			if(addRequest.workflow.isDraftWorkflow){
				var alertMessage:String = "";
				var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title_readonly");
				var parentWorkflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowByName(addRequest.workflow.name,false);

                var startNode:FlexWorkflowObject = addRequest.edge.startNode.data as FlexWorkflowObject;

				if(startNode is FlexJiraStep && !parentStepHasOutgoingTransition(parentWorkflow,startNode as FlexJiraStep)){
                    var step:FlexJiraStep = addRequest.edge.startNode.data as FlexJiraStep;

					alertMessage = niceResourceManager.getString("json","workflow.designer.draft_step_on_parent_add_transition",[step.name]);
					Alert.show(alertMessage,alertTitle,Alert.OK,null,null,null,Alert.OK);
					workflowDiagramManager.getCurrentDiagram().forceDeleteEdge(addRequest.edge);
					return;
				} else {
                    //if it's not a step, it's the initial action which will get caught in the next processor
					successor.processRequest(request);
				}
			} else {
				successor.processRequest(request);
			}
		}

		
		private function parentStepHasOutgoingTransition(parentWorkflow:FlexJiraWorkflow,step:FlexJiraStep):Boolean {
			var parentStepHasTransition:Boolean = true;
			var parentStep:FlexJiraStep = parentWorkflow.getStep(step.id);
			
			if(parentStep && (!parentStep.actions || parentStep.actions.length < 1)){
				parentStepHasTransition = false;
			}
			return parentStepHasTransition;
		}
	}
}		
