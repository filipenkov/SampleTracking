package com.atlassian.crowd.embedded.admin.directory;

import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LdapDirectoryAttributesTest
{
    @Test
    public void testToAttributesMapIncludesAutoAddGroups() throws Exception
    {
        LdapDirectoryAttributes attrs = new LdapDirectoryAttributes();

        attrs.setLdapAutoAddGroups("test-group");

        Map<String, String> map;
        attrs.setLdapAutoAddGroups("test-group");
        map = attrs.toAttributesMap();
        assertEquals("test-group", map.get(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));

        attrs.setLdapAutoAddGroups("test-group1|test-group2|test-group1");
        map = attrs.toAttributesMap();
        assertEquals("No conversion or normalising is performed",
            "test-group1|test-group2|test-group1", map.get(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS));
    }

    @Test
    public void testFromAttributesMapIncludesAutoAddGroups() throws Exception
    {
        Map<String, String> m;
        LdapDirectoryAttributes attrs;

        m = Collections.singletonMap(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS, "test-group");
        attrs = LdapDirectoryAttributes.fromAttributesMap(m);
        assertEquals("test-group", attrs.getLdapAutoAddGroups());

        m = Collections.singletonMap(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS, "test-group1|test-group2|test-group1");
        attrs = LdapDirectoryAttributes.fromAttributesMap(m);
        assertEquals("No conversion or normalising is performed",
            "test-group1|test-group2|test-group1", attrs.getLdapAutoAddGroups());
    }

    @Test
    public void testToAttributesMapIncludesIncrementalSyncEnabled() throws Exception
    {
        final LdapDirectoryAttributes attrs = new LdapDirectoryAttributes();

        assertFalse(Boolean.parseBoolean(attrs.toAttributesMap().get(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)));

        attrs.setIncrementalSyncEnabled(true);

        assertTrue(Boolean.parseBoolean(attrs.toAttributesMap().get(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)));
    }

    @Test
    public void testFromAttributesMapIncludesIncrementalSyncEnabled() throws Exception
    {
        assertFalse(LdapDirectoryAttributes.fromAttributesMap(Collections.<String, String>emptyMap()).isIncrementalSyncEnabled());

        final Map<String, String> map = Collections.singletonMap(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED, "true");

        assertTrue(LdapDirectoryAttributes.fromAttributesMap(map).isIncrementalSyncEnabled());
    }

    @Test
    public void testToAttributeMapIncludesAttributesWithEmptyValues() throws Exception
    {
        LdapDirectoryAttributes attributes = new LdapDirectoryAttributes();
        Map<String, String> attributesMap = attributes.toAttributesMap();
        assertThat(attributesMap.containsKey(LDAPPropertiesMapper.USER_GROUP_KEY), is(true));
    }
}
