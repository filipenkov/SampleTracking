package com.atlassian.upm.osgi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Accesses the packages installed in an OSGi container
 */
public interface PackageAccessor
{
    /**
     * Fetch all of the packages
     *
     * @return the packages
     */
    Iterable<Package> getPackages();

    /**
     * Fetch a package exported by a specific bundle, with a given name and version
     *
     * @param bundleId the id of the bundle exporting the package
     * @param name the name of the package to fetch
     * @param version the version of the named exported package
     * @return the exported package
     */
    @Nullable
    Package getExportedPackage(long bundleId, String name, Version version);

    /**
     * Fetch all packages exported by a specific bundle with a given name.
     * Will return multiple packages of the same name with different versions, sorted by version.
     *
     * @param bundleId the id of the bundle exporting the packages
     * @param name the name of the packages to fetch
     * @return the exported packages
     */
    @Nonnull
    Iterable<Package> getExportedPackages(final long bundleId, final String name);

    /**
     * Fetch a package imported by a specific bundle, with a given name and satisfying
     * a given version range.  If more than one package version satisfies the range, the
     * package with the highest version will be returned.
     *
     * @param bundleId the id of the bundle importing the package
     * @param name the name of the package to fetch
     * @param versionRange the version range the imported package must satisfy
     * @return the imported package
     */
    @Nullable
    Package getImportedPackage(long bundleId, String name, VersionRange versionRange);
}
