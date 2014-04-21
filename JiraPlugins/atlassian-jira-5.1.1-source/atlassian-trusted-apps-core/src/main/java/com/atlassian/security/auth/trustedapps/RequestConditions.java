package com.atlassian.security.auth.trustedapps;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @since v2.2
 */
public final class RequestConditions
{
    private final long certificateTimeout;
    private final Set<String> urlPatterns;
    private final Set<String> ipPatterns;

    private RequestConditions(final long certificateTimeout, final Set<String> ipPatterns, final Set<String> urlPatterns)
    {
        this.certificateTimeout = certificateTimeout;
        this.ipPatterns = Collections.unmodifiableSet(new HashSet<String>(ipPatterns));
        this.urlPatterns = Collections.unmodifiableSet(new HashSet<String>(urlPatterns));
    }

    public static final class RulesBuilder
    {
        private long certificateTimeout = 0L;
        private Set<String> urlPatterns = new HashSet<String>();
        private Set<String> ipPatterns = new HashSet<String>();

        private RulesBuilder() {}

        public RulesBuilder addURLPattern(final String... pattern)
        {
            for (final String p : pattern)
            {
                urlPatterns.add(p);
            }
            return this;
        }

        public RulesBuilder addIPPattern(final String... pattern) throws IPAddressFormatException
        {
            for (final String p : pattern)
            {
                AtlassianIPMatcher.parsePatternString(p);
                ipPatterns.add(p);
            }
            return this;
        }

        public RulesBuilder setCertificateTimeout(final long timeout)
        {
            if (timeout < 0L)
            {
                throw new IllegalArgumentException("timeout must be >= 0");
            }
            else
            {
                certificateTimeout = timeout;
                return this;
            }
        }

        public RequestConditions build()
        {
            return new RequestConditions(certificateTimeout, ipPatterns, urlPatterns);
        }
    }

    public static RulesBuilder builder()
    {
        return new RulesBuilder();
    }

    public long getCertificateTimeout()
    {
        return certificateTimeout;
    }

    public URLMatcher getURLMatcher()
    {
        return new DefaultURLMatcher(urlPatterns);
    }

    public IPMatcher getIPMatcher()
    {
        return new AtlassianIPMatcher(ipPatterns);
    }

    public Iterable<String> getURLPatterns()
    {
        return urlPatterns;
    }

    public Iterable<String> getIPPatterns()
    {
        return ipPatterns;
    }
}
