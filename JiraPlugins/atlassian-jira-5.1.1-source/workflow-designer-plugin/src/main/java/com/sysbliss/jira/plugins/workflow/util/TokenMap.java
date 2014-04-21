package com.sysbliss.jira.plugins.workflow.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple map with a timeout on the get/put methods.
 */
public class TokenMap<K,V> extends HashMap<K,V> {
    private long tokenTimeout;
    private final Map<K, Long> tokenTimeouts;

    public TokenMap(long tokenTimeout) {
        this.tokenTimeout = tokenTimeout;
        this.tokenTimeouts = new HashMap<K, Long>();
    }

    @Override
    public V put(K key, V value) {
        tokenTimeouts.put(key, nextExpiryTime());
        return super.put(key, value);
    }

    @Override
    public V get(Object key) {
        if (!super.containsKey(key)) {
            return null;
        }

        Long expiryTime = tokenTimeouts.get(key);
        if (expiryTime == null) {
            tokenTimeouts.remove(key);
            super.remove(key);
            return null;
        } else if (expiryTime < System.currentTimeMillis()) // expired!
        {
            tokenTimeouts.remove(key);
            super.remove(key);
            return null;
        } else // we're still timed in, extend another timeout
        {
            tokenTimeouts.put((K) key, nextExpiryTime());
        }

        return super.get(key);
    }

    private Long nextExpiryTime() {
        return System.currentTimeMillis() + tokenTimeout;
    }

    @Override
    public V remove(Object key) {
        tokenTimeouts.remove(key);
        return super.remove(key);
    }

    @Override
    public void clear() {
        tokenTimeouts.clear();
        super.clear();
    }
}
