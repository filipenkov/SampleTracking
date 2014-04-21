package com.sysbliss.jira.plugins.workflow.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple map with a timeout on the get/put methods.
 */
public class TokenMap extends HashMap {
    private long tokenTimeout;
    Map tokenTimeouts;

    public TokenMap(long tokenTimeout) {
        this.tokenTimeout = tokenTimeout;
        this.tokenTimeouts = new HashMap();
    }

    public Object put(Object key, Object value) {
        tokenTimeouts.put(key, nextExpiryTime());
        return super.put(key, value);
    }

    public Object get(Object key) {
        if (!super.containsKey(key)) {
            return null;
        }

        Long expiryTime = (Long) tokenTimeouts.get(key);
        if (expiryTime == null) {
            tokenTimeouts.remove(key);
            super.remove(key);
            return null;
        } else if (expiryTime.longValue() < System.currentTimeMillis()) // expired!
        {
            tokenTimeouts.remove(key);
            super.remove(key);
            return null;
        } else // we're still timed in, extend another timeout
        {
            tokenTimeouts.put(key, nextExpiryTime());
        }

        return super.get(key);
    }

    private Long nextExpiryTime() {
        return new Long(System.currentTimeMillis() + tokenTimeout);
    }

    public Object remove(Object key) {
        tokenTimeouts.remove(key);
        return super.remove(key);
    }

    public void clear() {
        tokenTimeouts.clear();
        super.clear();
    }

    public Object clone() {
        throw new UnsupportedOperationException("Not written yet.");
    }
}
