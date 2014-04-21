package com.sysbliss.jira.workflow.controller
{
	import com.sysbliss.diagram.data.Edge;
	import com.sysbliss.diagram.data.Node;
	import com.sysbliss.diagram.data.DiagramObject;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflowImpl;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraMetadataContainer;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.plugins.workflow.model.FlexWorkflowObject;
	import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;
	import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
	import com.sysbliss.jira.workflow.ui.dialog.PropertiesDialog;
	import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
	
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.rpc.events.ResultEvent;
	
	import org.swizframework.controller.AbstractController;

	public class EditPropertiesController extends WorkflowAbstractController
	{
		private var _currentMDO:FlexJiraMetadataContainer;
		private var _currentDO:DiagramObject;
		private var _savedData:Object;

		[Autowire]
		public var propertyDialog:PropertiesDialog;
		
		[Autowire]
		public var jiraProgressDialog:JiraProgressDialog;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;
		
		[Autowire]
		public var workflowDiagramManager:WorkflowDiagramManager;
		
		public function EditPropertiesController()
		{
			super();
		}
		
		[Mediate(event="${eventTypes.SHOW_EDIT_PROPERTIES}", properties="workflow,metadataObject,diagramObject")]
		public function editStep(wf:FlexJiraWorkflow,mdo:FlexJiraMetadataContainer,dobj:DiagramObject):void {
			_currentMDO = mdo;
			_currentDO = dobj;
			MDIDialogUtils.popModalDialog(propertyDialog);
			propertyDialog.clear();
			propertyDialog.setWorkflow(wf);
			propertyDialog.setWorkflowObject(mdo);
		}
		
		[Mediate(event="${eventTypes.DO_SAVE_METADATA}", properties="data")]
		public function doSaveMetadata(data:Object):void {
			MDIDialogUtils.removeModalDialog(propertyDialog);
			MDIDialogUtils.popModalDialog(jiraProgressDialog);
			jiraProgressDialog.progressLabel = niceResourceManager.getString("json","workflow.designer.progress_updating_properties");
			
			var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
			_savedData = data;
			executeServiceCall(jiraService.updateProperties(_currentMDO,data,workflow),onPropertiesUpdated,DefaultFaultHandler.handleFault,[workflow]);
		}
		
		private function onPropertiesUpdated(e:ResultEvent,fjw:FlexJiraWorkflow):void {
			var workflow:FlexJiraWorkflow = e.result as FlexJiraWorkflowImpl;
			workflow.uid = fjw.uid;
			workflowDiagramManager.updateWorkflow(workflow);
			
			if((_currentMDO is FlexJiraStep)){
				var oldstep:FlexJiraStep = _currentMDO as FlexJiraStep;
				var step:FlexJiraStep = workflow.getStep(oldstep.id);
				step.metaAttributes = _savedData;
				
				var node:Node = _currentDO as Node;
				node.data = step;
			} else if((_currentMDO is FlexJiraAction)){
				var oldaction:FlexJiraAction = _currentMDO as FlexJiraAction;
				var action:FlexJiraAction = workflow.getAction(oldaction.id);
				action.metaAttributes = _savedData;
				
				var edge:Edge = _currentDO as Edge;
				edge.data = action;
			}
			
			_currentMDO = null;
			_currentDO = null;
			MDIDialogUtils.removeModalDialog(jiraProgressDialog);
		}
		
	}
}