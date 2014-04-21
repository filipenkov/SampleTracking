package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.manager.JiraServerManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

	import mx.controls.Alert;
	import mx.events.CloseEvent;
	import mx.logging.ILogger;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class DeleteWorkflowController extends WorkflowAbstractController
	{

		private var _workflowToDelete:FlexJiraWorkflow;
		
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
			
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		[Autowire]
		public var jiraServerManager:JiraServerManager;
		
		public function DeleteWorkflowController()
		{
			super();
		}
		
		[Mediate(event="${eventTypes.DELETE_WORKFLOW}", properties="workflow")]
		public function deleteWorkflow(workflow:FlexJiraWorkflow):void {
			_workflowToDelete = workflow;

			if(jiraServerManager.getUserPrefs().confirmDeleteWorkflow){
				var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title.confirm_delete_selection");
				var alertMessage:String = niceResourceManager.getString("json","workflow.designer.confirm_delete_workflow");
				Alert.show(alertMessage,alertTitle,Alert.OK|Alert.CANCEL,null,confirmDeleteWorkflowHandler,null,Alert.OK);
			} else {
				doDeleteWorkflow();
			}
		}
		
		private function confirmDeleteWorkflowHandler(e:CloseEvent):void {
			if (e.detail==Alert.OK){
				doDeleteWorkflow();
			}
		}
		
		private function doDeleteWorkflow():void {
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_deleting_selections");
			executeServiceCall(jiraService.deleteWorkflow(_workflowToDelete),onWorkflowDeleted,DefaultFaultHandler.handleFault);
		}
		
		private function onWorkflowDeleted(e:ResultEvent):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			_workflowToDelete = null;
			Swiz.dispatch(EventTypes.REFRESH_WORKFLOW_LIST);
		}
		
	}
}