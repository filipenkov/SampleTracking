package com.sysbliss.util
{
	import flash.utils.*;
	import flash.errors.IllegalOperationError;
	public class AbstractClassEnforcer
	{
		public static function enforceConstructor(instance:Object, abstractClass:Class):void {
            var instanceClassName:String = getQualifiedClassName(instance);
            if (instance.constructor === abstractClass) {
                throw new IllegalOperationError("The class '" + instanceClassName + "' is abstract and cannot be instantiated.");
            }
        }
        
        public static function enforceMethod(name:String):void {
        	//just throw an error. If it's overridden this error won't be thrown
			throw new IllegalOperationError("The method '" + name + "' is abstract and must be overridden.");
        }
	}
}