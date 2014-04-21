package com.atlassian.upm.osgi;

public interface Package
{
    /**
     * Fetch the name of the package
     *
     * @return the package's name
     */
    String getName();

    /**
     * Fetch the bundle exporting this package
     *
     * @return the package's exporting bundle
     */
    Bundle getExportingBundle();

    /**
     * Fetch the bundles importing this package
     *
     * @return the package's importing bundles
     */
    Iterable<Bundle> getImportingBundles();

    /**
     * Fetch the version of this package
     *
     * @return the package's version
     */
    Version getVersion();
}
