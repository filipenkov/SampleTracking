package com.sysbliss.jira.workflow.manager
{
import com.sysbliss.diagram.data.Node;
import com.sysbliss.diagram.ui.UIEdge;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;

import mx.collections.ArrayCollection;

public interface CommonActionManager
{
    function getUIEdgesForActionId(id:int):ArrayCollection;
    function updateCommonActionUI(action:FlexJiraAction,endNode:Node):void;
    function addCommonActionUI(action:FlexJiraAction, uiEdge:UIEdge):void;
    function removeCommonActionUI(action:FlexJiraAction, uiEdge:UIEdge):void;
}
}