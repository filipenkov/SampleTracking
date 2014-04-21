package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Set;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestJacksonCompatibilityHack extends RestFuncTest
{
    /**
     * These are all the packages exported by Atlassian REST 2.5.0. We need to make sure they remain exported in all
     * JIRA 5.x releases.
     */
    static final Set<ExportPackage> REST_25x_PACKAGES;

    /**
     * Initialise {@link #REST_25x_PACKAGES}.
     */
    static
    {
        ImmutableSet.Builder<ExportPackage> builder = ImmutableSet.builder();
        ImmutableSet<String> packageNames = ImmutableSet.of(
                "org.codehaus.jackson.annotate",
                "org.codehaus.jackson.impl",
                "org.codehaus.jackson.io",
                "org.codehaus.jackson.jaxrs",
                "org.codehaus.jackson.map.annotate",
                "org.codehaus.jackson.map.deser",
                "org.codehaus.jackson.map.ext",
                "org.codehaus.jackson.map.introspect",
                "org.codehaus.jackson.map.ser",
                "org.codehaus.jackson.map.type",
                "org.codehaus.jackson.map.util",
                "org.codehaus.jackson.map",
                "org.codehaus.jackson.node",
                "org.codehaus.jackson.schema",
                "org.codehaus.jackson.sym",
                "org.codehaus.jackson.type",
                "org.codehaus.jackson.util",
                "org.codehaus.jackson.xc",
                "org.codehaus.jackson");

        for (String packageName : packageNames)
        {
            builder.add(new ExportPackage(packageName, "2.5.0"));
        }

        REST_25x_PACKAGES = builder.build();
    }

    public void testThatAllJacksonPackagesAreExportedWithVersion2_5_0() throws Exception
    {
        Set<ExportPackage> systemBundleExports = getBundleExportsFromUPM("com.atlassian.jira.atlassian-rest-module-compat25");
        Sets.SetView<ExportPackage> notExported = Sets.difference(REST_25x_PACKAGES, systemBundleExports);
        if (!notExported.isEmpty())
        {
            fail("Binary-incompatible change in Export-Package detected. Missing exports: \n * " + StringUtils.join(notExported, "\n * "));
        }
    }

    protected Set<ExportPackage> getBundleExportsFromUPM(String symbolicName) throws JSONException
    {
        int bundleId = getBundleId(symbolicName);

        Set<ExportPackage> exportPackages = Sets.newHashSet();

        JSONObject bundle = getJSON("rest/plugins/1.0/bundles/" + bundleId);
        JSONObject parsedHeaders = bundle.getJSONObject("parsedHeaders");
        JSONArray exportPackageArray = parsedHeaders.getJSONArray("Export-Package");
        for (int i = 0; i < exportPackageArray.length(); i++)
        {
            JSONObject path = exportPackageArray.getJSONObject(i);

            exportPackages.add(new ExportPackage(path.getString("path"), path.getJSONObject("parameters").getString("version")));
        }

        return exportPackages;
    }

    protected int getBundleId(String symbolicName) throws JSONException
    {
        JSONObject bundles = getJSON("rest/plugins/1.0/bundles/");
        JSONArray entries = bundles.getJSONArray("entries");
        for (int i = 0; i < entries.length(); i++)
        {
            JSONObject bundle = entries.getJSONObject(i);
            String bundleSymbolicName = bundle.getString("symbolicName");
            if (symbolicName.equals(bundleSymbolicName))
            {
                return bundle.getInt("id");
            }
        }

        throw new IllegalArgumentException("Not found: " + symbolicName);
    }

    public static class ExportPackage
    {
        public final String name;
        public final String version;

        public ExportPackage(String name, String version)
        {
            this.name = name;
            this.version = version;
        }

        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public String toString()
        {
            return name + ",version=" + version;
        }
    }
}
