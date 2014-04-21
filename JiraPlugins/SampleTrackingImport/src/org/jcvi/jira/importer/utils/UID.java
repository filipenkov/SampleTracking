package org.jcvi.jira.importer.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Just keeps track of some counters
 */
public class UID {
    //start with each class getting its own counter

    private static final Map<Class,Integer> counters
            = new HashMap<Class, Integer>();

    public static int getUID(Class requestingClass) {
        Integer currentValue = counters.get(requestingClass);
        if (currentValue == null) {
            currentValue = 1; //we have pre-incremented to make the code simpler
        }
        //save the new value
        counters.put(requestingClass,(currentValue+1));
        return currentValue;
    }
}
