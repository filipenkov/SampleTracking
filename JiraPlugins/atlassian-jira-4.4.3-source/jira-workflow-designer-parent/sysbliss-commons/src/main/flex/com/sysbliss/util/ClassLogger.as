package com.sysbliss.util
{
	import mx.logging.ILogger;
	import mx.logging.Log;
	
	public class ClassLogger
	{
		public static const DEFAULT_CATEGORY:String = "logger";
		
		public function ClassLogger()
		{
		}
		
		public static function getLogger() : ILogger
        {
            var category:String = category = getCallerFromStackTrace();
            
            if(category == null || category == ""){
            	category = DEFAULT_CATEGORY;
            }
            
            return Log.getLogger(category);
        }
        
		private static function getCallerFromStackTrace() : String
		{
			var callerMethod:String = "";
			var nullArray:Array;
			var stackTrace:String;
			var retCaller:String = "";
			// Pop a StackTrace by creating a RunTime error.
			try
			{
				nullArray.push("whoops");
			}
			catch (e:Error)
			{
				stackTrace = e.getStackTrace();
			}
			   
			// Extract the ClassName from the StackTrack
			if (stackTrace != null)
			{
				var parts : Array = new Array();
				stackTrace.split("\n").join("");
				parts = stackTrace.split("\tat ");
				if (parts[3] != null)
				{
					callerMethod = parts[3].split("(")[0];
				}
				retCaller = getCategoryNameFor(callerMethod)
			}

			return retCaller;
		}
		
		private static function getCategoryNameFor(qualifiedClassName : String) : String
        {
			return qualifiedClassName.indexOf("::") > -1 ? qualifiedClassName.split("::").join(".") : qualifiedClassName;
        }

	}
}