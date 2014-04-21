package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import static com.google.common.collect.Collections2.transform;

/**
 * A JSON-convertable representation of a Status
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class StatusJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String statusColor;

    @JsonProperty
    private String description;

    @JsonProperty
    private String iconUrl;

    @JsonProperty
    private String name;

    @JsonProperty
    private String id;

    public StatusJsonBean()
    {
    }

    public StatusJsonBean(String self, String statusColor, String description, String iconUrl, String name, String id)
    {
        this.self = self;
        this.statusColor = statusColor;
        this.description = description;
        this.iconUrl = iconUrl;
        this.name = name;
        this.id = id;
    }

    public String self()
    {
        return self;
    }

    public StatusJsonBean self(String self)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String statusColor()
    {
        return statusColor;
    }

    public StatusJsonBean statusColor(String statusColor)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String description()
    {
        return description;
    }

    public StatusJsonBean description(String description)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String iconUrl()
    {
        return iconUrl;
    }

    public StatusJsonBean iconUrl(String iconUrl)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String name()
    {
        return name;
    }

    public StatusJsonBean name(String name)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    public String id()
    {
        return id;
    }

    public StatusJsonBean id(String id)
    {
        return new StatusJsonBean(self, statusColor, description, iconUrl, name, id);
    }

    /**
     * @return null if the input is null
     */
    public static StatusJsonBean bean(final Status status, final JiraBaseUrls urls)
    {
        if (status == null)
        {
            return null;
        }

           String absoluteIconUrl;
        try
        {
            absoluteIconUrl = new URL(status.getIconUrl()).toString();
        }
        catch (MalformedURLException e)
        {
            absoluteIconUrl = urls.baseUrl() + status.getIconUrl();
        }

        return new StatusJsonBean()
                .self(urls.restApi2BaseUrl() + "status/" + JiraUrlCodec.encode(status.getId()))
                .name(status.getNameTranslation())
                .id(status.getId())
                .iconUrl(absoluteIconUrl)
                .description(status.getDescTranslation());
    }

    public static Collection<StatusJsonBean> beans(final Collection<Status> allowedValues, final JiraBaseUrls baseUrls)
    {
        return transform(allowedValues, new Function<Status, StatusJsonBean>()
        {
            @Override
            public StatusJsonBean apply(Status from)
            {
                return StatusJsonBean.bean(from, baseUrls);
            }
        });

    }

    public static StatusJsonBean bean(String id, String name, String self, String iconUrl, String description)
    {
        return new StatusJsonBean()
                .self(self)
                .description(description)
                .iconUrl(iconUrl)
                .name(name)
                .id(id);
    }
}
