package com.atlassian.upm.test.osgi;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.test.osgi.PropertyMatchers.Property;

import static com.atlassian.upm.test.osgi.PropertyMatchers.iterableProperty;
import static com.atlassian.upm.test.osgi.PropertyMatchers.property;

public final class ServiceMatchers
{
    public static final Property<Service, Bundle, Bundle> bundle =
        property("bundle");
    public static final Property<Service, Iterable<Bundle>, Bundle> usingBundles =
        iterableProperty("using bundles");
    public static final Property<Service, Iterable<String>, Iterable<String>> objectClasses =
        property("object classes");
    public static final Property<Service, String, String> description =
        property("description");
    public static final Property<Service, Long, Long> id =
        property("id");
    public static final Property<Service, Iterable<String>, Iterable<String>> pid =
        property("pid");
    public static final Property<Service, Integer, Integer> ranking =
        property("ranking");
    public static final Property<Service, String, String> vendor =
        property("vendor");
}
