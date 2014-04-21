package com.sysbliss.jira.workflow.utils
{
import com.sysbliss.jira.workflow.Il8n.INiceResourceManager;

import com.sysbliss.jira.workflow.Il8n.NiceResourceManager;
import com.sysbliss.jira.workflow.ui.dialog.BaseDialog;

import flash.display.DisplayObject;
	
	import flexlib.mdi.containers.MDIWindow;
	import flexlib.mdi.managers.MDIManager;
	
	import mx.core.Application;
	import mx.core.IChildList;
	import mx.core.UIComponent;
	import mx.managers.ISystemManager;
	import mx.managers.PopUpManager;
	import mx.managers.PopUpManagerChildList;
	
	public class MDIDialogUtils
	{
		private static const _instance:MDIDialogUtils = new MDIDialogUtils(SingletonLock);
		private var _showingList:Array;

        private const niceResourceManager:INiceResourceManager = NiceResourceManager.getInstance();

		public function MDIDialogUtils(lock:Class)
		{
			if(lock != SingletonLock){  
				throw new Error( niceResourceManager.getString('json','workflow.designer.invalid.singleton.access') );
			}
			
			_showingList = new Array();
		}
		
		public static function popModalDialog(window:MDIWindow):void {
			var parent:DisplayObject = Application.application as DisplayObject;

			MDIManager.global.add(window);
			Application.application.contextMenu = null;
			PopUpManager.removePopUp(window);
			PopUpManager.addPopUp(window,parent,true,PopUpManagerChildList.POPUP);
			PopUpManager.centerPopUp(window);
			_instance._showingList.push(window);

            if(window is BaseDialog) {
                BaseDialog(window).onShowDialog();
            }
		}
		
		public static function removeModalDialog(window:MDIWindow):void {
			MDIManager.global.remove(window);
			var i:int = _instance._showingList.indexOf(window);
			if(i > -1){
				_instance._showingList.splice(i,1);
			}
		}
		
		public static function removeAllDialogs():void {
			var sysMan:ISystemManager = Application.application.systemManager;
			var popups:IChildList = sysMan.popUpChildren;
			var i:int;
			var popup:DisplayObject;
			var toRemove:Array = new Array();
			for(i=0;i<popups.numChildren;i++){
				popup = popups.getChildAt(i) as UIComponent;
				if(popup){
					toRemove.push(popup);
				}
			}
			
			var index:int;
			for(i=0;i<toRemove.length;i++){
				PopUpManager.removePopUp(toRemove[i] as UIComponent);
				index = _instance._showingList.indexOf(toRemove[i]);
				if(index > -1){
					_instance._showingList.splice(index,1);
				}
			}
			
		}
		
		public static function isShowing(window:MDIWindow):Boolean {
			var showingList:Array = _instance._showingList;
			var showing:Boolean = false;
			var i:int;
			for(i=0;i<showingList.length;i++){
				if(window == showingList[i]){
					showing = true;
					break;
				}
			}
			return showing;
		}
		

	}
}

class SingletonLock{};

