package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.CreateWorkflowDialog;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

	import mx.logging.ILogger;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class CreateWorkflowController extends WorkflowAbstractController
	{

		[Autowire]
		public var createWorkflowDialog:CreateWorkflowDialog;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
			
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		public function CreateWorkflowController()
		{
			super();
		}
		
		[Mediate(event="${eventTypes.NEW_WORKFLOW}")]
		public function newWorkflow():void {
			
			createWorkflowDialog.clear();
			MDIDialogUtils.popModalDialog(createWorkflowDialog);
		}
		
		[Mediate(event="${eventTypes.DO_WORKFLOW_NEW}", properties="data")]
		public function doNewWorkflow(data:Object):void {
			MDIDialogUtils.removeModalDialog(createWorkflowDialog);
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_creating_workflow");
			executeServiceCall(jiraService.createNewWorkflow(data.newName,data.newDesc),onWorkflowCreated,DefaultFaultHandler.handleFault);
			//log.debug("creating workflow: " + data.newName);
		}
		
		private function onWorkflowCreated(e:ResultEvent):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			Swiz.dispatch(EventTypes.REFRESH_WORKFLOW_LIST);
		}
		
	}
}