package com.sysbliss.jira.workflow.event
{
	import com.sysbliss.diagram.event.DiagramEvent;
	
	public class EventTypes
	{
		public static const APPLICATION_INITIALIZED:String = "ApplicationInitializer::applicationInitialized";
		public static const WORKFLOW_LOADED:String = "WorkflowEvent::WorkflowLoaded";
		public static const OPEN_WORKFLOWS:String = "WorkflowListEvent::openWorkflows";
		public static const LOAD_WORKFLOW:String = "WorkflowEvent::loadWorkflows";
		public static const COPY_WORKFLOW:String = "WorkflowEvent::copyWorkflow";
		public static const NEW_WORKFLOW:String = "WorkflowEvent::newWorkflow";
		public static const CREATE_DRAFT_WORKFLOW:String = "WorkflowEvent::createDraftWorkflow";
		public static const PUBLISH_DRAFT_WORKFLOW:String = "WorkflowEvent::publishDraftWorkflow";
		public static const DELETE_WORKFLOW:String = "WorkflowEvent::deleteWorkflow";
		public static const REFRESH_WORKFLOW_LIST:String = "WorkflowPanel::refreshWorkflowList";
		public static const LOAD_WORKFLOWS_COMPLETED:String = "OpenWorkflowController::loadWorkflowsCompleted";
		public static const ALL_WORKFLOWS_CLOSED:String = "DiagramTabsController::allWorkflowsClosed";
		
		public static const REFRESH_STATUS_LIST:String = "StatusPanel::refreshStatusList";
		public static const NEW_STATUS:String = "StatusPanel::editStatus";
		public static const SHOW_STATUS_EDITOR:String = "StatusPanel::showStatusEditor";
		
		public static const WORKFLOW_MANAGER_INITIALIZED:String = "WorkflowDiagramManager::initialized";
		public static const WORKFLOW_REMOVED_FROM_MANAGER:String = "WorkflowDiagramManager::workflowRemoved";
		public static const DIAGRAM_REMOVED_FROM_MANAGER:String = "WorkflowDiagramManager::diagramRemoved";
		
		public static const DIAGRAM_CREATED:String = "OpenWorkflowController::diagramCreated";
		public static const DIAGRAM_TAB_CLOSED:String = "JiraDiagramEvent::diagramTabClosed";
		public static const FOCUS_DIAGRAM:String = "OpenWorkflowController::focusDiagram";
		public static const DIAGRAM_INITIALIZED:String = "OpenWorkflowController::diagramInitialized";
		public static const CURRENT_DIAGRAM_CHANGED:String = "JiraWorkflowDesigner::currentDiagramChanged";
		public static const CURRENT_DIAGRAM_UPDATED:String = "JiraWorkflowDesigner::currentDiagramUpdated";
		public static const CURRENT_WORKFLOW_CHANGED:String = "WorkflowDiagramManager::currentWorkflowChanged";
		
		public static const JIRA_WORKFLOW_LIST_RETRIEVED:String = "JiraService:workflowListRetrieved";
		public static const JIRA_STATUS_LIST_RETRIEVED:String = "JiraService:statusListRetrieved";
		public static const JIRA_FIELD_SCREENS_RETRIEVED:String = "JiraService:fieldScreensRetrieved";
		
		public static const DIAGRAM_SELECTION_DELETE:String = DiagramEvent.SELECTIONS_DELETED;
		public static const CONFIRM_SELECTION_DELETE:String = "WorkflowEvent::confirmSelectionDelete";
		
		public static const ADD_STEP:String = "DataEvent::addStep";
        public static const TOGGLE_ISSUE_EDITABLE:String = "WorkflowEvent::toggleIssueEditable";
		public static const ADD_TRANSITION_CANCELLED:String = "Event::addTransitionCancelled";
		public static const WORKFLOW_OBJECTS_DELETED:String = "WorkflowEvent::objectsDeleted";
		public static const WORKFLOW_STEP_UPDATED:String = "WorkflowEvent::stepUpdated";
		public static const WORKFLOW_STEP_ADDED:String = "WorkflowEvent::stepAdded";
		public static const WORKFLOW_TRANSITION_UPDATED:String = "WorkflowEvent::transitionUpdated";
		public static const DO_WORKFLOW_COPY:String = "DataEvent::doWorkflowCopy";
		public static const DO_WORKFLOW_NEW:String = "DataEvent::doWorkflowNew";
		public static const DO_PUBLISH_DRAFT_WORKFLOW:String = "DataEvent::doPublishDraft";
		public static const DO_ADD_TRANSITION:String = "DataEvent::doAddTransition";
        public static const DO_CLONE_TRANSITION:String = "DataEvent::doCloneTransition";
        public static const DO_REUSE_TRANSITION:String = "DataEvent::doReuseTransition";
		public static const DO_EDIT_TRANSITION:String = "DataEvent::doEditTransition";
		public static const DO_STATUS_NEW:String = "DataEvent::doStatusNew";
		public static const DO_STATUS_SAVE:String = "DataEvent::doStatusSave";
		public static const DO_STATUS_DELETE:String = "DataEvent::doStatusDelete";
		public static const STATUSES_REFRESHED:String = "StatusController::statusesRefreshed";
		public static const DO_SAVE_METADATA:String = "DataEvent::doSaveMetadata";
		
		public static const DO_LOGIN:String = "DataEvent::doLogin";
		public static const LOGIN_SUCCESS:String = "DataEvent::loginSuccess";
		public static const USER_SESSION_RETRIEVED:String = "DataEvent::userSessionRetrieved";
		public static const USER_TOKEN_AVAILABLE:String = "DataEvent::userTokenAvailable";
		
		public static const SHOW_EDIT_PROPERTIES:String = "WorkflowMetadataEvent:showEditProperties";
		public static const SHOW_ACTION_CONDITIONS:String = "DiagramEdgeEvent:showActionConditions";
		public static const SHOW_ACTION_VALIDATORS:String = "DiagramEdgeEvent:showActionValidators";
		public static const SHOW_ACTION_FUNCTIONS:String = "DiagramEdgeEvent:showActionFunctions";
		public static const SHOW_EDIT_ACTION:String = "DiagramEdgeEvent:showEditAction";

        public static const GLOBAL_ACTIONS_REFRESHED:String = "GlobalActionController::globalActionsRefreshed";
        public static const GLOBAL_ACTIONS_CHANGED:String = "GlobalActionController::globalActionsRefreshed";
        public static const NEW_GLOBAL_ACTION:String = "GlobalActionController::newGlobalAction";
        public static const ADD_GLOBAL_ACTION_CANCELLED:String = "GlobalActionController::addGlobalActionCancelled";
        public static const EDIT_GLOBAL_ACTION:String = "GlobalActionController::editGlobalAction";
        public static const DELETE_GLOBAL_ACTION:String = "GlobalActionController::deleteGlobalAction";
        public static const DO_ADD_GLOBAL_ACTION:String = "GlobalActionController::doAddGlobalAction";
        public static const DO_EDIT_GLOBAL_ACTION:String = "GlobalActionController::doEditGlobalAction";
        public static const DO_CLONE_GLOBAL_ACTION:String = "GlobalActionController::doCloneGlobalAction";
		
		public static const STATUS_ICON_SELECTED:String = "DataEvent:ststausIconSelected";
		
		public static const SERVER_INFO_LOADED:String = "ServerInfoEvent:serverInfoLoaded";
		
	}
}