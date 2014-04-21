package com.atlassian.crowd.embedded.core;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;

public class XmlFilteredGroupsProviderTest
{
    @Test
    public void testGetGroups()
    {
        Set<String> groups = readGroups("<filteredgroups><filteredgroup>__jira__</filteredgroup><filteredgroup>__confluence__</filteredgroup><filteredgroup>__bamboo__</filteredgroup></filteredgroups>");
        assertEquals(Sets.newHashSet("__jira__", "__confluence__", "__bamboo__"), groups);
    }

    @Test
    public void testGetGroupsNoFilteredGroups()
    {
        assertEquals(Sets.newHashSet(), readGroups("<filteredgroups></filteredgroups>"));
    }

    @Test
    public void testGetGroupsWhenFileDoesNotExist()
    {
        XmlFilteredGroupsProvider groupProvider = new XmlFilteredGroupsProvider(new NullXmlProvider());
        assertEquals(Sets.newHashSet(), groupProvider.getGroups());
    }

    @Test
    public void testReadFromActualClassPath()
    {
        XmlFilteredGroupsProvider groupProvider = new XmlFilteredGroupsProvider();
        assertEquals(Sets.newHashSet("test1", "test2"), groupProvider.getGroups());
    }

    private Set<String> readGroups(final String xmlContent)
    {
        XmlFilteredGroupsProvider groupProvider = new XmlFilteredGroupsProvider(new StringXmlProvider(xmlContent));
        return groupProvider.getGroups();
    }

    private static class NullXmlProvider implements XmlFilteredGroupsProvider.FilteredGroupsFileReader
    {
        public InputStream getStream()
        {
            return null;
        }
    }

    private static class StringXmlProvider implements XmlFilteredGroupsProvider.FilteredGroupsFileReader
    {
        private String xmlContent;

        private StringXmlProvider(String xmlContent)
        {
            this.xmlContent = xmlContent;
        }

        public InputStream getStream()
        {
            try
            {
                return new ByteArrayInputStream(xmlContent.getBytes("utf-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
