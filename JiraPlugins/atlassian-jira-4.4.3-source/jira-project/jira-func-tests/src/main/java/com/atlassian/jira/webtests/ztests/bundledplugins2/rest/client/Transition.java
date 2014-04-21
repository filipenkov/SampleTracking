package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.List;

/**
 * Representations for the transitions resource in the JIRA REST API.
 *
 * @since v4.3
 */
public class Transition
{
    public String name;
    public List<TransitionField> fields;
    public String transitionDestination;

    public static class TransitionField
    {
        public String id;
        public boolean required;
        public String type;

        public TransitionField()
        {
        }

        public TransitionField(String id, boolean required, String type)
        {
            this.id = id;
            this.required = required;
            this.type = type;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            TransitionField that = (TransitionField) o;

            if (required != that.required) { return false; }
            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (type != null ? !type.equals(that.type) : that.type != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (required ? 1 : 0);
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }
}
