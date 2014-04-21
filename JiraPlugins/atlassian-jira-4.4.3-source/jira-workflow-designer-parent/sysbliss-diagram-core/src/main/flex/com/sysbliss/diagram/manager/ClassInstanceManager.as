package com.sysbliss.diagram.manager
{
	import com.sysbliss.collections.HashMap;
	
	public class ClassInstanceManager
	{
		private static const _instance:ClassInstanceManager = new ClassInstanceManager(SingletonLock);
		
		private var _objects:HashMap;
		
		public function ClassInstanceManager(lock:Class)
		{
			if(lock != SingletonLock){  
				throw new Error( "Invalid Singleton access." );  
			}
			
			this._objects = new HashMap();
		}
		
		public static function getClassInstance(clazz:Class):Object {
			var obj:Object;
			
			if(!_instance._objects.keyExists(clazz)){
				obj = new clazz();
				_instance._objects.put(clazz,obj);
			} else {
				obj = _instance._objects.getValue(clazz);
			}
			
			return obj;
		}

	}
}

class SingletonLock{};

