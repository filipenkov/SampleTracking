package com.sysbliss.diagram.ui
{
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.ToolTypes;
	import com.sysbliss.diagram.data.Node;
	import com.sysbliss.diagram.manager.EdgeControlPointManager;
	import com.sysbliss.diagram.renderer.DirectedEdgeRenderer;
    import com.sysbliss.diagram.renderer.EdgeRenderer;
import com.sysbliss.diagram.util.ObjectChainUtil;
import com.sysbliss.util.PointUtils;
	
	import flash.events.MouseEvent;
	import flash.geom.Point;

import mx.core.UIComponent;

import mx.core.UIComponent;
import mx.events.FlexEvent;

	public class UIEdgePreviewImpl extends DefaultUIEdge
	{
		protected var _startNode:Node;
		protected var _endNode:Node;
		private var _previousPoint:Point;
		
		public function UIEdgePreviewImpl(diagram:Diagram,edgeRenderer:EdgeRenderer,edgeLabelRendererClass:Class,controlPointManager:EdgeControlPointManager)
		{
			super(diagram,null,edgeRenderer,edgeLabelRendererClass,null,controlPointManager);
			this.selectable = false;
			this.mouseEnabled = false;

		}
		
		override protected function init(event:FlexEvent):void {
			//do nothing
		}

		
		override protected function updateDisplayList(w:Number, h:Number):void {
			if(edgeRenderer && (edgeRenderer is DirectedEdgeRenderer)){
				DirectedEdgeRenderer(edgeRenderer).eraseUnderArrow = false;
			}
			super.updateDisplayList(w,h);
		}
		
		public function clear():void {
			graphics.clear();
			this._startNode = null;
			this._endNode = null;
			this._previousPoint = null;
			this._startPoint = new Point(0,0);
			this._endPoint = new Point(0,0);
			this._controlPoints = new Vector.<Point>();
		}
		
		public function startPreview(startNode:Node,startPoint:Point):void {
			clear();
			_startNode = startNode;
            UIComponent(_startNode.uiNode).mouseChildren = false;
			this.startPoint = startPoint;
			this.endPoint = new Point(startPoint.x,startPoint.y);
			this._previousPoint = startPoint;
			_diagram.addEventListener(MouseEvent.MOUSE_MOVE,onDiagramMouseMove);
			_diagram.addEventListener(MouseEvent.MOUSE_UP,onDiagramMouseUp);
            UIComponent(_diagram).setFocus();
		}
		
		public function completeLink(endNode:Node):Boolean {
			    var linkNotValid:Boolean = true;

                _diagram.stopScrollTimer();
                if(endNode) {
                    this._endNode = endNode;
                    linkNotValid = false;
                    endPoint = PointUtils.convertCoordinates(new Point(mouseX,mouseY),this,_diagram.edgeLayer);
                }

				_diagram.removeEventListener(MouseEvent.MOUSE_MOVE,onDiagramMouseMove);
				_diagram.removeEventListener(MouseEvent.MOUSE_UP,onDiagramMouseUp);

			return linkNotValid;
		}
		
		private function onDiagramMouseUp(e:MouseEvent):void {
			if(e.currentTarget == _diagram && !ObjectChainUtil.ancestorsContainType(e.target,UINode)){
				if(lineType != ToolTypes.LINK_STRAIGHT.name){
					var pt:Point = new Point(e.stageX,e.stageY);
					pt = _diagram.edgeLayer.globalToLocal(pt);
					_previousPoint = pt;
					addControlPoint(_previousPoint);
				}
			}
		}
		
		private function onDiagramMouseMove(e:MouseEvent):void {

			var pt:Point = new Point(e.stageX,e.stageY);
			pt = _diagram.edgeLayer.globalToLocal(pt);
			moveEndPointPreview(pt.x,pt.y);
			validateNow();
			_diagram.startScrollTimer();
		}
		
		override public function set lineType(type:String):void
		{
			_lineTypeChanged = true;
			_lineType = type;
			invalidateProperties();
		}
		
		override protected function onMouseDown(event:MouseEvent):void {
		}
		
		override protected function onMouseUp(event:MouseEvent):void {
		}
		
		public function set edgeRenderer(e:EdgeRenderer):void {
			this._renderer = e;
		}
		
		public function get startNode():Node {
			return _startNode;
		}
		
		public function get endNode():Node {
			return _endNode;
		}
	}
}