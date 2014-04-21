package com.sysbliss.jira.workflow.controller
{
import com.sysbliss.jira.plugins.workflow.model.FlexJiraFieldScreenImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraServerInfoImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStatusImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflowImpl;
import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.event.FieldScreenListEvent;
	import com.sysbliss.jira.workflow.event.GenericDataEvent;
	import com.sysbliss.jira.workflow.event.ServerInfoEvent;
	import com.sysbliss.jira.workflow.event.StatusListEvent;
	import com.sysbliss.jira.workflow.event.WorkflowListEvent;
	import com.sysbliss.jira.workflow.manager.JiraServerManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraServerInfo;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.command.CommandChain;

	public class ApplicationInitializer extends WorkflowAbstractController
	{
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
		
		[Autowire]
		public var jiraServerManager:JiraServerManager;
		
		private var initialized:Boolean = false;
		
		public function ApplicationInitializer()
		{
			super();
		}
		
		public function initialize():void {
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
            jiraProgressDialog.title = niceResourceManager.getString('json','workflow.designer.connecting.to.jira');
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_loading_server_info");
			executeServiceCall(jiraService.getUserSession(),onUserSession,DefaultFaultHandler.handleFault);
		}
		
		private function onUserSession(e:ResultEvent):void {
			Swiz.dispatchEvent(new GenericDataEvent(EventTypes.USER_SESSION_RETRIEVED,e.result as String));
		}
		
		[Mediate(event="${eventTypes.USER_TOKEN_AVAILABLE}")]
		public function initTheApp():void {
			if(!initialized){
				var chain:CommandChain = new CommandChain(CommandChain.SERIES);
				chain.addCommand(createCommand(jiraService.getJiraServerInfo,null,onGetServerInfo,DefaultFaultHandler.handleFault));
				chain.addCommand(createCommand(jiraService.getWorkflows,null,onGetWorkflows,DefaultFaultHandler.handleFault));
				chain.addCommand(createCommand(jiraService.getAllStatuses,null,onGetStatuses,DefaultFaultHandler.handleFault));
				chain.addCommand(createCommand(jiraService.getFieldScreens,null,onGetFieldScreens,DefaultFaultHandler.handleFault));
				chain.completeHandler = completeHandler;
				
				chain.proceed();
			}
		}
		
		private function onGetServerInfo(e:ResultEvent):void {
			var serverInfo:FlexJiraServerInfo = e.result as FlexJiraServerInfoImpl;
            jiraServerManager.setServerInfo(serverInfo);
			Swiz.dispatchEvent(new ServerInfoEvent(EventTypes.SERVER_INFO_LOADED,serverInfo));
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_loading_workflow_list");
		}
		
		private function onGetWorkflows(e:ResultEvent):void {
            var resultArray:ArrayCollection = e.result as ArrayCollection;
            var workflowArray:ArrayCollection = new ArrayCollection();
            for each(var workflow:FlexJiraWorkflowImpl in resultArray){
                workflowArray.addItem(workflow);
            }

			var evt:WorkflowListEvent = new WorkflowListEvent(EventTypes.JIRA_WORKFLOW_LIST_RETRIEVED,workflowArray);
			Swiz.dispatchEvent(evt);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_loading_status_list");
		}
		
		private function onGetStatuses(e:ResultEvent):void {

            var resultArray:ArrayCollection = e.result as ArrayCollection;
            var statusArray:ArrayCollection = new ArrayCollection();
            for each(var status:FlexJiraStatusImpl in resultArray){
                statusArray.addItem(status);
            }
			var evt:StatusListEvent = new StatusListEvent(EventTypes.JIRA_STATUS_LIST_RETRIEVED,statusArray);
			Swiz.dispatchEvent(evt);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_loading_field_screen_list");
		}
		
		private function onGetFieldScreens(e:ResultEvent):void {
            var resultArray:ArrayCollection = e.result as ArrayCollection;
            var fieldScreenArray:ArrayCollection = new ArrayCollection();
            for each(var fieldScreen:FlexJiraFieldScreenImpl in resultArray){
                fieldScreenArray.addItem(fieldScreen);
            }
			var evt:FieldScreenListEvent = new FieldScreenListEvent(EventTypes.JIRA_FIELD_SCREENS_RETRIEVED,fieldScreenArray);
			Swiz.dispatchEvent(evt);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_complete");
		}
		
		private function completeHandler():void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
			Swiz.dispatch(EventTypes.APPLICATION_INITIALIZED);
			initialized = true;
		}
		
		
		
	}
}