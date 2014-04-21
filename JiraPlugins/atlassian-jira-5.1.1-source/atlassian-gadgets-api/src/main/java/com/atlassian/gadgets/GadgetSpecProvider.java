package com.atlassian.gadgets;

import java.net.URI;

/**
 * <p>A simple representation of a container of gadget spec URIs.</p>
 *
 * <p>External gadgets are those with specs hosted on a web server, whether provided by a plugin or added
 * by an administrator after startup; gadgets with spec files stored inside the plugin bundle are called
 * internal.</p>
 *
 * <p>Host applications can implement this interface in order to provide gadget specs that are specific to the host
 * application; for example, the JIRA legacy portlet bridge gadget.</p>
 *
 * <p>Host applications that implement this interface should expose it as a
 * public component in atlassian-plugins.xml:</p>
 *
 * {@code
 *     <component key="myApplicationGadgetSpecProvider" public="true"
 *                class="com.atlassian.app.gadgets.ApplicationGadgetSpecProviderImpl">
 *         <interface>com.atlassian.gadgets.store.GadgetSpecProvider</interface>
 *     </component>
 * }
 */
public interface GadgetSpecProvider
{
    /**
     * Returns all the gadget URIs contained by this store.
     *
     * @return all the gadget URIs contained by this store.
     */
    Iterable<URI> entries();

    /**
     * Returns {@code true} if the URI is in the store, {@code false} otherwise.
     *
     * @param gadgetSpecUri URI to check is in the store
     * @return {@code true} if the URI is in the store, {@code false} otherwise
     */
    boolean contains(URI gadgetSpecUri);
}
