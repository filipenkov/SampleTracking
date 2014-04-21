package com.sysbliss.diagram.ui
{
import com.sysbliss.diagram.Diagram;
import com.sysbliss.diagram.ui.selectable.SelectionManagerFactory;
	import com.sysbliss.diagram.ui.selectable.SelectionManagerTypes;
	
	import flash.geom.Point;
	
	import mx.core.UIComponent;
	
	public class UIControlPointController extends AbstractUIKnot
	{
		private var _sibling:UIControlPointController;
		protected var _isPositive:Boolean;
		protected var _isNegative:Boolean;
		
		public function UIControlPointController(d:Diagram)
		{
			super(d);
			this._selectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.POINT_CONTROLS);
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
			super.updateDisplayList(unscaledWidth,unscaledHeight);
			if(_knotRenderer != null){
				var edgeAngle:Number = 0;
				if(_sibling){
					var deltaX:Number = (x - _sibling.x);
					var deltaY:Number = (y - _sibling.y);
					edgeAngle = Math.atan2(deltaY,deltaX);
				}

				_knotRenderer.draw(graphics,isSelected,edgeAngle);
			}
		}
		
		override public function move(newX:Number, newY:Number):void {
			if(!(newX == x) || !(newY == y)){
				var diffX:Number = (newX - x);
				var diffY:Number = (newY - y);
				
				super.move(newX,newY);
				
				_sibling.x = (_sibling.x - diffX);
				_sibling.y = (_sibling.y - diffY);
				
				var midX:Number = (newX + (_knotRenderer.getBounds().width/2));
				var midY:Number = (newY + (_knotRenderer.getBounds().height/2));
				
				if(_isPositive){
					_uiEdge.movePositiveController(point,newX,newY);
				} else if(_isNegative){
					_uiEdge.moveNegativeController(point,newX,newY);

				}

                UIComponent(_uiEdge).validateNow();
                
				//need to invalidate and validate to make proper handle rotation happen				
				invalidateDisplayList();
				validateNow();
				
				_sibling.invalidateDisplayList();
				_sibling.validateNow();
			}
		}
		
		override public function select(quiet:Boolean = false) : void
		{
			super.select(quiet);
			invalidateDisplayList();
		}
		
		override public function deselect(quiet:Boolean = false):void
		{
			super.deselect(quiet);
			invalidateDisplayList();
		}
		
		public function set sibling(p:UIControlPointController):void {
			this._sibling = p;
		}
		
		public function set isPositive(b:Boolean):void {
			this._isPositive = b;
		}
		
		public function get isPositive():Boolean {
			return _isPositive;
		}
		
		public function set isNegative(b:Boolean):void {
			this._isNegative = b;
		}
		
		public function get isNegative():Boolean {
			return _isNegative;
		}
		
	}
}