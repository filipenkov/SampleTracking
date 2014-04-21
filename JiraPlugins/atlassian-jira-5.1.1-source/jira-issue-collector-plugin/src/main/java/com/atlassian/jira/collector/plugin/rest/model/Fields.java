package com.atlassian.jira.collector.plugin.rest.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "fields")
public class Fields
{
    @XmlElement (name = "fields")
    final List<Field> fields = new ArrayList<Field>();

    public Fields() { }

    public void addField(final Field field)
    {
        fields.add(field);
    }
}
