package com.sysbliss.jira.workflow.controller {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.Annotation;
import com.sysbliss.diagram.data.DefaultAnnotation;
import com.sysbliss.diagram.event.DiagramAnnotationEvent;
import com.sysbliss.diagram.event.DiagramEvent;
import com.sysbliss.diagram.ui.StickyNote;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotationImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.workflow.layout.LayoutExporter;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
import com.sysbliss.jira.workflow.service.JiraWorkflowService;
import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
import com.sysbliss.jira.workflow.utils.SnapshotUtil;

import flash.display.DisplayObject;

import mx.controls.Alert;
import mx.events.CloseEvent;

import mx.rpc.events.ResultEvent;

public class AnnotationController extends WorkflowAbstractController {

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    [Autowire]
    public var jiraService:JiraWorkflowService;

    [Autowire]
    public var jiraProgressDialog:JiraProgressDialog;

    [Autowire]
    public var layoutExporter:LayoutExporter;

    private var _currentSticky:StickyNote;

    public function AnnotationController() {
        super();
    }

    [Mediate(event="${eventTypes.DIAGRAM_CREATED}", properties="diagram")]
    public function addDiagramEventListeners(d:Diagram):void {
        d.addEventListener(DiagramEvent.ANNOTATION_UPDATED, onDiagramAnnotationUpdate);
        d.addEventListener(DiagramEvent.ANNOTATION_DELETED, onDiagramAnnotationDelete);
    }

    /*
     var alertMessage:String = "";
     var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title_readonly");

     if(jiraServerManager.getUserPrefs().confirmDeleteSelection){
     alertTitle = niceResourceManager.getString("json","workflow.designer.title.confirm_delete_selection");

     var workflowObject:FlexWorkflowObject  = _currentRequest.selections.getItemAt(0) as FlexWorkflowObject;
     if ((workflowObject is FlexJiraStep)) {
     var step:FlexJiraStep = workflowObject as FlexJiraStep;
     alertMessage = niceResourceManager.getString("json","workflow.designer.confirm_delete_step",[step.name]);
     } else if ((workflowObject is FlexJiraAction)) {
     var action:FlexJiraAction = workflowObject as FlexJiraAction;
     alertMessage = niceResourceManager.getString("json","workflow.designer.confirm_delete_transition",[action.name]);
     }

     Alert.buttonWidth = 80;
     var alert:Alert = Alert.show(alertMessage,alertTitle,Alert.OK|Alert.CANCEL,null,confirmDeleteSelectionHandler,null,Alert.OK);

     }
     }

     private function confirmDeleteSelectionHandler(e:CloseEvent):void {
     if (e.detail==Alert.OK){
     successor.processRequest(_currentRequest);
     } else {
     return;
     }
     }


     */
    private function onDiagramAnnotationDelete(e:DiagramAnnotationEvent):void {
        _currentSticky = e.stickyNote;
        var alertMessage:String = niceResourceManager.getString("json","workflow.designer.confirm_delete_annotation");
        var alertTitle:String = niceResourceManager.getString("json","workflow.designer.title.confirm_delete_selection");
        Alert.buttonWidth = 80;
        var alert:Alert = Alert.show(alertMessage,alertTitle,Alert.OK|Alert.CANCEL,null,confirmDeleteHandler,null,Alert.OK);
    }

    private function confirmDeleteHandler(e:CloseEvent):void {
        if (e.detail==Alert.OK && _currentSticky){
            doDeleteAnnotation();
        }
    }

    private function doDeleteAnnotation():void {
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_updating_annotations");

        var annotation:WorkflowAnnotation = _currentSticky.annotation.data as WorkflowAnnotation;
        var jwdLayout:JWDLayout = getLayout();
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        executeServiceCall(jiraService.removeAnnotationFromWorkflow(workflow, annotation, jwdLayout), onAnnotationDeleted, DefaultFaultHandler.handleFault, [_currentSticky]);
    }

    private function onAnnotationDeleted(e:ResultEvent, sticky:StickyNote):void {
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
        var diagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        diagram.removeAnnotation(sticky);
        _currentSticky = null;

    }

    private function onDiagramAnnotationUpdate(e:DiagramAnnotationEvent):void {


        var annotation:WorkflowAnnotation = e.stickyNote.annotation.data as WorkflowAnnotation;
        var jwdLayout:JWDLayout = getLayout();
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_updating_annotations");

        executeServiceCall(jiraService.updateAnnotationForWorkflow(workflow, annotation, jwdLayout), onAnnotationUpdated, DefaultFaultHandler.handleFault, [e.stickyNote]);
    }

    private function onAnnotationUpdated(e:ResultEvent, sticky:StickyNote):void {
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
    }

    public function createAnnotation():void {
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_updating_annotations");

        var annotation:WorkflowAnnotation = new WorkflowAnnotationImpl();
        var jwdLayout:JWDLayout = getLayout();
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        executeServiceCall(jiraService.addAnnotationToWorkflow(workflow, annotation, jwdLayout), onAnnotationCreated, DefaultFaultHandler.handleFault, [annotation]);
    }

    private function onAnnotationCreated(e:ResultEvent, annotation:WorkflowAnnotation):void {
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
        var dataObj:Annotation = new DefaultAnnotation(annotation.id);
        dataObj.data = annotation;

        workflowDiagramManager.getCurrentDiagram().createAnnotation(dataObj);
    }

    private function getLayout():JWDLayout {
        var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        var graphBounds:LayoutRect = SnapshotUtil.getGraphBounds(DisplayObject(currentDiagram));

        return layoutExporter.export(currentDiagram, currentDiagram.getRootUINodes(), graphBounds, workflow, false);
    }

}
}
