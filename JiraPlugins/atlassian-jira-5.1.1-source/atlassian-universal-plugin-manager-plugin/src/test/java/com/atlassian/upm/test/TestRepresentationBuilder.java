package com.atlassian.upm.test;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.atlassian.upm.rest.representations.LicenseDetailsRepresentation;
import com.atlassian.upm.rest.representations.PluginModuleRepresentation;
import com.atlassian.upm.rest.representations.PluginRepresentation;
import com.atlassian.upm.rest.representations.PluginRepresentation.ModuleEntryRepresentation;
import com.atlassian.upm.rest.representations.PluginRepresentation.Vendor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Ignore;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class TestRepresentationBuilder
{
    @Ignore
    public static final class PluginRepresentationBuilder
    {
        private Map<String, URI> links = ImmutableMap.of();
        private String key = "com.example.test.plugin";
        private boolean enabled = true;
        private boolean enabledByDefault = true;
        private String version = "1.0";
        private String description = "Dummy Description";
        private Vendor vendor = newVendor("Dummy Vendor", "http://dummy.vendor.url/");
        private String name = "Dummy Plugin";
        private Collection<ModuleEntryRepresentation> modules = ImmutableList.of();
        private boolean userInstalled = false;
        private boolean optional = true;
        private String configureUrl = "/dummy/configure/link/";
        private String restartState = "";
        private boolean unrecognizedModuleTypes = false;
        private LicenseDetailsRepresentation licenseDetails = null;
        private boolean licenseReadOnly = false;
        private URI licenseAdminUri;
        private String hostLicenseOrganizationName = "dummy company";

        public PluginRepresentationBuilder links(Map<String, URI> links)
        {
            this.links = ImmutableMap.copyOf(links);
            return this;
        }

        public PluginRepresentationBuilder enabled(boolean enabled)
        {
            this.enabled = enabled;
            return this;
        }

        public PluginRepresentationBuilder enabledByDefault(boolean enabledByDefault)
        {
            this.enabledByDefault = enabledByDefault;
            return this;
        }

        public PluginRepresentationBuilder version(String version)
        {
            this.version = version;
            return this;
        }

        public PluginRepresentationBuilder description(String description)
        {
            this.description = description;
            return this;
        }

        public PluginRepresentationBuilder vendor(Vendor vendor)
        {
            this.vendor = vendor;
            return this;
        }

        public PluginRepresentationBuilder name(String name)
        {
            this.name = name;
            return this;
        }

        public PluginRepresentationBuilder hostLicenseOrganizationName(String hostLicenseOrganizationName)
        {
            this.hostLicenseOrganizationName = hostLicenseOrganizationName;
            return this;
        }

        public PluginRepresentationBuilder rawLicense(String rawLicense)
        {
            this.licenseDetails = new LicenseDetailsRepresentation(true, null, false, false, 0,
                                                                                        new Date(), null, new Date(),
                                                                                        rawLicense, new Date().toString(),
                                                                                        null, hostLicenseOrganizationName, "owner@example.com");
            return this;
        }
        
        public PluginRepresentationBuilder licenseReadOnly(boolean licenseReadOnly)
        {
            this.licenseReadOnly = licenseReadOnly;
            return this;
        }

        public PluginRepresentationBuilder licenseAdminUri(URI licenseAdminUri)
        {
            this.licenseAdminUri = licenseAdminUri;
            return this;
        }
        
        public PluginRepresentationBuilder modules(Collection<ModuleEntryRepresentation> modules)
        {
            this.modules = ImmutableList.copyOf(modules);
            return this;
        }

        public PluginRepresentationBuilder userInstalled(boolean userInstalled)
        {
            this.userInstalled = userInstalled;
            return this;
        }

        public PluginRepresentationBuilder optional(boolean optional)
        {
            this.optional = optional;
            return this;
        }

        public PluginRepresentationBuilder configureUrl(String configureUrl)
        {
            this.configureUrl = configureUrl;
            return this;
        }

        public PluginRepresentationBuilder restartState(String restartState)
        {
            this.restartState = restartState;
            return this;
        }

        public PluginRepresentation build()
        {
            return new PluginRepresentation(links, key, enabled, enabledByDefault, version, description, vendor, name,
                modules, userInstalled, optional, unrecognizedModuleTypes, configureUrl, restartState, 
                licenseDetails, licenseReadOnly, licenseAdminUri);
        }

        private Vendor newVendor(String name, String url)
        {
            if (isEmpty(name))
            {
                return null;
            }
            return new Vendor(name, isEmpty(url) ? null : URI.create(url));
        }
    }

    @Ignore
    public static final class PluginModuleRepresentationBuilder
    {
        private Map<String, URI> links = ImmutableMap.of();
        private boolean enabled = true;
        private String name = "Dummy Plugin Module";
        private String description = "Dummy Plugin Module Description";

        public PluginModuleRepresentationBuilder links(Map<String, URI> links)
        {
            this.links = ImmutableMap.copyOf(links);
            return this;
        }

        public PluginModuleRepresentationBuilder enabled(boolean enabled)
        {
            this.enabled = enabled;
            return this;
        }

        public PluginModuleRepresentationBuilder name(String name)
        {
            this.name = name;
            return this;
        }

        public PluginModuleRepresentationBuilder description(String description)
        {
            this.description = description;
            return this;
        }

        public PluginModuleRepresentation build()
        {
            return new PluginModuleRepresentation(links, enabled, name, description);
        }
    }
}
