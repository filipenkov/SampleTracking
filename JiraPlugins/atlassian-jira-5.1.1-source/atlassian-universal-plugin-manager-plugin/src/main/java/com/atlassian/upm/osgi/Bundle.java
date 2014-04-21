package com.atlassian.upm.osgi;

import java.net.URI;
import java.util.Map;

import javax.annotation.Nullable;

public interface Bundle
{
    /**
     * Maps integers representing bundle state to an enum representing bundle state.
     */
    public static enum State
    {
        UNINSTALLED, INSTALLED, RESOLVED, STARTING, STOPPING, ACTIVE;
    }

    /**
     * Encapsulates clauses in a parsed header (OSGi R4, 3.2.4).  Note that attributes
     * (e.g. "version=1.0") and directives (e.g. resolution:=optional) of an entry are
     * treated uniformly as "parameters" here, and any clause containing multiple paths
     * is split by the parser into multiple clauses with identical parameters.
     */
    public interface HeaderClause
    {
        /**
         * Fetch the path from the parsed header clause
         *
         * @return the header clause's path
         */
        String getPath();

        /**
         * Fetch the parameters for the parsed header clause
         *
         * @return the header clause's parameters
         */
        Map<String, String> getParameters();

        /**
         * Fetch the package referenced by the header clause,
         * or null if unavailable or inapplicable.
         *
         * @return the header clause's referenced package
         */
        Package getReferencedPackage();
    }

    /**
     * Fetch the state of the bundle
     *
     * @return the bundle state
     */
    State getState();

    /**
     * Fetch the bundle's headers that are not parseable (per OSGi R4, 3.2.4, e.g. Bundle-Description)
     *
     * @return the bundle's unparsed headers
     */
    Map<String, String> getUnparsedHeaders();

    /**
     * Fetch the bundle's headers that are parseable (per OSGi R4, 3.2.4, e.g. Import-Package)
     *
     * @return the bundle's parsed headers
     */
    Map<String, Iterable<HeaderClause>> getParsedHeaders();

    /**
     * Fetch the id of the bundle
     *
     * @return the bundle id
     */
    long getId();

    /**
     * Fetch the location of the bundle
     *
     * @return the bundle location
     */
    @Nullable
    URI getLocation();

    /**
     * Fetch the services registered by the bundle
     *
     * @return the services registered by the bundle
     */
    Iterable<Service> getRegisteredServices();

    /**
     * Fetch the services in use by the bundle
     *
     * @return the services in use by the bundle
     */
    Iterable<Service> getServicesInUse();

    /**
     * Fetch the symbolic name of the bundle
     *
     * @return the symbolic name of the bundle
     */
    String getSymbolicName();

    /**
     * Fetch the display name of the bundle, if any
     *
     * @return the display name of the bundle
     */
    @Nullable
    String getName();

    /**
     * Fetch the version of the bundle
     *
     * @return the version of the bundle
     */
    Version getVersion();
}
