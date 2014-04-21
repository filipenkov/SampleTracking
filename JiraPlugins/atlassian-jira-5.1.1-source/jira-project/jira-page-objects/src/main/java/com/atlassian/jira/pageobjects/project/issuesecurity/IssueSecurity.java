package com.atlassian.jira.pageobjects.project.issuesecurity;

import com.google.common.base.Objects;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * @since v4.4
 */
public class IssueSecurity
{
        private String name;
        private String description;
        private List<String> entities;

        public String getName()
        {
            return name;
        }

        public IssueSecurity setName(String name)
        {
            this.name = name;
            return this;
        }

        public String getDescription()
        {
            return description;
        }

        public IssueSecurity setDescription(String description)
        {
            this.description = description;
            return this;
        }

        public List<String> getEntities()
        {
            return entities;
        }

        public IssueSecurity setEntities(List<String> entities)
        {
            this.entities = entities;
            return this;
        }

        public boolean equals(final Object object)
        {
            return EqualsBuilder.reflectionEquals(this, object);
        }

        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
}
