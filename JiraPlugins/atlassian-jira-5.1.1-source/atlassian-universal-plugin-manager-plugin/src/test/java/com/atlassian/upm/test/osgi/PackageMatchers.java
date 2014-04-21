package com.atlassian.upm.test.osgi;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.test.osgi.PropertyMatchers.Property;

import static com.atlassian.upm.test.osgi.PropertyMatchers.iterableProperty;
import static com.atlassian.upm.test.osgi.PropertyMatchers.property;

public class PackageMatchers
{
    public static final Property<Package, String, String> name =
        property("name");
    public static final Property<Package, Bundle, Bundle> exportingBundle =
        property("exporting bundle");
    public static final Property<Package, Iterable<Bundle>, Bundle> importingBundles =
        iterableProperty("importing bundles");
    public static final Property<Package, Version, Version> version =
        property("version");
}
