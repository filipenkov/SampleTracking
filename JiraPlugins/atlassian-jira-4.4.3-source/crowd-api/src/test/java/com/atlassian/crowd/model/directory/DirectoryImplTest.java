package com.atlassian.crowd.model.directory;

import com.atlassian.crowd.model.InternalEntityTemplate;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DirectoryImplTest
{
    private static final String LONG_STRING = StringUtils.repeat("X", 300);

    @Test(expected=IllegalArgumentException.class)
    public void testDirectoryImpl()
    {
        new DirectoryImpl(new InternalEntityTemplate(0L, LONG_STRING, true, null, null));
    }

    @Test
    public void testUpdateAttributesFrom()
    {
        final DirectoryImpl directory = new DirectoryImpl();
        directory.updateAttributesFrom(ImmutableMap.of("k1", "v1", "k2", "v2"));
        assertEquals(ImmutableMap.of("k1", "v1", "k2", "v2"), directory.getAttributes());
        directory.updateAttributesFrom(ImmutableMap.of("k2", "v2a", "k3", "v3"));
        assertEquals(ImmutableMap.of("k2", "v2a", "k3", "v3"), directory.getAttributes());
    }
}
