package com.sysbliss.diagram.renderer
{
	import com.sysbliss.diagram.Diagram;
    import com.sysbliss.diagram.data.Edge;
    import com.sysbliss.diagram.data.EdgeLabel;
	import com.sysbliss.util.AbstractClassEnforcer;
	
	import mx.containers.Canvas;
import mx.containers.HBox;

public class AbstractEdgeLabelRenderer extends HBox implements EdgeLabelRenderer
	{
		protected var _edgeLabel:EdgeLabel;
		protected var _diagram:Diagram;
		protected var _edgeLabelChanged:Boolean;
        protected var _edge:Edge;
		
		public function AbstractEdgeLabelRenderer(diagram:Diagram)
		{
			super();
			AbstractClassEnforcer.enforceConstructor(this,AbstractEdgeLabelRenderer);
			_diagram = diagram;
		}
		
		override protected function commitProperties():void
        {
			super.commitProperties();
			if(_edgeLabelChanged)
            {
				_edgeLabelChanged = false;
			}
			invalidateDisplayList();
		}

        public function highlight():void {
            //override if needed
        }

        public function unhighlight():void {
            //override if needed
        }

		public function set edgeLabel(value:EdgeLabel):void
		{
			_edgeLabelChanged = true;
			_edgeLabel = value;
			invalidateProperties();
		}
		
		public function get edgeLabel():EdgeLabel
		{
			return _edgeLabel;
		}

		public function set edge(value:Edge):void
		{
			_edge = value;
		}

		public function get edge():Edge
		{
			return _edge;
		}
		
		public function get diagram():Diagram
        {
			return _diagram;
		}
    }
}