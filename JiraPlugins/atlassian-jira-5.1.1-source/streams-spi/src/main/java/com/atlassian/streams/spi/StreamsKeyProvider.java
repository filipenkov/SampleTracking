package com.atlassian.streams.spi;


/**
 * Provides keys to filter against in an Activity Stream.  These will depend on the implementing application. For
 * example in JIRA this will be project keys, in Confluence space keys and in Fisheye repositories.
 *
 * @since v3.0
 */
public interface StreamsKeyProvider
{
    static final String ALL_PROJECTS_KEY = "__all_projects__";

    static class StreamsKey
    {
        private final String key;
        private final String label;

        public StreamsKey(final String key, final String label)
        {
            this.key = key;
            this.label = label;
        }

        public String getKey()
        {
            return key;
        }

        public String getLabel()
        {
            return label;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final StreamsKey that = (StreamsKey) o;

            if (!key.equals(that.key))
            {
                return false;
            }
            if (!label.equals(that.label))
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = key.hashCode();
            result = 31 * result + label.hashCode();
            return result;
        }
    }

    /**
     * Returns a set of StreamsKeys to allow to filter the stream by
     *
     * @return a set of StreamsKeys to allow to filter the stream by
     */
    Iterable<StreamsKey> getKeys();
}
