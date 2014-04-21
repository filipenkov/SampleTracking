package com.sysbliss.diagram.manager
{

	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.ToolTypes;
	import com.sysbliss.diagram.event.DiagramEvent;
	import com.sysbliss.diagram.event.SelectableEvent;
	import com.sysbliss.diagram.renderer.KnotRenderer;
	import com.sysbliss.diagram.ui.DefaultUIControlPoint;
import com.sysbliss.diagram.ui.InteractiveDiagramObject;
import com.sysbliss.diagram.ui.UIControlPoint;
	import com.sysbliss.diagram.ui.UIEdge;
	import com.sysbliss.diagram.ui.UINode;
	import com.sysbliss.diagram.ui.selectable.SelectionManager;
	import com.sysbliss.diagram.ui.selectable.SelectionManagerFactory;
	import com.sysbliss.diagram.ui.selectable.SelectionManagerTypes;
	
	import flash.display.DisplayObject;
	import flash.geom.Point;
	import flash.geom.Rectangle;
	
	import mx.core.UIComponent;
	
	public class EdgeControlPointManager
	{
		private var _diagram:Diagram;
		private var _selectionManager:SelectionManager;
		private var _controlsVisible:Boolean;
		private var _currentUIEdge:UIEdge;
		private var _knots:Vector.<UIControlPoint>;
		
		public function EdgeControlPointManager(diagram:Diagram,manager:SelectionManager)
		{
			this._diagram = diagram;
			this._selectionManager = manager;
			this._controlsVisible = false;
			this._knots = new Vector.<UIControlPoint>();
			
			_selectionManager.addEventListener(SelectableEvent.SELECTED,onObjectSelected);
			_selectionManager.addEventListener(SelectableEvent.SELECT_ALL,onSelectAll);
			_selectionManager.addEventListener(SelectableEvent.SELECTION_ADDED,onSelectionAdded);
			_selectionManager.addEventListener(SelectableEvent.SELECTION_REMOVED,onSelectionRemoved);
			_selectionManager.addEventListener(SelectableEvent.DESELECT_ALL,onDeselectAll);
			
		}
		
		private function onObjectSelected(e:SelectableEvent):void {
			if((e.data is UIEdge) && (!(_currentUIEdge === e.data) || !_controlsVisible)){
				hideControls();
				var currentEdge:UIEdge = e.data as UIEdge;
				currentEdge.addEventListener(DiagramEvent.EDGE_CONTROL_POINT_INSERTED,onControlPointInserted);
				showControls(currentEdge);
			} else if(_controlsVisible) {
				hideControls();				
			}
		}
		
		private function onSelectAll(e:SelectableEvent):void {
			if(_controlsVisible){
				hideControls();
			}
		}
		
		private function onSelectionAdded(e:SelectableEvent):void {
			
			if(_selectionManager.numSelected > 1 && _controlsVisible){
				hideControls();
			} else if(_selectionManager.numSelected == 1 && (_selectionManager.currentlySelected[0] is UIEdge)){
				hideControls();
				var currentEdge:UIEdge = _selectionManager.currentlySelected[0] as UIEdge;
				
				showControls(currentEdge);
				currentEdge.addEventListener(DiagramEvent.EDGE_CONTROL_POINT_INSERTED,onControlPointInserted);
			}
		}
		
		private function onSelectionRemoved(e:SelectableEvent):void {
			//todo: try to figure out if there's only a single selected edge left and show controls for it
			var currentEdge:UIEdge;
			if(_selectionManager.numSelected < 1 && _controlsVisible){
				hideControls();
				if(e.data is UIEdge){
					currentEdge = e.data as UIEdge;
					currentEdge.removeEventListener(DiagramEvent.EDGE_CONTROL_POINT_INSERTED,onControlPointInserted);
				}
			} else if(_selectionManager.numSelected == 1 && (_selectionManager.currentlySelected[0] is UIEdge)){
				hideControls();
				currentEdge = _selectionManager.currentlySelected[0] as UIEdge;
				
				showControls(currentEdge);
				currentEdge.addEventListener(DiagramEvent.EDGE_CONTROL_POINT_INSERTED,onControlPointInserted);
			}
		}
		
		private function onControlPointInserted(e:DiagramEvent):void {
			var uiEdge:UIEdge = _diagram.getUIEdge(e.data.edge.id);
			showControls(uiEdge);
		}
		
		private function onDeselectAll(e:SelectableEvent):void {
			if(_controlsVisible){
				hideControls();
			}
		}
		
		
		public function hideControls():void {
			var i:int;
			for(i=0;i<_knots.length;i++){
				_knots[i].visible = false;
				_knots[i].selectionManager.deselectAll();
			}
			_controlsVisible = false;
			_currentUIEdge = null;
		}
		
		public function showControls(uiEdge:UIEdge):void {
			var renderer:KnotRenderer = ClassInstanceManager.getClassInstance(uiEdge.knotRendererClass) as KnotRenderer;
			var startPoint:Point = uiEdge.startPoint;
			var endPoint:Point = uiEdge.endPoint;
			var controlPoints:Vector.<Point> = uiEdge.controlPoints;
			var showCPoints:Boolean = false;
			if(uiEdge.lineType != ToolTypes.LINK_STRAIGHT.name){
				showCPoints = true;
				createKnots((controlPoints.length + 2));
			} else {
				showCPoints = false;
				createKnots(2);
			}
						
			var startUI:UINode = _diagram.getUINode(uiEdge.edge.startNode.id);
			var endUI:UINode = _diagram.getUINode(uiEdge.edge.endNode.id);
			var startBounds:Rectangle = new Rectangle(startUI.x,startUI.y,startUI.width,startUI.height);
			var endBounds:Rectangle = new Rectangle(endUI.x,endUI.y,endUI.width,endUI.height);
			
			configureKnot(_knots[0],renderer,uiEdge,startPoint,startBounds);
			_knots[0].isStart = true;
			_knots[0].isEnd = false;
			
			if(showCPoints){
				var i:int = 0;
				for(i=0;i<controlPoints.length;i++){
					configureKnot(_knots[i+1],renderer,uiEdge,controlPoints[i]);
					_knots[i+1].isStart = false;
					_knots[i+1].isEnd = false;
				}
			}
			configureKnot(_knots[_knots.length-1],renderer,uiEdge,endPoint,endBounds);
			_knots[_knots.length-1].isStart = false;
			_knots[_knots.length-1].isEnd = true;
			
			_controlsVisible = true;
			_currentUIEdge = uiEdge;
		}
		
		private function configureKnot(knot:UIControlPoint,knotRenderer:KnotRenderer,uiEdge:UIEdge,point:Point,bounds:Rectangle=null):void {
			knot.knotRenderer = knotRenderer;
			knot.uiEdge = uiEdge;
			knot.point = point;
			knot.x = point.x;
			knot.y = point.y;
			InteractiveDiagramObject(knot).dragBounds = bounds;

			knot.visible = true;
				
		}
		
		private function createKnots(count:int):void {
			var knotsNeeded:int = (count - _knots.length);
			var knotSelMan:SelectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.EDGE_CONTROLS + "_" + UIComponent(_diagram).uid);
				
			if(knotsNeeded > 0){
				var i:int;
				var knot:UIControlPoint;
				for(i=0;i<knotsNeeded;i++){
					knot = new DefaultUIControlPoint(_diagram);
					knot.selectionManager = knotSelMan;
					_knots.push(knot);
					_diagram.controlsLayer.addChild(knot as DisplayObject);
					knot.visible = false;
				}
			}
		}
		
		public function get currentUIEdge():UIEdge {
			return _currentUIEdge;
		}

	}
}