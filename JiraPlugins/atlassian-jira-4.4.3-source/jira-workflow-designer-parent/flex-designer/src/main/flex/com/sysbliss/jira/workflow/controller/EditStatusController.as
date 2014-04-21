package com.sysbliss.jira.workflow.controller
{
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStatus;
import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.EditStatusDialog;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

	import mx.logging.ILogger;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class EditStatusController extends WorkflowAbstractController
	{

		[Autowire]
		public var editStatusDialog:EditStatusDialog;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
			
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		public function EditStatusController()
		{
			super();
		}
		
		[Mediate(event="${eventTypes.SHOW_STATUS_EDITOR}", properties="data")]
		public function showStatusEditor(data:Object):void {

			MDIDialogUtils.popModalDialog(editStatusDialog);
			editStatusDialog.clear();

            if(data.status != null) {
                editStatusDialog.selectStatus(FlexJiraStatus(data.status));
            }

		}
		
		[Mediate(event="${eventTypes.DO_STATUS_SAVE}", properties="data")]
		public function doSaveStatus(data:Object):void {
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_updating_status");
			executeServiceCall(jiraService.updateStatus(data.id,data.newName,data.newDesc,data.iconUrl),onStatusUpdated,DefaultFaultHandler.handleFault);
		}
		
		private function onStatusUpdated(e:ResultEvent):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			Swiz.dispatch(EventTypes.REFRESH_STATUS_LIST);
		}
		
		[Mediate(event="${eventTypes.DO_STATUS_DELETE}", properties="data")]
		public function doDeleteStatus(data:Object):void {
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_deleting_status");
			executeServiceCall(jiraService.deleteStatus(data.id),onStatusDeleted,DefaultFaultHandler.handleFault);
		}
		
		private function onStatusDeleted(e:ResultEvent):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			Swiz.dispatch(EventTypes.REFRESH_STATUS_LIST);
		}
		
	}
}