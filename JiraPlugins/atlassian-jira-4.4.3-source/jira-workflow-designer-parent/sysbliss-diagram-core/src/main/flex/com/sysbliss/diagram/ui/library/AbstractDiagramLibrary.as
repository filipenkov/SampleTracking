package com.sysbliss.diagram.ui.library
{
	import com.sysbliss.collections.HashMap;
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.renderer.DiagramObjectRenderer;
    import com.sysbliss.diagram.renderer.EdgeLabelRenderer;
    import com.sysbliss.diagram.renderer.EdgeRenderer;
	import com.sysbliss.diagram.renderer.NodeRenderer;
	import com.sysbliss.util.AbstractClassEnforcer;
	
	public class AbstractDiagramLibrary implements DiagramLibrary
	{
		public static const DEFAULT_GROUP:String = "defaultGroup";
		
		protected var _nodeGroups:HashMap;
		protected var _edgeGroups:HashMap;
		protected var _edgeLabelGroups:HashMap;
	
		public function AbstractDiagramLibrary()
		{
			AbstractClassEnforcer.enforceConstructor(this,AbstractDiagramLibrary);
			this._nodeGroups = new HashMap();
			this._edgeGroups = new HashMap();
			this._edgeLabelGroups = new HashMap();
		}
		
		public function addNodeRenderer(name:String,renderer:Class,group:String=null):void{
			var nodeMap:HashMap = getNodeGroup(group);
			nodeMap.put(name,renderer);
		}
		
		public function getNodeRendererInstance(name:String,diagram:Diagram,group:String=null):NodeRenderer{
			var nodeMap:HashMap = getNodeGroup(group);
			return getRendererInstance(name,nodeMap,diagram) as NodeRenderer;
		}
		
		public function getNodeRendererClass(name:String,group:String=null):Class{
			var nodeMap:HashMap = getNodeGroup(group);
			return nodeMap.getValue(name) as Class;
		}
		
		public function getNodeGroup(group:String):HashMap {
			return getGroup(_nodeGroups,group);
		}
		
		public function getAllNodeGroups():HashMap {
			return _nodeGroups;
		}
		
		public function hasNodeRenderer(type:String,group:String=null):Boolean {
			return hasRenderer(_nodeGroups,type,group);
		}
		
		public function addEdgeRenderer(name:String,renderer:Class,group:String=null):void{
			var edgeMap:HashMap = getEdgeGroup(group);
			edgeMap.put(name,renderer);
		}
		
		public function getEdgeRendererInstance(name:String,diagram:Diagram,group:String=null):EdgeRenderer{
			var edgeMap:HashMap = getEdgeGroup(group);
			return getRendererInstance(name,edgeMap,diagram) as EdgeRenderer;
		}
		
		public function getEdgeRendererClass(name:String,group:String=null):Class{
			var edgeMap:HashMap = getEdgeGroup(group);
			return edgeMap.getValue(name) as Class;
		}
		
		public function getEdgeGroup(group:String):HashMap {
			return getGroup(_edgeGroups,group);
		}
		
		public function getAllEdgeGroups():HashMap {
			return _edgeGroups;
		}
		
		public function hasEdgeRenderer(type:String,group:String=null):Boolean {
			return hasRenderer(_edgeGroups,type,group);
		}
		
		protected function getRendererInstance(name:String,objMap:HashMap,diagram:Diagram):DiagramObjectRenderer
		{
			var renderClass:Class = objMap.getValue(name);
			
			var tmpRenderer:Object = new renderClass(diagram);
			
			return tmpRenderer as DiagramObjectRenderer;
		}
		protected function hasRenderer(groupMap:HashMap,type:String,group:String=null):Boolean {
			var innerMap:HashMap = getGroup(groupMap,group);
			return innerMap.keyExists(type);
		}
		
		protected function getGroup(groupMap:HashMap,group:String):HashMap {
			var _group:String = group;
			if(_group == null){
				_group = DEFAULT_GROUP;
			}
			
			if(!groupMap.keyExists(_group)){
				groupMap.put(_group,new HashMap());
			}
			
			return groupMap.getValue(_group);
		}

        public function addEdgeLabelRenderer(name:String, renderer:Class, group:String = null):void {
			var edgeLabelMap:HashMap = getEdgeLabelGroup(group);
			edgeLabelMap.put(name,renderer);
        }

        public function getEdgeLabelRendererInstance(name:String, diagram:Diagram, group:String = null):EdgeLabelRenderer {
			var edgeLabelMap:HashMap = getEdgeLabelGroup(group);
			return getRendererInstance(name,edgeLabelMap,diagram) as EdgeLabelRenderer;
        }

        public function getEdgeLabelRendererClass(name:String, group:String = null):Class {
			var edgeLabelMap:HashMap = getEdgeLabelGroup(group);
			return edgeLabelMap.getValue(name) as Class;
        }

        public function getEdgeLabelGroup(group:String):HashMap {
			return getGroup(_edgeLabelGroups,group);
        }

        public function getAllEdgeLabelGroups():HashMap {
			return _edgeLabelGroups;
        }

        public function hasEdgeLabelRenderer(type:String, group:String = null):Boolean {
			return hasRenderer(_edgeLabelGroups,type,group);
        }
    }
}
