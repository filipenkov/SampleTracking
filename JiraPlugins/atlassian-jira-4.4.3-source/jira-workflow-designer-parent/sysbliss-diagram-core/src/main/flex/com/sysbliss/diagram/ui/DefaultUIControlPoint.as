package com.sysbliss.diagram.ui
{
import com.sysbliss.diagram.Diagram;

import flash.events.KeyboardEvent;
import flash.geom.Point;
import flash.ui.Keyboard;
	
	import mx.core.UIComponent;
	
	public class DefaultUIControlPoint extends AbstractUIControlPoint
	{
		public function DefaultUIControlPoint(d:Diagram)
		{
			super(d);
		}
		
		override protected function updateDisplayList(unscaledWidth:Number, unscaledHeight:Number):void {
			super.updateDisplayList(unscaledWidth,unscaledHeight);
			if(_knotRenderer != null){
				_knotRenderer.draw(graphics,isSelected);
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
		
		override public function move(newX:Number, newY:Number):void {
			if(!(newX == x) || !(newY == y)){

                var point:Point = new Point(newX, newY);
                applyConstraints(point);

				super.move(point.x,point.y);

				if(!_isStart && !_isEnd){
					_uiEdge.moveControlPoint(_point,point.x,point.y);
				} else if(_isStart){
					_uiEdge.moveStartPoint(point.x,point.y);
				} else if(_isEnd){
					_uiEdge.moveEndPoint(point.x,point.y);
				}
				
				UIComponent(_uiEdge).invalidateDisplayList();
				UIComponent(_uiEdge).validateNow();
			}
		}
		
		override protected function onKeyDown(e:KeyboardEvent):void {
			if(e.keyCode == Keyboard.DELETE){
				e.stopPropagation();
				_uiEdge.removeSelectedControlPoints();
			} else {
				super.onKeyDown(e);
			}

		}
		
	}
}