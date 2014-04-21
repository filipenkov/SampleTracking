package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.CopyWorkflowDialog;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

	import mx.logging.ILogger;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class CopyWorkflowController extends WorkflowAbstractController
	{


		private var _workflowToCopy:FlexJiraWorkflow;
		
		[Autowire]
		public var copyWorkflowDialog:CopyWorkflowDialog;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
			
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		[Autowire]
		public var workflowDiagramManager:WorkflowDiagramManager;
		
		public function CopyWorkflowController()
		{
			super();
		}
		
		[Mediate(event="${eventTypes.COPY_WORKFLOW}", properties="workflow")]
		public function copyWorkflow(workflow:FlexJiraWorkflow):void {
			_workflowToCopy = workflow;
			
			copyWorkflowDialog.setWorkflow(workflow);
			MDIDialogUtils.popModalDialog(copyWorkflowDialog);
		}
		
		[Mediate(event="${eventTypes.DO_WORKFLOW_COPY}", properties="data")]
		public function doCopyWorkflow(data:Object):void {
			MDIDialogUtils.removeModalDialog(copyWorkflowDialog);
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_copying_workflow");
			executeServiceCall(jiraService.copyWorkflow(data.newName,data.newDesc,_workflowToCopy),onWorkflowCopied,DefaultFaultHandler.handleFault);
			//log.debug("adding workflow: " + data.newName);
		}
		
		private function onWorkflowCopied(e:ResultEvent):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			_workflowToCopy = null;
			Swiz.dispatch(EventTypes.REFRESH_WORKFLOW_LIST);
		}
	}
}