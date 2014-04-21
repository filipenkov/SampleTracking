package com.atlassian.upm.test.osgi;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Bundle.HeaderClause;
import com.atlassian.upm.osgi.Bundle.State;
import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.test.osgi.PropertyMatchers.Property;

import static com.atlassian.upm.test.osgi.PropertyMatchers.iterableProperty;
import static com.atlassian.upm.test.osgi.PropertyMatchers.iterableValue;
import static com.atlassian.upm.test.osgi.PropertyMatchers.key;
import static com.atlassian.upm.test.osgi.PropertyMatchers.mapProperty;
import static com.atlassian.upm.test.osgi.PropertyMatchers.property;

public class BundleMatchers
{
    public static final Property<Bundle, State, State> state =
        property("state");
    public static final Property<Bundle, Map<String, String>, Map<String, String>> unparsedHeaders =
        property("unparsed headers");
    public static final Property<Bundle, Map<String, Iterable<HeaderClause>>, Entry<String, Iterable<HeaderClause>>> parsedHeaders =
        mapProperty("parsed headers");
    public static final Property<Bundle, Long, Long> id =
        property("id");
    public static final Property<Bundle, URI, URI> location =
        property("location");
    public static final Property<Bundle, Iterable<Service>, Service> registeredServices =
        iterableProperty("registered services");
    public static final Property<Bundle, Iterable<Service>, Service> servicesInUse =
        iterableProperty("services in use");
    public static final Property<Bundle, String, String> symbolicName =
        property("symbolic name");
    public static final Property<Bundle, String, String> name =
        property("name");
    public static final Property<Bundle, Version, Version> version =
        property("version");

    public static final Property<Entry<String, Iterable<HeaderClause>>, String, String> headerName =
        key("header name");
    public static final Property<Entry<String, Iterable<HeaderClause>>, Iterable<HeaderClause>, HeaderClause> headerClauses =
        iterableValue("header clauses");

    public static final Property<HeaderClause, String, String> path =
        property("path");
    public static final Property<HeaderClause, Map<String, String>, Map<String, String>> parameters =
        property("parameters");
    public static final Property<HeaderClause, Package, Package> referencedPackage =
        property("referenced package");
}
