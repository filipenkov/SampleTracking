package com.sysbliss.jira.workflow.manager
{
	
	import com.sysbliss.collections.HashMap;
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.data.Node;
	import com.sysbliss.jira.workflow.event.EventTypes;
	import com.sysbliss.jira.workflow.event.JiraDiagramEvent;
	import com.sysbliss.jira.workflow.event.WorkflowEvent;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	import com.sysbliss.jira.workflow.service.JiraWorkflowService;

    import mx.collections.ArrayCollection;
	
	import org.swizframework.Swiz;

	public class WorkflowDiagramManagerImpl implements WorkflowDiagramManager
	{
		
		public var _workflows:ArrayCollection;
		private var _openWorkflowIds:Array;
		private var _workflowDiagramMap:HashMap;
		private var _currentDiagram:Diagram;
		private var _currentWorkflow:FlexJiraWorkflow;
		private var _tmpNewWorkflowList:Array;
        private var _workflowActionManagerMap:HashMap;
		
		[Autowire]
		public var jiraService:JiraWorkflowService;

		public function WorkflowDiagramManagerImpl()
		{
			_workflowDiagramMap = new HashMap();
			_openWorkflowIds = new Array();
			_workflows = new ArrayCollection();
            _workflowActionManagerMap = new HashMap();
		}
		
		[Mediate(event="${eventTypes.JIRA_WORKFLOW_LIST_RETRIEVED}", properties="workflows")]
		public function setWorkflowList(list:ArrayCollection):void {
			for each(var workflow:FlexJiraWorkflow in list){
				updateWorkflow(workflow);
			}
			
			_tmpNewWorkflowList = list.source;
			var removeList:Array = _workflows.source.filter(workflowsToRemoveFilter);
			for each(var remove:FlexJiraWorkflow in removeList){
				removeWorkflow(remove);
			}
			
			Swiz.dispatch(EventTypes.WORKFLOW_MANAGER_INITIALIZED);
		}
		
		public function getAllWorkflows():ArrayCollection
		{
			return this._workflows;
		}
		
		public function getAllWorkflowNames():ArrayCollection {
			var names:Array = new Array();
			names = _workflows.source.map(workflowNameMapper);
			return new ArrayCollection(names);
		}
		
		public function getOpenWorkflows():ArrayCollection
		{
			return new ArrayCollection(_workflows.source.filter(openWorkflowsFilter));
		}
		
		public function getOpenWorkflowIds():ArrayCollection
		{
			return new ArrayCollection(_openWorkflowIds);
		}
		
		public function getClosedWorkflows():ArrayCollection
		{
			return new ArrayCollection(_workflows.source.filter(closedWorkflowsFilter));
		}
		
		
		public function addOpenWorkflow(workflow:FlexJiraWorkflow, diagram:Diagram):void
		{
			//log.debug("adding wf to open list: " + workflow.name);
			if(!_workflowDiagramMap.keyExists(workflow.uid)){
				_workflowDiagramMap.put(workflow.uid,diagram);
			}
			
			if(_openWorkflowIds.indexOf(workflow.uid) < 0){
				_openWorkflowIds.push(workflow.uid);
			}
		}
		
		public function removeOpenWorkflow(workflow:FlexJiraWorkflow):void
		{
			_workflowDiagramMap.remove(workflow.uid);
			
			var idIndex:int = _openWorkflowIds.indexOf(workflow.uid);
			if(idIndex > -1){
				_openWorkflowIds.splice(idIndex,1);
			}

            CommonActionManagerFactory.removeActionManager(workflow);
		}
		
		public function getWorkflowForDiagram(diagram:Diagram):FlexJiraWorkflow
		{
			var i:int;
			var workflow:FlexJiraWorkflow;
			var tmpDiagram:Diagram;
			for(i=0;i<_workflowDiagramMap.values.length;i++){
				tmpDiagram = _workflowDiagramMap.values[i] as Diagram;
				if(tmpDiagram === diagram){
					workflow = getWorkflowByUID(_workflowDiagramMap.getKeyAt(i) as String);
					break;
				}
			}
			return workflow;
		}
		
		public function getDiagramForWorkflow(workflow:FlexJiraWorkflow):Diagram
		{
			var keys:Array = _workflowDiagramMap.getKeys();
			var i:int;
			var mapWorkflow:FlexJiraWorkflow;
			var diagram:Diagram;
			for(i=0;i<keys.length;i++){
				mapWorkflow = getWorkflowByUID(_workflowDiagramMap.getKeyAt(i) as String);
				if(mapWorkflow.uid == workflow.uid){
					diagram = _workflowDiagramMap.getValue(mapWorkflow.uid) as Diagram;
					break;
				}
			}
			return diagram;
		}
		
		public function getCurrentDiagram():Diagram {
			return _currentDiagram;
		}
		
		[Mediate(event="${eventTypes.CURRENT_DIAGRAM_CHANGED}", properties="diagram")]
		[Mediate(event="${eventTypes.DIAGRAM_INITIALIZED}", properties="diagram")]
		public function setCurrentDiagram(d:Diagram):void {
			if(_workflowDiagramMap.valueExists(d)){
				setCurrentDiagramQuietly(d);
				Swiz.dispatchEvent(new WorkflowEvent(EventTypes.CURRENT_WORKFLOW_CHANGED,_currentWorkflow));
			}
		}
		
		public function setCurrentDiagramQuietly(d:Diagram):void {
			if(_workflowDiagramMap.valueExists(d)){
				_currentDiagram = d;
				_currentWorkflow = getWorkflowForDiagram(d);
			}
		}
		
		public function getCurrentWorkflow():FlexJiraWorkflow {
			return _currentWorkflow;
		}
		
		public function setCurrentWorkflow(fjw:FlexJiraWorkflow):void {
			if(_workflowDiagramMap.keyExists(fjw)){
				setCurrentWorkflowQuietly(fjw);
				Swiz.dispatchEvent(new WorkflowEvent(EventTypes.CURRENT_WORKFLOW_CHANGED,_currentWorkflow));
			}
		}
		
		public function setCurrentWorkflowQuietly(fjw:FlexJiraWorkflow):void {
			if(_workflowDiagramMap.keyExists(fjw)){
				_currentDiagram = _workflowDiagramMap.getValue(fjw.uid);
				_currentWorkflow = fjw;
			}
		}
		
		public function updateWorkflow(newWorkflow:FlexJiraWorkflow):void {
			var oldWorkflow:FlexJiraWorkflow = getWorkflowByName(newWorkflow.name,newWorkflow.isDraftWorkflow);
			
			if(oldWorkflow){
				newWorkflow.uid = oldWorkflow.uid;
				CommonActionManagerFactory.updateWorkflow(oldWorkflow,newWorkflow);

				if(newWorkflow.isLoaded){
					if(_currentWorkflow && _currentWorkflow.uid == oldWorkflow.uid){
						_currentWorkflow = newWorkflow;
					}
					
					//log.debug("update check map...");
					if(_workflowDiagramMap.keyExists(oldWorkflow.uid)){
						//log.debug("update found in map");
						var diagram:Diagram = _workflowDiagramMap.getValue(oldWorkflow.uid);
						_workflowDiagramMap.remove(oldWorkflow.uid);
						_workflowDiagramMap.put(newWorkflow.uid,diagram);
					}
					
					var i:int = _workflows.getItemIndex(oldWorkflow);
					if(i > -1){
						_workflows.removeItemAt(i);
						_workflows.addItemAt(newWorkflow,i);
					} else {
						_workflows.addItem(newWorkflow);
					}
				}
			} else {
				_workflows.addItem(newWorkflow);
			}
			
		}
		
		[Mediate(event="${eventTypes.DIAGRAM_TAB_CLOSED}", properties="diagram")]
		public function removeDiagramQuietly(d:Diagram):void {
			var fjw:FlexJiraWorkflow = getWorkflowForDiagram(d);
			removeWorkflowQuietly(fjw);
		}
		
		public function removeWorkflow(fjw:FlexJiraWorkflow):void {
			//log.debug("attempting to remove workflow: " + fjw.name);
			var oldWorkflow:FlexJiraWorkflow = getWorkflowByUID(fjw.uid);
			if(oldWorkflow){
				var diagram:Diagram;
				if(_workflowDiagramMap.keyExists(oldWorkflow.uid)){
					diagram = _workflowDiagramMap.getValue(oldWorkflow.uid);
				}
				
				removeWorkflowQuietly(fjw);
				
				//log.debug("dispatching workflow removed");
				Swiz.dispatchEvent(new WorkflowEvent(EventTypes.WORKFLOW_REMOVED_FROM_MANAGER,oldWorkflow));
				if(diagram){
					//log.debug("dispatching diagram removed");
					Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.DIAGRAM_REMOVED_FROM_MANAGER,diagram));
				}
			}
			
		}
		
		public function removeWorkflowQuietly(fjw:FlexJiraWorkflow):void {
			//log.debug("attempting to remove workflow: " + fjw.name);
			var oldWorkflow:FlexJiraWorkflow = getWorkflowByUID(fjw.uid);
			
			if(oldWorkflow){
				var diagram:Diagram;
				if(_currentWorkflow && _currentWorkflow.uid == oldWorkflow.uid){
					_currentWorkflow = null;
				}
				
				//log.debug("checking diagram map...");
				if(_workflowDiagramMap.keyExists(oldWorkflow.uid)){
					//log.debug("found workflow to remove in diagram map");
					diagram = _workflowDiagramMap.getValue(oldWorkflow.uid);
					_workflowDiagramMap.remove(oldWorkflow.uid);
				}
				
				if(diagram && _currentDiagram == diagram){
					_currentDiagram = null;
				}
				
				var i:int = _workflows.getItemIndex(oldWorkflow);
				if(i > -1){
					_workflows.removeItemAt(i);
				}
				
				var openIndex:int = _openWorkflowIds.indexOf(oldWorkflow.uid);
				if(openIndex > -1){
					_openWorkflowIds.splice(openIndex,1);
				}

                CommonActionManagerFactory.removeActionManager(oldWorkflow);
			}
			
		}
		
		public function getEditableWorkflowByName(name:String,isDraft:Boolean):FlexJiraWorkflow {
			var editableWorkflow:FlexJiraWorkflow;
			for each(var workflow:FlexJiraWorkflow in _workflows){
				if(workflow.name.toLowerCase() == name.toLowerCase() && workflow.isEditable && workflow.isDraftWorkflow == isDraft) {
					editableWorkflow = workflow;
					break;
				}
			}
			return editableWorkflow;
		}
		
		public function getWorkflowByName(name:String,isDraft:Boolean):FlexJiraWorkflow {
			var foundWorkflow:FlexJiraWorkflow;
			for each(var workflow:FlexJiraWorkflow in _workflows){
				if(workflow.name.toLowerCase() == name.toLowerCase() && workflow.isDraftWorkflow == isDraft) {
					foundWorkflow = workflow;
					break;
				}
			}
			return foundWorkflow;
		}
		
		public function getWorkflowByUID(uid:String):FlexJiraWorkflow {
			var foundWorkflow:FlexJiraWorkflow;
			for each(var workflow:FlexJiraWorkflow in _workflows){
				if(workflow.uid == uid) {
					foundWorkflow = workflow;
					break;
				}
			}
			return foundWorkflow;
		}
		
		public function getNodeForStepId(stepId:int,diagram:Diagram):Node {
			var nodes:Vector.<Node> = diagram.getNodes();
			var i:int;
			var node:Node;
			var step:FlexJiraStep;
			var returnNode:Node;
			for(i=0;i<nodes.length;i++){
				node = nodes[i];
				step = node.data as FlexJiraStep;
				if(step && step.id == stepId){
					returnNode = node;
					break;
				}
			}
			return returnNode;
		}
		
		private function openWorkflowsFilter(element:*,index:int,arr:Array):Boolean {
			var fjw:FlexJiraWorkflow = element as FlexJiraWorkflow;
			
			return _openWorkflowIds.indexOf(fjw.uid) > -1;	
		}
		
		private function workflowsToRemoveFilter(element:*,index:int,arr:Array):Boolean {
			var fjw:FlexJiraWorkflow = element as FlexJiraWorkflow;
			var remove:Boolean = true;
			for each(var newWorkflow:FlexJiraWorkflow in _tmpNewWorkflowList){
				if(newWorkflow.uid == fjw.uid){
					remove = false;
					break;
				}
			}
			return remove;
		}
		
		private function closedWorkflowsFilter(element:*,index:int,arr:Array):Boolean {
			var fjw:FlexJiraWorkflow = element as FlexJiraWorkflow;
			//log.debug("checking if " + fjw.name + " [" + fjw.uid + "] is closed");
			//log.debug("index in open workflows: " + _openWorkflowIds.indexOf(fjw.uid));
			//log.debug("open workflows: " + _openWorkflowIds.toString());
			return _openWorkflowIds.indexOf(fjw.uid) < 0;	
		}
		
		private function workflowNameMapper(element:*,index:int,arr:Array):String {
			var wf:FlexJiraWorkflow = element as FlexJiraWorkflow;
			return wf.name;
		}

    }
}