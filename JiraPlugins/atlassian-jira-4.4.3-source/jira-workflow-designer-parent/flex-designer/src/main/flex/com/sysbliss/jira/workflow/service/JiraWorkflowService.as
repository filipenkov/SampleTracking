package com.sysbliss.jira.workflow.service
{
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraDeleteRequest;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraMetadataContainer;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
	
	import mx.rpc.AsyncToken;
	
	public interface JiraWorkflowService
	{
		function getJiraServerInfo():AsyncToken;
		function getJiraUserPrefs():AsyncToken;
		function getWorkflows():AsyncToken;
		function loadWorkflow(wfd:FlexJiraWorkflow):AsyncToken;
		function getAllStatuses():AsyncToken;
		function getFieldScreens():AsyncToken;
		function addStep(step:FlexJiraStep,workflow:FlexJiraWorkflow,layout:JWDLayout):AsyncToken;
		function addTransition(name:String, desc:String, view:String, fjFromStep:FlexJiraStep, fjToStep:FlexJiraStep, fjw:FlexJiraWorkflow):AsyncToken;
        function cloneTransition(name:String, desc:String, actionIdToClone:int, fjFromStep:FlexJiraStep, fjToStep:FlexJiraStep, fjw:FlexJiraWorkflow):AsyncToken;
        function useCommonTransition(actionIdToReuse:int, fjFromStep:FlexJiraStep, fjw:FlexJiraWorkflow):AsyncToken;
        function addGlobalTransition(name:String, desc:String,resultId:int, view:String, fjw:FlexJiraWorkflow):AsyncToken;
        function cloneGlobalTransition(name:String, desc:String, actionIdToClone:int, fjw:FlexJiraWorkflow):AsyncToken;

		function deleteStepsAndActions(deleteRequest:FlexJiraDeleteRequest,workflow:FlexJiraWorkflow,layout:JWDLayout):AsyncToken;
		function copyWorkflow(newName:String, newDesc:String, fjw:FlexJiraWorkflow):AsyncToken;
    	function createDraftWorkflow(fjw:FlexJiraWorkflow):AsyncToken;
    	function deleteWorkflow(fjw:FlexJiraWorkflow):AsyncToken;
    	function publishDraftWorkflow(fjw:FlexJiraWorkflow, enableBackup:Boolean=false, backupName:String=""):AsyncToken;
    	function createNewWorkflow(name:String, desc:String):AsyncToken;
		function updateStep(step:FlexJiraStep,newName:String,newStatus:String,fjw:FlexJiraWorkflow):AsyncToken;
        function updateIssueEditable(step:FlexJiraStep,editable:Boolean,fjw:FlexJiraWorkflow):AsyncToken;
		function updateAction(action:FlexJiraAction,newName:String,newDesc:String,newDestStep:FlexJiraStep,newView:String,workflow:FlexJiraWorkflow):AsyncToken;
        function updateGlobalAction(action:FlexJiraAction,newName:String,newDesc:String,newDestStepId:int,newView:String,workflow:FlexJiraWorkflow):AsyncToken;
		function createNewStatus(name:String, desc:String, iconUrl:String):AsyncToken;
		function updateStatus(id:String, name:String, desc:String, iconUrl:String):AsyncToken;
		function deleteStatus(id:String):AsyncToken;
		function updateProperties(mdo:FlexJiraMetadataContainer,data:Object,workflow:FlexJiraWorkflow):AsyncToken;
		function login(username:String,password:String):AsyncToken;
		function getUserSession():AsyncToken;
		function loadLayout(fjw:FlexJiraWorkflow):AsyncToken;
		function calculateLayout(layout:JWDLayout):AsyncToken;
		function saveActiveLayout(name:String,layout:JWDLayout):AsyncToken;
		function saveDraftLayout(name:String,layout:JWDLayout):AsyncToken;

        function deleteGlobalAction(actionId:int,fjw:FlexJiraWorkflow):AsyncToken;

        function addAnnotationToWorkflow(workflow:FlexJiraWorkflow, annotation:WorkflowAnnotation,layout:JWDLayout):AsyncToken;
        function removeAnnotationFromWorkflow(workflow:FlexJiraWorkflow, annotation:WorkflowAnnotation,layout:JWDLayout):AsyncToken;
        function updateAnnotationForWorkflow(workflow:FlexJiraWorkflow, annotation:WorkflowAnnotation,layout:JWDLayout):AsyncToken;
	}
}