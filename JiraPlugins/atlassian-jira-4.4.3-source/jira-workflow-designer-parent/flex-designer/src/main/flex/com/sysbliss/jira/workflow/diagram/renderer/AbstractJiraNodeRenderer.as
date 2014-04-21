package com.sysbliss.jira.workflow.diagram.renderer {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.renderer.AbstractNodeRenderer;
import com.sysbliss.diagram.ui.UINode;
import com.sysbliss.diagram.ui.selectable.Selectable;
import com.sysbliss.diagram.util.CursorUtil;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;
import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.utils.ContextMenuUtils;
import com.sysbliss.jira.workflow.utils.StatusUtils;

import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.events.MouseEvent;
import flash.geom.Point;

import mx.collections.ArrayCollection;
import mx.containers.HBox;
import mx.containers.VBox;
import mx.controls.Button;
import mx.controls.Image;
import mx.controls.Label;
import mx.controls.Menu;
import mx.events.FlexEvent;
import mx.events.MenuEvent;

public class AbstractJiraNodeRenderer extends AbstractNodeRenderer {
    protected var _label:Label;
    protected var _icon:Image;
    private var _hbox:HBox;
    protected var _button:Button;

    [Autowire]
    public var statusUtils:StatusUtils;

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    [Autowire]
    public var contextMenuUtils:ContextMenuUtils;

    private const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

    private var buttonVBox:VBox;

    private var _menu:Menu = null;

    public function AbstractJiraNodeRenderer(diagram:Diagram) {
        super(diagram);
    }

    override protected function commitProperties():void {
        if (_nodeChanged && _node) {
            var nodeLabel:String = "";
            var nodeIcon:Class;

            var fjs:FlexJiraStep = _node.data as FlexJiraStep;
            if (!fjs) {
                var fja:FlexJiraAction = _node.data as FlexJiraAction;
                if (fja) {
                    nodeLabel = fja.name;
                    var fjw:FlexJiraWorkflow = workflowDiagramManager.getWorkflowForDiagram(this._diagram);
                    fjs = fjw.getStep(fja.unconditionalResult.stepId);
                }
            }
            else {
                nodeLabel = fjs.name;
            }

            nodeIcon = statusUtils.getThumbnailForStatusId(fjs.linkedStatus);

            _label.text = nodeLabel;
            _label.x = ((width - _label.getExplicitOrMeasuredWidth()) / 2);
            _label.y = ((height - _label.getExplicitOrMeasuredHeight()) / 2);

            _icon.addEventListener(FlexEvent.UPDATE_COMPLETE, onIconDims);
            _icon.source = nodeIcon;
            _icon.scaleX = .5;
            _icon.scaleY = .5;
            _icon.invalidateDisplayList();

        }
        super.commitProperties();
    }

    override protected function updateDisplayList(w:Number, h:Number):void {
        if (Selectable(parent).isSelected) {
            this.setStyle("borderColor", 0xff0000);
        }
        else {
            this.setStyle("borderColor", 0xBBBBBB);
        }

        super.updateDisplayList(w, h);
    }


    override protected function createChildren():void {
        super.createChildren();

        // TODO: move hardcoded styling choices into CSS file

        if (!_hbox) {
            _hbox = new HBox();
            _hbox.percentWidth = 100;
            _hbox.percentHeight = 100;
            _hbox.setStyle("paddingLeft", 4);
            _hbox.setStyle("paddingTop", 4);
            _hbox.setStyle("paddingRight", 4);
            _hbox.setStyle("paddingBottom", 4);
            _hbox.setStyle("horizontalGap", 3);

            addChild(_hbox);
        }

        if (!_icon) {
            _icon = new Image();
            _icon.maintainAspectRatio = true;
            //_icon.alpha = .8;
            _icon.percentWidth = 100;
            _icon.percentHeight = 100;

            _hbox.addChild(_icon);
        }

        if (!_label) {
            _label = new Label();
            _label.styleName = this;

            var labelVBox:VBox = new VBox();
            labelVBox.percentWidth = 100;
            labelVBox.percentHeight = 100;
            labelVBox.setStyle("verticalAlign", "middle");
            labelVBox.addChild(_label);
            _hbox.addChild(labelVBox);
        }

        if (!_button) {
            _button = new Button();
            _button.styleName = "Cog";
            _button.height = 20;
            _button.width = 20;

            _button.label = "";

            _button.useHandCursor = false;
            _button.buttonMode = false;


            buttonVBox = new VBox();
            buttonVBox.percentWidth = 100;
            buttonVBox.percentHeight = 100;
            buttonVBox.setStyle("verticalAlign", "middle");
            buttonVBox.addChild(_button);

            _button.visible = false;

            _hbox.addChild(buttonVBox);
        }

        _button.addEventListener(MouseEvent.MOUSE_DOWN, buttonClickListener);
        _button.addEventListener(MouseEvent.MOUSE_OVER, buttonOverListener);
        _button.addEventListener(MouseEvent.MOUSE_OUT, buttonOutListener);
        parent.addEventListener(MouseEvent.MOUSE_OVER, mouseOverEventListener, false, 10000);
        parent.addEventListener(MouseEvent.MOUSE_OUT, mouseOutEventListener);
    }

    private function onIconDims(e:FlexEvent):void {
        _icon.removeEventListener(FlexEvent.UPDATE_COMPLETE, onIconDims);
        var bmpdata:BitmapData = Bitmap(_icon.content).bitmapData;
        _icon.minWidth = bmpdata.width;
        _icon.minHeight = bmpdata.height;
    }

    private function mouseOverEventListener(event:MouseEvent):void {
        if (!_diagram.isLinking()) {
            _button.visible = true;
        }


    }

    private function mouseOutEventListener(event:MouseEvent):void {
        if ((_menu == null || !_menu.visible) && !_button.hitTestPoint(event.stageX, event.stageY)) {
            _button.visible = false;
        }
    }

    private function buttonClickListener(event:MouseEvent):void {
        event.stopImmediatePropagation();
        event.stopPropagation();
        showNodeMenu(localToGlobal(new Point(buttonVBox.x + _button.x, buttonVBox.y + _button.y + _button.getExplicitOrMeasuredHeight())));
    }

    private function buttonOverListener(event:MouseEvent):void {
        event.stopImmediatePropagation();
        event.stopPropagation();
        CursorUtil.showPointer();
    }

    private function buttonOutListener(event:MouseEvent):void {

    }

    public function showNodeMenu(p:Point):void {
        var uiNode:UINode = _node.uiNode;

        var step:FlexJiraStep = uiNode.node.data as FlexJiraStep;
        var issueEditable:Boolean = true;
        var issueEditableEnabled:Boolean = true;


        if (!_menu) {

            if(step == null) {
                issueEditableEnabled = false;
            }else if(step.metaAttributes.hasOwnProperty("jira.issue.editable") && step.metaAttributes["jira.issue.editable"] == "false") {

                issueEditable = false;
                issueEditableEnabled = true;
            }
            var menuItems:ArrayCollection = new ArrayCollection();

            menuItems.addItem({label: niceResourceManager.getString('json', 'workflow.designer.delete.step')});

            menuItems.addItem({type: 'separator'});

            menuItems.addItem({label: niceResourceManager.getString('json', 'workflow.designer.step.properties')});

            menuItems.addItem({label: niceResourceManager.getString('json', 'workflow.designer.step.issue.editable'), type:'check', toggled:issueEditable, enabled:issueEditableEnabled});

            _menu = Menu.createMenu(null, menuItems);

            _menu.addEventListener(MenuEvent.ITEM_CLICK, menuItemClick);

            _menu.addEventListener(MenuEvent.MENU_HIDE, menuHide);
        }



        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

        if (!uiNode.isSelected) {
            uiNode.selectionManager.setSelected(uiNode);
        }


        if (!step || !workflow.isEditable) {
            _menu.enabled = false;
        } else {
            _menu.enabled = true;
        }


        _menu.show(p.x, p.y);
    }

    private function menuItemClick(e:MenuEvent):void {
        switch (e.label) {
            case niceResourceManager.getString('json', 'workflow.designer.delete.step'):
            {
                contextMenuUtils.onDeleteSelected();
                break;
            }

            case niceResourceManager.getString('json', 'workflow.designer.step.properties'):
            {
                contextMenuUtils.onNodeProperties(_node.uiNode);
                break;
            }

            case niceResourceManager.getString('json', 'workflow.designer.step.issue.editable'):
            {
                var fjs:FlexJiraStep = _node.data as FlexJiraStep;
                contextMenuUtils.onToggleIssueEditable(fjs,e.item.toggled, e.item);
                break;
            }

        }
    }

    private function menuHide(event:MenuEvent):void {
        _button.visible = false;
    }

}
}