package com.sysbliss.diagram.ui.selectable
{
	import com.sysbliss.collections.HashMap;
	
	public class SelectionManagerFactory
	{
		private static const _instance:SelectionManagerFactory = new SelectionManagerFactory(SingletonLock);
		
		private var _managers:HashMap;
		
		public function SelectionManagerFactory(lock:Class)
		{
			if(lock != SingletonLock){  
				throw new Error( "Invalid Singleton access.  Use SelectionManagerFactory.instance" );  
			}
			
			this._managers = new HashMap();
		}
		
		public static function getSelectionManager(key:String):SelectionManager {
			var manager:SelectionManager;
			
			if(!_instance._managers.keyExists(key)){
				manager = new SelectionManagerImpl();
				_instance._managers.put(key,manager);
			} else {
				manager = _instance._managers.getValue(key) as SelectionManager;
			}
			
			return manager;
		}

	}
}

class SingletonLock{};
