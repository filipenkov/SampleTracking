package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.ui.dialog.PublishWorkflowDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

	import mx.logging.ILogger;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class DraftWorkflowController extends WorkflowAbstractController
	{

		private var _workflowToPublish:FlexJiraWorkflow;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
			
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		[Autowire]
		public var publishWorkflowDialog:PublishWorkflowDialog;
		
		public function DraftWorkflowController()
		{
			super();
		}
		
		[Mediate(event="${eventTypes.CREATE_DRAFT_WORKFLOW}",properties="workflow")]
		public function newDraftWorkflow(workflow:FlexJiraWorkflow):void {
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_creating_draft_workflow");
			executeServiceCall(jiraService.createDraftWorkflow(workflow),onDraftCreated,DefaultFaultHandler.handleFault,[workflow]);
			//log.debug("creating draft workflow");
		}
		
		private function onDraftCreated(e:ResultEvent,parentWorkflow:FlexJiraWorkflow):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			Swiz.dispatch(EventTypes.REFRESH_WORKFLOW_LIST);
		}
		
		[Mediate(event="${eventTypes.PUBLISH_DRAFT_WORKFLOW}",properties="workflow")]
		public function publishDraftWorkflow(workflow:FlexJiraWorkflow):void {
			_workflowToPublish = workflow;
			
			publishWorkflowDialog.setWorkflow(workflow);
			MDIDialogUtils.popModalDialog(publishWorkflowDialog);
		}
		
		[Mediate(event="${eventTypes.DO_PUBLISH_DRAFT_WORKFLOW}", properties="data")]
		public function doPublishDraftWorkflow(data:Object):void {
			MDIDialogUtils.removeModalDialog(publishWorkflowDialog);
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_publishing_workflow");
			executeServiceCall(jiraService.publishDraftWorkflow(_workflowToPublish,data.doBackup,data.newName),onWorkflowPublished,DefaultFaultHandler.handleFault);
			//log.debug("adding workflow: " + data.newName);
		}
		
		private function onWorkflowPublished(e:ResultEvent):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			_workflowToPublish = null;
			Swiz.dispatch(EventTypes.REFRESH_WORKFLOW_LIST);
		}
		
	}

}