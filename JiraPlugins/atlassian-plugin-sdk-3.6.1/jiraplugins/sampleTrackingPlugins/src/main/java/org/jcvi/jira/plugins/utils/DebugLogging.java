package org.jcvi.jira.plugins.utils;

import com.opensymphony.module.propertyset.PropertySet;

import java.util.Map;

/**
 * Contains several methods for logging specific objects
 */
public class DebugLogging {
    public static String getLogMessageFor(String name, PropertySet map) {
        String message = "";
        for (Object key : map.getKeys()) {
            //OSWorkflow only uses Strings as keys
            if (key instanceof String) {
                message += wrapLine(name,key + "=<String>"+
                                         map.getString((String)key));
            } else {
                message += wrapLine(name,"Invalid key in PropertySet "+name+". Key="+key);
            }
        }
        return message;
    }

    public static String getLogMessageFor(String name, Map map) {
        String message = "";
        for (Object key : map.keySet()) {
            Object value = map.get(key);
            message += wrapLine(name, nullToString(key) +
                    "=<" + safeGetClass(value) + ">"
                    + nullToString(value));
        }
        return message;
    }

    private static String nullToString(Object object) {
        if (object != null) {
            return object.toString();
        }
        return "NULL";
    }

    public static String safeGetClass(Object object) {
        if (object != null) {
            return object.getClass().toString();
        }
        return "?";
    }

    public static String wrapLine(String name, String content) {
        return name+": "+content+"\n";
    }

}
