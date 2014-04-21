package com.atlassian.applinks.core.rest.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "webItem")
public class WebItemEntity
{
    private String id;
    private String url;
    private String accessKey;
    private String iconUrl;
    private Integer iconHeight;
    private Integer iconWidth;
    private String label;
    private String tooltip;
    private String styleClass;

    private WebItemEntity()
    {
    }

    /**
     * Use the {@link Builder}
     */
    private WebItemEntity(final String id, final String url, final String accessKey, final String iconUrl,
                         final Integer iconHeight, final Integer iconWidth, final String label, final String tooltip,
                         final String styleClass)
    {
        this.id = id;
        this.url = url;
        this.accessKey = accessKey;
        this.iconUrl = iconUrl;
        this.iconHeight = iconHeight;
        this.iconWidth = iconWidth;
        this.label = label;
        this.tooltip = tooltip;
        this.styleClass = styleClass;
    }

    public String getId()
    {
        return id;
    }

    public String getUrl()
    {
        return url;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public Integer getIconHeight()
    {
        return iconHeight;
    }

    public Integer getIconWidth()
    {
        return iconWidth;
    }

    public String getLabel()
    {
        return label;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public String getStyleClass()
    {
        return styleClass;
    }

    public static class Builder {

        private String id;
        private String url;
        private String accessKey;
        private String iconUrl;
        private Integer iconHeight;
        private Integer iconWidth;
        private String label;
        private String tooltip;
        private String styleClass;

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder url(final String url) {
            this.url = url;
            return this;
        }

        public Builder accessKey(final String accessKey) {
            this.accessKey = accessKey;
            return this;
        }

        public Builder iconUrl(final String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Builder iconHeight(final Integer iconHeight) {
            this.iconHeight = iconHeight;
            return this;
        }

        public Builder iconWidth(final Integer iconWidth) {
            this.iconWidth = iconWidth;
            return this;
        }

        public Builder label(final String label) {
            this.label = label;
            return this;
        }

        public Builder tooltip(final String tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder styleClass(final String styleClass) {
            this.styleClass = styleClass;
            return this;
        }

        public WebItemEntity build() {
            return new WebItemEntity(id, url, accessKey, iconUrl, iconHeight, iconWidth, label, tooltip, styleClass);
        }

    }
}
