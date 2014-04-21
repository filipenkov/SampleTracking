package com.atlassian.streams.spi;

import com.atlassian.streams.api.ActivityObjectType;
import com.atlassian.streams.api.ActivityVerb;

import static com.atlassian.streams.api.common.Preconditions.checkNotBlank;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides filter options in an Activity Stream. These will depend on the implementing application. For
 * example in JIRA this will be project keys, in Confluence space keys and in Fisheye repositories.
 *
 * @since v3.0
 */
public interface StreamsFilterOptionProvider
{
    /**
     * The list of filter options available for the specific application
     *
     * @return The list of filter options available for the specific application
     */
    Iterable<StreamsFilterOption> getFilterOptions();

    /**
     * The list of activities that the application provider supports.
     *
     * @return The list of activities that the application provider supports
     */
    Iterable<ActivityOption> getActivities();
    
    final class ActivityOption
    {
        private final String displayName;
        private final ActivityObjectType type;
        private final ActivityVerb verb;
        
        public ActivityOption(String displayName, ActivityObjectType type, ActivityVerb verb)
        {
            this.displayName = checkNotBlank(displayName, "displayName");
            this.type = checkNotNull(type, "type");
            this.verb = checkNotNull(verb, "verb");
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public ActivityObjectType getType()
        {
            return type;
        }

        public ActivityVerb getVerb()
        {
            return verb;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            return prime * (prime + type.hashCode()) + verb.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            ActivityOption other = (ActivityOption) obj;
            if (type == null && other.type != null ||
                    verb == null && other.verb != null)
            {
                return false;
            }
            else
            {
                return type.equals(other.type) && verb.equals(other.verb);
            }
        }
    }
}
