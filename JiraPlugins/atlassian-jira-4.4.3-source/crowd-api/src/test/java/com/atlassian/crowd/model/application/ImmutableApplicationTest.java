package com.atlassian.crowd.model.application;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests for <tt>ImmutableApplication</tt>.
 *
 * @since 2.2
 */
public class ImmutableApplicationTest
{
    @Test
    public void testGetDirectoryMapping() throws Exception
    {
        List<DirectoryMapping> directoryMappings = Lists.newArrayList(
                createDirectoryMapping(1L),
                createDirectoryMapping(2L),
                createDirectoryMapping(null),
                createDirectoryMapping(3L)
        );

        ImmutableApplication application = ImmutableApplication.builder("Application", ApplicationType.CONFLUENCE)
                                            .setDirectoryMappings(directoryMappings)
                                            .build();

        // remove the expected mapping to ensure that ImmutableApplication makes a copy of the collection
        directoryMappings.remove(directoryMappings.size() - 1);

        DirectoryMapping directoryMapping = application.getDirectoryMapping(3L);
        assertEquals(Long.valueOf(3L), directoryMapping.getDirectory().getId());
    }

    /**
     * Creates a new DirectoryMapping mapping the specified directory.
     *
     * @param directoryId directory ID
     * @return new DirectoryMapping
     */
    private static DirectoryMapping createDirectoryMapping(final Long directoryId)
    {
        return new DirectoryMapping(null, createDirectory(directoryId), true);
    }

    /**
     * Creates a new Directory with the specified directory ID.
     *
     * @param directoryId directory ID
     * @return new Directory
     */
    private static Directory createDirectory(final Long directoryId)
    {
        return new Directory() {
            public Long getId()
            {
                return directoryId;
            }

            public String getName()
            {
                return null;
            }

            public boolean isActive()
            {
                return false;
            }

            public String getEncryptionType()
            {
                return null;
            }

            public Map<String, String> getAttributes()
            {
                return null;
            }

            public Set<OperationType> getAllowedOperations()
            {
                return null;
            }

            public String getDescription()
            {
                return null;
            }

            public DirectoryType getType()
            {
                return null;
            }

            public String getImplementationClass()
            {
                return null;
            }

            public Date getCreatedDate()
            {
                return null;
            }

            public Date getUpdatedDate()
            {
                return null;
            }

            public Set<String> getValues(final String key)
            {
                return null;
            }

            public String getValue(final String key)
            {
                return null;
            }

            public Set<String> getKeys()
            {
                return null;
            }

            public boolean isEmpty()
            {
                return false;
            }
        };
    }
}
