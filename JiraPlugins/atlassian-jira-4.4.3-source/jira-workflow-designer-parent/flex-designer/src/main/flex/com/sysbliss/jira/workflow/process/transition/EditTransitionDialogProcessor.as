package com.sysbliss.jira.workflow.process.transition
{
	import com.sysbliss.jira.workflow.process.AbstractProcessor;
	import com.sysbliss.jira.workflow.process.Request;
	import com.sysbliss.jira.workflow.ui.dialog.EditTransitionDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

	public class EditTransitionDialogProcessor extends AbstractProcessor
	{
		[Autowire]
		public var editTransitionDialog:EditTransitionDialog;
		
		public function EditTransitionDialogProcessor()
		{
			super();
		}
		
		override public function processRequest(request:Request):void
		{
			var editRequest:WorkflowEditTransitionRequest = request as WorkflowEditTransitionRequest;
			
			editTransitionDialog.setWorkflow(editRequest.workflow);
			editTransitionDialog.setEdge(editRequest.edge);
			MDIDialogUtils.popModalDialog(editTransitionDialog);
			return;
		}
		
	}
}