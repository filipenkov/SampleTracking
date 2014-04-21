package com.atlassian.gadgets.opensocial.spi;

import java.net.URI;

/**
 * A whitelist to determine what remote systems an administrator has deemed appropriate to make requests to.
 * 
 * @since 2.2.0
 */
public interface Whitelist
{
    /**
     * Returns {@code true} if the {@code URI} is in the whitelist of remote systems that are appropriate to make 
     * requests to, {@code false} otherwise.
     * 
     * @param uri {@code} URI of the remote system to check for whitelisting
     * @return {@code true} if the {@code URI} is in the whitelist, {@code false} otherwise
     */
    boolean allows(URI uri);
}
