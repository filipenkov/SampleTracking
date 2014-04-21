package com.sysbliss.jira.workflow.controller {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.ToolTypes;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayoutImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
import com.sysbliss.jira.workflow.event.EventTypes;
import com.sysbliss.jira.workflow.event.JiraDiagramEvent;
import com.sysbliss.jira.workflow.layout.LayoutExporter;
import com.sysbliss.jira.workflow.layout.LayoutImporter;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.service.DefaultFaultHandler;
import com.sysbliss.jira.workflow.service.JiraWorkflowService;
import com.sysbliss.jira.workflow.ui.dialog.JiraProgressDialog;
import com.sysbliss.jira.workflow.utils.MDIDialogUtils;
import com.sysbliss.jira.workflow.utils.SnapshotUtil;

import flash.display.DisplayObject;

import mx.controls.ButtonBar;
import mx.events.ItemClickEvent;
import mx.rpc.events.ResultEvent;

import org.swizframework.Swiz;

public class LayoutToolbarController extends WorkflowAbstractController {
    private var _toolbar:ButtonBar;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    [Autowire]
    public var layoutImporter:LayoutImporter;

    [Autowire]
    public var layoutExporter:LayoutExporter;


    [Autowire]
    public var jiraService:JiraWorkflowService;

    [Autowire]
    public var jiraProgressDialog:JiraProgressDialog;

    public function LayoutToolbarController() {
        super();
    }

    private function setupToolbarContainer():void {
        _toolbar.addEventListener(ItemClickEvent.ITEM_CLICK, onToolClick);
    }

    private function onToolClick(e:ItemClickEvent):void {
        var currentWorkflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();

        if (currentDiagram && currentDiagram.isLinking()) {
            currentDiagram.cancelLink();
        }

        if (currentWorkflow) {
            if (e.item.name == ToolTypes.LAYOUT_SAVE.name) {
                doSaveLayout(currentWorkflow);
            } else if (e.item.name == ToolTypes.LAYOUT_LOAD.name) {
                doLoadLayout(currentWorkflow);
            } else if (e.item.name == ToolTypes.LAYOUT_AUTO.name) {
                doAutoLayout(currentWorkflow);
            }
        }
    }

    public function doSaveLayout(workflow:FlexJiraWorkflow):void {
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_saving_layout");

        var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        var graphBounds:LayoutRect = SnapshotUtil.getGraphBounds(DisplayObject(currentDiagram));
        var jwdLayout:JWDLayout = layoutExporter.export(currentDiagram,currentDiagram.getRootUINodes(), graphBounds, workflow, false);
        var layoutName:String;
        if (workflow.isDraftWorkflow) {
            executeServiceCall(jiraService.saveDraftLayout(workflow.name, jwdLayout), onLayoutSaved, DefaultFaultHandler.handleFault);
        } else {
            executeServiceCall(jiraService.saveActiveLayout(workflow.name, jwdLayout), onLayoutSaved, DefaultFaultHandler.handleFault);
        }

    }

    private function onLayoutSaved(e:ResultEvent):void {
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
    }

    public function doLoadLayout(workflow:FlexJiraWorkflow):void {
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_loading_layout");

            executeServiceCall(jiraService.loadLayout(workflow), onLayoutLoaded, DefaultFaultHandler.handleFault);

    }

    public function doAutoLayout(workflow:FlexJiraWorkflow):void {
        MDIDialogUtils.popModalDialog(jiraProgressDialog);
        jiraProgressDialog.progressLabel = niceResourceManager.getString("json", "workflow.designer.progress_loading_layout");

        var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();

        var graphBounds:LayoutRect = SnapshotUtil.getGraphBounds(DisplayObject(currentDiagram));
        var jwdLayout:JWDLayout = layoutExporter.export(currentDiagram,currentDiagram.getRootUINodes(), graphBounds, workflow, true);

        executeServiceCall(jiraService.calculateLayout(jwdLayout), onAutoLayoutLoaded, DefaultFaultHandler.handleFault);
    }

    private function onLayoutLoaded(e:ResultEvent):void {
        var jwdLayout:JWDLayout = e.result as JWDLayoutImpl;

        runLayoutImporter(jwdLayout, false);
    }

    private function onAutoLayoutLoaded(e:ResultEvent):void {
        var jwdLayout:JWDLayout = e.result as JWDLayoutImpl;
        runLayoutImporter(jwdLayout, true);

    }

    private function runLayoutImporter(layout:JWDLayout, isAuto:Boolean):void {
        if (layout.roots.length < 1) {
            MDIDialogUtils.removeModalDialog(jiraProgressDialog);
            return;
        }
        var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();

        //import the layout here
        layoutImporter.applyLayout(currentDiagram, layout, isAuto);

        Swiz.dispatchEvent(new JiraDiagramEvent(EventTypes.CURRENT_DIAGRAM_UPDATED, currentDiagram));
        MDIDialogUtils.removeModalDialog(jiraProgressDialog);
    }

    public function set toolbar(t:ButtonBar):void {
        this._toolbar = t;
        setupToolbarContainer();
    }

    public function get toolbar():ButtonBar {
        return this._toolbar;
    }

}
}