package com.sysbliss.jira.workflow.process.transition
{
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	import com.sysbliss.jira.workflow.ui.dialog.AddTransitionDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
	
	public class AddTransitionDialogProcessor extends AbstractProcessor
	{
		[Autowire]
		public var addTransitionDialog:AddTransitionDialog;
		
		public function AddTransitionDialogProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var addRequest:WorkflowAddTransitionRequest = request as WorkflowAddTransitionRequest;
			
			addTransitionDialog.setWorkflow(addRequest.workflow);
			addTransitionDialog.setEdge(addRequest.edge);
            addTransitionDialog.firstRun();
			MDIDialogUtils.popModalDialog(addTransitionDialog);
			return;
		}
	}
}