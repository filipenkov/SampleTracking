package com.atlassian.jira.collector.plugin.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "field")
public class Field
{
    @XmlElement (name = "id")
    private String id;
    @XmlElement (name = "label")
    private String label;
    @XmlElement (name = "required")
    private boolean required;
    @XmlElement (name = "editHtml")
    private String editHtml;

    private Field() {}

    public Field(final String id, final String label, final boolean required, final String editHtml)
    {
        this.id = id;
        this.label = label;
        this.required = required;
        this.editHtml = editHtml;
    }


}
