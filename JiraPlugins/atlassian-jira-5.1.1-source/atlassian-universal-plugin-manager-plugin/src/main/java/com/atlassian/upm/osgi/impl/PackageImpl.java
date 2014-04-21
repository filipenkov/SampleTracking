package com.atlassian.upm.osgi.impl;

import javax.annotation.Nullable;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Bundle.HeaderClause;
import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.osgi.VersionRange;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.osgi.service.packageadmin.ExportedPackage;

import static com.google.common.collect.Iterables.any;
import static org.osgi.framework.Constants.IMPORT_PACKAGE;
import static org.osgi.framework.Constants.VERSION_ATTRIBUTE;

/**
 * A wrapper class around OSGi packages
 */
public final class PackageImpl implements com.atlassian.upm.osgi.Package
{
    private final ExportedPackage pkg;
    private final PackageAccessor packageAccessor;

    PackageImpl(ExportedPackage pkg, PackageAccessor packageAccessor)
    {
        this.pkg = pkg;
        this.packageAccessor = packageAccessor;
    }

    public String getName()
    {
        return pkg.getName();
    }

    public Bundle getExportingBundle()
    {
        return BundleImpl.wrap(packageAccessor).fromSingleton(pkg.getExportingBundle());
    }

    public Iterable<Bundle> getImportingBundles()
    {
        Bundle exportingBundle = getExportingBundle();
        Iterable<Bundle> importingBundles = BundleImpl.wrap(packageAccessor).fromArray(pkg.getImportingBundles());
        // ExportedPackage.getImportingBundles() will never include the exporting bundle, which is dumb.
        // This works around that by adding the exporting bundle if it has an Import-Package clause matching this package.
        Iterable<HeaderClause> importClauses = exportingBundle.getParsedHeaders().get(IMPORT_PACKAGE);
        return importClauses == null ?
            importingBundles :
            any(importClauses, matchesThis) ?
                ImmutableList.<Bundle> builder().addAll(importingBundles).add(exportingBundle).build() :
                importingBundles;
    }

    public Version getVersion()
    {
        return Versions.wrap.fromSingleton(pkg.getVersion());
    }

    protected static Wrapper<ExportedPackage, Package> wrap(final PackageAccessor packageAccessor)
    {
        return new Wrapper<ExportedPackage, Package>("package")
        {
            protected Package wrap(ExportedPackage exportedPackage)
            {
                return new PackageImpl(exportedPackage, packageAccessor);
            }
        };
    }

    private final Predicate<HeaderClause> matchesThis = new Predicate<HeaderClause>()
        {
            public boolean apply(@Nullable HeaderClause headerClause)
            {
                String versionRange = headerClause.getParameters().get(VERSION_ATTRIBUTE);
                return headerClause.getPath().equals(getName()) &&
                    VersionRange.fromString(versionRange == null ? "0" : versionRange).contains(getVersion());
            }
        };
}
