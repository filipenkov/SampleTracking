package com.sysbliss.jira.workflow.manager
{
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.data.Node;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
	
	import mx.collections.ArrayCollection;
	
	public interface WorkflowDiagramManager
	{
		function getAllWorkflows():ArrayCollection;
		function getAllWorkflowNames():ArrayCollection;
		function getOpenWorkflows():ArrayCollection;
		function getOpenWorkflowIds():ArrayCollection;
		function getClosedWorkflows():ArrayCollection;
		function addOpenWorkflow(workflow:FlexJiraWorkflow,diagram:Diagram):void;
		function removeOpenWorkflow(workflow:FlexJiraWorkflow):void;
		function getWorkflowForDiagram(diagram:Diagram):FlexJiraWorkflow;
		function getDiagramForWorkflow(workflow:FlexJiraWorkflow):Diagram;
		function getCurrentDiagram():Diagram;
		function setCurrentDiagram(d:Diagram):void;
		function getCurrentWorkflow():FlexJiraWorkflow;
		function setCurrentWorkflow(fjw:FlexJiraWorkflow):void;
		function updateWorkflow(newWorkflow:FlexJiraWorkflow):void;
		function getEditableWorkflowByName(name:String,isDraft:Boolean):FlexJiraWorkflow;
		function getWorkflowByName(name:String,isDraft:Boolean):FlexJiraWorkflow;
		function getWorkflowByUID(name:String):FlexJiraWorkflow;
		function removeWorkflow(fjw:FlexJiraWorkflow):void;
		function setWorkflowList(list:ArrayCollection):void;
		function setCurrentDiagramQuietly(d:Diagram):void;
		function setCurrentWorkflowQuietly(fjw:FlexJiraWorkflow):void;
		function removeDiagramQuietly(d:Diagram):void;
		function removeWorkflowQuietly(fjw:FlexJiraWorkflow):void;
		function getNodeForStepId(stepId:int,d:Diagram):Node;
	}
}