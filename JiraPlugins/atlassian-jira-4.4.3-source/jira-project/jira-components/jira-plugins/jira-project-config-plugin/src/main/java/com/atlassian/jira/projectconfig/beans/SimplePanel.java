package com.atlassian.jira.projectconfig.beans;

/**
 * A simple bean containing rendered panels for the summary page. Only has the name
 * and the contents to render
 *
 * @since v4.4
 */
public class SimplePanel
{
    private final String name;
    private final String panelKey;
    private final String contentHtml;

    public SimplePanel(final String name, final String panelKey, final String contentHtml)
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


    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SimplePanel that = (SimplePanel) o;

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

    @Override
    public String toString()
    {
        return "SimplePanel{" +
                "name='" + name + '\'' +
                ", panelKey='" + panelKey + '\'' +
                ", contentHtml='" + contentHtml + '\'' +
                '}';
    }
}
