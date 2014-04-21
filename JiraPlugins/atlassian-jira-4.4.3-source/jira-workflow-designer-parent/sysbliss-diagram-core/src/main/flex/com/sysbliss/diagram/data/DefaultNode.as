package com.sysbliss.diagram.data
{
	
	
	import com.sysbliss.diagram.ui.DiagramUIObject;
	import com.sysbliss.diagram.ui.UINode;
	
	import mx.utils.UIDUtil;
	
	public class DefaultNode extends AbstractDiagramObject implements Node
	{

		protected var _inLinks:Vector.<Edge>;
		protected var _outLinks:Vector.<Edge>;
		protected var _uiNode:UINode;
		
		public function DefaultNode(dataObj:Object=null)
		{
			super(dataObj);
			this._inLinks = new Vector.<Edge>();
			this._outLinks = new Vector.<Edge>();
		}
		
				
		public function addInLink(edge:Edge):void {
			var i:int = _inLinks.indexOf(edge);
			if(i<0){
				_inLinks.push(edge);
			}
		}
		
		public function removeInLink(edge:Edge):void {
			var i:int = _inLinks.indexOf(edge);
			if(i>-1){
				_inLinks.splice(i,1);
			}
		}
		
		public function get inLinks():Vector.<Edge> {
			return _inLinks;
		}
		
		public function addOutLink(edge:Edge):void {
			var i:int = _outLinks.indexOf(edge);
			if(i<0){
				_outLinks.push(edge);
			}
		}
		
		public function removeOutLink(edge:Edge):void {
			var i:int = _outLinks.indexOf(edge);
			if(i>-1){
				_outLinks.splice(i,1);
			}
		}
		
		public function get outLinks():Vector.<Edge> {
			return _outLinks;
		}
		
		public function get predecessors():Vector.<Node>{
			var i:int;
			var nodes:Vector.<Node> = new Vector.<Node>();
			for(i=0;i<_inLinks.length;i++){
                if(_inLinks[i].startNode != this) {
                    nodes.push(_inLinks[i].startNode);
                }

			}
			return nodes;
		}
		public function get successors():Vector.<Node> {
			var i:int;
			var nodes:Vector.<Node> = new Vector.<Node>();
			for(i=0;i<_outLinks.length;i++){
                if(_outLinks[i].endNode != this) {
                    nodes.push(_outLinks[i].endNode);
                }

			}
			return nodes;
		}
		
		override public function get uiObject():DiagramUIObject {
			return _uiNode as DiagramUIObject;	
		}
		
		public function set uiNode(uiNode:UINode):void {
			this._uiNode = uiNode;
		}
		
		public function get uiNode():UINode {
			return _uiNode;
		}
		
	}
}