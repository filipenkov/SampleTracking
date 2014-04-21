package com.sysbliss.jira.workflow.diagram.renderer {
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.renderer.AbstractEdgeLabelRenderer;
import com.sysbliss.diagram.ui.UIEdge;
import com.sysbliss.diagram.util.CursorUtil;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;
import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;
import com.sysbliss.jira.workflow.manager.WorkflowDiagramManager;
import com.sysbliss.jira.workflow.utils.ContextMenuUtils;

import flash.events.MouseEvent;
import flash.geom.Point;
import flash.text.AntiAliasType;

import mx.collections.ArrayCollection;
import mx.controls.Button;
import mx.controls.Label;
import mx.controls.Menu;
import mx.core.UIComponent;
import mx.events.MenuEvent;

public class AbstractJiraEdgeLabelRenderer extends AbstractEdgeLabelRenderer {
    //default style stuff
    private static const DEFAULT_BG_COLOR:uint = 0xd0DFEE;
    private static const DEFAULT_BORDER_COLOR:uint = 0x3C78B5;
    private static const SELECTED_BORDER_COLOR:uint = 0xFF0000;

    private var defaultBorderColor:uint = DEFAULT_BG_COLOR;
    private var selectedBorderColor:uint = SELECTED_BORDER_COLOR;

    protected var _label:Label;

    private var _button:Button;

    //private var _layoutContainer:LayoutContainer;

    private const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

    [Autowire]
    public var workflowDiagramManager:WorkflowDiagramManager;

    [Autowire]
    public var contextMenuUtils:ContextMenuUtils;

    private var _menu:Menu = null;

    public function AbstractJiraEdgeLabelRenderer(diagram:Diagram) {
        super(diagram);
        this.styleName = "EdgeLabel";
    }

    //setup styles
    override public function styleChanged(styleProp:String):void {

        super.styleChanged(styleProp);

        var allStyles:Boolean = (!styleProp || styleProp == "styleName");

        if (allStyles || styleProp == "defaultBorderColor") {
            if (getStyle("defaultBorderColor") is uint)
            {
                defaultBorderColor = getStyle("defaultBorderColor");
            } else {
                defaultBorderColor = DEFAULT_BORDER_COLOR;
            }

            setStyle("borderColor",defaultBorderColor);
        }

        if (allStyles || styleProp == "selectedBorderColor") {
            if (getStyle("selectedBorderColor") is uint)
            {
                selectedBorderColor = getStyle("selectedBorderColor");
            } else {
                selectedBorderColor = SELECTED_BORDER_COLOR;
            }
        }

        invalidateDisplayList();
    }

    override protected function commitProperties():void {
        if (_edgeLabelChanged && _edgeLabel) {
            _label.text = _edgeLabel.text;
        }
        super.commitProperties();
    }

    override protected function createChildren():void {
        super.createChildren();

        if (!_label) {

            _label = new Label();
            _label.styleName = "EdgeLabelLabel";

            //_layoutContainer.addChild(_label);

            addChild(_label);

            _button = new Button();
            _button.styleName = "CogSmall";
            _button.height = 20;
            _button.width = 20;

            _button.visible = false;

            _button.label = "";
            _button.buttonMode = false;
            _button.useHandCursor = false;
            addChild(_button);

            _button.addEventListener(MouseEvent.MOUSE_DOWN, buttonClickListener);
            _button.addEventListener(MouseEvent.MOUSE_OVER, buttonOverListener);
            parent.addEventListener(MouseEvent.MOUSE_UP, onMouseClick);
            parent.addEventListener(MouseEvent.MOUSE_OVER, mouseOverEventListener, false, 10000);
            parent.addEventListener(MouseEvent.MOUSE_OUT, mouseOutEventListener);

        }
    }

    private function onMouseClick(e:MouseEvent):void {
        _edge.uiEdge.selectionManager.setSelected(_edge.uiEdge);
    }

    private function mouseOverEventListener(event:MouseEvent):void {
        _button.visible = true;

        _edge.uiEdge.highlightEdge();
        highlight();

        //event.stopImmediatePropagation();
        //event.stopPropagation();
        //ObjectHandles(this.parent).removeCustomCursor();
    }

    private function mouseOutEventListener(event:MouseEvent):void {
        if ((_menu == null || !_menu.visible) && !_button.hitTestPoint(event.stageX,event.stageY)) {
            _button.visible = false;
        }

        if(!_edge.uiEdge.isSelected) {
            unhighlight();
            _edge.uiEdge.unhighlightEdge();
        }

    }

    private function buttonClickListener(event:MouseEvent):void {
        event.stopImmediatePropagation();
        event.stopPropagation();
        showEdgeMenu(localToGlobal(new Point(_button.x, _button.y + _button.getExplicitOrMeasuredHeight())));
    }

    private function buttonOverListener(event:MouseEvent):void
        {
            event.stopImmediatePropagation();
            event.stopPropagation();
            CursorUtil.showPointer();
        }

    override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
        super.updateDisplayList(unscaledWidth, unscaledHeight);

        graphics.clear();
        graphics.drawRoundRect(0, 0, unscaledWidth, unscaledHeight, 10, 10);
    }

    public function showEdgeMenu(p:Point):void {
        var menuItems:ArrayCollection = new ArrayCollection();

        var uiEdge:UIEdge = _edge.uiEdge;

        var action:FlexJiraAction = uiEdge.edge.data as FlexJiraAction;
        var workflow:FlexJiraWorkflow = workflowDiagramManager.getCurrentWorkflow();

        if (!uiEdge.isSelected) {
            uiEdge.selectionManager.setSelected(uiEdge);
        }

        var edgeDeleteItemEnabled:Boolean = true;
        var edgeEditItemEnabled:Boolean = true;
        var edgePropertiesItemEnabled:Boolean = true;
        var edgeConditionItemEnabled:Boolean = true;

        if (!action || !workflow.isEditable || workflow.isInitialAction(action.id)) {
            edgeDeleteItemEnabled = false;
            edgeEditItemEnabled = false;
        }

        if (!action || !workflow.isEditable) {
            edgePropertiesItemEnabled = false;
        }

        if (workflow.isInitialAction(action.id)) {
            edgeConditionItemEnabled = false;
        }

        menuItems.addItem({label: niceResourceManager.getString('json', 'workflow.designer.view.conditions'),
            enabled: edgeConditionItemEnabled});

        menuItems.addItem({label: niceResourceManager.getString('json', 'workflow.designer.view.validators')});

        menuItems.addItem({label: niceResourceManager.getString('json', 'workflow.designer.view.post.functions')});

        menuItems.addItem({type: 'separator'});

        menuItems.addItem({label: niceResourceManager.getString('json', 'workflow.designer.edit.transition.title'),
            enabled: edgeDeleteItemEnabled});

        menuItems.addItem({label: niceResourceManager.getString('json', 'workflow.designer.delete.transition'),
            enabled: edgeEditItemEnabled});

        menuItems.addItem({type: 'separator'});

        menuItems.addItem({ label: niceResourceManager.getString('json', 'workflow.designer.transition.properties'),
            enabled: edgePropertiesItemEnabled});

        _menu = Menu.createMenu(null, menuItems);

        _menu.addEventListener(MenuEvent.ITEM_CLICK, menuItemClick);

        _menu.addEventListener(MenuEvent.MENU_HIDE, menuHide);

        _menu.show(p.x, p.y);
    }

    private function menuItemClick(e:MenuEvent):void {
        switch (e.label) {
            case niceResourceManager.getString('json', 'workflow.designer.view.conditions'):
            {
                contextMenuUtils.onEdgeConditions(_edge.uiEdge);
            }
                break;

            case niceResourceManager.getString('json', 'workflow.designer.view.validators'):
            {
                contextMenuUtils.onEdgeValidators(_edge.uiEdge);
            }
                break;

            case niceResourceManager.getString('json', 'workflow.designer.view.post.functions'):
            {
                contextMenuUtils.onEdgeFunctions(_edge.uiEdge);
            }
                break;

            case niceResourceManager.getString('json', 'workflow.designer.edit.transition.title'):
            {
                contextMenuUtils.onEdgeEdit(_edge.uiEdge);
            }
                break;

            case niceResourceManager.getString('json', 'workflow.designer.delete.transition'):
            {
                contextMenuUtils.onDeleteSelected();
            }
                break;

            case niceResourceManager.getString('json', 'workflow.designer.transition.properties'):
            {
                contextMenuUtils.onEdgeProperties(_edge.uiEdge);
            }
                break;
        }
    }

    override public function highlight():void {
        this.setStyle("borderColor", selectedBorderColor);
        invalidateDisplayList();
    }

    override public function unhighlight():void {
        this.setStyle("borderColor", defaultBorderColor);
        invalidateDisplayList();
    }

    private function menuHide(event:MenuEvent):void {
        _button.visible = false;
    }

}
}