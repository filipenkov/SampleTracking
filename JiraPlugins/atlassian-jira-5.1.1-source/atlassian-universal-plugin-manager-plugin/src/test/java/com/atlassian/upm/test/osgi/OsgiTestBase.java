package com.atlassian.upm.test.osgi;

import java.net.URI;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Bundle.HeaderClause;
import com.atlassian.upm.osgi.Bundle.State;
import com.atlassian.upm.osgi.BundleAccessor;
import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.ServiceAccessor;
import com.atlassian.upm.osgi.impl.BundleAccessorImpl;
import com.atlassian.upm.osgi.impl.PackageAccessorImpl;
import com.atlassian.upm.osgi.impl.ServiceAccessorImpl;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import static com.atlassian.upm.osgi.impl.Versions.fromString;
import static com.atlassian.upm.test.osgi.Mockery.arrayOf;
import static com.atlassian.upm.test.osgi.Mockery.listOf;
import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Constants.BUNDLE_NAME;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.osgi.framework.Constants.IMPORT_PACKAGE;
import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_ID;
import static org.osgi.framework.Constants.SERVICE_PID;
import static org.osgi.framework.Constants.SERVICE_RANKING;
import static org.osgi.framework.Constants.SERVICE_VENDOR;
import static org.osgi.framework.Version.emptyVersion;

@RunWith(MockitoJUnitRunner.class)
public class OsgiTestBase
{
    @Mock BundleContext bundleContext;
    @Mock ServiceReference packageAdminServiceReference;
    @Mock PackageAdmin packageAdmin;

    // bundles
    private BundleAccessorImpl bundleAccessor;
    final org.osgi.framework.Bundle[] OSGI_BUNDLES = arrayOf(org.osgi.framework.Bundle.class, 3);
    final List<Bundle> UPM_BUNDLES = listOf(Bundle.class, 3);

    // services
    private ServiceAccessorImpl serviceAccessor;
    final ServiceReference[] OSGI_SERVICES = arrayOf(ServiceReference.class, 9);
    final List<Service> UPM_SERVICES = listOf(Service.class, 9);

    // packages
    private PackageAccessorImpl packageAccessor;
    final ExportedPackage[] OSGI_PACKAGES = arrayOf(ExportedPackage.class, 9);
    final List<Package> UPM_PACKAGES = listOf(Package.class, 9);

    private void populateBundles()
    {
        for (int id = 0; id < OSGI_BUNDLES.length; ++id)
        {
            org.osgi.framework.Bundle osgiBundle = OSGI_BUNDLES[id];
            Bundle upmBundle = UPM_BUNDLES.get(id);
            String name = format("test.bundle%d", id);
            ServiceReference[] osgiRegisteredServices = { OSGI_SERVICES[id], OSGI_SERVICES[id + 3], OSGI_SERVICES[id + 6] };
            Iterable<Service> upmRegisteredServices = asList(UPM_SERVICES.get(id), UPM_SERVICES.get(id + 3), UPM_SERVICES.get(id + 6));

            when(osgiBundle.getState()).thenReturn(org.osgi.framework.Bundle.ACTIVE);
            when(upmBundle.getState()).thenReturn(State.ACTIVE);

            when(osgiBundle.getHeaders()).thenReturn(new Hashtable<String, String>(of(
                BUNDLE_NAME, format("Test Bundle %d", id),
                BUNDLE_SYMBOLICNAME, name,
                BUNDLE_VERSION, "1.0",
                EXPORT_PACKAGE, buildOsgiExportPackage(id),
                IMPORT_PACKAGE, buildOsgiImportPackage())));
            when(upmBundle.getUnparsedHeaders()).thenReturn(of(
                BUNDLE_NAME, format("Test Bundle %d", id),
                BUNDLE_SYMBOLICNAME, name,
                BUNDLE_VERSION, "1.0"));
            Map<String, Iterable<HeaderClause>> parsedHeaders = of(
                EXPORT_PACKAGE, buildUpmExportPackage(id),
                IMPORT_PACKAGE, buildUpmImportPackage());
            when(upmBundle.getParsedHeaders()).thenReturn(parsedHeaders);

            when(osgiBundle.getBundleId()).thenReturn((long) id);
            when(upmBundle.getId()).thenReturn((long) id);

            when(osgiBundle.getLocation()).thenReturn(format("file:///bundle/%d", id));
            when(upmBundle.getLocation()).thenReturn(URI.create(format("file:///bundle/%d", id)));

            when(osgiBundle.getRegisteredServices()).thenReturn(osgiRegisteredServices);
            when(upmBundle.getRegisteredServices()).thenReturn(upmRegisteredServices);

            when(osgiBundle.getServicesInUse()).thenReturn(OSGI_SERVICES);
            when(upmBundle.getServicesInUse()).thenReturn(UPM_SERVICES);

            when(upmBundle.getName()).thenReturn(format("Test Bundle %d", id));

            when(osgiBundle.getSymbolicName()).thenReturn(name);
            when(upmBundle.getSymbolicName()).thenReturn(name);

            when(osgiBundle.getVersion()).thenReturn(emptyVersion);
            when(upmBundle.getVersion()).thenReturn(fromString("0"));

            when(bundleContext.getBundle((long) id)).thenReturn(osgiBundle);
        }
    }

    private String buildOsgiExportPackage(int bundleId)
    {
        return format("test.package%d;test.package%d;test.package%d;version=\"0\"", bundleId, bundleId + 3, bundleId + 6);
    }

    private Iterable<HeaderClause> buildUpmExportPackage(int bundleId)
    {
        List<HeaderClause> clauses = listOf(HeaderClause.class, 3);
        for (int i = 0; i < 3; ++i)
        {
            HeaderClause clause = clauses.get(i);
            int packageId = bundleId + i * 3;

            when(clause.getPath()).thenReturn(format("test.package%d", packageId));
            when(clause.getParameters()).thenReturn(of("version", "0"));
            when(clause.getReferencedPackage()).thenReturn(UPM_PACKAGES.get(packageId));
        }
        return clauses;
    }

    private static String buildOsgiImportPackage()
    {
        StringBuilder header = new StringBuilder();
        for (int packageId = 0; packageId < 9; ++packageId)
        {
            if (packageId != 0)
            {
                header.append(',');
            }
            header.append(format("test.package%d", packageId));
        }
        return header.toString();
    }

    private Iterable<HeaderClause> buildUpmImportPackage()
    {
        List<HeaderClause> clauses = listOf(HeaderClause.class, 9);
        for (int packageId = 0; packageId < 9; ++packageId)
        {
            HeaderClause clause = clauses.get(packageId);
            when(clause.getPath()).thenReturn(format("test.package%d", packageId));
            when(clause.getParameters()).thenReturn(Collections.<String, String> emptyMap());
            when(clause.getReferencedPackage()).thenReturn(UPM_PACKAGES.get(packageId));
        }
        return clauses;
    }

    private void populateServices() throws Exception
    {
        for (int serviceId = 0; serviceId < 9; ++serviceId)
        {
            ServiceReference osgiService = OSGI_SERVICES[serviceId];
            Service upmService = UPM_SERVICES.get(serviceId);
            String name = format("test.service%d", serviceId);
            int bundleId = serviceId % 3;

            when(osgiService.getProperty(OBJECTCLASS)).thenReturn(new String[] { name + ".class0", name + "class1" });
            when(upmService.getObjectClasses()).thenReturn(asList(name + ".class0", name + "class1"));

            when(osgiService.getProperty(SERVICE_DESCRIPTION)).thenReturn(name);
            when(upmService.getDescription()).thenReturn(name);

            when(osgiService.getProperty(SERVICE_ID)).thenReturn((long) serviceId);
            when(upmService.getId()).thenReturn((long) serviceId);

            when(osgiService.getProperty(SERVICE_PID)).thenReturn(new String[] { name + ".pid0", name + ".pid1" });
            when(upmService.getPid()).thenReturn(asList(name + ".pid0", name + ".pid1"));

            when(osgiService.getProperty(SERVICE_RANKING)).thenReturn(0);
            when(upmService.getRanking()).thenReturn(0);

            when(osgiService.getProperty(SERVICE_VENDOR)).thenReturn("vendor." + name);
            when(upmService.getVendor()).thenReturn("vendor." + name);

            when(osgiService.getBundle()).thenReturn(OSGI_BUNDLES[bundleId]);
            when(upmService.getBundle()).thenReturn(UPM_BUNDLES.get(bundleId));

            when(osgiService.getUsingBundles()).thenReturn(OSGI_BUNDLES);
            when(upmService.getUsingBundles()).thenReturn(UPM_BUNDLES);

            when(bundleContext.getAllServiceReferences(null, format("(%s=%d)", SERVICE_ID, serviceId)))
                .thenReturn(new ServiceReference[] {osgiService});
        }
    }

    private void populatePackages()
    {
        for (int packageId = 0; packageId < 9; ++packageId)
        {
            ExportedPackage osgiPackage = OSGI_PACKAGES[packageId];
            Package upmPackage = UPM_PACKAGES.get(packageId);

            String name = format("test.package%d", packageId);
            int exportingBundleId = packageId % 3;

            when(osgiPackage.getName()).thenReturn(name);
            when(upmPackage.getName()).thenReturn(name);

            when(osgiPackage.getExportingBundle()).thenReturn(OSGI_BUNDLES[exportingBundleId]);
            when(upmPackage.getExportingBundle()).thenReturn(UPM_BUNDLES.get(exportingBundleId));

            when(osgiPackage.getImportingBundles()).thenReturn(buildOsgiImportingBundles(exportingBundleId));
            when(upmPackage.getImportingBundles()).thenReturn(UPM_BUNDLES);

            when(osgiPackage.getVersion()).thenReturn(emptyVersion);
            when(upmPackage.getVersion()).thenReturn(fromString("0"));

            when(packageAdmin.getExportedPackages(name)).thenReturn(new ExportedPackage[] { osgiPackage });
        }
    }

    private org.osgi.framework.Bundle[] buildOsgiImportingBundles(int bundleId)
    {
        switch (bundleId)
        {
            case 0: return new org.osgi.framework.Bundle[] { OSGI_BUNDLES[1], OSGI_BUNDLES[2] };
            case 1: return new org.osgi.framework.Bundle[] { OSGI_BUNDLES[0], OSGI_BUNDLES[2] };
            case 2: return new org.osgi.framework.Bundle[] { OSGI_BUNDLES[0], OSGI_BUNDLES[1] };
        }
        return null;
    }

    @Before
    public void populateAccessors() throws Exception
    {
        when(bundleContext.getBundles()).thenReturn(OSGI_BUNDLES);
        when(bundleContext.getAllServiceReferences(null, null)).thenReturn(OSGI_SERVICES);
        when(bundleContext.getServiceReference(PackageAdmin.class.getName())).thenReturn(packageAdminServiceReference);
        when(bundleContext.getService(packageAdminServiceReference)).thenReturn(packageAdmin);
        when(packageAdmin.getExportedPackages((org.osgi.framework.Bundle) null)).thenReturn(OSGI_PACKAGES);

        packageAccessor = new PackageAccessorImpl();
        packageAccessor.setBundleContext(bundleContext);
        populatePackages();

        bundleAccessor = new BundleAccessorImpl(packageAccessor);
        bundleAccessor.setBundleContext(bundleContext);
        populateBundles();

        serviceAccessor = new ServiceAccessorImpl(packageAccessor);
        serviceAccessor.setBundleContext(bundleContext);
        populateServices();
    }

    protected BundleAccessor getBundleAccessor()
    {
        return bundleAccessor;
    }

    protected Bundle getExpectedBundle(int id)
    {
        return UPM_BUNDLES.get(id);
    }

    protected ServiceAccessor getServiceAccessor()
    {
        return serviceAccessor;
    }

    protected Service getExpectedService(int id)
    {
        return UPM_SERVICES.get(id);
    }

    protected PackageAccessor getPackageAccessor()
    {
        return packageAccessor;
    }

    protected Package getExpectedPackage(int id)
    {
        return UPM_PACKAGES.get(id);
    }
}
