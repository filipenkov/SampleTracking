package com.sysbliss.jira.workflow.controller
{
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.Edge;
import com.sysbliss.diagram.data.Node;
import com.sysbliss.diagram.event.DiagramEvent;
import com.sysbliss.diagram.ui.UIEdge;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflowImpl;
import com.sysbliss.jira.workflow.event.EventTypes;
import com.sysbliss.jira.workflow.event.WorkflowEvent;
import com.sysbliss.jira.workflow.manager.CommonActionManager;
import com.sysbliss.jira.workflow.manager.CommonActionManagerFactory;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.process.Request;
import com.sysbliss.jira.workflow.process.transition.WorkflowEditTransitionProcessChain;
import com.sysbliss.jira.workflow.process.transition.WorkflowEditTransitionRequest;
import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
import com.sysbliss.jira.workflow.service.JiraWorkflowService;
import com.sysbliss.jira.workflow.ui.dialog.EditTransitionDialog;
import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
import com.sysbliss.jira.workflow.utils.MDIDialogUtils;

import flash.geom.Point;

import mx.core.UIComponent;

import mx.rpc.events.ResultEvent;

import org.swizframework.Swiz;

public class DiagramEditTransitionController extends WorkflowAbstractController
{
    private var _currentEdge:Edge;

    [Autowire]
    public var editTransitionDialog:EditTransitionDialog;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    [Autowire]
    public var jiraProgressDialog:JiraProgressDialog;

    [Autowire]
    public var editChain:WorkflowEditTransitionProcessChain;

    [Autowire]
    public var jiraService:JiraWorkflowService;


    public function DiagramEditTransitionController()
    {
        super();
    }

    [Mediate(event="${eventTypes.DIAGRAM_CREATED}", properties="diagram")]
    public function addDiagramEventListeners(d:Diagram):void
    {
        d.addEventListener(DiagramEvent.EDGE_DOUBLE_CLICK, onEdgeDoubleClickOrEnter);
        d.addEventListener(DiagramEvent.EDGE_ENTER_KEY, onEdgeDoubleClickOrEnter);
    }

    private function onEdgeDoubleClickOrEnter(e:DiagramEvent):void
    {
        var diagram:Diagram = e.data.diagram as Diagram;
        var edge:Edge = e.data.edge as Edge;
        editAction(diagram, edge);
    }

    [Mediate(event="${eventTypes.SHOW_EDIT_ACTION}", properties="diagram,edge")]
    public function editAction(d:Diagram, e:Edge):void
    {
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowForDiagram(d);
        _currentEdge = e;
        var action:FlexJiraAction = _currentEdge.data as FlexJiraAction;

        processEditTransition(workflow, e);
    }

    public function processEditTransition(fjw:FlexJiraWorkflow, edge:Edge):void
    {

        var editRequest:Request = new WorkflowEditTransitionRequest(fjw, edge);
        editChain.processRequest(editRequest);
    }

    [Mediate(event="${eventTypes.DO_EDIT_TRANSITION}", properties="data")]
    public function doEditTransition(data:Object):void
    {
        MDIDialogUtils.removeModalDialog(editTransitionDialog);
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_updating_transition", [_currentEdge.data.name]);

        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        var action:FlexJiraAction = _currentEdge.data as FlexJiraAction;
        executeServiceCall(jiraService.updateAction(action, data.name, data.desc, data.dest, data.view, workflow), onTransitionUpdated, DefaultFaultHandler.handleFault, [workflow]);
    }

    private function onTransitionUpdated(e:ResultEvent, fjw:FlexJiraWorkflow):void
    {
        var workflow:FlexJiraWorkflow = e.result as FlexJiraWorkflowImpl;
        var oldAction:FlexJiraAction = _currentEdge.data as FlexJiraAction;

        workflow.uid = fjw.uid;
        workflowDiagramManager.updateWorkflow(workflow);
        var diagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        var uiEdge:UIEdge = diagram.getUIEdge(_currentEdge.id);
        var action:FlexJiraAction = workflow.getAction(oldAction.id);

        uiEdge.edge.data = action;
        var endNode:Node = workflowDiagramManager.getNodeForStepId(action.unconditionalResult.stepId, diagram);
        if (action.isCommon)
        {
            var actionManager:CommonActionManager = CommonActionManagerFactory.getActionManager(workflow);
            actionManager.updateCommonActionUI(action,endNode);
        }


        _currentEdge.endNode = endNode;
        var newEndPoint:Point = new Point(endNode.uiNode.centerPoint.x,endNode.uiNode.centerPoint.y);
        uiEdge.moveEndPoint(newEndPoint.x, newEndPoint.y);
        UIComponent(uiEdge).invalidateDisplayList();
        UIComponent(uiEdge).validateNow();
        
        Swiz.dispatchEvent(new WorkflowEvent(EventTypes.WORKFLOW_TRANSITION_UPDATED, workflow));
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
    }

}
}