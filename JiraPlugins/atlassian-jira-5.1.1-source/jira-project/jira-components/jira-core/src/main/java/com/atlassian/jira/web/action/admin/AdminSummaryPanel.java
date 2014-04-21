package com.atlassian.jira.web.action.admin;

/**
 * A simple bean containing rendered panels for the summary page. Only has the name
 * and the contents to render
 *
 * @since v4.4
 */
public class AdminSummaryPanel
{
    private final String name;
    private final String panelKey;
    private final String contentHtml;

    public AdminSummaryPanel(final String name, final String panelKey, final String contentHtml)
    {
        this.name = name;
        this.panelKey = panelKey;
        this.contentHtml = contentHtml;
    }

    public String getName()
    {
        return name;
    }

    public String getPanelKey()
    {
        return panelKey;
    }

    public String getContentHtml()
    {
        return contentHtml;
    }

    public static class SimplePanelLink
    {
        private final String title;
        private final String url;

        public SimplePanelLink(final String title, final String url)
        {

            this.title = title;
            this.url = url;
        }

        public String getTitle()
        {
            return title;
        }

        public String getUrl()
        {
            return url;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            SimplePanelLink that = (SimplePanelLink) o;

            if (title != null ? !title.equals(that.title) : that.title != null) { return false; }
            if (url != null ? !url.equals(that.url) : that.url != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = title != null ? title.hashCode() : 0;
            result = 31 * result + (url != null ? url.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AdminSummaryPanel that = (AdminSummaryPanel) o;

        if (contentHtml != null ? !contentHtml.equals(that.contentHtml) : that.contentHtml != null) { return false; }
        if (name != null ? !name.equals(that.name) : that.name != null) { return false; }
        if (panelKey != null ? !panelKey.equals(that.panelKey) : that.panelKey != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (panelKey != null ? panelKey.hashCode() : 0);
        result = 31 * result + (contentHtml != null ? contentHtml.hashCode() : 0);
        return result;
    }
}
