package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.rest.model.adapter.ApplicationIdAdapter;
import com.atlassian.applinks.core.rest.model.adapter.RequiredURIAdapter;
import com.atlassian.applinks.core.rest.model.adapter.TypeIdAdapter;
import com.atlassian.applinks.core.rest.model.adapter.VersionAdapter;
import com.atlassian.applinks.core.rest.util.EntityUtil;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.sal.api.ApplicationProperties;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.util.Set;

/**
 * @since v3.0
 */
@XmlRootElement(name = "manifest")
public class ManifestEntity
{
    private static final Logger LOG = LoggerFactory.getLogger(ManifestEntity.class);

    @XmlJavaTypeAdapter(ApplicationIdAdapter.class)
    private ApplicationId id;
    private String name;
    @XmlJavaTypeAdapter(TypeIdAdapter.class)
    private TypeId typeId;
    private String version;
    private long buildNumber;
    @XmlJavaTypeAdapter(VersionAdapter.class)
    private Version applinksVersion;
    private Set<String> inboundAuthenticationTypes;
    private Set<String> outboundAuthenticationTypes;
    private Boolean publicSignup;
    @XmlJavaTypeAdapter(RequiredURIAdapter.class)
    private URI url;

    @SuppressWarnings("unused")
    private ManifestEntity()
    {
    }

    public ManifestEntity(final InternalHostApplication internalHostApp, final ApplicationProperties applicationProperties, final AppLinkPluginUtil pluginUtil)
    {
        this.name = internalHostApp.getName();
        this.typeId = TypeId.getTypeId(internalHostApp.getType());
        this.url = internalHostApp.getBaseUrl();
        this.inboundAuthenticationTypes = EntityUtil.getClassNames(internalHostApp.getSupportedInboundAuthenticationTypes());
        this.outboundAuthenticationTypes = EntityUtil.getClassNames(internalHostApp.getSupportedInboundAuthenticationTypes());
        this.id = internalHostApp.getId();
        this.applinksVersion = pluginUtil.getVersion();
        this.version = applicationProperties.getVersion();
        this.publicSignup = internalHostApp.hasPublicSignup();
        try
        {
            this.buildNumber = Long.parseLong(applicationProperties.getBuildNumber());
        }
        catch (NumberFormatException nfe)
        {
            this.buildNumber = 0L;
            LOG.warn("Cannot parse the application's build number {0}, using 0 instead.",
                    applicationProperties.getBuildNumber());
        }
    }

    public ManifestEntity(final Manifest manifest)
    {
        this.name = manifest.getName();
        this.typeId = manifest.getTypeId();
        this.url = manifest.getUrl();
        this.inboundAuthenticationTypes = EntityUtil.getClassNames(manifest.getInboundAuthenticationTypes());
        this.outboundAuthenticationTypes = EntityUtil.getClassNames(manifest.getOutboundAuthenticationTypes());
        this.id = manifest.getId();
        this.applinksVersion = manifest.getAppLinksVersion();
        this.version = manifest.getVersion();
        this.buildNumber = manifest.getBuildNumber();
        this.publicSignup = manifest.hasPublicSignup();
    }

    public ApplicationId getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public TypeId getTypeId()
    {
        return typeId;
    }

    public long getBuildNumber()
    {
        return buildNumber;
    }

    public String getVersion()
    {
        return version;
    }

    public URI getUrl()
    {
        return url;
    }

    public Version getApplinksVersion()
    {
        return applinksVersion;
    }

    public Boolean hasPublicSignup()
    {
        return publicSignup;
    }

    public Set<String> getInboundAuthenticationTypes()
    {
        return inboundAuthenticationTypes;
    }

    public Set<String> getOutboundAuthenticationTypes()
    {
        return outboundAuthenticationTypes;
    }
}
