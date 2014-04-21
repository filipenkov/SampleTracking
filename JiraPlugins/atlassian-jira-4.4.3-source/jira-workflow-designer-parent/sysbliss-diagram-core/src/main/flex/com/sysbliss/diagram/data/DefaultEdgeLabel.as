package com.sysbliss.diagram.data
{
	import com.sysbliss.diagram.ui.DiagramUIObject;
    import com.sysbliss.diagram.ui.UIEdgeLabel;
	
	public class DefaultEdgeLabel extends AbstractDiagramObject implements EdgeLabel
	{
		protected var _uiEdgeLabel:UIEdgeLabel;

        protected var _text:String;

		public function DefaultEdgeLabel(dataObj:Object=null)
		{
			super(dataObj);
		}

		override public function get uiObject():DiagramUIObject
        {
			return _uiEdgeLabel as DiagramUIObject;
		}

        public function set uiEdgeLabel(uiEdgeLabel:UIEdgeLabel):void
        {
            this._uiEdgeLabel = uiEdgeLabel;
        }

        public function get uiEdgeLabel():UIEdgeLabel
        {
            return _uiEdgeLabel;
        }

        public function get text():String {
            return _text;
        }

        public function set text(value:String):void {
            this._text = value;

            // force renderer to update its label
            // is there a clearer way to do this?
            _uiEdgeLabel.edgeLabelRenderer.edgeLabel = this;
        }
    }
}