package com.atlassian.crowd.integration.rest.entity;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MultiValuedAttributeEntityListTest
{
    @Test
    public void testGetValue() throws Exception
    {
        final MultiValuedAttributeEntityList attributes = new MultiValuedAttributeEntityList(Collections.singletonList(new MultiValuedAttributeEntity("key", Collections.singletonList("value"))));

        assertEquals("value", attributes.getValue("key"));
    }
    @Test
    public void testGetValue_EmptyAttributes() throws Exception
    {
        final MultiValuedAttributeEntityList attributes = new MultiValuedAttributeEntityList(Collections.<MultiValuedAttributeEntity>emptyList());

        assertNull(attributes.getValue("key"));
        assertNull(attributes.getValue(null));
    }

    @Test
    public void testGetValue_NullValues() throws Exception
    {
        final MultiValuedAttributeEntityList attributes = new MultiValuedAttributeEntityList(Collections.singletonList(new MultiValuedAttributeEntity("key", null)));

        assertNull(attributes.getValue("key"));
        assertNull(attributes.getValue(null));
    }

    @Test
    public void testGetValue_EmptyValues() throws Exception
    {
        final MultiValuedAttributeEntityList attributes = new MultiValuedAttributeEntityList(Collections.singletonList(new MultiValuedAttributeEntity("key", Collections.<String>emptyList())));

        assertNull(attributes.getValue("key"));
        assertNull(attributes.getValue(null));
    }
}
