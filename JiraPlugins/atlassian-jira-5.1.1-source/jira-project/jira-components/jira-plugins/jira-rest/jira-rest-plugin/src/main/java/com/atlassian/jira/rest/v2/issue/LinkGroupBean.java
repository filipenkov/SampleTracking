package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.v2.common.SimpleLinkBean;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a group of links. Link groups contain:
 * <ul>
 *     <li>id: an optional identifier</li>
 *     <li>header: an optional styled header, e.g. for a dropdown trigger</li>
 *     <li>links: a list of simple links</li>
 *     <li>groups: link groups nested within this link group</li>
 * </ul>
 *
 * @since v5.0
 */
public class LinkGroupBean
{
    @JsonProperty
    private final String id;

    @JsonProperty
    private final String styleClass;

    @JsonProperty
    private final SimpleLinkBean header;

    @JsonProperty
    private final List<SimpleLinkBean> links = new ArrayList<SimpleLinkBean>();

    @JsonProperty
    private final List<LinkGroupBean> groups = new ArrayList<LinkGroupBean>();

    private LinkGroupBean(String id, String styleClass, SimpleLinkBean header, List<SimpleLinkBean> links, List<LinkGroupBean> groups)
    {
        this.id = id;
        this.styleClass = styleClass;
        this.header = header;
        if (links != null)
        {
            this.links.addAll(links);
        }
        if (groups != null)
        {
            this.groups.addAll(groups);
        }
    }

    public static class Builder
    {
        private String id;
        private String styleClass;
        private SimpleLinkBean header;
        private List<SimpleLinkBean> links = new ArrayList<SimpleLinkBean>();
        private List<LinkGroupBean> groups = new ArrayList<LinkGroupBean>();

        public Builder id(String id)
        {
            this.id = id;
            return this;
        }

        public Builder styleClass(String styleClass)
        {
            this.styleClass = styleClass;
            return this;
        }

        public Builder header(SimpleLinkBean header)
        {
            this.header = header;
            return this;
        }

        public Builder addLinks(SimpleLinkBean... links)
        {
            Collections.addAll(this.links, links);
            return this;
        }

        public Builder addLinks(List<SimpleLinkBean> links)
        {
            this.links.addAll(links);
            return this;
        }

        public Builder addGroups(LinkGroupBean... groups)
        {
            Collections.addAll(this.groups, groups);
            return this;
        }
        
        public Builder addGroups(List<LinkGroupBean> groups)
        {
            this.groups.addAll(groups);
            return this;
        }

        public LinkGroupBean build()
        {
            return new LinkGroupBean(id, styleClass, header, links, groups);
        }
    }
    
    public static final LinkGroupBean DOC_EXAMPLE = new LinkGroupBean(
            null,
            null,
            null,
            Lists.newArrayList(SimpleLinkBean.DOC_EXAMPLE, SimpleLinkBean.DOC_EXAMPLE),
            Collections.<LinkGroupBean>emptyList()
    );
    
    public static final LinkGroupBean RECURSIVE_DOC_EXAMPLE = new LinkGroupBean(
            "view.issue.opsbar",
            "sample-style-class",
            SimpleLinkBean.DOC_EXAMPLE,
            Lists.newArrayList(SimpleLinkBean.DOC_EXAMPLE, SimpleLinkBean.DOC_EXAMPLE),
            Lists.newArrayList(DOC_EXAMPLE)
    );
}
