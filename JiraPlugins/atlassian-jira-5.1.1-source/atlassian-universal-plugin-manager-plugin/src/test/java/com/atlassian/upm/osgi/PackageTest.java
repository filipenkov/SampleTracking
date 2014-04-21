package com.atlassian.upm.osgi;

import com.atlassian.upm.test.osgi.OsgiTestBase;

import org.junit.Test;

import static com.atlassian.upm.test.osgi.BundleMatchers.id;
import static com.atlassian.upm.test.osgi.PackageMatchers.exportingBundle;
import static com.atlassian.upm.test.osgi.PackageMatchers.importingBundles;
import static com.atlassian.upm.test.osgi.PackageMatchers.name;
import static com.atlassian.upm.test.osgi.PackageMatchers.version;
import static com.atlassian.upm.test.osgi.PropertyMatchers.matches;
import static org.hamcrest.MatcherAssert.assertThat;

public class PackageTest extends OsgiTestBase
{
    @Test
    public void assertThatUpmServicesWrapOsgiServices()
    {
        for (int i = 0; i < 9; ++i)
        {
            Package expected = getExpectedPackage(i);
            Package actual = getPackageAccessor().getExportedPackage(
                expected.getExportingBundle().getId(),
                expected.getName(),
                expected.getVersion());

            assertThat(actual, matches("package", expected).by(
                name,
                exportingBundle.by(id),
                importingBundles.by(id),
                version));
        }
    }
}
