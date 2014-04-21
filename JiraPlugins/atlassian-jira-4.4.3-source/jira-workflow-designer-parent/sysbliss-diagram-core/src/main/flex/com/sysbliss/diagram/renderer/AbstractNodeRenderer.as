package com.sysbliss.diagram.renderer
{
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.data.Node;
	import com.sysbliss.util.AbstractClassEnforcer;
	
	import mx.containers.Canvas;

	public class AbstractNodeRenderer extends Canvas implements NodeRenderer
	{
		protected var _node:Node;
		protected var _diagram:Diagram;
		protected var _nodeChanged:Boolean;
		
		public function AbstractNodeRenderer(diagram:Diagram)
		{
			super();
			AbstractClassEnforcer.enforceConstructor(this,AbstractNodeRenderer);
			_diagram = diagram;
		}
		
		override protected function commitProperties():void
        {
			super.commitProperties();
			if(_nodeChanged)
            {
				_nodeChanged = false;
			}
			invalidateDisplayList();
		}
		
		public function set node(value:Node):void
		{
			_nodeChanged = true;
			_node = value;
			invalidateProperties();
		}
		
		public function get node():Node
		{
			return _node;
		}
		
		public function get diagram():Diagram
        {
			return _diagram;
		}
	}
}