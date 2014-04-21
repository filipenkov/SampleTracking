package com.sysbliss.diagram.manager
{

	import com.sysbliss.collections.HashMap;
	import com.sysbliss.diagram.Diagram;
	import com.sysbliss.diagram.renderer.KnotRenderer;
	import com.sysbliss.diagram.ui.UIControlPointController;
	import com.sysbliss.diagram.ui.UIKnot;
	import com.sysbliss.diagram.ui.selectable.SelectionManager;
	import com.sysbliss.diagram.ui.selectable.SelectionManagerFactory;
	import com.sysbliss.diagram.ui.selectable.SelectionManagerTypes;
	
	import flash.display.DisplayObject;
	
	import mx.core.UIComponent;
	
	public class ControllerKnotManager
	{
		private static const _instances:HashMap = new HashMap();
		private var _knots:Vector.<UIKnot>;
		private var _needsKnots:Boolean;
		
		public function ControllerKnotManager(lock:Class)
		{
			if(lock != SingletonLock){  
				throw new Error( "Invalid Singleton access." );  
			}
			this._knots = new Vector.<UIKnot>(2,true);
			this._needsKnots = true;
		}
		
		private static function getInstance(diagram:Diagram):ControllerKnotManager {
			var manager:ControllerKnotManager;
			if(!_instances.keyExists(diagram)){
				manager = new ControllerKnotManager(SingletonLock);
				_instances.put(diagram,manager);
			} else {
				manager = _instances.getValue(diagram) as ControllerKnotManager;
			}
			
			return manager;
		}
		
		public static function getControllerKnots(diagram:Diagram):Vector.<UIKnot> {
			var manager:ControllerKnotManager = getInstance(diagram);
			if(manager._needsKnots){
				manager._needsKnots = false;
				var conrollerSelMan:SelectionManager = SelectionManagerFactory.getSelectionManager(SelectionManagerTypes.POINT_CONTROLS + "_" + UIComponent(diagram).uid);
				var controller0:UIControlPointController = new UIControlPointController(diagram);
				var controller1:UIControlPointController = new UIControlPointController(diagram);
				controller0.selectionManager = conrollerSelMan;
				controller1.selectionManager = conrollerSelMan;
				
				manager._knots[0] = controller0;
				manager._knots[1] = controller1;
				
				var renderer:KnotRenderer = ClassInstanceManager.getClassInstance(diagram.defaultKnotControllerRenderer) as KnotRenderer;
				manager._knots[0].knotRenderer = renderer;
				manager._knots[1].knotRenderer = renderer;
				
				manager.addKnotsToDiagram(diagram);
			}
			
			return manager._knots;
		}
		
		private function addKnotsToDiagram(diagram:Diagram):void {
			_knots[0].visible = false;
			_knots[1].visible = false;
			diagram.controlsLayer.addChild(DisplayObject(_knots[0]));
			diagram.controlsLayer.addChild(DisplayObject(_knots[1]));
		}

	}
}

class SingletonLock{};
