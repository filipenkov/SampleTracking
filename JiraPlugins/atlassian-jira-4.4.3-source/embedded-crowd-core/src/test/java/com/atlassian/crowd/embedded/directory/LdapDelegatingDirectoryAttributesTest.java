package com.atlassian.crowd.embedded.directory;

import java.util.Collections;
import java.util.Map;

import com.atlassian.crowd.model.directory.DirectoryImpl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LdapDelegatingDirectoryAttributesTest
{
    @Test
    public void testToAttributesMapIncludesAutoAddGroups() throws Exception
    {
        LdapDelegatingDirectoryAttributes attrs = new LdapDelegatingDirectoryAttributes();
        
        attrs.setLdapAutoAddGroups("test-group");
        
        Map<String,String> map;
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
        LdapDelegatingDirectoryAttributes attrs;
        
        m = Collections.singletonMap(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS, "test-group");
        attrs = LdapDelegatingDirectoryAttributes.fromAttributesMap(m);
        assertEquals("test-group", attrs.getLdapAutoAddGroups());
        
        m = Collections.singletonMap(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS, "test-group1|test-group2|test-group1");
        attrs = LdapDelegatingDirectoryAttributes.fromAttributesMap(m);
        assertEquals("No conversion or normalising is performed",
                "test-group1|test-group2|test-group1", attrs.getLdapAutoAddGroups());
    }
}
