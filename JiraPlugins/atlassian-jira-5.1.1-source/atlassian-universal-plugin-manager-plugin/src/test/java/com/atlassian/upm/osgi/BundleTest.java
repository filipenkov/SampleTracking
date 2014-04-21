package com.atlassian.upm.osgi;

import com.atlassian.upm.test.osgi.OsgiTestBase;
import com.atlassian.upm.test.osgi.PackageMatchers;
import com.atlassian.upm.test.osgi.ServiceMatchers;

import org.junit.Test;

import static com.atlassian.upm.test.osgi.BundleMatchers.headerClauses;
import static com.atlassian.upm.test.osgi.BundleMatchers.headerName;
import static com.atlassian.upm.test.osgi.BundleMatchers.id;
import static com.atlassian.upm.test.osgi.BundleMatchers.location;
import static com.atlassian.upm.test.osgi.BundleMatchers.name;
import static com.atlassian.upm.test.osgi.BundleMatchers.parameters;
import static com.atlassian.upm.test.osgi.BundleMatchers.parsedHeaders;
import static com.atlassian.upm.test.osgi.BundleMatchers.path;
import static com.atlassian.upm.test.osgi.BundleMatchers.referencedPackage;
import static com.atlassian.upm.test.osgi.BundleMatchers.registeredServices;
import static com.atlassian.upm.test.osgi.BundleMatchers.servicesInUse;
import static com.atlassian.upm.test.osgi.BundleMatchers.state;
import static com.atlassian.upm.test.osgi.BundleMatchers.symbolicName;
import static com.atlassian.upm.test.osgi.BundleMatchers.unparsedHeaders;
import static com.atlassian.upm.test.osgi.BundleMatchers.version;
import static com.atlassian.upm.test.osgi.PropertyMatchers.matches;
import static org.hamcrest.MatcherAssert.assertThat;

public class BundleTest extends OsgiTestBase
{
    @Test
    public void assertThatUpmBundlesWrapOsgiBundles()
    {
        for (int i = 0; i < 3; ++i)
        {
            Bundle expected = getExpectedBundle(i);
            Bundle actual = getBundleAccessor().getBundle(i);

            assertThat(actual, matches("bundle", expected).by(
                state,
                unparsedHeaders,
                parsedHeaders.by(
                    headerName,
                    headerClauses.by(
                        path,
                        parameters,
                        referencedPackage.by(PackageMatchers.name))),
                id,
                location,
                registeredServices.by(ServiceMatchers.id),
                servicesInUse.by(ServiceMatchers.id),
                symbolicName,
                name,
                version));
        }
    }
}
