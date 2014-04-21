package com.atlassian.gadgets.renderer.internal.http;

import java.net.URI;

import com.atlassian.gadgets.opensocial.spi.Whitelist;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.concat;

/**
 * A whitelist which delegates to 1 or more other whitelists and only allows access if one of the delegates allows
 * access. 
 */
public class DelegatingWhitelist implements Whitelist
{
    private final Whitelist whitelist;
    private final Iterable<Whitelist> optionalWhitelists;

    public DelegatingWhitelist(Whitelist whitelist, Iterable<Whitelist> optionalWhitelists)
    {
        this.whitelist = checkNotNull(whitelist, "whitelist");
        this.optionalWhitelists = checkNotNull(optionalWhitelists, "optionalWhitelists");
    }
    
    public boolean allows(URI uri)
    {
        return any(concat(ImmutableSet.of(whitelist), optionalWhitelists), allowsP(checkNotNull(uri, "uri")));
    }

    private Predicate<Whitelist> allowsP(URI uri)
    {
        return new WhitelistAllows(uri);
    }
    
    private static final class WhitelistAllows implements Predicate<Whitelist>
    {
        private final URI uri;

        public WhitelistAllows(URI uri)
        {
            this.uri = uri;
        }

        public boolean apply(Whitelist whitelist)
        {
            return whitelist.allows(uri);
        }
    }
}
