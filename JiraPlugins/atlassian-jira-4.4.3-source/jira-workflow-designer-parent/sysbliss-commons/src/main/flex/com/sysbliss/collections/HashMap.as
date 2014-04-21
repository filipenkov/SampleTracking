package com.sysbliss.collections
{
	
	import com.sysbliss.util.ClassLogger;
	
	import flash.utils.Dictionary;
	
	import mx.logging.ILogger;
	
	public class HashMap implements IMap
	{
		private var _map:Dictionary;
		
		private var log:ILogger = ClassLogger.getLogger();

		public function HashMap(useWeakReferences:Boolean = true)
		{
			this._map = new Dictionary(useWeakReferences);
		}

		public function put(key:*, value:*):void
        {
            _map[key] = value;
        }
		
		public function get keys():Array {
			return getKeys();	
		}
		
		public function getKeyAt(i:int):* {
			return getKeys()[i];
		}
		
		public function get values():Array {
			return getValues();
		}
		public function getKeys():Array
        {
            var keys:Array = new Array();

            for (var key:* in _map)
            {
                keys.push( key );
            }
            return keys;
        }

		public function getValues():Array
        {
            var values:Array = new Array();

            for (var key:* in _map)
            {
                values.push( _map[key] );
            }
            return values;
        }
	
		public function getValue(key:*):*
        {
            return _map[key];
        }
	
		public function keyExists( key:* ):Boolean{
			return _map.hasOwnProperty(key);
		}
	
		public function valueExists( val:* ):Boolean{
			var result:Boolean = false;

            for ( var key:* in _map )
            {
                if ( _map[key] == val )
                {
                    result = true;
                    break;
                }
            }
            return result;
		}
	
		public function remove(key:*):void
        {
            delete _map[key];
        }
        
        public function size():int
        {
            var length:int = 0;
            for (var key:* in _map)
            {
                length++;
            }
            return length;
        }
        
        public function clear():void
        {
            for ( var key:* in _map )
            {
                remove( key );
            }
        }

		
	}
}