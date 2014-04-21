package com.sysbliss.jira.workflow.controller {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;

import flash.events.MouseEvent;

import mx.containers.HBox;
import mx.controls.Button;

public class LabelToolbarController extends WorkflowAbstractController {
    private var _toolbar:HBox;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    public function LabelToolbarController() {
        super();
    }

    private function setupToolbarContainer():void {
        var checkBox:Button = _toolbar.getChildByName("labelButton") as Button;
        checkBox.addEventListener(MouseEvent.CLICK, onToolClick);
    }

    private function onToolClick(e:MouseEvent):void {
        var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        if (currentDiagram) {
            if (currentDiagram.isLinking()) {
                currentDiagram.cancelLink();
            }
            currentDiagram.toggleLabels();
        }
    }

    public function set toolbar(t:HBox):void {
        this._toolbar = t;
        setupToolbarContainer();
    }

    public function get toolbar():HBox {
        return this._toolbar;
    }
}
}
