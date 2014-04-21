package com.sysbliss.jira.workflow.controller
{
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflowImpl;
import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.event.WorkflowListEvent;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
	
	import mx.collections.ArrayCollection;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class WorkflowListController extends WorkflowAbstractController
	{
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
		
		public function WorkflowListController()
		{
			super();
		}
		
		[Mediate(event="${eventTypes.REFRESH_WORKFLOW_LIST}")]
		public function refreshWorkflowList():void {
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_loading_workflow_list");
			executeServiceCall(jiraService.getWorkflows(),onGetWorkflows,DefaultFaultHandler.handleFault);
		}
		
		private function onGetWorkflows(e:ResultEvent):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
            var resultArray:ArrayCollection = e.result as ArrayCollection;
            var workflowArray:ArrayCollection = new ArrayCollection();
            for each(var workflow:FlexJiraWorkflowImpl in resultArray){
                workflowArray.addItem(workflow);
            }
			var evt:WorkflowListEvent = new WorkflowListEvent(EventTypes.JIRA_WORKFLOW_LIST_RETRIEVED,workflowArray);
			Swiz.dispatchEvent(evt);
		}
		
	}
}