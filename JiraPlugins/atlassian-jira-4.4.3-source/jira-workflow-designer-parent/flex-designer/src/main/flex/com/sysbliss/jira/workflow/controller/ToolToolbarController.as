package com.sysbliss.jira.workflow.controller {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.ToolTypes;
import com.sysbliss.diagram.tools.DiagramAnnotationTool;
import com.sysbliss.diagram.tools.DiagramLinkTool;
import com.sysbliss.diagram.tools.DiagramTool;
import com.sysbliss.diagram.ui.UINode;
import com.sysbliss.diagram.util.CursorUtil;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;

import com.sysbliss.jira.workflow.ui.AssetEmbedder;

import flash.display.InteractiveObject;

import mx.controls.Button;
import mx.controls.ToggleButtonBar;
import mx.controls.buttonBarClasses.ButtonBarButton;
import mx.core.UIComponent;
import mx.events.ItemClickEvent;
import mx.skins.halo.ButtonBarButtonSkin;

public class ToolToolbarController extends WorkflowAbstractController {
    private var _toolbar:ToggleButtonBar;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    public function ToolToolbarController() {
        super();
    }

    private function setupToolbarContainer():void {
        _toolbar.addEventListener(ItemClickEvent.ITEM_CLICK, onToolClick);
    }


    private function onToolClick(e:ItemClickEvent):void {
        var currentDiagram:Diagram = workflowDiagramManager.getCurrentDiagram();
        if (currentDiagram) {
            currentDiagram.currentTool = e.item as DiagramTool;
            trace("clicked: " + e.item);
            var nodes:Vector.<UINode> = currentDiagram.getUINodes();
            var node:UINode;
            var i:int;
            if (e.item is DiagramLinkTool) {
                for(i=0;i<nodes.length;i++) {
                    node = nodes[i];
                    UIComponent(node).mouseChildren = false;
                    UIComponent(node).validateNow();
                }

                UIComponent(currentDiagram.labelLayer).mouseChildren = false;
                currentDiagram.currentTool = ToolTypes.TOOL_LINK;
                currentDiagram.currentLineType = e.item.name;
                if (currentDiagram.selectionManager.numSelected > 0) {
                    currentDiagram.updateSelectedEdgesLineType(currentDiagram.currentLineType);
                }
            } else if ( !(e.item is DiagramLinkTool) && currentDiagram.isLinking()) {
                currentDiagram.cancelLink();
                UIComponent(currentDiagram.labelLayer).mouseChildren = true;
                for(i=0;i<nodes.length;i++) {
                    node = nodes[i];
                    UIComponent(node).mouseChildren = true;
                }
            } else {
                UIComponent(currentDiagram.labelLayer).mouseChildren = true;
                for(i=0;i<nodes.length;i++) {
                    node = nodes[i];
                    UIComponent(node).mouseChildren = true;
                }
            }
        }
    }

    [Mediate(event="${eventTypes.LOAD_WORKFLOWS_COMPLETED}")]
    public function onWorkflowsLoaded():void {
        var fjw:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();
        
        if ((fjw.isActive || fjw.isSystemWorkflow) && !fjw.isDraftWorkflow) {
            //make sure the select tool is selected
            onToolClick(new ItemClickEvent(ItemClickEvent.ITEM_CLICK, false, false, null, -1, null, ToolTypes.TOOL_SELECT));

            //disable link tools
            toggleLinkButtons(false);
        } else {
            //make sure link tools are enabled
            toggleLinkButtons(true);
        }
    }

    [Mediate(event="${eventTypes.CURRENT_DIAGRAM_CHANGED}", properties="diagram")]
    public function updateDiagramTool(d:Diagram):void {
        d.currentTool = _toolbar.dataProvider[_toolbar.selectedIndex] as DiagramTool;

    }

    private function toggleLinkButtons(enabled:Boolean):void {
        var i:int;
        var button:ButtonBarButton;
        for (i = 1; i < _toolbar.dataProvider.length; i++) {
            button = _toolbar.getChildAt(i) as ButtonBarButton;
            if (button) {
                button.enabled = enabled;
                if(enabled) {
                    switch(i) {
                        case 1 : {
                            button.setStyle("icon", AssetEmbedder.straightIcon);
                            break;
                        }
                        case 2 : {
                            button.setStyle("icon", AssetEmbedder.polyIcon);
                            break;
                        }
                        case 3 : {
                            button.setStyle("icon", AssetEmbedder.bezierIcon);
                            break;
                        }
                    }
                } else {
                    switch(i) {
                        case 1 : {
                            button.setStyle("icon", AssetEmbedder.straightDisabledIcon);
                            break;
                        }
                        case 2 : {
                            button.setStyle("icon", AssetEmbedder.polyDisabledIcon);
                            break;
                        }
                        case 3 : {
                            button.setStyle("icon", AssetEmbedder.bezierDisabledIcon);
                            break;
                        }
                    }
                }
            }
        }
    }

    public function set toolbar(t:ToggleButtonBar):void {
        this._toolbar = t;
        setupToolbarContainer();
    }

    public function get toolbar():ToggleButtonBar {
        return this._toolbar;
    }

}
}