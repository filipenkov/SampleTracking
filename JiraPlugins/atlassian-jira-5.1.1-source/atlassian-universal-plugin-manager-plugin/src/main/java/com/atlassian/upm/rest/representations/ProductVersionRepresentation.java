package com.atlassian.upm.rest.representations;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class ProductVersionRepresentation
{
    @JsonProperty private final boolean development;
    @JsonProperty private final boolean unknown;

    @JsonCreator
    public ProductVersionRepresentation(
            @JsonProperty("development") boolean development,
            @JsonProperty("unknown") boolean unknown)
    {
        this.development = development;
        this.unknown = unknown;
    }
    
    public boolean isDevelopment()
    {
        return development;
    }
    
    public boolean isUnknown()
    {
        return unknown;
    }
}
