package com.atlassian.upm.test.osgi;

import com.atlassian.upm.Functions;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.test.osgi.PropertyMatchers.Property;

import static com.atlassian.upm.test.osgi.PropertyMatchers.property;

public final class VersionMatchers
{
    public static final Property<Version, Integer, Integer> majorComponent =
        property("major component", Functions.<Version, Integer> getter("major"));
    public static final Property<Version, Integer, Integer> minorComponent =
        property("minor component", Functions.<Version, Integer> getter("minor"));
    public static final Property<Version, Integer, Integer> microComponent =
        property("micro component", Functions.<Version, Integer> getter("micro"));
    public static final Property<Version, String, String> qualifier =
        property("qualifier");
}
