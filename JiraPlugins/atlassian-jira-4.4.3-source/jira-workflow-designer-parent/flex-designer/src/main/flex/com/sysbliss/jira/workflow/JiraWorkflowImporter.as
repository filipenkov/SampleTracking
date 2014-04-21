package com.sysbliss.jira.workflow
{
    import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.Annotation;
import com.sysbliss.diagram.data.DefaultAnnotation;
import com.sysbliss.diagram.data.Edge;
    import com.sysbliss.diagram.data.Node;
    import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
    import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;
    import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;
import com.sysbliss.jira.workflow.diagram.renderer.DefaultJiraEdgeLabelRenderer;
    import com.sysbliss.jira.workflow.diagram.renderer.DefaultJiraNodeRenderer;
    import com.sysbliss.jira.workflow.manager.CommonActionManager;
    import com.sysbliss.jira.workflow.manager.CommonActionManagerFactory;
    import com.sysbliss.jira.workflow.utils.StatusUtils;

    import flash.display.InteractiveObject;
    import flash.events.MouseEvent;

import flash.geom.Point;

import mx.collections.ArrayCollection;
    import mx.core.UIComponent;

    public class JiraWorkflowImporter
    {

        [Autowire]
        public var _statusUtils:StatusUtils;

        private var _diagram:Diagram;
        private var _manager:DiagramNodeManager;
        private var _jiraWorkflow:FlexJiraWorkflow;

        private var _initialNodePosition:Point;

        public function JiraWorkflowImporter(diagram:Diagram, workflow:FlexJiraWorkflow)
        {
            this._diagram = diagram;
            this._jiraWorkflow = workflow;
            this._manager = new DiagramNodeManager();
            this._initialNodePosition = new Point();
            _initialNodePosition.x = diagram.centerPoint.x;
            _initialNodePosition.y = 0;

        }

        public function importWorkflow():void
        {
            addInitialAction();
            addNonLinkedSteps();
            addAnnotations();
            UIComponent(_diagram).validateNow();
        }

        private function addAnnotations():void {
            var annotations:ArrayCollection = _jiraWorkflow.workflowAnnotations;
            var i;
            for (i = 0;i<annotations.length;i++)
            {

                var annotation:WorkflowAnnotation = annotations.getItemAt(i) as WorkflowAnnotation;
                trace("ann id:" + annotation.id + " desc:" + annotation.description);
                var diagramAnnotation:Annotation = new DefaultAnnotation(annotation.id);
                diagramAnnotation.data = annotation;

                _diagram.createAnnotation(diagramAnnotation);
            }
        }

        private function addInitialAction():void
        {
            var action:FlexJiraAction = _jiraWorkflow.initialActions.getItemAt(0) as FlexJiraAction;
            //log.debug("initial-action: " + action);
            var step:FlexJiraStep = _jiraWorkflow.getStep(action.unconditionalResult.stepId);

            //add the node (which is really an action for the initial action)
            var root:Node = _diagram.createNodeQuietly(_statusUtils.getNodeRendererForStatusId(step.linkedStatus), action, _initialNodePosition);
            //create our steps
            var nextStep:Node = createStep(step);

            //link them up
            var rootLink:Edge = _diagram.createLinkQuietly(root, nextStep, null, DefaultJiraEdgeLabelRenderer, action);
            //InteractiveObject(rootLink.uiEdge.uiLabel).contextMenu = _contextUtils.getEdgeMenu();
            //var tf:UITextField = rootLink.uiEdge.uiLabel.mx_internal::getTextField() as UITextField;
            //InteractiveObject(tf).contextMenu = _contextUtils.getEdgeMenu();
        }

        private function addNonLinkedSteps():void
        {
            for each(var step:FlexJiraStep in _jiraWorkflow.allSteps)
            {
                if (!_manager.hasStepId(step.id))
                {
                    createStep(step);
                }
            }
        }

        private function createStep(step:FlexJiraStep):Node
        {
            var stepNode:Node;
            if (_manager.hasStepId(step.id))
            {
                //log.debug("got existing step: " + step.id);
                stepNode = _manager.getNodeForStepId(step.id) as Node;
            } else
            {
                //log.debug("creating new step: " + step.id);
                stepNode = _diagram.createNodeQuietly(_statusUtils.getNodeRendererForStatusId(step.linkedStatus), step, _initialNodePosition);
                _manager.addStep(stepNode, step);
            }
            addActions(stepNode, step);
            return stepNode;
        }

        private function addActions(node:Node, step:FlexJiraStep):void
        {
            var actionList:ArrayCollection = step.actions;
            if (actionList.length < 1)
            {
                return;
            }
            for (var i:int = 0; i < actionList.length; i++)
            {
                var action:FlexJiraAction = actionList.getItemAt(i) as FlexJiraAction;
                var nextStep:FlexJiraStep;
                if(action.unconditionalResult.stepId > -1) {
                    nextStep = _jiraWorkflow.getStep(action.unconditionalResult.stepId);
                } else {
                    nextStep = step;
                }

                createAction(node, action, nextStep);

            }
        }

        private function createAction(startNode:Node, action:FlexJiraAction, nextStep:FlexJiraStep):void
        {
            var nextStepNode:Node;
            if (!_manager.hasStepId(nextStep.id))
            {
                nextStepNode = createStep(nextStep);
            } else
            {
                nextStepNode = _manager.getNodeForStepId(nextStep.id);
            }
            var link:Edge = _diagram.createLinkQuietly(startNode, nextStepNode, null, DefaultJiraEdgeLabelRenderer, action);

            if (action.isCommon)
            {
                var actionManager:CommonActionManager = CommonActionManagerFactory.getActionManager(_jiraWorkflow);
                actionManager.addCommonActionUI(action, link.uiEdge);
            }

        }

    }
}

import com.sysbliss.collections.HashMap;
import com.sysbliss.diagram.data.Node;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraStep;

class DiagramNodeManager
{
    private var _nodeMap:HashMap;
    private var _stepIds:HashMap;
    private var _nodeIds:HashMap;

    public function DiagramNodeManager()
    {
        this._nodeMap = new HashMap();
        this._stepIds = new HashMap();
        this._nodeIds = new HashMap();
    }

    public function stepExists(step:Node):Boolean
    {
        return _nodeMap.keyExists(step);
    }

    public function getStepForNode(node:Node):FlexJiraStep
    {
        return _nodeMap.getValue(node) as FlexJiraStep;
    }

    public function getStepForId(stepId:int):FlexJiraStep
    {
        return _stepIds.getValue(stepId) as FlexJiraStep;
    }

    public function getNodeForStepId(stepId:int):Node
    {
        return _nodeIds.getValue(stepId) as Node;
    }

    public function hasStepId(id:int):Boolean
    {
        return _stepIds.keyExists(id);
    }

    public function addStep(node:Node, step:FlexJiraStep):void
    {
        _nodeMap.put(node, step);
        _stepIds.put(step.id, step);
        _nodeIds.put(step.id, node);
    }

    public function removeNode(node:Node):void
    {
        var step:FlexJiraStep = getStepForNode(node);
        _nodeMap.remove(node);
        _stepIds.remove(step.id);
        _nodeIds.remove(step.id);
    }

}