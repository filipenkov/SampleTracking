package com.atlassian.jira.rest.v2.common;

import com.atlassian.jira.plugin.webfragment.model.SimpleLink;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean representing a SimpleLink.  This is useful when constructing dropdowns populated dynamically.
 *
 * @since v5.0
 */
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="link")
public class SimpleLinkBean
{
    @XmlElement
    private String id;

    @XmlElement
    private String styleClass;

    @XmlElement
    private String iconClass;

    @XmlElement
    private String label;

    @XmlElement
    private String title;

    @XmlElement
    private String href;

    //Needed so that JAXB works.
    public SimpleLinkBean(){}

    public SimpleLinkBean(SimpleLink link)
    {
        this(link.getId(), link.getStyleClass(), link.getLabel(), link.getTitle(), link.getUrl(), link.getParams() != null ? link.getParams().get("iconClass") : null);
    }
    public SimpleLinkBean(String id, String styleClass, String label, String title, String href, String iconClass)
    {
        this.id = id;
        this.styleClass = styleClass;
        this.label = label;
        this.title = title;
        this.href = href;
        this.iconClass = iconClass;
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

    public String getTitle()
    {
        return title;
    }

    public String getHref()
    {
        return href;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SimpleLinkBean that = (SimpleLinkBean) o;

        if (!href.equals(that.href)) { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (!label.equals(that.label)) { return false; }
        if (styleClass != null ? !styleClass.equals(that.styleClass) : that.styleClass != null) { return false; }
        if (title != null ? !title.equals(that.title) : that.title != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (styleClass != null ? styleClass.hashCode() : 0);
        result = 31 * result + label.hashCode();
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + href.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return "SimpleLinkBean{" +
                "id='" + id + '\'' +
                ", styleClass='" + styleClass + '\'' +
                ", label='" + label + '\'' +
                ", title='" + title + '\'' +
                ", href='" + href + '\'' +
                '}';
    }

    public static final SimpleLinkBean DOC_EXAMPLE = new SimpleLinkBean("edit-issue", "edit-cls", "Edit",
            "Click to Edit the Issue", "/secure/EditIssue!default.jspa?id=10000", "icon-edit");
}
