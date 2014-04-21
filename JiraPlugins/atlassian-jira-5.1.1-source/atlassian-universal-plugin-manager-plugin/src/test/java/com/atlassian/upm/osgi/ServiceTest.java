package com.atlassian.upm.osgi;

import com.atlassian.upm.test.osgi.BundleMatchers;
import com.atlassian.upm.test.osgi.OsgiTestBase;

import org.junit.Test;

import static com.atlassian.upm.test.osgi.PropertyMatchers.matches;
import static com.atlassian.upm.test.osgi.ServiceMatchers.bundle;
import static com.atlassian.upm.test.osgi.ServiceMatchers.description;
import static com.atlassian.upm.test.osgi.ServiceMatchers.objectClasses;
import static com.atlassian.upm.test.osgi.ServiceMatchers.pid;
import static com.atlassian.upm.test.osgi.ServiceMatchers.ranking;
import static com.atlassian.upm.test.osgi.ServiceMatchers.usingBundles;
import static com.atlassian.upm.test.osgi.ServiceMatchers.vendor;
import static org.hamcrest.MatcherAssert.assertThat;

public class ServiceTest extends OsgiTestBase
{
    @Test
    public void assertThatUpmServicesWrapOsgiServices()
    {
        for (int i = 0; i < 9; ++i)
        {
            Service expected = getExpectedService(i);
            Service actual = getServiceAccessor().getService(i);

            assertThat(actual, matches("service", expected).by(
                bundle.by(BundleMatchers.id),
                usingBundles.by(BundleMatchers.id),
                objectClasses,
                description,
                pid,
                ranking,
                vendor));
        }
    }
}
