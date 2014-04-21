package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.collections.HashMap;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStatusImpl;
import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.event.StatusListEvent;
	import com.sysbliss.jira.workflow.event.WorkflowEvent;
	import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStatus;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
	import com.sysbliss.jira.workflow.utils.StatusUtils;

	import mx.collections.ArrayCollection;
	import mx.logging.ILogger;
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.Swiz;
	import org.swizframework.controller.AbstractController;

	public class JiraStatusController extends WorkflowAbstractController
	{
        [ArrayElementType("com.sysbliss.jira.plugins.workflow.model.FlexJiraStatus")]
		private var _allStatuses:ArrayCollection;
		private var _listProvider:ArrayCollection;
		private var _statusListItemMap:HashMap;
		private var _workflowsToLoad:int;
		
		[Bindable]
		public var usedStatuses:ArrayCollection;
		
		[Bindable]
		public var unusedStatues:ArrayCollection;
		
		[Autowire]
		public var statusUtils:StatusUtils;
		
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
		
		[Autowire]
		public var workflowDiagramManager:WorkflowDiagramManager;
		
		public function JiraStatusController()
		{
			super();
			this._allStatuses = new ArrayCollection();
		}
		
		[Mediate(event="${eventTypes.REFRESH_STATUS_LIST}")]
		public function refreshStatusList():void {
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_loading_status_list");
			executeServiceCall(jiraService.getAllStatuses(),onGetStatuses,DefaultFaultHandler.handleFault);
		}
		
		private function onGetStatuses(e:ResultEvent):void {
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
            var resultArray:ArrayCollection = e.result as ArrayCollection;
            var statusArray:ArrayCollection = new ArrayCollection();
            for each(var status:FlexJiraStatusImpl in resultArray){
                statusArray.addItem(status);
            }
			var evt:StatusListEvent = new StatusListEvent(EventTypes.JIRA_STATUS_LIST_RETRIEVED,statusArray);
			Swiz.dispatchEvent(evt);
		}
		
		[Mediate(event="${eventTypes.JIRA_STATUS_LIST_RETRIEVED}", properties="statuses")]
		public function setStatuses(statuses:ArrayCollection):void {
			this._allStatuses = statuses;
			statusUtils.setAllStatuses(statuses);

			_statusListItemMap = createListItemMap();
			
			//if there are open workflows, we need to update them
			_workflowsToLoad = 0;
			var openWorkflows:ArrayCollection = workflowDiagramManager.getOpenWorkflows();
			if(openWorkflows.length > 0){
				_workflowsToLoad = openWorkflows.length;
				var i:int;
				var workflow:FlexJiraWorkflow;
				for(i=0;i<openWorkflows.length;i++){
					workflow = openWorkflows.getItemAt(i) as FlexJiraWorkflow;
					Swiz.dispatchEvent(new WorkflowEvent(EventTypes.LOAD_WORKFLOW,workflow,WorkflowEvent.STATUS_REFRESH));
				}
			}else{
				updateStandardListProvider();
			}
		}
		
		public function getAllStatuses():ArrayCollection {
			return _allStatuses;
		}
		
		[Mediate(event="${eventTypes.WORKFLOW_LOADED}", properties="workflow,reason")]
		public function processRefresh(fjw:FlexJiraWorkflow,reason:String):void {
			if(reason == WorkflowEvent.STATUS_REFRESH){
				_workflowsToLoad -= 1;
				if(_workflowsToLoad < 1){
					MDIDialogUtils.removeModalDialog(jiraProgressDialog);
					updateListProviderForWorkflow(workflowDiagramManager.getCurrentWorkflow());
				}
			}
		}
		
		public function getListItemForId(id:String):Object {
			var dataObject:Object = new Object();
			var fjs:FlexJiraStatus;
			for each(var status:FlexJiraStatus in _allStatuses){
				if(status.id == id){
					fjs = status;
					break;
				}
			}
			
			dataObject.nodeRendererClass = statusUtils.getNodeRendererForStatus(fjs);
			dataObject.data = fjs;
			dataObject.label = fjs.name;
			
			return dataObject;
		}
		
		public function getListIcon(item:Object):Class {
			if(item is FlexJiraStatus){
				return statusUtils.getIconForStatus(item as FlexJiraStatus);
			} else {
				return statusUtils.getIconForStatus(item.data as FlexJiraStatus);
			}
			
		}
		
		private function createListItemMap():HashMap {
			var dp:HashMap = new HashMap();
			var i:int;
			var dataObject:Object;
			for each(var status:FlexJiraStatus in _allStatuses){
				dataObject = new Object();
				dataObject.nodeRendererClass = statusUtils.getNodeRendererForStatus(status);
				dataObject.data = status;
				dataObject.label = status.name;
				dp.put(status.id,dataObject);
			}
			return dp;
		}
		
		[Mediate(event="${eventTypes.CURRENT_WORKFLOW_CHANGED}", properties="workflow")]
		[Mediate(event="${eventTypes.WORKFLOW_OBJECTS_DELETED}", properties="workflow")]
		[Mediate(event="${eventTypes.WORKFLOW_STEP_UPDATED}", properties="workflow")]
		[Mediate(event="${eventTypes.WORKFLOW_STEP_ADDED}", properties="workflow")]
		public function updateListProviderForWorkflow(workflow:FlexJiraWorkflow):void {
			var dp:ArrayCollection = new ArrayCollection();
			for each(var status:FlexJiraStatus in workflow.unlinkedStatuses) {
				if(_statusListItemMap.keyExists(status.id)){
					dp.addItem(_statusListItemMap.getValue(status.id));
				}
			}
			this.listProvider = dp;
			Swiz.dispatch(EventTypes.STATUSES_REFRESHED);		
		}
		
		[Mediate(event="${eventTypes.ALL_WORKFLOWS_CLOSED}")]
		public function updateStandardListProvider():void {
			var dp:ArrayCollection = new ArrayCollection();
			for each(var dataObject:Object in _statusListItemMap.getValues()){
				dp.addItem(dataObject);
			}
			this.listProvider = dp;
			Swiz.dispatch(EventTypes.STATUSES_REFRESHED);
		}
		
		[Bindable]
		public function get listProvider():ArrayCollection {
			return _listProvider;
		}
		
		public function set listProvider(dp:ArrayCollection):void {
			_listProvider = dp;
		}
	}
}