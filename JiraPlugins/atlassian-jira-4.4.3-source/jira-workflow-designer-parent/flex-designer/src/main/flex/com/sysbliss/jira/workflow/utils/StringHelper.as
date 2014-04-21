package com.sysbliss.jira.workflow.utils {
import mx.utils.StringUtil;

public class StringHelper {
    public static function beginsWith(input:String, prefix:String):Boolean {
        return (prefix == input.substring(0, prefix.length));
    }

    public static function isBlank(str:String):Boolean {
        var strLen:int;
        if (str == null || (strLen = str.length) == 0) {
            return true;
        }
        for (var i:int = 0; i < strLen; i++) {
            if ((StringUtil.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }


    public static function isNotBlank(inputString:String):Boolean {
        return !isBlank(inputString);
    }
}
}