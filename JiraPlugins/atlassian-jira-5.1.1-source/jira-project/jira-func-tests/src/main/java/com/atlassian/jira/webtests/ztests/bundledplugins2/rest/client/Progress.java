package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.codehaus.jackson.annotate.JsonProperty;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;

/**
 * Representation of a progress in the JIRA REST API.
 *
 * @since v5.0
 */
public class Progress
{
    @JsonProperty
    private Long progress;

    @JsonProperty
    private Long total;

    @JsonProperty
    private Long percent;


    public Progress()
    {
    }

    public Progress(Long progress, Long total, Long percent)
    {
        this.progress = progress;
        this.total = total;
        this.percent = percent;
    }

    public Long progress()
    {
        return this.progress;
    }

    public Progress progress(Long progress)
    {
        return new Progress(progress, total, percent);
    }

    public Long total()
    {
        return this.total;
    }

    public Progress total(Long total)
    {
        return new Progress(progress, total, percent);
    }

    public Long percent()
    {
        return this.percent;
    }

    public Progress percent(Long percent)
    {
        return new Progress(progress, total, percent);
    }


    @Override
    public boolean equals(Object o) { return reflectionEquals(this, o); }

    @Override
    public int hashCode() { return reflectionHashCode(this); }
}
