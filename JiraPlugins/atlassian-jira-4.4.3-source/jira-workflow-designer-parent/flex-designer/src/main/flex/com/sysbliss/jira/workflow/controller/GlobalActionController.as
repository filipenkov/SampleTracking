package com.sysbliss.jira.workflow.controller
{
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraActionImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflowImpl;
import com.sysbliss.jira.workflow.event.EventTypes;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
import com.sysbliss.jira.workflow.service.JiraWorkflowService;
import com.sysbliss.jira.workflow.ui.dialog.AddGlobalActionDialog;
import com.sysbliss.jira.workflow.ui.dialog.EditGlobalActionDialog;
import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
import com.sysbliss.jira.workflow.utils.StatusUtils;

import mx.collections.ArrayCollection;
import mx.controls.Alert;
import mx.events.CloseEvent;
import mx.resources.IResourceManager;
import mx.resources.ResourceManager;
import mx.rpc.events.ResultEvent;

import org.swizframework.Swiz;
import org.swizframework.controller.AbstractController;

public class GlobalActionController extends WorkflowAbstractController
{
    private var _listProvider:ArrayCollection;


    [Autowire]
    public var jiraProgressDialog:JiraProgressDialog;

    [Autowire]
    public var jiraService:JiraWorkflowService;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    [Autowire]
    public var statusUtils:StatusUtils;

    [Autowire]
    public var addGlobalActionDialog:AddGlobalActionDialog;

    [Autowire]
    public var editGlobalActionDialog:EditGlobalActionDialog;

    private var _actionToDelete:int;

    private var _actionToUpdate:FlexJiraAction;

    public function GlobalActionController()
    {
        super();
    }


    public function getListIcon(item:Object):Class
    {
        var action:FlexJiraAction;
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        var step:FlexJiraStep = null;
        var statusId:String = null;
        if (item is FlexJiraAction)
        {
            action = item as FlexJiraAction;
        } else
        {
            action = item.data as FlexJiraAction;
        }

        if (action != null)
        {
            step = workflow.getStep(action.unconditionalResult.stepId);
            if (step != null)
            {
                statusId = step.linkedStatus;
            }
        }

        return statusUtils.getIconForStatusId(statusId);

    }

    [Mediate(event="${eventTypes.NEW_GLOBAL_ACTION}")]
    public function showAddGlobalAction(o:Object):void
    {

        addGlobalActionDialog.setWorkflow(workflowDiagramManager.getCurrentWorkflow());
        MDIDialogUtils.popModalDialog(addGlobalActionDialog);
    }

    [Mediate(event="${eventTypes.DO_ADD_GLOBAL_ACTION}", properties="data")]
    public function doAddTransition(data:Object):void
    {
        MDIDialogUtils.removeModalDialog(addGlobalActionDialog);
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_adding_transition");

        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

        //TODO: change fault handler to remove edge if fault
        executeServiceCall(jiraService.addGlobalTransition(data.name, data.desc, data.resultId, data.view, workflow), onTransitionAdded, DefaultFaultHandler.handleFault, [workflow]);
    }

    [Mediate(event="${eventTypes.DO_CLONE_GLOBAL_ACTION}", properties="data")]
    public function doCloneTransition(data:Object):void
    {
        MDIDialogUtils.removeModalDialog(addGlobalActionDialog);
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_adding_transition");

        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

        //TODO: change fault handler to remove edge if fault
        executeServiceCall(jiraService.cloneGlobalTransition(data.name, data.desc, data.actionIdToClone, workflow), onTransitionAdded, DefaultFaultHandler.handleFault, [workflow]);
    }

    private function onTransitionAdded(e:ResultEvent, fjw:FlexJiraWorkflow):void
    {
        var workflow:FlexJiraWorkflow = e.result.workflow as FlexJiraWorkflowImpl;
        var action:FlexJiraAction = e.result.action as FlexJiraActionImpl;

        workflow.uid = fjw.uid;
        workflowDiagramManager.updateWorkflow(workflow);


        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
        updateListProviderForWorkflow(workflow);
    }

    [Mediate(event="${eventTypes.CURRENT_WORKFLOW_CHANGED}", properties="workflow")]
    public function updateListProviderForWorkflow(workflow:FlexJiraWorkflow):void
    {
        var dp:ArrayCollection = new ArrayCollection();
        var dataObject:Object;
        for each(var action:FlexJiraAction in workflow.globalActions)
        {
            dataObject = new Object();
            dataObject.data = action;
            dataObject.label = action.name + " (" + action.id + ")";
            dp.addItem(dataObject);
        }
        this.listProvider = dp;
        Swiz.dispatch(EventTypes.GLOBAL_ACTIONS_REFRESHED);
    }

    [Mediate(event="${eventTypes.ALL_WORKFLOWS_CLOSED}")]
    public function updateStandardListProvider():void
    {
        var dp:ArrayCollection = new ArrayCollection();

        this.listProvider = dp;
        Swiz.dispatch(EventTypes.GLOBAL_ACTIONS_REFRESHED);
    }

    [Mediate(event="${eventTypes.DELETE_GLOBAL_ACTION}", properties="data")]
    public function deleteGlobalAction(data:Object):void
    {
        _actionToDelete = data.actionId;
        var alertMessage:String = niceResourceManager.getString("json", "workflow.designer.message.delete.global.transition");
        var alertTitle:String = niceResourceManager.getString("json", "workflow.designer.title.delete.global.transition");
        Alert.show(alertMessage, alertTitle, Alert.OK | Alert.CANCEL, null, confirmDeleteWorkflowHandler, null, Alert.OK);
    }

    private function confirmDeleteWorkflowHandler(e:CloseEvent):void
    {
        if (e.detail == Alert.OK)
        {
            doDeleteGlobalAction();
        }
    }

    private function doDeleteGlobalAction():void
    {
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_deleting_selections");
        executeServiceCall(jiraService.deleteGlobalAction(_actionToDelete, workflowDiagramManager.getCurrentWorkflow()), onActionDeleted, DefaultFaultHandler.handleFault, [workflowDiagramManager.getCurrentWorkflow()]);
    }

    private function onActionDeleted(e:ResultEvent, fjw:FlexJiraWorkflow):void
    {
        var workflow:FlexJiraWorkflow = e.result as FlexJiraWorkflowImpl;

        workflow.uid = fjw.uid;
        workflowDiagramManager.updateWorkflow(workflow);
        _actionToDelete = -1;
        updateListProviderForWorkflow(workflow);

        MDIDialogUtils.removeModalDialog(jiraProgressDialog);

    }

    [Mediate(event="${eventTypes.EDIT_GLOBAL_ACTION}", properties="data")]
    public function editGlobalAction(data:Object):void
    {
        _actionToUpdate = data.action;
        editGlobalActionDialog.setWorkflow(workflowDiagramManager.getCurrentWorkflow());
        editGlobalActionDialog.setAction(data.action);
        MDIDialogUtils.popModalDialog(editGlobalActionDialog);
    }

    [Mediate(event="${eventTypes.DO_EDIT_GLOBAL_ACTION}", properties="data")]
    public function doEditTransition(data:Object):void
    {
        MDIDialogUtils.removeModalDialog(editGlobalActionDialog);
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_updating_transition", [_actionToUpdate.name]);

        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        executeServiceCall(jiraService.updateGlobalAction(_actionToUpdate, data.name, data.desc, data.dest, data.view, workflow), onTransitionUpdated, DefaultFaultHandler.handleFault, [workflow]);
    }

    private function onTransitionUpdated(e:ResultEvent, fjw:FlexJiraWorkflow):void
    {
        var workflow:FlexJiraWorkflow = e.result as FlexJiraWorkflowImpl;
        var oldAction:FlexJiraAction = _actionToUpdate;

        workflow.uid = fjw.uid;
        workflowDiagramManager.updateWorkflow(workflow);

        var action:FlexJiraAction = workflow.getAction(oldAction.id);

        _actionToUpdate = null;

        updateListProviderForWorkflow(workflow);
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
    }

    [Bindable]
    public function get listProvider():ArrayCollection
    {
        return _listProvider;
    }

    public function set listProvider(dp:ArrayCollection):void
    {
        _listProvider = dp;
    }
}
}