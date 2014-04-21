package com.atlassian.upm.osgi.impl;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.osgi.VersionRange;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSortedSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.springframework.osgi.context.BundleContextAware;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.filter;

public final class PackageAccessorImpl implements PackageAccessor, BundleContextAware
{
    private BundleContext bundleContext;

    public void setBundleContext(BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext, "bundleContext");
    }

    public Iterable<Package> getPackages()
    {
        return getPackages(null, Predicates.<Package> alwaysTrue());
    }

    @Nullable
    public Package getExportedPackage(final long bundleId, final String name, final Version version)
    {
        checkNotNull(name, "name");
        checkNotNull(version, "version");
        Iterable<Package> packages = getPackages(name, new Predicate<Package>()
            {
                public boolean apply(@Nullable Package pkg)
                {
                    return bundleId == pkg.getExportingBundle().getId() &&
                        version.compareTo(pkg.getVersion()) == 0;
                }
            });
        Iterator<Package> it = packages.iterator();
        return (it.hasNext() ? it.next() : null);
    }

    @Nonnull
    public Iterable<Package> getExportedPackages(final long bundleId, final String name)
    {
        checkNotNull(name, "name");
        Iterable<Package> packages = getPackages(name, new Predicate<Package>()
            {
                public boolean apply(@Nullable Package pkg)
                {
                    return bundleId == pkg.getExportingBundle().getId();
                }
            });
        return ImmutableSortedSet.copyOf(versionComparator, packages);
    }

    @Nullable
    public Package getImportedPackage(final long bundleId, final String name, final VersionRange versionRange)
    {
        checkNotNull(name, "name");
        checkNotNull(versionRange, "versionRange");

        // DynamicImport-Package clauses can evidently contain a path of '*'.
        if (name.equals("*"))
        {
            return null;
        }

        Iterable<Package> packages = getPackages(name, new Predicate<Package>()
            {
                public boolean apply(@Nullable Package pkg)
                {
                    return versionRange.contains(pkg.getVersion()) &&
                        any(pkg.getImportingBundles(), new Predicate<Bundle>()
                            {
                                public boolean apply(@Nullable Bundle bundle)
                                {
                                    return bundle.getId() == bundleId;
                                }
                            });
                }
            });
        SortedSet<Package> sortedPackages = ImmutableSortedSet.copyOf(versionComparator, packages);
        return (sortedPackages.size() == 0) ? null : sortedPackages.last();
    }

    private Iterable<Package> getPackages(@Nullable String name, @Nullable Predicate<Package> filterFn)
    {
        ServiceReference adminSvcRef = bundleContext.getServiceReference(PackageAdmin.class.getName());
        PackageAdmin admin = (PackageAdmin) bundleContext.getService(adminSvcRef);
        try
        {
            ExportedPackage[] packages =
                name == null ?
                    admin.getExportedPackages((org.osgi.framework.Bundle) null) :
                    admin.getExportedPackages(name);
            return copyOf(filter(PackageImpl.wrap(this).fromArray(packages), filterFn));
        }
        finally
        {
            bundleContext.ungetService(adminSvcRef);
        }
    }

    private static final Comparator<Package> versionComparator = new Comparator<Package>()
    {
        public int compare(Package lhs, Package rhs)
        {
            return lhs.getVersion().compareTo(rhs.getVersion());
        }
    };
}
