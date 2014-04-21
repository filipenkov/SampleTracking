package com.atlassian.jira.pageobjects.project.issuetypes;

import com.atlassian.pageobjects.elements.PageElement;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.4
 */
public class IssueType
{
    private String name;
    private boolean subtask;
    private boolean defaultIssueType;
    private String description;
    private Link workflow;
    private String workflowName;
    private Link fieldLayout;
    private String fieldLayoutName;
    private Link fieldScreenScheme;
    private String fieldScreenSchemeName;

    public String getName()
    {
        return name;
    }

    public IssueType setName(String name)
    {
        this.name = name;
        return this;
    }

    public boolean isSubtask()
    {
        return subtask;
    }

    public void setSubtask(boolean subtask)
    {
        this.subtask = subtask;
    }

    public boolean isDefaultIssueType()
    {
        return defaultIssueType;
    }

    public void setDefaultIssueType(boolean defaultIssueType)
    {
        this.defaultIssueType = defaultIssueType;
    }

    public String getDescription()
    {
        return description;
    }

    public IssueType setDescription(String description)
    {
        this.description = description;
        return this;
    }

    public Link getWorkflow()
    {
        return workflow;
    }

    public void setWorkflow(Link workflow)
    {
        this.workflow = workflow;
    }

    public String getWorkflowName()
    {
        return workflowName;
    }

    public void setWorkflowName(String workflowName)
    {
        this.workflowName = workflowName;
    }

    public Link getFieldLayout()
    {
        return fieldLayout;
    }

    public void setFieldLayout(Link fieldLayout)
    {
        this.fieldLayout = fieldLayout;
    }

    public String getFieldLayoutName()
    {
        return fieldLayoutName;
    }

    public void setFieldLayoutName(String fieldLayoutName)
    {
        this.fieldLayoutName = fieldLayoutName;
    }

    public Link getScreenScheme()
    {
        return fieldScreenScheme;
    }

    public void setFieldScreenScheme(Link fieldScreenScheme)
    {
        this.fieldScreenScheme = fieldScreenScheme;
    }

    public String getScreenSchemeName()
    {
        return fieldScreenSchemeName;
    }

    public void setFieldScreenSchemeName(String fieldScreenSchemeName)
    {
        this.fieldScreenSchemeName = fieldScreenSchemeName;
    }

    public static class Link
    {
        private final String text;
        private final String href;

        public Link(String text, String href)
        {
            this.text = text;
            this.href = href;
        }

        public Link(PageElement element)
        {
            this.text = element.getText();
            this.href = element.getAttribute("href");
        }

        public String getText()
        {
            return text;
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

            Link link = (Link) o;

            if (href != null ? !href.equals(link.href) : link.href != null) { return false; }
            if (text != null ? !text.equals(link.text) : link.text != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = text != null ? text.hashCode() : 0;
            result = 31 * result + (href != null ? href.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return "Link{" +
                    "text='" + text + '\'' +
                    ", href='" + href + '\'' +
                    '}';
        }
    }
}
