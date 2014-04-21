package com.atlassian.security.auth.trustedapps;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Takes a set of patterns and assumes a URL matches if it starts with one of the given patterns.
 */
public class DefaultURLMatcher implements URLMatcher
{
    private final Set<String> patterns;

    public DefaultURLMatcher(final Set<String> patterns)
    {
        this.patterns = Collections.unmodifiableSet(new LinkedHashSet<String>(patterns));
    }

    /**
     * returns true if the given URL starts with one of the given patterns and false otherwise
     */
    public boolean match(final String urlPath)
    {
        if (patterns.isEmpty())
        {
            return true;
        }
        for (final String pattern : patterns)
        {
            if (urlPath.startsWith(pattern))
            {
                return true;
            }
        }
        return false;
    }
}
