package com.sysbliss.jira.workflow.controller
{
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.Edge;
import com.sysbliss.diagram.event.DiagramEvent;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraActionImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflowImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.workflow.event.EventTypes;
import com.sysbliss.jira.workflow.event.WorkflowEvent;
import com.sysbliss.jira.workflow.layout.LayoutExporter;
import com.sysbliss.jira.workflow.manager.CommonActionManager;
import com.sysbliss.jira.workflow.manager.CommonActionManagerFactory;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.process.Request;
import com.sysbliss.jira.workflow.process.transition.WorkflowAddTransitionProcessChain;
import com.sysbliss.jira.workflow.process.transition.WorkflowAddTransitionRequest;
import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
import com.sysbliss.jira.workflow.service.JiraWorkflowService;
import com.sysbliss.jira.workflow.ui.dialog.AddTransitionDialog;
import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
import com.sysbliss.jira.workflow.utils.SnapshotUtil;

import flash.display.DisplayObject;

import flash.display.InteractiveObject;
import flash.events.MouseEvent;

import mx.resources.IResourceManager;
import mx.resources.ResourceManager;
import mx.rpc.events.ResultEvent;

import org.swizframework.Swiz;
import org.swizframework.controller.AbstractController;

public class DiagramAddTransitionController extends WorkflowAbstractController
{
    private var _currentEdge:Edge;

    [Autowire]
    public var addTransitionDialog:AddTransitionDialog;

    [Autowire]
    public var jiraService:JiraWorkflowService;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    [Autowire]
    public var jiraProgressDialog:JiraProgressDialog;

    [Autowire]
    public var addChain:WorkflowAddTransitionProcessChain;

    [Autowire]
    public var layoutExporter:LayoutExporter;

    public function DiagramAddTransitionController()
    {
        super();
    }

    [Mediate(event="${eventTypes.DIAGRAM_CREATED}", properties="diagram")]
    public function addDiagramEventListeners(d:Diagram):void
    {
        d.addEventListener(DiagramEvent.EDGE_CREATED, onDiagramEdgeCreated);
    }

    private function onDiagramEdgeCreated(e:DiagramEvent):void
    {
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowForDiagram(e.data.diagram as Diagram);
        _currentEdge = e.data.edge as Edge;

        if (workflow.isDraftWorkflow)
        {
            var parentWorkflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowByName(workflow.name, false);
            if (!parentWorkflow.isLoaded)
            {
                Swiz.dispatchEvent(new WorkflowEvent(EventTypes.LOAD_WORKFLOW, parentWorkflow, WorkflowEvent.TRANSITION_ADD));
                return;
            }
        }

        processTransitionAdd(workflow, WorkflowEvent.TRANSITION_ADD);
    }

    [Mediate(event="${eventTypes.WORKFLOW_LOADED}", properties="workflow,reason")]
    public function processTransitionAdd(fjw:FlexJiraWorkflow, reason:String):void
    {
        if (reason == WorkflowEvent.TRANSITION_ADD)
        {
            MDIDialogUtils.removeModalDialog(jiraProgressDialog);
            //the workflow passed in might be the parent, let's grab from manager to be sure we have the draft if needed
            var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

            var addRequest:Request = new WorkflowAddTransitionRequest(workflow, _currentEdge);
            addChain.processRequest(addRequest);
        }
    }

    [Mediate(event="${eventTypes.ADD_TRANSITION_CANCELLED}")]
    public function cancelAddTransition():void
    {
        workflowDiagramManager.getCurrentDiagram().forceDeleteEdge(_currentEdge);
    }

    [Mediate(event="${eventTypes.DO_ADD_TRANSITION}", properties="data")]
    public function doAddTransition(data:Object):void
    {
        MDIDialogUtils.removeModalDialog(addTransitionDialog);
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_adding_transition");

        var fromStep:FlexJiraStep = _currentEdge.startNode.data as FlexJiraStep;
        var toStep:FlexJiraStep = _currentEdge.endNode.data as FlexJiraStep;
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

        executeServiceCall(jiraService.addTransition(data.name, data.desc, data.view, fromStep, toStep, workflow), onTransitionAdded, DefaultFaultHandler.handleFault, [workflow]);
    }

    [Mediate(event="${eventTypes.DO_CLONE_TRANSITION}", properties="data")]
    public function doCloneTransition(data:Object):void
    {
        MDIDialogUtils.removeModalDialog(addTransitionDialog);
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_adding_transition");

        var fromStep:FlexJiraStep = _currentEdge.startNode.data as FlexJiraStep;
        var toStep:FlexJiraStep = _currentEdge.endNode.data as FlexJiraStep;
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

        executeServiceCall(jiraService.cloneTransition(data.name, data.desc, data.actionIdToClone, fromStep, toStep, workflow), onTransitionAdded, DefaultFaultHandler.handleFault, [workflow]);
    }

    [Mediate(event="${eventTypes.DO_REUSE_TRANSITION}", properties="data")]
    public function doReuseTransition(data:Object):void
    {
        MDIDialogUtils.removeModalDialog(addTransitionDialog);
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_adding_transition");

        var fromStep:FlexJiraStep = _currentEdge.startNode.data as FlexJiraStep;
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

        executeServiceCall(jiraService.useCommonTransition(data.actionIdToReuse, fromStep, workflow), onTransitionAdded, DefaultFaultHandler.handleFault, [workflow]);
    }

    private function onTransitionAdded(e:ResultEvent, fjw:FlexJiraWorkflow):void
    {
        var workflow:FlexJiraWorkflow = e.result.workflow as FlexJiraWorkflowImpl;
        var action:FlexJiraAction = e.result.action as FlexJiraActionImpl;

        workflow.uid = fjw.uid;
        workflowDiagramManager.updateWorkflow(workflow);

        var diagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        _currentEdge.data = action;

        if (action.isCommon)
        {
            var actionManager:CommonActionManager = CommonActionManagerFactory.getActionManager(workflow);
            actionManager.addCommonActionUI(action,_currentEdge.uiEdge);
        }

        var jwdLayout:JWDLayout = getLayout();

        if (workflow.isDraftWorkflow) {
            executeServiceCall(jiraService.saveDraftLayout(workflow.name, jwdLayout), onLayoutSaved, DefaultFaultHandler.handleFault);
        } else {
            executeServiceCall(jiraService.saveActiveLayout(workflow.name, jwdLayout), onLayoutSaved, DefaultFaultHandler.handleFault);
        }

    }

    private function onLayoutSaved(e:ResultEvent):void {
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
    }

    private function getLayout():JWDLayout {
        var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        var graphBounds:LayoutRect = SnapshotUtil.getGraphBounds(DisplayObject(currentDiagram));

        return layoutExporter.export(currentDiagram, currentDiagram.getRootUINodes(), graphBounds, workflow, false);
    }


}
}