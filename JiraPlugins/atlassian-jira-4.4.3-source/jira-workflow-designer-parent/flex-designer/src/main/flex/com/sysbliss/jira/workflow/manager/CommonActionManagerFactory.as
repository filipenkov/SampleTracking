package com.sysbliss.jira.workflow.manager
{
import com.sysbliss.collections.HashMap;
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.Node;
import com.sysbliss.diagram.ui.UIEdge;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;

import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;

import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;

import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;

import flash.geom.Point;

import mx.collections.ArrayCollection;
import mx.core.UIComponent;

public class CommonActionManagerFactory implements CommonActionManager
{
    private var actionMap:HashMap;
    private static var _managers:HashMap = new HashMap();

    private const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

    public function CommonActionManagerFactory(lock:Class)
    {
        if(lock != SingletonLock){
				throw new Error( niceResourceManager.getString('json','workflow.designer.invalid.singleton.access') );
			}
        this.actionMap = new HashMap();
    }

    public static function getActionManager(workflow:FlexJiraWorkflow):CommonActionManager {
        var manager:CommonActionManager;
        if(!_managers.keyExists(workflow.id)) {
            _managers.put(workflow.id, new CommonActionManagerFactory(SingletonLock));
        }

        return _managers.getValue(workflow.id) as CommonActionManager;
    }

    public static function removeActionManager(workflow:FlexJiraWorkflow):void {
        if(_managers.keyExists(workflow.id)) {
            _managers.remove(workflow.id);
        }
    }

    public static function updateWorkflow(oldWF:FlexJiraWorkflow,newWF:FlexJiraWorkflow):void {
        if(_managers.keyExists(oldWF.id)) {
            var manager:CommonActionManager = _managers.getValue(oldWF.id) as CommonActionManager;
            _managers.remove(oldWF.id);
            _managers.put(newWF.id,manager);
        }
    }

    public function getUIEdgesForActionId(id:int):ArrayCollection
    {
        var edges:ArrayCollection = new ArrayCollection();
        if(actionMap.keyExists(id)) {
            edges = actionMap.getValue(id) as ArrayCollection;
        }

        return edges;
    }

    public function updateCommonActionUI(action:FlexJiraAction,endNode:Node):void
    {
        if(action.isCommon) {
            if(actionMap.keyExists(action.id)) {
                var uiEdges:ArrayCollection = actionMap.getValue(action.id) as ArrayCollection;
                var i:int;
                var uiEdge:UIEdge;
                for(i=0;i<uiEdges.length;i++) {
                    uiEdge = uiEdges[i] as UIEdge;
                    uiEdge.edge.data = action;
                    uiEdge.edge.endNode = endNode;

                    var newEndPoint:Point = new Point(endNode.uiNode.centerPoint.x,endNode.uiNode.centerPoint.y);
                    uiEdge.moveEndPoint(newEndPoint.x, newEndPoint.y);
                    UIComponent(uiEdge).invalidateDisplayList();
                    UIComponent(uiEdge).validateNow();
                }
            }
        }
    }

    public function addCommonActionUI(action:FlexJiraAction, uiEdge:UIEdge):void
    {
        var edges:ArrayCollection;
        if(!actionMap.keyExists(action.id)) {
            edges = new ArrayCollection();
        } else {
            edges = actionMap.getValue(action.id);
        }

        var d:Diagram = uiEdge.diagram;
        var allUIEdges:Vector.<UIEdge> = d.getUIEdges();
        var tmpUIEdge:UIEdge;

        var i:int;
        for(i=0;i<allUIEdges.length;i++){
            tmpUIEdge = allUIEdges[i];
            if(tmpUIEdge.edge.data.id == uiEdge.edge.data.id) {
                edges.addItem(tmpUIEdge);
            }
        }

        actionMap.put(action.id,edges);
    }

    public function removeCommonActionUI(action:FlexJiraAction, uiEdge:UIEdge):void
    {
        if(actionMap.keyExists(action.id)) {
            var edges:ArrayCollection = actionMap.getValue(action.id);

            if(edges.contains(uiEdge)){
                edges.removeItemAt(edges.getItemIndex(uiEdge));
                actionMap.put(action.id,edges);
            }
        }
    }
}
}

class SingletonLock{};