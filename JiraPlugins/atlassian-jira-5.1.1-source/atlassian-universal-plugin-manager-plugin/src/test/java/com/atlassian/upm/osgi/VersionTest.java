package com.atlassian.upm.osgi;

import java.util.List;

import com.atlassian.upm.osgi.impl.Versions;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.upm.test.osgi.Mockery.listOf;
import static com.atlassian.upm.test.osgi.PropertyMatchers.matches;
import static com.atlassian.upm.test.osgi.VersionMatchers.majorComponent;
import static com.atlassian.upm.test.osgi.VersionMatchers.microComponent;
import static com.atlassian.upm.test.osgi.VersionMatchers.minorComponent;
import static com.atlassian.upm.test.osgi.VersionMatchers.qualifier;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VersionTest
{
    private static final List<String> VERSIONS =
        ImmutableList.of("", "1", "1.2", "1.2.3", "1.2.3.Test", "1.2.3-SNAPSHOT", "1.2.3-m1");

    private static final List<Version> EXPECTED = listOf(Version.class, VERSIONS.size());
    private static final List<Version> ACTUAL =
        copyOf(transform(VERSIONS, new Function<String, Version>()
        {
            public Version apply(String version)
            {
                return Versions.fromString(version);
            }
        }));

    private void populateVersion(int id, int major, int minor, int micro, String qualifier)
    {
        Version version = EXPECTED.get(id);
        when(version.getMajor()).thenReturn(major);
        when(version.getMinor()).thenReturn(minor);
        when(version.getMicro()).thenReturn(micro);
        when(version.getQualifier()).thenReturn(qualifier);
    }

    @Before
    public void populateVersions()
    {
        populateVersion(0, 0, 0, 0, "");
        populateVersion(1, 1, 0, 0, "");
        populateVersion(2, 1, 2, 0, "");
        populateVersion(3, 1, 2, 3, "");
        populateVersion(4, 1, 2, 3, "Test");
        populateVersion(5, 1, 2, 3, "SNAPSHOT");
        populateVersion(6, 1, 2, 3, "m1");
    }

    @Test
    public void assertThatFromStringWorks()
    {
        for (int i = 0; i < VERSIONS.size(); ++i)
        {
            Version expected = EXPECTED.get(i);
            Version actual = ACTUAL.get(i);
            assertThat(actual, matches("version", expected)
                .by(majorComponent, minorComponent, microComponent, qualifier));
        }
    }

    @Test
    public void assertThatVersionsAreOrderedCorrectly()
    {
        Version
            V0 = ACTUAL.get(0),
            V1 = ACTUAL.get(1),
            V2 = ACTUAL.get(2);
        assertThat(V0, is(lessThan(V1)));
        assertThat(V1, is(equalTo(V1)));
        assertThat(V2, is(greaterThan(V1)));
    }
}
