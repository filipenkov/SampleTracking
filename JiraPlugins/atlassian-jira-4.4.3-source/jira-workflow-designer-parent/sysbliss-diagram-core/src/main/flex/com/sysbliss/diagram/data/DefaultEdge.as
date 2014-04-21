package com.sysbliss.diagram.data
{
	import com.sysbliss.diagram.ui.DiagramUIObject;
	import com.sysbliss.diagram.ui.UIEdge;
	
	import mx.core.UIComponent;
	
	
	public class DefaultEdge extends AbstractDiagramObject implements Edge
	{
		protected var _startNode:Node;
		protected var _endNode:Node;
		protected var _uiEdge:UIEdge;
		
		public function DefaultEdge(startNode:Node,endNode:Node,dataObj:Object=null)
		{
			super(dataObj);
			this.startNode = startNode;
			this.endNode = endNode;
			this.startNode.addOutLink(this);
			this.endNode.addInLink(this);
		}
		
		
		[Bindable]
		public function get startNode():Node
		{
			return _startNode;
		}
		
		public function set startNode(n:Node):void
		{
			_startNode = n;
		}
		
		[Bindable]
		public function get endNode():Node
		{
			return _endNode;
		}
		
		public function set endNode(n:Node):void
		{
			var oldEndNode:Node = _endNode;
			if(oldEndNode && n){
				oldEndNode.removeInLink(this);
				_endNode = n;
				_endNode.addInLink(this);
				_uiEdge.endPoint = n.uiNode.centerPoint;
				UIComponent(_uiEdge).validateNow();
			}else {
				_endNode = n;
			}
			
		}
		
		override public function get uiObject():DiagramUIObject {
			return _uiEdge as DiagramUIObject;	
		}
		
		public function set uiEdge(uiEdge:UIEdge):void {
			this._uiEdge = uiEdge;
		}
		
		public function get uiEdge():UIEdge {
			return _uiEdge;
		}

        override public function set data(value:Object):void
		{
			super.data = value;
            if(_uiEdge != null) {
                _uiEdge.updateLabel();
            }
		}

	}
}