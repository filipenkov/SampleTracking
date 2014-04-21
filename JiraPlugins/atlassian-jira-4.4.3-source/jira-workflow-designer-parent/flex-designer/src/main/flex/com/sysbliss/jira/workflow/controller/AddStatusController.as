package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.CreateStatusDialog;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

	import mx.logging.ILogger;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class AddStatusController extends WorkflowAbstractController
	{

		[Autowire]
		public var createStatusDialog:CreateStatusDialog;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
			
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		public function AddStatusController()
		{
			super();
		}
		
		[Mediate(event="${eventTypes.NEW_STATUS}")]
		public function newStatus():void {
			MDIDialogUtils.popModalDialog(createStatusDialog);
		}
		
		[Mediate(event="${eventTypes.DO_STATUS_NEW}", properties="data")]
		public function doNewStatus(data:Object):void {
			MDIDialogUtils.removeModalDialog(createStatusDialog);
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_creating_status");
			executeServiceCall(jiraService.createNewStatus(data.newName,data.newDesc,data.iconUrl),onStatusCreated,DefaultFaultHandler.handleFault);
			//log.debug("creating workflow: " + data.newName);
		}
		
		private function onStatusCreated(e:ResultEvent):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			Swiz.dispatch(EventTypes.REFRESH_STATUS_LIST);
		}
	}
}