package com.atlassian.jira.quickedit.rest.api.field;

import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides a Tab with its fields sorted in alphabetical order.
 *
 * @since v1.0
 */
@XmlRootElement (name = "tab")
public class TabWithLabels
{
    @XmlElement (name = "label")
    private String label;

    @XmlElement (name = "fields")
    private SortedSet<Field> fields = new TreeSet<Field>();

    private TabWithLabels() {}

    public TabWithLabels(final String label)
    {
        this.label = label;
    }

    public void add(Field field)
    {
        this.fields.add(field);
    }

    public SortedSet<Field> getFields()
    {
        return fields;
    }

    @XmlRootElement
    static class Field implements Comparable<Field>
    {
        @XmlElement (name = "label")
        private String label;
        @XmlElement (name = "id")
        private String id;

        private Field() {}

        Field(final String label, final String id)
        {
            this.label = label;
            this.id = id;
        }
        
        @Override
        public int compareTo(final Field otherField)
        {
            return this.label.compareTo(otherField.label);
        }
    }
}
