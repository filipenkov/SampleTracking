package com.sysbliss.jira.workflow.layout
{
	//import __AS3__.vec.Vector;
	
	import com.sysbliss.collections.HashMap;
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.data.Edge;
import com.sysbliss.diagram.ui.StickyNote;
import com.sysbliss.diagram.ui.UINode;
	import com.sysbliss.jira.plugins.workflow.model.FlexJiraAction;
import com.sysbliss.jira.plugins.workflow.model.FlexJiraWorkflow;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;
import com.sysbliss.jira.plugins.workflow.model.layout.AnnotationLayout;
import com.sysbliss.jira.plugins.workflow.model.layout.AnnotationLayoutImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.EdgeLayout;
	import com.sysbliss.jira.plugins.workflow.model.layout.EdgeLayoutImpl;
	import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
	import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayoutImpl;
	import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPoint;
	import com.sysbliss.jira.plugins.workflow.model.layout.LayoutPointImpl;
	import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRect;
	import com.sysbliss.jira.plugins.workflow.model.layout.LayoutRectImpl;
	import com.sysbliss.jira.plugins.workflow.model.layout.NodeLayout;
	import com.sysbliss.jira.plugins.workflow.model.layout.NodeLayoutImpl;
	
	import flash.geom.Point;
	
	import mx.collections.ArrayCollection;
	import mx.core.UIComponent;
	
	public class LayoutExporter
	{
		private var _nodeLayoutMap:HashMap;
		private var _edgeLayoutMap:HashMap;
		private var _resetLabels:Boolean;

		public function LayoutExporter()
		{
		}
		
		public function export(diagram:Diagram, roots:Array,graphBounds:LayoutRect,workflow:FlexJiraWorkflow, resetLabels:Boolean = false):JWDLayout {
			_nodeLayoutMap = new HashMap();
			_edgeLayoutMap = new HashMap();
			_resetLabels = resetLabels;

			var newRoots:ArrayCollection = new ArrayCollection();
			
			var i:int;
			var uiRoot:UINode;
						
			for(i=0;i<roots.length;i++){
				uiRoot = roots[i] as UINode;
				var layoutRoot:NodeLayout = createReturnNode(uiRoot);
				if((uiRoot.node.data is FlexJiraAction)){
					layoutRoot.isInitialAction = true;
				}
				newRoots.addItem(layoutRoot);
			}

            var annotations:ArrayCollection = createAnnotations(diagram.getAllStickies());

			var layout:JWDLayout = new JWDLayoutImpl();
            layout.workflowName = workflow.name;
            layout.isDraftWorkflow = workflow.isDraftWorkflow;
			layout.width = graphBounds.width;
			layout.roots = newRoots;
            layout.graphBounds = graphBounds;
            layout.annotations = annotations;
			
			return layout;
		}

        private function createAnnotations(stickies:Array):ArrayCollection {
            var i:int;
            var annotations:ArrayCollection = new ArrayCollection();

            for(i = 0;i<stickies.length;i++) {
                var sticky:StickyNote = stickies[i];
                var annotation:WorkflowAnnotation = sticky.annotation.data as WorkflowAnnotation;
                var layout:AnnotationLayout = new AnnotationLayoutImpl();
                layout.id = annotation.id;

                var rect:LayoutRect = new LayoutRectImpl();
                rect.x = sticky.x;
                rect.y = sticky.y;
                rect.width = sticky.width;
                rect.height = sticky.height;

                layout.rect = rect;

                annotations.addItem(layout);
            }

            return annotations;

        }
		
		private function createReturnNode(uiNode:UINode):NodeLayout {
			var nodeLayout:NodeLayout = null;
			
			if (!_nodeLayoutMap.keyExists(uiNode.node.id)) {
				nodeLayout = new NodeLayoutImpl();
				_nodeLayoutMap.put(uiNode.node.id,nodeLayout);
				nodeLayout.stepId = uiNode.node.data.id;
				
				var rect:LayoutRect = new LayoutRectImpl();
				UIComponent(uiNode).validateNow();
				rect.x = uiNode.x;
				rect.y = uiNode.y;
				rect.width = UIComponent(uiNode).getExplicitOrMeasuredWidth();
				rect.height = UIComponent(uiNode).getExplicitOrMeasuredHeight();
				
				nodeLayout.rect = rect;
				nodeLayout.id = uiNode.node.id;
                nodeLayout.label = uiNode.node.data.name;
			} else {
				nodeLayout = _nodeLayoutMap.getValue(uiNode.node.id) as NodeLayout;
			}

			createReturnEdges(nodeLayout, uiNode);

			return nodeLayout;
    	}
    	
    	private function createReturnEdges(parentLayout:NodeLayout,parentUINode:UINode):void {
    		var edges:Vector.<Edge> = parentUINode.node.outLinks;
    		var edge:Edge;
    		var i:int;
    		for(i=0;i<edges.length;i++){
    			edge = edges[i];
    			if(!_edgeLayoutMap.keyExists(edge.id)){
    				createEdgeLayout(parentLayout,edge);
    			}
    		}
    	}
    	
    	private function createEdgeLayout(parentLayout:NodeLayout,edge:Edge):EdgeLayout {
    		var edgeLayout:EdgeLayout;
    		if(!_edgeLayoutMap.keyExists(edge.id)){
	    		edgeLayout = new EdgeLayoutImpl();
	    					
				edgeLayout.id = edge.id;
				_edgeLayoutMap.put(edge.id,edgeLayout);

                edgeLayout.label = edge.data.name;
				edgeLayout.actionId = edge.data.id;
				edgeLayout.startStepId = edge.startNode.data.id;
				edgeLayout.endStepId = edge.endNode.data.id;
				edgeLayout.startNode = parentLayout;
				parentLayout.outLinks.addItem(edgeLayout);
				edgeLayout.lineType = edge.uiEdge.lineType;
				
				var startPoint:LayoutPoint = new LayoutPointImpl();
				startPoint.x = edge.uiEdge.startPoint.x;
				startPoint.y = edge.uiEdge.startPoint.y;
				
				var endPoint:LayoutPoint = new LayoutPointImpl();
				endPoint.x = edge.uiEdge.endPoint.x;
				endPoint.y = edge.uiEdge.endPoint.y;

                var labelPoint:LayoutPoint = new LayoutPointImpl();
                if(!_resetLabels) {
                    labelPoint.x = edge.uiEdge.uiEdgeLabel.x;
				    labelPoint.y = edge.uiEdge.uiEdgeLabel.y;
                } else {
                    labelPoint.x = -200;
				    labelPoint.y = -200;
                }

			
				edgeLayout.startPoint = startPoint;
				edgeLayout.endPoint = endPoint;
                edgeLayout.labelPoint = labelPoint;
				
				var controlPoints:ArrayCollection = new ArrayCollection();
				var cpoints:Vector.<Point> = edge.uiEdge.controlPoints;
				var cpoint:Point;
				var posPoint:Point;
				var controlPoint:LayoutPoint;
				var posController:LayoutPoint;
				var i:int;
				for(i=0;i<cpoints.length;i++){
					cpoint = cpoints[i] as Point;
					controlPoint = new LayoutPointImpl();
					controlPoint.x = cpoint.x;
					controlPoint.y = cpoint.y;
					
					posPoint = edge.uiEdge.getPositiveControllerAt(i+1);
					posController = new LayoutPointImpl();
					posController.x = posPoint.x;
					posController.y = posPoint.y;
					
					controlPoint.positiveController = posController;
					
					controlPoints.addItem(controlPoint);
				}
				
				edgeLayout.controlPoints = controlPoints;
				
				var endNodeLayout:NodeLayout = createReturnNode(edge.endNode.uiNode);
				edgeLayout.endNode = endNodeLayout;
				endNodeLayout.inLinks.addItem(edgeLayout);
				
    		}else {
    			edgeLayout = _edgeLayoutMap.getValue(edge.id) as EdgeLayout;
    		}
			
			return edgeLayout;
    	}

	}
}