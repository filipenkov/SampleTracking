package com.atlassian.upm.rest.resources.updateall;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.collect.ImmutableMap.copyOf;

public final class UpdateAllResults extends UpdateStatus
{
    @JsonProperty private final List<UpdateSucceeded> successes;
    @JsonProperty private final List<UpdateFailed> failures;
    @JsonProperty private final Map<String, URI> links;

    @JsonCreator
    public UpdateAllResults(@JsonProperty("successes") List<UpdateSucceeded> successes,
                            @JsonProperty("failures") List<UpdateFailed> failures,
                            @JsonProperty("links") Map<String, URI> links)
    {
        super(State.COMPLETE);
        this.successes = successes;
        this.failures = failures;
        this.links = copyOf(links);
    }

    public List<UpdateSucceeded> getSuccesses()
    {
        return successes;
    }

    public List<UpdateFailed> getFailures()
    {
        return failures;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }
}