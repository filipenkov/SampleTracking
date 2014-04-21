package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.base.Function;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

import static com.google.common.collect.Collections2.transform;

/**
* @since v5.0
*/
public class ResolutionJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String description;

    @JsonProperty
    private String iconUrl;

    @JsonProperty
    private String name;

    public static Collection<ResolutionJsonBean> shortBeans(final Collection<Resolution> resolutions, final JiraBaseUrls urls) {
        return transform(resolutions, new Function<Resolution, ResolutionJsonBean>()
        {
            @Override
            public ResolutionJsonBean apply(Resolution from)
            {
                return shortBean(from, urls);
            }
        });
    }

    /**
     *
     * @return null if the input is null
     */
    public static ResolutionJsonBean shortBean(final Resolution resolution, final JiraBaseUrls urls)
    {
        if (resolution == null)
        {
            return null;
        }
        final ResolutionJsonBean bean = new ResolutionJsonBean();
        bean.self = urls.restApi2BaseUrl() + "resolution/" + JiraUrlCodec.encode(resolution.getId().toString());
        bean.id = resolution.getId();
        bean.name = resolution.getNameTranslation();
        bean.description = resolution.getDescTranslation();
        // Icon URL is not currently used for Resolutions
        // bean.iconUrl = resolution.getIconUrl();
        return bean;
    }
}
