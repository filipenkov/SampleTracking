package com.atlassian.jira.plugin.issuenav.viewissue.webpanel;

import com.atlassian.jira.rest.v2.issue.LinkGroupBean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Represents a web-panel on the view issue page.
 *
 * @since v5.1
 */
@XmlRootElement
public class WebPanelBean
{
    @XmlElement
    private String completeKey;
    @XmlElement
    private String prefix;
    @XmlElement
    private String id;
    @XmlElement
    private String styleClass;
    @XmlElement
    private String label;
    @XmlElement
    private boolean renderHeader;
    @XmlElement
    private LinkGroupBean headerLinks;
    @XmlElement
    private List<String> subpanelHtmls;
    @XmlElement
    private String html;

    WebPanelBean() { }

    private WebPanelBean(final String completeKey, final String prefix, final String id, final String styleClass,
            final String label, final boolean renderHeader, final LinkGroupBean headerLinks,
            final List<String> subpanelHtmls, final String html)
    {
        this.completeKey = completeKey;
        this.prefix = prefix;
        this.id = id;
        this.styleClass = styleClass;
        this.label = label;
        this.renderHeader = renderHeader;
        this.headerLinks = headerLinks;
        this.subpanelHtmls = subpanelHtmls;
        this.html = html;
    }

    public String getCompleteKey()
    {
        return completeKey;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getId()
    {
        return id;
    }

    public String getStyleClass()
    {
        return styleClass;
    }

    public String getLabel()
    {
        return label;
    }

    public boolean isRenderHeader()
    {
        return renderHeader;
    }

    public LinkGroupBean getHeaderLinks()
    {
        return headerLinks;
    }

    public List<String> getSubpanelHtmls()
    {
        return subpanelHtmls;
    }

    public String getHtml()
    {
        return html;
    }
    
    public static class Builder
    {
        private String completeKey;
        private String prefix;
        private String id;
        private String styleClass;
        private String label;
        private boolean renderHeader;
        private LinkGroupBean headerLinks;
        private List<String> subpanelHtmls;
        private String html;

        public Builder completeKey(final String completeKey)
        {
            this.completeKey = completeKey;
            return this;
        }

        public Builder prefix(final String prefix)
        {
            this.prefix = prefix;
            return this;
        }

        public Builder id(final String id)
        {
            this.id = id;
            return this;
        }

        public Builder styleClass(final String styleClass)
        {
            this.styleClass = styleClass;
            return this;
        }

        public Builder label(final String label)
        {
            this.label = label;
            return this;
        }

        public Builder renderHeader(final boolean renderHeader)
        {
            this.renderHeader = renderHeader;
            return this;
        }

        public Builder headerLinks(final LinkGroupBean headerLinks)
        {
            this.headerLinks = headerLinks;
            return this;
        }

        public Builder subpanelHtmls(final List<String> subpanelHtmls)
        {
            this.subpanelHtmls = subpanelHtmls;
            return this;
        }

        public Builder html(final String html)
        {
            this.html = html;
            return this;
        }

        public WebPanelBean build()
        {
            return new WebPanelBean(completeKey, prefix, id, styleClass, label, renderHeader, headerLinks, subpanelHtmls, html);
        }
    }
}
