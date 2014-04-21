package com.sysbliss.jira.workflow.controller
{
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.Node;
import com.sysbliss.diagram.event.DiagramEvent;
import com.sysbliss.diagram.ui.DiagramUIObject;
import com.sysbliss.diagram.ui.UIEdge;
import com.sysbliss.diagram.ui.UIEdgeLabel;
import com.sysbliss.diagram.ui.selectable.Selectable;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraDeleteRequest;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStepImpl;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflowImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.workflow.event.EventTypes;
import com.sysbliss.jira.workflow.event.WorkflowEvent;
import com.sysbliss.jira.workflow.layout.LayoutExporter;
import com.sysbliss.jira.workflow.manager.CommonActionManager;
import com.sysbliss.jira.workflow.manager.CommonActionManagerFactory;
import com.sysbliss.jira.workflow.manager.JiraServerManager;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.process.Request;
import com.sysbliss.jira.workflow.process.selection.WorkflowSelectionDeleteProcessChain;
import com.sysbliss.jira.workflow.process.selection.WorkflowSelectionDeleteRequest;
import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
import com.sysbliss.jira.workflow.service.JiraWorkflowService;
import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
import com.sysbliss.jira.workflow.utils.JiraUtils;
import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
import com.sysbliss.jira.workflow.utils.SnapshotUtil;
import com.sysbliss.jira.workflow.utils.StatusUtils;

import flash.display.DisplayObject;

import flash.display.InteractiveObject;
import flash.geom.Point;

import mx.collections.ArrayCollection;
import mx.events.DragEvent;
import mx.resources.IResourceManager;
import mx.resources.ResourceManager;
import mx.rpc.events.ResultEvent;

import org.swizframework.Swiz;
import org.swizframework.controller.AbstractController;

public class DiagramController extends WorkflowAbstractController
{
    private var _diagramDropPoint:Point;
    private var _droppedObject:Object;

    private var _diagramSelections:ArrayCollection;
    private var _jiraDeleteRequest:FlexJiraDeleteRequest;
    private var _isDeleting:Boolean;

    [Autowire]
    public var jiraService:JiraWorkflowService;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    [Autowire]
    public var jiraServerManager:JiraServerManager;

    [Autowire]
    public var jiraProgressDialog:JiraProgressDialog;

    [Autowire]
    public var deleteChain:WorkflowSelectionDeleteProcessChain;

    [Autowire]
    public var layoutExporter:LayoutExporter;

    [Autowire]
		public var statusUtils:StatusUtils;

    public function DiagramController()
    {
        super();
        _isDeleting = false;
    }

    [Mediate(event="${eventTypes.DIAGRAM_CREATED}", properties="diagram")]
    public function addDiagramEventListeners(d:Diagram):void
    {
        d.addEventListener(DiagramEvent.SELECTIONS_DELETED, diagramSelectionDeleteHandler);
        d.addEventListener(DragEvent.DRAG_DROP, diagramDragDropHandler);
    }

    private function diagramSelectionDeleteHandler(e:DiagramEvent):void
    {
        e.preventDefault();
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowForDiagram(e.data.diagram as Diagram);
        var selections:Vector.<Object> = e.data.selections;
        _jiraDeleteRequest = JiraUtils.createJiraDeleteRequest(e.data.selections);
        _diagramSelections = new ArrayCollection();
        var i:int;
        for (i = 0; i < selections.length; i++)
        {
            _diagramSelections.addItem(selections[i].data);
        }

        if (workflow.isDraftWorkflow)
        {
            var parentWorkflow:FlexJiraWorkflow = workflowDiagramManager.getWorkflowByName(workflow.name, false);
            if (!parentWorkflow.isLoaded)
            {
                Swiz.dispatchEvent(new WorkflowEvent(EventTypes.LOAD_WORKFLOW, parentWorkflow, WorkflowEvent.OBJECT_DELETION));
                return;
            }
        }

        processDeleteChain(workflow, WorkflowEvent.OBJECT_DELETION);
    }

    [Mediate(event="${eventTypes.WORKFLOW_LOADED}", properties="workflow,reason")]
    public function processDeleteChain(fjw:FlexJiraWorkflow, reason:String):void
    {
        if (reason == WorkflowEvent.OBJECT_DELETION)
        {
            MDIDialogUtils.removeModalDialog(jiraProgressDialog);

            //the workflow passed in might be the parent, let's grab from manager to be sure we have the draft if needed
            var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
            var deleteRequest:Request = new WorkflowSelectionDeleteRequest(workflow, _diagramSelections);
            deleteChain.processRequest(deleteRequest);
        }
    }


    [Mediate(event="${eventTypes.CONFIRM_SELECTION_DELETE}", properties="workflow")]
    public function confirmDeleteSelectionHandler(workflow:FlexJiraWorkflow):void
    {
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_deleting_selections");

        var jwdLayout:JWDLayout = getLayout();
        executeServiceCall(jiraService.deleteStepsAndActions(_jiraDeleteRequest, workflow,jwdLayout), onSelectionsDeleted, DefaultFaultHandler.handleFault, [workflow]);
    }

    private function onSelectionsDeleted(e:ResultEvent, fjw:FlexJiraWorkflow):void
    {
        var workflow:FlexJiraWorkflow = e.result as FlexJiraWorkflowImpl;
        workflow.uid = fjw.uid;
        workflowDiagramManager.updateWorkflow(workflow);
        workflowDiagramManager.getCurrentDiagram().forceDeleteSelected();

        var actionManager:CommonActionManager = CommonActionManagerFactory.getActionManager(workflow);

        var i:int;
        var obj:Object;
        var action:FlexJiraAction;
        var uiEdge:UIEdge;
        var objects:Vector.<Selectable> = workflowDiagramManager.getCurrentDiagram().selectionManager.currentlySelected;
        var diagramObject:DiagramUIObject;
        for (i = 0; i < objects.length; i++)
        {
            diagramObject = objects[i] as DiagramUIObject;
            if (diagramObject)
            {
                if ((diagramObject is UIEdge))
                {
                    uiEdge = diagramObject as UIEdge;
                    action = uiEdge.edge.data as FlexJiraAction;
                    if(action.isCommon){
                        actionManager.removeCommonActionUI(action,uiEdge);
                    }
                } else if ((diagramObject is UIEdgeLabel))
                {
                    uiEdge = UIEdgeLabel(diagramObject).edgeLabelRenderer.edge.uiEdge;
                    action = uiEdge.edge.data as FlexJiraAction;
                    if(action.isCommon){
                        actionManager.removeCommonActionUI(action,uiEdge);
                    }
                }
            }
        }


        Swiz.dispatchEvent(new WorkflowEvent(EventTypes.WORKFLOW_OBJECTS_DELETED, workflow));
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
    }

    private function diagramDragDropHandler(e:DragEvent):void
    {
        e.preventDefault();
        _diagramDropPoint = new Point(e.stageX, e.stageY);
        _droppedObject = (e.dragSource.dataForFormat("items") as Array)[0];
        doAddStep(_droppedObject.data.name);
    }

    [Mediate(event="${eventTypes.ADD_STEP}", properties="data")]
    public function doAddStep(stepName:String):void
    {
        //MDIDialogUtils.removeModalDialog(addStepDialog);
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_adding_step");
        var newStep:FlexJiraStep = new FlexJiraStepImpl();
        newStep.name = stepName;
        newStep.linkedStatus = _droppedObject.data.id;

        var jwdLayout:JWDLayout = getLayout();
        executeServiceCall(jiraService.addStep(newStep, workflowDiagramManager.getCurrentWorkflow(),jwdLayout), onStepAdded, DefaultFaultHandler.handleFault, [stepName]);
        //log.debug("adding step: " + stepName);
    }

    private function onStepAdded(e:ResultEvent, stepName:String):void
    {
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
        var newWorkflow:FlexJiraWorkflow = e.result as FlexJiraWorkflowImpl;
        workflowDiagramManager.updateWorkflow(newWorkflow);

        var diagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        var newStep:FlexJiraStep = newWorkflow.getStepForName(stepName);
        var newNode:Node = diagram.createNode(_droppedObject.nodeRendererClass, newStep, _diagramDropPoint);

        Swiz.dispatchEvent(new WorkflowEvent(EventTypes.WORKFLOW_STEP_ADDED, newWorkflow));

        statusUtils.getStatusForId(newStep.linkedStatus).isActive = true;

        _diagramDropPoint = null;
        _droppedObject = null;
    }

    private function getLayout():JWDLayout {
        var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        var graphBounds:LayoutRect = SnapshotUtil.getGraphBounds(DisplayObject(currentDiagram));

        return layoutExporter.export(currentDiagram, currentDiagram.getRootUINodes(), graphBounds, workflow, false);
    }
}
}