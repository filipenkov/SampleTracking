package com.sysbliss.diagram.renderer
{
	import com.sysbliss.diagram.Diagram;
	
	import mx.controls.Label;
	
	public class DefaultEdgeLabelRenderer extends AbstractEdgeLabelRenderer
	{
		protected var _label:Label;
		
		public function DefaultEdgeLabelRenderer(diagram:Diagram)
        {
			super(diagram);	
		}
		
		override protected function commitProperties():void
        {
			if(_edgeLabelChanged && _edgeLabel)
            {
				_label.text = _edgeLabel.text;
            	_label.x = ((width - _label.getExplicitOrMeasuredWidth())/2);
            	_label.y = ((height - _label.getExplicitOrMeasuredHeight())/2);
			}
			super.commitProperties();
		}

		override protected function createChildren():void
        {
			super.createChildren();
			_label = new Label();
            _label.styleName = this;
            addChild(_label);
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void
        {
			super.updateDisplayList(unscaledWidth, unscaledHeight);
            
            graphics.clear();
            graphics.lineStyle(2,0xFF0000);
            graphics.beginFill(0x0000FF,1);
            graphics.endFill();
            graphics.drawRoundRect(0,0,unscaledWidth,unscaledHeight,10,10);
		}
	}
}