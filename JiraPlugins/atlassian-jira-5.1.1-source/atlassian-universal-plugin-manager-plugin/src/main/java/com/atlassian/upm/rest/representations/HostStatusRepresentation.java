package com.atlassian.upm.rest.representations;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Wraps several global state properties which are included in most of our responses.
 */
public class HostStatusRepresentation
{
    @JsonProperty private final boolean safeMode;
    @JsonProperty private final boolean pacDisabled;
    @JsonProperty private final boolean pacUnavailable;
    @JsonProperty private final LicenseDetailsRepresentation hostLicense;
    
    @JsonCreator
    public HostStatusRepresentation(@JsonProperty("safeMode") boolean safeMode,
                                @JsonProperty("pacDisabled") boolean pacDisabled,
                                @JsonProperty("pacUnavailable") boolean pacUnavailable,
                                @JsonProperty("hostLicense") LicenseDetailsRepresentation hostLicense)
    {
        this.safeMode = safeMode;
        this.pacDisabled = pacDisabled;
        this.pacUnavailable = pacUnavailable;
        this.hostLicense = hostLicense;
    }

    public boolean isSafeMode()
    {
        return safeMode;
    }

    public boolean isPacDisabled()
    {
        return pacDisabled;
    }
    
    public boolean isPacUnavailable()
    {
        return pacUnavailable;
    }
    
    public LicenseDetailsRepresentation getHostLicense()
    {
        return hostLicense;
    }
}
